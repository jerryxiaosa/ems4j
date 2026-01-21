package info.zhihui.ems.foundation.user;

import info.zhihui.ems.foundation.user.service.PasswordService;
import info.zhihui.ems.foundation.user.service.impl.BCryptPasswordServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("密码单元测试")
class BCryptPasswordServiceImplUnitTest {

    private final PasswordService passwordService = new BCryptPasswordServiceImpl();

    @Test
    @DisplayName("加密后可以匹配")
    void testEncodeAndMatch() {
        String raw = "Abc123!@#";
        String encoded = passwordService.encode(raw);
        assertThat(encoded).isNotBlank();
        assertThat(passwordService.matchesPassword(raw, encoded)).isTrue();
    }

    @Test
    @DisplayName("不同密码不匹配")
    void testMismatch() {
        String raw = "Abc123!@#";
        String encoded = passwordService.encode(raw);
        assertThat(passwordService.matchesPassword("Different1!", encoded)).isFalse();
    }
}
