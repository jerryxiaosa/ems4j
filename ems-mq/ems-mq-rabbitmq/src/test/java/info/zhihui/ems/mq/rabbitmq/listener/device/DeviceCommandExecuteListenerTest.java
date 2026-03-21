package info.zhihui.ems.mq.rabbitmq.listener.device;

import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.foundation.integration.biz.command.enums.CommandSourceEnum;
import info.zhihui.ems.foundation.integration.biz.command.exception.DeviceCommandExecuteException;
import info.zhihui.ems.foundation.integration.biz.command.service.DeviceCommandService;
import info.zhihui.ems.mq.api.message.device.DeviceCommandExecuteMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DeviceCommandExecuteListenerTest {

    @Mock
    private DeviceCommandService deviceCommandService;

    @InjectMocks
    private DeviceCommandExecuteListener deviceCommandExecuteListener;

    @Test
    @DisplayName("接收到设备命令执行消息应按 commandId 委托给命令服务")
    void testHandle_ShouldDelegateToDeviceCommandService() {
        DeviceCommandExecuteMessage message = new DeviceCommandExecuteMessage().setCommandId(1001);

        deviceCommandExecuteListener.handle(message);

        verify(deviceCommandService).execDeviceCommand(1001, CommandSourceEnum.SYSTEM);
    }

    @Test
    @DisplayName("命令执行失败异常应记录后吞掉")
    void testHandle_WhenDeviceCommandExecuteException_ShouldNotThrow() {
        DeviceCommandExecuteMessage message = new DeviceCommandExecuteMessage().setCommandId(1001);
        doThrow(new DeviceCommandExecuteException("执行失败"))
                .when(deviceCommandService).execDeviceCommand(1001, CommandSourceEnum.SYSTEM);

        assertThatCode(() -> deviceCommandExecuteListener.handle(message)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("业务异常应记录后吞掉")
    void testHandle_WhenBusinessRuntimeException_ShouldNotThrow() {
        DeviceCommandExecuteMessage message = new DeviceCommandExecuteMessage().setCommandId(1001);
        doThrow(new BusinessRuntimeException("状态已变化"))
                .when(deviceCommandService).execDeviceCommand(1001, CommandSourceEnum.SYSTEM);

        assertThatCode(() -> deviceCommandExecuteListener.handle(message)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("基础设施异常应继续抛出")
    void testHandle_WhenUnexpectedException_ShouldRethrow() {
        DeviceCommandExecuteMessage message = new DeviceCommandExecuteMessage().setCommandId(1001);
        doThrow(new RuntimeException("db down"))
                .when(deviceCommandService).execDeviceCommand(1001, CommandSourceEnum.SYSTEM);

        assertThatThrownBy(() -> deviceCommandExecuteListener.handle(message))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("db down");
    }
}
