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

    ;

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

}
