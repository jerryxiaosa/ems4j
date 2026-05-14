package info.zhihui.ems.foundation.thirdparty.wechat.service.impl;

import info.zhihui.ems.common.constant.ResultCode;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.components.redis.utils.RedisUtil;
import info.zhihui.ems.foundation.thirdparty.wechat.client.WechatMiniProgramClient;
import info.zhihui.ems.foundation.thirdparty.wechat.config.WechatMiniProgramProperties;
import info.zhihui.ems.foundation.thirdparty.wechat.dto.WechatAccessTokenDto;
import info.zhihui.ems.foundation.thirdparty.wechat.dto.WechatCodeSessionDto;
import info.zhihui.ems.foundation.thirdparty.wechat.dto.WechatMiniProgramLoginDto;
import info.zhihui.ems.foundation.thirdparty.wechat.dto.WechatPhoneNumberDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    private WechatMiniProgramProperties properties;
    @InjectMocks
    private WechatMiniProgramServiceImpl service;

    @BeforeEach
    void setUp() {
        properties = new WechatMiniProgramProperties();
        properties.setAppId(APP_ID);
        properties.setAppSecret(APP_SECRET);
        properties.setAccessTokenExpireAheadSeconds(300);
        service = new WechatMiniProgramServiceImpl(client, properties);
    }

    @Test
    void testResolveLogin_WhenWechatResponsesValid_ShouldReturnMiniProgramLogin() {
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
            assertThat(result.getPhoneNumber()).isEqualTo("+8613800138000");
            assertThat(result.getPurePhoneNumber()).isEqualTo("13800138000");
            assertThat(result.getCountryCode()).isEqualTo("86");
            redisMock.verify(() -> RedisUtil.setCacheObject(CACHE_KEY, "access-token", Duration.ofSeconds(6900)));
        }
    }

    @Test
    void testResolveLogin_WhenAccessTokenCached_ShouldReuseCachedToken() {
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
    void testResolveLogin_WhenAppConfigMissing_ShouldThrowLoginCodeInvalid() {
        properties.setAppSecret("");

        assertMiniException(() -> service.resolveLogin(LOGIN_CODE, PHONE_CODE), ResultCode.MINI_WECHAT_LOGIN_CODE_INVALID);

        verifyNoInteractions(client);
    }

    @Test
    void testResolveLogin_WhenCodeSessionInvalid_ShouldThrowLoginCodeInvalid() {
        when(client.code2Session(APP_ID, APP_SECRET, LOGIN_CODE)).thenReturn(new WechatCodeSessionDto()
                .setErrcode(40029)
                .setErrmsg("invalid code"));

        assertMiniException(() -> service.resolveLogin(LOGIN_CODE, PHONE_CODE), ResultCode.MINI_WECHAT_LOGIN_CODE_INVALID);
    }

    @Test
    void testResolveLogin_WhenAccessTokenInvalid_ShouldThrowLoginCodeInvalid() {
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
        when(client.code2Session(APP_ID, APP_SECRET, LOGIN_CODE)).thenReturn(successSession());
        when(client.getAccessToken(APP_ID, APP_SECRET)).thenThrow(new RuntimeException("wechat unavailable"));

        try (MockedStatic<RedisUtil> redisMock = mockStatic(RedisUtil.class)) {
            redisMock.when(() -> RedisUtil.getCacheObject(CACHE_KEY)).thenReturn(null);

            assertMiniException(() -> service.resolveLogin(LOGIN_CODE, PHONE_CODE), ResultCode.MINI_WECHAT_LOGIN_CODE_INVALID);
        }
    }

    @Test
    void testResolveLogin_WhenAccessTokenExpiresBeforeAhead_ShouldNotCacheToken() {
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
    void testResolveLogin_WhenWatermarkAppIdMismatch_ShouldThrowPhoneCodeInvalid() {
        WechatPhoneNumberDto phoneNumber = successPhoneNumber();
        phoneNumber.getPhoneInfo().getWatermark().setAppid("other-app-id");
        when(client.code2Session(APP_ID, APP_SECRET, LOGIN_CODE)).thenReturn(successSession());
        when(client.getPhoneNumber("cached-token", PHONE_CODE)).thenReturn(phoneNumber);

        try (MockedStatic<RedisUtil> redisMock = mockStatic(RedisUtil.class)) {
            redisMock.when(() -> RedisUtil.getCacheObject(CACHE_KEY)).thenReturn("cached-token");

            assertMiniException(() -> service.resolveLogin(LOGIN_CODE, PHONE_CODE), ResultCode.MINI_WECHAT_PHONE_CODE_INVALID);
        }
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
