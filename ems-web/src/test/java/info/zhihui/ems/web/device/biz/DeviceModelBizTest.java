package info.zhihui.ems.web.device.biz;

import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.foundation.integration.core.bo.DeviceModelBo;
import info.zhihui.ems.foundation.integration.core.dto.DeviceModelQueryDto;
import info.zhihui.ems.foundation.integration.core.service.DeviceModelService;
import info.zhihui.ems.web.device.mapstruct.DeviceModelWebMapper;
import info.zhihui.ems.web.device.vo.DeviceModelQueryVo;
import info.zhihui.ems.web.device.vo.DeviceModelVo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceModelBizTest {

    @InjectMocks
    private DeviceModelBiz deviceModelBiz;

    @Mock
    private DeviceModelService deviceModelService;

    @Mock
    private DeviceModelWebMapper deviceModelWebMapper;

    @Test
    @DisplayName("分页查询设备型号_应透传查询条件与分页参数")
    void testFindDeviceModelPage_ShouldPassQueryAndPageParam() {
        DeviceModelQueryVo queryVo = new DeviceModelQueryVo()
                .setTypeIds(List.of(1, 2))
                .setManufacturerName("制造商A")
                .setModelName("型号A");
        DeviceModelQueryDto queryDto = new DeviceModelQueryDto()
                .setTypeIds(List.of(1, 2))
                .setManufacturerName("制造商A")
                .setModelName("型号A");
        PageResult<DeviceModelBo> pageResult = new PageResult<DeviceModelBo>()
                .setPageNum(2)
                .setPageSize(20)
                .setTotal(1L)
                .setList(List.of(new DeviceModelBo().setId(1).setModelName("型号A")));
        PageResult<DeviceModelVo> expectedPage = new PageResult<DeviceModelVo>()
                .setPageNum(2)
                .setPageSize(20)
                .setTotal(1L)
                .setList(List.of(new DeviceModelVo().setId(1).setModelName("型号A")));
        when(deviceModelWebMapper.toDeviceModelQueryDto(queryVo)).thenReturn(queryDto);
        when(deviceModelService.findPage(any(), any())).thenReturn(pageResult);
        when(deviceModelWebMapper.toDeviceModelVoPage(pageResult)).thenReturn(expectedPage);

        PageResult<DeviceModelVo> result = deviceModelBiz.findDeviceModelPage(queryVo, 2, 20);

        assertThat(result).isSameAs(expectedPage);
        ArgumentCaptor<DeviceModelQueryDto> queryCaptor = ArgumentCaptor.forClass(DeviceModelQueryDto.class);
        verify(deviceModelService).findPage(queryCaptor.capture(), any());
        assertThat(queryCaptor.getValue().getTypeIds()).containsExactly(1, 2);
        assertThat(queryCaptor.getValue().getManufacturerName()).isEqualTo("制造商A");
        assertThat(queryCaptor.getValue().getModelName()).isEqualTo("型号A");
    }

    @Test
    @DisplayName("分页查询设备型号_查询条件为空_应创建默认查询对象")
    void testFindDeviceModelPage_WithNullQueryDto_ShouldCreateDefaultQueryDto() {
        PageResult<DeviceModelBo> pageResult = new PageResult<DeviceModelBo>()
                .setPageNum(1)
                .setPageSize(10)
                .setTotal(0L)
                .setList(Collections.emptyList());
        PageResult<DeviceModelVo> expectedPage = new PageResult<DeviceModelVo>()
                .setPageNum(1)
                .setPageSize(10)
                .setTotal(0L)
                .setList(Collections.emptyList());
        when(deviceModelWebMapper.toDeviceModelQueryDto(any())).thenReturn(null);
        when(deviceModelService.findPage(any(), any())).thenReturn(pageResult);
        when(deviceModelWebMapper.toDeviceModelVoPage(pageResult)).thenReturn(expectedPage);

        PageResult<DeviceModelVo> result = deviceModelBiz.findDeviceModelPage(new DeviceModelQueryVo(), null, null);

        assertThat(result).isSameAs(expectedPage);
        ArgumentCaptor<DeviceModelQueryDto> queryCaptor = ArgumentCaptor.forClass(DeviceModelQueryDto.class);
        ArgumentCaptor<PageParam> pageParamCaptor = ArgumentCaptor.forClass(PageParam.class);
        verify(deviceModelService).findPage(queryCaptor.capture(), pageParamCaptor.capture());
        assertThat(queryCaptor.getValue()).isNotNull();
        assertThat(pageParamCaptor.getValue().getPageNum()).isEqualTo(1);
        assertThat(pageParamCaptor.getValue().getPageSize()).isEqualTo(10);
    }
}
