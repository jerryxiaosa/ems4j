package info.zhihui.ems.web.plan;

import com.fasterxml.jackson.databind.ObjectMapper;
import info.zhihui.ems.config.satoken.SaWebConfig;
import info.zhihui.ems.web.plan.biz.WarnPlanBiz;
import info.zhihui.ems.web.plan.controller.WarnPlanController;
import info.zhihui.ems.web.plan.vo.WarnPlanSaveVo;
import info.zhihui.ems.web.plan.vo.WarnPlanVo;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WarnPlanController.class)
class WarnPlanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WarnPlanBiz warnPlanBiz;

    @MockitoBean
    private SaWebConfig saWebConfig;

    @Test
    @DisplayName("查询预警方案列表")
    void testFindWarnPlanList() throws Exception {
        WarnPlanVo vo = new WarnPlanVo().setId(1).setName("默认预警");
        when(warnPlanBiz.findWarnPlanList(any())).thenReturn(List.of(vo));

        mockMvc.perform(get("/plan/warn-plans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("默认预警"));
    }

    @Test
    @DisplayName("新增预警方案")
    void testAddWarnPlan() throws Exception {
        when(warnPlanBiz.addWarnPlan(any(WarnPlanSaveVo.class))).thenReturn(5);

        WarnPlanSaveVo saveVo = new WarnPlanSaveVo()
                .setName("预警方案A")
                .setFirstLevel(BigDecimal.valueOf(20))
                .setSecondLevel(BigDecimal.valueOf(10));

        mockMvc.perform(post("/plan/warn-plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saveVo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(5));
    }

    @Test
    @DisplayName("更新预警方案")
    void testUpdateWarnPlan() throws Exception {
        doNothing().when(warnPlanBiz).updateWarnPlan(any(), any());

        WarnPlanSaveVo saveVo = new WarnPlanSaveVo()
                .setName("方案B")
                .setAutoClose(Boolean.TRUE);

        mockMvc.perform(put("/plan/warn-plans/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saveVo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("删除预警方案")
    void testDeleteWarnPlan() throws Exception {
        doNothing().when(warnPlanBiz).deleteWarnPlan(1);

        mockMvc.perform(delete("/plan/warn-plans/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
