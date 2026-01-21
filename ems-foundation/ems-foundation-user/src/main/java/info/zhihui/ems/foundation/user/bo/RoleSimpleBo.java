package info.zhihui.ems.foundation.user.bo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 角色简单业务对象 - 用于用户角色关联场景
 */
@Data
@Accessors(chain = true)
public class RoleSimpleBo {

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
     * 是否禁用
     */
    private Boolean isDisabled;
}