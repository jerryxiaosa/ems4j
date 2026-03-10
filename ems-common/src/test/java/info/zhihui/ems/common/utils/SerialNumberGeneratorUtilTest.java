package info.zhihui.ems.common.utils;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SerialNumberGeneratorUtilTest {

    @Test
    void testGenUniqueNo_ShouldUseCompactBase62Suffix() {
        String no = SerialNumberGeneratorUtil.genUniqueNo("CM");
        assertTrue(no.matches("^CM\\d{6}[0-9A-Za-z]{8}$"));
        assertEquals(16, no.length());

        String suffix = no.substring("CM".length() + 6);
        assertEquals(8, suffix.length());
    }

    @Test
    void testGenUniqueNo_ShouldGenerateDifferentValues() {
        Set<String> serialNoSet = new HashSet<>();
        for (int i = 0; i < 2000; i++) {
            serialNoSet.add(SerialNumberGeneratorUtil.genUniqueNo("CM"));
        }
        assertEquals(2000, serialNoSet.size());
    }
}
