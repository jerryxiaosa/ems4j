package info.zhihui.ems.web.device.vo;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 新增电表请求
 */
@Data
@Accessors(chain = true)
@Schema(name = "ElectricMeterCreateVo", description = "新增电表参数")
public class ElectricMeterCreateVo {

    @NotNull
    @Schema(description = "空间ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer spaceId;

    @NotBlank
    @Size(max = 20)
    @Schema(description = "电表名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String meterName;

    @Schema(description = "设备编号，设备上报标识")
    private String deviceNo;

    @Schema(description = "是否计量", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean isCalculate;

    @Schema(description = "计量类型编码")
    private Integer calculateType;

    @NotNull
    @Schema(description = "是否为预付费", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean isPrepay;

    @NotNull
    @Schema(description = "设备型号ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer modelId;

    @Schema(description = "网关ID")
    private Integer gatewayId;

    @Schema(description = "串口号")
    private Integer portNo;

    @Schema(description = "电表通讯地址")
    private Integer meterAddress;

    @Schema(description = "设备IMEI")
    private String imei;

    @Positive
    @Max(65535)
    @Schema(description = "CT变比")
    private Integer ct;
}
