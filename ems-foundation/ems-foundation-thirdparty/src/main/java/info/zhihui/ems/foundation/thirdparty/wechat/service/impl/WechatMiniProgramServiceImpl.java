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
import info.zhihui.ems.foundation.thirdparty.wechat.service.WechatMiniProgramService;
import info.zhihui.ems.foundation.system.service.ConfigService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.Objects;

import static info.zhihui.ems.foundation.system.constant.SystemConfigConstant.MINI_ACCOUNT;

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
    private final ConfigService configService;

    /**
     * 解析微信小程序登录身份。
     *
     * <p>该方法封装小程序登录需要的两段微信接口调用：
     * 先用 {@code loginCode} 换取 openId/sessionKey，再用 {@code phoneCode}
     * 换取用户手机号。业务层只需要消费统一的 {@link WechatMiniProgramLoginDto}，
     * 不直接感知微信接口细节。</p>
     *
     * @param loginCode 小程序端 {@code wx.login} 返回的登录凭证
     * @param phoneCode 小程序端手机号授权返回的一次性凭证
     * @return 微信小程序登录身份和手机号信息
     */
    @Override
    public WechatMiniProgramLoginDto resolveLogin(@NotBlank String loginCode, @NotBlank String phoneCode) {
        WechatMiniProgramAccountConfig accountConfig = getMiniAccountConfig();
        WechatCodeSessionDto sessionDto = code2Session(accountConfig, loginCode);
        String accessToken = getAccessToken(accountConfig);
        WechatPhoneNumberDto phoneNumberDto = getPhoneNumber(accountConfig.getAppId(), accessToken, phoneCode);
        WechatPhoneNumberDto.PhoneInfo phoneInfo = phoneNumberDto.getPhoneInfo();

        return new WechatMiniProgramLoginDto()
                .setAppId(accountConfig.getAppId())
                .setOpenId(sessionDto.getOpenid())
                .setUnionId(sessionDto.getUnionid())
                .setSessionKey(sessionDto.getSessionKey())
                .setPhoneNumber(phoneInfo.getPhoneNumber())
                .setPurePhoneNumber(phoneInfo.getPurePhoneNumber())
                .setCountryCode(phoneInfo.getCountryCode());
    }

    /**
     * 读取并校验微信小程序账号配置。
     *
     * <p>appId 和 appSecret 是调用微信登录、手机号接口的前置条件。
     * 缺失时统一返回小程序登录凭证无效，避免把配置细节暴露给前端。</p>
     */
    private WechatMiniProgramAccountConfig getMiniAccountConfig() {
        try {
            WechatMiniProgramAccountConfig accountConfig = configService.getValueByKey(MINI_ACCOUNT, new TypeReference<>() {
            });
            if (accountConfig == null || !StringUtils.hasText(accountConfig.getAppId()) ||
                    !StringUtils.hasText(accountConfig.getAppSecret())) {
                log.warn("微信小程序账号配置缺失，key={}", MINI_ACCOUNT);
                throw miniWechatLoginCodeInvalid();
            }
            return accountConfig;
        } catch (BusinessRuntimeException e) {
            if (Objects.equals(e.getCode(), ResultCode.MINI_WECHAT_LOGIN_CODE_INVALID.getCode())) {
                throw e;
            }
            log.warn("读取微信小程序账号配置失败，key={}", MINI_ACCOUNT, e);
            throw miniWechatLoginCodeInvalid();
        } catch (Exception e) {
            log.warn("读取微信小程序账号配置失败，key={}", MINI_ACCOUNT, e);
            throw miniWechatLoginCodeInvalid();
        }
    }

    /**
     * 使用登录凭证换取微信小程序会话信息。
     *
     * <p>微信正常成功时 {@code errcode} 可能为空或为 0，因此成功判断统一交给
     * {@link #isSuccess(Integer)}。openId 是后续第三方身份绑定和小程序支付的基础，
     * 所以这里必须校验非空。</p>
     *
     * @param loginCode 小程序登录凭证
     * @return 微信返回的小程序会话信息
     */
    private WechatCodeSessionDto code2Session(WechatMiniProgramAccountConfig accountConfig, String loginCode) {
        try {
            WechatCodeSessionDto dto = client.code2Session(accountConfig.getAppId(), accountConfig.getAppSecret(), loginCode);
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

    /**
     * 获取并缓存微信接口调用凭证。
     *
     * <p>access_token 用于调用获取手机号接口。这里按 appId 维度缓存，
     * 并在微信过期时间前提前失效，避免临界时间使用到即将过期的 token。</p>
     *
     * @return 有效的微信 access_token
     */
    private String getAccessToken(WechatMiniProgramAccountConfig accountConfig) {
        String cacheKey = ACCESS_TOKEN_CACHE_KEY_PREFIX + accountConfig.getAppId();
        String cachedToken = RedisUtil.getCacheObject(cacheKey);
        if (StringUtils.hasText(cachedToken)) {
            return cachedToken;
        }

        try {
            WechatAccessTokenDto dto = client.getAccessToken(accountConfig.getAppId(), accountConfig.getAppSecret());
            if (dto == null || !isSuccess(dto.getErrcode()) || !StringUtils.hasText(dto.getAccessToken())) {
                log.warn("获取微信小程序 access_token 失败，errcode={}, errmsg={}", dto == null ? null : dto.getErrcode(), dto == null ? null : dto.getErrmsg());
                throw miniWechatLoginCodeInvalid();
            }

            cacheAccessTokenIfSafe(cacheKey, dto);
            return dto.getAccessToken();
        } catch (BusinessRuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.warn("调用微信小程序 access_token 接口失败", e);
            throw miniWechatLoginCodeInvalid();
        }
    }

    private void cacheAccessTokenIfSafe(String cacheKey, WechatAccessTokenDto dto) {
        Integer expiresIn = dto.getExpiresIn();
        if (expiresIn == null || expiresIn <= 0) {
            log.warn("微信小程序 access_token 过期时间无效，不写入缓存，expiresIn={}", expiresIn);
            return;
        }
        int expireAheadSeconds = Math.max(0, Objects.requireNonNullElse(properties.getAccessTokenExpireAheadSeconds(), 0));
        int cacheSeconds = expiresIn - expireAheadSeconds;
        if (cacheSeconds <= 0) {
            log.warn("微信小程序 access_token 剩余有效期不足，不写入缓存，expiresIn={}, expireAheadSeconds={}", expiresIn, expireAheadSeconds);
            return;
        }
        RedisUtil.setCacheObject(cacheKey, dto.getAccessToken(), Duration.ofSeconds(cacheSeconds));
    }

    private BusinessRuntimeException miniWechatLoginCodeInvalid() {
        return new BusinessRuntimeException(ResultCode.MINI_WECHAT_LOGIN_CODE_INVALID.getCode(),
                ResultCode.MINI_WECHAT_LOGIN_CODE_INVALID.getMessage());
    }

    /**
     * 使用手机号凭证换取微信绑定手机号。
     *
     * <p>业务用户匹配依赖不带区号的纯手机号 {@code purePhoneNumber}，
     * 因此该字段为空时视为手机号凭证无效。接口返回后还会校验 watermark，
     * 确认手机号数据确实来自当前小程序 appId。</p>
     *
     * @param accessToken 微信接口调用凭证
     * @param phoneCode 小程序手机号授权返回的一次性凭证
     * @return 微信返回的手机号信息
     */
    private WechatPhoneNumberDto getPhoneNumber(String appId, String accessToken, String phoneCode) {
        try {
            WechatPhoneNumberDto dto = client.getPhoneNumber(accessToken, phoneCode);
            if (dto == null || !isSuccess(dto.getErrcode()) || dto.getPhoneInfo() == null ||
                    !StringUtils.hasText(dto.getPhoneInfo().getPurePhoneNumber())) {
                log.warn("获取微信小程序手机号失败，errcode={}, errmsg={}", dto == null ? null : dto.getErrcode(), dto == null ? null : dto.getErrmsg());
                throw new BusinessRuntimeException(ResultCode.MINI_WECHAT_PHONE_CODE_INVALID.getCode(),
                        ResultCode.MINI_WECHAT_PHONE_CODE_INVALID.getMessage());
            }
            validateWatermark(appId, dto);
            return dto;
        } catch (BusinessRuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.warn("调用微信小程序手机号接口失败", e);
            throw new BusinessRuntimeException(ResultCode.MINI_WECHAT_PHONE_CODE_INVALID.getCode(),
                    ResultCode.MINI_WECHAT_PHONE_CODE_INVALID.getMessage());
        }
    }

    /**
     * 校验手机号数据水印。
     *
     * <p>微信手机号响应里的 watermark 会带上 appId。
     * 这里要求它必须等于当前配置的 appId，防止错误小程序或错误配置下的数据被继续使用。</p>
     *
     * @param dto 微信手机号响应
     */
    private void validateWatermark(String appId, WechatPhoneNumberDto dto) {
        WechatPhoneNumberDto.Watermark watermark = dto.getPhoneInfo().getWatermark();
        if (watermark == null || !appId.equals(watermark.getAppid())) {
            log.warn("微信小程序手机号 watermark 校验失败，appid={}", watermark == null ? null : watermark.getAppid());
            throw new BusinessRuntimeException(ResultCode.MINI_WECHAT_PHONE_CODE_INVALID.getCode(),
                    ResultCode.MINI_WECHAT_PHONE_CODE_INVALID.getMessage());
        }
    }

    /**
     * 判断微信接口是否调用成功。
     *
     * <p>部分微信接口成功响应不会返回 {@code errcode}，因此空值和 0 都按成功处理。</p>
     */
    private boolean isSuccess(Integer errcode) {
        return errcode == null || SUCCESS_CODE == errcode;
    }
}
