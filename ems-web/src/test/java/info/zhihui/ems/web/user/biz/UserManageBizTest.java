package info.zhihui.ems.web.user.biz;

import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.foundation.user.bo.UserBo;
import info.zhihui.ems.foundation.user.service.UserService;
import info.zhihui.ems.web.user.mapstruct.UserManageWebMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserManageBizTest {

    @InjectMocks
    private UserManageBiz userManageBiz;

    @Mock
    private UserService userService;

    @Mock
    private UserManageWebMapper userManageWebMapper;

    @Test
    @DisplayName("删除用户_admin账号应禁止删除")
    void testDeleteUser_WhenUserNameIsAdmin_ShouldThrowException() {
        when(userService.getUserInfo(1)).thenReturn(new UserBo().setId(1).setUserName("admin"));

        assertThrows(BusinessRuntimeException.class, () -> userManageBiz.deleteUser(1));

        verify(userService).getUserInfo(1);
        verify(userService, never()).delete(1);
    }

    @Test
    @DisplayName("删除用户_普通账号应继续调用删除")
    void testDeleteUser_WhenUserNameIsNotAdmin_ShouldDelete() {
        when(userService.getUserInfo(2)).thenReturn(new UserBo().setId(2).setUserName("tester"));

        userManageBiz.deleteUser(2);

        verify(userService).getUserInfo(2);
        verify(userService).delete(2);
    }
}
