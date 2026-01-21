package info.zhihui.ems.web.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户密码更新 VO
 */
@Data
@Schema(name = "UserPasswordUpdateVo", description = "用户密码更新参数")
public class UserPasswordUpdateVo {

    @NotBlank
    @Size(min = 6, max = 20)
    @Schema(description = "旧密码")
    private String oldPassword;

    @NotBlank
    @Size(min = 6, max = 20)
    @Schema(description = "新密码")
    private String newPassword;
}
