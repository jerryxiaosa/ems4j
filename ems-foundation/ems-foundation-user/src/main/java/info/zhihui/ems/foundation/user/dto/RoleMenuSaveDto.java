package info.zhihui.ems.foundation.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 角色菜单保存DTO
 */
@Data
@Accessors(chain = true)
public class RoleMenuSaveDto {

    /**
     * 角色ID
     */
    @NotNull
    private Integer roleId;

    /**
     * 菜单ID列表
     */
    private List<Integer> menuIds;
}