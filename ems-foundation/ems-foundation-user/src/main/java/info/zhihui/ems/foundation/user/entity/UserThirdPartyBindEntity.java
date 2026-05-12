package info.zhihui.ems.foundation.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import info.zhihui.ems.components.datasource.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 用户第三方身份绑定实体。
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@TableName("sys_user_third_party_bind")
public class UserThirdPartyBindEntity extends BaseEntity {

    private Integer id;

    /**
     * EMS 用户ID。
     */
    private Integer userId;

    /**
     * 第三方平台标识。
     */
    private String platform;

    /**
     * 第三方应用ID。
     */
    private String appId;

    /**
     * 第三方平台用户ID，例如微信 openId。
     */
    private String thirdPartyUserId;

    /**
     * 第三方平台联合用户ID，例如微信 unionId。
     */
    private String thirdPartyUnionId;

    /**
     * 第三方返回的手机号。
     */
    private String phone;

    /**
     * 最近一次登录时间。
     */
    private LocalDateTime lastLoginTime;
}
