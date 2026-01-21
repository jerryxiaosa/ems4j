package info.zhihui.ems.foundation.user.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import info.zhihui.ems.components.datasource.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class RoleEntity extends BaseEntity {

    /**
     * 角色主键
     */
    @TableId
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
     * 是否系统内置 0否 1是
     */
    private Boolean isSystem;

    /**
     * 是否禁用 0否 1是
     */
    private Boolean isDisabled;

}