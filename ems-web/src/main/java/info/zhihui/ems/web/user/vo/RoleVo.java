package info.zhihui.ems.web.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 角色信息 VO
 */
@Data
@Accessors(chain = true)
@Schema(name = "RoleVo", description = "角色信息数据")
public class RoleVo {

    @Schema(description = "角色ID")
    private Integer id;

    @Schema(description = "角色名称")
    private String roleName;

    @Schema(description = "角色唯一标识")
    private String roleKey;

    @Schema(description = "显示排序号")
    private Integer sortNum;

    @Schema(description = "角色备注")
    private String remark;

    @Schema(description = "是否系统内置 0否 1是")
    private Boolean isSystem;

    @Schema(description = "是否禁用 0否 1是")
    private Boolean isDisabled;
}