package info.zhihui.ems.business.mobile.bo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 小程序登录结果。
 */
@Data
@Accessors(chain = true)
public class MiniLoginResultBo {
    private String accessToken;
    private Long expireIn;
}
