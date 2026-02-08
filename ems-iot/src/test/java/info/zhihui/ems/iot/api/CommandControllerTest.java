package info.zhihui.ems.iot.api;

import info.zhihui.ems.common.constant.ResultCode;
import info.zhihui.ems.iot.application.DeviceVendorFacade;
import info.zhihui.ems.iot.vo.electric.ElectricDateDurationVo;
import info.zhihui.ems.iot.vo.electric.ElectricDurationVo;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommandController.class)
class CommandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DeviceVendorFacade deviceVendorFacade;

    @Test
    @DisplayName("拉闸命令")
    void testCutOff_ShouldReturnSuccess() throws Exception {
        mockMvc.perform(post("/api/commands/1/cut-off"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(deviceVendorFacade).cutPower(1);
    }

    @Test
    @DisplayName("恢复供电命令")
    void testRecover_ShouldReturnSuccess() throws Exception {
        mockMvc.perform(post("/api/commands/1/recover"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(deviceVendorFacade).recoverPower(1);
    }

    @Test
    @DisplayName("获取互感器倍率")
    void testGetCt_ShouldReturnSuccess() throws Exception {
        when(deviceVendorFacade.getCt(1)).thenReturn(200);

        mockMvc.perform(get("/api/commands/1/ct"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(200));

        verify(deviceVendorFacade).getCt(1);
    }

    @Test
    @DisplayName("设置互感器倍率")
    void testSetCt_ShouldReturnSuccess() throws Exception {
        mockMvc.perform(post("/api/commands/1/ct")
                        .param("ct", "200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(deviceVendorFacade).setCt(1, 200);
    }

    @Test
    @DisplayName("读取时段电价")
    void testGetDuration_ShouldReturnSuccess() throws Exception {
        ElectricDurationVo durationVo = new ElectricDurationVo()
                .setPeriod(1)
                .setHour("08")
                .setMin("30");
        when(deviceVendorFacade.getDuration(1, 1)).thenReturn(List.of(durationVo));

        mockMvc.perform(get("/api/commands/1/duration")
                        .param("dailyPlanId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].period").value(1))
                .andExpect(jsonPath("$.data[0].hour").value("08"))
                .andExpect(jsonPath("$.data[0].min").value("30"));

        verify(deviceVendorFacade).getDuration(1, 1);
    }

    @Test
    @DisplayName("设置时段电价")
    void testSetDuration_ShouldReturnSuccess() throws Exception {
        String body = """
                {
                  "dailyPlanId": 1,
                  "electricDurations": [
                    {
                      "period": 1,
                      "hour": "08",
                      "min": "30"
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/commands/1/duration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(deviceVendorFacade).setDuration(eq(1), any());
    }

    @Test
    @DisplayName("读取日期电价")
    void testGetDateDuration_ShouldReturnSuccess() throws Exception {
        ElectricDateDurationVo dateDurationVo = new ElectricDateDurationVo()
                .setMonth("1")
                .setDay("1")
                .setDailyPlanId("1");
        when(deviceVendorFacade.getDateDuration(1)).thenReturn(List.of(dateDurationVo));

        mockMvc.perform(get("/api/commands/1/date-duration"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].month").value("1"))
                .andExpect(jsonPath("$.data[0].day").value("1"))
                .andExpect(jsonPath("$.data[0].dailyPlanId").value("1"));

        verify(deviceVendorFacade).getDateDuration(1);
    }

    @Test
    @DisplayName("设置日期电价")
    void testSetDateDuration_ShouldReturnSuccess() throws Exception {
        String body = """
                [
                  {
                    "month": "1",
                    "day": "1",
                    "dailyPlanId": "1"
                  }
                ]
                """;

        mockMvc.perform(post("/api/commands/1/date-duration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(deviceVendorFacade).setDateDuration(eq(1), any());
    }

    @Test
    @DisplayName("读取用电量")
    void testGetUsedPower_ShouldReturnSuccess() throws Exception {
        when(deviceVendorFacade.getUsedPower(1, 2)).thenReturn(new BigDecimal("12.34"));

        mockMvc.perform(get("/api/commands/1/used-power")
                        .param("type", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(12.34));

        verify(deviceVendorFacade).getUsedPower(1, 2);
    }

    @Test
    @DisplayName("读取时段电价-日方案编号越界时返回参数错误")
    void testGetDuration_WhenDailyPlanIdOutOfRange_ShouldReturnParameterError() throws Exception {
        mockMvc.perform(get("/api/commands/1/duration")
                        .param("dailyPlanId", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(ResultCode.PARAMETER_ERROR.getCode()));

        verifyNoInteractions(deviceVendorFacade);
    }

    @Test
    @DisplayName("设置时段电价-时间格式非法时返回参数错误")
    void testSetDuration_WhenTimeInvalid_ShouldReturnParameterError() throws Exception {
        String body = """
                {
                  "dailyPlanId": 1,
                  "electricDurations": [
                    {
                      "period": 1,
                      "hour": "AA",
                      "min": "00"
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/commands/1/duration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(ResultCode.PARAMETER_ERROR.getCode()));

        verifyNoInteractions(deviceVendorFacade);
    }

    @Test
    @DisplayName("拉闸命令-设备ID非法时返回参数错误")
    void testCutOff_WhenDeviceIdInvalid_ShouldReturnParameterError() throws Exception {
        mockMvc.perform(post("/api/commands/0/cut-off"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(ResultCode.PARAMETER_ERROR.getCode()));

        verifyNoInteractions(deviceVendorFacade);
    }
}
