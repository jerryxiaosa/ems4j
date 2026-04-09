package info.zhihui.ems.business.report.enums;

import info.zhihui.ems.common.enums.CodeEnum;
import lombok.Getter;

@Getter
public enum ReportTriggerTypeEnum implements CodeEnum<Integer> {
    SCHEDULED(0, "定时任务"),
    MANUAL(1, "手工触发");

    private final Integer code;

    private final String info;

    ReportTriggerTypeEnum(Integer code, String info) {
        this.code = code;
        this.info = info;
    }
}
