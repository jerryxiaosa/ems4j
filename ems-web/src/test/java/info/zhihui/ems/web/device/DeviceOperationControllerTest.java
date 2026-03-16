package info.zhihui.ems.web.device;

import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.common.vo.RestResult;
import info.zhihui.ems.web.common.constant.ApiPathConstant;
import info.zhihui.ems.web.device.biz.DeviceOperationBiz;
import info.zhihui.ems.web.device.controller.DeviceOperationController;
import info.zhihui.ems.web.device.vo.DeviceOperationDetailVo;
import info.zhihui.ems.web.device.vo.DeviceOperationExecuteRecordVo;
import info.zhihui.ems.web.device.vo.DeviceOperationQueryVo;
import info.zhihui.ems.web.device.vo.DeviceOperationVo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceOperationControllerTest {

    @InjectMocks
    private DeviceOperationController deviceOperationController;

    @Mock
    private DeviceOperationBiz deviceOperationBiz;

    @Test
    @DisplayName("分页查询设备操作_应委托业务层并返回成功结果")
    void testFindDeviceOperationPage_ShouldDelegateToBiz() {
        PageResult<DeviceOperationVo> pageResult = new PageResult<DeviceOperationVo>()
                .setPageNum(1)
                .setPageSize(10)
                .setTotal(0L)
                .setList(List.of());
        DeviceOperationQueryVo queryVo = new DeviceOperationQueryVo();
        when(deviceOperationBiz.findDeviceOperationPage(queryVo, 1, 10)).thenReturn(pageResult);

        RestResult<PageResult<DeviceOperationVo>> result = deviceOperationController.findDeviceOperationPage(queryVo, 1, 10);

        verify(deviceOperationBiz).findDeviceOperationPage(queryVo, 1, 10);
        assertThat(result.getSuccess()).isTrue();
        assertThat(result.getData()).isEqualTo(pageResult);
    }

    @Test
    @DisplayName("查询设备操作详情_应委托业务层并返回成功结果")
    void testGetDeviceOperation_ShouldDelegateToBiz() {
        DeviceOperationDetailVo detailVo = new DeviceOperationDetailVo().setId(12);
        when(deviceOperationBiz.getDeviceOperation(12)).thenReturn(detailVo);

        RestResult<DeviceOperationDetailVo> result = deviceOperationController.getDeviceOperation(12);

        verify(deviceOperationBiz).getDeviceOperation(12);
        assertThat(result.getSuccess()).isTrue();
        assertThat(result.getData()).isEqualTo(detailVo);
    }

    @Test
    @DisplayName("查询执行记录_应委托业务层并返回成功结果")
    void testFindDeviceOperationExecuteRecordList_ShouldDelegateToBiz() {
        List<DeviceOperationExecuteRecordVo> executeRecordVoList = List.of(new DeviceOperationExecuteRecordVo().setId(1));
        when(deviceOperationBiz.findDeviceOperationExecuteRecordList(12)).thenReturn(executeRecordVoList);

        RestResult<List<DeviceOperationExecuteRecordVo>> result = deviceOperationController.findDeviceOperationExecuteRecordList(12);

        verify(deviceOperationBiz).findDeviceOperationExecuteRecordList(12);
        assertThat(result.getSuccess()).isTrue();
        assertThat(result.getData()).isEqualTo(executeRecordVoList);
    }

    @Test
    @DisplayName("设备操作控制器_路由应符合约定")
    void testRouteAnnotations_ShouldMatchExpectedPath() throws NoSuchMethodException {
        RequestMapping classRequestMapping = DeviceOperationController.class.getAnnotation(RequestMapping.class);
        assertThat(classRequestMapping).isNotNull();
        assertThat(classRequestMapping.value()).containsExactly(ApiPathConstant.V1 + "/device/operations");

        Method pageMethod = DeviceOperationController.class.getMethod(
                "findDeviceOperationPage", DeviceOperationQueryVo.class, Integer.class, Integer.class
        );
        Method detailMethod = DeviceOperationController.class.getMethod("getDeviceOperation", Integer.class);
        Method recordMethod = DeviceOperationController.class.getMethod("findDeviceOperationExecuteRecordList", Integer.class);

        assertThat(pageMethod.getAnnotation(GetMapping.class).value()).containsExactly("/page");
        assertThat(detailMethod.getAnnotation(GetMapping.class).value()).containsExactly("/{id}");
        assertThat(recordMethod.getAnnotation(GetMapping.class).value()).containsExactly("/{id}/execute-records");
    }
}
