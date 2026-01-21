package info.zhihui.ems.business.device.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class MeterOpenDetailDto {

    /**
     * 表id
     */
    @NotNull(message = "电表ID不能为空")
    private Integer meterId;

}
