package info.zhihui.ems.web.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 用户角色 VO
 */
@Data
@Accessors(chain = true)
@Schema(name = "UserRoleVo", description = "用户角色信息")
public class UserRoleVo {

    @Schema(description = "角色ID")
    private Integer id;

    @Schema(description = "角色名称")
    private String roleName;

    @Schema(description = "角色标识")
    private String roleKey;
}
