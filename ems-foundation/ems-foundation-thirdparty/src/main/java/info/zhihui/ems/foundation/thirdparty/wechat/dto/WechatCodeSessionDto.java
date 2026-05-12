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
    private String openid;

    @JsonProperty("session_key")
    private String sessionKey;

    private String unionid;

    private Integer errcode;

    private String errmsg;
}
