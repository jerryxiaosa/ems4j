package info.zhihui.ems.foundation.user.service.impl;

import info.zhihui.ems.foundation.user.service.PasswordService;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

/**
 * @author jerryxiaosa
 */
@Service
public class BCryptPasswordServiceImpl implements PasswordService {

    /**
     * 验证密码
     *
     * @param rawPassword     密码
     * @param encodedPassword 原密码
     * @return 密码是否匹配
     */
    @Override
    public boolean matchesPassword(String rawPassword, String encodedPassword) {
        return BCrypt.checkpw(rawPassword, encodedPassword);
    }

    /**
     * 加密密码
     *
     * @param rawPassword 密码
     * @return 加密后的密码
     */
    @Override
    public String encode(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }
}
