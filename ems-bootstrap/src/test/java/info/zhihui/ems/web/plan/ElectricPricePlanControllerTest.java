package info.zhihui.ems.web.plan;

import com.fasterxml.jackson.databind.ObjectMapper;
import info.zhihui.ems.config.satoken.SaWebConfig;
import info.zhihui.ems.web.plan.biz.ElectricPricePlanBiz;
import info.zhihui.ems.web.plan.controller.ElectricPricePlanController;
import info.zhihui.ems.web.plan.vo.ElectricPricePlanSaveVo;
import info.zhihui.ems.web.plan.vo.ElectricPricePlanVo;
import info.zhihui.ems.web.plan.vo.ElectricPriceTimeSettingVo;
import info.zhihui.ems.web.plan.vo.ElectricPriceTypeVo;
import info.zhihui.ems.web.plan.vo.StepPriceVo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ElectricPricePlanController.class)
class ElectricPricePlanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ElectricPricePlanBiz electricPricePlanBiz;

    @MockitoBean
    private SaWebConfig saWebConfig;

    @Test
    @DisplayName("查询电价方案列表")
    void testFindElectricPricePlanList() throws Exception {
        ElectricPricePlanVo vo = new ElectricPricePlanVo()
                .setId(1)
                .setName("默认电价");
        when(electricPricePlanBiz.findElectricPricePlanList(any())).thenReturn(List.of(vo));

        mockMvc.perform(get("/plan/electric-price-plans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("默认电价"));
    }

    @Test
    @DisplayName("新增电价方案")
    void testAddElectricPricePlan() throws Exception {
        when(electricPricePlanBiz.addElectricPricePlan(any(ElectricPricePlanSaveVo.class))).thenReturn(10);

        ElectricPricePlanSaveVo saveVo = new ElectricPricePlanSaveVo()
                .setName("阶梯电价")
                .setPriceHigher(BigDecimal.valueOf(1.2))
                .setPriceHigh(BigDecimal.valueOf(1.0))
                .setPriceLow(BigDecimal.valueOf(0.8))
                .setPriceLower(BigDecimal.valueOf(0.6))
                .setPriceDeepLow(BigDecimal.valueOf(0.5))
                .setIsCustomPrice(Boolean.TRUE);

        mockMvc.perform(post("/plan/electric-price-plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saveVo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(10));
    }

    @Test
    @DisplayName("获取默认阶梯电价配置")
    void testGetDefaultStepPrice() throws Exception {
        when(electricPricePlanBiz.getDefaultStepPrice()).thenReturn(List.of(new StepPriceVo().setStart(BigDecimal.ZERO)));

        mockMvc.perform(get("/plan/electric-price-plans/default/step-price"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].start").value(0));
    }

    @Test
    @DisplayName("更新默认尖峰平谷电价")
    void testUpdateDefaultElectricPrice() throws Exception {
        doNothing().when(electricPricePlanBiz).updateDefaultElectricPrice(any());

        List<ElectricPriceTypeVo> priceVos = List.of(new ElectricPriceTypeVo().setType(0).setPrice(BigDecimal.ONE));

        mockMvc.perform(put("/plan/electric-price-plans/default/price")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(priceVos)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("更新默认尖峰平谷时间段")
    void testUpdateDefaultElectricTime() throws Exception {
        doNothing().when(electricPricePlanBiz).updateDefaultElectricTime(any());

        List<ElectricPriceTimeSettingVo> timeList = List.of(new ElectricPriceTimeSettingVo().setType(0).setStart(LocalTime.MIDNIGHT));

        mockMvc.perform(put("/plan/electric-price-plans/default/time")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(timeList)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
