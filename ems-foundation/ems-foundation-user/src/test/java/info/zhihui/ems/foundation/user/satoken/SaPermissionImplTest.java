package info.zhihui.ems.foundation.user.satoken;

import info.zhihui.ems.foundation.user.bo.RoleSimpleBo;
import info.zhihui.ems.foundation.user.bo.UserBo;
import info.zhihui.ems.foundation.user.service.RoleService;
import info.zhihui.ems.foundation.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * SaPermissionImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SaPermissionImpl 单元测试")
class SaPermissionImplTest {

    @Mock
    private UserService userService;

    @Mock
    private RoleService roleService;

    @InjectMocks
    private SaPermissionImpl saPermission;

    @Test
    @DisplayName("getPermissionList 过滤禁用角色，仅返回有效权限")
    void testGetPermissionList_FilterDisabledRoles() {
        UserBo user = new UserBo().setId(1).setRoles(List.of(
                new RoleSimpleBo().setId(10).setRoleKey("enabled-role").setIsDisabled(false),
                new RoleSimpleBo().setId(11).setRoleKey("disabled-role").setIsDisabled(true),
                new RoleSimpleBo().setId(12).setRoleKey("no-flag-role").setIsDisabled(null)
        ));
        when(userService.getUserInfo(1)).thenReturn(user);

        when(roleService.getRolePermissions(10)).thenReturn(List.of("menu:view", "menu:edit"));
        when(roleService.getRolePermissions(12)).thenReturn(List.of("menu:edit", "menu:delete"));

        List<String> permissions = saPermission.getPermissionList(1, "web");

        assertThat(permissions).containsExactlyInAnyOrder("menu:view", "menu:edit", "menu:delete");
        verify(roleService).getRolePermissions(10);
        verify(roleService).getRolePermissions(12);
        verify(roleService, never()).getRolePermissions(eq(11));
    }

    @Test
    @DisplayName("getPermissionList 所有角色禁用返回空集合")
    void testGetPermissionList_AllRolesDisabled() {
        UserBo user = new UserBo().setId(2).setRoles(List.of(
                new RoleSimpleBo().setId(20).setRoleKey("disabled1").setIsDisabled(true),
                new RoleSimpleBo().setId(21).setRoleKey("disabled2").setIsDisabled(true)
        ));
        when(userService.getUserInfo(2)).thenReturn(user);

        List<String> permissions = saPermission.getPermissionList(2, "web");

        assertThat(permissions).isEmpty();
        verify(roleService, never()).getRolePermissions(any());
    }

    @Test
    @DisplayName("getPermissionList 用户无角色返回空集合")
    void testGetPermissionList_NoRoles() {
        UserBo user = new UserBo().setId(3).setRoles(Collections.emptyList());
        when(userService.getUserInfo(3)).thenReturn(user);

        List<String> permissions = saPermission.getPermissionList(3, "web");

        assertThat(permissions).isEmpty();
        verify(roleService, never()).getRolePermissions(any());
    }

    @Test
    @DisplayName("getPermissionList loginId为null返回空集合")
    void testGetPermissionList_LoginIdNull() {
        assertThrows(info.zhihui.ems.common.exception.BusinessRuntimeException.class,
                () -> saPermission.getPermissionList(null, "web"));

        verify(userService, never()).getUserInfo(anyInt());
    }

    @Test
    @DisplayName("getPermissionList 用户不存在返回空集合")
    void testGetPermissionList_UserNotFound() {
        when(userService.getUserInfo(99)).thenThrow(new info.zhihui.ems.common.exception.NotFoundException("用户不存在"));

        assertThrows(info.zhihui.ems.common.exception.NotFoundException.class,
                () -> saPermission.getPermissionList("99", "web"));
    }

    @Test
    @DisplayName("getRoleList 使用loginId参数返回角色列表")
    void testGetRoleList_ByLoginId() {
        UserBo user = new UserBo().setId(4).setRoles(List.of(
                new RoleSimpleBo().setId(40).setRoleKey("role-a"),
                new RoleSimpleBo().setId(41).setRoleKey("role-b")
        ));
        when(userService.getUserInfo(4)).thenReturn(user);

        List<String> roles = saPermission.getRoleList(4L, "web");

        assertThat(roles).containsExactlyInAnyOrder("role-a", "role-b");
    }
}
