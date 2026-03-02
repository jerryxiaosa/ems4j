package info.zhihui.ems.web.device;

import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.config.satoken.SaWebConfig;
import info.zhihui.ems.web.device.biz.DeviceModelBiz;
import info.zhihui.ems.web.device.controller.DeviceModelController;
import info.zhihui.ems.web.device.vo.DeviceModelQueryVo;
import info.zhihui.ems.web.device.vo.DeviceModelVo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DeviceModelController.class)
class DeviceModelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DeviceModelBiz deviceModelBiz;

    @MockitoBean
    private SaWebConfig saWebConfig;

    @Test
    @DisplayName("查询设备型号列表_应返回列表并绑定查询参数")
    void testFindDeviceModelList_ShouldReturnList() throws Exception {
        List<DeviceModelVo> deviceModelVoList = List.of(new DeviceModelVo()
                .setId(1)
                .setTypeId(10)
                .setTypeKey("electric")
                .setManufacturerName("制造商A")
                .setModelName("型号A")
                .setProductCode("P-001")
                .setModelProperty(Map.of("baudRate", 9600)));
        when(deviceModelBiz.findDeviceModelList(any())).thenReturn(deviceModelVoList);

        mockMvc.perform(get("/device/device-models")
                        .param("typeIds", "10", "20")
                        .param("manufacturerName", "制造商A")
                        .param("modelName", "型号A"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].typeKey").value("electric"))
                .andExpect(jsonPath("$.data[0].manufacturerName").value("制造商A"))
                .andExpect(jsonPath("$.data[0].modelName").value("型号A"))
                .andExpect(jsonPath("$.data[0].productCode").value("P-001"))
                .andExpect(jsonPath("$.data[0].modelProperty.baudRate").value(9600));

        ArgumentCaptor<DeviceModelQueryVo> queryCaptor = ArgumentCaptor.forClass(DeviceModelQueryVo.class);
        verify(deviceModelBiz).findDeviceModelList(queryCaptor.capture());
        assertThat(queryCaptor.getValue().getTypeIds()).containsExactly(10, 20);
        assertThat(queryCaptor.getValue().getManufacturerName()).isEqualTo("制造商A");
        assertThat(queryCaptor.getValue().getModelName()).isEqualTo("型号A");
    }

    @Test
    @DisplayName("分页查询设备型号_应返回分页结果并绑定查询参数")
    void testFindDeviceModelPage_ShouldReturnPageResult() throws Exception {
        DeviceModelVo deviceModelVo = new DeviceModelVo()
                .setId(1)
                .setTypeId(10)
                .setTypeKey("electric")
                .setManufacturerName("制造商A")
                .setModelName("型号A")
                .setProductCode("P-001")
                .setModelProperty(Map.of("baudRate", 9600));
        PageResult<DeviceModelVo> pageResult = new PageResult<DeviceModelVo>()
                .setPageNum(1)
                .setPageSize(10)
                .setTotal(1L)
                .setList(List.of(deviceModelVo));
        when(deviceModelBiz.findDeviceModelPage(any(), eq(1), eq(10))).thenReturn(pageResult);

        mockMvc.perform(get("/device/device-models/page")
                        .param("typeIds", "10", "20")
                        .param("manufacturerName", "制造商A")
                        .param("modelName", "型号A")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.list[0].id").value(1))
                .andExpect(jsonPath("$.data.list[0].typeKey").value("electric"))
                .andExpect(jsonPath("$.data.list[0].manufacturerName").value("制造商A"))
                .andExpect(jsonPath("$.data.list[0].modelName").value("型号A"))
                .andExpect(jsonPath("$.data.list[0].productCode").value("P-001"))
                .andExpect(jsonPath("$.data.list[0].modelProperty.baudRate").value(9600));

        ArgumentCaptor<DeviceModelQueryVo> queryCaptor = ArgumentCaptor.forClass(DeviceModelQueryVo.class);
        verify(deviceModelBiz).findDeviceModelPage(queryCaptor.capture(), eq(1), eq(10));
        assertThat(queryCaptor.getValue().getTypeIds()).containsExactly(10, 20);
        assertThat(queryCaptor.getValue().getManufacturerName()).isEqualTo("制造商A");
        assertThat(queryCaptor.getValue().getModelName()).isEqualTo("型号A");
    }

    @Test
    @DisplayName("分页查询设备型号_未传分页参数_应使用默认分页值")
    void testFindDeviceModelPage_WithoutPageParams_ShouldUseDefaultPagination() throws Exception {
        PageResult<DeviceModelVo> pageResult = new PageResult<DeviceModelVo>()
                .setPageNum(1)
                .setPageSize(10)
                .setTotal(0L)
                .setList(List.of());
        when(deviceModelBiz.findDeviceModelPage(any(), eq(1), eq(10))).thenReturn(pageResult);

        mockMvc.perform(get("/device/device-models/page"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(deviceModelBiz).findDeviceModelPage(any(), eq(1), eq(10));
    }
}
