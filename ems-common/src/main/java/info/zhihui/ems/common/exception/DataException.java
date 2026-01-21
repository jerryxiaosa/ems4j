package info.zhihui.ems.common.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class DataException extends RuntimeException {

    private String message;

    public static DataException notExist(String obj) {
        return new DataException(obj + "不存在");
    }

    public static DataException repeat(String obj) {
        return new DataException(obj + "已存在");
    }

    public static DataException valueErr(String obj) {
        return new DataException(obj + "数据异常");
    }
}
