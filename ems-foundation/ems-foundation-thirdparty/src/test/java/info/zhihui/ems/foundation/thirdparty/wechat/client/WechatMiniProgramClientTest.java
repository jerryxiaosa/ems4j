package info.zhihui.ems.foundation.thirdparty.wechat.client;

import info.zhihui.ems.foundation.thirdparty.wechat.dto.WechatCodeSessionDto;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class WechatMiniProgramClientTest {

    @Test
    void testCode2Session_WhenWechatReturnsTextPlainJson_ShouldParseRawResponse() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        WechatMiniProgramClient client = new WechatMiniProgramClient(builder.build());

        server.expect(requestTo("https://api.weixin.qq.com/sns/jscode2session?appid=wx-app-id&secret=app-secret&js_code=login-code&grant_type=authorization_code"))
                .andRespond(withSuccess("""
                        {"session_key":"qSFYpN7RpaCFyr/IRgaYtw==","openid":"ojhHQ6mfBBbBOyMk6dgbbDTYStyI"}
                        """, MediaType.TEXT_PLAIN));

        WechatCodeSessionDto result = client.code2Session("wx-app-id", "app-secret", "login-code");

        assertThat(result.getOpenid()).isEqualTo("ojhHQ6mfBBbBOyMk6dgbbDTYStyI");
        assertThat(result.getSessionKey()).isEqualTo("qSFYpN7RpaCFyr/IRgaYtw==");
        assertThat(result.getUnionid()).isNull();
        assertThat(result.getErrcode()).isNull();
        assertThat(result.getErrmsg()).isNull();
        server.verify();
    }
}
