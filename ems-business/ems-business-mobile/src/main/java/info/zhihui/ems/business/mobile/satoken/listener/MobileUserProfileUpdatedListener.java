package info.zhihui.ems.business.mobile.satoken.listener;

import cn.dev33.satoken.session.SaSession;
import info.zhihui.ems.business.mobile.utils.MobileStpUtil;
import info.zhihui.ems.foundation.user.constants.LoginConstant;
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
        SaSession loginSession = MobileStpUtil.getStpLogic().getSessionByLoginId(event.getUserId(), false);
        if (loginSession == null) {
            return;
        }

        loginSession.set(LoginConstant.LOGIN_USER_REAL_NAME, event.getUserRealName());
        loginSession.set(LoginConstant.LOGIN_USER_PHONE, event.getUserPhone());
        log.debug("移动端用户会话数据已刷新, userId={}", event.getUserId());
    }
}
