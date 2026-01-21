package info.zhihui.ems.web.space;

import com.fasterxml.jackson.databind.ObjectMapper;
import info.zhihui.ems.config.satoken.SaWebConfig;
import info.zhihui.ems.web.space.biz.SpaceBiz;
import info.zhihui.ems.web.space.controller.SpaceController;
import info.zhihui.ems.web.space.vo.SpaceCreateVo;
import info.zhihui.ems.web.space.vo.SpaceVo;
import info.zhihui.ems.web.space.vo.SpaceUpdateVo;
import info.zhihui.ems.web.space.vo.SpaceWithChildrenVo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SpaceController.class)
class SpaceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SpaceBiz spaceBiz;

    @MockitoBean
    private SaWebConfig saWebConfig;

    @Test
    @DisplayName("查询空间列表")
    void testFindSpaceList() throws Exception {
        SpaceVo vo = new SpaceVo()
                .setId(1)
                .setName("空间一");
        when(spaceBiz.findSpaceList(any())).thenReturn(List.of(vo));

        mockMvc.perform(get("/spaces"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    @DisplayName("查询空间树")
    void testFindSpaceTree() throws Exception {
        SpaceWithChildrenVo child = new SpaceWithChildrenVo()
                .setId(2)
                .setName("子空间")
                .setPid(1)
                .setChildren(List.of());
        SpaceWithChildrenVo root = new SpaceWithChildrenVo()
                .setId(1)
                .setName("父空间")
                .setPid(0)
                .setChildren(List.of(child));

        when(spaceBiz.findSpaceTree(any())).thenReturn(List.of(root));

        mockMvc.perform(get("/spaces/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].children[0].id").value(2));
    }

    @Test
    @DisplayName("获取空间详情")
    void testGetSpace() throws Exception {
        SpaceVo vo = new SpaceVo().setId(5).setName("空间详情");
        when(spaceBiz.getSpace(5)).thenReturn(vo);

        mockMvc.perform(get("/spaces/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(5));
    }

    @Test
    @DisplayName("新增空间")
    void testCreateSpace() throws Exception {
        when(spaceBiz.createSpace(any(SpaceCreateVo.class))).thenReturn(88);

        SpaceCreateVo createVo = new SpaceCreateVo()
                .setName("新增空间")
                .setPid(0)
                .setType(1)
                .setArea(new BigDecimal("100"))
                .setSortIndex(1);

        mockMvc.perform(post("/spaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createVo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(88));
    }

    @Test
    @DisplayName("更新空间")
    void testUpdateSpace() throws Exception {
        SpaceUpdateVo updateVo = new SpaceUpdateVo()
                .setName("更新空间")
                .setPid(0)
                .setType(1)
                .setArea(new BigDecimal("120"))
                .setSortIndex(2);

        doNothing().when(spaceBiz).updateSpace(eq(6), any(SpaceUpdateVo.class));

        mockMvc.perform(put("/spaces/6")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateVo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("删除空间")
    void testDeleteSpace() throws Exception {
        doNothing().when(spaceBiz).deleteSpace(6);

        mockMvc.perform(delete("/spaces/6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
