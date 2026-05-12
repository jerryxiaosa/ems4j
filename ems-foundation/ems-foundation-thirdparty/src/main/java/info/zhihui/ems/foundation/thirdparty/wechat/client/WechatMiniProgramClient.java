package info.zhihui.ems.foundation.thirdparty.wechat.client;

import info.zhihui.ems.foundation.thirdparty.wechat.dto.WechatAccessTokenDto;
import info.zhihui.ems.foundation.thirdparty.wechat.dto.WechatCodeSessionDto;
import info.zhihui.ems.foundation.thirdparty.wechat.dto.WechatPhoneNumberDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

/**
 * 微信小程序官方接口客户端。
 */
@Component
public class WechatMiniProgramClient {

    private static final String CODE_SESSION_URL = "https://api.weixin.qq.com/sns/jscode2session";
    private static final String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token";
    private static final String PHONE_NUMBER_URL = "https://api.weixin.qq.com/wxa/business/getuserphonenumber";

    private final RestClient restClient;

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
        return restClient.get()
                .uri(url)
                .retrieve()
                .body(WechatCodeSessionDto.class);
    }

    public WechatAccessTokenDto getAccessToken(String appId, String appSecret) {
        String url = UriComponentsBuilder.fromUriString(ACCESS_TOKEN_URL)
                .queryParam("grant_type", "client_credential")
                .queryParam("appid", appId)
                .queryParam("secret", appSecret)
                .build()
                .toUriString();
        return restClient.get()
                .uri(url)
                .retrieve()
                .body(WechatAccessTokenDto.class);
    }

    public WechatPhoneNumberDto getPhoneNumber(String accessToken, String phoneCode) {
        String url = UriComponentsBuilder.fromUriString(PHONE_NUMBER_URL)
                .queryParam("access_token", accessToken)
                .build()
                .toUriString();
        return restClient.post()
                .uri(url)
                .body(Map.of("code", phoneCode))
                .retrieve()
                .body(WechatPhoneNumberDto.class);
    }
}
