package info.zhihui.ems.web.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 用户查询 VO
 */
@Data
@Schema(name = "UserQueryVo", description = "用户查询条件")
public class UserQueryVo {

    @Size(max = 40)
    @Schema(description = "用户名模糊查询条件")
    private String userNameLike;

    @Size(max = 30)
    @Schema(description = "真实姓名模糊查询条件")
    private String realNameLike;

    @Size(max = 20)
    @Schema(description = "手机号")
    private String userPhone;

    @Schema(description = "机构ID")
    private Integer organizationId;

    @Schema(description = "用户ID集合")
    private List<Integer> ids;
}
