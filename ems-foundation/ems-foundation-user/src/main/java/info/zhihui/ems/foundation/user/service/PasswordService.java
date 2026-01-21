package info.zhihui.ems.foundation.user.service;

/**
 * @author jerryxiaosa
 */
public interface PasswordService {
    /**
     * 验证密码
     *
     * @param rawPassword     密码
     * @param encodedPassword 原密码
     * @return 密码是否匹配
     */
    boolean matchesPassword(String rawPassword, String encodedPassword);

    /**
     * 加密密码
     *
     * @param rawPassword 密码
     * @return 加密后的密码
     */
    String encode(String rawPassword);
}
