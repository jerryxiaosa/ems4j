package info.zhihui.ems.business.device.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
public class ElectricMeterCtDto {

    /**
     * 电表ID
     */
    @NotNull(message = "电表ID不能为空")
    private Integer meterId;

    /**
     * CT变比
     */
    @NotNull(message = "CT变比不能为空")
    @Positive(message = "CT变比必须大于0")
    @Max(value = 65535, message = "CT变比不能超过65535")
    private Integer ct;
}
