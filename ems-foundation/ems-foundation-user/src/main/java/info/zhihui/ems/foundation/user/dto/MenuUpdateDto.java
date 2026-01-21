package info.zhihui.ems.foundation.user.dto;

import info.zhihui.ems.foundation.user.enums.MenuSourceEnum;
import info.zhihui.ems.foundation.user.enums.MenuTypeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 菜单更新DTO
 */
@Data
@Accessors(chain = true)
public class MenuUpdateDto {

    /**
     * 菜单主键
     */
    @NotNull
    private Integer id;

    /**
     * 菜单名称
     */
    @NotBlank
    @Size(max = 64)
    private String menuName;

    /**
     * 菜单唯一键
     */
    @NotBlank
    @Size(max = 64)
    private String menuKey;

    /**
     * 显示排序号
     */
    private Integer sortNum;

    /**
     * 前端访问路径
     */
    @Size(max = 128)
    private String path;

    /**
     * 菜单来源
     */
    @NotNull
    private MenuSourceEnum menuSource;

    /**
     * 菜单类型
     */
    @NotNull
    private MenuTypeEnum menuType;

    /**
     * 菜单图标
     */
    @Size(max = 64)
    private String icon;

    /**
     * 菜单备注
     */
    @Size(max = 255)
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