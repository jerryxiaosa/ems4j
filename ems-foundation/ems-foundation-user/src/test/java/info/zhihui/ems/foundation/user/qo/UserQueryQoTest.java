package info.zhihui.ems.foundation.user.qo;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class UserQueryQoTest {

    @Test
    void testUserPhone_ExactMatchSql_Exists() throws Exception {
        assertThat(UserQueryQo.class.getDeclaredField("userPhone")).isNotNull();

        String mapperXml = new String(Objects.requireNonNull(
                getClass().getClassLoader().getResourceAsStream("mapper/UserRepository.xml")
        ).readAllBytes(), StandardCharsets.UTF_8);

        assertThat(mapperXml).contains("qo.userPhone");
        assertThat(mapperXml).contains("t.user_phone = #{qo.userPhone}");
    }
}
