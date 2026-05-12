package info.zhihui.ems.foundation.thirdparty.wechat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 微信手机号响应。
 */
@Data
@Accessors(chain = true)
public class WechatPhoneNumberDto {

    @JsonProperty("phone_info")
    private PhoneInfo phoneInfo;

    private Integer errcode;

    private String errmsg;

    @Data
    @Accessors(chain = true)
    public static class PhoneInfo {
        private String phoneNumber;
        private String purePhoneNumber;
        private String countryCode;
        private Watermark watermark;
    }

    @Data
    @Accessors(chain = true)
    public static class Watermark {
        private Long timestamp;
        private String appid;
    }
}
