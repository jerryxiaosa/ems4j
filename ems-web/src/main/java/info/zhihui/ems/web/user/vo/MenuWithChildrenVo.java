package info.zhihui.ems.web.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 菜单树结构信息 VO
 */
@Data
@Accessors(chain = true)
@Schema(name = "MenuWithChildrenVo", description = "菜单树结构信息")
public class MenuWithChildrenVo {

    @Schema(description = "菜单ID")
    private Integer id;

    @Schema(description = "菜单名称")
    private String menuName;

    @Schema(description = "菜单唯一标识")
    private String menuKey;

    @Schema(description = "父级菜单ID")
    private Integer pid;

    @Schema(description = "菜单排序号")
    private Integer sortNum;

    @Schema(description = "路由路径")
    private String path;

    @Schema(description = "菜单来源，参考menuSource")
    private String menuSource;

    @Schema(description = "菜单类型，参考menuType")
    private String menuType;

    @Schema(description = "菜单图标")
    private String icon;

    @Schema(description = "菜单备注")
    private String remark;

    @Schema(description = "是否隐藏")
    private Boolean hidden;

    @Schema(description = "子菜单列表")
    private List<MenuWithChildrenVo> children;
}
