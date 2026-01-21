package info.zhihui.ems.web.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import info.zhihui.ems.config.satoken.SaWebConfig;
import info.zhihui.ems.web.user.biz.MenuBiz;
import info.zhihui.ems.web.user.controller.MenuController;
import info.zhihui.ems.web.user.vo.*;
import info.zhihui.ems.web.user.vo.MenuWithChildrenVo;
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

@WebMvcTest(MenuController.class)
class MenuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MenuBiz menuBiz;

    @MockitoBean
    private SaWebConfig saWebConfig;

    @Test
    @DisplayName("查询菜单树列表")
    void testFindMenuTree() throws Exception {
        MenuWithChildrenVo child = new MenuWithChildrenVo()
                .setId(2)
                .setMenuName("用户管理")
                .setPid(1)
                .setChildren(List.of());
        MenuWithChildrenVo root = new MenuWithChildrenVo()
                .setId(1)
                .setMenuName("系统管理")
                .setPid(0)
                .setChildren(List.of(child));

        when(menuBiz.findTree(any(MenuQueryVo.class))).thenReturn(List.of(root));

        mockMvc.perform(get("/menus")
                        .param("menuNameLike", "系统"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].children[0].id").value(2));
    }

    @Test
    @DisplayName("获取菜单详情")
    void testGetDetail() throws Exception {
        MenuVo menuVo = new MenuVo()
                .setId(1)
                .setMenuName("系统管理")
                .setMenuKey("system")
                .setPid(0)
                .setSortNum(1)
                .setPath("/system")
                .setMenuSource("1")
                .setMenuType("1")
                .setIcon("system")
                .setRemark("系统管理菜单")
                .setHidden(false)
                .setPermissionCodes(List.of("system:view", "system:manage"));

        when(menuBiz.getDetail(eq(1))).thenReturn(menuVo);

        mockMvc.perform(get("/menus/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.menuName").value("系统管理"))
                .andExpect(jsonPath("$.data.menuKey").value("system"))
                .andExpect(jsonPath("$.data.pid").value(0))
                .andExpect(jsonPath("$.data.sortNum").value(1))
                .andExpect(jsonPath("$.data.path").value("/system"))
                .andExpect(jsonPath("$.data.menuSource").value("1"))
                .andExpect(jsonPath("$.data.menuType").value("1"))
                .andExpect(jsonPath("$.data.icon").value("system"))
                .andExpect(jsonPath("$.data.remark").value("系统管理菜单"))
                .andExpect(jsonPath("$.data.hidden").value(false))
                .andExpect(jsonPath("$.data.permissionCodes[0]").value("system:view"))
                .andExpect(jsonPath("$.data.permissionCodes[1]").value("system:manage"));
    }

    @Test
    @DisplayName("新增菜单")
    void testAdd() throws Exception {
        when(menuBiz.add(any(MenuCreateVo.class))).thenReturn(99);

        MenuCreateVo createVo = new MenuCreateVo()
                .setMenuName("测试菜单")
                .setMenuKey("test")
                .setPid(0)
                .setSortNum(1)
                .setPath("/test")
                .setMenuSource(1)
                .setMenuType(1)
                .setIcon("test")
                .setRemark("测试菜单备注")
                .setHidden(false)
                .setPermissionCodes(List.of("test:view", "test:manage"));

        mockMvc.perform(post("/menus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createVo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(99));
    }

    @Test
    @DisplayName("更新菜单信息")
    void testUpdate() throws Exception {
        MenuUpdateVo updateVo = new MenuUpdateVo()
                .setMenuName("更新后的菜单")
                .setMenuKey("updated")
                .setSortNum(2)
                .setPath("/updated")
                .setMenuSource(1)
                .setMenuType(1)
                .setIcon("updated")
                .setRemark("更新后的备注")
                .setHidden(true)
                .setPermissionCodes(List.of("updated:view"));

        mockMvc.perform(put("/menus/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateVo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("删除菜单")
    void testDelete() throws Exception {
        mockMvc.perform(delete("/menus/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}