package info.zhihui.ems.foundation.user.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 用户第三方身份绑定业务对象。
 */
@Data
@Accessors(chain = true)
public class UserThirdPartyBindBo {
    private Integer id;
    private Integer userId;
    private String platform;
    private String appId;
    private String thirdPartyUserId;
    private String thirdPartyUnionId;
    private String phone;
    private LocalDateTime lastLoginTime;
}
