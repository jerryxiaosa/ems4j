package info.zhihui.ems.web.device.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 网关新增请求
 */
@Data
@Accessors(chain = true)
@Schema(name = "GatewayAddVo", description = "网关新增参数")
public class GatewayAddVo {

    @NotNull
    @Schema(description = "空间ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer spaceId;

    @NotBlank
    @Size(max = 50)
    @Schema(description = "网关名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String gatewayName;

    @NotNull
    @Schema(description = "型号ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer modelId;

    @NotBlank
    @Schema(description = "设备编号，设备上报标识", requiredMode = Schema.RequiredMode.REQUIRED)
    private String deviceNo;

    @Schema(description = "网关序列号")
    private String sn;

    @Schema(description = "网关IMEI")
    private String imei;

    @NotBlank
    @Schema(description = "网关配置信息JSON", requiredMode = Schema.RequiredMode.REQUIRED)
    private String configInfo;

    @Schema(description = "备注")
    private String remark;
}
