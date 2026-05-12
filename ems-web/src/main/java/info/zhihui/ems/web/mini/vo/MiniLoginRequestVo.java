package info.zhihui.ems.web.mini.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 小程序登录请求。
 */
@Data
@Accessors(chain = true)
public class MiniLoginRequestVo {

    @NotBlank
    private String loginCode;

    @NotBlank
    private String phoneCode;
}
