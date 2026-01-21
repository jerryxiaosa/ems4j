package info.zhihui.ems.web.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

/**
 * 系统配置查询条件 VO
 */
@Data
@Schema(name = "ConfigQueryVo", description = "系统配置查询条件")
public class ConfigQueryVo {

    @Schema(description = "配置ID集合")
    private Set<Integer> ids;

    @Size(max = 64)
    @Schema(description = "配置模块名称")
    private String configModuleName;

    @Size(max = 64)
    @Schema(description = "配置键模糊查询条件")
    private String configKeyLike;

    @Size(max = 128)
    @Schema(description = "配置名称模糊查询条件")
    private String configNameLike;

    @Schema(description = "是否系统内置配置")
    private Boolean isSystem;
}
