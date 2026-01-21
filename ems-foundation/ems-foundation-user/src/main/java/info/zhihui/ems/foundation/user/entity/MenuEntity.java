package info.zhihui.ems.foundation.user.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import info.zhihui.ems.components.datasource.entity.BaseEntity;
import info.zhihui.ems.foundation.user.enums.MenuSourceEnum;
import info.zhihui.ems.foundation.user.enums.MenuTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 菜单实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
public class MenuEntity extends BaseEntity {

    /**
     * 菜单主键
     */
    @TableId
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
     * 菜单来源：1 web, 2 mobile
     */
    private Integer menuSource;

    /**
     * 菜单类型：1 菜单, 2 按钮
     */
    private Integer menuType;

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

}