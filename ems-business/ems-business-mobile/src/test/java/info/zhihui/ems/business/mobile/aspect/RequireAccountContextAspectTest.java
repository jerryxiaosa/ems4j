package info.zhihui.ems.business.mobile.aspect;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.context.mock.SaTokenContextMockUtil;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import info.zhihui.ems.business.account.bo.AccountBo;
import info.zhihui.ems.business.account.dto.AccountCancelDetailDto;
import info.zhihui.ems.business.account.dto.AccountCancelQueryDto;
import info.zhihui.ems.business.account.dto.AccountCancelRecordDto;
import info.zhihui.ems.business.account.dto.AccountQueryDto;
import info.zhihui.ems.business.account.service.AccountInfoService;
import info.zhihui.ems.business.mobile.utils.MobileStpUtil;
import info.zhihui.ems.common.constant.ResultCode;
import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.components.context.RequestContext;
import info.zhihui.ems.components.context.model.UserRequestData;
import info.zhihui.ems.components.context.setter.RequestContextSetter;
import info.zhihui.ems.foundation.user.constants.LoginConstant;
import info.zhihui.ems.foundation.user.enums.MenuSourceEnum;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RequireAccountContextAspectTest {

    private final RequestContext requestContext = new RequestContext();

    @AfterEach
    void tearDown() {
        RequestContextSetter.clear();
    }

    @Test
    void ensureAccountContext_WhenSessionHasNoAccountId_ShouldThrowMobileAccountAbnormal() {
        SaTokenContextMockUtil.setMockContext(() -> {
            loginMobileSession();
            RequestContextSetter.doSet(7, new UserRequestData("移动用户", "13900139000"));
            RequireAccountContextAspect aspect = new RequireAccountContextAspect(new StubAccountInfoService(null), requestContext);

            assertThatThrownBy(aspect::ensureAccountContext)
                    .isInstanceOfSatisfying(BusinessRuntimeException.class, exception -> {
                        assertThat(exception.getCode()).isEqualTo(ResultCode.MOBILE_ACCOUNT_ABNORMAL.getCode());
                        assertThat(exception.getMessage()).isEqualTo(ResultCode.MOBILE_ACCOUNT_ABNORMAL.getMessage());
                    });

            logoutMobileQuietly();
        });
    }

    @Test
    void ensureAccountContext_WhenAccountNotFound_ShouldThrowMobileAccountAbnormal() {
        SaTokenContextMockUtil.setMockContext(() -> {
            loginMobileSession();
            MobileStpUtil.getSession().set(LoginConstant.LOGIN_ACCOUNT_ID, 20);
            RequestContextSetter.doSet(7, new UserRequestData("移动用户", "13900139000"));
            RequireAccountContextAspect aspect = new RequireAccountContextAspect(new StubAccountInfoService(new NotFoundException("账户不存在")), requestContext);

            assertThatThrownBy(aspect::ensureAccountContext)
                    .isInstanceOfSatisfying(BusinessRuntimeException.class, exception ->
                            assertThat(exception.getCode()).isEqualTo(ResultCode.MOBILE_ACCOUNT_ABNORMAL.getCode()));

            logoutMobileQuietly();
        });
    }

    @Test
    void ensureAccountContext_WhenAccountTypeIsNull_ShouldThrowMobileAccountAbnormal() {
        SaTokenContextMockUtil.setMockContext(() -> {
            loginMobileSession();
            MobileStpUtil.getSession().set(LoginConstant.LOGIN_ACCOUNT_ID, 20);
            RequestContextSetter.doSet(7, new UserRequestData("移动用户", "13900139000"));
            RequireAccountContextAspect aspect = new RequireAccountContextAspect(new StubAccountInfoService(new AccountBo().setId(20)), requestContext);

            assertThatThrownBy(aspect::ensureAccountContext)
                    .isInstanceOfSatisfying(BusinessRuntimeException.class, exception ->
                            assertThat(exception.getCode()).isEqualTo(ResultCode.MOBILE_ACCOUNT_ABNORMAL.getCode()));

            logoutMobileQuietly();
        });
    }

    @Test
    void ensureAccountContext_WhenAccountValid_ShouldSetAccountIdToRequestContext() {
        SaTokenContextMockUtil.setMockContext(() -> {
            loginMobileSession();
            MobileStpUtil.getSession().set(LoginConstant.LOGIN_ACCOUNT_ID, 20);
            RequestContextSetter.doSet(7, new UserRequestData("移动用户", "13900139000"));
            RequireAccountContextAspect aspect = new RequireAccountContextAspect(new StubAccountInfoService(
                    new AccountBo().setId(20).setElectricAccountType(ElectricAccountTypeEnum.MERGED)
            ), requestContext);

            aspect.ensureAccountContext();

            assertThat(requestContext.getUserId()).isEqualTo(7);
            assertThat(requestContext.getUserRealName()).isEqualTo("移动用户");
            assertThat(requestContext.getUserPhone()).isEqualTo("13900139000");
            assertThat(requestContext.getAccountId()).isEqualTo(20);

            logoutMobileQuietly();
        });
    }

    private void loginMobileSession() {
        SaManager.getConfig().setJwtSecretKey("require-account-context-test-secret");
        MobileStpUtil.login(7, new SaLoginParameter().setDeviceType(MenuSourceEnum.MOBILE.getInfo()));
    }

    private void logoutMobileQuietly() {
        try {
            MobileStpUtil.logout();
        } catch (NotLoginException ignore) {
            // ignore cleanup failure
        }
    }

    private static final class StubAccountInfoService implements AccountInfoService {
        private final Object accountOrException;

        private StubAccountInfoService(Object accountOrException) {
            this.accountOrException = accountOrException;
        }

        @Override
        public List<AccountBo> findList(AccountQueryDto query) {
            throw new UnsupportedOperationException();
        }

        @Override
        public PageResult<AccountBo> findPage(AccountQueryDto query, PageParam pageParam) {
            throw new UnsupportedOperationException();
        }

        @Override
        public AccountBo getById(Integer id) {
            if (accountOrException instanceof NotFoundException exception) {
                throw exception;
            }
            return (AccountBo) accountOrException;
        }

        @Override
        public PageResult<AccountCancelRecordDto> findCancelRecordPage(AccountCancelQueryDto query, PageParam pageParam) {
            throw new UnsupportedOperationException();
        }

        @Override
        public AccountCancelDetailDto getCancelRecordDetail(String cancelNo) {
            throw new UnsupportedOperationException();
        }
    }
}
