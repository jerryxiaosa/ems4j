package info.zhihui.ems.business.order.dto.thirdparty.wx;

import lombok.Data;

@Data
public class WxPayConfig {
    private String merchantId;
    private String appId;
    private String merchantSerialNumber;
    private String privateKeyPath;
    private String wechatPayCertificatePath;
    private String apiV3Key;
    private String notifyUrl;
}
