package info.zhihui.ems.common.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class RestResult<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = -1814535413855065311L;

    private Boolean success = false;

    private Integer code;

    private String message;

    private T data;
}