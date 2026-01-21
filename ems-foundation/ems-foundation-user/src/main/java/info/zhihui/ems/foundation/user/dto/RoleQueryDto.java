package info.zhihui.ems.foundation.user.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 角色查询DTO
 */
@Data
@Accessors(chain = true)
public class RoleQueryDto {

    /**
     * 角色名称模糊查询
     */
    private String roleNameLike;

    /**
     * 角色标识
     */
    private String roleKey;

    /**
     * 是否系统角色
     */
    private Boolean isSystem;

    /**
     * 是否禁用
     */
    private Boolean isDisabled;

    /**
     * ID集合
     */
    private List<Integer> ids;

    /**
     * 排除的ID集合
     */
    private List<Integer> excludeIds;
}