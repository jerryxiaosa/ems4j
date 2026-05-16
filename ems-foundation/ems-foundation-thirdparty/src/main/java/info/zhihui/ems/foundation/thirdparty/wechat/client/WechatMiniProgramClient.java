package info.zhihui.ems.foundation.thirdparty.wechat.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.zhihui.ems.foundation.thirdparty.wechat.dto.WechatAccessTokenDto;
import info.zhihui.ems.foundation.thirdparty.wechat.dto.WechatCodeSessionDto;
import info.zhihui.ems.foundation.thirdparty.wechat.dto.WechatPhoneNumberDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

/**
 * 微信小程序官方接口客户端。
 */
@Slf4j
@Component
public class WechatMiniProgramClient {

    private static final String CODE_SESSION_URL = "https://api.weixin.qq.com/sns/jscode2session";
    private static final String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token";
    private static final String PHONE_NUMBER_URL = "https://api.weixin.qq.com/wxa/business/getuserphonenumber";

    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public WechatMiniProgramClient(@Qualifier("wechatRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public WechatCodeSessionDto code2Session(String appId, String appSecret, String loginCode) {
        String url = UriComponentsBuilder.fromUriString(CODE_SESSION_URL)
                .queryParam("appid", appId)
                .queryParam("secret", appSecret)
                .queryParam("js_code", loginCode)
                .queryParam("grant_type", "authorization_code")
                .build()
                .toUriString();
        String rawResponse = restClient.get()
                .uri(url)
                .retrieve()
                .body(String.class);
        return parseWechatResponse("code2Session", rawResponse, WechatCodeSessionDto.class);
    }

    public WechatAccessTokenDto getAccessToken(String appId, String appSecret) {
        String url = UriComponentsBuilder.fromUriString(ACCESS_TOKEN_URL)
                .queryParam("grant_type", "client_credential")
                .queryParam("appid", appId)
                .queryParam("secret", appSecret)
                .build()
                .toUriString();
        String rawResponse = restClient.get()
                .uri(url)
                .retrieve()
                .body(String.class);
        return parseWechatResponse("getAccessToken", rawResponse, WechatAccessTokenDto.class);
    }

    public WechatPhoneNumberDto getPhoneNumber(String accessToken, String phoneCode) {
        String url = UriComponentsBuilder.fromUriString(PHONE_NUMBER_URL)
                .queryParam("access_token", accessToken)
                .build()
                .toUriString();
        String rawResponse = restClient.post()
                .uri(url)
                .body(Map.of("code", phoneCode))
                .retrieve()
                .body(String.class);
        return parseWechatResponse("getPhoneNumber", rawResponse, WechatPhoneNumberDto.class);
    }

    private <T> T parseWechatResponse(String action, String rawResponse, Class<T> responseType) {
        log.debug("微信小程序 {} 原始响应：{}", action, rawResponse);
        if (!StringUtils.hasText(rawResponse)) {
            log.warn("微信小程序 {} 原始响应为空", action);
            throw new IllegalStateException("微信小程序接口响应为空：" + action);
        }
        try {
            return objectMapper.readValue(rawResponse, responseType);
        } catch (JsonProcessingException e) {
            log.warn("微信小程序 {} 原始响应解析失败，response={}", action, rawResponse, e);
            throw new IllegalStateException("微信小程序接口响应解析失败：" + action, e);
        }
    }
}
