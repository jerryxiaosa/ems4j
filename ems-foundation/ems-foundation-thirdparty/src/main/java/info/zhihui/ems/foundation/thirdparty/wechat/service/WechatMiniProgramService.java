package info.zhihui.ems.foundation.thirdparty.wechat.service;

import info.zhihui.ems.foundation.thirdparty.wechat.dto.WechatMiniProgramLoginDto;
import jakarta.validation.constraints.NotBlank;

/**
 * 微信小程序服务。
 */
public interface WechatMiniProgramService {

    /**
     * 解析小程序登录凭证和手机号凭证。
     *
     * @param loginCode 微信登录凭证
     * @param phoneCode 微信手机号凭证
     * @return 微信小程序登录身份
     */
    WechatMiniProgramLoginDto resolveLogin(@NotBlank String loginCode, @NotBlank String phoneCode);
}
