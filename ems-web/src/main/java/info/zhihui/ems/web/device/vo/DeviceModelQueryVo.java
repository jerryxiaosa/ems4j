package info.zhihui.ems.web.device.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 设备型号查询条件。
 */
@Data
@Accessors(chain = true)
@Schema(name = "DeviceModelQueryVo", description = "设备型号查询条件")
public class DeviceModelQueryVo {

    @Schema(description = "设备品类ID列表")
    private List<Integer> typeIds;

    @Schema(description = "设备品类标识")
    private String typeKey;

    @Schema(description = "制造商名称")
    private String manufacturerName;

    @Schema(description = "型号名称")
    private String modelName;

    @Schema(description = "产品唯一标识")
    private String productCode;
}
