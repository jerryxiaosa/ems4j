package info.zhihui.ems.web.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.config.satoken.SaWebConfig;
import info.zhihui.ems.web.user.biz.RoleBiz;
import info.zhihui.ems.web.user.controller.RoleController;
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

@WebMvcTest(RoleController.class)
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RoleBiz roleBiz;

    @MockitoBean
    private SaWebConfig saWebConfig;

    @Test
    @DisplayName("分页查询角色列表")
    void testFindRolePage() throws Exception {
        RoleVo roleVo = new RoleVo().setId(1).setRoleName("管理员").setRoleKey("admin");
        when(roleBiz.findRolePage(any(RoleQueryVo.class), eq(1), eq(10)))
                .thenReturn(new PageResult<RoleVo>().setPageNum(1).setPageSize(10).setTotal(1L).setList(List.of(roleVo)));

        mockMvc.perform(get("/roles/page")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].roleName").value("管理员"));
    }

    @Test
    @DisplayName("查询角色列表")
    void testFindRoleList() throws Exception {
        RoleVo roleVo = new RoleVo().setId(1).setRoleName("管理员").setRoleKey("admin");
        when(roleBiz.findRoleList(any(RoleQueryVo.class))).thenReturn(List.of(roleVo));

        mockMvc.perform(get("/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].roleName").value("管理员"));
    }

    @Test
    @DisplayName("获取角色详情")
    void testGetRoleDetail() throws Exception {
        RoleDetailVo roleDetailVo = new RoleDetailVo().setId(1).setRoleName("管理员").setRoleKey("admin");
        when(roleBiz.getRoleDetail(1)).thenReturn(roleDetailVo);

        mockMvc.perform(get("/roles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.roleName").value("管理员"));
    }

    @Test
    @DisplayName("新增角色")
    void testCreateRole() throws Exception {
        when(roleBiz.createRole(any(RoleCreateVo.class))).thenReturn(99);

        RoleCreateVo createVo = new RoleCreateVo();
        createVo.setRoleName("测试角色");
        createVo.setRoleKey("test");
        createVo.setSortNum(1);
        createVo.setRemark("测试角色备注");
        createVo.setIsSystem(false);
        createVo.setIsDisabled(false);

        mockMvc.perform(post("/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createVo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(99));
    }

    @Test
    @DisplayName("更新角色信息")
    void testUpdateRole() throws Exception {
        RoleUpdateVo updateVo = new RoleUpdateVo();
        updateVo.setRoleKey("test");
        updateVo.setRoleName("更新后的角色");
        updateVo.setRemark("更新后的备注");

        mockMvc.perform(put("/roles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateVo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("保存角色菜单关联")
    void testSaveRoleMenu() throws Exception {
        RoleMenuSaveVo saveVo = new RoleMenuSaveVo();
        saveVo.setMenuIds(List.of(1, 2, 3));

        mockMvc.perform(put("/roles/1/menus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saveVo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("删除角色")
    void testDeleteRole() throws Exception {
        mockMvc.perform(delete("/roles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
