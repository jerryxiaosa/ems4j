package info.zhihui.ems.foundation.thirdparty.wechat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 微信接口调用凭证响应。
 */
@Data
@Accessors(chain = true)
public class WechatAccessTokenDto {
    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("expires_in")
    private Integer expiresIn;

    private Integer errcode;

    private String errmsg;
}
