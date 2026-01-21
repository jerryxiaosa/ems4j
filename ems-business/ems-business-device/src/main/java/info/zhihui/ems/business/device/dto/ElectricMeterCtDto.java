package info.zhihui.ems.business.device.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

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
    @DecimalMin(value = "0", inclusive = false, message = "CT变比必须大于0")
    private BigDecimal ct;
}
