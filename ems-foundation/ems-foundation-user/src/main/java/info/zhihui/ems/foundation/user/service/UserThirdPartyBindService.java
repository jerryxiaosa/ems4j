package info.zhihui.ems.foundation.user.service;

import info.zhihui.ems.foundation.user.bo.UserThirdPartyBindBo;
import info.zhihui.ems.foundation.user.dto.UserThirdPartyBindDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * 用户第三方身份绑定服务。
 */
public interface UserThirdPartyBindService {

    /**
     * 新增或更新用户第三方身份绑定。
     *
     * @param dto 绑定参数
     * @return 绑定信息
     */
    UserThirdPartyBindBo bindOrUpdate(@NotNull @Valid UserThirdPartyBindDto dto);
}
