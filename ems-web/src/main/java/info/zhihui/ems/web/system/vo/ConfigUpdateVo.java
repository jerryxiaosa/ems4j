package info.zhihui.ems.web.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 系统配置更新 VO
 */
@Data
@Accessors(chain = true)
@Schema(name = "ConfigUpdateVo", description = "系统配置更新参数")
public class ConfigUpdateVo {

    @NotBlank
    @Size(max = 64)
    @Schema(description = "配置键", requiredMode = Schema.RequiredMode.REQUIRED)
    private String configKey;

    @Size(max = 128)
    @Schema(description = "配置名称")
    private String configName;

    @Schema(description = "配置值")
    private String configValue;
}
