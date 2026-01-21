package info.zhihui.ems.foundation.integration.biz.command.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.page.PageMethod;
import info.zhihui.ems.common.enums.DeviceTypeEnum;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.components.lock.core.LockTemplate;
import info.zhihui.ems.foundation.integration.biz.command.bo.DeviceCommandExecuteRecordBo;
import info.zhihui.ems.foundation.integration.biz.command.bo.DeviceCommandRecordBo;
import info.zhihui.ems.foundation.integration.biz.command.dto.DeviceCommandAddDto;
import info.zhihui.ems.foundation.integration.biz.command.dto.DeviceCommandCancelDto;
import info.zhihui.ems.foundation.integration.biz.command.dto.DeviceCommandQueryDto;
import info.zhihui.ems.foundation.integration.biz.command.entity.DeviceCommandExecuteRecordEntity;
import info.zhihui.ems.foundation.integration.biz.command.entity.DeviceCommandRecordEntity;
import info.zhihui.ems.foundation.integration.biz.command.enums.CommandSourceEnum;
import info.zhihui.ems.foundation.integration.biz.command.enums.CommandTypeEnum;
import info.zhihui.ems.foundation.integration.biz.command.repository.DeviceCommandExecuteRecordRepository;
import info.zhihui.ems.foundation.integration.biz.command.repository.DeviceCommandRecordRepository;
import info.zhihui.ems.foundation.integration.biz.command.service.DeviceCommandExecutor;
import info.zhihui.ems.foundation.integration.biz.command.service.DeviceCommandExecutorContext;
import info.zhihui.ems.foundation.integration.biz.command.service.impl.DeviceCommandServiceImpl;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.locks.Lock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceCommandServiceImplTest {

    @InjectMocks
    private DeviceCommandServiceImpl deviceCommandService;

    @Mock
    private DeviceCommandRecordRepository commandRecordRepository;

    @Mock
    private DeviceCommandExecuteRecordRepository commandExecuteRecordRepository;

    @Mock
    private DeviceCommandExecutorContext deviceCommandExecutorContext;

    @Mock
    private LockTemplate lockTemplate;

    @Mock
    private Lock mockLock;

    @Mock
    private DeviceCommandExecutor mockExecutor;

    private DeviceCommandAddDto addDto;
    private DeviceCommandRecordEntity recordEntity;
    private DeviceCommandRecordBo recordBo;
    private DeviceCommandQueryDto queryDto;
    private DeviceCommandCancelDto cancelDto;

    @BeforeEach
    void setUp() {
        addDto = new DeviceCommandAddDto()
                .setDeviceType(DeviceTypeEnum.ELECTRIC)
                .setDeviceId(1)
                .setDeviceIotId("iot123")
                .setDeviceNo("DEV001")
                .setDeviceName("测试设备")
                .setSpaceId(1)
                .setSpaceName("测试空间")
                .setCommandType(CommandTypeEnum.ENERGY_ELECTRIC_TURN_ON)
                .setCommandSource(CommandSourceEnum.USER)
                .setCommandData("{\"action\":\"turnOn\"}")
                .setEnsureSuccess(true)
                .setRemark("测试备注")
                .setAreaId(100)
                .setOperateUser(1)
                .setOperateUserName("测试用户");

        recordEntity = new DeviceCommandRecordEntity()
                .setId(1)
                .setDeviceTypeKey("electricMeter")
                .setDeviceId(1)
                .setDeviceIotId("iot123")
                .setDeviceNo("DEV001")
                .setDeviceName("测试设备")
                .setCommandType(1)
                .setCommandSource(1)
                .setCommandData("{\"action\":\"turnOn\"}")
                .setSpaceId(1)
                .setSpaceName("测试空间")
                .setSuccess(true)
                .setEnsureSuccess(true)
                .setExecuteTimes(1)
                .setRemark("测试备注");
        recordEntity.setOwnAreaId(100)
                .setCreateUser(1)
                .setCreateUserName("测试用户")
                .setCreateTime(LocalDateTime.now());

        recordBo = new DeviceCommandRecordBo()
                .setId(1)
                .setDeviceType(DeviceTypeEnum.ELECTRIC)
                .setDeviceId(1)
                .setDeviceIotId("iot123")
                .setDeviceNo("DEV001")
                .setDeviceName("测试设备")
                .setCommandType(CommandTypeEnum.ENERGY_ELECTRIC_TURN_ON)
                .setCommandSource(CommandSourceEnum.USER)
                .setCommandData("{\"action\":\"turnOn\"}")
                .setSpaceId(1)
                .setSpaceName("测试空间")
                .setAreaId(100)
                .setSuccess(true)
                .setEnsureSuccess(true)
                .setExecuteTimes(1)
                .setOperateUser(1)
                .setOperateUserName("测试用户")
                .setCreateTime(LocalDateTime.now())
                .setRemark("测试备注");

        queryDto = new DeviceCommandQueryDto()
                .setOperateUserName("测试用户")
                .setCommandType(CommandTypeEnum.ENERGY_ELECTRIC_TURN_ON)
                .setSuccess(true)
                .setOrganizationName("测试机构")
                .setSpaceName("测试空间")
                .setDeviceName("测试设备")
                .setDeviceNo("DEV001")
                .setDeviceType(DeviceTypeEnum.ELECTRIC);

        cancelDto = new DeviceCommandCancelDto()
                .setDeviceId(1)
                .setDeviceType(DeviceTypeEnum.ELECTRIC)
                .setCommandType(CommandTypeEnum.ENERGY_ELECTRIC_TURN_ON)
                .setReason("取消原因");
    }

    @Test
    void saveDeviceCommand_shouldReturnCommandId() {
        // Given
        when(commandRecordRepository.insert(any(DeviceCommandRecordEntity.class))).thenAnswer(invocation -> {
            DeviceCommandRecordEntity entity = invocation.getArgument(0);
            entity.setId(1);
            return 1;
        });

        // When
        Integer result = deviceCommandService.saveDeviceCommand(addDto);

        // Then
        assertEquals(1, result);
        verify(commandRecordRepository).insert(any(DeviceCommandRecordEntity.class));
    }

    @Test
    void execDeviceCommand_whenLockFailed_shouldThrowException() {
        // Given
        when(commandRecordRepository.selectById(recordBo.getId())).thenReturn(recordEntity);
        when(lockTemplate.getLock(anyString())).thenReturn(mockLock);
        when(mockLock.tryLock()).thenReturn(false);

        // When & Then
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class, () -> {
            deviceCommandService.execDeviceCommand(recordBo.getId(), CommandSourceEnum.USER);
        });

        assertEquals("设备命令正在处理中，请稍后重试", exception.getMessage());
    }

    @Test
    void execDeviceCommand_whenExecuteSuccess_shouldSaveSuccessRecord() {
        // Given
        when(lockTemplate.getLock(anyString())).thenReturn(mockLock);
        when(mockLock.tryLock()).thenReturn(true);
        when(commandRecordRepository.selectById(recordBo.getId())).thenReturn(recordEntity);
        when(deviceCommandExecutorContext.getDeviceCommandExecutor(any(CommandTypeEnum.class))).thenReturn(mockExecutor);
        doNothing().when(mockExecutor).execute(any(DeviceCommandRecordBo.class));

        // When
        deviceCommandService.execDeviceCommand(recordBo.getId(), CommandSourceEnum.USER);

        // Then
        verify(mockExecutor).execute(any(DeviceCommandRecordBo.class));
        verify(commandExecuteRecordRepository).insert(any(DeviceCommandExecuteRecordEntity.class));
        verify(commandRecordRepository).updateCommandExecuteInfo(any(DeviceCommandRecordEntity.class));
        verify(mockLock).unlock();
    }

    @Test
    void execDeviceCommand_whenExecuteFailed_shouldSaveFailureRecord() {
        // Given
        when(lockTemplate.getLock(anyString())).thenReturn(mockLock);
        when(mockLock.tryLock()).thenReturn(true);
        when(commandRecordRepository.selectById(recordBo.getId())).thenReturn(recordEntity);
        when(deviceCommandExecutorContext.getDeviceCommandExecutor(any(CommandTypeEnum.class))).thenReturn(mockExecutor);
        doThrow(new RuntimeException("执行失败")).when(mockExecutor).execute(any(DeviceCommandRecordBo.class));

        // When
        deviceCommandService.execDeviceCommand(recordBo.getId(), CommandSourceEnum.USER);

        // Then
        verify(mockExecutor).execute(any(DeviceCommandRecordBo.class));
        verify(commandExecuteRecordRepository).insert(any(DeviceCommandExecuteRecordEntity.class));
        verify(commandRecordRepository).updateCommandExecuteInfo(any(DeviceCommandRecordEntity.class));
        verify(mockLock).unlock();
    }

    @Test
    void execDeviceCommand_withNullDeviceId_shouldThrowException() {
        // Given
        when(commandRecordRepository.selectById(1)).thenReturn(recordEntity.setDeviceId(null));

        // When & Then
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class, () -> {
            deviceCommandService.execDeviceCommand(1, CommandSourceEnum.USER);
        });

        assertEquals("设备ID和类型不能为空", exception.getMessage());
    }

    @Test
    void findDeviceCommandPage_shouldReturnPageResult() {
        // Given
        PageParam pageParam = new PageParam().setPageNum(1).setPageSize(10);
        List<DeviceCommandRecordEntity> entityList = List.of(recordEntity);

        try (MockedStatic<PageMethod> pageHelper = Mockito.mockStatic(PageMethod.class)) {
            Page mockPage = Mockito.mock(Page.class);
            PageInfo<DeviceCommandRecordEntity> pageInfo = new PageInfo<>();
            pageInfo.setList(entityList);
            pageInfo.setTotal(1);
            pageHelper.when(() -> PageMethod.startPage(pageParam.getPageNum(), pageParam.getPageSize())).thenReturn(mockPage);

            when(mockPage.doSelectPageInfo(any())).thenReturn(pageInfo);

            PageResult<DeviceCommandRecordBo> result = deviceCommandService.findDeviceCommandPage(queryDto, pageParam);

            assertNotNull(result);
            assertEquals(1, result.getList().size());
        }
    }


    @Test
    void getDeviceCommandDetail_whenFound_shouldReturnBo() {
        // Given
        when(commandRecordRepository.selectById(1)).thenReturn(recordEntity);

        // When
        DeviceCommandRecordBo result = deviceCommandService.getDeviceCommandDetail(1);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("测试设备", result.getDeviceName());
        verify(commandRecordRepository).selectById(1);
    }

    @Test
    void getDeviceCommandDetail_whenNotFound_shouldThrowException() {
        // Given
        when(commandRecordRepository.selectById(1)).thenReturn(null);

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            deviceCommandService.getDeviceCommandDetail(1);
        });

        assertEquals("没找到命令记录", exception.getMessage());
    }

    @Test
    void findCommandExecuteRecordList_shouldReturnList() {
        // Given
        DeviceCommandExecuteRecordEntity executeEntity = new DeviceCommandExecuteRecordEntity()
                .setId(1)
                .setCommandId(1)
                .setSuccess(true)
                .setReason(null)
                .setExecuteTime(LocalDateTime.now())
                .setCommandSource(1);

        when(commandExecuteRecordRepository.findList(1)).thenReturn(List.of(executeEntity));

        // When
        List<DeviceCommandExecuteRecordBo> result = deviceCommandService.findCommandExecuteRecordList(1);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getId());
        verify(commandExecuteRecordRepository).findList(1);
    }

    @Test
    void cancelDeviceCommand_whenLockFailed_shouldThrowException() {
        // Given
        when(lockTemplate.getLock(anyString())).thenReturn(mockLock);
        when(mockLock.tryLock()).thenReturn(false);

        // When & Then
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class, () -> {
            deviceCommandService.cancelDeviceCommand(cancelDto);
        });

        assertEquals("设备命令正在处理中，请稍后重试", exception.getMessage());
    }

    @Test
    void cancelDeviceCommand_whenLockSuccess_shouldCancelCommand() {
        // Given
        when(lockTemplate.getLock(anyString())).thenReturn(mockLock);
        when(mockLock.tryLock()).thenReturn(true);

        // When
        deviceCommandService.cancelDeviceCommand(cancelDto);

        // Then
        verify(commandRecordRepository).cancelDeviceCommand(any());
        verify(mockLock).unlock();
    }

}