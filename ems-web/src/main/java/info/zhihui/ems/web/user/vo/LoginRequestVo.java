package info.zhihui.ems.web.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户登录请求 VO
 */
@Data
@Schema(name = "LoginRequestVo", description = "用户登录请求参数")
public class LoginRequestVo {

    @NotBlank
    @Schema(description = "用户名")
    private String userName;

    @NotBlank
    @Schema(description = "密码")
    private String password;

    @NotBlank
    @Schema(description = "验证码缓存键")
    private String captchaKey;

    @NotBlank
    @Schema(description = "验证码值")
    private String captchaValue;
}
