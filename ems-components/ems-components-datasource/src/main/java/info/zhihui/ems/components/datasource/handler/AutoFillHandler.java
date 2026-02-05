package info.zhihui.ems.components.datasource.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import info.zhihui.ems.components.context.RequestContext;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.reflection.MetaObject;

import java.time.LocalDateTime;

@RequiredArgsConstructor
public class AutoFillHandler implements MetaObjectHandler {
    private final RequestContext requestContext;

    @Override
    public void insertFill(MetaObject metaObject) {
        Integer userId = requestContext.getUserId();
        String userRealName = requestContext.getUserRealName();
        LocalDateTime now = LocalDateTime.now();

        if (metaObject.hasGetter("createUser") && metaObject.getValue("createUser") == null) {
            this.strictInsertFill(metaObject, "createUser", Integer.class, userId);
        }
        if (metaObject.hasGetter("createUserName") && metaObject.getValue("createUserName") == null) {
            this.strictInsertFill(metaObject, "createUserName", String.class, userRealName);
        }
        if (metaObject.hasGetter("createTime") && metaObject.getValue("createTime") == null) {
            this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        }

        if (metaObject.hasGetter("isDeleted") && metaObject.getValue("isDeleted") == null) {
            this.strictInsertFill(metaObject, "isDeleted", Boolean.class, false);
        }

        updateFill(metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        Integer userId = requestContext.getUserId();
        String userRealName = requestContext.getUserRealName();
        LocalDateTime now = LocalDateTime.now();

        // 无条件覆盖更新人/更新时间
        if (metaObject.hasGetter("updateUser")) {
            this.setFieldValByName("updateUser", userId, metaObject);
        }
        if (metaObject.hasGetter("updateUserName")) {
            this.setFieldValByName("updateUserName", userRealName, metaObject);
        }
        if (metaObject.hasGetter("updateTime")) {
            this.setFieldValByName("updateTime", now, metaObject);
        }
    }
}
