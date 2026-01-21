package info.zhihui.ems.foundation.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 角色创建DTO
 */
@Data
@Accessors(chain = true)
public class RoleCreateDto {

    /**
     * 角色名称
     */
    @NotBlank
    @Size(max = 64)
    private String roleName;

    /**
     * 角色唯一标识
     */
    @NotBlank
    @Size(max = 64)
    private String roleKey;

    /**
     * 显示排序号
     */
    private Integer sortNum;

    /**
     * 角色备注
     */
    @Size(max = 255)
    private String remark;

    /**
     * 是否系统内置 0否 1是
     */
    private Boolean isSystem;

    /**
     * 是否禁用 0否 1是
     */
    private Boolean isDisabled;

    /**
     * 关联的菜单ID列表
     */
    private List<Integer> menuIds;
}