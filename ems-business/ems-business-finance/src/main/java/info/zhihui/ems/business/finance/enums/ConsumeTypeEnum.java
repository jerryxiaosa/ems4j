package info.zhihui.ems.business.finance.enums;

import info.zhihui.ems.common.enums.CodeEnum;
import lombok.Getter;

/**
 * 消费类型
 */
@Getter
public enum ConsumeTypeEnum implements CodeEnum<Integer> {
    MONTHLY(0, "包月消费"),
    ELECTRIC(1, "用电消费"),
    CORRECTION(2, "补正消费");

    private final Integer code;

    private final String info;

    ConsumeTypeEnum(Integer code, String info) {
        this.code = code;
        this.info = info;
    }
}
