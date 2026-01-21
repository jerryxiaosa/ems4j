package info.zhihui.ems.business.account.enums;

import info.zhihui.ems.common.enums.CodeEnum;
import lombok.Getter;

/**
 * @author jerryxiaosa
 * 销户结算类型
 */
@Getter
public enum CleanBalanceTypeEnum implements CodeEnum<Integer> {
    // 无处理
    SKIP(0),

    // 退费
    REFUND(1),

    // 补缴
    PAY(2),
    ;

    private final Integer code;

    CleanBalanceTypeEnum(Integer code) {
        this.code = code;
    }
}
