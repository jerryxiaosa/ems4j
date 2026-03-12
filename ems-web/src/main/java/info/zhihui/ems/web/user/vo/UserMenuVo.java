package info.zhihui.ems.web.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 用户菜单详情 VO
 */
@Data
@Accessors(chain = true)
@Schema(name = "UserMenuVo", description = "用户菜单详情")
public class UserMenuVo {

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

    @Schema(description = "菜单类型，参考menuType")
    private Integer menuType;

    @Schema(description = "菜单图标")
    private String icon;

    @Schema(description = "是否隐藏")
    private Boolean hidden;

    @Schema(description = "菜单来源，参考menuSource")
    private Integer menuSource;

}
