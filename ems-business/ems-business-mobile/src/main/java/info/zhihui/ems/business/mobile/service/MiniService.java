package info.zhihui.ems.business.mobile.service;

import info.zhihui.ems.business.mobile.bo.MiniCurrentUserBo;
import info.zhihui.ems.business.mobile.bo.MiniLoginBo;
import info.zhihui.ems.business.mobile.bo.MiniLoginResultBo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * 小程序用户端服务。
 */
public interface MiniService {

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

    /**
     * 查询当前登录用户及开户账户信息。
     *
     * @return 当前用户与开户账户信息
     */
    MiniCurrentUserBo getCurrentUser();
}
