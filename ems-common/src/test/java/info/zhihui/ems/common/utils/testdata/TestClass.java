package info.zhihui.ems.common.utils.testdata;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TestClass {
    private String key;
    private LocalDateTime localDateTime;

    public TestClass() {
    }

    public TestClass(String key, LocalDateTime localDateTime) {
        this.key = key;
        this.localDateTime = localDateTime;
    }

}
