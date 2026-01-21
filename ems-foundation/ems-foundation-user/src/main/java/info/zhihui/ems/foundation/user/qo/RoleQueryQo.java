package info.zhihui.ems.foundation.user.qo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 角色查询条件QO
 */
@Data
@Accessors(chain = true)
public class RoleQueryQo {

    /**
     * 角色名称模糊查询
     */
    private String roleNameLike;

    /**
     * 角色唯一标识
     */
    private String roleKey;

    /**
     * 是否系统内置
     */
    private Boolean isSystem;

    /**
     * 是否禁用
     */
    private Boolean isDisabled;

    /**
     * 角色ID列表
     */
    private List<Integer> ids;

    /**
     * 排除的角色ID列表
     */
    private List<Integer> excludeIds;
}