package info.zhihui.ems.web.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 系统配置 VO
 */
@Data
@Accessors(chain = true)
@Schema(name = "ConfigVo", description = "系统配置信息")
public class ConfigVo {

    @Schema(description = "配置ID")
    private Integer id;

    @Schema(description = "配置所属模块名称")
    private String configModuleName;

    @Schema(description = "配置键")
    private String configKey;

    @Schema(description = "配置名称")
    private String configName;

    @Schema(description = "配置值")
    private String configValue;

    @Schema(description = "是否为系统内置配置")
    private Boolean isSystem;
}
