package info.zhihui.ems.web.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 登录响应 VO
 */
@Data
@Accessors(chain = true)
@Schema(name = "LoginResponseVo", description = "用户登录响应数据")
public class LoginResponseVo {

    @Schema(description = "刷新令牌")
    private String refreshToken;

    @Schema(description = "访问令牌")
    private String accessToken;

    @Schema(description = "访问令牌过期时间，单位：秒")
    private Long expireIn;
}
