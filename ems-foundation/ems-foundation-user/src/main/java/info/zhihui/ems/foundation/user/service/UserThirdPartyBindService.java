package info.zhihui.ems.foundation.user.service;

import info.zhihui.ems.foundation.user.bo.UserThirdPartyBindBo;
import info.zhihui.ems.foundation.user.dto.UserThirdPartyBindDto;
import info.zhihui.ems.foundation.user.enums.UserThirdPartyPlatformEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 用户第三方身份绑定服务。
 */
public interface UserThirdPartyBindService {

    /**
     * 新增或更新用户第三方身份绑定。
     *
     * @param dto 绑定参数
     */
    void bindOrUpdate(@NotNull @Valid UserThirdPartyBindDto dto);

    /**
     * 获取用户在指定第三方平台应用下的身份绑定。
     *
     * @param userId 用户ID
     * @param platform 第三方平台
     * @param appId 第三方应用ID
     * @return 绑定信息，不存在时返回 null
     */
    UserThirdPartyBindBo getByUserPlatformAndAppId(@NotNull Integer userId,
                                                   @NotNull UserThirdPartyPlatformEnum platform,
                                                   @NotBlank String appId);
}
