package info.zhihui.ems.business.mobile.aspect;

import cn.dev33.satoken.session.SaSession;
import info.zhihui.ems.business.account.bo.AccountBo;
import info.zhihui.ems.business.account.service.AccountInfoService;
import info.zhihui.ems.business.mobile.utils.MobileStpUtil;
import info.zhihui.ems.common.constant.ResultCode;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.components.context.RequestContext;
import info.zhihui.ems.components.context.model.UserRequestData;
import info.zhihui.ems.components.context.setter.RequestContextSetter;
import info.zhihui.ems.foundation.user.constants.LoginConstant;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 移动端账户上下文校验切面。
 */
@Aspect
@Component
@RequiredArgsConstructor
public class RequireAccountContextAspect {

    private final AccountInfoService accountInfoService;
    private final RequestContext requestContext;

    @Around("@within(info.zhihui.ems.business.mobile.annotation.RequireAccountContext) "
            + "|| @annotation(info.zhihui.ems.business.mobile.annotation.RequireAccountContext)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        ensureAccountContext();
        return joinPoint.proceed();
    }

    void ensureAccountContext() {
        SaSession session = MobileStpUtil.getSession();
        Integer accountId = (Integer) session.get(LoginConstant.LOGIN_ACCOUNT_ID);
        if (accountId == null) {
            throw accountAbnormal();
        }

        AccountBo account;
        try {
            account = accountInfoService.getById(accountId);
        } catch (NotFoundException e) {
            throw accountAbnormal();
        }
        if (account == null || account.getElectricAccountType() == null) {
            throw accountAbnormal();
        }

        RequestContextSetter.doSet(requestContext.getUserId(), new UserRequestData(
                requestContext.getUserRealName(),
                requestContext.getUserPhone(),
                accountId
        ));
    }

    private BusinessRuntimeException accountAbnormal() {
        return new BusinessRuntimeException(
                ResultCode.MOBILE_ACCOUNT_ABNORMAL.getCode(),
                ResultCode.MOBILE_ACCOUNT_ABNORMAL.getMessage()
        );
    }
}
