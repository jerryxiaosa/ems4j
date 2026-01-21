package info.zhihui.ems.web.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(name = "CaptchaVo", description = "验证码信息")
@Data
public class CaptchaVo {
    @Schema(description = "验证码标识")
    private String captchaKey;

    @Schema(description = "验证码图片Base64")
    private String img;
}
