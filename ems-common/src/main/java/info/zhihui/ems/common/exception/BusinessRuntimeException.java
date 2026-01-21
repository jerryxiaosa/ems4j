package info.zhihui.ems.common.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class BusinessRuntimeException extends RuntimeException {
    private Integer code;

    public BusinessRuntimeException(String message) {
        super(message);
    }

    public BusinessRuntimeException(Integer code, String message) {
        super(message);
        this.code = code;
    }

}
