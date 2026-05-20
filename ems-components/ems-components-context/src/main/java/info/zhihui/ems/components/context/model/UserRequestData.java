package info.zhihui.ems.components.context.model;

import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;

@Getter
public class UserRequestData implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String userRealName;

    private final String userPhone;

    private final Integer accountId;

    public UserRequestData(String userRealName, String userPhone) {
        this(userRealName, userPhone, null);
    }

    public UserRequestData(String userRealName, String userPhone, Integer accountId) {
        this.userRealName = userRealName;
        this.userPhone = userPhone;
        this.accountId = accountId;
    }
}
