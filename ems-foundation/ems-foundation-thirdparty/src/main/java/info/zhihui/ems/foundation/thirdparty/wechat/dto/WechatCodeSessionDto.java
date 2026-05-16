package info.zhihui.ems.foundation.thirdparty.wechat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 微信登录凭证校验响应。
 */
@Data
@Accessors(chain = true)
public class WechatCodeSessionDto {
    /**
     * 用户在当前小程序下的唯一标识，成功响应必须返回。
     */
    private String openid;

    /**
     * 微信会话密钥。当前登录流程不解密敏感数据，但保留该字段便于后续扩展。
     */
    @JsonProperty("session_key")
    private String sessionKey;

    /**
     * 用户在开放平台下的统一标识。小程序未绑定开放平台时微信不会返回该字段。
     */
    private String unionid;

    /**
     * 错误码。微信成功响应通常不返回 errcode，空值应按成功处理。
     */
    private Integer errcode;

    /**
     * 错误信息。微信成功响应通常不返回 errmsg。
     */
    private String errmsg;
}
