package info.zhihui.ems.common.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class QueryValueUtilTest {

    @Test
    void normalizeLikeValue_WhenNull_ShouldReturnNull() {
        assertNull(QueryValueUtil.normalizeLikeValue(null));
    }

    @Test
    void normalizeLikeValue_WhenBlank_ShouldReturnNull() {
        assertNull(QueryValueUtil.normalizeLikeValue("   "));
        assertNull(QueryValueUtil.normalizeLikeValue(""));
    }

    @Test
    void normalizeLikeValue_WhenHasText_ShouldTrimAndReturnText() {
        assertEquals("abc", QueryValueUtil.normalizeLikeValue("  abc  "));
    }
}
