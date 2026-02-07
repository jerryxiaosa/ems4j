package info.zhihui.ems.foundation.user.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户资料更新事件
 *
 * @author jerryxiaosa
 */
@Getter
@AllArgsConstructor
public class UserProfileUpdatedEvent {

    private final Integer userId;

    private final String userRealName;

    private final String userPhone;
}
