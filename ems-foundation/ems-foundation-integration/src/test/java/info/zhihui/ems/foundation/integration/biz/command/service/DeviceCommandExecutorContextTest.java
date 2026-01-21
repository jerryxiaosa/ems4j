package info.zhihui.ems.foundation.integration.biz.command.service;

import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.foundation.integration.biz.command.enums.CommandTypeEnum;
import info.zhihui.ems.foundation.integration.biz.command.service.DeviceCommandExecutor;
import info.zhihui.ems.foundation.integration.biz.command.service.DeviceCommandExecutorContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceCommandExecutorContextTest {

    @Mock
    private DeviceCommandExecutor mockExecutor1;

    @Mock
    private DeviceCommandExecutor mockExecutor2;

    private DeviceCommandExecutorContext context;


    @Test
    void constructor_withExecutorList_shouldInitializeMap() {
        // Given
        List<DeviceCommandExecutor> executorList = Arrays.asList(mockExecutor1, mockExecutor2);
        when(mockExecutor1.getCommandType()).thenReturn(CommandTypeEnum.ENERGY_ELECTRIC_TURN_ON);
        when(mockExecutor2.getCommandType()).thenReturn(CommandTypeEnum.ENERGY_ELECTRIC_TURN_OFF);

        // When
        context = new DeviceCommandExecutorContext(executorList);

        // Then
        DeviceCommandExecutor result1 = context.getDeviceCommandExecutor(CommandTypeEnum.ENERGY_ELECTRIC_TURN_ON);
        DeviceCommandExecutor result2 = context.getDeviceCommandExecutor(CommandTypeEnum.ENERGY_ELECTRIC_TURN_OFF);

        assertEquals(mockExecutor1, result1);
        assertEquals(mockExecutor2, result2);
    }

    @Test
    void constructor_withEmptyList_shouldInitializeEmptyMap() {
        // Given
        List<DeviceCommandExecutor> executorList = Collections.emptyList();

        // When
        context = new DeviceCommandExecutorContext(executorList);

        // Then
        assertThrows(NotFoundException.class, () -> {
            context.getDeviceCommandExecutor(CommandTypeEnum.ENERGY_ELECTRIC_TURN_ON);
        });
    }

    @Test
    void getDeviceCommandExecutor_withValidCommandType_shouldReturnExecutor() {
        when(mockExecutor1.getCommandType()).thenReturn(CommandTypeEnum.ENERGY_ELECTRIC_TURN_ON);
        // Given
        List<DeviceCommandExecutor> executorList = Arrays.asList(mockExecutor1);
        context = new DeviceCommandExecutorContext(executorList);

        // When
        DeviceCommandExecutor result = context.getDeviceCommandExecutor(CommandTypeEnum.ENERGY_ELECTRIC_TURN_ON);

        // Then
        assertEquals(mockExecutor1, result);
    }

    @Test
    void getDeviceCommandExecutor_withInvalidCommandType_shouldThrowNotFoundException() {
        // Given
        List<DeviceCommandExecutor> executorList = Arrays.asList(mockExecutor1);
        context = new DeviceCommandExecutorContext(executorList);

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            context.getDeviceCommandExecutor(CommandTypeEnum.ENERGY_ELECTRIC_TURN_OFF);
        });

        assertEquals("没有找到对应的命令执行器", exception.getMessage());
    }

    @Test
    void getDeviceCommandExecutor_withNullCommandType_shouldThrowNotFoundException() {
        when(mockExecutor1.getCommandType()).thenReturn(CommandTypeEnum.ENERGY_ELECTRIC_TURN_ON);
        // Given
        List<DeviceCommandExecutor> executorList = Arrays.asList(mockExecutor1);
        context = new DeviceCommandExecutorContext(executorList);

        // When & Then
        assertThrows(NotFoundException.class, () -> {
            context.getDeviceCommandExecutor(null);
        });
    }
}