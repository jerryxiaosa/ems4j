package info.zhihui.ems.foundation.user.service.impl;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import cn.hutool.captcha.CircleCaptcha;
import cn.hutool.captcha.generator.CodeGenerator;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.exception.LoginException;
import info.zhihui.ems.components.redis.utils.RedisUtil;
import info.zhihui.ems.foundation.user.bo.MenuBo;
import info.zhihui.ems.foundation.user.bo.RoleSimpleBo;
import info.zhihui.ems.foundation.user.bo.UserBo;
import info.zhihui.ems.foundation.user.constants.LoginConstant;
import info.zhihui.ems.foundation.user.dto.CaptchaDto;
import info.zhihui.ems.foundation.user.dto.LoginRequestDto;
import info.zhihui.ems.foundation.user.dto.LoginResponseDto;
import info.zhihui.ems.foundation.user.dto.MenuQueryDto;
import info.zhihui.ems.foundation.user.entity.UserEntity;
import info.zhihui.ems.foundation.user.enums.MenuSourceEnum;
import info.zhihui.ems.foundation.user.enums.RoleEnum;
import info.zhihui.ems.foundation.user.qo.UserQueryQo;
import info.zhihui.ems.foundation.user.repository.UserRepository;
import info.zhihui.ems.foundation.user.service.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 用户登录服务接口
 *
 * @author jerryxiaosa
 */
@Service
@RequiredArgsConstructor
@Validated
@Slf4j
public class LoginServiceImpl implements LoginService {
    private final UserService userService;
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final MenuService menuService;
    private final PasswordService passwordService;
    private final CodeGenerator captchaGenerator;

    private static final Integer CAPTCHA_EXPIRATION = 1;

    @Override
    public CaptchaDto getCaptcha() {
        CaptchaDto captchaDto = new CaptchaDto();

        // 生成验证码
        CircleCaptcha circleCaptcha = new CircleCaptcha(110, 40);
        circleCaptcha.setGenerator(captchaGenerator);
        circleCaptcha.createCode();

        log.debug("生成的验证码是：{}", circleCaptcha.getCode());

        String captchaKey = IdUtil.simpleUUID();
        RedisUtil.setCacheObject(LoginConstant.CAPTCHA_CODE + captchaKey, circleCaptcha.getCode(), Duration.ofMinutes(CAPTCHA_EXPIRATION));
        captchaDto.setCaptchaKey(captchaKey);
        captchaDto.setImg(circleCaptcha.getImageBase64());
        return captchaDto;
    }

    @Override
    public LoginResponseDto login(@NotNull @Valid LoginRequestDto loginRequestDto) {
        validateCaptcha(loginRequestDto.getCaptchaKey(), loginRequestDto.getCaptchaValue());

        UserEntity user = getUserByAccount(loginRequestDto.getUserName());
        validatePassword(user, loginRequestDto.getPassword());

        UserBo userInfo = userService.getUserInfo(user.getId());
        if (CollectionUtils.isEmpty(userInfo.getRoles())) {
            throw LoginException.noPermission();
        }

        // web登录
        StpUtil.login(userInfo.getId(), new SaLoginParameter().setDeviceType(MenuSourceEnum.WEB.getInfo()));

        return new LoginResponseDto()
                .setAccessToken(StpUtil.getTokenValue())
                .setExpireIn(StpUtil.getTokenTimeout());
    }

    private void validateCaptcha(String captchaKey, String captchaValue) {
        if (captchaValue == null) {
            throw LoginException.captchaError();
        }
        String cacheKey = LoginConstant.CAPTCHA_CODE + captchaKey;
        String cacheValue = RedisUtil.getCacheObject(LoginConstant.CAPTCHA_CODE + captchaKey);
        if (cacheValue == null) {
            throw new BusinessRuntimeException("验证码已过期");
        }
        RedisUtil.deleteObject(cacheKey);
        if (!captchaGenerator.verify(cacheValue, captchaValue)) {
            throw LoginException.captchaError();
        }
    }

    private UserEntity getUserByAccount(String userName) {
        List<UserEntity> userList = userRepository.selectByQo(new UserQueryQo().setUserName(userName));
        if (CollectionUtils.isEmpty(userList)) {
            throw LoginException.passwordError();
        } else if (userList.size() > 1) {
            throw LoginException.accountRepeat();
        }

        return userList.get(0);
    }

    private void validatePassword(UserEntity user, String inputPassword) {
        String errorKey = LoginConstant.PWD_ERR + user.getId();
        // 重试次数限制
        int maxRetryCount = 20;
        int lockMinutes = 5;
        int errorNumber = ObjectUtil.defaultIfNull(RedisUtil.getCacheObject(errorKey), 0);
        if (errorNumber >= maxRetryCount) {
            throw LoginException.maxTry(lockMinutes);
        }
        if (!passwordService.matchesPassword(inputPassword, user.getPassword())) {
            // 错误次数递增
            errorNumber++;
            RedisUtil.setCacheObject(errorKey, errorNumber, Duration.ofMinutes(lockMinutes));
            // 达到规定错误次数 则锁定登录
            if (errorNumber >= maxRetryCount) {
                throw LoginException.maxTry(lockMinutes);
            } else {
                // 未达到规定错误次数
                throw LoginException.passwordError();
            }
        }
        // 登录成功 清空错误次数
        RedisUtil.deleteObject(errorKey);
    }

    @Override
    public void logout() {
        try {
            StpUtil.logout();
        } catch (NotLoginException ignore) {
        } catch (Exception e) {
            log.error("用户退出登录异常", e);
            throw new BusinessRuntimeException("退出登录异常");
        }
    }

    /**
     * 获取登录用户菜单
     * @param menuSource 菜单来源
     *
     * @return 菜单列表
     */
    @Override
    public List<MenuBo> getLoginUserMenus(@NotNull MenuSourceEnum menuSource) {
        int loginUserId;
        try {
            loginUserId = StpUtil.getLoginIdAsInt();
        } catch (NotLoginException e) {
            log.error("获取登录用户信息异常：{}", e.getMessage());
            throw new BusinessRuntimeException("获取登录用户信息异常：" + e.getMessage());
        } catch (Exception e) {
            log.error("获取登录用户信息异常：", e);
            throw new BusinessRuntimeException("获取登录用户信息异常：" + e.getMessage());
        }

        UserBo loginUser = userService.getUserInfo(loginUserId);
        List<RoleSimpleBo> roleSimpleBoList = loginUser.getRoles();
        List<MenuBo> menus = new ArrayList<>();

        if (CollectionUtils.isEmpty(roleSimpleBoList)) {
            return menus;
        }

        List<RoleSimpleBo> availableRoles = roleSimpleBoList.stream()
                .filter(role -> !Boolean.TRUE.equals(role.getIsDisabled()))
                .toList();
        if (CollectionUtils.isEmpty(availableRoles)) {
            return menus;
        }

        // 超级管理员拥有所有权限
        if (availableRoles.stream().anyMatch(r -> RoleEnum.SUPER_ADMIN.getCode().equals(r.getRoleKey()))) {
            menus = menuService.findList(new MenuQueryDto().setMenuSource(menuSource));
        } else {
            Set<Integer> menuIds = new HashSet<>();
            availableRoles.forEach(r -> menuIds.addAll(roleService.getDetail(r.getId()).getMenuIds()));
            if (!CollectionUtils.isEmpty(menuIds)) {
                menus = menuService.findList(new MenuQueryDto().setMenuSource(menuSource).setIds(new ArrayList<>(menuIds)));
            }
        }

        return menus;
    }
}
