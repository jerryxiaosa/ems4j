package info.zhihui.ems.business.mini.me.service;

import info.zhihui.ems.business.mini.me.bo.MiniCurrentUserBo;

/**
 * 小程序当前用户服务。
 */
public interface MiniCurrentUserService {

    /**
     * 查询当前登录用户及开户账户信息。
     *
     * @return 当前用户与开户账户信息
     */
    MiniCurrentUserBo getCurrentUser();
}
