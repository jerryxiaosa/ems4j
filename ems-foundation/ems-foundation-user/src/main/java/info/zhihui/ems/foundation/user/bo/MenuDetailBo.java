package info.zhihui.ems.foundation.user.bo;

import info.zhihui.ems.foundation.user.enums.MenuSourceEnum;
import info.zhihui.ems.foundation.user.enums.MenuTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 菜单详细内容对象
 */
@Data
@Accessors(chain = true)
public class MenuDetailBo {

    /**
     * 菜单主键
     */
    private Integer id;

    /**
     * 菜单名称
     */
    private String menuName;

    /**
     * 菜单唯一键
     */
    private String menuKey;

    /**
     * 父级菜单ID（0表示顶级）
     */
    private Integer pid;

    /**
     * 显示排序号
     */
    private Integer sortNum;

    /**
     * 前端访问路径
     */
    private String path;

    /**
     * 菜单来源
     */
    private MenuSourceEnum menuSource;

    /**
     * 菜单类型
     */
    private MenuTypeEnum menuType;

    /**
     * 菜单图标
     */
    private String icon;

    /**
     * 菜单备注
     */
    private String remark;

    /**
     * 是否隐藏 0否 1是
     */
    private Boolean isHidden;

    /**
     * 接口权限标识列表
     */
    private List<String> permissionCodes;
}