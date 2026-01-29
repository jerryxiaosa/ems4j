package info.zhihui.ems.iot.domain.command.concrete;

import info.zhihui.ems.iot.domain.command.DeviceCommandRequest;
import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 设置日期方案命令。
 */
@Data
@Accessors(chain = true)
public class SetDatePlanCommand implements DeviceCommandRequest {

    private List<DatePlanItem> items;

    @Override
    public DeviceCommandTypeEnum type() {
        return DeviceCommandTypeEnum.SET_DATE_PLAN;
    }

    @Override
    public void validate() {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("日期方案不能为空");
        }
    }
}
