package info.zhihui.ems.business.device.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 电表开合闸状态同步参数
 *
 * <p>用于从物联网平台拉取真实的开合闸状态后回写本地状态。</p>
 */
@Data
@Accessors(chain = true)
public class ElectricMeterSwitchStatusSyncDto {

    /**
     * 电表ID
     */
    @NotNull(message = "电表ID不能为空")
    private Integer meterId;

}
