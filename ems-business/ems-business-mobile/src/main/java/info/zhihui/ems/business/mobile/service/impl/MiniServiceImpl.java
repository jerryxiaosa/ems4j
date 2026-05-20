package info.zhihui.ems.business.mobile.service.impl;

import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import info.zhihui.ems.business.account.bo.AccountBo;
import info.zhihui.ems.business.account.dto.AccountElectricBalanceAggregateItemDto;
import info.zhihui.ems.business.account.dto.AccountQueryDto;
import info.zhihui.ems.business.account.service.AccountAdditionalInfoService;
import info.zhihui.ems.business.account.service.AccountInfoService;
import info.zhihui.ems.business.device.dto.ElectricMeterQueryDto;
import info.zhihui.ems.business.device.service.ElectricMeterInfoService;
import info.zhihui.ems.business.mobile.bo.MiniCurrentUserBo;
import info.zhihui.ems.business.mobile.bo.MiniLoginBo;
import info.zhihui.ems.business.mobile.bo.MiniLoginResultBo;
import info.zhihui.ems.business.mobile.service.MiniService;
import info.zhihui.ems.business.mobile.utils.MobileStpUtil;
import info.zhihui.ems.common.constant.ResultCode;
import info.zhihui.ems.common.enums.OwnerTypeEnum;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.components.context.RequestContext;
import info.zhihui.ems.foundation.thirdparty.wechat.dto.WechatMiniProgramLoginDto;
import info.zhihui.ems.foundation.thirdparty.wechat.service.WechatMiniProgramService;
import info.zhihui.ems.foundation.user.bo.UserBo;
import info.zhihui.ems.foundation.user.constants.LoginConstant;
import info.zhihui.ems.foundation.user.dto.UserThirdPartyBindDto;
import info.zhihui.ems.foundation.user.enums.MenuSourceEnum;
import info.zhihui.ems.foundation.user.enums.UserThirdPartyPlatformEnum;
import info.zhihui.ems.foundation.user.service.UserService;
import info.zhihui.ems.foundation.user.service.UserThirdPartyBindService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static info.zhihui.ems.common.utils.BigDecimalUtils.zeroIfNull;

/**
 * 小程序用户端服务实现。
 */
@Service
@Validated
@RequiredArgsConstructor
public class MiniServiceImpl implements MiniService {

    private final WechatMiniProgramService wechatMiniProgramService;
    private final UserService userService;
    private final AccountInfoService accountInfoService;
    private final UserThirdPartyBindService userThirdPartyBindService;
    private final RequestContext requestContext;
    private final AccountAdditionalInfoService accountAdditionalInfoService;
    private final ElectricMeterInfoService electricMeterInfoService;

    @Override
    public MiniLoginResultBo login(@NotNull @Valid MiniLoginBo loginBo) {
        WechatMiniProgramLoginDto wechatLogin = wechatMiniProgramService.resolveLogin(
                loginBo.getLoginCode(),
                loginBo.getPhoneCode()
        );
        UserBo user = getMiniLoginUserByPhone(wechatLogin.getPurePhoneNumber());
        AccountBo account = getMiniLoginAccount(user);
        bindThirdPartyIdentity(user, wechatLogin);
        loginUser(user, account);

        return new MiniLoginResultBo()
                .setAccessToken(MobileStpUtil.getTokenValue())
                .setExpireIn(MobileStpUtil.getTokenTimeout());
    }

    @Override
    public void logout() {
        MobileStpUtil.logout();
    }

    @Override
    public MiniCurrentUserBo getCurrentUser() {
        AccountBo account = accountInfoService.getById(requestContext.getAccountId());
        BigDecimal balance = getBalance(account);
        int meterCount = electricMeterInfoService.findList(new ElectricMeterQueryDto()
                .setAccountIds(List.of(account.getId()))).size();

        return new MiniCurrentUserBo()
                .setUserPhone(requestContext.getUserPhone())
                .setElectricAccountId(account.getId())
                .setElectricAccountName(account.getOwnerName())
                .setElectricAccountType(account.getElectricAccountType())
                .setBalance(balance)
                .setMeterCount(meterCount);
    }

    private UserBo getMiniLoginUserByPhone(String phoneNumber) {
        try {
            return userService.getUserByPhone(phoneNumber);
        } catch (NotFoundException | BusinessRuntimeException e) {
            throw new BusinessRuntimeException(
                    ResultCode.MOBILE_PHONE_NOT_BOUND.getCode(),
                    ResultCode.MOBILE_PHONE_NOT_BOUND.getMessage()
            );
        }
    }

    private void bindThirdPartyIdentity(UserBo user, WechatMiniProgramLoginDto wechatLogin) {
        userThirdPartyBindService.bindOrUpdate(new UserThirdPartyBindDto()
                .setUserId(user.getId())
                .setPlatform(UserThirdPartyPlatformEnum.WECHAT_MINI)
                .setAppId(wechatLogin.getAppId())
                .setThirdPartyUserId(wechatLogin.getOpenId())
                .setThirdPartyUnionId(wechatLogin.getUnionId())
                .setPhone(wechatLogin.getPurePhoneNumber()));
    }

    private AccountBo getMiniLoginAccount(UserBo user) {
        if (user.getOrganizationId() == null) {
            throw accountAbnormal();
        }
        List<AccountBo> accountList = accountInfoService.findList(new AccountQueryDto()
                .setOwnerType(OwnerTypeEnum.ENTERPRISE)
                .setOwnerIds(List.of(user.getOrganizationId())));
        if (CollectionUtils.isEmpty(accountList) || accountList.size() > 1 || accountList.get(0).getElectricAccountType() == null) {
            throw accountAbnormal();
        }
        return accountList.get(0);
    }

    private void loginUser(UserBo user, AccountBo account) {
        MobileStpUtil.login(user.getId(), new SaLoginParameter().setDeviceType(MenuSourceEnum.MOBILE.getInfo()));
        MobileStpUtil.getSession().set(LoginConstant.LOGIN_USER_REAL_NAME, user.getRealName());
        MobileStpUtil.getSession().set(LoginConstant.LOGIN_USER_PHONE, user.getUserPhone());
        MobileStpUtil.getSession().set(LoginConstant.LOGIN_ACCOUNT_ID, account.getId());
    }

    private BigDecimal getBalance(AccountBo account) {
        Map<Integer, BigDecimal> balanceMap = accountAdditionalInfoService.findElectricBalanceAmountMap(List.of(
                new AccountElectricBalanceAggregateItemDto()
                        .setAccountId(account.getId())
                        .setElectricAccountType(account.getElectricAccountType())
        ));
        return zeroIfNull(balanceMap.get(account.getId()));
    }

    private BusinessRuntimeException accountAbnormal() {
        return new BusinessRuntimeException(
                ResultCode.MOBILE_ACCOUNT_ABNORMAL.getCode(),
                ResultCode.MOBILE_ACCOUNT_ABNORMAL.getMessage()
        );
    }
}
