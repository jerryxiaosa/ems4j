package info.zhihui.ems.web.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 角色详情 VO
 */
@Data
@Accessors(chain = true)
@Schema(name = "RoleDetailVo", description = "角色详情数据")
public class RoleDetailVo {

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

    @Schema(description = "关联的菜单ID列表")
    private List<Integer> menuIds;

    @Schema(description = "权限列表")
    private List<String> permissions;
}