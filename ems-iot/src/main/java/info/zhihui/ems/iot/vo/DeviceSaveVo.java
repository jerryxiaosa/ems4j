package info.zhihui.ems.iot.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(name = "DeviceSaveVo", description = "设备新增/更新请求参数")
public class DeviceSaveVo {

    @NotBlank(message = "设备编号不能为空")
    @Schema(description = "设备编号（设备上报标识）", requiredMode = Schema.RequiredMode.REQUIRED, example = "dev-10001")
    private String deviceNo;

    @Schema(description = "端口号（网关子设备场景）", example = "1")
    private Integer portNo;

    @Schema(description = "电表通讯地址", example = "1")
    private Integer meterAddress;

    @Schema(description = "设备密钥", example = "secret-abc123")
    private String deviceSecret;

    @Schema(description = "Modbus从站地址", example = "1")
    private Integer slaveAddress;

    @NotBlank(message = "产品编码不能为空")
    @Schema(description = "产品编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "acrel-4g")
    private String productCode;

    @Schema(description = "父设备ID（网关设备ID）", example = "1001")
    private Integer parentId;
}
