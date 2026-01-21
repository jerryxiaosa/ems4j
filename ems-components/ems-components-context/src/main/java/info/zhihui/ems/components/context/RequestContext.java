package info.zhihui.ems.components.context;

import info.zhihui.ems.common.utils.ThreadLocalUtil;
import info.zhihui.ems.components.context.constant.RequestContextConstant;
import info.zhihui.ems.components.context.model.UserRequestData;

import java.util.Optional;

public class RequestContext {

    public Integer getUserId() {
        return (Integer) ThreadLocalUtil.get(RequestContextConstant.USER_ID);
    }

    public String getUserPhone() {
        return Optional.ofNullable(getUserRequestData())
                .map(UserRequestData::getUserPhone)
                .orElse(null);
    }

    public String getUserRealName() {
        return Optional.ofNullable(getUserRequestData())
                .map(UserRequestData::getUserRealName)
                .orElse(null);
    }

    private UserRequestData getUserRequestData() {
        return (UserRequestData) ThreadLocalUtil.get(RequestContextConstant.USER_REQUEST_DATA);
    }

}
