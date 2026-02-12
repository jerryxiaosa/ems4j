package info.zhihui.ems.foundation.user.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户密码重置事件
 */
@Getter
@AllArgsConstructor
public class UserPasswordResetEvent {

    private final Integer userId;
}
