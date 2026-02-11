package info.zhihui.ems.web.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户密码重置 VO
 */
@Data
@Schema(name = "UserPasswordResetVo", description = "用户密码重置参数")
public class UserPasswordResetVo {

    @NotBlank
    @Size(min = 6, max = 20)
    @Schema(description = "新密码")
    private String newPassword;
}
