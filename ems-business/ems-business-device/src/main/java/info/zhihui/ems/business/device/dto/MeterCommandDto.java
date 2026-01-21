package info.zhihui.ems.business.device.dto;

import info.zhihui.ems.common.enums.OwnerTypeEnum;
import info.zhihui.ems.business.device.bo.ElectricMeterBo;
import info.zhihui.ems.foundation.integration.biz.command.enums.CommandSourceEnum;
import info.zhihui.ems.foundation.integration.biz.command.enums.CommandTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
public class MeterCommandDto {
    private ElectricMeterBo meter;
    private OwnerTypeEnum ownerType;
    private CommandTypeEnum commandType;
    private String commandData;
    private CommandSourceEnum commandSource;
}
