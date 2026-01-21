package info.zhihui.ems.foundation.user.dto;

import info.zhihui.ems.foundation.user.enums.MenuSourceEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 菜单查询列表DTO
 */
@Data
@Accessors(chain = true)
public class MenuQueryDto {

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
    private MenuSourceEnum menuSource;

    /**
     * 菜单ID列表
     */
    private List<Integer> ids;

    /**
     * 排除的菜单ID列表
     */
    private List<Integer> excludeIds;
}
