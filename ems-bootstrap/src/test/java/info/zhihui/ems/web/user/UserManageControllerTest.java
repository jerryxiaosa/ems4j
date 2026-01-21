package info.zhihui.ems.web.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.config.satoken.SaWebConfig;
import info.zhihui.ems.web.user.biz.UserManageBiz;
import info.zhihui.ems.web.user.controller.UserManageController;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserManageController.class)
class UserManageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserManageBiz userManageBiz;

    @MockitoBean
    private SaWebConfig saWebConfig;

    @Test
    @DisplayName("分页查询用户")
    void testFindUserPage() throws Exception {
        UserVo userVo = new UserVo().setId(1).setUserName("admin");
        when(userManageBiz.findUserPage(any(UserQueryVo.class), eq(1), eq(10)))
                .thenReturn(new PageResult<UserVo>().setPageNum(1).setPageSize(10).setTotal(1L).setList(List.of(userVo)));

        mockMvc.perform(get("/users/page")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    @DisplayName("查询用户列表")
    void testFindUserList() throws Exception {
        UserVo userVo = new UserVo().setId(1).setUserName("admin");
        when(userManageBiz.findUserList(any(UserQueryVo.class))).thenReturn(List.of(userVo));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].userName").value("admin"));
    }

    @Test
    @DisplayName("查询用户详情")
    void testGetUser() throws Exception {
        UserVo userVo = new UserVo().setId(1).setUserName("admin");
        when(userManageBiz.getUser(1)).thenReturn(userVo);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userName").value("admin"));
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

        mockMvc.perform(post("/users")
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

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateVo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("删除用户")
    void testDeleteUser() throws Exception {
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("修改密码")
    void testUpdatePassword() throws Exception {
        UserPasswordUpdateVo passwordVo = new UserPasswordUpdateVo();
        passwordVo.setOldPassword("oldPass");
        passwordVo.setNewPassword("newPass123");

        mockMvc.perform(put("/users/1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordVo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
