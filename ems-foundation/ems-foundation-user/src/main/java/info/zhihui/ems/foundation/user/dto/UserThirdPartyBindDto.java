package info.zhihui.ems.foundation.user.dto;

import info.zhihui.ems.foundation.user.enums.UserThirdPartyPlatformEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 用户第三方身份绑定参数。
 */
@Data
@Accessors(chain = true)
public class UserThirdPartyBindDto {

    @NotNull
    private Integer userId;

    @NotNull
    private UserThirdPartyPlatformEnum platform;

    @NotBlank
    @Size(max = 128)
    private String appId;

    @NotBlank
    @Size(max = 128)
    private String thirdPartyUserId;

    @Size(max = 128)
    private String thirdPartyUnionId;

    @Size(max = 20)
    private String phone;
}
