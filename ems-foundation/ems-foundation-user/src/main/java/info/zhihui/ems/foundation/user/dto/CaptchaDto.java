package info.zhihui.ems.foundation.user.dto;

import lombok.Data;

/**
 * @author jerryxiaosa
 */
@Data
public class CaptchaDto {
    private String captchaKey;

    private String img;
}
