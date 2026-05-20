package info.zhihui.ems.business.mobile.satoken.listener;

import cn.dev33.satoken.exception.NotLoginException;
import info.zhihui.ems.business.mobile.utils.MobileStpUtil;
import info.zhihui.ems.foundation.user.event.UserDeletedEvent;
import info.zhihui.ems.foundation.user.event.UserPasswordResetEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 移动端用户登录态清理监听器。
 */
@Slf4j
@Component
public class MobileUserLoginStateCleanupListener {

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
            MobileStpUtil.getStpLogic().logout(userId);
        } catch (NotLoginException ignore) {
            // ignore when user is not logged in on mobile
        } catch (Exception e) {
            log.warn("强制移动端用户下线异常, userId={}", userId, e);
        }
    }
}
