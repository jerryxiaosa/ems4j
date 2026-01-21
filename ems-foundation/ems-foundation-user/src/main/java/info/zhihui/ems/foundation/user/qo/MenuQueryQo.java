package info.zhihui.ems.foundation.user.qo;

import info.zhihui.ems.foundation.user.enums.MenuSourceEnum;
import info.zhihui.ems.foundation.user.enums.MenuTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 菜单查询条件QO
 */
@Data
@Accessors(chain = true)
public class MenuQueryQo {

    /**
     * 菜单名称模糊查询
     */
    private String menuNameLike;

    /**
     * 菜单唯一键
     */
    private String menuKey;

    /**
     * 父级菜单ID
     */
    private Integer pid;

    /**
     * 菜单来源
     */
    private Integer menuSource;

    /**
     * 菜单ID列表
     */
    private List<Integer> ids;

    /**
     * 排除的菜单ID列表
     */
    private List<Integer> excludeIds;
}
