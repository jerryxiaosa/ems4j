package info.zhihui.ems.business.report.enums;

import info.zhihui.ems.common.enums.CodeEnum;
import lombok.Getter;

@Getter
public enum ReportJobStatusEnum implements CodeEnum<Integer> {
    RUNNING(0, "运行中"),
    SUCCESS(1, "成功"),
    FAILED(2, "失败");

    private final Integer code;

    private final String info;

    ReportJobStatusEnum(Integer code, String info) {
        this.code = code;
        this.info = info;
    }
}
