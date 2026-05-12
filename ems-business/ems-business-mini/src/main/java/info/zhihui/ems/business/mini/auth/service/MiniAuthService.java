package info.zhihui.ems.business.mini.auth.service;

import info.zhihui.ems.business.mini.auth.bo.MiniLoginBo;
import info.zhihui.ems.business.mini.auth.bo.MiniLoginResultBo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * 小程序认证服务。
 */
public interface MiniAuthService {

    /**
     * 微信手机号快捷登录。
     *
     * @param loginBo 登录参数
     * @return 登录结果
     */
    MiniLoginResultBo login(@NotNull @Valid MiniLoginBo loginBo);

    /**
     * 退出当前小程序登录态。
     */
    void logout();
}
