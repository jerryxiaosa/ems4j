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
    MOBILE_PHONE_NOT_BOUND(-11003, "当前手机号未绑定用户"),
    MOBILE_THIRD_PARTY_BIND_CONFLICT(-11011, "当前第三方身份已绑定其他用户"),
    MOBILE_ACCOUNT_ABNORMAL(-11010, "账户未开户或状态异常"),

    ;

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

}
