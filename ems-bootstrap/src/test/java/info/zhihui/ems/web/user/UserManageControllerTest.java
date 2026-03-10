package info.zhihui.ems.web.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.config.satoken.SaWebConfig;
import info.zhihui.ems.components.translate.engine.TranslateEngine;
import info.zhihui.ems.components.translate.engine.TranslateMetadataCache;
import info.zhihui.ems.components.translate.resolver.EnumLabelResolver;
import info.zhihui.ems.components.translate.web.advice.ResponseTranslateAdvice;
import info.zhihui.ems.foundation.organization.bo.OrganizationBo;
import info.zhihui.ems.foundation.organization.service.OrganizationService;
import info.zhihui.ems.web.common.formatter.PhoneMaskFormatter;
import info.zhihui.ems.web.common.resolver.OrganizationNameResolver;
import info.zhihui.ems.web.user.biz.UserManageBiz;
import info.zhihui.ems.web.user.controller.UserManageController;
import info.zhihui.ems.web.user.vo.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserManageController.class)
@Import({
        ResponseTranslateAdvice.class,
        TranslateEngine.class,
        TranslateMetadataCache.class,
        EnumLabelResolver.class,
        OrganizationNameResolver.class,
        PhoneMaskFormatter.class
})
class UserManageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserManageBiz userManageBiz;

    @MockitoBean
    private OrganizationService organizationService;

    @MockitoBean
    private SaWebConfig saWebConfig;

    @Test
    @DisplayName("分页查询用户")
    void testFindUserPage() throws Exception {
        UserVo userVo = new UserVo()
                .setId(1)
                .setUserName("admin")
                .setOrganizationId(1)
                .setCertificatesType(1)
                .setCreateTime(LocalDateTime.of(2026, 3, 10, 9, 30, 15))
                .setUpdateTime(LocalDateTime.of(2026, 3, 10, 18, 45, 20))
                .setUserPhone("13800138000");
        when(userManageBiz.findUserPage(any(UserQueryVo.class), eq(1), eq(10)))
                .thenReturn(new PageResult<UserVo>().setPageNum(1).setPageSize(10).setTotal(1L).setList(List.of(userVo)));
        when(organizationService.findOrganizationList(any()))
                .thenReturn(List.of(new OrganizationBo().setId(1).setName("机构A")));

        mockMvc.perform(get("/v1/users/page")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].organizationName").value("机构A"))
                .andExpect(jsonPath("$.data.list[0].certificatesTypeText").value("身份证"))
                .andExpect(jsonPath("$.data.list[0].createTime").value("2026-03-10 09:30:15"))
                .andExpect(jsonPath("$.data.list[0].updateTime").value("2026-03-10 18:45:20"))
                .andExpect(jsonPath("$.data.list[0].userPhone").value("138****8000"));
    }

    @Test
    @DisplayName("查询用户列表")
    void testFindUserList() throws Exception {
        UserVo userVo = new UserVo()
                .setId(1)
                .setUserName("admin")
                .setOrganizationId(1)
                .setCertificatesType(1)
                .setCreateTime(LocalDateTime.of(2026, 3, 10, 9, 30, 15))
                .setUpdateTime(LocalDateTime.of(2026, 3, 10, 18, 45, 20))
                .setUserPhone("13800138000");
        when(userManageBiz.findUserList(any(UserQueryVo.class))).thenReturn(List.of(userVo));
        when(organizationService.findOrganizationList(any()))
                .thenReturn(List.of(new OrganizationBo().setId(1).setName("机构A")));

        mockMvc.perform(get("/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].userName").value("admin"))
                .andExpect(jsonPath("$.data[0].organizationName").value("机构A"))
                .andExpect(jsonPath("$.data[0].certificatesTypeText").value("身份证"))
                .andExpect(jsonPath("$.data[0].createTime").value("2026-03-10 09:30:15"))
                .andExpect(jsonPath("$.data[0].updateTime").value("2026-03-10 18:45:20"))
                .andExpect(jsonPath("$.data[0].userPhone").value("138****8000"));
    }

    @Test
    @DisplayName("查询用户详情")
    void testGetUser() throws Exception {
        UserVo userVo = new UserVo()
                .setId(1)
                .setUserName("admin")
                .setOrganizationId(1)
                .setCertificatesType(1)
                .setCreateTime(LocalDateTime.of(2026, 3, 10, 9, 30, 15))
                .setUpdateTime(LocalDateTime.of(2026, 3, 10, 18, 45, 20))
                .setUserPhone("13800138000");
        when(userManageBiz.getUser(1)).thenReturn(userVo);
        when(organizationService.findOrganizationList(any()))
                .thenReturn(List.of(new OrganizationBo().setId(1).setName("机构A")));

        mockMvc.perform(get("/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userName").value("admin"))
                .andExpect(jsonPath("$.data.organizationName").value("机构A"))
                .andExpect(jsonPath("$.data.certificatesTypeText").value("身份证"))
                .andExpect(jsonPath("$.data.createTime").value("2026-03-10 09:30:15"))
                .andExpect(jsonPath("$.data.updateTime").value("2026-03-10 18:45:20"))
                .andExpect(jsonPath("$.data.userPhone").value("138****8000"));
    }

    @Test
    @DisplayName("新增用户")
    void testCreateUser() throws Exception {
        when(userManageBiz.createUser(any(UserCreateVo.class))).thenReturn(99);

        UserCreateVo createVo = new UserCreateVo();
        createVo.setUserName("newUser");
        createVo.setPassword("Pass123!");
        createVo.setRealName("New User");
        createVo.setUserPhone("13800138000");
        createVo.setUserGender(1);
        createVo.setOrganizationId(1);

        mockMvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createVo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(99));
    }

    @Test
    @DisplayName("更新用户")
    void testUpdateUser() throws Exception {
        UserUpdateVo updateVo = new UserUpdateVo();
        updateVo.setRealName("Updated");
        updateVo.setUserGender(2);

        mockMvc.perform(put("/v1/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateVo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("删除用户")
    void testDeleteUser() throws Exception {
        mockMvc.perform(delete("/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("修改密码")
    void testUpdatePassword() throws Exception {
        UserPasswordUpdateVo passwordVo = new UserPasswordUpdateVo();
        passwordVo.setOldPassword("oldPass");
        passwordVo.setNewPassword("newPass123");

        mockMvc.perform(put("/v1/users/1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(passwordVo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("重置用户密码")
    void testResetPassword() throws Exception {
        UserPasswordResetVo resetVo = new UserPasswordResetVo();
        resetVo.setNewPassword("newPass123");

        mockMvc.perform(put("/v1/users/1/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetVo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("重置用户密码-参数校验失败")
    void testResetPassword_ValidationFailed() throws Exception {
        UserPasswordResetVo resetVo = new UserPasswordResetVo();
        resetVo.setNewPassword("");

        mockMvc.perform(put("/v1/users/1/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetVo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(-102001));
    }
}
