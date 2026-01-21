package info.zhihui.ems.iot.domain.command.concrete;

import info.zhihui.ems.iot.domain.command.DeviceCommandRequest;
import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 获取日期方案命令。
 */
@Data
@Accessors(chain = true)
public class GetDatePlanCommand implements DeviceCommandRequest {

    private Integer plan;

    @Override
    public DeviceCommandTypeEnum type() {
        return DeviceCommandTypeEnum.GET_DATE_PLAN;
    }
}
