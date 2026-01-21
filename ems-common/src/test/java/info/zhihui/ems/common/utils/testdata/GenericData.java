package info.zhihui.ems.common.utils.testdata;

import lombok.Data;

/**
 * 用于测试嵌套泛型反序列化的数据类
 */
@Data
public class GenericData<T> {
    private String id;
    private T content;

    public GenericData() {
    }

    public GenericData(String id, T content) {
        this.id = id;
        this.content = content;
    }
}