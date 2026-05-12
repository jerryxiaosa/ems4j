package info.zhihui.ems.common.constant;

import lombok.Getter;

@Getter
public enum ResultCode {
    // 通用
    SUCCESS(100001, "成功"),
    FAILED(-100001, "接口异常"),

    BUSINESS_ERROR(-101001, "错误"),
    PARAMETER_ERROR(-102001, "接口参数异常"),
    ACCOUNT_ERROR(-102002, "用户异常"),

    NOT_LOGIN_ERROR(-103001, "请先登录"),
    PERMISSION_ERROR(-103002, "权限不足"),

    MINI_WECHAT_LOGIN_CODE_INVALID(-11001, "微信登录凭证已失效，请重新登录"),
    MINI_WECHAT_PHONE_CODE_INVALID(-11002, "获取微信手机号失败，请重新授权"),
    MINI_PHONE_NOT_BOUND(-11003, "当前手机号未绑定系统用户"),
    MINI_PHONE_BINDING_ABNORMAL(-11004, "手机号绑定异常，请联系管理员"),
    MINI_USER_UNAVAILABLE(-11005, "当前用户不可用，请联系管理员"),
    MINI_ACCOUNT_NOT_FOUND(-11011, "未匹配到开户账户"),
    MINI_ACCOUNT_NOT_OPENED(-11012, "账户未开户"),
    MINI_ACCOUNT_ABNORMAL(-11013, "账户未开户或状态异常"),

    ;

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

}
