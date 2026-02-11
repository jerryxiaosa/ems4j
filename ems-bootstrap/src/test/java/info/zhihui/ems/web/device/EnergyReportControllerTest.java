package info.zhihui.ems.web.device;

import com.fasterxml.jackson.databind.ObjectMapper;
import info.zhihui.ems.config.satoken.SaWebConfig;
import info.zhihui.ems.web.device.biz.EnergyReportBiz;
import info.zhihui.ems.web.device.controller.EnergyReportController;
import info.zhihui.ems.web.device.vo.StandardEnergyReportSaveVo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EnergyReportController.class)
class EnergyReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EnergyReportBiz energyReportBiz;

    @MockitoBean
    private SaWebConfig saWebConfig;

    @Test
    @DisplayName("标准电量上报")
    void testAddStandardReport() throws Exception {
        StandardEnergyReportSaveVo saveVo = new StandardEnergyReportSaveVo();
        saveVo.setSource("IOT");
        saveVo.setSourceReportId("RPT-20260211-001");
        saveVo.setDeviceNo("DEV-001");
        saveVo.setRecordTime(LocalDateTime.of(2026, 2, 11, 10, 0, 0));
        saveVo.setTotalEnergy(new BigDecimal("123.45"));
        saveVo.setHigherEnergy(new BigDecimal("11.11"));
        saveVo.setHighEnergy(new BigDecimal("22.22"));
        saveVo.setLowEnergy(new BigDecimal("33.33"));
        saveVo.setLowerEnergy(new BigDecimal("44.44"));
        saveVo.setDeepLowEnergy(new BigDecimal("12.35"));
        doNothing().when(energyReportBiz).addStandardReport(any(StandardEnergyReportSaveVo.class));

        mockMvc.perform(post("/device/energy-reports/standard")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saveVo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
