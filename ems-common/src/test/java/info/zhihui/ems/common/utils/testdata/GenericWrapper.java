package info.zhihui.ems.common.utils.testdata;

import lombok.Data;

import java.util.List;

/**
 * 用于测试嵌套泛型反序列化的包装类
 */
@Data
public class GenericWrapper<T> {
    private String name;
    private GenericData<T> data;
    private List<GenericData<T>> dataList;

    public GenericWrapper() {
    }

    public GenericWrapper(String name, GenericData<T> data, List<GenericData<T>> dataList) {
        this.name = name;
        this.data = data;
        this.dataList = dataList;
    }
}