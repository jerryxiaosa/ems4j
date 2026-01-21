package info.zhihui.ems.foundation.user.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * @author jerryxiaosa
 */
@Data
public class LoginRequestDto {
    @NotEmpty
    private String userName;

    @NotEmpty
    private String password;

    private String captchaKey;

    private String captchaValue;
}
