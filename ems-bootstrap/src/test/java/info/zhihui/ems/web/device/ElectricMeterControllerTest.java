package info.zhihui.ems.web.device;

import com.fasterxml.jackson.databind.ObjectMapper;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.config.satoken.SaWebConfig;
import info.zhihui.ems.web.device.biz.ElectricMeterBiz;
import info.zhihui.ems.web.device.controller.ElectricMeterController;
import info.zhihui.ems.web.device.vo.*;
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

@WebMvcTest(ElectricMeterController.class)
class ElectricMeterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ElectricMeterBiz electricMeterBiz;

    @MockitoBean
    private SaWebConfig saWebConfig;

    @Test
    @DisplayName("分页查询电表")
    void testFindElectricMeterPage() throws Exception {
        ElectricMeterVo vo = new ElectricMeterVo().setId(1).setMeterName("MeterA");
        PageResult<ElectricMeterVo> pageResult = new PageResult<ElectricMeterVo>()
                .setList(List.of(vo))
                .setPageNum(1)
                .setPageSize(10)
                .setTotal(1L);
        when(electricMeterBiz.findElectricMeterPage(any(), eq(1), eq(10))).thenReturn(pageResult);

        mockMvc.perform(get("/device/electric-meters/page")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.list[0].meterName").value("MeterA"));
    }

    @Test
    @DisplayName("新增电表")
    void testAddElectricMeter() throws Exception {
        when(electricMeterBiz.addElectricMeter(any(ElectricMeterCreateVo.class))).thenReturn(100);

        ElectricMeterCreateVo createVo = new ElectricMeterCreateVo()
                .setSpaceId(1)
                .setMeterName("MeterA")
                .setIsPrepay(Boolean.TRUE)
                .setModelId(2);

        mockMvc.perform(post("/device/electric-meters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createVo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(100));
    }

    @Test
    @DisplayName("查询电表电量")
    void testGetMeterPower() throws Exception {
        List<ElectricMeterPowerVo> powerVos = List.of(new ElectricMeterPowerVo().setType(0).setValue(BigDecimal.ONE));
        when(electricMeterBiz.getMeterPower(eq(1), any(ElectricMeterPowerQueryVo.class))).thenReturn(powerVos);

        ElectricMeterPowerQueryVo queryVo = new ElectricMeterPowerQueryVo().setTypes(List.of(0));

        mockMvc.perform(post("/device/electric-meters/1/power")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(queryVo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].type").value(0));
    }

}
