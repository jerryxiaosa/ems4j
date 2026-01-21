package info.zhihui.ems.web.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import info.zhihui.ems.config.satoken.SaWebConfig;
import info.zhihui.ems.web.order.biz.ServiceRateBiz;
import info.zhihui.ems.web.order.controller.ServiceRateController;
import info.zhihui.ems.web.order.vo.ServiceRateVo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ServiceRateController.class)
class ServiceRateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ServiceRateBiz serviceRateBiz;

    @MockitoBean
    private SaWebConfig saWebConfig;

    @Test
    @DisplayName("获取默认服务费率")
    void testGetDefaultServiceRate() throws Exception {
        when(serviceRateBiz.getDefaultServiceRate()).thenReturn(BigDecimal.valueOf(0.05));

        mockMvc.perform(get("/orders/service-rate/default"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.defaultServiceRate").value(0.05));
    }

    @Test
    @DisplayName("更新默认服务费率")
    void testUpdateDefaultServiceRate() throws Exception {
        doNothing().when(serviceRateBiz).updateDefaultServiceRate(BigDecimal.valueOf(0.08));

        ServiceRateVo vo = new ServiceRateVo().setDefaultServiceRate(BigDecimal.valueOf(0.08));

        mockMvc.perform(put("/orders/service-rate/default")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
