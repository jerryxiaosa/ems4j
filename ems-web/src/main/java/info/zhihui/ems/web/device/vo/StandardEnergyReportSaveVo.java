package info.zhihui.ems.web.device.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 标准电量上报参数
 */
@Data
@Schema(name = "StandardEnergyReportSaveVo", description = "标准电量上报参数")
public class StandardEnergyReportSaveVo {

    @Schema(description = "上报来源，默认STANDARD", example = "IOT")
    private String source;

    @NotBlank(message = "来源流水号不能为空")
    @Schema(description = "来源流水号", requiredMode = Schema.RequiredMode.REQUIRED, example = "202602110001")
    private String sourceReportId;

    @NotBlank(message = "设备编号不能为空")
    @Schema(description = "设备编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "DEV-0001")
    private String deviceNo;

    @NotNull(message = "抄表时间不能为空")
    @Schema(description = "抄表时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime recordTime;

    @NotNull(message = "总电量不能为空")
    @DecimalMin(value = "0", message = "总电量不能小于0")
    @Schema(description = "总电量", requiredMode = Schema.RequiredMode.REQUIRED, example = "123.45")
    private BigDecimal totalEnergy;

    @NotNull(message = "尖电量不能为空")
    @DecimalMin(value = "0", message = "尖电量不能小于0")
    @Schema(description = "尖电量", requiredMode = Schema.RequiredMode.REQUIRED, example = "10.11")
    private BigDecimal higherEnergy;

    @NotNull(message = "峰电量不能为空")
    @DecimalMin(value = "0", message = "峰电量不能小于0")
    @Schema(description = "峰电量", requiredMode = Schema.RequiredMode.REQUIRED, example = "20.22")
    private BigDecimal highEnergy;

    @NotNull(message = "平电量不能为空")
    @DecimalMin(value = "0", message = "平电量不能小于0")
    @Schema(description = "平电量", requiredMode = Schema.RequiredMode.REQUIRED, example = "30.33")
    private BigDecimal lowEnergy;

    @NotNull(message = "谷电量不能为空")
    @DecimalMin(value = "0", message = "谷电量不能小于0")
    @Schema(description = "谷电量", requiredMode = Schema.RequiredMode.REQUIRED, example = "40.44")
    private BigDecimal lowerEnergy;

    @NotNull(message = "深谷电量不能为空")
    @DecimalMin(value = "0", message = "深谷电量不能小于0")
    @Schema(description = "深谷电量", requiredMode = Schema.RequiredMode.REQUIRED, example = "22.35")
    private BigDecimal deepLowEnergy;
}
