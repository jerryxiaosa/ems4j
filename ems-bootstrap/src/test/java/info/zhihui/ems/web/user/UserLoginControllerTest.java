package info.zhihui.ems.web.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import info.zhihui.ems.config.satoken.SaWebConfig;
import info.zhihui.ems.web.user.biz.UserLoginBiz;
import info.zhihui.ems.web.user.controller.UserLoginController;
import info.zhihui.ems.web.user.vo.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserLoginController.class)
class UserLoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserLoginBiz userLoginBiz;

    @MockitoBean
    private SaWebConfig saWebConfig;

    @Test
    @DisplayName("获取验证码")
    void testGetCaptcha() throws Exception {
        CaptchaVo captcha = new CaptchaVo();
        captcha.setCaptchaKey("key123");
        captcha.setImg("imgBase64");
        when(userLoginBiz.getCaptcha()).thenReturn(captcha);

        mockMvc.perform(get("/users/captcha"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.captchaKey").value("key123"));
    }
    @Test
    @DisplayName("登出成功")
    void testLogout() throws Exception {
        mockMvc.perform(post("/users/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("登录成功")
    void testLogin() throws Exception {
        LoginResponseVo responseVo = new LoginResponseVo()
                .setAccessToken("token")
                .setRefreshToken("refresh")
                .setExpireIn(3600L);
        when(userLoginBiz.login(any())).thenReturn(responseVo);

        LoginRequestVo requestVo = new LoginRequestVo();
        requestVo.setUserName("admin");
        requestVo.setPassword("Pass123!");
        requestVo.setCaptchaKey("captcha");
        requestVo.setCaptchaValue("1234");

        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestVo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("token"));
    }

    @Test
    @DisplayName("查询当前用户菜单-指定来源")
    void testFindCurrentUserMenus() throws Exception {
        UserMenuVo menuVo = new UserMenuVo().setId(1).setMenuName("系统管理").setPath("/xxx");
        when(userLoginBiz.findCurrentUserMenus(1)).thenReturn(List.of(menuVo));

        mockMvc.perform(get("/users/current/menus")
                        .param("source", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].menuName").value("系统管理"));
    }

    @Test
    @DisplayName("查询当前登录用户信息")
    void testFindCurrentUser() throws Exception {
        UserVo userVo = new UserVo().setId(1).setUserName("admin");
        when(userLoginBiz.findCurrentUser()).thenReturn(userVo);

        mockMvc.perform(get("/users/current")
                        .requestAttr("loginUserId", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userName").value("admin"));
    }
}
