package info.zhihui.ems.web.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 角色菜单保存 VO
 */
@Data
@Accessors(chain = true)
@Schema(name = "RoleMenuSaveVo", description = "角色菜单保存数据")
public class RoleMenuSaveVo {

    @Schema(description = "菜单ID列表")
    private List<Integer> menuIds;
}
