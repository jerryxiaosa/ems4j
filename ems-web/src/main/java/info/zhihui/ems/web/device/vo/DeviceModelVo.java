package info.zhihui.ems.web.device.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * 设备型号分页展示对象。
 */
@Data
@Accessors(chain = true)
@Schema(name = "DeviceModelVo", description = "设备型号信息")
public class DeviceModelVo {

    @Schema(description = "设备型号ID")
    private Integer id;

    @Schema(description = "设备品类ID")
    private Integer typeId;

    @Schema(description = "设备品类标识")
    private String typeKey;

    @Schema(description = "制造商名称")
    private String manufacturerName;

    @Schema(description = "型号名称")
    private String modelName;

    @Schema(description = "产品唯一标识")
    private String productCode;

    @Schema(description = "型号配置")
    private Map<String, Object> modelProperty;
}
