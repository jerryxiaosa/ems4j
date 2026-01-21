package info.zhihui.ems.foundation.user.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 角色详情对象
 */
@Data
@Accessors(chain = true)
public class RoleDetailBo {

    /**
     * 角色主键
     */
    private Integer id;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色唯一标识
     */
    private String roleKey;

    /**
     * 显示排序号
     */
    private Integer sortNum;

    /**
     * 角色备注
     */
    private String remark;

    /**
     * 是否系统内置
     */
    private Boolean isSystem;

    /**
     * 是否禁用
     */
    private Boolean isDisabled;

    /**
     * 关联的菜单ID列表
     */
    private List<Integer> menuIds;

    /**
     * 角色权限标识列表
     */
    private List<String> permissions;
}