package info.zhihui.ems.web.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 角色查询 VO
 */
@Data
@Schema(name = "RoleQueryVo", description = "角色查询条件")
public class RoleQueryVo {

    @Size(max = 64)
    @Schema(description = "角色名称模糊查询条件")
    private String roleNameLike;

    @Size(max = 64)
    @Schema(description = "角色标识模糊查询条件")
    private String roleKeyLike;

    @Schema(description = "是否系统内置 0否 1是")
    private Boolean isSystem;

    @Schema(description = "是否禁用 0否 1是")
    private Boolean isDisabled;
}