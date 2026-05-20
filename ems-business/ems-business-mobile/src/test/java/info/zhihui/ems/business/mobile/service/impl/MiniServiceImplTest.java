package info.zhihui.ems.business.mobile.service.impl;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.context.mock.SaTokenContextMockUtil;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import info.zhihui.ems.business.account.bo.AccountBo;
import info.zhihui.ems.business.account.dto.AccountCancelDetailDto;
import info.zhihui.ems.business.account.dto.AccountCancelQueryDto;
import info.zhihui.ems.business.account.dto.AccountCancelRecordDto;
import info.zhihui.ems.business.account.dto.AccountCandidateMeterDto;
import info.zhihui.ems.business.account.dto.AccountElectricBalanceAggregateItemDto;
import info.zhihui.ems.business.account.dto.AccountOwnerInfoDto;
import info.zhihui.ems.business.account.dto.AccountQueryDto;
import info.zhihui.ems.business.account.dto.OwnerCandidateMeterQueryDto;
import info.zhihui.ems.business.account.service.AccountAdditionalInfoService;
import info.zhihui.ems.business.account.service.AccountInfoService;
import info.zhihui.ems.business.device.bo.ElectricMeterBo;
import info.zhihui.ems.business.device.dto.CanceledMeterDto;
import info.zhihui.ems.business.device.dto.ElectricMeterQueryDto;
import info.zhihui.ems.business.device.service.ElectricMeterInfoService;
import info.zhihui.ems.business.mobile.bo.MiniCurrentUserBo;
import info.zhihui.ems.business.mobile.bo.MiniLoginBo;
import info.zhihui.ems.business.mobile.bo.MiniLoginResultBo;
import info.zhihui.ems.business.mobile.utils.MobileStpUtil;
import info.zhihui.ems.common.constant.ResultCode;
import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import info.zhihui.ems.common.enums.OwnerTypeEnum;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.components.context.RequestContext;
import info.zhihui.ems.components.context.model.UserRequestData;
import info.zhihui.ems.components.context.setter.RequestContextSetter;
import info.zhihui.ems.foundation.thirdparty.wechat.dto.WechatMiniProgramLoginDto;
import info.zhihui.ems.foundation.thirdparty.wechat.service.WechatMiniProgramService;
import info.zhihui.ems.foundation.user.bo.UserBo;
import info.zhihui.ems.foundation.user.bo.UserThirdPartyBindBo;
import info.zhihui.ems.foundation.user.constants.LoginConstant;
import info.zhihui.ems.foundation.user.dto.UserCreateDto;
import info.zhihui.ems.foundation.user.dto.UserQueryDto;
import info.zhihui.ems.foundation.user.dto.UserResetPasswordDto;
import info.zhihui.ems.foundation.user.dto.UserThirdPartyBindDto;
import info.zhihui.ems.foundation.user.dto.UserUpdateDto;
import info.zhihui.ems.foundation.user.dto.UserUpdatePasswordDto;
import info.zhihui.ems.foundation.user.enums.MenuSourceEnum;
import info.zhihui.ems.foundation.user.enums.UserThirdPartyPlatformEnum;
import info.zhihui.ems.foundation.user.service.UserService;
import info.zhihui.ems.foundation.user.service.UserThirdPartyBindService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MiniServiceImplTest {

    private static final String OLD_MINI_OPEN_ID_SESSION_KEY = "user::login::mini::open_id";
    private static final String OLD_MINI_UNION_ID_SESSION_KEY = "user::login::mini::union_id";
    private static final String OLD_THIRD_PARTY_APP_ID_SESSION_KEY = "user::login::third_party::app_id";

    @BeforeEach
    void setUp() {
        SaManager.getConfig().setJwtSecretKey("mini-service-test-secret");
    }

    @AfterEach
    void tearDown() {
        RequestContextSetter.clear();
    }

    @Test
    void login_WhenPhoneNotBound_ShouldThrowMobilePhoneNotBoundCode() {
        MiniLoginBo loginBo = new MiniLoginBo()
                .setLoginCode("login-code")
                .setPhoneCode("phone-code");
        StubWechatMiniProgramService wechatService = new StubWechatMiniProgramService(new WechatMiniProgramLoginDto()
                .setOpenId("open-id")
                .setPurePhoneNumber("13800138000"));
        StubUserService userService = new StubUserService(new NotFoundException("用户不存在"));
        StubUserThirdPartyBindService bindService = new StubUserThirdPartyBindService();
        MiniServiceImpl miniService = newMiniService(wechatService, userService, new StubAccountInfoService(null), bindService);

        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class, () -> miniService.login(loginBo));

        assertThat(exception.getCode()).isEqualTo(ResultCode.MOBILE_PHONE_NOT_BOUND.getCode());
        assertThat(exception.getMessage()).isEqualTo(ResultCode.MOBILE_PHONE_NOT_BOUND.getMessage());
        assertThat(bindService.boundDto).isNull();
    }

    @Test
    void login_WhenPhoneBindingAbnormal_ShouldThrowMobilePhoneNotBoundCode() {
        MiniLoginBo loginBo = new MiniLoginBo()
                .setLoginCode("login-code")
                .setPhoneCode("phone-code");
        StubWechatMiniProgramService wechatService = new StubWechatMiniProgramService(new WechatMiniProgramLoginDto()
                .setOpenId("open-id")
                .setPurePhoneNumber("13800138000"));
        StubUserService userService = new StubUserService(new BusinessRuntimeException("手机号绑定多个用户"));
        StubUserThirdPartyBindService bindService = new StubUserThirdPartyBindService();
        MiniServiceImpl miniService = newMiniService(wechatService, userService, new StubAccountInfoService(null), bindService);

        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class, () -> miniService.login(loginBo));

        assertThat(exception.getCode()).isEqualTo(ResultCode.MOBILE_PHONE_NOT_BOUND.getCode());
        assertThat(exception.getMessage()).isEqualTo(ResultCode.MOBILE_PHONE_NOT_BOUND.getMessage());
        assertThat(bindService.boundDto).isNull();
    }

    @Test
    void login_ShouldQueryUserByExactWechatPhoneNumber() {
        SaTokenContextMockUtil.setMockContext(() -> {
            MiniLoginBo loginBo = new MiniLoginBo()
                    .setLoginCode("login-code")
                    .setPhoneCode("phone-code");
            StubWechatMiniProgramService wechatService = new StubWechatMiniProgramService(new WechatMiniProgramLoginDto()
                    .setAppId("wx-app")
                    .setOpenId("open-id")
                    .setPurePhoneNumber("13800138000"));
            StubUserService userService = new StubUserService(new UserBo()
                    .setId(2)
                    .setRealName("张三")
                    .setUserPhone("13800138000")
                    .setOrganizationId(10));
            StubAccountInfoService accountService = new StubAccountInfoService(new AccountBo()
                    .setId(30)
                    .setElectricAccountType(ElectricAccountTypeEnum.MERGED));
            MiniServiceImpl miniService = newMiniService(wechatService, userService, accountService, new StubUserThirdPartyBindService());

            miniService.login(loginBo);

            assertThat(userService.lastPhone).isEqualTo("13800138000");
            logoutMobileQuietly();
        });
    }

    @Test
    void login_WhenUnionIdMissing_ShouldNotWriteWechatIdentityToSession() {
        SaTokenContextMockUtil.setMockContext(() -> {
            MiniLoginBo loginBo = new MiniLoginBo()
                    .setLoginCode("login-code")
                    .setPhoneCode("phone-code");
            StubWechatMiniProgramService wechatService = new StubWechatMiniProgramService(new WechatMiniProgramLoginDto()
                    .setAppId("app-id")
                    .setOpenId("open-id")
                    .setUnionId(null)
                    .setPurePhoneNumber("13800138000"));
            StubUserService userService = new StubUserService(new UserBo()
                    .setId(1)
                    .setRealName("测试用户")
                    .setUserPhone("13800138000")
                    .setOrganizationId(10));
            StubAccountInfoService accountService = new StubAccountInfoService(new AccountBo()
                    .setId(20)
                    .setElectricAccountType(ElectricAccountTypeEnum.MERGED));
            StubUserThirdPartyBindService bindService = new StubUserThirdPartyBindService();
            MiniServiceImpl miniService = newMiniService(wechatService, userService, accountService, bindService);

            MiniLoginResultBo result = miniService.login(loginBo);

            assertThat(result.getAccessToken()).isNotBlank();
            assertThat(result.getExpireIn()).isPositive();
            assertThat(MobileStpUtil.getLoginIdAsInt()).isEqualTo(1);
            assertThat(MobileStpUtil.getSession().get(LoginConstant.LOGIN_USER_REAL_NAME)).isEqualTo("测试用户");
            assertThat(MobileStpUtil.getSession().get(LoginConstant.LOGIN_USER_PHONE)).isEqualTo("13800138000");
            assertThat(MobileStpUtil.getSession().get(LoginConstant.LOGIN_ACCOUNT_ID)).isEqualTo(20);
            assertThat(MobileStpUtil.getSession().get(OLD_THIRD_PARTY_APP_ID_SESSION_KEY)).isNull();
            assertThat(MobileStpUtil.getSession().get(OLD_MINI_OPEN_ID_SESSION_KEY)).isNull();
            assertThat(MobileStpUtil.getSession().get(OLD_MINI_UNION_ID_SESSION_KEY)).isNull();
            assertThat(accountService.lastQuery.getOwnerType()).isEqualTo(OwnerTypeEnum.ENTERPRISE);
            assertThat(accountService.lastQuery.getOwnerIds()).containsExactly(10);
            logoutMobileQuietly();
        });
    }

    @Test
    void login_WhenUnionIdExists_ShouldBindIdentityAndReturnToken() {
        SaTokenContextMockUtil.setMockContext(() -> {
            MiniLoginBo loginBo = new MiniLoginBo()
                    .setLoginCode("login-code")
                    .setPhoneCode("phone-code");
            StubWechatMiniProgramService wechatService = new StubWechatMiniProgramService(new WechatMiniProgramLoginDto()
                    .setAppId("wx-app")
                    .setOpenId("open-id")
                    .setUnionId("union-id")
                    .setPurePhoneNumber("13800138000"));
            StubUserService userService = new StubUserService(new UserBo()
                    .setId(2)
                    .setRealName("张三")
                    .setUserPhone("13800138000")
                    .setOrganizationId(10));
            StubAccountInfoService accountService = new StubAccountInfoService(new AccountBo()
                    .setId(30)
                    .setElectricAccountType(ElectricAccountTypeEnum.MERGED));
            StubUserThirdPartyBindService bindService = new StubUserThirdPartyBindService();
            MiniServiceImpl miniService = newMiniService(wechatService, userService, accountService, bindService);

            MiniLoginResultBo result = miniService.login(loginBo);

            assertThat(result.getAccessToken()).isNotBlank();
            assertThat(result.getExpireIn()).isPositive();
            assertThat(MobileStpUtil.getLoginIdAsInt()).isEqualTo(2);
            assertThat(MobileStpUtil.getSession().get(LoginConstant.LOGIN_USER_REAL_NAME)).isEqualTo("张三");
            assertThat(MobileStpUtil.getSession().get(LoginConstant.LOGIN_USER_PHONE)).isEqualTo("13800138000");
            assertThat(MobileStpUtil.getSession().get(LoginConstant.LOGIN_ACCOUNT_ID)).isEqualTo(30);
            assertThat(MobileStpUtil.getSession().get(OLD_THIRD_PARTY_APP_ID_SESSION_KEY)).isNull();
            assertThat(MobileStpUtil.getSession().get(OLD_MINI_OPEN_ID_SESSION_KEY)).isNull();
            assertThat(MobileStpUtil.getSession().get(OLD_MINI_UNION_ID_SESSION_KEY)).isNull();

            UserThirdPartyBindDto bindDto = bindService.boundDto;
            assertThat(bindDto.getUserId()).isEqualTo(2);
            assertThat(bindDto.getPlatform()).isEqualTo(UserThirdPartyPlatformEnum.WECHAT_MINI);
            assertThat(bindDto.getAppId()).isEqualTo("wx-app");
            assertThat(bindDto.getThirdPartyUserId()).isEqualTo("open-id");
            assertThat(bindDto.getThirdPartyUnionId()).isEqualTo("union-id");
            assertThat(bindDto.getPhone()).isEqualTo("13800138000");
            logoutMobileQuietly();
        });
    }

    @Test
    void login_WhenAccountMissing_ShouldThrowAccountAbnormalAndNotBindOrLogin() {
        SaTokenContextMockUtil.setMockContext(() -> {
            MiniLoginBo loginBo = new MiniLoginBo()
                    .setLoginCode("login-code")
                    .setPhoneCode("phone-code");
            StubWechatMiniProgramService wechatService = new StubWechatMiniProgramService(new WechatMiniProgramLoginDto()
                    .setAppId("wx-app")
                    .setOpenId("open-id")
                    .setPurePhoneNumber("13800138000"));
            StubUserService userService = new StubUserService(new UserBo()
                    .setId(2)
                    .setRealName("张三")
                    .setUserPhone("13800138000")
                    .setOrganizationId(10));
            StubAccountInfoService accountService = new StubAccountInfoService(List.of());
            StubUserThirdPartyBindService bindService = new StubUserThirdPartyBindService();
            MiniServiceImpl miniService = newMiniService(wechatService, userService, accountService, bindService);

            BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class, () -> miniService.login(loginBo));

            assertThat(exception.getCode()).isEqualTo(ResultCode.MOBILE_ACCOUNT_ABNORMAL.getCode());
            assertThat(exception.getMessage()).isEqualTo(ResultCode.MOBILE_ACCOUNT_ABNORMAL.getMessage());
            assertThat(bindService.boundDto).isNull();
            assertThatThrownBy(MobileStpUtil::getLoginIdAsInt).isInstanceOf(NotLoginException.class);
        });
    }

    @Test
    void login_WhenUserHasNoOrganization_ShouldThrowAccountAbnormalAndNotBindOrLogin() {
        SaTokenContextMockUtil.setMockContext(() -> {
            MiniLoginBo loginBo = new MiniLoginBo()
                    .setLoginCode("login-code")
                    .setPhoneCode("phone-code");
            StubWechatMiniProgramService wechatService = new StubWechatMiniProgramService(new WechatMiniProgramLoginDto()
                    .setAppId("wx-app")
                    .setOpenId("open-id")
                    .setPurePhoneNumber("13800138000"));
            StubUserService userService = new StubUserService(new UserBo()
                    .setId(2)
                    .setRealName("张三")
                    .setUserPhone("13800138000"));
            StubAccountInfoService accountService = new StubAccountInfoService(new AccountBo()
                    .setId(30)
                    .setElectricAccountType(ElectricAccountTypeEnum.MERGED));
            StubUserThirdPartyBindService bindService = new StubUserThirdPartyBindService();
            MiniServiceImpl miniService = newMiniService(wechatService, userService, accountService, bindService);

            BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class, () -> miniService.login(loginBo));

            assertThat(exception.getCode()).isEqualTo(ResultCode.MOBILE_ACCOUNT_ABNORMAL.getCode());
            assertThat(bindService.boundDto).isNull();
            assertThatThrownBy(MobileStpUtil::getLoginIdAsInt).isInstanceOf(NotLoginException.class);
        });
    }

    @Test
    void login_WhenMultipleAccountsMatched_ShouldThrowAccountAbnormalAndNotBindOrLogin() {
        SaTokenContextMockUtil.setMockContext(() -> {
            MiniLoginBo loginBo = new MiniLoginBo()
                    .setLoginCode("login-code")
                    .setPhoneCode("phone-code");
            StubWechatMiniProgramService wechatService = new StubWechatMiniProgramService(new WechatMiniProgramLoginDto()
                    .setAppId("wx-app")
                    .setOpenId("open-id")
                    .setPurePhoneNumber("13800138000"));
            StubUserService userService = new StubUserService(new UserBo()
                    .setId(2)
                    .setRealName("张三")
                    .setUserPhone("13800138000")
                    .setOrganizationId(10));
            StubAccountInfoService accountService = new StubAccountInfoService(List.of(
                    new AccountBo().setId(30).setElectricAccountType(ElectricAccountTypeEnum.MERGED),
                    new AccountBo().setId(31).setElectricAccountType(ElectricAccountTypeEnum.MONTHLY)
            ));
            StubUserThirdPartyBindService bindService = new StubUserThirdPartyBindService();
            MiniServiceImpl miniService = newMiniService(wechatService, userService, accountService, bindService);

            BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class, () -> miniService.login(loginBo));

            assertThat(exception.getCode()).isEqualTo(ResultCode.MOBILE_ACCOUNT_ABNORMAL.getCode());
            assertThat(bindService.boundDto).isNull();
            assertThatThrownBy(MobileStpUtil::getLoginIdAsInt).isInstanceOf(NotLoginException.class);
        });
    }

    @Test
    void getCurrentUser_FromRequestContext_ShouldReturnAccountInfo() {
        RequestContextSetter.doSet(7, new UserRequestData("移动用户", "13800138000", 20));
        RequestContext requestContext = new RequestContext();
        StubAccountInfoService accountService = new StubAccountInfoService(new AccountBo()
                .setId(20)
                .setOwnerName("星河家园 2 栋住户账户")
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY));
        StubAccountAdditionalInfoService additionalInfoService = new StubAccountAdditionalInfoService(Map.of(20, new BigDecimal("12.34")));
        StubElectricMeterInfoService meterInfoService = new StubElectricMeterInfoService(List.of(new ElectricMeterBo(), new ElectricMeterBo()));
        MiniServiceImpl miniService = newMiniService(accountService, requestContext, additionalInfoService, meterInfoService);

        MiniCurrentUserBo result = miniService.getCurrentUser();

        assertThat(result.getUserPhone()).isEqualTo("13800138000");
        assertThat(result.getElectricAccountId()).isEqualTo(20);
        assertThat(result.getElectricAccountName()).isEqualTo("星河家园 2 栋住户账户");
        assertThat(result.getElectricAccountType()).isEqualTo(ElectricAccountTypeEnum.QUANTITY);
        assertThat(result.getBalance()).isEqualByComparingTo("12.34");
        assertThat(result.getMeterCount()).isEqualTo(2);
        assertThat(additionalInfoService.lastItemList).hasSize(1);
        assertThat(additionalInfoService.lastItemList.get(0).getAccountId()).isEqualTo(20);
        assertThat(additionalInfoService.lastItemList.get(0).getElectricAccountType()).isEqualTo(ElectricAccountTypeEnum.QUANTITY);
        assertThat(meterInfoService.lastQuery.getAccountIds()).containsExactly(20);
    }

    @Test
    void getCurrentUser_WhenBalanceMissing_ShouldUseZeroBalance() {
        RequestContextSetter.doSet(7, new UserRequestData("移动用户", "13800138000", 20));
        RequestContext requestContext = new RequestContext();
        StubAccountInfoService accountService = new StubAccountInfoService(new AccountBo()
                .setId(20)
                .setOwnerName("星河家园 2 栋住户账户")
                .setElectricAccountType(ElectricAccountTypeEnum.MONTHLY));
        StubAccountAdditionalInfoService additionalInfoService = new StubAccountAdditionalInfoService(Map.of());
        StubElectricMeterInfoService meterInfoService = new StubElectricMeterInfoService(List.of());
        MiniServiceImpl miniService = newMiniService(accountService, requestContext, additionalInfoService, meterInfoService);

        MiniCurrentUserBo result = miniService.getCurrentUser();

        assertThat(result.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getMeterCount()).isZero();
    }

    @Test
    void logout_ShouldDelegateToMobileStpUtil() {
        SaTokenContextMockUtil.setMockContext(() -> {
            SaManager.getConfig().setJwtSecretKey("mini-service-test-secret");
            MobileStpUtil.login(100, new SaLoginParameter().setDeviceType(MenuSourceEnum.MOBILE.getInfo()));
            MiniServiceImpl miniService = newMiniService(
                    new StubWechatMiniProgramService(null),
                    new StubUserService(null),
                    new StubAccountInfoService(null),
                    new StubUserThirdPartyBindService()
            );

            miniService.logout();

            assertThatThrownBy(MobileStpUtil::getLoginIdAsInt).isInstanceOf(NotLoginException.class);
        });
    }

    private MiniServiceImpl newMiniService(WechatMiniProgramService wechatService,
                                           UserService userService,
                                           AccountInfoService accountInfoService,
                                           UserThirdPartyBindService bindService) {
        return new MiniServiceImpl(
                wechatService,
                userService,
                accountInfoService,
                bindService,
                new RequestContext(),
                new StubAccountAdditionalInfoService(Map.of()),
                new StubElectricMeterInfoService(List.of())
        );
    }

    private MiniServiceImpl newMiniService(AccountInfoService accountInfoService,
                                           RequestContext requestContext,
                                           AccountAdditionalInfoService additionalInfoService,
                                           ElectricMeterInfoService meterInfoService) {
        return new MiniServiceImpl(
                new StubWechatMiniProgramService(null),
                new StubUserService(null),
                accountInfoService,
                new StubUserThirdPartyBindService(),
                requestContext,
                additionalInfoService,
                meterInfoService
        );
    }

    private void logoutMobileQuietly() {
        try {
            MobileStpUtil.logout();
        } catch (NotLoginException ignore) {
            // ignore cleanup failure
        }
    }

    private static final class StubWechatMiniProgramService implements WechatMiniProgramService {
        private final WechatMiniProgramLoginDto loginDto;

        private StubWechatMiniProgramService(WechatMiniProgramLoginDto loginDto) {
            this.loginDto = loginDto;
        }

        @Override
        public WechatMiniProgramLoginDto resolveLogin(String loginCode, String phoneCode) {
            return loginDto;
        }
    }

    private static final class StubUserService implements UserService {
        private final Object userOrException;
        private String lastPhone;

        private StubUserService(Object userOrException) {
            this.userOrException = userOrException;
        }

        @Override
        public UserBo getUserByPhone(String userPhone) {
            lastPhone = userPhone;
            if (userOrException instanceof NotFoundException exception) {
                throw exception;
            }
            if (userOrException instanceof BusinessRuntimeException exception) {
                throw exception;
            }
            return (UserBo) userOrException;
        }

        @Override
        public PageResult<UserBo> findUserPage(UserQueryDto queryDto, PageParam pageParam) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<UserBo> findUserList(UserQueryDto queryDto) {
            throw new UnsupportedOperationException();
        }

        @Override
        public UserBo getUserInfo(Integer id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Integer add(UserCreateDto dto) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void update(UserUpdateDto dto) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void delete(Integer id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void updatePassword(UserUpdatePasswordDto dto) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void resetPassword(UserResetPasswordDto dto) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean hasPermission(Integer userId, String permission) {
            throw new UnsupportedOperationException();
        }
    }

    private static final class StubAccountInfoService implements AccountInfoService {
        private final Object accountListOrAccount;
        private AccountQueryDto lastQuery;

        private StubAccountInfoService(Object accountListOrAccount) {
            this.accountListOrAccount = accountListOrAccount;
        }

        @Override
        @SuppressWarnings("unchecked")
        public List<AccountBo> findList(AccountQueryDto query) {
            lastQuery = query;
            if (accountListOrAccount instanceof List<?>) {
                return (List<AccountBo>) accountListOrAccount;
            }
            return List.of((AccountBo) accountListOrAccount);
        }

        @Override
        public AccountBo getById(Integer id) {
            if (accountListOrAccount instanceof NotFoundException exception) {
                throw exception;
            }
            return (AccountBo) accountListOrAccount;
        }

        @Override
        public PageResult<AccountBo> findPage(AccountQueryDto query, PageParam pageParam) {
            throw new UnsupportedOperationException();
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

    private static final class StubUserThirdPartyBindService implements UserThirdPartyBindService {
        private UserThirdPartyBindDto boundDto;

        @Override
        public void bindOrUpdate(UserThirdPartyBindDto dto) {
            boundDto = dto;
        }

        @Override
        public UserThirdPartyBindBo getByUserPlatformAndAppId(Integer userId, UserThirdPartyPlatformEnum platform, String appId) {
            throw new UnsupportedOperationException();
        }
    }

    private static final class StubAccountAdditionalInfoService implements AccountAdditionalInfoService {
        private final Map<Integer, BigDecimal> balanceMap;
        private List<AccountElectricBalanceAggregateItemDto> lastItemList;

        private StubAccountAdditionalInfoService(Map<Integer, BigDecimal> balanceMap) {
            this.balanceMap = balanceMap;
        }

        @Override
        public Map<Integer, BigDecimal> findElectricBalanceAmountMap(List<AccountElectricBalanceAggregateItemDto> itemDtoList) {
            lastItemList = itemDtoList;
            return balanceMap;
        }

        @Override
        public List<AccountCandidateMeterDto> findCandidateMeterList(OwnerCandidateMeterQueryDto queryDto) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Map<Integer, Integer> countTotalOpenableMeterByAccountOwnerInfoList(List<AccountOwnerInfoDto> accountOwnerInfoDtoList) {
            throw new UnsupportedOperationException();
        }
    }

    private static final class StubElectricMeterInfoService implements ElectricMeterInfoService {
        private final List<ElectricMeterBo> meterList;
        private ElectricMeterQueryDto lastQuery;

        private StubElectricMeterInfoService(List<ElectricMeterBo> meterList) {
            this.meterList = meterList;
        }

        @Override
        public List<ElectricMeterBo> findList(ElectricMeterQueryDto query) {
            lastQuery = query;
            return meterList;
        }

        @Override
        public PageResult<ElectricMeterBo> findPage(ElectricMeterQueryDto query, PageParam pageParam) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ElectricMeterBo getDetail(Integer id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ElectricMeterBo getByDeviceNo(String deviceNo) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<CanceledMeterDto> findMetersByCancelNo(String cancelNo) {
            throw new UnsupportedOperationException();
        }
    }
}
