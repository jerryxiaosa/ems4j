package info.zhihui.ems.foundation.thirdparty.wechat.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import info.zhihui.ems.common.constant.ResultCode;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.components.redis.utils.RedisUtil;
import info.zhihui.ems.foundation.thirdparty.wechat.client.WechatMiniProgramClient;
import info.zhihui.ems.foundation.thirdparty.wechat.config.WechatMiniProgramAccountConfig;
import info.zhihui.ems.foundation.thirdparty.wechat.config.WechatMiniProgramProperties;
import info.zhihui.ems.foundation.thirdparty.wechat.dto.WechatAccessTokenDto;
import info.zhihui.ems.foundation.thirdparty.wechat.dto.WechatCodeSessionDto;
import info.zhihui.ems.foundation.thirdparty.wechat.dto.WechatMiniProgramLoginDto;
import info.zhihui.ems.foundation.thirdparty.wechat.dto.WechatPhoneNumberDto;
import info.zhihui.ems.foundation.system.service.ConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static info.zhihui.ems.foundation.system.constant.SystemConfigConstant.MINI_ACCOUNT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WechatMiniProgramServiceImplTest {

    private static final String APP_ID = "wx-app-id";
    private static final String APP_SECRET = "wx-app-secret";
    private static final String LOGIN_CODE = "login-code";
    private static final String PHONE_CODE = "phone-code";
    private static final String CACHE_KEY = "third-party:wechat:mini-program:access-token:" + APP_ID;

    @Mock
    private WechatMiniProgramClient client;
    @Mock
    private ConfigService configService;
    private WechatMiniProgramProperties properties;
    @InjectMocks
    private WechatMiniProgramServiceImpl service;

    @BeforeEach
    void setUp() {
        properties = new WechatMiniProgramProperties();
        properties.setAccessTokenExpireAheadSeconds(300);
        service = new WechatMiniProgramServiceImpl(client, properties, configService);
    }

    @Test
    void testResolveLogin_WhenWechatResponsesValid_ShouldReturnMiniProgramLogin() {
        stubMiniAccountConfig(miniAccountConfig());
        when(client.code2Session(APP_ID, APP_SECRET, LOGIN_CODE)).thenReturn(successSession());
        when(client.getAccessToken(APP_ID, APP_SECRET)).thenReturn(new WechatAccessTokenDto()
                .setAccessToken("access-token")
                .setExpiresIn(7200));
        when(client.getPhoneNumber("access-token", PHONE_CODE)).thenReturn(successPhoneNumber());

        try (MockedStatic<RedisUtil> redisMock = mockStatic(RedisUtil.class)) {
            redisMock.when(() -> RedisUtil.getCacheObject(CACHE_KEY)).thenReturn(null);

            WechatMiniProgramLoginDto result = service.resolveLogin(LOGIN_CODE, PHONE_CODE);

            assertThat(result.getAppId()).isEqualTo(APP_ID);
            assertThat(result.getOpenId()).isEqualTo("open-id");
            assertThat(result.getUnionId()).isEqualTo("union-id");
            assertThat(result.getSessionKey()).isEqualTo("session-key");
            assertThat(result.getPurePhoneNumber()).isEqualTo("13800138000");
            redisMock.verify(() -> RedisUtil.setCacheObject(CACHE_KEY, "access-token", Duration.ofSeconds(6900)));
        }
    }

    @Test
    void testResolveLogin_WhenAccessTokenCached_ShouldReuseCachedToken() {
        stubMiniAccountConfig(miniAccountConfig());
        when(client.code2Session(APP_ID, APP_SECRET, LOGIN_CODE)).thenReturn(successSession());
        when(client.getPhoneNumber("cached-token", PHONE_CODE)).thenReturn(successPhoneNumber());

        try (MockedStatic<RedisUtil> redisMock = mockStatic(RedisUtil.class)) {
            redisMock.when(() -> RedisUtil.getCacheObject(CACHE_KEY)).thenReturn("cached-token");

            WechatMiniProgramLoginDto result = service.resolveLogin(LOGIN_CODE, PHONE_CODE);

            assertThat(result.getOpenId()).isEqualTo("open-id");
            assertThat(result.getPurePhoneNumber()).isEqualTo("13800138000");
            verify(client, never()).getAccessToken(APP_ID, APP_SECRET);
            redisMock.verify(() -> RedisUtil.setCacheObject(CACHE_KEY, "cached-token", Duration.ofSeconds(60)), never());
        }
    }

    @Test
    void testResolveLogin_WhenCodeSessionMissesOptionalKeys_ShouldReturnMiniProgramLogin() {
        stubMiniAccountConfig(miniAccountConfig());
        when(client.code2Session(APP_ID, APP_SECRET, LOGIN_CODE)).thenReturn(new WechatCodeSessionDto()
                .setOpenid("ojhHQ6mfBBbBOyMk6dgbbDTYStyI")
                .setSessionKey("qSFYpN7RpaCFyr/IRgaYtw=="));
        when(client.getAccessToken(APP_ID, APP_SECRET)).thenReturn(new WechatAccessTokenDto()
                .setAccessToken("access-token")
                .setExpiresIn(7200));
        when(client.getPhoneNumber("access-token", PHONE_CODE)).thenReturn(successPhoneNumber());

        try (MockedStatic<RedisUtil> redisMock = mockStatic(RedisUtil.class)) {
            redisMock.when(() -> RedisUtil.getCacheObject(CACHE_KEY)).thenReturn(null);

            WechatMiniProgramLoginDto result = service.resolveLogin(LOGIN_CODE, PHONE_CODE);

            assertThat(result.getOpenId()).isEqualTo("ojhHQ6mfBBbBOyMk6dgbbDTYStyI");
            assertThat(result.getSessionKey()).isEqualTo("qSFYpN7RpaCFyr/IRgaYtw==");
            assertThat(result.getUnionId()).isNull();
            assertThat(result.getPurePhoneNumber()).isEqualTo("13800138000");
        }
    }

    @Test
    void testResolveLogin_WhenAppConfigMissing_ShouldThrowLoginCodeInvalid() {
        stubMiniAccountConfig(new WechatMiniProgramAccountConfig()
                .setAppId(APP_ID)
                .setAppSecret(""));

        assertMiniException(() -> service.resolveLogin(LOGIN_CODE, PHONE_CODE), ResultCode.MINI_WECHAT_LOGIN_CODE_INVALID);

        verifyNoInteractions(client);
    }

    @Test
    void testResolveLogin_WhenMiniAccountConfigReadThrows_ShouldThrowLoginCodeInvalid() {
        when(configService.getValueByKey(eq(MINI_ACCOUNT), ArgumentMatchers.<TypeReference<WechatMiniProgramAccountConfig>>any()))
                .thenThrow(new RuntimeException("bad config"));

        assertMiniException(() -> service.resolveLogin(LOGIN_CODE, PHONE_CODE), ResultCode.MINI_WECHAT_LOGIN_CODE_INVALID);

        verifyNoInteractions(client);
    }

    @Test
    void testResolveLogin_WhenCodeSessionInvalid_ShouldThrowLoginCodeInvalid() {
        stubMiniAccountConfig(miniAccountConfig());
        when(client.code2Session(APP_ID, APP_SECRET, LOGIN_CODE)).thenReturn(new WechatCodeSessionDto()
                .setErrcode(40029)
                .setErrmsg("invalid code"));

        assertMiniException(() -> service.resolveLogin(LOGIN_CODE, PHONE_CODE), ResultCode.MINI_WECHAT_LOGIN_CODE_INVALID);
    }

    @Test
    void testResolveLogin_WhenCodeSessionMissingOpenId_ShouldThrowLoginCodeInvalid() {
        stubMiniAccountConfig(miniAccountConfig());
        when(client.code2Session(APP_ID, APP_SECRET, LOGIN_CODE)).thenReturn(new WechatCodeSessionDto()
                .setSessionKey("session-key"));

        assertMiniException(() -> service.resolveLogin(LOGIN_CODE, PHONE_CODE), ResultCode.MINI_WECHAT_LOGIN_CODE_INVALID);

        verify(client, never()).getAccessToken(APP_ID, APP_SECRET);
    }

    @Test
    void testResolveLogin_WhenAccessTokenInvalid_ShouldThrowLoginCodeInvalid() {
        stubMiniAccountConfig(miniAccountConfig());
        when(client.code2Session(APP_ID, APP_SECRET, LOGIN_CODE)).thenReturn(successSession());
        when(client.getAccessToken(APP_ID, APP_SECRET)).thenReturn(new WechatAccessTokenDto()
                .setErrcode(40001)
                .setErrmsg("invalid credential"));

        try (MockedStatic<RedisUtil> redisMock = mockStatic(RedisUtil.class)) {
            redisMock.when(() -> RedisUtil.getCacheObject(CACHE_KEY)).thenReturn(null);

            assertMiniException(() -> service.resolveLogin(LOGIN_CODE, PHONE_CODE), ResultCode.MINI_WECHAT_LOGIN_CODE_INVALID);
        }
    }

    @Test
    void testResolveLogin_WhenAccessTokenRequestThrows_ShouldThrowLoginCodeInvalid() {
        stubMiniAccountConfig(miniAccountConfig());
        when(client.code2Session(APP_ID, APP_SECRET, LOGIN_CODE)).thenReturn(successSession());
        when(client.getAccessToken(APP_ID, APP_SECRET)).thenThrow(new RuntimeException("wechat unavailable"));

        try (MockedStatic<RedisUtil> redisMock = mockStatic(RedisUtil.class)) {
            redisMock.when(() -> RedisUtil.getCacheObject(CACHE_KEY)).thenReturn(null);

            assertMiniException(() -> service.resolveLogin(LOGIN_CODE, PHONE_CODE), ResultCode.MINI_WECHAT_LOGIN_CODE_INVALID);
        }
    }

    @Test
    void testResolveLogin_WhenAccessTokenExpiresBeforeAhead_ShouldNotCacheToken() {
        stubMiniAccountConfig(miniAccountConfig());
        when(client.code2Session(APP_ID, APP_SECRET, LOGIN_CODE)).thenReturn(successSession());
        when(client.getAccessToken(APP_ID, APP_SECRET)).thenReturn(new WechatAccessTokenDto()
                .setAccessToken("short-lived-token")
                .setExpiresIn(120));
        when(client.getPhoneNumber("short-lived-token", PHONE_CODE)).thenReturn(successPhoneNumber());

        try (MockedStatic<RedisUtil> redisMock = mockStatic(RedisUtil.class)) {
            redisMock.when(() -> RedisUtil.getCacheObject(CACHE_KEY)).thenReturn(null);

            service.resolveLogin(LOGIN_CODE, PHONE_CODE);

            redisMock.verify(() -> RedisUtil.getCacheObject(CACHE_KEY));
            redisMock.verifyNoMoreInteractions();
        }
    }

    @Test
    void testResolveLogin_WhenAccessTokenExpiresInMissing_ShouldNotCacheToken() {
        stubMiniAccountConfig(miniAccountConfig());
        when(client.code2Session(APP_ID, APP_SECRET, LOGIN_CODE)).thenReturn(successSession());
        when(client.getAccessToken(APP_ID, APP_SECRET)).thenReturn(new WechatAccessTokenDto()
                .setAccessToken("missing-expire-token"));
        when(client.getPhoneNumber("missing-expire-token", PHONE_CODE)).thenReturn(successPhoneNumber());

        try (MockedStatic<RedisUtil> redisMock = mockStatic(RedisUtil.class)) {
            redisMock.when(() -> RedisUtil.getCacheObject(CACHE_KEY)).thenReturn(null);

            service.resolveLogin(LOGIN_CODE, PHONE_CODE);

            redisMock.verify(() -> RedisUtil.getCacheObject(CACHE_KEY));
            redisMock.verifyNoMoreInteractions();
        }
    }

    @Test
    void testResolveLogin_WhenPhoneNumberInvalid_ShouldThrowPhoneCodeInvalid() {
        stubMiniAccountConfig(miniAccountConfig());
        when(client.code2Session(APP_ID, APP_SECRET, LOGIN_CODE)).thenReturn(successSession());
        when(client.getPhoneNumber("cached-token", PHONE_CODE)).thenReturn(new WechatPhoneNumberDto()
                .setErrcode(40029)
                .setErrmsg("invalid code"));

        try (MockedStatic<RedisUtil> redisMock = mockStatic(RedisUtil.class)) {
            redisMock.when(() -> RedisUtil.getCacheObject(CACHE_KEY)).thenReturn("cached-token");

            assertMiniException(() -> service.resolveLogin(LOGIN_CODE, PHONE_CODE), ResultCode.MINI_WECHAT_PHONE_CODE_INVALID);
        }
    }

    @Test
    void testResolveLogin_WhenPhoneNumberMissingPurePhone_ShouldThrowPhoneCodeInvalid() {
        stubMiniAccountConfig(miniAccountConfig());
        WechatPhoneNumberDto phoneNumber = successPhoneNumber();
        phoneNumber.getPhoneInfo().setPurePhoneNumber("");
        when(client.code2Session(APP_ID, APP_SECRET, LOGIN_CODE)).thenReturn(successSession());
        when(client.getPhoneNumber("cached-token", PHONE_CODE)).thenReturn(phoneNumber);

        try (MockedStatic<RedisUtil> redisMock = mockStatic(RedisUtil.class)) {
            redisMock.when(() -> RedisUtil.getCacheObject(CACHE_KEY)).thenReturn("cached-token");

            assertMiniException(() -> service.resolveLogin(LOGIN_CODE, PHONE_CODE), ResultCode.MINI_WECHAT_PHONE_CODE_INVALID);
        }
    }

    @Test
    void testResolveLogin_WhenWatermarkAppIdMismatch_ShouldThrowPhoneCodeInvalid() {
        stubMiniAccountConfig(miniAccountConfig());
        WechatPhoneNumberDto phoneNumber = successPhoneNumber();
        phoneNumber.getPhoneInfo().getWatermark().setAppid("other-app-id");
        when(client.code2Session(APP_ID, APP_SECRET, LOGIN_CODE)).thenReturn(successSession());
        when(client.getPhoneNumber("cached-token", PHONE_CODE)).thenReturn(phoneNumber);

        try (MockedStatic<RedisUtil> redisMock = mockStatic(RedisUtil.class)) {
            redisMock.when(() -> RedisUtil.getCacheObject(CACHE_KEY)).thenReturn("cached-token");

            assertMiniException(() -> service.resolveLogin(LOGIN_CODE, PHONE_CODE), ResultCode.MINI_WECHAT_PHONE_CODE_INVALID);
        }
    }

    private void stubMiniAccountConfig(WechatMiniProgramAccountConfig config) {
        when(configService.getValueByKey(eq(MINI_ACCOUNT), ArgumentMatchers.<TypeReference<WechatMiniProgramAccountConfig>>any()))
                .thenReturn(config);
    }

    private WechatMiniProgramAccountConfig miniAccountConfig() {
        return new WechatMiniProgramAccountConfig()
                .setAppId(APP_ID)
                .setAppSecret(APP_SECRET);
    }

    private WechatCodeSessionDto successSession() {
        return new WechatCodeSessionDto()
                .setOpenid("open-id")
                .setUnionid("union-id")
                .setSessionKey("session-key");
    }

    private WechatPhoneNumberDto successPhoneNumber() {
        WechatPhoneNumberDto.Watermark watermark = new WechatPhoneNumberDto.Watermark()
                .setTimestamp(1710000000L)
                .setAppid(APP_ID);
        WechatPhoneNumberDto.PhoneInfo phoneInfo = new WechatPhoneNumberDto.PhoneInfo()
                .setPhoneNumber("+8613800138000")
                .setPurePhoneNumber("13800138000")
                .setCountryCode("86")
                .setWatermark(watermark);
        return new WechatPhoneNumberDto().setPhoneInfo(phoneInfo);
    }

    private void assertMiniException(Runnable action, ResultCode resultCode) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(BusinessRuntimeException.class, exception -> {
                    assertThat(exception.getCode()).isEqualTo(resultCode.getCode());
                    assertThat(exception.getMessage()).isEqualTo(resultCode.getMessage());
                });
    }
}
