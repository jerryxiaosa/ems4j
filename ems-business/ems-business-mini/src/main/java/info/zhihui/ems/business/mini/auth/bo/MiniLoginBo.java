package info.zhihui.ems.business.mini.auth.bo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 小程序登录参数。
 */
@Data
@Accessors(chain = true)
public class MiniLoginBo {

    @NotBlank
    private String loginCode;

    @NotBlank
    private String phoneCode;
}
