package info.zhihui.ems.web.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 菜单查询 VO
 */
@Data
@Schema(name = "MenuQueryVo", description = "菜单查询条件")
public class MenuQueryVo {

    @Schema(description = "菜单名称模糊查询条件")
    private String menuNameLike;

    @Schema(description = "菜单唯一标识")
    private String menuKey;

    @Schema(description = "父级菜单ID")
    private Integer pid;

    @Schema(description = "菜单来源编码，参考 menuSource")
    private Integer menuSource;

    @Schema(description = "菜单ID集合")
    private List<Integer> ids;

    @Schema(description = "排除的菜单ID集合")
    private List<Integer> excludeIds;
}
