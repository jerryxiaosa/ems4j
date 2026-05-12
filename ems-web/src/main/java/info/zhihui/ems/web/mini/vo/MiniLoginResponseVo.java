package info.zhihui.ems.web.mini.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 小程序登录响应。
 */
@Data
@Accessors(chain = true)
public class MiniLoginResponseVo {
    private String accessToken;
    private Long expireIn;
}
