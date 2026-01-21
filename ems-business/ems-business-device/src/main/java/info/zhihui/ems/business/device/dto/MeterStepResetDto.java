package info.zhihui.ems.business.device.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 重建电表阶梯记录的数据载体
 */
@Data
@Accessors(chain = true)
public class MeterStepResetDto {

    /**
     * 电表ID
     */
    @NotNull
    private Integer meterId;

}
