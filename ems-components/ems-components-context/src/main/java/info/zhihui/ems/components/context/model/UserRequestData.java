package info.zhihui.ems.components.context.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;

@AllArgsConstructor
public class UserRequestData implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Getter
    private final String userRealName;

    @Getter
    private final String userPhone;
}
