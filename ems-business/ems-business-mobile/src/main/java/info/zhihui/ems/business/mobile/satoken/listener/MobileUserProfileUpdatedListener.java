package info.zhihui.ems.business.mobile.satoken.listener;

import cn.dev33.satoken.exception.NotLoginException;
import info.zhihui.ems.business.mobile.utils.MobileStpUtil;
import info.zhihui.ems.foundation.user.event.UserProfileUpdatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 移动端用户资料更新事件监听器。
 */
@Slf4j
@Component
public class MobileUserProfileUpdatedListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserProfileUpdated(UserProfileUpdatedEvent event) {
        try {
            MobileStpUtil.getStpLogic().logout(event.getUserId());
        } catch (NotLoginException ignore) {
            // 用户没有移动端登录态时无需处理
        } catch (Exception e) {
            log.warn("用户资料更新后强制移动端用户下线异常, userId={}", event.getUserId(), e);
        }
    }
}
