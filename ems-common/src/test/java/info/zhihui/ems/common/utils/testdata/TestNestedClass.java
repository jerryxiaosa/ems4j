package info.zhihui.ems.common.utils.testdata;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TestNestedClass {
    private String name;
    private TestClass innerObject;
    private List<TestClass> innerList;
    private LocalDateTime createTime;

    public TestNestedClass() {
    }

    public TestNestedClass(String name, TestClass innerObject, List<TestClass> innerList, LocalDateTime createTime) {
        this.name = name;
        this.innerObject = innerObject;
        this.innerList = innerList;
        this.createTime = createTime;
    }
}