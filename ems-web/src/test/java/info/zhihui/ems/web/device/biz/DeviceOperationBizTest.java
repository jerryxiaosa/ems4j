package info.zhihui.ems.web.device.biz;

import info.zhihui.ems.common.enums.DeviceTypeEnum;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.foundation.integration.biz.command.bo.DeviceCommandExecuteRecordBo;
import info.zhihui.ems.foundation.integration.biz.command.bo.DeviceCommandRecordBo;
import info.zhihui.ems.foundation.integration.biz.command.dto.DeviceCommandQueryDto;
import info.zhihui.ems.foundation.integration.biz.command.enums.CommandSourceEnum;
import info.zhihui.ems.foundation.integration.biz.command.enums.CommandTypeEnum;
import info.zhihui.ems.foundation.integration.biz.command.service.DeviceCommandService;
import info.zhihui.ems.web.device.vo.DeviceOperationDetailVo;
import info.zhihui.ems.web.device.vo.DeviceOperationExecuteRecordVo;
import info.zhihui.ems.web.device.vo.DeviceOperationQueryVo;
import info.zhihui.ems.web.device.vo.DeviceOperationVo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceOperationBizTest {

    @InjectMocks
    private DeviceOperationBiz deviceOperationBiz;

    @Mock
    private DeviceCommandService deviceCommandService;

    @Test
    @DisplayName("分页查询设备操作_应正确映射查询条件和结果")
    void testFindDeviceOperationPage_ShouldMapQueryAndResult() {
        DeviceOperationQueryVo queryVo = new DeviceOperationQueryVo();
        queryVo.setOperateUserName("张三");
        queryVo.setCommandType(1);
        queryVo.setSuccess(true);
        queryVo.setDeviceType("electricMeter");
        queryVo.setDeviceNo("EM0001");
        queryVo.setDeviceName("电表A");
        queryVo.setSpaceName("101");

        DeviceCommandRecordBo recordBo = new DeviceCommandRecordBo()
                .setId(11)
                .setDeviceType(DeviceTypeEnum.ELECTRIC)
                .setDeviceNo("EM0001")
                .setDeviceName("电表A")
                .setSpaceName("101")
                .setCommandType(CommandTypeEnum.ENERGY_ELECTRIC_TURN_ON)
                .setSuccess(true)
                .setExecuteTimes(2)
                .setOperateUserName("张三")
                .setCreateTime(LocalDateTime.of(2026, 3, 16, 12, 0, 0));
        PageResult<DeviceCommandRecordBo> commandPage = new PageResult<DeviceCommandRecordBo>()
                .setPageNum(2)
                .setPageSize(5)
                .setTotal(9L)
                .setList(List.of(recordBo));
        when(deviceCommandService.findDeviceCommandPage(any(DeviceCommandQueryDto.class), any(PageParam.class))).thenReturn(commandPage);

        PageResult<DeviceOperationVo> result = deviceOperationBiz.findDeviceOperationPage(queryVo, 2, 5);

        ArgumentCaptor<DeviceCommandQueryDto> queryCaptor = ArgumentCaptor.forClass(DeviceCommandQueryDto.class);
        ArgumentCaptor<PageParam> pageCaptor = ArgumentCaptor.forClass(PageParam.class);
        verify(deviceCommandService).findDeviceCommandPage(queryCaptor.capture(), pageCaptor.capture());

        DeviceCommandQueryDto actualQuery = queryCaptor.getValue();
        assertThat(actualQuery.getOperateUserName()).isEqualTo("张三");
        assertThat(actualQuery.getCommandType()).isEqualTo(CommandTypeEnum.ENERGY_ELECTRIC_TURN_ON);
        assertThat(actualQuery.getSuccess()).isTrue();
        assertThat(actualQuery.getDeviceType()).isEqualTo(DeviceTypeEnum.ELECTRIC);
        assertThat(actualQuery.getDeviceNo()).isEqualTo("EM0001");
        assertThat(actualQuery.getDeviceName()).isEqualTo("电表A");
        assertThat(actualQuery.getSpaceName()).isEqualTo("101");

        PageParam actualPage = pageCaptor.getValue();
        assertThat(actualPage.getPageNum()).isEqualTo(2);
        assertThat(actualPage.getPageSize()).isEqualTo(5);

        assertThat(result.getPageNum()).isEqualTo(2);
        assertThat(result.getPageSize()).isEqualTo(5);
        assertThat(result.getTotal()).isEqualTo(9L);
        assertThat(result.getList()).hasSize(1);
        DeviceOperationVo operationVo = result.getList().get(0);
        assertThat(operationVo.getId()).isEqualTo(11);
        assertThat(operationVo.getDeviceType()).isEqualTo("electricMeter");
        assertThat(operationVo.getCommandType()).isEqualTo(1);
        assertThat(operationVo.getCommandTypeName()).isEqualTo("电表充值自动合闸");
    }

    @Test
    @DisplayName("分页查询设备操作_操作类型非法应抛异常")
    void testFindDeviceOperationPage_WithInvalidCommandType_ShouldThrowException() {
        DeviceOperationQueryVo queryVo = new DeviceOperationQueryVo();
        queryVo.setCommandType(999);

        assertThrows(BusinessRuntimeException.class, () -> deviceOperationBiz.findDeviceOperationPage(queryVo, 1, 10));
    }

    @Test
    @DisplayName("分页查询设备操作_设备类型非法应抛异常")
    void testFindDeviceOperationPage_WithInvalidDeviceType_ShouldThrowException() {
        DeviceOperationQueryVo queryVo = new DeviceOperationQueryVo();
        queryVo.setDeviceType("unknown");

        assertThrows(BusinessRuntimeException.class, () -> deviceOperationBiz.findDeviceOperationPage(queryVo, 1, 10));
    }

    @Test
    @DisplayName("查询设备操作详情_应正确映射")
    void testGetDeviceOperation_ShouldMapDetail() {
        DeviceCommandRecordBo recordBo = new DeviceCommandRecordBo()
                .setId(19)
                .setDeviceType(DeviceTypeEnum.ELECTRIC)
                .setDeviceId(100)
                .setDeviceIotId("iot-100")
                .setDeviceNo("EM0019")
                .setDeviceName("电表19")
                .setCommandType(CommandTypeEnum.ENERGY_ELECTRIC_CT)
                .setCommandSource(CommandSourceEnum.USER)
                .setCommandData("{\"ct\":120}")
                .setSpaceId(7)
                .setSpaceName("201")
                .setAreaId(3)
                .setAccountId(88)
                .setSuccess(false)
                .setEnsureSuccess(true)
                .setExecuteTimes(3)
                .setOperateUser(1)
                .setOperateUserName("管理员")
                .setRemark("重试中");
        when(deviceCommandService.getDeviceCommandDetail(19)).thenReturn(recordBo);

        DeviceOperationDetailVo detailVo = deviceOperationBiz.getDeviceOperation(19);

        assertThat(detailVo.getId()).isEqualTo(19);
        assertThat(detailVo.getDeviceType()).isEqualTo("electricMeter");
        assertThat(detailVo.getCommandType()).isEqualTo(5);
        assertThat(detailVo.getCommandTypeName()).isEqualTo("设置CT变比");
        assertThat(detailVo.getCommandSource()).isEqualTo(1);
        assertThat(detailVo.getCommandSourceName()).isEqualTo("用户命令");
        assertThat(detailVo.getCommandData()).isEqualTo("{\"ct\":120}");
        assertThat(detailVo.getSpaceName()).isEqualTo("201");
        assertThat(detailVo.getRemark()).isEqualTo("重试中");
    }

    @Test
    @DisplayName("查询设备操作执行记录_应正确映射")
    void testFindDeviceOperationExecuteRecordList_ShouldMapResult() {
        DeviceCommandExecuteRecordBo executeRecordBo = new DeviceCommandExecuteRecordBo()
                .setId(1)
                .setCommandId(19)
                .setSuccess(false)
                .setReason("设备离线")
                .setRunTime(LocalDateTime.of(2026, 3, 16, 8, 30, 0))
                .setCommandSource(CommandSourceEnum.SYSTEM);
        when(deviceCommandService.findCommandExecuteRecordList(19)).thenReturn(List.of(executeRecordBo));

        List<DeviceOperationExecuteRecordVo> result = deviceOperationBiz.findDeviceOperationExecuteRecordList(19);

        assertThat(result).hasSize(1);
        DeviceOperationExecuteRecordVo recordVo = result.get(0);
        assertThat(recordVo.getCommandId()).isEqualTo(19);
        assertThat(recordVo.getSuccess()).isFalse();
        assertThat(recordVo.getReason()).isEqualTo("设备离线");
        assertThat(recordVo.getCommandSource()).isEqualTo(0);
        assertThat(recordVo.getCommandSourceName()).isEqualTo("系统命令");
    }
}
