package info.zhihui.ems.business.finance.enums;

import info.zhihui.ems.common.enums.CodeEnum;
import lombok.Getter;

/**
 * @author jerryxiaosa
 *
 * 支付渠道枚举
 */
@Getter
public enum PaymentChannelEnum implements CodeEnum<String> {
    OFFLINE("OFFLINE", "线下支付"),
    WX_MINI("WX_MINI", "微信小程序");

    private final String code;

    private final String info;

    PaymentChannelEnum(String code, String info) {
        this.code = code;
        this.info = info;
    }
}
