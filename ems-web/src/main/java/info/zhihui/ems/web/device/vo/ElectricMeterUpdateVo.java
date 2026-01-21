package info.zhihui.ems.web.device.vo;

import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 更新电表请求
 */
@Data
@Schema(name = "ElectricMeterUpdateVo", description = "更新电表参数")
public class ElectricMeterUpdateVo {

    @Schema(description = "空间ID")
    private Integer spaceId;

    @Size(max = 20)
    @Schema(description = "电表名称")
    private String meterName;

    @Schema(description = "设备编号，设备上报标识")
    private String deviceNo;

    @Schema(description = "是否计量")
    private Boolean isCalculate;

    @Schema(description = "计量类型编码")
    private Integer calculateType;

    @Schema(description = "是否为预付费")
    private Boolean isPrepay;
}
