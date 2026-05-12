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
import info.zhihui.ems.foundation.thirdparty.wechat.service.WechatMiniProgramService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.Objects;

/**
 * 微信小程序服务实现。
 */
@Service
@Validated
@RequiredArgsConstructor
@Slf4j
public class WechatMiniProgramServiceImpl implements WechatMiniProgramService {

    private static final int SUCCESS_CODE = 0;
    private static final String ACCESS_TOKEN_CACHE_KEY_PREFIX = "third-party:wechat:mini-program:access-token:";

    private final WechatMiniProgramClient client;
    private final WechatMiniProgramProperties properties;

    @Override
    public WechatMiniProgramLoginDto resolveLogin(@NotBlank String loginCode, @NotBlank String phoneCode) {
        validateProperties();
        WechatCodeSessionDto sessionDto = code2Session(loginCode);
        String accessToken = getAccessToken();
        WechatPhoneNumberDto phoneNumberDto = getPhoneNumber(accessToken, phoneCode);
        WechatPhoneNumberDto.PhoneInfo phoneInfo = phoneNumberDto.getPhoneInfo();

        return new WechatMiniProgramLoginDto()
                .setAppId(properties.getAppId())
                .setOpenId(sessionDto.getOpenid())
                .setUnionId(sessionDto.getUnionid())
                .setSessionKey(sessionDto.getSessionKey())
                .setPhoneNumber(phoneInfo.getPhoneNumber())
                .setPurePhoneNumber(phoneInfo.getPurePhoneNumber())
                .setCountryCode(phoneInfo.getCountryCode());
    }

    private void validateProperties() {
        if (!StringUtils.hasText(properties.getAppId()) || !StringUtils.hasText(properties.getAppSecret())) {
            log.warn("微信小程序 appId 或 appSecret 未配置");
            throw new BusinessRuntimeException(ResultCode.MINI_WECHAT_LOGIN_CODE_INVALID.getCode(),
                    ResultCode.MINI_WECHAT_LOGIN_CODE_INVALID.getMessage());
        }
    }

    private WechatCodeSessionDto code2Session(String loginCode) {
        try {
            WechatCodeSessionDto dto = client.code2Session(properties.getAppId(), properties.getAppSecret(), loginCode);
            if (dto == null || !isSuccess(dto.getErrcode()) || !StringUtils.hasText(dto.getOpenid())) {
                log.warn("微信小程序登录凭证校验失败，errcode={}, errmsg={}", dto == null ? null : dto.getErrcode(), dto == null ? null : dto.getErrmsg());
                throw new BusinessRuntimeException(ResultCode.MINI_WECHAT_LOGIN_CODE_INVALID.getCode(),
                        ResultCode.MINI_WECHAT_LOGIN_CODE_INVALID.getMessage());
            }
            return dto;
        } catch (BusinessRuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.warn("调用微信小程序登录凭证校验接口失败", e);
            throw new BusinessRuntimeException(ResultCode.MINI_WECHAT_LOGIN_CODE_INVALID.getCode(),
                    ResultCode.MINI_WECHAT_LOGIN_CODE_INVALID.getMessage());
        }
    }

    private String getAccessToken() {
        String cacheKey = ACCESS_TOKEN_CACHE_KEY_PREFIX + properties.getAppId();
        String cachedToken = RedisUtil.getCacheObject(cacheKey);
        if (StringUtils.hasText(cachedToken)) {
            return cachedToken;
        }

        WechatAccessTokenDto dto = client.getAccessToken(properties.getAppId(), properties.getAppSecret());
        if (dto == null || !isSuccess(dto.getErrcode()) || !StringUtils.hasText(dto.getAccessToken())) {
            log.warn("获取微信小程序 access_token 失败，errcode={}, errmsg={}", dto == null ? null : dto.getErrcode(), dto == null ? null : dto.getErrmsg());
            throw new BusinessRuntimeException(ResultCode.MINI_WECHAT_LOGIN_CODE_INVALID.getCode(),
                    ResultCode.MINI_WECHAT_LOGIN_CODE_INVALID.getMessage());
        }

        int expiresIn = Objects.requireNonNullElse(dto.getExpiresIn(), 7200);
        int cacheSeconds = Math.max(60, expiresIn - properties.getAccessTokenExpireAheadSeconds());
        RedisUtil.setCacheObject(cacheKey, dto.getAccessToken(), Duration.ofSeconds(cacheSeconds));
        return dto.getAccessToken();
    }

    private WechatPhoneNumberDto getPhoneNumber(String accessToken, String phoneCode) {
        try {
            WechatPhoneNumberDto dto = client.getPhoneNumber(accessToken, phoneCode);
            if (dto == null || !isSuccess(dto.getErrcode()) || dto.getPhoneInfo() == null ||
                    !StringUtils.hasText(dto.getPhoneInfo().getPurePhoneNumber())) {
                log.warn("获取微信小程序手机号失败，errcode={}, errmsg={}", dto == null ? null : dto.getErrcode(), dto == null ? null : dto.getErrmsg());
                throw new BusinessRuntimeException(ResultCode.MINI_WECHAT_PHONE_CODE_INVALID.getCode(),
                        ResultCode.MINI_WECHAT_PHONE_CODE_INVALID.getMessage());
            }
            validateWatermark(dto);
            return dto;
        } catch (BusinessRuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.warn("调用微信小程序手机号接口失败", e);
            throw new BusinessRuntimeException(ResultCode.MINI_WECHAT_PHONE_CODE_INVALID.getCode(),
                    ResultCode.MINI_WECHAT_PHONE_CODE_INVALID.getMessage());
        }
    }

    private void validateWatermark(WechatPhoneNumberDto dto) {
        WechatPhoneNumberDto.Watermark watermark = dto.getPhoneInfo().getWatermark();
        if (watermark == null || !properties.getAppId().equals(watermark.getAppid())) {
            log.warn("微信小程序手机号 watermark 校验失败，appid={}", watermark == null ? null : watermark.getAppid());
            throw new BusinessRuntimeException(ResultCode.MINI_WECHAT_PHONE_CODE_INVALID.getCode(),
                    ResultCode.MINI_WECHAT_PHONE_CODE_INVALID.getMessage());
        }
    }

    private boolean isSuccess(Integer errcode) {
        return errcode == null || SUCCESS_CODE == errcode;
    }
}
