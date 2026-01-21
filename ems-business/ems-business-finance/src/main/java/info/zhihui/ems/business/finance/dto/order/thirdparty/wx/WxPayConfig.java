package info.zhihui.ems.business.finance.dto.order.thirdparty.wx;

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
