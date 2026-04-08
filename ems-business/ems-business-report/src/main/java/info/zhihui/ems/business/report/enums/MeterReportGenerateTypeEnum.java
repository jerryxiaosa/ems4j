package info.zhihui.ems.business.report.enums;

import info.zhihui.ems.common.enums.CodeEnum;
import lombok.Getter;

@Getter
public enum MeterReportGenerateTypeEnum implements CodeEnum<Integer> {
    NORMAL(0, "正常"),
    ZERO(1, "零用电"),
    CANCEL(2, "销户");

    private final Integer code;

    private final String info;

    MeterReportGenerateTypeEnum(Integer code, String info) {
        this.code = code;
        this.info = info;
    }
}
