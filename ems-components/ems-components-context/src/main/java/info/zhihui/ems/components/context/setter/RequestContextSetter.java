package info.zhihui.ems.components.context.setter;


import info.zhihui.ems.common.utils.ThreadLocalUtil;
import info.zhihui.ems.components.context.constant.RequestContextConstant;
import info.zhihui.ems.components.context.model.UserRequestData;

public class RequestContextSetter {

    public static void doSet() {
        doSet(RequestContextConstant.BACKEND_TASK_USER);
    }

    public static void doSet(Integer userId) {
        ThreadLocalUtil.put(RequestContextConstant.USER_ID, userId);
        ThreadLocalUtil.put(RequestContextConstant.USER_REQUEST_DATA, null);
    }

    public static void doSet(Integer userId, UserRequestData userRequestData) {
        ThreadLocalUtil.put(RequestContextConstant.USER_ID, userId);
        ThreadLocalUtil.put(RequestContextConstant.USER_REQUEST_DATA, userRequestData);
    }

}
