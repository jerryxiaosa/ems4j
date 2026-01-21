package info.zhihui.ems.common.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class LoginException extends RuntimeException {

    private String message;

    public static LoginException captchaError() {
        return new LoginException("验证码错误，请输入正确的验证码");
    }

    public static LoginException passwordError() {
        return new LoginException("账号或密码错误");
    }

    public static LoginException maxTry(Integer lockTime) {
        return new LoginException(String.format("密码重试次数过多，请%d分钟后重试", lockTime));
    }

    public static LoginException accountRepeat() {
        return new LoginException("账号重复，请联系管理员");
    }

    public static LoginException disable() {
        return new LoginException("用户账号已被冻结，请联系管理员");
    }

    public static LoginException noPermission() {
        return new LoginException("用户尚未分配功能权限，登录失败");
    }

    public static LoginException wxDisable() {
        return new LoginException("账号不存在或冻结，请联系管理员");
    }
}
