package info.zhihui.ems.foundation.user.satoken;

import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import info.zhihui.ems.foundation.user.bo.RoleSimpleBo;
import info.zhihui.ems.foundation.user.bo.UserBo;
import info.zhihui.ems.foundation.user.service.RoleService;
import info.zhihui.ems.foundation.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * sa-token 权限实现
 *
 * @author jerryxiaosa
 */
@Service
@Primary
@RequiredArgsConstructor
public class SaPermissionImpl implements StpInterface {

    private final UserService userService;
    private final RoleService roleService;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        UserBo user = userService.getUserInfo(StpUtil.getLoginIdAsInt());
        List<RoleSimpleBo> roles = user.getRoles();
        if (CollectionUtils.isEmpty(roles)) {
            return new ArrayList<>();
        }

        List<RoleSimpleBo> availableRoles = roles.stream()
                .filter(role -> !Boolean.TRUE.equals(role.getIsDisabled()))
                .toList();
        if (CollectionUtils.isEmpty(availableRoles)) {
            return new ArrayList<>();
        }

        Set<Integer> roleIds = availableRoles.stream()
                .map(RoleSimpleBo::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (CollectionUtils.isEmpty(roleIds)) {
            return new ArrayList<>();
        }

        return roleIds.stream()
                .map(roleService::getRolePermissions)
                .filter(list -> !CollectionUtils.isEmpty(list))
                .flatMap(List::stream)
                .distinct()
                .toList();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        UserBo user = userService.getUserInfo(StpUtil.getLoginIdAsInt());
        List<RoleSimpleBo> roles = user.getRoles();
        if (CollectionUtils.isEmpty(roles)) {
            return new ArrayList<>();
        }

        return roles.stream()
                .map(RoleSimpleBo::getRoleKey)
                .filter(StringUtils::hasLength)
                .toList();
    }
}
