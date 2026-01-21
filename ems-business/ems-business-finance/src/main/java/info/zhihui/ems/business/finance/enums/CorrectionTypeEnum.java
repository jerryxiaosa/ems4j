package info.zhihui.ems.business.finance.enums;

import info.zhihui.ems.common.enums.CodeEnum;
import lombok.Getter;

/**
 * @author jerryxiaosa
 * 补正类型
 */
@Getter
public enum CorrectionTypeEnum implements CodeEnum<Integer> {

    // 退费
    REFUND(1),

    // 补缴
    PAY(2),
    ;

    private final Integer code;

    CorrectionTypeEnum(Integer code) {
        this.code = code;
    }
}
