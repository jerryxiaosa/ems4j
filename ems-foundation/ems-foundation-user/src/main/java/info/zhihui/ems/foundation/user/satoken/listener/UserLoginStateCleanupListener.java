package info.zhihui.ems.foundation.user.satoken.listener;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.StpUtil;
import info.zhihui.ems.components.redis.utils.RedisUtil;
import info.zhihui.ems.foundation.user.constants.LoginConstant;
import info.zhihui.ems.foundation.user.event.UserDeletedEvent;
import info.zhihui.ems.foundation.user.event.UserPasswordResetEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 用户登录态清理监听器
 */
@Slf4j
@Component
public class UserLoginStateCleanupListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserDeleted(UserDeletedEvent event) {
        clearLoginState(event.getUserId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserPasswordReset(UserPasswordResetEvent event) {
        clearLoginState(event.getUserId());
    }

    private void clearLoginState(Integer userId) {
        try {
            RedisUtil.deleteObject(LoginConstant.PWD_ERR + userId);
        } catch (Exception e) {
            log.warn("清理登录失败计数异常, userId={}", userId, e);
        }

        try {
            StpUtil.logout(userId);
        } catch (NotLoginException ignore) {
            // ignore when user is not logged in
        } catch (Exception e) {
            log.warn("强制用户下线异常, userId={}", userId, e);
        }
    }
}
