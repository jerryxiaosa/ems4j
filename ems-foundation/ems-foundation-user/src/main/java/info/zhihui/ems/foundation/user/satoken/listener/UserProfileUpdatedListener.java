package info.zhihui.ems.foundation.user.satoken.listener;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import info.zhihui.ems.foundation.user.constants.LoginConstant;
import info.zhihui.ems.foundation.user.event.UserProfileUpdatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 用户资料更新事件监听器
 * 用于同步在线会话中的用户基础信息
 *
 * @author jerryxiaosa
 */
@Slf4j
@Component
public class UserProfileUpdatedListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserProfileUpdated(UserProfileUpdatedEvent event) {
        SaSession loginSession = StpUtil.getSessionByLoginId(event.getUserId(), false);
        if (loginSession == null) {
            return;
        }

        loginSession.set(LoginConstant.LOGIN_USER_REAL_NAME, event.getUserRealName());
        loginSession.set(LoginConstant.LOGIN_USER_PHONE, event.getUserPhone());
        log.debug("用户会话数据已刷新, userId={}", event.getUserId());
    }
}
