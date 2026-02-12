package info.zhihui.ems.foundation.user.satoken.listener;

import cn.dev33.satoken.stp.StpUtil;
import info.zhihui.ems.components.redis.utils.RedisUtil;
import info.zhihui.ems.foundation.user.constants.LoginConstant;
import info.zhihui.ems.foundation.user.event.UserDeletedEvent;
import info.zhihui.ems.foundation.user.event.UserPasswordResetEvent;
import cn.dev33.satoken.exception.NotLoginException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mockStatic;

@DisplayName("用户登录态清理监听器测试")
class UserLoginStateCleanupListenerTest {

    private final UserLoginStateCleanupListener listener = new UserLoginStateCleanupListener();

    @Test
    @DisplayName("密码重置事件-应清理失败计数并强制下线")
    void testOnUserPasswordReset_ShouldClearLoginState() {
        try (MockedStatic<RedisUtil> redisMock = mockStatic(RedisUtil.class);
             MockedStatic<StpUtil> stpMock = mockStatic(StpUtil.class)) {
            redisMock.when(() -> RedisUtil.deleteObject(LoginConstant.PWD_ERR + 1)).thenReturn(true);

            listener.onUserPasswordReset(new UserPasswordResetEvent(1));

            redisMock.verify(() -> RedisUtil.deleteObject(LoginConstant.PWD_ERR + 1));
            stpMock.verify(() -> StpUtil.logout(1));
        }
    }

    @Test
    @DisplayName("用户删除事件-未登录异常应被忽略")
    void testOnUserDeleted_NotLoginException_ShouldIgnoreException() {
        try (MockedStatic<RedisUtil> redisMock = mockStatic(RedisUtil.class);
             MockedStatic<StpUtil> stpMock = mockStatic(StpUtil.class)) {
            redisMock.when(() -> RedisUtil.deleteObject(LoginConstant.PWD_ERR + 2)).thenReturn(true);
            stpMock.when(() -> StpUtil.logout(2))
                    .thenThrow(new NotLoginException("not login", "loginType", "token"));

            assertDoesNotThrow(() -> listener.onUserDeleted(new UserDeletedEvent(2)));

            redisMock.verify(() -> RedisUtil.deleteObject(LoginConstant.PWD_ERR + 2));
            stpMock.verify(() -> StpUtil.logout(2));
        }
    }

    @Test
    @DisplayName("用户删除事件-运行时异常应被忽略")
    void testOnUserDeleted_RuntimeException_ShouldIgnoreException() {
        try (MockedStatic<RedisUtil> redisMock = mockStatic(RedisUtil.class);
             MockedStatic<StpUtil> stpMock = mockStatic(StpUtil.class)) {
            redisMock.when(() -> RedisUtil.deleteObject(LoginConstant.PWD_ERR + 2)).thenReturn(true);
            stpMock.when(() -> StpUtil.logout(2))
                    .thenThrow(new RuntimeException("mock logout failure"));

            assertDoesNotThrow(() -> listener.onUserDeleted(new UserDeletedEvent(2)));

            redisMock.verify(() -> RedisUtil.deleteObject(LoginConstant.PWD_ERR + 2));
            stpMock.verify(() -> StpUtil.logout(2));
        }
    }
}
