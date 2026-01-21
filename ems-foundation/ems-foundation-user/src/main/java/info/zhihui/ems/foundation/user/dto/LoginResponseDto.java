package info.zhihui.ems.foundation.user.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
public class LoginResponseDto {
    private String refreshToken;

    private String accessToken;

    private Long expireIn;
}
