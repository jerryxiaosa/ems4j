package info.zhihui.ems.business.device.service.impl;

import info.zhihui.ems.business.device.bo.ElectricMeterBo;
import info.zhihui.ems.business.device.bo.GatewayBo;
import info.zhihui.ems.business.device.dto.*;
import info.zhihui.ems.business.device.entity.ElectricMeterEntity;
import info.zhihui.ems.business.device.entity.MeterCancelRecordEntity;
import info.zhihui.ems.business.device.entity.MeterStepEntity;
import info.zhihui.ems.business.device.entity.OpenMeterEntity;
import info.zhihui.ems.business.device.enums.ElectricSwitchStatusEnum;
import info.zhihui.ems.business.device.mapper.ElectricMeterMapper;
import info.zhihui.ems.business.device.qo.AccountMeterStepQo;
import info.zhihui.ems.business.device.qo.ElectricMeterBatchUpdateQo;
import info.zhihui.ems.business.device.qo.ElectricMeterResetAccountQo;
import info.zhihui.ems.business.device.repository.ElectricMeterRepository;
import info.zhihui.ems.business.device.repository.MeterCancelRecordRepository;
import info.zhihui.ems.business.device.repository.MeterStepRepository;
import info.zhihui.ems.business.device.repository.OpenMeterRepository;
import info.zhihui.ems.business.device.service.DeviceStatusSynchronizer;
import info.zhihui.ems.business.device.service.ElectricMeterInfoService;
import info.zhihui.ems.business.device.service.GatewayService;
import info.zhihui.ems.business.device.service.impl.sync.ElectricMeterOnlineStatusSynchronizer;
import info.zhihui.ems.business.finance.bo.BalanceBo;
import info.zhihui.ems.business.finance.dto.BalanceQueryDto;
import info.zhihui.ems.business.finance.dto.ElectricMeterDetailDto;
import info.zhihui.ems.business.finance.dto.ElectricMeterPowerRecordDto;
import info.zhihui.ems.business.finance.service.record.ElectricMeterPowerRecordService;
import info.zhihui.ems.business.finance.service.balance.BalanceService;
import info.zhihui.ems.business.finance.service.consume.MeterConsumeService;
import info.zhihui.ems.business.plan.bo.WarnPlanBo;
import info.zhihui.ems.business.plan.dto.ElectricPriceTimeDto;
import info.zhihui.ems.business.plan.service.ElectricPricePlanService;
import info.zhihui.ems.business.plan.service.WarnPlanService;
import info.zhihui.ems.common.enums.*;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.components.context.RequestContext;
import info.zhihui.ems.foundation.integration.biz.command.dto.DeviceCommandAddDto;
import info.zhihui.ems.foundation.integration.biz.command.dto.DeviceCommandCancelDto;
import info.zhihui.ems.foundation.integration.biz.command.enums.CommandSourceEnum;
import info.zhihui.ems.foundation.integration.biz.command.service.DeviceCommandService;
import info.zhihui.ems.foundation.integration.concrete.energy.dto.BaseElectricDeviceDto;
import info.zhihui.ems.foundation.integration.concrete.energy.dto.ElectricDeviceAddDto;
import info.zhihui.ems.foundation.integration.concrete.energy.dto.ElectricDeviceDegreeDto;
import info.zhihui.ems.foundation.integration.concrete.energy.service.EnergyService;
import info.zhihui.ems.foundation.integration.core.bo.DeviceModelBo;
import info.zhihui.ems.foundation.integration.core.service.DeviceModelService;
import info.zhihui.ems.foundation.integration.core.service.DeviceModuleContext;
import info.zhihui.ems.foundation.space.bo.SpaceBo;
import info.zhihui.ems.foundation.space.enums.SpaceTypeEnum;
import info.zhihui.ems.foundation.space.service.SpaceService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * ElectricMeterServiceImpl单元测试类
 *
 * @author jerryxiaosa
 */
@ExtendWith(MockitoExtension.class)
class ElectricMeterManagerServiceImplTest {

    @Mock
    private ElectricMeterRepository repository;

    @Mock
    private ElectricMeterMapper mapper;

    @Mock
    private ElectricPricePlanService electricPlanService;

    @Mock
    private RequestContext requestContext;

    @Mock
    private GatewayService gatewayService;

    @Mock
    private DeviceCommandService deviceCommandService;

    @Mock
    private DeviceModuleContext deviceModuleContext;

    @Mock
    private DeviceModelService deviceModelService;

    @Mock
    private WarnPlanService warnPlanService;

    @Mock
    private SpaceService spaceService;

    @Mock
    private EnergyService energyService;

    @Mock
    private MeterConsumeService meterConsumeService;

    @Mock
    private BalanceService balanceService;

    @Mock
    private OpenMeterRepository openMeterRepository;

    @Mock
    private MeterStepRepository accountMeterStepRepository;

    @Mock
    private ElectricMeterInfoService electricMeterInfoService;

    @Mock
    private MeterCancelRecordRepository meterCancelRecordRepository;

    @Mock
    private ElectricMeterPowerRecordService electricMeterPowerRecordService;

    @InjectMocks
    private ElectricMeterManagerServiceImpl electricMeterService;

    private ElectricMeterCreateDto saveDto;
    private ElectricMeterUpdateDto updateDto;
    private ElectricMeterEntity entity;
    private ElectricMeterBo bo;
    private DeviceModelBo deviceModelBo;
    private SpaceBo spaceBo;
    private GatewayBo gatewayBo;

    @BeforeEach
    void setUp() {
        saveDto = new ElectricMeterCreateDto()
                .setSpaceId(100)
                .setMeterName("测试电表")
                .setIsCalculate(true)
                .setIsPrepay(false)
                .setModelId(200)
                .setGatewayId(300)
                .setPortNo(1)
                .setMeterAddress(1)
                .setCt(1);
        updateDto = new ElectricMeterUpdateDto()
                .setId(1)
                .setSpaceId(100)
                .setMeterName("测试电表")
                .setCalculateType(CalculateTypeEnum.AIR_CONDITIONING)
                .setIsCalculate(true)
                .setIsPrepay(false);

        entity = new ElectricMeterEntity();
        entity.setId(1)
                .setSpaceId(100)
                .setMeterName("测试电表")
                .setMeterNo("EM202401010001")
                .setDeviceNo("GW-DEVICE-001:1:1")
                .setModelId(200)
                .setProductCode("厂商型号")
                .setCommunicateModel("485")
                .setGatewayId(300)
                .setPortNo(1)
                .setMeterAddress(1)
                .setIsCalculate(true)
                .setIsPrepay(false)
                .setCt(1)
                .setOwnAreaId(1000);
        // 避免链式调用返回父类导致方法不可用，分开设置
        entity.setIotId(12345);
        entity.setIsDeleted(false);

        bo = new ElectricMeterBo()
                .setId(1)
                .setSpaceId(100)
                .setMeterName("测试电表")
                .setMeterNo("EM202401010001")
                .setDeviceNo("GW-DEVICE-001:1:1")
                .setModelId(200)
                .setGatewayId(300)
                .setPortNo(1)
                .setMeterAddress(1)
                .setIsCutOff(false)
                .setIsCalculate(true)
                .setIsPrepay(false)
                .setCt(1)
                .setOwnAreaId(1000)
                .setIotId(12345);

        Map<String, Object> modelProperty = Map.of("communicateModel", "485", "isCt", true, "isPrepay", false);
        deviceModelBo = new DeviceModelBo()
                .setId(200)
                .setManufacturerName("厂商")
                .setModelName("型号")
                .setProductCode("meter-model-200")
                .setTypeKey("electricMeter")
                .setModelProperty(modelProperty);

        spaceBo = new SpaceBo()
                .setId(100)
                .setName("测试空间")
                .setOwnAreaId(1000)
                .setType(SpaceTypeEnum.ROOM);

        gatewayBo = new GatewayBo()
                .setId(300)
                .setIotId(54321)
                .setDeviceNo("GW-DEVICE-001")
                .setIsOnline(true);

        DeviceStatusSynchronizer<ElectricMeterBo> electricMeterOnlineStatusSynchronizer = new ElectricMeterOnlineStatusSynchronizer(deviceModuleContext, repository);
        ReflectionTestUtils.setField(electricMeterService, "electricMeterOnlineStatusSynchronizer", electricMeterOnlineStatusSynchronizer);
    }


    @Test
    void testAdd_Success() {
        // 准备数据
        List<ElectricPriceTimeDto> timeList = List.of(new ElectricPriceTimeDto().setStart(LocalTime.of(0, 0)).setType(ElectricPricePeriodEnum.HIGHER));

        // Mock行为
        when(electricMeterInfoService.getDetail(1)).thenReturn(bo);
        when(mapper.saveDtoToEntity(saveDto)).thenReturn(entity);
        when(deviceModelService.getDetail(200)).thenReturn(deviceModelBo);
        when(spaceService.getDetail(100)).thenReturn(spaceBo);
        when(gatewayService.getDetail(300)).thenReturn(gatewayBo);
        ElectricMeterQueryDto query = new ElectricMeterQueryDto().setGatewayId(300).setPortNo(1).setMeterAddress(1);
        when(electricMeterInfoService.findList(query)).thenReturn(Collections.emptyList());
        when(repository.updateById(any(ElectricMeterEntity.class))).thenReturn(1);
        when(deviceModuleContext.getService(eq(EnergyService.class), eq(1000))).thenReturn(energyService);
        when(energyService.addDevice(any(ElectricDeviceAddDto.class))).thenReturn(12345);
        when(electricPlanService.getElectricTime()).thenReturn(timeList);
        when(requestContext.getUserId()).thenReturn(1001);
        when(requestContext.getUserRealName()).thenReturn("测试用户");

        // 执行测试
        Integer result = electricMeterService.add(saveDto);

        // 验证结果
        assertNotNull(result);
        verify(repository).insert(entity);
        verify(repository, times(2)).updateById(any(ElectricMeterEntity.class));
        ArgumentCaptor<ElectricDeviceAddDto> deviceAddCaptor = ArgumentCaptor.forClass(ElectricDeviceAddDto.class);
        verify(energyService).addDevice(deviceAddCaptor.capture());
        ElectricDeviceAddDto capturedDto = deviceAddCaptor.getValue();
        assertEquals("meter-model-200", capturedDto.getProductCode());
        assertEquals(1000, capturedDto.getAreaId());
    }

    @Test
    void testAdd_DeviceModelNotFound() {
        // Mock行为
        when(mapper.saveDtoToEntity(saveDto)).thenReturn(entity);
        when(deviceModelService.getDetail(200)).thenThrow(new NotFoundException("电表型号不存在，重新选择"));

        // 执行测试并验证异常
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> electricMeterService.add(saveDto));

        assertEquals("电表型号不存在，重新选择", exception.getMessage());
    }

    @Test
    void testUpdate_Success() {
        // 准备数据
        ElectricMeterBo oldBo = new ElectricMeterBo()
                .setId(1)
                .setModelId(200)
                .setCommunicateModel("485")
                .setMeterAddress(1)
                .setPortNo(1)
                .setGatewayId(300)
                .setSpaceId(100)
                .setAccountId(null)
                .setIsPrepay(false)
                .setCt(1);

        ElectricMeterEntity updateEntity = new ElectricMeterEntity()
                .setId(1)
                .setSpaceId(101)
                .setMeterName("测试电表")
                .setCalculateType(CalculateTypeEnum.AIR_CONDITIONING.getCode())
                .setIsCalculate(true)
                .setIsPrepay(false);

        // Mock行为
        when(electricMeterInfoService.getDetail(1)).thenReturn(oldBo);
        when(mapper.updateDtoToEntity(updateDto)).thenReturn(updateEntity);
        when(spaceService.getDetail(101)).thenReturn(spaceBo);

        // 设置请求上下文用户信息，用于校验更新人信息
        when(requestContext.getUserId()).thenReturn(1001);
        when(requestContext.getUserRealName()).thenReturn("测试用户");

        // 执行测试
        assertDoesNotThrow(() -> electricMeterService.update(updateDto));

        // 验证结果 - 现在应该调用新的统一更新方法，并传递正确的 resetCalculateType 参数
        // 由于 updateDto.getCalculateType() 不为 null，所以 resetCalculateType 应该为 false
        ArgumentCaptor<ElectricMeterEntity> entityCaptor = ArgumentCaptor.forClass(ElectricMeterEntity.class);
        ArgumentCaptor<Boolean> resetCaptor = ArgumentCaptor.forClass(Boolean.class);
        verify(repository).updateWithCalculateTypeControl(entityCaptor.capture(), resetCaptor.capture());

        ElectricMeterEntity capturedUpdate = entityCaptor.getValue();
        assertEquals(1001, capturedUpdate.getUpdateUser());
        assertEquals("测试用户", capturedUpdate.getUpdateUserName());
        assertNotNull(capturedUpdate.getUpdateTime());
        assertFalse(resetCaptor.getValue());
        verify(repository, never()).updateById(any(ElectricMeterEntity.class));
    }

    @Test
    void testUpdate_WithCalculateTypeNull_Success() {
        // 准备数据 - calculateType为null的情况
        ElectricMeterBo oldBo = new ElectricMeterBo()
                .setId(1)
                .setModelId(200)
                .setSpaceId(100)
                .setAccountId(null)
                .setIsPrepay(false);

        ElectricMeterUpdateDto updateDtoWithNullCalculateType = new ElectricMeterUpdateDto()
                .setId(1)
                .setMeterName("测试电表")
                .setCalculateType(null) // 明确设置为null
                .setIsCalculate(true);

        ElectricMeterEntity updateEntity = new ElectricMeterEntity()
                .setId(1)
                .setMeterName("测试电表")
                .setCalculateType(null) // 映射后也应该是null
                .setIsCalculate(true);

        // Mock行为
        when(electricMeterInfoService.getDetail(1)).thenReturn(oldBo);
        when(mapper.updateDtoToEntity(updateDtoWithNullCalculateType)).thenReturn(updateEntity);

        // 设置请求上下文用户信息，用于校验更新人信息
        when(requestContext.getUserId()).thenReturn(1001);
        when(requestContext.getUserRealName()).thenReturn("测试用户");

        // 执行测试
        assertDoesNotThrow(() -> electricMeterService.update(updateDtoWithNullCalculateType));

        // 验证结果 - 未显式设置resetCalculateType，不应清空calculateType
        ArgumentCaptor<ElectricMeterEntity> entityCaptor = ArgumentCaptor.forClass(ElectricMeterEntity.class);
        ArgumentCaptor<Boolean> resetCaptor = ArgumentCaptor.forClass(Boolean.class);
        verify(repository).updateWithCalculateTypeControl(entityCaptor.capture(), resetCaptor.capture());

        ElectricMeterEntity capturedUpdate = entityCaptor.getValue();
        assertEquals(1001, capturedUpdate.getUpdateUser());
        assertEquals("测试用户", capturedUpdate.getUpdateUserName());
        assertNotNull(capturedUpdate.getUpdateTime());
        assertFalse(resetCaptor.getValue());
        verify(repository, never()).updateById(any(ElectricMeterEntity.class));
    }

    @Test
    void testUpdate_ResetCalculateType_Success() {
        ElectricMeterBo oldBo = new ElectricMeterBo()
                .setId(1)
                .setModelId(200)
                .setSpaceId(100)
                .setAccountId(null)
                .setIsPrepay(false);

        ElectricMeterUpdateDto updateDtoWithReset = new ElectricMeterUpdateDto()
                .setId(1)
                .setMeterName("测试电表")
                .setCalculateType(null)
                .setResetCalculateType(true)
                .setIsCalculate(true);

        ElectricMeterEntity updateEntity = new ElectricMeterEntity()
                .setId(1)
                .setMeterName("测试电表")
                .setCalculateType(null)
                .setIsCalculate(true);

        when(electricMeterInfoService.getDetail(1)).thenReturn(oldBo);
        when(mapper.updateDtoToEntity(updateDtoWithReset)).thenReturn(updateEntity);
        when(requestContext.getUserId()).thenReturn(1001);
        when(requestContext.getUserRealName()).thenReturn("测试用户");

        assertDoesNotThrow(() -> electricMeterService.update(updateDtoWithReset));

        ArgumentCaptor<ElectricMeterEntity> entityCaptor = ArgumentCaptor.forClass(ElectricMeterEntity.class);
        ArgumentCaptor<Boolean> resetCaptor = ArgumentCaptor.forClass(Boolean.class);
        verify(repository).updateWithCalculateTypeControl(entityCaptor.capture(), resetCaptor.capture());
        assertTrue(resetCaptor.getValue());
    }

    @Test
    void testUpdate_ResetCalculateType_WithCalculateType_ShouldFail() {
        ElectricMeterUpdateDto updateDtoWithConflict = new ElectricMeterUpdateDto()
                .setId(1)
                .setCalculateType(CalculateTypeEnum.AIR_CONDITIONING)
                .setResetCalculateType(true);

        when(electricMeterInfoService.getDetail(1)).thenReturn(bo);
        when(mapper.updateDtoToEntity(updateDtoWithConflict)).thenReturn(new ElectricMeterEntity().setId(1));

        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> electricMeterService.update(updateDtoWithConflict));

        assertEquals("resetCalculateType为true时不允许设置calculateType", exception.getMessage());
        verify(repository, never()).updateWithCalculateTypeControl(any(), anyBoolean());
    }
    @Test
    void testUpdate_WithCalculateTypeNotNull_Success() {
        // 准备数据 - calculateType不为null的情况
        ElectricMeterBo oldBo = new ElectricMeterBo()
                .setId(1)
                .setModelId(200)
                .setSpaceId(100)
                .setAccountId(null)
                .setIsPrepay(false);

        ElectricMeterUpdateDto updateDtoWithCalculateType = new ElectricMeterUpdateDto()
                .setId(1)
                .setMeterName("测试电表")
                .setCalculateType(CalculateTypeEnum.AIR_CONDITIONING) // 设置具体的计量类型
                .setIsCalculate(true);

        ElectricMeterEntity updateEntity = new ElectricMeterEntity()
                .setId(1)
                .setMeterName("测试电表")
                .setCalculateType(CalculateTypeEnum.AIR_CONDITIONING.getCode())
                .setIsCalculate(true);

        // Mock行为
        when(electricMeterInfoService.getDetail(1)).thenReturn(oldBo);
        when(mapper.updateDtoToEntity(updateDtoWithCalculateType)).thenReturn(updateEntity);

        // 设置请求上下文用户信息，用于校验更新人信息
        when(requestContext.getUserId()).thenReturn(1001);
        when(requestContext.getUserRealName()).thenReturn("测试用户");

        // 执行测试
        assertDoesNotThrow(() -> electricMeterService.update(updateDtoWithCalculateType));

        // 验证结果 - 应该调用新的统一更新方法
        ArgumentCaptor<ElectricMeterEntity> entityCaptor = ArgumentCaptor.forClass(ElectricMeterEntity.class);
        ArgumentCaptor<Boolean> resetCaptor = ArgumentCaptor.forClass(Boolean.class);
        verify(repository).updateWithCalculateTypeControl(entityCaptor.capture(), resetCaptor.capture());

        ElectricMeterEntity capturedUpdate = entityCaptor.getValue();
        assertEquals(1001, capturedUpdate.getUpdateUser());
        assertEquals("测试用户", capturedUpdate.getUpdateUserName());
        assertNotNull(capturedUpdate.getUpdateTime());
        assertFalse(resetCaptor.getValue());
        verify(repository, never()).updateById(any(ElectricMeterEntity.class));
    }

    @Test
    void testUpdate_PrepayMoveToNonRoomShouldFail() {
        ElectricMeterBo oldBo = new ElectricMeterBo()
                .setId(1)
                .setSpaceId(100)
                .setAccountId(null)
                .setIsPrepay(true);

        ElectricMeterEntity updateEntity = new ElectricMeterEntity()
                .setId(1)
                .setSpaceId(101)
                .setIsPrepay(true);

        SpaceBo nonRoomSpace = new SpaceBo()
                .setId(101)
                .setOwnAreaId(2000)
                .setType(SpaceTypeEnum.INNER_SPACE);

        when(electricMeterInfoService.getDetail(1)).thenReturn(oldBo);
        when(mapper.updateDtoToEntity(updateDto)).thenReturn(updateEntity);
        when(spaceService.getDetail(101)).thenReturn(nonRoomSpace);

        updateDto.setSpaceId(101).setIsPrepay(true);

        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> electricMeterService.update(updateDto));

        assertEquals("预付费模式下电表只允许绑定到房间", exception.getMessage());
        verify(repository, never()).updateById(any(ElectricMeterEntity.class));
    }

    @Test
    void testSetProtectModel_Success_SetProtect() {
        // 准备数据 - 设置保电模式
        List<Integer> ids = List.of(1, 2);
        ElectricMeterBo meter1 = new ElectricMeterBo()
                .setId(1)
                .setMeterNo("EM001")
                .setIsPrepay(true)
                .setAccountId(100)
                .setIsOnline(true);
        ElectricMeterBo meter2 = new ElectricMeterBo()
                .setId(2)
                .setMeterNo("EM002")
                .setIsPrepay(true)
                .setAccountId(101)
                .setIsOnline(true);
        List<ElectricMeterBo> meterList = List.of(meter1, meter2);

        // Mock行为
        ElectricMeterQueryDto queryDto = new ElectricMeterQueryDto().setInIds(ids);
        when(electricMeterInfoService.findList(queryDto)).thenReturn(meterList);
        when(repository.batchUpdate(any(ElectricMeterBatchUpdateQo.class))).thenReturn(2);

        // 设置请求上下文用户信息，用于校验更新人信息
        when(requestContext.getUserId()).thenReturn(1001);
        when(requestContext.getUserRealName()).thenReturn("测试用户");

        // 执行测试
        assertDoesNotThrow(() -> electricMeterService.setProtectModel(ids, true));

        // 验证批量更新操作
        ArgumentCaptor<ElectricMeterBatchUpdateQo> captor = ArgumentCaptor.forClass(ElectricMeterBatchUpdateQo.class);
        verify(repository).batchUpdate(captor.capture());
        ElectricMeterBatchUpdateQo updateQo = captor.getValue();

        assertEquals(ids, updateQo.getMeterIds());
        assertTrue(updateQo.getProtectedModel());
        // 校验批量更新的更新人信息
        assertEquals(1001, updateQo.getUpdateUser());
        assertEquals("测试用户", updateQo.getUpdateUserName());
        assertNotNull(updateQo.getUpdateTime());
    }

    @Test
    void testSetProtectModel_Success_CancelProtect() {
        // 准备数据 - 取消保电模式
        List<Integer> ids = List.of(1);
        ElectricMeterBo meter = new ElectricMeterBo()
                .setId(1)
                .setMeterNo("EM001")
                .setIsPrepay(true)
                .setAccountId(100)
                .setIsOnline(true);
        List<ElectricMeterBo> meterList = List.of(meter);

        // Mock行为
        ElectricMeterQueryDto queryDto = new ElectricMeterQueryDto().setInIds(ids);

        when(electricMeterInfoService.findList(queryDto)).thenReturn(meterList);
        when(repository.batchUpdate(any(ElectricMeterBatchUpdateQo.class))).thenReturn(1);

        // 设置请求上下文用户信息，用于校验更新人信息
        when(requestContext.getUserId()).thenReturn(1001);
        when(requestContext.getUserRealName()).thenReturn("测试用户");

        // 执行测试
        assertDoesNotThrow(() -> electricMeterService.setProtectModel(ids, false));

        // 验证批量更新操作
        ArgumentCaptor<ElectricMeterBatchUpdateQo> captor = ArgumentCaptor.forClass(ElectricMeterBatchUpdateQo.class);
        verify(repository).batchUpdate(captor.capture());
        ElectricMeterBatchUpdateQo updateQo = captor.getValue();

        assertEquals(ids, updateQo.getMeterIds());
        assertFalse(updateQo.getProtectedModel());
        // 校验批量更新的更新人信息
        assertEquals(1001, updateQo.getUpdateUser());
        assertEquals("测试用户", updateQo.getUpdateUserName());
        assertNotNull(updateQo.getUpdateTime());
    }

    @Test
    void testSetProtectModel_DataException() {
        // 准备数据 - 数据异常（查询到的电表数量与ID数量不匹配）
        List<Integer> ids = List.of(1, 2);
        List<ElectricMeterBo> meterList = List.of(
                new ElectricMeterBo().setId(1).setMeterNo("EM001").setIsOnline(true)
        ); // 只返回一个电表，但请求了两个ID

        // Mock行为
        ElectricMeterQueryDto query = new ElectricMeterQueryDto().setInIds(ids);
        when(electricMeterInfoService.findList(query)).thenReturn(meterList);

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> electricMeterService.setProtectModel(ids, true));

        assertEquals("部分电表数据不存在或已被删除", exception.getMessage());
        verify(repository, never()).batchUpdate(any());
    }

    @Test
    void testSetProtectModel_PrepayMeter() {
        // 准备数据 - 预付费电表（不支持保电）
        List<Integer> ids = List.of(1);
        ElectricMeterBo prepayMeter = new ElectricMeterBo()
                .setId(1)
                .setMeterNo("EM001")
                .setIsPrepay(false) // 预付费电表
                .setAccountId(100)
                .setIsOnline(true);
        List<ElectricMeterBo> meterList = List.of(prepayMeter);

        // Mock行为
        ElectricMeterQueryDto queryDto = new ElectricMeterQueryDto().setInIds(ids);
        when(electricMeterInfoService.findList(queryDto)).thenReturn(meterList);

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> electricMeterService.setProtectModel(ids, true));

        assertEquals("只支持预付费电表:EM001不符合要求", exception.getMessage());
        verify(repository, never()).batchUpdate(any());
    }

    @Test
    void testSetProtectModel_MeterNotOpened() {
        // 准备数据 - 电表尚未开户
        List<Integer> ids = List.of(1);
        // Mock行为
        ElectricMeterQueryDto queryDto = new ElectricMeterQueryDto().setInIds(ids);
        List<ElectricMeterBo> boList = List.of(
                new ElectricMeterBo().setId(1).setMeterNo("EM001").setIsPrepay(true).setAccountId(null).setIsOnline(true)
        );
        when(electricMeterInfoService.findList(queryDto)).thenReturn(boList);

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> electricMeterService.setProtectModel(ids, true));

        assertEquals("EM001尚未开户，无法操作", exception.getMessage());
        verify(repository, never()).updateById(any(ElectricMeterEntity.class));
    }

    @Test
    void testDelete_Success_WithIot() {
        when(electricMeterInfoService.getDetail(1)).thenReturn(bo);
        when(deviceModuleContext.getService(eq(EnergyService.class), eq(1000))).thenReturn(energyService);

        assertDoesNotThrow(() -> electricMeterService.delete(1));

        verify(repository).deleteById(1);

        ArgumentCaptor<DeviceCommandCancelDto> cancelCaptor = ArgumentCaptor.forClass(DeviceCommandCancelDto.class);
        verify(deviceCommandService).cancelDeviceCommand(cancelCaptor.capture());
        DeviceCommandCancelDto cancelDto = cancelCaptor.getValue();
        assertEquals(1, cancelDto.getDeviceId());
        assertEquals(DeviceTypeEnum.ELECTRIC, cancelDto.getDeviceType());
        assertEquals("用户删除电表", cancelDto.getReason());

        ArgumentCaptor<BaseElectricDeviceDto> delCaptor = ArgumentCaptor.forClass(BaseElectricDeviceDto.class);
        verify(energyService).delDevice(delCaptor.capture());
        BaseElectricDeviceDto delDto = delCaptor.getValue();
        assertEquals(12345, delDto.getDeviceId());
        assertEquals(1000, delDto.getAreaId());
    }

    @Test
    void testDelete_Success_NoIot() {
        bo.setIotId(null);
        when(electricMeterInfoService.getDetail(1)).thenReturn(bo);

        assertDoesNotThrow(() -> electricMeterService.delete(1));

        verify(repository).deleteById(1);

        ArgumentCaptor<DeviceCommandCancelDto> cancelDtoCaptor = ArgumentCaptor.forClass(DeviceCommandCancelDto.class);
        verify(deviceCommandService).cancelDeviceCommand(cancelDtoCaptor.capture());
        DeviceCommandCancelDto capturedCancelDto = cancelDtoCaptor.getValue();
        assertNotNull(capturedCancelDto.getDeviceId());

        verify(deviceModuleContext, never()).getService(eq(EnergyService.class), anyInt());
        verifyNoInteractions(energyService);
    }

    @Test
    void testDelete_AccountOpened_ShouldThrow() {
        bo.setAccountId(999);
        when(electricMeterInfoService.getDetail(1)).thenReturn(bo);

        BusinessRuntimeException ex = assertThrows(BusinessRuntimeException.class, () -> electricMeterService.delete(1));
        assertEquals("该电表已开户，无法删除，请先销户", ex.getMessage());

        verify(repository, never()).deleteById(anyInt());
        verify(deviceCommandService, never()).cancelDeviceCommand(any());
        verifyNoInteractions(energyService);
    }

    @Test
    void testDelete_NotFound_NoOp() {
        when(electricMeterInfoService.getDetail(1)).thenThrow(NotFoundException.class);

        assertThrows(NotFoundException.class, () -> electricMeterService.delete(1));

        verify(repository, never()).deleteById(anyInt());
        verify(deviceCommandService, never()).cancelDeviceCommand(any());
        verifyNoInteractions(energyService);
    }

    @Test
    void testAdd_CtNotSupportedButProvided() {
        // 准备数据 - 型号不支持CT但提供了CT值
        Map<String, Object> modelProperty = Map.of("communicateModel", "485", "isCt", false, "isPrepay", false);
        DeviceModelBo nonCtDeviceModel = new DeviceModelBo()
                .setId(200)
                .setManufacturerName("厂商")
                .setProductCode("meter-model-nonct")
                .setTypeKey("electricMeter")
                .setModelProperty(modelProperty);

        saveDto.setCt(2); // 设置CT值
        entity.setCt(2);

        // Mock行为
        when(mapper.saveDtoToEntity(saveDto)).thenReturn(entity);
        when(deviceModelService.getDetail(200)).thenReturn(nonCtDeviceModel);

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> electricMeterService.add(saveDto));

        assertEquals("当前电表型号不支持CT变比", exception.getMessage());
    }

    @Test
    void testAdd_CtSupportedButNotProvided() {
        // 准备数据 - 型号支持CT但未提供CT值，应该设置默认值
        List<ElectricPriceTimeDto> timeList = List.of(new ElectricPriceTimeDto().setStart(LocalTime.of(0, 0)).setType(ElectricPricePeriodEnum.HIGHER));

        saveDto.setCt(null); // 不设置CT值
        entity.setCt(null);

        // Mock行为
        ElectricMeterEntity saveEntity = new ElectricMeterEntity().setId(1)
                .setSpaceId(100)
                .setMeterName("测试电表")
                .setIsCalculate(true)
                .setIsPrepay(false)
                .setModelId(200)
                .setGatewayId(300)
                .setPortNo(1)
                .setMeterAddress(1)
                .setCt(1);

        when(mapper.saveDtoToEntity(saveDto)).thenReturn(saveEntity);
        when(electricMeterInfoService.getDetail(1)).thenReturn(bo);
        when(deviceModelService.getDetail(200)).thenReturn(deviceModelBo); // deviceModelBo支持CT
        when(spaceService.getDetail(100)).thenReturn(spaceBo);
        when(gatewayService.getDetail(300)).thenReturn(gatewayBo);
        when(repository.updateById(any(ElectricMeterEntity.class))).thenReturn(1);
        when(deviceModuleContext.getService(eq(EnergyService.class), eq(1000))).thenReturn(energyService);
        when(energyService.addDevice(any(ElectricDeviceAddDto.class))).thenReturn(12345);
        when(electricPlanService.getElectricTime()).thenReturn(timeList);
        when(requestContext.getUserId()).thenReturn(1001);
        when(requestContext.getUserRealName()).thenReturn("测试用户");

        // 执行测试
        Integer result = electricMeterService.add(saveDto);

        // 验证结果 - CT应该被设置为默认值1
        assertNotNull(result);
        verify(repository).insert(saveEntity);
    }

    @Test
    void testAdd_PrepayNotSupported() {
        // 准备数据 - 型号不支持预付费但设置了预付费
        Map<String, Object> modelProperty = Map.of("communicateModel", "485", "isCt", true, "isPrepay", false);
        DeviceModelBo nonPrepayDeviceModel = new DeviceModelBo()
                .setId(200)
                .setManufacturerName("厂商")
                .setProductCode("meter-model-nonprepay")
                .setTypeKey("electricMeter")
                .setModelProperty(modelProperty);

        saveDto.setIsPrepay(true); // 设置预付费
        entity.setIsPrepay(true);

        // Mock行为
        when(mapper.saveDtoToEntity(saveDto)).thenReturn(entity);
        when(deviceModelService.getDetail(200)).thenReturn(nonPrepayDeviceModel);

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> electricMeterService.add(saveDto));

        assertEquals("当前电表型号不支持预付费", exception.getMessage());
    }

    @Test
    void testAdd_NbModeWithoutImei() {
        // 准备数据 - NB模式但没有IMEI
        Map<String, Object> modelProperty = Map.of("communicateModel", "NB", "isCt", true, "isPrepay", false);
        DeviceModelBo nbDeviceModel = new DeviceModelBo()
                .setId(200)
                .setManufacturerName("厂商")
                .setModelName("型号")
                .setProductCode("meter-model-nb-1")
                .setTypeKey("electricMeter")
                .setModelProperty(modelProperty);

        entity.setCommunicateModel("NB");
        entity.setImei(""); // 空IMEI

        // Mock行为
        when(mapper.saveDtoToEntity(saveDto)).thenReturn(entity);
        when(deviceModelService.getDetail(200)).thenReturn(nbDeviceModel);

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> electricMeterService.add(saveDto));

        assertEquals("NB模式电表IMEI不能为空", exception.getMessage());
    }

    @Test
    void testAdd_NbModeWithDuplicateInfo() {
        // 准备数据 - NB模式重复信息
        Map<String, Object> modelProperty = Map.of("communicateModel", "NB", "isCt", true, "isPrepay", false);
        DeviceModelBo nbDeviceModel = new DeviceModelBo()
                .setId(200)
                .setManufacturerName("厂商")
                .setModelName("型号")
                .setProductCode("meter-model-nb-2")
                .setTypeKey("electricMeter")
                .setModelProperty(modelProperty);

        entity.setCommunicateModel("NB");
        entity.setImei("123456789012345");
        saveDto.setDeviceNo("NB-DEVICE-001");

        ElectricMeterBo duplicate = new ElectricMeterBo();
        duplicate.setId(2);

        // Mock行为
        when(mapper.saveDtoToEntity(saveDto)).thenReturn(entity);
        when(deviceModelService.getDetail(200)).thenReturn(nbDeviceModel);
        ElectricMeterQueryDto query = new ElectricMeterQueryDto().setImei("123456789012345");
        when(electricMeterInfoService.findList(query)).thenReturn(Collections.singletonList(duplicate));

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> electricMeterService.add(saveDto));

        assertEquals("电表信息重复", exception.getMessage());
    }

    @Test
    void testAdd_NonNbModeWithoutGateway() {
        // 准备数据 - 非NB模式但没有网关
        saveDto.setGatewayId(null);
        entity.setGatewayId(null);

        // Mock行为
        when(mapper.saveDtoToEntity(saveDto)).thenReturn(entity);
        when(deviceModelService.getDetail(200)).thenReturn(deviceModelBo);

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> electricMeterService.add(saveDto));

        assertEquals("非NB模式电表必须绑定网关", exception.getMessage());
    }

    @Test
    void testAdd_NonNbModeWithoutPortNo() {
        // 准备数据 - 非NB模式但没有串口号
        saveDto.setPortNo(null);
        entity.setPortNo(null);

        // Mock行为
        when(mapper.saveDtoToEntity(saveDto)).thenReturn(entity);
        when(deviceModelService.getDetail(200)).thenReturn(deviceModelBo);

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> electricMeterService.add(saveDto));

        assertEquals("非NB模式电表串口号不能为空", exception.getMessage());
    }

    @Test
    void testAdd_GatewayNotFound() {
        // 准备数据 - 网关不存在
        // Mock行为
        when(mapper.saveDtoToEntity(saveDto)).thenReturn(entity);
        when(deviceModelService.getDetail(200)).thenReturn(deviceModelBo);
        when(gatewayService.getDetail(300)).thenReturn(null);

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> electricMeterService.add(saveDto));

        assertEquals("网关信息有误，请重新选择", exception.getMessage());
    }

    @Test
    void testAdd_GatewayWithoutIotId() {
        // 准备数据 - 网关没有IotId
        GatewayBo invalidGateway = new GatewayBo()
                .setId(300)
                .setIotId(null)
                .setIsOnline(true);

        // Mock行为
        when(mapper.saveDtoToEntity(saveDto)).thenReturn(entity);
        when(deviceModelService.getDetail(200)).thenReturn(deviceModelBo);
        when(gatewayService.getDetail(300)).thenReturn(invalidGateway);

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> electricMeterService.add(saveDto));

        assertEquals("网关信息有误，请重新选择", exception.getMessage());
    }

    @Test
    void testAdd_GatewayOffline() {
        // 准备数据 - 网关离线
        GatewayBo offlineGateway = new GatewayBo()
                .setId(300)
                .setIotId(54321)
                .setIsOnline(false);

        // Mock行为
        when(mapper.saveDtoToEntity(saveDto)).thenReturn(entity);
        when(deviceModelService.getDetail(200)).thenReturn(deviceModelBo);
        when(gatewayService.getDetail(300)).thenReturn(offlineGateway);

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> electricMeterService.add(saveDto));

        assertEquals("网关不在线，请重试", exception.getMessage());
    }

    @Test
    void testAdd_NonNbModeWithDuplicateInfo() {
        // 准备数据 - 非NB模式重复信息
        ElectricMeterBo duplicateBo = new ElectricMeterBo();
        duplicateBo.setId(2);

        // Mock行为
        when(mapper.saveDtoToEntity(saveDto)).thenReturn(entity);
        when(deviceModelService.getDetail(200)).thenReturn(deviceModelBo);
        when(gatewayService.getDetail(300)).thenReturn(gatewayBo);
        ElectricMeterQueryDto query = new ElectricMeterQueryDto().setGatewayId(300).setPortNo(1).setMeterAddress(1);
        when(electricMeterInfoService.findList(query)).thenReturn(Collections.singletonList(duplicateBo));

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> electricMeterService.add(saveDto));

        assertEquals("电表信息重复", exception.getMessage());
    }

    @Test
    void testAdd_PrepayModeNotInRoom() {
        // 准备数据 - 预付费模式但不是房间类型
        Map<String, Object> modelProperty = Map.of("communicateModel", "485", "isCt", true, "isPrepay", true);
        DeviceModelBo prepayDeviceModel = new DeviceModelBo()
                .setId(200)
                .setManufacturerName("厂商")
                .setProductCode("meter-model-prepay")
                .setTypeKey("electricMeter")
                .setModelProperty(modelProperty);

        SpaceBo buildingSpace = new SpaceBo()
                .setId(100)
                .setName("测试楼栋")
                .setOwnAreaId(1000)
                .setType(SpaceTypeEnum.INNER_SPACE); // 不是房间类型

        saveDto.setIsPrepay(true);
        entity.setIsPrepay(true);

        // Mock行为
        when(mapper.saveDtoToEntity(saveDto)).thenReturn(entity);
        when(deviceModelService.getDetail(200)).thenReturn(prepayDeviceModel);
        when(spaceService.getDetail(100)).thenReturn(buildingSpace);
        when(gatewayService.getDetail(300)).thenReturn(gatewayBo);
        ElectricMeterQueryDto query = new ElectricMeterQueryDto().setMeterAddress(1).setGatewayId(300).setPortNo(1);
        when(electricMeterInfoService.findList(query)).thenReturn(Collections.emptyList());

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> electricMeterService.add(saveDto));

        assertEquals("预付费模式下电表只允许绑定到房间", exception.getMessage());
    }

    @Test
    void testAdd_NbModeSuccess() {
        // 准备数据 - NB模式成功添加
        Map<String, Object> modelProperty = Map.of("communicateModel", "NB", "isCt", true, "isPrepay", false);
        DeviceModelBo nbDeviceModel = new DeviceModelBo()
                .setId(200)
                .setManufacturerName("厂商")
                .setModelName("型号")
                .setProductCode("meter-model-nb-3")
                .setTypeKey("electricMeter")
                .setModelProperty(modelProperty);

        entity.setCommunicateModel("NB");
        entity.setImei("123456789012345");
        entity.setMeterAddress(1);
        saveDto.setDeviceNo("NB-DEVICE-002");

        List<ElectricPriceTimeDto> timeList = List.of(new ElectricPriceTimeDto().setStart(LocalTime.of(0, 0)).setType(ElectricPricePeriodEnum.HIGHER));

        // Mock行为
        when(electricMeterInfoService.getDetail(1)).thenReturn(bo);
        when(mapper.saveDtoToEntity(saveDto)).thenReturn(entity);
        when(deviceModelService.getDetail(200)).thenReturn(nbDeviceModel);
        when(spaceService.getDetail(100)).thenReturn(spaceBo);
        ElectricMeterQueryDto query = new ElectricMeterQueryDto().setImei("123456789012345");
        when(electricMeterInfoService.findList(query)).thenReturn(Collections.emptyList());
        when(repository.updateById(any(ElectricMeterEntity.class))).thenReturn(1);
        when(deviceModuleContext.getService(eq(EnergyService.class), eq(1000))).thenReturn(energyService);
        when(energyService.addDevice(any(ElectricDeviceAddDto.class))).thenReturn(12345);
        when(electricPlanService.getElectricTime()).thenReturn(timeList);
        when(requestContext.getUserId()).thenReturn(1001);
        when(requestContext.getUserRealName()).thenReturn("测试用户");

        // 执行测试
        Integer result = electricMeterService.add(saveDto);

        // 验证结果 - NB模式下网关ID应该被设置为null
        assertNotNull(result);
        verify(repository).insert(entity);
        verify(repository, times(2)).updateById(any(ElectricMeterEntity.class));

        ArgumentCaptor<ElectricDeviceAddDto> deviceCaptor = ArgumentCaptor.forClass(ElectricDeviceAddDto.class);
        verify(energyService).addDevice(deviceCaptor.capture());
        ElectricDeviceAddDto capturedDevice = deviceCaptor.getValue();
        assertEquals("meter-model-nb-3", capturedDevice.getProductCode());
        assertEquals(1000, capturedDevice.getAreaId());
    }


    @Test
    void testSetSwitchStatusSingle_Success_TurnOn() {
        // 准备数据 - 电表当前为断闸状态，需要合闸
        ElectricMeterBo meterBo = new ElectricMeterBo();
        meterBo.setId(1)
                .setMeterNo("EM202401010001")
                .setSpaceId(100)
                .setIotId(12345)
                .setIsOnline(true)
                .setIsCutOff(true) // 当前断闸
                .setOwnAreaId(1000);

        // Mock行为
        when(electricMeterInfoService.getDetail(1)).thenReturn(meterBo);

        // 创建正确的DTO参数
        ElectricMeterSwitchStatusDto switchStatusDto = new ElectricMeterSwitchStatusDto()
                .setId(1)
                .setSwitchStatus(ElectricSwitchStatusEnum.ON)
                .setCommandSource(CommandSourceEnum.USER);

        // 执行测试
        assertDoesNotThrow(() -> electricMeterService.setSwitchStatus(switchStatusDto));

        ArgumentCaptor<DeviceCommandAddDto> commandCaptor = ArgumentCaptor.forClass(DeviceCommandAddDto.class);
        verify(deviceCommandService).saveDeviceCommand(commandCaptor.capture());
        DeviceCommandAddDto capturedCommand = commandCaptor.getValue();
        assertEquals("12345", capturedCommand.getDeviceIotId());
        assertEquals(CommandSourceEnum.USER, capturedCommand.getCommandSource());

        // 验证更新电表状态
        ArgumentCaptor<ElectricMeterEntity> captor = ArgumentCaptor.forClass(ElectricMeterEntity.class);
        verify(repository).updateById(captor.capture());
        ElectricMeterEntity updatedEntity = captor.getValue();
        assertEquals(1, updatedEntity.getId());
        assertFalse(updatedEntity.getIsCutOff()); // 应该设置为合闸状态
    }

    @Test
    void testSetSwitchStatusSingle_MissingIotId_ShouldThrow() {
        ElectricMeterBo meterBo = new ElectricMeterBo();
        meterBo.setId(1)
                .setMeterNo("EM202401010001")
                .setSpaceId(100)
                .setIotId(null)
                .setIsOnline(true)
                .setIsCutOff(true)
                .setOwnAreaId(1000);

        when(electricMeterInfoService.getDetail(1)).thenReturn(meterBo);

        ElectricMeterSwitchStatusDto switchStatusDto = new ElectricMeterSwitchStatusDto()
                .setId(1)
                .setSwitchStatus(ElectricSwitchStatusEnum.ON)
                .setCommandSource(CommandSourceEnum.USER);

        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> electricMeterService.setSwitchStatus(switchStatusDto));

        assertEquals("设备EM202401010001异常，请联系管理员处理", exception.getMessage());
        verify(deviceCommandService, never()).saveDeviceCommand(any(DeviceCommandAddDto.class));
    }

    @Test
    void testSetSwitchStatusSingle_Success_TurnOff() {
        // 准备数据 - 电表当前为合闸状态，需要断闸
        ElectricMeterBo meterBo = new ElectricMeterBo();
        meterBo.setId(1)
                .setMeterNo("EM202401010001")
                .setSpaceId(100)
                .setIotId(12345)
                .setIsOnline(true)
                .setIsCutOff(false)
                .setOwnAreaId(1000);

        // Mock行为
        when(electricMeterInfoService.getDetail(1)).thenReturn(meterBo);

        // 创建正确的DTO参数
        ElectricMeterSwitchStatusDto switchStatusDto = new ElectricMeterSwitchStatusDto()
                .setId(1)
                .setSwitchStatus(ElectricSwitchStatusEnum.OFF)
                .setCommandSource(CommandSourceEnum.USER);

        // 执行测试
        assertDoesNotThrow(() -> electricMeterService.setSwitchStatus(switchStatusDto));

        // 验证结果
        ArgumentCaptor<DeviceCommandAddDto> commandCaptor = ArgumentCaptor.forClass(DeviceCommandAddDto.class);
        verify(deviceCommandService).saveDeviceCommand(commandCaptor.capture());
        DeviceCommandAddDto capturedCommand = commandCaptor.getValue();
        assertEquals("12345", capturedCommand.getDeviceIotId());
        assertEquals(CommandSourceEnum.USER, capturedCommand.getCommandSource());

        // 验证更新电表状态
        ArgumentCaptor<ElectricMeterEntity> captor = ArgumentCaptor.forClass(ElectricMeterEntity.class);
        verify(repository).updateById(captor.capture());
        ElectricMeterEntity updatedEntity = captor.getValue();
        assertEquals(1, updatedEntity.getId());
        assertTrue(updatedEntity.getIsCutOff()); // 应该设置为断闸状态
    }

    @Test
    void testSetSwitchStatusSingle_MeterNotFound() {
        // Mock行为 - 电表不存在
        when(electricMeterInfoService.getDetail(1)).thenThrow(new NotFoundException("电表数据不存在或已被删除"));

        // 创建正确的DTO参数
        ElectricMeterSwitchStatusDto switchStatusDto = new ElectricMeterSwitchStatusDto()
                .setId(1)
                .setSwitchStatus(ElectricSwitchStatusEnum.ON)
                .setCommandSource(CommandSourceEnum.USER);

        // 执行测试并验证异常
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> electricMeterService.setSwitchStatus(switchStatusDto));

        assertEquals("电表数据不存在或已被删除", exception.getMessage());
        verify(deviceCommandService, never()).saveDeviceCommand(any());
    }

    @Test
    void testSetSwitchStatusSingle_IotIdIsNull() {
        // 准备数据 - IoT ID为空
        ElectricMeterBo meterWithoutIot = new ElectricMeterBo()
                .setId(1)
                .setMeterNo("EM202401010001")
                .setIotId(null) // IoT ID为空
                .setIsOnline(true);

        // Mock行为
        when(electricMeterInfoService.getDetail(1)).thenReturn(meterWithoutIot);

        // 创建正确的DTO参数
        ElectricMeterSwitchStatusDto switchStatusDto = new ElectricMeterSwitchStatusDto()
                .setId(1)
                .setSwitchStatus(ElectricSwitchStatusEnum.ON)
                .setCommandSource(CommandSourceEnum.USER);

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> electricMeterService.setSwitchStatus(switchStatusDto));

        assertEquals("设备EM202401010001异常，请联系管理员处理", exception.getMessage());
        verify(deviceCommandService, never()).saveDeviceCommand(any());
    }

    @Test
    void testSetSwitchStatusSingle_MeterOffline() {
        // 准备数据 - 电表离线
        ElectricMeterBo offlineMeter = new ElectricMeterBo()
                .setId(1)
                .setMeterNo("EM202401010001")
                .setIotId(12345)
                .setIsOnline(false); // 离线状态

        // Mock行为
        when(electricMeterInfoService.getDetail(1)).thenReturn(offlineMeter);

        // 创建正确的DTO参数
        ElectricMeterSwitchStatusDto switchStatusDto = new ElectricMeterSwitchStatusDto()
                .setId(1)
                .setSwitchStatus(ElectricSwitchStatusEnum.ON)
                .setCommandSource(CommandSourceEnum.USER);

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> electricMeterService.setSwitchStatus(switchStatusDto));

        assertEquals("设备EM202401010001处于离线状态，请重新选择", exception.getMessage());
        verify(deviceCommandService, never()).saveDeviceCommand(any());
    }

    @Test
    void testSetSwitchStatusSingle_AlreadyInTargetState_On() {
        // 准备数据 - 电表已经是合闸状态，要求合闸
        ElectricMeterBo onlineMeter = new ElectricMeterBo()
                .setId(1)
                .setMeterNo("EM202401010001")
                .setIotId(12345)
                .setIsOnline(true)
                .setIsCutOff(false); // 已经是合闸状态

        // Mock行为
        when(electricMeterInfoService.getDetail(1)).thenReturn(onlineMeter);

        // 创建正确的DTO参数
        ElectricMeterSwitchStatusDto switchStatusDto = new ElectricMeterSwitchStatusDto()
                .setId(1)
                .setSwitchStatus(ElectricSwitchStatusEnum.ON)
                .setCommandSource(CommandSourceEnum.USER);

        // 执行测试
        assertDoesNotThrow(() -> electricMeterService.setSwitchStatus(switchStatusDto));

        // 验证：仍应下发命令并更新数据库状态，避免状态不一致
        ArgumentCaptor<DeviceCommandAddDto> commandCaptor = ArgumentCaptor.forClass(DeviceCommandAddDto.class);
        verify(deviceCommandService).saveDeviceCommand(commandCaptor.capture());
        DeviceCommandAddDto capturedCommand = commandCaptor.getValue();
        assertEquals("12345", capturedCommand.getDeviceIotId());
        assertEquals(CommandSourceEnum.USER, capturedCommand.getCommandSource());

        ArgumentCaptor<ElectricMeterEntity> entityCaptor = ArgumentCaptor.forClass(ElectricMeterEntity.class);
        verify(repository).updateById(entityCaptor.capture());
        ElectricMeterEntity updatedEntity = entityCaptor.getValue();
        assertEquals(1, updatedEntity.getId());
        assertFalse(updatedEntity.getIsCutOff()); // 目标合闸
    }

    @Test
    void testSetSwitchStatusSingle_AlreadyInTargetState_Off() {
        // 准备数据 - 电表已经是断闸状态，要求断闸
        ElectricMeterBo onlineMeter = new ElectricMeterBo()
                .setId(1)
                .setMeterNo("EM202401010001")
                .setIotId(12345)
                .setIsOnline(true)
                .setIsCutOff(true); // 已经是断闸状态

        // Mock行为
        when(electricMeterInfoService.getDetail(1)).thenReturn(onlineMeter);

        // 创建正确的DTO参数
        ElectricMeterSwitchStatusDto switchStatusDto = new ElectricMeterSwitchStatusDto()
                .setId(1)
                .setSwitchStatus(ElectricSwitchStatusEnum.OFF)
                .setCommandSource(CommandSourceEnum.USER);

        // 执行测试
        assertDoesNotThrow(() -> electricMeterService.setSwitchStatus(switchStatusDto));

        // 验证：仍应下发命令并更新数据库状态，避免状态不一致
        ArgumentCaptor<DeviceCommandAddDto> commandCaptor = ArgumentCaptor.forClass(DeviceCommandAddDto.class);
        verify(deviceCommandService).saveDeviceCommand(commandCaptor.capture());
        DeviceCommandAddDto capturedCommand = commandCaptor.getValue();
        assertEquals("12345", capturedCommand.getDeviceIotId());
        assertEquals(CommandSourceEnum.USER, capturedCommand.getCommandSource());

        ArgumentCaptor<ElectricMeterEntity> entityCaptor = ArgumentCaptor.forClass(ElectricMeterEntity.class);
        verify(repository).updateById(entityCaptor.capture());
        ElectricMeterEntity updatedEntity = entityCaptor.getValue();
        assertEquals(1, updatedEntity.getId());
        assertTrue(updatedEntity.getIsCutOff()); // 目标断闸
    }


    @Test
    void testUpdate_CheckUpdateInfo_ModifyPrepayForOpenedAccount() {
        // 准备数据 - 已开户电表修改预付费属性
        ElectricMeterBo oldBo = new ElectricMeterBo()
                .setId(1)
                .setSpaceId(100)
                .setAccountId(500) // 已开户
                .setIsPrepay(false); // 原来是后付费

        ElectricMeterUpdateDto updateDto = new ElectricMeterUpdateDto()
                .setId(1)
                .setSpaceId(100)
                .setIsPrepay(true); // 修改为预付费

        ElectricMeterEntity updateEntity = new ElectricMeterEntity()
                .setId(1)
                .setSpaceId(100)
                .setIsPrepay(true);

        // Mock行为
        when(electricMeterInfoService.getDetail(1)).thenReturn(oldBo);
        when(mapper.updateDtoToEntity(updateDto)).thenReturn(updateEntity);

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> electricMeterService.update(updateDto));

        assertEquals("已开户电表不允许修改预付费属性", exception.getMessage());

        // 验证没有执行更新操作
        verify(repository, never()).updateById(any(ElectricMeterEntity.class));
    }

    @Test
    void testUpdate_CheckUpdateInfo_ModifySpaceForOpenedAccount() {
        // 准备数据 - 已开户电表修改绑定房间
        ElectricMeterBo oldBo = new ElectricMeterBo()
                .setId(1)
                .setSpaceId(100) // 原房间
                .setAccountId(500) // 已开户
                .setIsPrepay(false);

        ElectricMeterUpdateDto updateDto = new ElectricMeterUpdateDto()
                .setId(1)
                .setSpaceId(101) // 修改房间
                .setIsPrepay(false);

        ElectricMeterEntity updateEntity = new ElectricMeterEntity()
                .setId(1)
                .setSpaceId(101)
                .setIsPrepay(false);

        // Mock行为
        when(electricMeterInfoService.getDetail(1)).thenReturn(oldBo);
        when(mapper.updateDtoToEntity(updateDto)).thenReturn(updateEntity);

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> electricMeterService.update(updateDto));

        assertEquals("已开户电表不允许修改绑定房间", exception.getMessage());

        // 验证没有执行更新操作
        verify(repository, never()).updateById(any(ElectricMeterEntity.class));
    }

    @Test
    void testUpdate_CheckUpdateInfo_ModifySpaceForUnopenedAccount_Success() {
        ElectricMeterUpdateDto updateDto = new ElectricMeterUpdateDto()
                .setId(1)
                .setSpaceId(101) // 修改房间
                .setIsPrepay(false);

        ElectricMeterEntity updateEntity = new ElectricMeterEntity()
                .setId(1)
                .setSpaceId(101)
                .setIsPrepay(false);

        SpaceBo newSpace = new SpaceBo()
                .setId(101)
                .setName("新房间")
                .setOwnAreaId(1000)
                .setType(SpaceTypeEnum.ROOM);

        // Mock行为
        when(electricMeterInfoService.getDetail(1)).thenReturn(bo);
        when(mapper.updateDtoToEntity(updateDto)).thenReturn(updateEntity);
        when(spaceService.getDetail(101)).thenReturn(newSpace);

        // 执行测试
        assertDoesNotThrow(() -> electricMeterService.update(updateDto));

        // 验证空间信息设置
        assertEquals(1000, updateEntity.getOwnAreaId());

        // 验证执行了更新操作
        verify(repository).updateWithCalculateTypeControl(updateEntity, false);
        verify(spaceService).getDetail(101);
    }

    @Test
    void testUpdate_CheckUpdateInfo_ModifySpaceForUnopenedAccount_PrepayNotInRoom() {
        ElectricMeterUpdateDto updateDto = new ElectricMeterUpdateDto()
                .setId(1)
                .setSpaceId(101) // 修改到楼栋
                .setIsPrepay(true);

        ElectricMeterEntity updateEntity = new ElectricMeterEntity()
                .setId(1)
                .setSpaceId(101)
                .setIsPrepay(true);

        SpaceBo buildingSpace = new SpaceBo()
                .setId(101)
                .setName("测试楼栋")
                .setOwnAreaId(1000)
                .setType(SpaceTypeEnum.INNER_SPACE); // 不是房间类型

        Map<String, Object> modelProperty = Map.of("communicateModel", "485", "isCt", true, "isPrepay", true);
        DeviceModelBo deviceModelBo = new DeviceModelBo().setId(200).setModelProperty(modelProperty);

        // Mock行为
        when(electricMeterInfoService.getDetail(1)).thenReturn(bo);
        when(mapper.updateDtoToEntity(updateDto)).thenReturn(updateEntity);
        when(deviceModelService.getDetail(200)).thenReturn(deviceModelBo);
        when(spaceService.getDetail(101)).thenReturn(buildingSpace);

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> electricMeterService.update(updateDto));

        assertEquals("预付费模式下电表只允许绑定到房间", exception.getMessage());

        // 验证没有执行更新操作
        verify(repository, never()).updateById(any(ElectricMeterEntity.class));
        verify(spaceService).getDetail(101);
    }

    @Test
    void testUpdate_CheckUpdateInfo_NoChangeNeeded() {
        // 准备数据 - 没有修改关键属性，不触发校验
        ElectricMeterBo oldBo = new ElectricMeterBo()
                .setId(1)
                .setSpaceId(100)
                .setAccountId(500) // 已开户
                .setIsPrepay(false);

        ElectricMeterUpdateDto updateDto = new ElectricMeterUpdateDto()
                .setId(1)
                .setSpaceId(100) // 房间不变
                .setIsPrepay(false) // 预付费属性不变
                .setMeterName("新电表名称"); // 只修改名称

        ElectricMeterEntity updateEntity = new ElectricMeterEntity()
                .setId(1)
                .setSpaceId(100)
                .setIsPrepay(false)
                .setMeterName("新电表名称");

        // Mock行为
        when(electricMeterInfoService.getDetail(1)).thenReturn(oldBo);
        when(mapper.updateDtoToEntity(updateDto)).thenReturn(updateEntity);

        // 执行测试
        assertDoesNotThrow(() -> electricMeterService.update(updateDto));

        // 验证执行了更新操作，但没有调用空间服务
        verify(repository).updateWithCalculateTypeControl(updateEntity, false);
        verify(spaceService, never()).getDetail(any());
    }

    @Test
    void testUpdate_CheckUpdateInfo_UnopenedAccountModifyPrepay() {
        ElectricMeterUpdateDto updateDto = new ElectricMeterUpdateDto()
                .setId(1)
                .setSpaceId(100)
                .setIsPrepay(true); // 修改为预付费

        ElectricMeterEntity updateEntity = new ElectricMeterEntity()
                .setId(1)
                .setSpaceId(100)
                .setIsPrepay(true);

        Map<String, Object> modelProperty = Map.of("communicateModel", "485", "isCt", true, "isPrepay", true);
        DeviceModelBo deviceModelBo = new DeviceModelBo().setId(200).setModelProperty(modelProperty);

        // Mock行为
        when(electricMeterInfoService.getDetail(1)).thenReturn(bo);
        when(deviceModelService.getDetail(200)).thenReturn(deviceModelBo);
        when(mapper.updateDtoToEntity(updateDto)).thenReturn(updateEntity);

        // 执行测试
        assertDoesNotThrow(() -> electricMeterService.update(updateDto));

        // 验证执行了更新操作
        verify(repository).updateWithCalculateTypeControl(updateEntity, false);
    }

    @Test
    void testSetMeterCt_Success() {
        // 准备测试数据
        ElectricMeterCtDto dto = new ElectricMeterCtDto()
                .setMeterId(1)
                .setCt(100);

        ElectricMeterEntity existingEntity = new ElectricMeterEntity()
                .setId(1)
                .setModelId(1000)
                .setCt(50)
                .setAccountId(null) // 未开户
                .setIotId(123);

        DeviceModelBo deviceModel = new DeviceModelBo()
                .setId(1000)
                .setProductCode("meter-model-1000")
                .setModelProperty(Map.of("isCt", true));

        // Mock行为
        ElectricMeterBo detailBo = new ElectricMeterBo().setId(1).setModelId(1000).setIotId(123).setCt(110);
        when(electricMeterInfoService.getDetail(1)).thenReturn(detailBo);
        existingEntity.setCt(dto.getCt());
        when(mapper.boToEntity(detailBo)).thenReturn(existingEntity);
        when(deviceModelService.getDetail(1000)).thenReturn(deviceModel);
        int newMeterId = 2;
        when(repository.insert(any(ElectricMeterEntity.class))).thenAnswer(invocation -> {
            ElectricMeterEntity entity = invocation.getArgument(0);
            entity.setId(newMeterId);
            return 1;
        });
        ElectricMeterBo newBo = new ElectricMeterBo()
                .setId(newMeterId)
                .setModelId(1000)
                .setIotId(123)
                .setCt(dto.getCt());
        when(electricMeterInfoService.getDetail(newMeterId)).thenReturn(newBo);

        // 执行测试
        assertDoesNotThrow(() -> electricMeterService.setMeterCt(dto));

        // 验证调用了删除和重建操作
        verify(repository).deleteById(1);
        ArgumentCaptor<ElectricMeterEntity> entityCaptor = ArgumentCaptor.forClass(ElectricMeterEntity.class);
        verify(repository).insert(entityCaptor.capture());
        ElectricMeterEntity insertedEntity = entityCaptor.getValue();
        assertEquals(dto.getCt(), insertedEntity.getCt());
        assertEquals(newMeterId, insertedEntity.getId());
    }

    @Test
    void testSetMeterCt_SameValue_StillExecutes() {
        // 准备测试数据
        ElectricMeterCtDto dto = new ElectricMeterCtDto()
                .setMeterId(1)
                .setCt(100);

        ElectricMeterBo existing = new ElectricMeterBo()
                .setId(1)
                .setModelId(1000)
                .setCt(100) // 相同的CT值
                .setAccountId(null)
                .setIotId(123)
                .setOwnAreaId(1000);

        DeviceModelBo deviceModel = new DeviceModelBo()
                .setId(1000)
                .setProductCode("meter-model-1000")
                .setModelProperty(Map.of("isCt", true));

        ElectricMeterEntity entityToInsert = new ElectricMeterEntity()
                .setId(1)
                .setModelId(1000)
                .setIotId(123)
                .setCt(100);

        // Mock行为
        when(electricMeterInfoService.getDetail(1)).thenReturn(existing);
        when(deviceModelService.getDetail(1000)).thenReturn(deviceModel);
        when(mapper.boToEntity(existing)).thenReturn(entityToInsert);
        int newMeterId = 2;
        when(repository.insert(any(ElectricMeterEntity.class))).thenAnswer(invocation -> {
            ElectricMeterEntity entity = invocation.getArgument(0);
            entity.setId(newMeterId);
            return 1;
        });
        ElectricMeterBo newBo = new ElectricMeterBo()
                .setId(newMeterId)
                .setModelId(1000)
                .setIotId(123)
                .setCt(dto.getCt());
        when(electricMeterInfoService.getDetail(newMeterId)).thenReturn(newBo);

        // 执行测试：不再抛出异常
        assertDoesNotThrow(() -> electricMeterService.setMeterCt(dto));

        // 验证进行了删除和重建操作
        verify(repository).deleteById(1);
        ArgumentCaptor<ElectricMeterEntity> entityCaptor = ArgumentCaptor.forClass(ElectricMeterEntity.class);
        verify(repository).insert(entityCaptor.capture());
        ElectricMeterEntity insertedEntity = entityCaptor.getValue();
        assertEquals(dto.getCt(), insertedEntity.getCt());
        assertEquals(newMeterId, insertedEntity.getId());

        // 验证设备命令被保存（下发CT指令）
        verify(deviceCommandService).saveDeviceCommand(any(DeviceCommandAddDto.class));
        // 验证执行设备命令（来源为SYSTEM）
        verify(deviceCommandService).execDeviceCommand(anyInt(), eq(CommandSourceEnum.SYSTEM));
    }

    @Test
    void testSetCt_DeviceNotSupportMeterCt() {
        // 准备测试数据
        ElectricMeterCtDto dto = new ElectricMeterCtDto()
                .setMeterId(1)
                .setCt(100);

        ElectricMeterBo existingBo = new ElectricMeterBo()
                .setId(1)
                .setModelId(1000)
                .setCt(50)
                .setAccountId(null);

        DeviceModelBo deviceModel = new DeviceModelBo()
                .setId(1000)
                .setProductCode("meter-model-1000-noct")
                .setModelProperty(Map.of("isCt", false)); // 不支持CT

        // Mock行为
        when(electricMeterInfoService.getDetail(1)).thenReturn(existingBo);
        when(deviceModelService.getDetail(1000)).thenReturn(deviceModel);

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> electricMeterService.setMeterCt(dto));

        assertEquals("该电表不支持ct变比", exception.getMessage());

        // 验证没有进行任何更新操作
        verify(repository, never()).deleteById(anyInt());
        verify(repository, never()).updateById(any(ElectricMeterEntity.class));
    }

    @Test
    void testSetMeterCt_AccountOpened_ShouldThrow() {
        // 准备测试数据
        ElectricMeterCtDto dto = new ElectricMeterCtDto()
                .setMeterId(1)
                .setCt(100);

        ElectricMeterBo existingBo = new ElectricMeterBo()
                .setId(1)
                .setModelId(1000)
                .setCt(50)
                .setAccountId(500); // 已开户

        DeviceModelBo deviceModel = new DeviceModelBo()
                .setId(1000)
                .setProductCode("meter-model-1000-ct")
                .setModelProperty(Map.of("isCt", true));

        // Mock行为
        when(electricMeterInfoService.getDetail(1)).thenReturn(existingBo);
        when(deviceModelService.getDetail(1000)).thenReturn(deviceModel);

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> electricMeterService.setMeterCt(dto));

        assertEquals("已开户电表不允许修改ct", exception.getMessage());

        // 验证没有进行任何更新操作
        verify(repository, never()).deleteById(anyInt());
        verify(repository, never()).updateById(any(ElectricMeterEntity.class));
    }

    @Test
    void testSetMeterCt_DeviceModelPropertiesNull() {
        // 准备测试数据
        ElectricMeterCtDto dto = new ElectricMeterCtDto()
                .setMeterId(1)
                .setCt(100);

        ElectricMeterBo existingBo = new ElectricMeterBo()
                .setId(1)
                .setModelId(1000)
                .setCt(50)
                .setAccountId(null);

        DeviceModelBo deviceModel = new DeviceModelBo()
                .setId(1000)
                .setProductCode("meter-model-1000-null")
                .setModelProperty(null); // 属性为null

        // Mock行为
        when(electricMeterInfoService.getDetail(1)).thenReturn(existingBo);
        when(deviceModelService.getDetail(1000)).thenReturn(deviceModel);

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> electricMeterService.setMeterCt(dto));

        assertEquals("该电表不支持ct变比", exception.getMessage());
    }

    @Test
    void testSetCt_MissingIsMeterCtProperty() {
        // 准备测试数据
        ElectricMeterCtDto dto = new ElectricMeterCtDto()
                .setMeterId(1)
                .setCt(100);

        ElectricMeterBo existing = new ElectricMeterBo()
                .setId(1)
                .setModelId(1000)
                .setCt(50)
                .setAccountId(null);

        DeviceModelBo deviceModel = new DeviceModelBo()
                .setId(1000)
                .setProductCode("meter-model-1000-missing-ct")
                .setModelProperty(Map.of("otherProperty", "value")); // 缺少isCt属性

        // Mock行为
        when(electricMeterInfoService.getDetail(1)).thenReturn(existing);
        when(deviceModelService.getDetail(1000)).thenReturn(deviceModel);

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> electricMeterService.setMeterCt(dto));

        assertEquals("该电表不支持ct变比", exception.getMessage());
    }

    @Test
    void testOpenMeterAccount_Success() {
        // 准备数据
        List<MeterOpenDetailDto> meterOpenDetail = List.of(
                new MeterOpenDetailDto().setMeterId(1),
                new MeterOpenDetailDto().setMeterId(2)
        );

        List<ElectricMeterBo> meterList = List.of(
                new ElectricMeterBo().setId(1).setMeterNo("EM001").setAccountId(null).setIsPrepay(true).setIsOnline(true),
                new ElectricMeterBo().setId(2).setMeterNo("EM002").setAccountId(null).setIsPrepay(true).setIsOnline(true)
        );
        List<ElectricMeterBo> meterList2 = List.of(
                new ElectricMeterBo().setId(1).setMeterNo("EM001").setAccountId(100).setIsPrepay(true).setIsOnline(true),
                new ElectricMeterBo().setId(2).setMeterNo("EM002").setAccountId(100).setIsPrepay(true).setIsOnline(true)
        );
        Integer accountId = 100;
        MeterOpenDto meterOpenDto = new MeterOpenDto()
                .setMeterOpenDetail(meterOpenDetail)
                .setAccountId(accountId)
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY)
                .setElectricPricePlanId(1)
                .setWarnPlanId(1);

        ElectricMeterQueryDto queryDto = new ElectricMeterQueryDto().setInIds(List.of(1, 2));

        // Mock依赖调用
        when(electricMeterInfoService.findList(queryDto)).thenReturn(meterList).thenReturn(meterList2);
        ElectricMeterEntity meterEntity1 = new ElectricMeterEntity()
                .setId(1)
                .setAccountId(accountId);
        ElectricMeterEntity meterEntity2 = new ElectricMeterEntity()
                .setId(2)
                .setAccountId(accountId);
        when(repository.updateById(meterEntity1)).thenReturn(1);
        when(repository.updateById(meterEntity2)).thenReturn(1);
        when(warnPlanService.getDetail(1)).thenReturn(new WarnPlanBo().setFirstLevel(new BigDecimal("1000")).setSecondLevel(new BigDecimal("400")));
        when(electricMeterInfoService.getDetail(1)).thenReturn(meterList.get(0));
        when(electricMeterInfoService.getDetail(2)).thenReturn(meterList.get(1));
        when(balanceService.query(new BalanceQueryDto().setBalanceRelationId(1).setBalanceType(BalanceTypeEnum.ELECTRIC_METER)))
                .thenReturn(new BalanceBo().setBalance(new BigDecimal("800")));
        when(balanceService.query(new BalanceQueryDto().setBalanceRelationId(2).setBalanceType(BalanceTypeEnum.ELECTRIC_METER)))
                .thenReturn(new BalanceBo().setBalance(new BigDecimal("300")));
        when(electricMeterInfoService.findList(new ElectricMeterQueryDto().setInIds(List.of(1)))).thenReturn(List.of(meterList2.get(0)));
        when(electricMeterInfoService.findList(new ElectricMeterQueryDto().setInIds(List.of(2)))).thenReturn(List.of(meterList2.get(1)));

        when(repository.batchUpdate(any(ElectricMeterBatchUpdateQo.class))).thenReturn(2);
        when(deviceModuleContext.getService(EnergyService.class, meterList.get(0).getOwnAreaId())).thenReturn(energyService);
        when(energyService.getMeterEnergy(any(ElectricDeviceDegreeDto.class))).thenReturn(new BigDecimal("100"));

        // 执行测试
        assertDoesNotThrow(() -> electricMeterService.openMeterAccount(meterOpenDto));

        // 验证调用
        verify(repository).updateById(meterEntity1);
        verify(repository).updateById(meterEntity2);
        verify(balanceService).initElectricMeterBalance(meterEntity1.getId(), meterEntity1.getAccountId());
        verify(balanceService).initElectricMeterBalance(meterEntity2.getId(), meterEntity2.getAccountId());
        ArgumentCaptor<ElectricMeterPowerRecordDto> recordCaptor = ArgumentCaptor.forClass(ElectricMeterPowerRecordDto.class);
        verify(meterConsumeService, times(2)).savePowerRecord(recordCaptor.capture());
        List<ElectricMeterPowerRecordDto> capturedRecords = recordCaptor.getAllValues();
        assertEquals(2, capturedRecords.size());
        assertEquals(1, capturedRecords.get(0).getElectricMeterDetailDto().getMeterId());
        assertEquals(new BigDecimal("500"), capturedRecords.get(0).getPower());
        assertEquals(2, capturedRecords.get(1).getElectricMeterDetailDto().getMeterId());
        assertEquals(new BigDecimal("500"), capturedRecords.get(1).getPower());
        assertTrue(capturedRecords.stream().allMatch(r -> Boolean.FALSE.equals(r.getNeedConsume())));

        ArgumentCaptor<MeterStepEntity> stepCaptor = ArgumentCaptor.forClass(MeterStepEntity.class);
        verify(accountMeterStepRepository, times(2)).insert(stepCaptor.capture());
        List<MeterStepEntity> capturedSteps = stepCaptor.getAllValues();
        assertEquals(2, capturedSteps.size());
        assertEquals(1, capturedSteps.get(0).getMeterId());
        assertEquals(2, capturedSteps.get(1).getMeterId());
        // 验证step起始值与历史偏移
        assertEquals(new BigDecimal("500"), capturedSteps.get(0).getStepStartValue());
        assertEquals(BigDecimal.ZERO, capturedSteps.get(0).getHistoryPowerOffset());
        assertEquals(new BigDecimal("500"), capturedSteps.get(1).getStepStartValue());
        assertEquals(BigDecimal.ZERO, capturedSteps.get(1).getHistoryPowerOffset());
        assertTrue(capturedSteps.stream().allMatch(step -> Boolean.TRUE.equals(step.getIsLatest())));
        verify(accountMeterStepRepository, times(2)).clearLatestFlag(any(AccountMeterStepQo.class));
    }

    @Test
    void testOpenMeterAccount_MeterNotFound() {
        // 准备数据
        List<ElectricMeterBo> meterList = List.of(
                new ElectricMeterBo().setId(1).setMeterNo("EM001").setAccountId(null)
        );
        MeterOpenDto meterOpenDto = new MeterOpenDto()
                .setMeterOpenDetail(List.of(
                        new MeterOpenDetailDto().setMeterId(1),
                        new MeterOpenDetailDto().setMeterId(2)
                ))
                .setElectricAccountType(ElectricAccountTypeEnum.MONTHLY)
                .setAccountId(100);

        ElectricMeterQueryDto queryDto = new ElectricMeterQueryDto().setInIds(List.of(1, 2));

        // Mock依赖调用
        when(electricMeterInfoService.findList(queryDto)).thenReturn(meterList);

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> electricMeterService.openMeterAccount(meterOpenDto));
        assertEquals("电表开户操作失败：部分电表数据不存在或已被删除", exception.getMessage());

        // 验证调用
        verify(repository, never()).updateById(any(ElectricMeterEntity.class));
    }

    @Test
    void testOpenMeterAccount_MeterAlreadyOpened() {
        // 准备数据
        List<ElectricMeterBo> meterList = List.of(
                new ElectricMeterBo().setId(1).setMeterNo("EM001").setAccountId(999) // 已开户
        );
        MeterOpenDto meterOpenDto = new MeterOpenDto()
                .setMeterOpenDetail(List.of(
                        new MeterOpenDetailDto().setMeterId(1)
                ))
                .setElectricPricePlanId(1)
                .setWarnPlanId(20)
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY)
                .setAccountId(100);

        ElectricMeterQueryDto queryDto = new ElectricMeterQueryDto().setInIds(List.of(1));

        // Mock依赖调用
        when(electricMeterInfoService.findList(queryDto)).thenReturn(meterList);

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> electricMeterService.openMeterAccount(meterOpenDto));
        assertEquals("电表开户操作失败：EM001已开户，请勿重复开户", exception.getMessage());

        // 验证调用
        verify(repository, never()).updateById(any(ElectricMeterEntity.class));
    }

    @Test
    void testOpenMeterAccount_MultipleMetersAlreadyOpened() {
        // 准备数据
        List<ElectricMeterBo> meterList = List.of(
                new ElectricMeterBo().setId(1).setMeterNo("EM001").setAccountId(null).setIsOnline(true).setIsPrepay(true),
                new ElectricMeterBo().setId(2).setMeterNo("EM002").setAccountId(999).setIsOnline(true).setIsPrepay(true)// 已开户
        );
        MeterOpenDto meterOpenDto = new MeterOpenDto()
                .setMeterOpenDetail(List.of(
                        new MeterOpenDetailDto().setMeterId(1),
                        new MeterOpenDetailDto().setMeterId(2)
                ))
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY)
                .setWarnPlanId(10)
                .setElectricPricePlanId(20)
                .setAccountId(100);

        ElectricMeterQueryDto queryDto = new ElectricMeterQueryDto().setInIds(List.of(1, 2));

        // Mock依赖调用
        when(electricMeterInfoService.findList(queryDto)).thenReturn(meterList);

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> electricMeterService.openMeterAccount(meterOpenDto));
        assertEquals("电表开户操作失败：EM002已开户，请勿重复开户", exception.getMessage());

        // 验证调用
        verify(repository, never()).updateById(ArgumentMatchers.<List<ElectricMeterEntity>>any());
    }

    @Test
    void testOpenMeterAccount_Single() {
        // 准备数据
        List<ElectricMeterBo> meterList = List.of(
                new ElectricMeterBo().setId(1).setMeterNo("EM001").setAccountId(null).setIsOnline(true).setIsPrepay(true)
        );
        List<ElectricMeterBo> meterList2 = List.of(
                new ElectricMeterBo().setId(1).setMeterNo("EM001").setAccountId(100).setIsOnline(true).setIsPrepay(true)
        );
        MeterOpenDto meterOpenDto = new MeterOpenDto()
                .setMeterOpenDetail(List.of(
                        new MeterOpenDetailDto().setMeterId(1)
                ))
                .setElectricAccountType(ElectricAccountTypeEnum.MONTHLY)
                .setAccountId(100);

        ElectricMeterQueryDto queryDto = new ElectricMeterQueryDto().setInIds(List.of(1));

        // Mock依赖调用
        when(electricMeterInfoService.findList(queryDto)).thenReturn(meterList).thenReturn(meterList2);
        ElectricMeterEntity updateEntity1 = new ElectricMeterEntity()
                .setId(1)
                .setAccountId(meterOpenDto.getAccountId());
        when(repository.updateById(updateEntity1)).thenReturn(1);
        when(electricMeterInfoService.getDetail(1)).thenReturn(meterList.get(0));
        when(deviceModuleContext.getService(EnergyService.class, meterList.get(0).getOwnAreaId())).thenReturn(energyService);
        when(energyService.getMeterEnergy(any(ElectricDeviceDegreeDto.class))).thenReturn(new BigDecimal("100"));

        // 执行测试
        assertDoesNotThrow(() -> electricMeterService.openMeterAccount(meterOpenDto));

        // 验证调用
        verify(repository, times(1)).updateById(updateEntity1);
        ArgumentCaptor<OpenMeterEntity> openMeterCaptor = ArgumentCaptor.forClass(OpenMeterEntity.class);
        verify(openMeterRepository, times(1)).insert(openMeterCaptor.capture());
        OpenMeterEntity capturedOpenMeter = openMeterCaptor.getValue();
        assertEquals(1, capturedOpenMeter.getMeterId());
        assertEquals(100, capturedOpenMeter.getAccountId());

        ArgumentCaptor<MeterStepEntity> stepCaptor = ArgumentCaptor.forClass(MeterStepEntity.class);
        verify(accountMeterStepRepository, times(1)).insert(stepCaptor.capture());
        MeterStepEntity capturedStep = stepCaptor.getValue();
        assertEquals(1, capturedStep.getMeterId());
        assertEquals(100, capturedStep.getAccountId());
        // 验证step起始值与历史偏移
        assertEquals(new BigDecimal("500"), capturedStep.getStepStartValue());
        assertEquals(BigDecimal.ZERO, capturedStep.getHistoryPowerOffset());
        assertEquals(Boolean.TRUE, capturedStep.getIsLatest());
        verify(accountMeterStepRepository, times(1)).clearLatestFlag(any(AccountMeterStepQo.class));
    }


    @Test
    void testAdd_Success_WithSetMeterCtCommand() {
        // 准备数据 - 确保CT为null以触发setElectricCt调用
        List<ElectricPriceTimeDto> timeList = List.of(new ElectricPriceTimeDto().setStart(LocalTime.of(0, 0)).setType(ElectricPricePeriodEnum.HIGHER));

        // 设置entity的CT为null，这样会触发setElectricCt方法
        entity.setCt(null);
        saveDto.setCt(null);

        ElectricMeterQueryDto queryDto = new ElectricMeterQueryDto()
                .setGatewayId(300)
                .setPortNo(1)
                .setMeterAddress(1);

        // Mock行为
        when(electricMeterInfoService.getDetail(1)).thenReturn(bo);
        when(mapper.saveDtoToEntity(saveDto)).thenReturn(entity);
        when(deviceModelService.getDetail(200)).thenReturn(deviceModelBo);
        when(spaceService.getDetail(100)).thenReturn(spaceBo);
        when(gatewayService.getDetail(300)).thenReturn(gatewayBo);
        when(electricMeterInfoService.findList(queryDto)).thenReturn(Collections.emptyList());
        when(repository.updateById(any(ElectricMeterEntity.class))).thenReturn(1);
        when(deviceModuleContext.getService(eq(EnergyService.class), eq(1000))).thenReturn(energyService);
        when(energyService.addDevice(any(ElectricDeviceAddDto.class))).thenReturn(12345);
        when(electricPlanService.getElectricTime()).thenReturn(timeList);
        when(requestContext.getUserId()).thenReturn(1001);
        when(requestContext.getUserRealName()).thenReturn("测试用户");

        // 执行测试
        Integer result = electricMeterService.add(saveDto);

        // 验证结果
        assertNotNull(result);
        verify(repository).insert(entity);
        verify(repository, times(2)).updateById(any(ElectricMeterEntity.class));
        ArgumentCaptor<ElectricDeviceAddDto> addDeviceCaptor = ArgumentCaptor.forClass(ElectricDeviceAddDto.class);
        verify(energyService).addDevice(addDeviceCaptor.capture());
        ElectricDeviceAddDto capturedAddDevice = addDeviceCaptor.getValue();
        assertNotNull(capturedAddDevice.getProductCode());
        assertNotNull(capturedAddDevice.getDeviceNo());

        // 验证setElectricCt被调用 - 通过验证saveMeterCommandAndRun被调用
        ArgumentCaptor<DeviceCommandAddDto> commandCaptor = ArgumentCaptor.forClass(DeviceCommandAddDto.class);
        verify(deviceCommandService, times(2)).saveDeviceCommand(commandCaptor.capture());
        List<DeviceCommandAddDto> capturedCommands = commandCaptor.getAllValues();
        assertEquals(2, capturedCommands.size());
        assertEquals("12345", capturedCommands.get(0).getDeviceIotId());
        assertEquals("12345", capturedCommands.get(1).getDeviceIotId());
    }

    @Test
    void testAdd_Success_WithCtAlreadySet() {
        // 准备数据 - CT已设置，不应触发setElectricCt调用
        List<ElectricPriceTimeDto> timeList = List.of(new ElectricPriceTimeDto().setStart(LocalTime.of(0, 0)).setType(ElectricPricePeriodEnum.HIGHER));

        // 设置entity的CT不为null，这样不会触发setElectricCt方法
        entity.setCt(100);
        saveDto.setCt(100);

        ElectricMeterQueryDto query = new ElectricMeterQueryDto().setPortNo(1).setGatewayId(300).setMeterAddress(1);

        // Mock行为
        when(electricMeterInfoService.getDetail(1)).thenReturn(bo);
        when(mapper.saveDtoToEntity(saveDto)).thenReturn(entity);
        when(deviceModelService.getDetail(200)).thenReturn(deviceModelBo);
        when(spaceService.getDetail(100)).thenReturn(spaceBo);
        when(gatewayService.getDetail(300)).thenReturn(gatewayBo);
        when(electricMeterInfoService.findList(query)).thenReturn(Collections.emptyList());
        when(repository.updateById(any(ElectricMeterEntity.class))).thenReturn(1);
        when(deviceModuleContext.getService(eq(EnergyService.class), eq(1000))).thenReturn(energyService);
        when(energyService.addDevice(any(ElectricDeviceAddDto.class))).thenReturn(12345);
        when(electricPlanService.getElectricTime()).thenReturn(timeList);
        when(requestContext.getUserId()).thenReturn(1001);
        when(requestContext.getUserRealName()).thenReturn("测试用户");

        // 执行测试
        Integer result = electricMeterService.add(saveDto);

        // 验证结果
        assertNotNull(result);
        verify(repository).insert(entity);
        verify(repository, times(2)).updateById(any(ElectricMeterEntity.class));
        ArgumentCaptor<ElectricDeviceAddDto> addDeviceCaptor2 = ArgumentCaptor.forClass(ElectricDeviceAddDto.class);
        verify(energyService).addDevice(addDeviceCaptor2.capture());
        ElectricDeviceAddDto capturedAddDevice2 = addDeviceCaptor2.getValue();
        assertNotNull(capturedAddDevice2.getProductCode());
        assertNotNull(capturedAddDevice2.getDeviceNo());
    }

    @Test
    void testSetCt_Success_WithSetMeterCtCommand() {
        // 准备测试数据
        ElectricMeterCtDto dto = new ElectricMeterCtDto();
        dto.setMeterId(1);
        dto.setCt(100);

        ElectricMeterBo existingBo = new ElectricMeterBo();
        existingBo.setId(1)
                .setModelId(1000)
                .setCt(50)
                .setAccountId(null) // 未开户
                .setIotId(123)
                .setOwnAreaId(1000);

        DeviceModelBo deviceModel = new DeviceModelBo()
                .setId(1000)
                .setProductCode("meter-model-1000-sync")
                .setModelProperty(Map.of("isCt", true));

        // Mock行为
        when(electricMeterInfoService.getDetail(1)).thenReturn(existingBo);
        when(mapper.boToEntity(existingBo)).thenReturn(new ElectricMeterEntity().setId(1).setModelId(1000).setIotId(123).setCt(100));
        when(deviceModelService.getDetail(1000)).thenReturn(deviceModel);
        int newMeterId = 2;
        when(repository.insert(any(ElectricMeterEntity.class))).thenAnswer(invocation -> {
            ElectricMeterEntity entity = invocation.getArgument(0);
            entity.setId(newMeterId);
            return 1;
        });
        ElectricMeterBo newBo = new ElectricMeterBo()
                .setId(newMeterId)
                .setModelId(1000)
                .setIotId(123)
                .setCt(dto.getCt())
                .setOwnAreaId(1000);
        when(electricMeterInfoService.getDetail(newMeterId)).thenReturn(newBo);

        // 执行测试
        assertDoesNotThrow(() -> electricMeterService.setMeterCt(dto));

        // 验证调用了删除和重建操作
        verify(repository).deleteById(1);

        // 验证新实体被插入，CT已更新
        ArgumentCaptor<ElectricMeterEntity> entityCaptor = ArgumentCaptor.forClass(ElectricMeterEntity.class);
        verify(repository).insert(entityCaptor.capture());
        ElectricMeterEntity insertedEntity = entityCaptor.getValue();
        assertEquals(dto.getCt(), insertedEntity.getCt());
        assertEquals(newMeterId, insertedEntity.getId());

        // 验证setElectricCt被调用 - 通过验证saveMeterCommandAndRun被调用
        ArgumentCaptor<DeviceCommandAddDto> commandCaptor = ArgumentCaptor.forClass(DeviceCommandAddDto.class);
        verify(deviceCommandService).saveDeviceCommand(commandCaptor.capture());
        DeviceCommandAddDto capturedCommand = commandCaptor.getValue();
        assertEquals("123", capturedCommand.getDeviceIotId());
        assertEquals(CommandSourceEnum.SYSTEM, capturedCommand.getCommandSource());
        assertEquals(newMeterId, capturedCommand.getDeviceId());
    }

    /**
     * 测试同步电表在线状态 - 强制更新为在线
     */
    @Test
    void testSyncMeterOnlineStatus_ForceOnline_Success() {
        // Given
        ElectricMeterOnlineStatusDto dto = new ElectricMeterOnlineStatusDto()
                .setMeterId(1)
                .setOnlineStatus(true)
                .setForce(true);

        when(electricMeterInfoService.getDetail(1)).thenReturn(bo);

        // When
        electricMeterService.syncMeterOnlineStatus(dto);

        // Then
        ArgumentCaptor<ElectricMeterEntity> captor = ArgumentCaptor.forClass(ElectricMeterEntity.class);
        verify(repository).updateById(captor.capture());

        ElectricMeterEntity updatedEntity = captor.getValue();
        assertEquals(1, updatedEntity.getId());
        assertTrue(updatedEntity.getIsOnline());

        // 验证不会调用 EnergyService
        verify(deviceModuleContext, never()).getService(any(), any());
    }

    /**
     * 测试同步电表在线状态 - 强制更新为离线
     */
    @Test
    void testSyncMeterOnlineStatus_ForceOffline_Success() {
        // Given
        ElectricMeterOnlineStatusDto dto = new ElectricMeterOnlineStatusDto()
                .setMeterId(1)
                .setOnlineStatus(false)
                .setForce(true);

        when(electricMeterInfoService.getDetail(1)).thenReturn(bo);

        // When
        electricMeterService.syncMeterOnlineStatus(dto);

        // Then
        ArgumentCaptor<ElectricMeterEntity> captor = ArgumentCaptor.forClass(ElectricMeterEntity.class);
        verify(repository).updateById(captor.capture());

        ElectricMeterEntity updatedEntity = captor.getValue();
        assertEquals(1, updatedEntity.getId());
        assertFalse(updatedEntity.getIsOnline());

        // 验证不会调用 EnergyService
        verify(deviceModuleContext, never()).getService(any(), any());
    }

    /**
     * 测试同步电表在线状态 - 强制更新但未提供在线状态，应查询 EnergyService
     */
    @Test
    void testSyncMeterOnlineStatus_ForceWithoutStatus_QueryEnergyService() {
        // Given
        ElectricMeterOnlineStatusDto dto = new ElectricMeterOnlineStatusDto()
                .setMeterId(1)
                .setOnlineStatus(null)
                .setForce(true);

        BaseElectricDeviceDto deviceDto = new BaseElectricDeviceDto();
        deviceDto.setDeviceId(12345);
        deviceDto.setAreaId(1000);

        when(electricMeterInfoService.getDetail(1)).thenReturn(bo);
        when(deviceModuleContext.getService(EnergyService.class, 1000)).thenReturn(energyService);
        when(energyService.isOnline(deviceDto)).thenReturn(true);

        // When
        electricMeterService.syncMeterOnlineStatus(dto);

        // Then
        ArgumentCaptor<BaseElectricDeviceDto> deviceCaptor = ArgumentCaptor.forClass(BaseElectricDeviceDto.class);
        verify(energyService).isOnline(deviceCaptor.capture());

        BaseElectricDeviceDto capturedDeviceDto = deviceCaptor.getValue();
        assertEquals(12345, capturedDeviceDto.getDeviceId());
        assertEquals(1000, capturedDeviceDto.getAreaId());

        ArgumentCaptor<ElectricMeterEntity> entityCaptor = ArgumentCaptor.forClass(ElectricMeterEntity.class);
        verify(repository).updateById(entityCaptor.capture());

        ElectricMeterEntity updatedEntity = entityCaptor.getValue();
        assertEquals(1, updatedEntity.getId());
        assertTrue(updatedEntity.getIsOnline());
    }

    /**
     * 测试同步电表在线状态 - 非强制更新，查询 EnergyService
     */
    @Test
    void testSyncMeterOnlineStatus_NotForce_QueryEnergyService() {
        // Given
        ElectricMeterOnlineStatusDto dto = new ElectricMeterOnlineStatusDto()
                .setMeterId(1)
                .setOnlineStatus(true) // 这个值会被忽略
                .setForce(false);

        BaseElectricDeviceDto deviceDto = new BaseElectricDeviceDto();
        deviceDto.setDeviceId(12345);
        deviceDto.setAreaId(1000);

        when(electricMeterInfoService.getDetail(1)).thenReturn(bo);
        when(deviceModuleContext.getService(EnergyService.class, 1000)).thenReturn(energyService);
        when(energyService.isOnline(deviceDto)).thenReturn(false);

        // When
        electricMeterService.syncMeterOnlineStatus(dto);

        // Then
        ArgumentCaptor<BaseElectricDeviceDto> deviceCaptor = ArgumentCaptor.forClass(BaseElectricDeviceDto.class);
        verify(energyService).isOnline(deviceCaptor.capture());

        BaseElectricDeviceDto capturedDeviceDto2 = deviceCaptor.getValue();
        assertEquals(12345, capturedDeviceDto2.getDeviceId());
        assertEquals(1000, capturedDeviceDto2.getAreaId());

        ArgumentCaptor<ElectricMeterEntity> entityCaptor = ArgumentCaptor.forClass(ElectricMeterEntity.class);
        verify(repository).updateById(entityCaptor.capture());

        ElectricMeterEntity updatedEntity = entityCaptor.getValue();
        assertEquals(1, updatedEntity.getId());
        assertFalse(updatedEntity.getIsOnline()); // 使用 EnergyService 返回的状态
    }

    /**
     * 测试同步电表在线状态 - force 为 null，查询 EnergyService
     */
    @Test
    void testSyncMeterOnlineStatus_ForceNull_QueryEnergyService() {
        // Given
        ElectricMeterOnlineStatusDto dto = new ElectricMeterOnlineStatusDto()
                .setMeterId(1)
                .setOnlineStatus(true)
                .setForce(null);

        BaseElectricDeviceDto deviceDto = new BaseElectricDeviceDto();
        deviceDto.setDeviceId(12345);
        deviceDto.setAreaId(1000);

        when(electricMeterInfoService.getDetail(1)).thenReturn(bo);
        when(deviceModuleContext.getService(EnergyService.class, 1000)).thenReturn(energyService);
        when(energyService.isOnline(deviceDto)).thenReturn(true);

        // When
        electricMeterService.syncMeterOnlineStatus(dto);

        // Then
        verify(energyService).isOnline(deviceDto);

        ArgumentCaptor<ElectricMeterEntity> entityCaptor = ArgumentCaptor.forClass(ElectricMeterEntity.class);
        verify(repository).updateById(entityCaptor.capture());

        ElectricMeterEntity updatedEntity = entityCaptor.getValue();
        assertEquals(1, updatedEntity.getId());
        assertTrue(updatedEntity.getIsOnline());
    }

    /**
     * 测试同步电表在线状态 - 电表不存在
     */
    @Test
    void testSyncMeterOnlineStatus_MeterNotFound() {
        // Given
        ElectricMeterOnlineStatusDto dto = new ElectricMeterOnlineStatusDto()
                .setMeterId(999)
                .setOnlineStatus(true)
                .setForce(true);

        when(electricMeterInfoService.getDetail(999)).thenThrow(new NotFoundException("电表不存在"));

        // When & Then
        assertThrows(NotFoundException.class, () -> electricMeterService.syncMeterOnlineStatus(dto));

        verify(repository, never()).updateById(any(ElectricMeterEntity.class));
    }

    // NB模式相关参数校验测试用例
    @Test
    void testAdd_NbModeImeiNull() {
        ElectricMeterCreateDto dto = new ElectricMeterCreateDto()
                .setSpaceId(1)
                .setMeterName("测试电表")
                .setModelId(1)
                .setImei(null)
                .setMeterAddress(1);

        ElectricMeterEntity entity = new ElectricMeterEntity()
                .setSpaceId(1)
                .setMeterName("测试电表")
                .setModelId(1)
                .setImei(null)
                .setMeterAddress(1);
        entity.setOwnAreaId(1);

        // Mock mapper转换
        when(mapper.saveDtoToEntity(dto)).thenReturn(entity);

        // Mock设备型号为NB模式
        Map<String, Object> modelProperty = Map.of("communicateModel", "NB");
        DeviceModelBo deviceModel = new DeviceModelBo()
                .setId(1)
                .setManufacturerName("厂商")
                .setProductCode("meter-model-1")
                .setTypeKey("electricMeter")
                .setModelProperty(modelProperty);
        when(deviceModelService.getDetail(1)).thenReturn(deviceModel);

        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> electricMeterService.add(dto));

        assertEquals("NB模式电表IMEI不能为空", exception.getMessage());
        verify(repository, never()).insert(any(ElectricMeterEntity.class));
    }

    @Test
    void testAdd_NbModeImeiEmpty() {
        ElectricMeterCreateDto dto = new ElectricMeterCreateDto()
                .setSpaceId(1)
                .setMeterName("测试电表")
                .setModelId(1)
                .setImei("")
                .setMeterAddress(1);

        ElectricMeterEntity entity = new ElectricMeterEntity()
                .setSpaceId(1)
                .setMeterName("测试电表")
                .setModelId(1)
                .setImei("")
                .setMeterAddress(1);
        entity.setOwnAreaId(1);

        // Mock mapper转换
        when(mapper.saveDtoToEntity(dto)).thenReturn(entity);

        // Mock设备型号为NB模式
        Map<String, Object> modelProperty = Map.of("communicateModel", "NB");
        DeviceModelBo deviceModel = new DeviceModelBo()
                .setId(1)
                .setManufacturerName("厂商")
                .setProductCode("meter-model-1-repeat")
                .setTypeKey("electricMeter")
                .setModelProperty(modelProperty);
        when(deviceModelService.getDetail(1)).thenReturn(deviceModel);

        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> electricMeterService.add(dto));

        assertEquals("NB模式电表IMEI不能为空", exception.getMessage());
        verify(repository, never()).insert(any(ElectricMeterEntity.class));
    }

    @Test
    void testAdd_NbModeMeterAddressNull() {
        ElectricMeterCreateDto dto = new ElectricMeterCreateDto()
                .setSpaceId(1)
                .setMeterName("测试电表")
                .setModelId(1)
                .setImei("123456789012345")
                .setDeviceNo("NB-DEVICE-003")
                .setMeterAddress(1); // 设置有效的电表地址，让代码能执行到repository.insert

        ElectricMeterEntity entity = new ElectricMeterEntity()
                .setSpaceId(1)
                .setMeterName("测试电表")
                .setModelId(1)
                .setImei("123456789012345")
                .setDeviceNo("NB-DEVICE-003")
                .setMeterAddress(1); // 设置有效的电表地址
        entity.setOwnAreaId(1);

        // Mock mapper转换
        when(mapper.saveDtoToEntity(dto)).thenReturn(entity);

        // Mock设备型号为NB模式
        Map<String, Object> modelProperty = Map.of("communicateModel", "NB", "isCt", false, "isPrepay", false);
        DeviceModelBo deviceModel = new DeviceModelBo()
                .setId(1)
                .setManufacturerName("厂商")
                .setProductCode("meter-model-1-valid")
                .setTypeKey("electricMeter")
                .setModelProperty(modelProperty);
        when(deviceModelService.getDetail(1)).thenReturn(deviceModel);

        // Mock空间信息
        SpaceBo space = new SpaceBo().setOwnAreaId(1).setType(SpaceTypeEnum.ROOM);
        when(spaceService.getDetail(1)).thenReturn(space);

        ElectricMeterQueryDto query = new ElectricMeterQueryDto().setImei("123456789012345");
        when(electricMeterInfoService.findList(query)).thenReturn(Collections.emptyList());

        // Mock repository.insert行为，设置entity的id
        doAnswer(invocation -> {
            ElectricMeterEntity insertEntity = invocation.getArgument(0);
            insertEntity.setId(100); // 模拟数据库自动生成的ID
            return null;
        }).when(repository).insert(entity);

        // Mock repository.updateById返回成功
        when(repository.updateById(any(ElectricMeterEntity.class))).thenReturn(1);

        ElectricMeterBo boSelected = new ElectricMeterBo()
                .setId(100)
                .setSpaceId(1)
                .setMeterName("测试电表")
                .setModelId(1)
                .setIotId(123)
                .setImei("123456789012345")
                .setMeterAddress(1);
        when(electricMeterInfoService.getDetail(100)).thenReturn(boSelected);

        // Mock其他必要的服务调用
        when(deviceModuleContext.getService(eq(EnergyService.class), eq(1))).thenReturn(energyService);
        when(energyService.addDevice(any())).thenReturn(12345);
        List<ElectricPriceTimeDto> timeList = List.of(new ElectricPriceTimeDto().setStart(LocalTime.of(0, 0)).setType(ElectricPricePeriodEnum.HIGHER));
        when(electricPlanService.getElectricTime()).thenReturn(timeList);
        when(requestContext.getUserId()).thenReturn(1001);
        when(requestContext.getUserRealName()).thenReturn("测试用户");

        // 执行测试
        Integer result = electricMeterService.add(dto);

        // 验证结果
        assertNotNull(result);
        assertEquals(100, result);

        // 验证repository.insert被调用
        ArgumentCaptor<ElectricMeterEntity> insertEntityCaptor = ArgumentCaptor.forClass(ElectricMeterEntity.class);
        verify(repository).insert(insertEntityCaptor.capture());
        ElectricMeterEntity insertedEntity = insertEntityCaptor.getValue();
        assertEquals(1, insertedEntity.getSpaceId());
        assertEquals("测试电表", insertedEntity.getMeterName());
        assertEquals(1, insertedEntity.getModelId());

        // 验证repository.updateById被调用，并校验实际参数
        ArgumentCaptor<ElectricMeterEntity> updateEntityCaptor = ArgumentCaptor.forClass(ElectricMeterEntity.class);
        verify(repository, times(2)).updateById(updateEntityCaptor.capture());

        List<ElectricMeterEntity> updateEntities = updateEntityCaptor.getAllValues();
        // 第一次updateById是设置电表编号
        ElectricMeterEntity firstUpdateEntity = updateEntities.get(0);
        assertEquals(100, firstUpdateEntity.getId());
        assertNotNull(firstUpdateEntity.getMeterNo());

        // 第二次updateById是设置iotId
        ElectricMeterEntity secondUpdateEntity = updateEntities.get(1);
        assertEquals(100, secondUpdateEntity.getId());
        assertEquals(12345, secondUpdateEntity.getIotId());
    }

    @Test
    void testAdd_NbModeDuplicateInfo() {
        ElectricMeterCreateDto dto = new ElectricMeterCreateDto()
                .setSpaceId(1)
                .setMeterName("测试电表")
                .setModelId(1)
                .setImei("123456789012345")
                .setDeviceNo("NB-DEVICE-004")
                .setMeterAddress(1);

        ElectricMeterEntity entity = new ElectricMeterEntity()
                .setSpaceId(1)
                .setMeterName("测试电表")
                .setModelId(1)
                .setImei("123456789012345")
                .setDeviceNo("NB-DEVICE-004")
                .setMeterAddress(1);
        entity.setOwnAreaId(1);

        // Mock mapper转换
        when(mapper.saveDtoToEntity(dto)).thenReturn(entity);

        // Mock设备型号为NB模式
        Map<String, Object> modelProperty = Map.of("communicateModel", "NB");

        DeviceModelBo deviceModel = new DeviceModelBo()
                .setId(1)
                .setManufacturerName("厂商")
                .setModelName("型号")
                .setProductCode("meter-model-1-duplicate")
                .setTypeKey("electricMeter")
                .setModelProperty(modelProperty);
        when(deviceModelService.getDetail(1)).thenReturn(deviceModel);

        // Mock重复的电表信息
        List<ElectricMeterBo> duplicateList = List.of(new ElectricMeterBo().setId(2));
        ElectricMeterQueryDto queryDto = new ElectricMeterQueryDto().setImei("123456789012345");
        when(electricMeterInfoService.findList(queryDto)).thenReturn(duplicateList);

        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> electricMeterService.add(dto));

        assertEquals("电表信息重复", exception.getMessage());
        verify(repository, never()).insert(any(ElectricMeterEntity.class));
    }

    // 非NB模式相关参数校验测试用例
    @Test
    void testAdd_NonNbModeGatewayIdNull() {
        ElectricMeterCreateDto dto = new ElectricMeterCreateDto()
                .setSpaceId(1)
                .setMeterName("测试电表")
                .setModelId(1)
                .setGatewayId(null)
                .setPortNo(1)
                .setMeterAddress(1);

        ElectricMeterEntity entity = new ElectricMeterEntity()
                .setSpaceId(1)
                .setMeterName("测试电表")
                .setModelId(1)
                .setGatewayId(null)
                .setPortNo(1)
                .setMeterAddress(1);
        entity.setOwnAreaId(1);

        // Mock mapper转换
        when(mapper.saveDtoToEntity(dto)).thenReturn(entity);

        // Mock设备型号为非NB模式
        Map<String, Object> modelProperty = Map.of("communicateMode", "485");
        DeviceModelBo deviceModel = new DeviceModelBo()
                .setId(1)
                .setManufacturerName("厂商")
                .setModelName("型号")
                .setProductCode("meter-model-1-port")
                .setTypeKey("electricMeter")
                .setModelProperty(modelProperty);
        when(deviceModelService.getDetail(1)).thenReturn(deviceModel);

        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> electricMeterService.add(dto));

        assertEquals("非NB模式电表必须绑定网关", exception.getMessage());
        verify(repository, never()).insert(any(ElectricMeterEntity.class));
    }

    @Test
    void testAdd_NonNbModePortNoNull() {
        ElectricMeterCreateDto dto = new ElectricMeterCreateDto()
                .setSpaceId(1)
                .setMeterName("测试电表")
                .setModelId(1)
                .setGatewayId(1)
                .setPortNo(null)
                .setMeterAddress(1);

        ElectricMeterEntity entity = new ElectricMeterEntity()
                .setSpaceId(1)
                .setMeterName("测试电表")
                .setModelId(1)
                .setGatewayId(1)
                .setPortNo(null)
                .setMeterAddress(1);
        entity.setOwnAreaId(1);

        // Mock mapper转换
        when(mapper.saveDtoToEntity(dto)).thenReturn(entity);

        // Mock设备型号为非NB模式
        Map<String, Object> modelProperty = Map.of("communicateMode", "485");
        DeviceModelBo deviceModel = new DeviceModelBo()
                .setId(1)
                .setManufacturerName("厂商")
                .setModelName("型号")
                .setProductCode("meter-model-1-port-null")
                .setTypeKey("electricMeter")
                .setModelProperty(modelProperty);
        when(deviceModelService.getDetail(1)).thenReturn(deviceModel);

        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> electricMeterService.add(dto));

        assertEquals("非NB模式电表串口号不能为空", exception.getMessage());
        verify(repository, never()).insert(any(ElectricMeterEntity.class));
    }

    @Test
    void testAdd_NonNbModeGatewayNotFound() {
        ElectricMeterCreateDto dto = new ElectricMeterCreateDto()
                .setSpaceId(1)
                .setMeterName("测试电表")
                .setModelId(1)
                .setGatewayId(1)
                .setPortNo(1)
                .setMeterAddress(1);

        ElectricMeterEntity entity = new ElectricMeterEntity()
                .setSpaceId(1)
                .setMeterName("测试电表")
                .setModelId(1)
                .setGatewayId(1)
                .setPortNo(1)
                .setMeterAddress(1);
        entity.setOwnAreaId(1);

        // Mock mapper转换
        when(mapper.saveDtoToEntity(dto)).thenReturn(entity);

        // Mock设备型号为非NB模式
        Map<String, Object> modelProperty = Map.of("communicateMode", "485");
        DeviceModelBo deviceModel = new DeviceModelBo()
                .setId(1)
                .setManufacturerName("厂商")
                .setProductCode("meter-model-1-gateway-missing")
                .setTypeKey("electricMeter")
                .setModelProperty(modelProperty);
        when(deviceModelService.getDetail(1)).thenReturn(deviceModel);

        // Mock网关不存在
        when(gatewayService.getDetail(1)).thenReturn(null);

        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> electricMeterService.add(dto));

        assertEquals("网关信息有误，请重新选择", exception.getMessage());
        verify(repository, never()).insert(any(ElectricMeterEntity.class));
    }

    @Test
    void testAdd_NonNbModeGatewayOffline() {
        ElectricMeterCreateDto dto = new ElectricMeterCreateDto()
                .setSpaceId(1)
                .setMeterName("测试电表")
                .setModelId(1)
                .setGatewayId(1)
                .setPortNo(1)
                .setMeterAddress(1);

        // Mock mapper转换
        ElectricMeterEntity entity = new ElectricMeterEntity();
        entity.setSpaceId(1)
                .setMeterName("测试电表")
                .setModelId(1)
                .setGatewayId(1)
                .setPortNo(1)
                .setMeterAddress(1);
        entity.setOwnAreaId(1);
        when(mapper.saveDtoToEntity(dto)).thenReturn(entity);

        // Mock设备型号为非NB模式
        Map<String, Object> modelProperty = Map.of("communicateMode", "485");
        DeviceModelBo deviceModel = new DeviceModelBo()
                .setId(1)
                .setManufacturerName("厂商")
                .setProductCode("meter-model-1-gateway-offline")
                .setTypeKey("electricMeter")
                .setModelProperty(modelProperty);
        when(deviceModelService.getDetail(1)).thenReturn(deviceModel);

        // Mock网关离线
        GatewayBo gateway = new GatewayBo().setId(1).setIotId(123).setIsOnline(false);
        when(gatewayService.getDetail(1)).thenReturn(gateway);

        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> electricMeterService.add(dto));

        assertEquals("网关不在线，请重试", exception.getMessage());
        verify(repository, never()).insert(any(ElectricMeterEntity.class));
    }

    @Test
    void testAdd_NonNbModeDuplicateInfo() {
        ElectricMeterCreateDto dto = new ElectricMeterCreateDto()
                .setSpaceId(1)
                .setMeterName("测试电表")
                .setModelId(1)
                .setGatewayId(1)
                .setPortNo(1)
                .setMeterAddress(1);

        ElectricMeterEntity entity = new ElectricMeterEntity()
                .setSpaceId(1)
                .setMeterName("测试电表")
                .setModelId(1)
                .setGatewayId(1)
                .setPortNo(1)
                .setMeterAddress(1);
        entity.setOwnAreaId(1);

        // Mock mapper转换
        when(mapper.saveDtoToEntity(dto)).thenReturn(entity);

        // Mock设备型号为非NB模式
        Map<String, Object> modelProperty = Map.of("communicateMode", "485");
        DeviceModelBo deviceModel = new DeviceModelBo()
                .setId(1)
                .setManufacturerName("厂商")
                .setProductCode("meter-model-1-plan")
                .setTypeKey("electricMeter")
                .setModelProperty(modelProperty);
        when(deviceModelService.getDetail(1)).thenReturn(deviceModel);

        // Mock网关在线
        GatewayBo gateway = new GatewayBo().setId(1).setIotId(123).setIsOnline(true).setDeviceNo("GW-DEVICE-002");
        when(gatewayService.getDetail(1)).thenReturn(gateway);

        // Mock重复的电表信息
        List<ElectricMeterBo> duplicateList = List.of(new ElectricMeterBo().setId(2));
        ElectricMeterQueryDto query = new ElectricMeterQueryDto()
                .setGatewayId(1)
                .setPortNo(1)
                .setMeterAddress(1);
        when(electricMeterInfoService.findList(query)).thenReturn(duplicateList);

        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> electricMeterService.add(dto));

        assertEquals("电表信息重复", exception.getMessage());
        verify(repository, never()).insert(any(ElectricMeterEntity.class));
    }

    @Test
    void testAdd_PrepayNotSupportedByModel() {
        ElectricMeterCreateDto dto = new ElectricMeterCreateDto()
                .setSpaceId(1)
                .setMeterName("测试电表")
                .setModelId(1)
                .setIsPrepay(true);

        ElectricMeterEntity entity = new ElectricMeterEntity()
                .setSpaceId(1)
                .setMeterName("测试电表")
                .setModelId(1)
                .setIsPrepay(true);
        entity.setOwnAreaId(1);

        // Mock mapper转换
        when(mapper.saveDtoToEntity(dto)).thenReturn(entity);

        // Mock设备型号不支持预付费
        Map<String, Object> modelProperty = Map.of("isPrepay", false);
        DeviceModelBo deviceModel = new DeviceModelBo()
                .setId(1)
                .setManufacturerName("厂商")
                .setProductCode("meter-model-1-prepay")
                .setTypeKey("electricMeter")
                .setModelProperty(modelProperty);
        when(deviceModelService.getDetail(1)).thenReturn(deviceModel);

        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> electricMeterService.add(dto));

        assertEquals("当前电表型号不支持预付费", exception.getMessage());
        verify(repository, never()).insert(any(ElectricMeterEntity.class));
    }

    @Test
    void testAdd_PrepayModeNotInRoomSpace() {
        ElectricMeterCreateDto dto = new ElectricMeterCreateDto()
                .setSpaceId(1)
                .setMeterName("测试电表")
                .setModelId(1)
                .setImei("abc")
                .setDeviceNo("NB-DEVICE-005")
                .setMeterAddress(111)
                .setIsPrepay(true);

        ElectricMeterEntity entity = new ElectricMeterEntity()
                .setSpaceId(1)
                .setMeterName("测试电表")
                .setModelId(1)
                .setImei("abc")
                .setDeviceNo("NB-DEVICE-005")
                .setIsPrepay(true);
        entity.setOwnAreaId(1);

        // Mock mapper转换
        when(mapper.saveDtoToEntity(dto)).thenReturn(entity);

        // Mock设备型号支持预付费
        Map<String, Object> modelProperty = Map.of("isPrepay", true, "communicateModel", "NB");
        DeviceModelBo deviceModel = new DeviceModelBo()
                .setId(1)
                .setManufacturerName("厂商")
                .setProductCode("meter-model-1-prepay-room")
                .setTypeKey("electricMeter")
                .setModelProperty(modelProperty);
        when(deviceModelService.getDetail(1)).thenReturn(deviceModel);

        // Mock空间类型不是房间
        SpaceBo space = new SpaceBo().setOwnAreaId(1).setType(SpaceTypeEnum.INNER_SPACE);
        when(spaceService.getDetail(1)).thenReturn(space);

        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> electricMeterService.add(dto));

        assertEquals("预付费模式下电表只允许绑定到房间", exception.getMessage());
        verify(repository, never()).insert(any(ElectricMeterEntity.class));
    }

    @Test
    void testUpdate_ModifyPrepayForOpenedAccount() {
        ElectricMeterUpdateDto dto = new ElectricMeterUpdateDto()
                .setId(1)
                .setIsPrepay(true);

        // Mock已开户的电表
        ElectricMeterBo existing = new ElectricMeterBo()
                .setId(1)
                .setAccountId(123)
                .setIsPrepay(false)
                .setSpaceId(100);
        when(electricMeterInfoService.getDetail(1)).thenReturn(existing);

        // Mock mapper转换
        ElectricMeterEntity entity = new ElectricMeterEntity()
                .setId(1)
                .setIsPrepay(true);
        entity.setOwnAreaId(1);
        when(mapper.updateDtoToEntity(dto)).thenReturn(entity);

        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> electricMeterService.update(dto));

        assertEquals("已开户电表不允许修改预付费属性", exception.getMessage());
        verify(repository, never()).updateById(any(ElectricMeterEntity.class));
    }

    /**
     * 测试setMeterCt方法参数校验 - CT变比为负数
     */
    @Test
    void testSetMeterCt_CtNegative() {
        // Given
        ElectricMeterCtDto dto = new ElectricMeterCtDto();
        dto.setMeterId(1);
        dto.setCt(-1);

        // When & Then
        assertThrows(Exception.class,
                () -> electricMeterService.setMeterCt(dto));

        verify(repository, never()).deleteById(anyInt());
        verify(repository, never()).insert(any(ElectricMeterEntity.class));
    }

    /**
     * 测试setMeterCt方法参数校验 - CT变比为零
     */
    @Test
    void testSetMeterCt_CtZero() {
        // Given
        ElectricMeterCtDto dto = new ElectricMeterCtDto();
        dto.setMeterId(1);
        dto.setCt(0);

        // When & Then
        assertThrows(Exception.class,
                () -> electricMeterService.setMeterCt(dto));

        verify(repository, never()).deleteById(anyInt());
        verify(repository, never()).insert(any(ElectricMeterEntity.class));
    }

    /**
     * 测试setMeterCt方法参数校验 - meterId为null
     */
    @Test
    void testSetMeterCt_MeterIdNull() {
        // Given
        ElectricMeterCtDto dto = new ElectricMeterCtDto();
        dto.setMeterId(null);
        dto.setCt(100);

        // When & Then
        assertThrows(Exception.class,
                () -> electricMeterService.setMeterCt(dto));

        verify(repository, never()).selectById(anyInt());
        verify(repository, never()).deleteById(anyInt());
        verify(repository, never()).insert(any(ElectricMeterEntity.class));
    }

    @Test
    void testGetMeterPower_Success() {
        // 准备数据
        Integer meterId = 1;
        ElectricPricePeriodEnum type = ElectricPricePeriodEnum.TOTAL;
        BigDecimal expectedPower = BigDecimal.valueOf(100.50);

        // Mock电表信息
        ElectricMeterBo meterBo = new ElectricMeterBo()
                .setId(meterId)
                .setIotId(12345)
                .setOwnAreaId(1000)
                .setMeterName("测试电表");

        // Mock行为
        when(electricMeterInfoService.getDetail(meterId)).thenReturn(bo);
        when(deviceModuleContext.getService(EnergyService.class, 1000)).thenReturn(energyService);
        when(energyService.getMeterEnergy(any(ElectricDeviceDegreeDto.class))).thenReturn(expectedPower);

        // 执行测试
        Map<ElectricPricePeriodEnum, BigDecimal> result = electricMeterService.getMeterPower(meterId, List.of(type));

        // 验证结果
        assertNotNull(result);
        assertEquals(expectedPower, result.get(type));

        // 验证方法调用
        verify(deviceModuleContext).getService(EnergyService.class, 1000);

        // 验证ElectricDeviceDegreeDto参数
        ArgumentCaptor<ElectricDeviceDegreeDto> dtoCaptor = ArgumentCaptor.forClass(ElectricDeviceDegreeDto.class);
        verify(energyService).getMeterEnergy(dtoCaptor.capture());
        ElectricDeviceDegreeDto capturedDto = dtoCaptor.getValue();
        assertEquals(type, capturedDto.getType());
        assertEquals(meterBo.getIotId(), capturedDto.getDeviceId());
        assertEquals(meterBo.getOwnAreaId(), capturedDto.getAreaId());
    }

    @Test
    void testGetMeterPower_MeterNotFound() {
        // 准备数据
        Integer meterId = 999;
        ElectricPricePeriodEnum type = ElectricPricePeriodEnum.HIGH;

        // Mock行为 - 电表不存在
        when(electricMeterInfoService.getDetail(meterId)).thenThrow(new NotFoundException("电表数据不存在或已被删除"));

        // 执行测试并验证异常
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> electricMeterService.getMeterPower(meterId, List.of(type)));

        assertEquals("电表数据不存在或已被删除", exception.getMessage());

        // 验证方法调用
        verify(deviceModuleContext, never()).getService(any(), any());
        verify(energyService, never()).getMeterEnergy(any());
    }

    @Test
    void testGetMeterPower_NullMeterId() {
        // 准备数据
        ElectricPricePeriodEnum type = ElectricPricePeriodEnum.LOW;

        when(electricMeterInfoService.getDetail(null)).thenThrow(new NotFoundException("参数id不能为空"));

        // 执行测试并验证异常
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> electricMeterService.getMeterPower(null, List.of(type)));

        assertEquals("参数id不能为空", exception.getMessage());

        // 验证没有调用其他方法
        verify(repository, never()).selectById(any());
        verify(deviceModuleContext, never()).getService(any(), any());
        verify(energyService, never()).getMeterEnergy(any());
    }

    @Test
    void testGetMeterPower_DifferentElectricTypes() {
        // Given - 准备测试数据
        Integer meterId = 1;
        List<ElectricPricePeriodEnum> types = List.of(
                ElectricPricePeriodEnum.TOTAL,
                ElectricPricePeriodEnum.HIGHER,
                ElectricPricePeriodEnum.HIGH,
                ElectricPricePeriodEnum.LOW,
                ElectricPricePeriodEnum.LOWER,
                ElectricPricePeriodEnum.DEEP_LOW
        );

        ElectricMeterBo meterBo = new ElectricMeterBo()
                .setId(meterId)
                .setMeterNo("EM001")
                .setOwnAreaId(1000)
                .setIotId(12345);

        // Mock 不同类型返回不同的电量值
        BigDecimal totalPower = new BigDecimal("1000.50");
        BigDecimal higherPower = new BigDecimal("200.25");
        BigDecimal highPower = new BigDecimal("300.75");
        BigDecimal lowPower = new BigDecimal("250.00");
        BigDecimal lowerPower = new BigDecimal("150.30");
        BigDecimal deepLowPower = new BigDecimal("100.10");

        // Mock 行为
        when(electricMeterInfoService.getDetail(meterId)).thenReturn(meterBo);
        when(deviceModuleContext.getService(eq(EnergyService.class), eq(1000))).thenReturn(energyService);

        // Mock 每个类型的电量查询 - 使用 ElectricDeviceDegreeDto 参数匹配
        ElectricDeviceDegreeDto dto1 = new ElectricDeviceDegreeDto();
        dto1.setType(ElectricPricePeriodEnum.TOTAL).setDeviceId(meterBo.getIotId()).setAreaId(meterBo.getOwnAreaId());
        when(energyService.getMeterEnergy(dto1))
                .thenReturn(totalPower);

        ElectricDeviceDegreeDto dto2 = new ElectricDeviceDegreeDto();
        dto2.setType(ElectricPricePeriodEnum.HIGHER).setDeviceId(meterBo.getIotId()).setAreaId(meterBo.getOwnAreaId());
        when(energyService.getMeterEnergy(dto2))
                .thenReturn(higherPower);

        ElectricDeviceDegreeDto dto3 = new ElectricDeviceDegreeDto();
        dto3.setType(ElectricPricePeriodEnum.HIGH).setDeviceId(meterBo.getIotId()).setAreaId(meterBo.getOwnAreaId());
        when(energyService.getMeterEnergy(dto3))
                .thenReturn(highPower);

        ElectricDeviceDegreeDto dto4 = new ElectricDeviceDegreeDto();
        dto4.setType(ElectricPricePeriodEnum.LOW).setDeviceId(meterBo.getIotId()).setAreaId(meterBo.getOwnAreaId());
        when(energyService.getMeterEnergy(dto4))
                .thenReturn(lowPower);

        ElectricDeviceDegreeDto dto5 = new ElectricDeviceDegreeDto();
        dto5.setType(ElectricPricePeriodEnum.LOWER).setDeviceId(meterBo.getIotId()).setAreaId(meterBo.getOwnAreaId());
        when(energyService.getMeterEnergy(dto5))
                .thenReturn(lowerPower);

        ElectricDeviceDegreeDto dto6 = new ElectricDeviceDegreeDto();
        dto6.setType(ElectricPricePeriodEnum.DEEP_LOW).setDeviceId(meterBo.getIotId()).setAreaId(meterBo.getOwnAreaId());
        when(energyService.getMeterEnergy(ArgumentMatchers.argThat(dto ->
                dto.getDeviceId().equals(12345) && dto.getType() == ElectricPricePeriodEnum.DEEP_LOW)))
                .thenReturn(deepLowPower);

        // When - 执行测试方法
        Map<ElectricPricePeriodEnum, BigDecimal> result = electricMeterService.getMeterPower(meterId, types);

        // Then - 验证结果
        assertNotNull(result);
        assertEquals(6, result.size());

        // 验证每个类型都有对应的电量值
        assertEquals(totalPower, result.get(ElectricPricePeriodEnum.TOTAL));
        assertEquals(higherPower, result.get(ElectricPricePeriodEnum.HIGHER));
        assertEquals(highPower, result.get(ElectricPricePeriodEnum.HIGH));
        assertEquals(lowPower, result.get(ElectricPricePeriodEnum.LOW));
        assertEquals(lowerPower, result.get(ElectricPricePeriodEnum.LOWER));
        assertEquals(deepLowPower, result.get(ElectricPricePeriodEnum.DEEP_LOW));

        // 验证每个类型都调用了 energyService.getMeterEnergy 方法
        verify(energyService).getMeterEnergy(ArgumentMatchers.argThat(dto ->
                dto.getDeviceId().equals(12345) && dto.getType() == ElectricPricePeriodEnum.TOTAL));
        verify(energyService).getMeterEnergy(ArgumentMatchers.argThat(dto ->
                dto.getDeviceId().equals(12345) && dto.getType() == ElectricPricePeriodEnum.HIGHER));
        verify(energyService).getMeterEnergy(ArgumentMatchers.argThat(dto ->
                dto.getDeviceId().equals(12345) && dto.getType() == ElectricPricePeriodEnum.HIGH));
        verify(energyService).getMeterEnergy(ArgumentMatchers.argThat(dto ->
                dto.getDeviceId().equals(12345) && dto.getType() == ElectricPricePeriodEnum.LOW));
        verify(energyService).getMeterEnergy(ArgumentMatchers.argThat(dto ->
                dto.getDeviceId().equals(12345) && dto.getType() == ElectricPricePeriodEnum.LOWER));
        verify(energyService).getMeterEnergy(ArgumentMatchers.argThat(dto ->
                dto.getDeviceId().equals(12345) && dto.getType() == ElectricPricePeriodEnum.DEEP_LOW));

        // 验证总共调用了6次
        verify(energyService, times(6)).getMeterEnergy(any(ElectricDeviceDegreeDto.class));
    }

    @Test
    void testGetMeterPower_EnergyServiceReturnsNull() {
        // 准备数据
        Integer meterId = 1;
        ElectricPricePeriodEnum type = ElectricPricePeriodEnum.TOTAL;

        // Mock行为 - 能源服务返回null
        when(electricMeterInfoService.getDetail(meterId)).thenReturn(bo);
        when(deviceModuleContext.getService(EnergyService.class, 1000)).thenReturn(energyService);
        when(energyService.getMeterEnergy(any(ElectricDeviceDegreeDto.class))).thenReturn(null);

        // 执行测试
        Map<ElectricPricePeriodEnum, BigDecimal> result = electricMeterService.getMeterPower(meterId, List.of(type));

        // 验证结果
        assertNull(result.get(type));

        // 验证方法调用
        ArgumentCaptor<ElectricDeviceDegreeDto> degreeCaptor = ArgumentCaptor.forClass(ElectricDeviceDegreeDto.class);
        verify(energyService).getMeterEnergy(degreeCaptor.capture());
        ElectricDeviceDegreeDto capturedDegree = degreeCaptor.getValue();
        assertEquals(entity.getIotId(), capturedDegree.getDeviceId());
        assertEquals(type, capturedDegree.getType());
    }

    @Test
    void testGetMeterPower_ZeroPower() {
        // 准备数据
        Integer meterId = 1;
        ElectricPricePeriodEnum type = ElectricPricePeriodEnum.TOTAL;
        BigDecimal zeroPower = BigDecimal.ZERO;

        // Mock行为
        when(electricMeterInfoService.getDetail(meterId)).thenReturn(bo);
        when(deviceModuleContext.getService(EnergyService.class, 1000)).thenReturn(energyService);
        when(energyService.getMeterEnergy(any(ElectricDeviceDegreeDto.class))).thenReturn(zeroPower);

        // 执行测试
        Map<ElectricPricePeriodEnum, BigDecimal> result = electricMeterService.getMeterPower(meterId, List.of(type));

        // 验证结果
        assertNotNull(result.get(type));
        assertEquals(0, result.get(type).compareTo(BigDecimal.ZERO));
    }

    // ==================== closeMeterAccount 测试用例 ====================

    /**
     * 测试按量计费账户正常销户（在线电表）
     */
    @Test
    void testCancelMeterAccount_Success_QuantityAccountOnline() {
        // Given
        MeterCancelDto meterCancelDto = new MeterCancelDto()
                .setAccountId(1)
                .setOwnerId(100)
                .setOwnerType(OwnerTypeEnum.ENTERPRISE)
                .setOwnerName("测试企业")
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY)
                .setMeterCloseDetail(List.of(
                        new MeterCancelDetailDto()
                                .setMeterId(1)
                ));

        // Mock 电表信息
        ElectricMeterBo meterDetailDto = new ElectricMeterBo()
                .setId(1)
                .setMeterName("测试电表1")
                .setSpaceId(100)
                .setOwnAreaId(1000)
                .setIsOnline(true)
                .setMeterNo("EM202401010001")
                .setProductCode("测试型号")
                .setCt(1);

        // Mock 余额信息
        BalanceBo balanceBo = new BalanceBo()
                .setBalance(new BigDecimal("50.00"));

        // Mock 空间信息
        SpaceBo spaceBo = new SpaceBo()
                .setId(100)
                .setOwnAreaId(1000)
                .setName("测试空间")
                .setParentsIds(List.of(1, 2))
                .setParentsNames(List.of("父级1", "父级2"));

        // Mock 行为
        when(electricMeterInfoService.getDetail(1)).thenReturn(meterDetailDto);
        when(deviceModuleContext.getService(EnergyService.class, 1000)).thenReturn(energyService);
        when(energyService.getMeterEnergy(any(ElectricDeviceDegreeDto.class)))
                .thenReturn(new BigDecimal("20.10"))
                .thenReturn(new BigDecimal("30.20"))
                .thenReturn(new BigDecimal("25.15"))
                .thenReturn(new BigDecimal("15.05"))
                .thenReturn(new BigDecimal("10.00"));
        when(balanceService.query(any(BalanceQueryDto.class))).thenReturn(balanceBo);
        when(spaceService.getDetail(100)).thenReturn(spaceBo);
        when(meterCancelRecordRepository.insert(any(MeterCancelRecordEntity.class))).thenReturn(1);
        when(repository.resetMeterAccountInfo(any(ElectricMeterResetAccountQo.class))).thenReturn(1);

        // When
        List<MeterCancelResultDto> result = electricMeterService.cancelMeterAccount(meterCancelDto);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        MeterCancelResultDto closedBalance = result.get(0);
        assertEquals(1, closedBalance.getMeterId());
        assertEquals(new BigDecimal("50.00"), closedBalance.getBalance());

        // 验证方法调用
        verify(electricMeterInfoService, times(2)).getDetail(1);
        verify(spaceService).getDetail(100);

        // 验证 meterConsumeService.savePowerRecord 的所有入参
        verify(meterConsumeService).savePowerRecord(ArgumentMatchers.argThat(record -> {
            // 验证基本字段
            if (!record.getAccountId().equals(1)) return false;
            if (!record.getOwnerId().equals(100)) return false;
            if (!record.getOwnerType().equals(OwnerTypeEnum.ENTERPRISE)) return false;
            if (!record.getOwnerName().equals("测试企业")) return false;
            if (!record.getElectricAccountType().equals(ElectricAccountTypeEnum.QUANTITY)) return false;
            if (!record.getNeedConsume()) return false;

            // 验证电量数据
            if (!record.getPower().equals(new BigDecimal("100.50"))) return false;
            if (!record.getPowerHigher().equals(new BigDecimal("20.10"))) return false;
            if (!record.getPowerHigh().equals(new BigDecimal("30.20"))) return false;
            if (!record.getPowerLow().equals(new BigDecimal("25.15"))) return false;
            if (!record.getPowerLower().equals(new BigDecimal("15.05"))) return false;
            if (!record.getPowerDeepLow().equals(new BigDecimal("10.00"))) return false;

            // 验证时间字段
            if (record.getRecordTime() == null) return false;

            // 验证 electricMeterDetailDto
            ElectricMeterDetailDto detailDto = record.getElectricMeterDetailDto();
            if (detailDto == null) return false;
            if (!detailDto.getMeterId().equals(1)) return false;
            if (!detailDto.getMeterName().equals("测试电表1")) return false;
            if (!detailDto.getMeterNo().equals("EM202401010001")) return false;
            if (!detailDto.getSpaceId().equals(100)) return false;
            if (!detailDto.getCt().equals(1)) return false;

            return true;
        }));

        verify(balanceService).query(ArgumentMatchers.argThat(dto ->
                dto.getBalanceRelationId().equals(1) &&
                        dto.getBalanceType().equals(BalanceTypeEnum.ELECTRIC_METER)
        ));
        verify(spaceService).getDetail(100);

        // 验证 cancelMeterRecordRepository.insert 的所有入参
        verify(meterCancelRecordRepository).insert(ArgumentMatchers.<MeterCancelRecordEntity>argThat(entity -> {
            // 验证基本信息
            if (!entity.getAccountId().equals(1)) return false;
            if (!entity.getMeterId().equals(1)) return false;
            if (!entity.getMeterName().equals("测试电表1")) return false;
            if (!entity.getMeterNo().equals("EM202401010001")) return false;
            if (!entity.getMeterType().equals(DeviceTypeEnum.ELECTRIC.getMeterTypeCode())) return false;

            // 验证空间信息
            if (!entity.getSpaceId().equals(100)) return false;
            if (!entity.getSpaceName().equals("测试空间")) return false;

            // 验证状态信息
            if (!entity.getBalance().equals(new BigDecimal("50.00"))) return false;

            // 验证电量信息
            if (!entity.getPower().equals(new BigDecimal("100.50"))) return false;
            if (!entity.getPowerHigher().equals(new BigDecimal("20.10"))) return false;
            if (!entity.getPowerHigh().equals(new BigDecimal("30.20"))) return false;
            if (!entity.getPowerLow().equals(new BigDecimal("25.15"))) return false;
            if (!entity.getPowerLower().equals(new BigDecimal("15.05"))) return false;
            if (!entity.getPowerDeepLow().equals(new BigDecimal("10.00"))) return false;
            // 验证历史电量总计
            if (!entity.getHistoryPowerTotal().equals(new BigDecimal("100.50"))) return false;

            // 验证时间字段
            if (entity.getShowTime() == null) return false;

            return true;
        }));
    }

    /**
     * 测试按量计费账户正常销户（离线电表，手动输入电量）
     */
    @Test
    void testCancelMeterAccount_Success_QuantityAccountOffline() {
        // Given
        MeterCancelDto meterCancelDto = new MeterCancelDto()
                .setAccountId(1)
                .setOwnerId(100)
                .setOwnerType(OwnerTypeEnum.ENTERPRISE)
                .setOwnerName("测试企业")
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY)
                .setMeterCloseDetail(List.of(
                        new MeterCancelDetailDto()
                                .setMeterId(1)
                                .setPowerHigher(new BigDecimal("25.00"))
                                .setPowerHigh(new BigDecimal("35.00"))
                                .setPowerLow(new BigDecimal("30.00"))
                                .setPowerLower(new BigDecimal("20.00"))
                                .setPowerDeepLow(new BigDecimal("10.00"))
                ));

        // Mock 电表信息
        ElectricMeterBo meterBo = new ElectricMeterBo()
                .setId(1)
                .setMeterName("测试电表1")
                .setSpaceId(100)
                .setMeterNo("EM202401010001")
                .setCt(1)
                .setAccountId(100);

        // Mock 空间信息
        SpaceBo spaceBo = new SpaceBo()
                .setId(100)
                .setName("测试空间");

        // Mock 余额信息
        BalanceBo balanceBo = new BalanceBo()
                .setBalance(new BigDecimal("75.50"));

        // Mock 行为
        when(electricMeterInfoService.getDetail(1)).thenReturn(meterBo);
        when(spaceService.getDetail(100)).thenReturn(spaceBo);
        when(balanceService.query(any(BalanceQueryDto.class))).thenReturn(balanceBo);
        when(meterCancelRecordRepository.insert(any(MeterCancelRecordEntity.class))).thenReturn(1);
        when(repository.resetMeterAccountInfo(any(ElectricMeterResetAccountQo.class))).thenReturn(1);

        // When
        List<MeterCancelResultDto> result = electricMeterService.cancelMeterAccount(meterCancelDto);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        MeterCancelResultDto closedBalance = result.get(0);
        assertEquals(1, closedBalance.getMeterId());
        assertEquals(new BigDecimal("75.50"), closedBalance.getBalance());

        // 验证方法调用
        verify(electricMeterInfoService).getDetail(1);

        // 验证 meterConsumeService.savePowerRecord 的所有入参
        verify(meterConsumeService).savePowerRecord(ArgumentMatchers.argThat(record -> {
            // 验证基本字段
            if (!record.getAccountId().equals(1)) return false;
            if (!record.getOwnerId().equals(100)) return false;
            if (!record.getOwnerType().equals(OwnerTypeEnum.ENTERPRISE)) return false;
            if (!record.getOwnerName().equals("测试企业")) return false;
            if (!record.getElectricAccountType().equals(ElectricAccountTypeEnum.QUANTITY)) return false;
            if (!record.getNeedConsume()) return false;

            // 验证手动输入的电量数据
            if (!record.getPower().equals(new BigDecimal("120.00"))) return false;
            if (!record.getPowerHigher().equals(new BigDecimal("25.00"))) return false;
            if (!record.getPowerHigh().equals(new BigDecimal("35.00"))) return false;
            if (!record.getPowerLow().equals(new BigDecimal("30.00"))) return false;
            if (!record.getPowerLower().equals(new BigDecimal("20.00"))) return false;
            if (!record.getPowerDeepLow().equals(new BigDecimal("10.00"))) return false;

            // 验证时间字段
            if (record.getRecordTime() == null) return false;

            // 验证 electricMeterDetailDto
            ElectricMeterDetailDto detailDto = record.getElectricMeterDetailDto();
            if (detailDto == null) return false;
            if (!detailDto.getMeterId().equals(1)) return false;
            if (!detailDto.getMeterName().equals("测试电表1")) return false;
            if (!detailDto.getMeterNo().equals("EM202401010001")) return false;
            if (!detailDto.getSpaceId().equals(100)) return false;
            if (!detailDto.getCt().equals(1)) return false;

            return true;
        }));

        verify(balanceService).query(any(BalanceQueryDto.class));

        // 验证 cancelMeterRecordRepository.insert 的所有入参
        verify(meterCancelRecordRepository).insert(ArgumentMatchers.<MeterCancelRecordEntity>argThat(entity -> {
            // 验证基本信息
            if (!entity.getAccountId().equals(1)) return false;
            if (!entity.getMeterId().equals(1)) return false;
            if (!entity.getMeterName().equals("测试电表1")) return false;
            if (!entity.getMeterNo().equals("EM202401010001")) return false;
            if (!entity.getMeterType().equals(DeviceTypeEnum.ELECTRIC.getMeterTypeCode())) return false;

            // 验证空间信息
            if (!entity.getSpaceId().equals(100)) return false;
            if (!entity.getSpaceName().equals("测试空间")) return false;

            // 验证状态信息
            if (!entity.getBalance().equals(new BigDecimal("75.50"))) return false;

            // 验证电量信息（手动输入）
            if (!entity.getPower().equals(new BigDecimal("120.00"))) return false;
            if (!entity.getPowerHigher().equals(new BigDecimal("25.00"))) return false;
            if (!entity.getPowerHigh().equals(new BigDecimal("35.00"))) return false;
            if (!entity.getPowerLow().equals(new BigDecimal("30.00"))) return false;
            if (!entity.getPowerLower().equals(new BigDecimal("20.00"))) return false;
            if (!entity.getPowerDeepLow().equals(new BigDecimal("10.00"))) return false;
            // 验证历史电量总计
            if (!entity.getHistoryPowerTotal().equals(new BigDecimal("120.00"))) return false;

            // 验证时间字段
            if (entity.getShowTime() == null) return false;

            return true;
        }));

        // 验证离线电表不调用获取电量的方法
        verify(deviceModuleContext, never()).getService(any(), any());
    }

    /**
     * 测试包月计费账户销户
     */
    @Test
    void testCancelMeterAccount_Success_MonthlyAccount() {
        // Given
        MeterCancelDto meterCancelDto = new MeterCancelDto()
                .setAccountId(1)
                .setOwnerId(100)
                .setOwnerType(OwnerTypeEnum.ENTERPRISE)
                .setOwnerName("测试企业")
                .setElectricAccountType(ElectricAccountTypeEnum.MONTHLY)
                .setMeterCloseDetail(List.of(
                        new MeterCancelDetailDto()
                                .setMeterId(1)
                ));

        // Mock 电表信息
        ElectricMeterBo meterBo = new ElectricMeterBo()
                .setId(1)
                .setMeterName("测试电表1")
                .setSpaceId(null) // 无空间信息
                .setIsOnline(true)
                .setMeterNo("EM202401010001")
                .setCt(1)
                .setAccountId(100)
                .setOwnAreaId(1000);

        // Mock 行为
        when(electricMeterInfoService.getDetail(1)).thenReturn(meterBo);
        when(deviceModuleContext.getService(EnergyService.class, 1000)).thenReturn(energyService);
        when(energyService.getMeterEnergy(any(ElectricDeviceDegreeDto.class)))
                .thenReturn(new BigDecimal("40.00"))
                .thenReturn(new BigDecimal("60.00"))
                .thenReturn(new BigDecimal("50.00"))
                .thenReturn(new BigDecimal("30.00"))
                .thenReturn(new BigDecimal("20.00"));
        when(meterCancelRecordRepository.insert(any(MeterCancelRecordEntity.class))).thenReturn(1);
        when(repository.resetMeterAccountInfo(any(ElectricMeterResetAccountQo.class))).thenReturn(1);

        // When
        List<MeterCancelResultDto> result = electricMeterService.cancelMeterAccount(meterCancelDto);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        MeterCancelResultDto closedBalance = result.get(0);
        assertEquals(1, closedBalance.getMeterId());
        assertNull(closedBalance.getBalance()); // 包月账户没有表上的余额

        // 验证方法调用
        verify(electricMeterInfoService, times(2)).getDetail(1);

        // 验证 meterConsumeService.savePowerRecord 的所有入参
        verify(meterConsumeService).savePowerRecord(ArgumentMatchers.argThat(record -> {
            // 验证基本字段
            if (!record.getAccountId().equals(1)) return false;
            if (!record.getOwnerId().equals(100)) return false;
            if (!record.getOwnerType().equals(OwnerTypeEnum.ENTERPRISE)) return false;
            if (!record.getOwnerName().equals("测试企业")) return false;
            if (!record.getElectricAccountType().equals(ElectricAccountTypeEnum.MONTHLY)) return false;
            if (!record.getNeedConsume()) return false;

            // 验证电量数据（从能源服务获取）
            if (!record.getPower().equals(new BigDecimal("200.00"))) return false;
            if (!record.getPowerHigher().equals(new BigDecimal("40.00"))) return false;
            if (!record.getPowerHigh().equals(new BigDecimal("60.00"))) return false;
            if (!record.getPowerLow().equals(new BigDecimal("50.00"))) return false;
            if (!record.getPowerLower().equals(new BigDecimal("30.00"))) return false;
            if (!record.getPowerDeepLow().equals(new BigDecimal("20.00"))) return false;

            // 验证时间字段
            if (record.getRecordTime() == null) return false;

            // 验证 electricMeterDetailDto
            ElectricMeterDetailDto detailDto = record.getElectricMeterDetailDto();
            if (detailDto == null) return false;
            if (!detailDto.getMeterId().equals(1)) return false;
            if (!detailDto.getMeterName().equals("测试电表1")) return false;
            if (!detailDto.getMeterNo().equals("EM202401010001")) return false;
            if (detailDto.getSpaceId() != null) return false; // 无空间信息
            if (!detailDto.getCt().equals(1)) return false;

            return true;
        }));

        // 包月账户不查询余额
        verify(balanceService, never()).query(any(BalanceQueryDto.class));
        // 无空间信息不调用空间服务
        verify(spaceService, never()).getDetail(any());

        // 验证 cancelMeterRecordRepository.insert 的所有入参
        verify(meterCancelRecordRepository).insert(ArgumentMatchers.<MeterCancelRecordEntity>argThat(entity -> {
            // 验证基本信息
            if (!entity.getAccountId().equals(1)) return false;
            if (!entity.getMeterId().equals(1)) return false;
            if (!entity.getMeterName().equals("测试电表1")) return false;
            if (!entity.getMeterNo().equals("EM202401010001")) return false;
            if (!entity.getMeterType().equals(DeviceTypeEnum.ELECTRIC.getMeterTypeCode())) return false;

            // 验证空间信息（无空间信息的情况）
            if (entity.getSpaceId() != null) return false;
            if (entity.getSpaceName() != null) return false;
            if (entity.getSpaceParentIds() != null) return false;
            if (entity.getSpaceParentNames() != null) return false;

            // 验证状态信息（包月账户无余额）
            if (entity.getBalance() != null) return false;

            // 验证电量信息
            if (!entity.getPower().equals(new BigDecimal("200.00"))) return false;
            if (!entity.getPowerHigher().equals(new BigDecimal("40.00"))) return false;
            if (!entity.getPowerHigh().equals(new BigDecimal("60.00"))) return false;
            if (!entity.getPowerLow().equals(new BigDecimal("50.00"))) return false;
            if (!entity.getPowerLower().equals(new BigDecimal("30.00"))) return false;
            if (!entity.getPowerDeepLow().equals(new BigDecimal("20.00"))) return false;
            // 验证历史电量总计
            if (!entity.getHistoryPowerTotal().equals(new BigDecimal("200.00"))) return false;

            // 验证时间字段
            if (entity.getShowTime() == null) return false;

            return true;
        }));
    }

    /**
     * 测试电表离线但未提供手动电量的异常情况
     */
    @Test
    void testCancelMeterAccount_Exception_OfflineWithoutManualPower() {
        // Given
        MeterCancelDto meterCancelDto = new MeterCancelDto()
                .setAccountId(1)
                .setOwnerId(100)
                .setOwnerType(OwnerTypeEnum.ENTERPRISE)
                .setOwnerName("测试企业")
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY)
                .setMeterCloseDetail(List.of(
                        new MeterCancelDetailDto()
                                .setMeterId(1)
                        // 未提供手动电量
                ));

        // Mock 电表信息
        ElectricMeterBo meterBo = new ElectricMeterBo()
                .setId(1)
                .setMeterName("测试电表1")
                .setMeterNo("EM202401010001")
                .setCt(1)
                .setAccountId(100);

        when(electricMeterInfoService.getDetail(1)).thenReturn(meterBo);

        // When & Then
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> electricMeterService.cancelMeterAccount(meterCancelDto));

        assertTrue(exception.getMessage().contains("电表离线") || exception.getMessage().contains("手动电量"));

        // 验证方法调用
        verify(electricMeterInfoService).getDetail(1);
        // 异常情况下不应该调用后续方法
        verify(meterConsumeService, never()).savePowerRecord(any());
        verify(meterCancelRecordRepository, never()).insert(any(MeterCancelRecordEntity.class));
        verify(repository, never()).resetMeterAccountInfo(any());
    }

    /**
     * 测试 cancelNo 字段正常传入时能正确保存到 CancelMeterRecordEntity 中
     */
    @Test
    void testCancelMeterAccount_CancelNo_CorrectlySaved() {
        // Given
        String expectedCancelNo = "CANCEL202401010001";
        MeterCancelDto meterCancelDto = new MeterCancelDto()
                .setAccountId(1)
                .setCancelNo(expectedCancelNo)
                .setOwnerId(100)
                .setOwnerType(OwnerTypeEnum.ENTERPRISE)
                .setOwnerName("测试企业")
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY)
                .setMeterCloseDetail(List.of(
                        new MeterCancelDetailDto()
                                .setMeterId(1)
                                .setPowerHigher(new BigDecimal("25.00"))
                                .setPowerHigh(new BigDecimal("35.00"))
                                .setPowerLow(new BigDecimal("30.00"))
                                .setPowerLower(new BigDecimal("20.00"))
                                .setPowerDeepLow(new BigDecimal("10.00"))
                ));

        // Mock 电表信息
        ElectricMeterBo meterBo = new ElectricMeterBo()
                .setId(1)
                .setMeterName("测试电表1")
                .setSpaceId(100)
                .setMeterNo("EM202401010001")
                .setCt(1)
                .setAccountId(100);

        // Mock 空间信息
        SpaceBo spaceBo = new SpaceBo()
                .setId(100)
                .setName("测试空间");

        // Mock 余额信息
        BalanceBo balanceBo = new BalanceBo()
                .setBalance(new BigDecimal("75.50"));

        // Mock 行为
        when(electricMeterInfoService.getDetail(1)).thenReturn(meterBo);
        when(spaceService.getDetail(100)).thenReturn(spaceBo);
        when(balanceService.query(any(BalanceQueryDto.class))).thenReturn(balanceBo);
        when(meterCancelRecordRepository.insert(any(MeterCancelRecordEntity.class))).thenReturn(1);
        when(repository.resetMeterAccountInfo(any(ElectricMeterResetAccountQo.class))).thenReturn(1);

        // When
        List<MeterCancelResultDto> result = electricMeterService.cancelMeterAccount(meterCancelDto);

        // Then - 验证 CancelMeterRecordEntity 中的 cancelNo 字段被正确设置
        ArgumentCaptor<MeterCancelRecordEntity> cancelRecordCaptor = ArgumentCaptor.forClass(MeterCancelRecordEntity.class);
        verify(meterCancelRecordRepository).insert(cancelRecordCaptor.capture());

        MeterCancelRecordEntity savedRecord = cancelRecordCaptor.getValue();
        assertEquals(expectedCancelNo, savedRecord.getCancelNo(), "cancelNo 字段应该被正确保存");
        assertEquals(1, savedRecord.getAccountId(), "accountId 应该正确");
        assertEquals(1, savedRecord.getMeterId(), "meterId 应该正确");

        // 验证返回结果
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    /**
     * 测试不同 cancelNo 值的销户记录能正确区分
     */
    @Test
    void testCancelMeterAccount_CancelNo_DifferentValues() {
        // Given - 第一个销户记录
        String firstCancelNo = "CANCEL001";
        MeterCancelDto firstCloseDto = new MeterCancelDto()
                .setAccountId(1)
                .setCancelNo(firstCancelNo)
                .setOwnerId(100)
                .setOwnerType(OwnerTypeEnum.ENTERPRISE)
                .setOwnerName("测试企业")
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY)
                .setMeterCloseDetail(List.of(
                        new MeterCancelDetailDto()
                                .setMeterId(1)
                                .setPowerHigher(new BigDecimal("25.00"))
                                .setPowerHigh(new BigDecimal("35.00"))
                                .setPowerLow(new BigDecimal("30.00"))
                                .setPowerLower(new BigDecimal("20.00"))
                                .setPowerDeepLow(new BigDecimal("10.00"))
                ));

        // Mock 电表信息
        ElectricMeterBo meterBo = new ElectricMeterBo()
                .setId(1)
                .setMeterName("测试电表1")
                .setSpaceId(100)
                .setMeterNo("EM202401010001")
                .setCt(1)
                .setAccountId(100);

        // Mock 空间信息
        SpaceBo spaceBo = new SpaceBo()
                .setId(100)
                .setName("测试空间");

        // Mock 余额信息
        BalanceBo balanceBo = new BalanceBo()
                .setBalance(new BigDecimal("75.50"));

        // Mock 行为
        when(electricMeterInfoService.getDetail(1)).thenReturn(meterBo);
        when(spaceService.getDetail(100)).thenReturn(spaceBo);
        when(balanceService.query(any(BalanceQueryDto.class))).thenReturn(balanceBo);
        when(meterCancelRecordRepository.insert(any(MeterCancelRecordEntity.class))).thenReturn(1);
        when(repository.resetMeterAccountInfo(any(ElectricMeterResetAccountQo.class))).thenReturn(1);

        electricMeterService.cancelMeterAccount(firstCloseDto);

        // Then - 验证第一个记录的 cancelNo
        ArgumentCaptor<MeterCancelRecordEntity> firstCaptor = ArgumentCaptor.forClass(MeterCancelRecordEntity.class);
        verify(meterCancelRecordRepository, times(1)).insert(firstCaptor.capture());
        assertEquals(firstCancelNo, firstCaptor.getValue().getCancelNo());

        // Given - 第二个销户记录（不同的 cancelNo）
        String secondCancelNo = "CANCEL002";
        MeterCancelDto secondCloseDto = new MeterCancelDto()
                .setAccountId(2)
                .setCancelNo(secondCancelNo)
                .setOwnerId(200)
                .setOwnerType(OwnerTypeEnum.ENTERPRISE)
                .setOwnerName("测试企业2")
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY)
                .setMeterCloseDetail(List.of(
                        new MeterCancelDetailDto()
                                .setMeterId(2)
                                .setPowerHigher(new BigDecimal("30.00"))
                                .setPowerHigh(new BigDecimal("40.00"))
                                .setPowerLow(new BigDecimal("35.00"))
                                .setPowerLower(new BigDecimal("25.00"))
                                .setPowerDeepLow(new BigDecimal("20.00"))
                ));

        // Mock 第二个电表信息
        ElectricMeterBo secondMeterBo = new ElectricMeterBo()
                .setId(2)
                .setMeterName("测试电表2")
                .setSpaceId(200)
                .setMeterNo("EM202401010002")
                .setCt(1)
                .setAccountId(200);

        SpaceBo secondSpaceBo = new SpaceBo()
                .setId(200)
                .setName("测试空间2");

        BalanceBo secondBalanceBo = new BalanceBo()
                .setBalance(new BigDecimal("85.00"));

        when(electricMeterInfoService.getDetail(2)).thenReturn(secondMeterBo);
        when(spaceService.getDetail(200)).thenReturn(secondSpaceBo);
        when(balanceService.query(ArgumentMatchers.argThat(dto -> dto.getBalanceRelationId().equals(2)))).thenReturn(secondBalanceBo);
        when(repository.resetMeterAccountInfo(any(ElectricMeterResetAccountQo.class))).thenReturn(1);

        // When - 执行第二次销户
        electricMeterService.cancelMeterAccount(secondCloseDto);

        // Then - 验证第二个记录的 cancelNo 与第一个不同
        ArgumentCaptor<MeterCancelRecordEntity> secondCaptor = ArgumentCaptor.forClass(MeterCancelRecordEntity.class);
        verify(meterCancelRecordRepository, times(2)).insert(secondCaptor.capture());

        List<MeterCancelRecordEntity> allRecords = secondCaptor.getAllValues();
        assertEquals(2, allRecords.size(), "应该有两条销户记录");
        assertEquals(firstCancelNo, allRecords.get(0).getCancelNo(), "第一条记录的 cancelNo 应该正确");
        assertEquals(secondCancelNo, allRecords.get(1).getCancelNo(), "第二条记录的 cancelNo 应该正确");
        assertNotEquals(allRecords.get(0).getCancelNo(), allRecords.get(1).getCancelNo(), "两条记录的 cancelNo 应该不同");
    }

    /**
     * 测试系统异常处理
     */
    @Test
    void testCancelMeterAccount_Exception_SystemError() {
        // Given
        MeterCancelDto meterCancelDto = new MeterCancelDto()
                .setAccountId(1)
                .setCancelNo("CANCEL001")
                .setOwnerId(100)
                .setOwnerType(OwnerTypeEnum.ENTERPRISE)
                .setOwnerName("测试企业")
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY)
                .setMeterCloseDetail(List.of(
                        new MeterCancelDetailDto()
                                .setMeterId(1)
                ));

        // Mock 异常
        when(electricMeterInfoService.getDetail(1))
                .thenThrow(new RuntimeException("系统异常"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> electricMeterService.cancelMeterAccount(meterCancelDto));

        assertEquals("批量电表销户操作失败：系统异常", exception.getMessage());

        // 验证方法调用
        verify(electricMeterInfoService).getDetail(1);
        // 异常情况下不应该调用后续方法
        verify(meterConsumeService, never()).savePowerRecord(any());
        verify(meterCancelRecordRepository, never()).insert(any(MeterCancelRecordEntity.class));
        verify(repository, never()).resetMeterAccountInfo(any());
    }

    /**
     * 测试设置预警计划：根据余额分组设置预警等级
     * 分组：FIRST(<=firstLevel)、SECOND(<=secondLevel)、NONE(>firstLevel或缺失)
     */
    @Test
    void testSetMeterWarnPlan_GroupingByBalance_Success() {
        // Given
        List<Integer> meterIds = List.of(1, 2, 3);
        ElectricMeterBo m1 = new ElectricMeterBo().setId(1).setMeterNo("EM001").setAccountId(100).setIsOnline(true).setIsPrepay(true);
        ElectricMeterBo m2 = new ElectricMeterBo().setId(2).setMeterNo("EM002").setAccountId(100).setIsOnline(true).setIsPrepay(true);
        ElectricMeterBo m3 = new ElectricMeterBo().setId(3).setMeterNo("EM003").setAccountId(100).setIsOnline(true).setIsPrepay(true);
        Map<Integer, ElectricMeterBo> idMap = Map.of(1, m1, 2, m2, 3, m3);

        when(electricMeterInfoService.findList(any(ElectricMeterQueryDto.class))).thenAnswer(invocation -> {
            ElectricMeterQueryDto query = invocation.getArgument(0);
            List<Integer> ids = query.getInIds();
            return ids.stream().map(idMap::get).toList();
        });

        when(warnPlanService.getDetail(1)).thenReturn(new WarnPlanBo()
                .setFirstLevel(new BigDecimal("1000"))
                .setSecondLevel(new BigDecimal("400")));

        when(balanceService.query(ArgumentMatchers.argThat(dto ->
                dto != null && Integer.valueOf(1).equals(dto.getBalanceRelationId()) && dto.getBalanceType() == BalanceTypeEnum.ELECTRIC_METER)))
                .thenReturn(new BalanceBo().setBalance(new BigDecimal("800")));
        when(balanceService.query(ArgumentMatchers.argThat(dto ->
                dto != null && Integer.valueOf(2).equals(dto.getBalanceRelationId()) && dto.getBalanceType() == BalanceTypeEnum.ELECTRIC_METER)))
                .thenReturn(new BalanceBo().setBalance(new BigDecimal("300")));
        when(balanceService.query(ArgumentMatchers.argThat(dto ->
                dto != null && Integer.valueOf(3).equals(dto.getBalanceRelationId()) && dto.getBalanceType() == BalanceTypeEnum.ELECTRIC_METER)))
                .thenReturn(new BalanceBo().setBalance(new BigDecimal("2000")));

        when(repository.batchUpdate(any(ElectricMeterBatchUpdateQo.class))).thenAnswer(invocation -> {
            ElectricMeterBatchUpdateQo qo = invocation.getArgument(0);
            return qo.getMeterIds() == null ? 0 : qo.getMeterIds().size();
        });

        ElectricMeterWarnPlanDto dto = new ElectricMeterWarnPlanDto()
                .setWarnPlanId(1)
                .setMeterIds(meterIds);

        // When
        assertDoesNotThrow(() -> electricMeterService.setMeterWarnPlan(dto));

        // Then
        ArgumentCaptor<ElectricMeterBatchUpdateQo> captor = ArgumentCaptor.forClass(ElectricMeterBatchUpdateQo.class);
        verify(repository, times(4)).batchUpdate(captor.capture());

        List<ElectricMeterBatchUpdateQo> updates = captor.getAllValues();

        // 验证：预警方案绑定
        ElectricMeterBatchUpdateQo planBind = updates.stream().filter(q -> q.getWarnPlanId() != null).findFirst().orElse(null);
        assertNotNull(planBind);
        assertEquals(meterIds, planBind.getMeterIds());

        // 验证：分组预警等级设置
        assertTrue(updates.stream().anyMatch(q -> "FIRST".equals(q.getWarnType()) && q.getMeterIds().equals(List.of(1))));
        assertTrue(updates.stream().anyMatch(q -> "SECOND".equals(q.getWarnType()) && q.getMeterIds().equals(List.of(2))));
        assertTrue(updates.stream().anyMatch(q -> "NONE".equals(q.getWarnType()) && q.getMeterIds().equals(List.of(3))));
    }

    @Test
    void testSetMeterWarnPlan_OnlyFirstLevel_ShouldNotThrow() {
        List<Integer> meterIds = List.of(21);
        ElectricMeterBo meter = new ElectricMeterBo().setId(21).setMeterNo("EM021").setAccountId(100).setIsOnline(true).setIsPrepay(true);
        when(electricMeterInfoService.findList(any(ElectricMeterQueryDto.class))).thenReturn(List.of(meter));

        when(warnPlanService.getDetail(1)).thenReturn(new WarnPlanBo()
                .setFirstLevel(new BigDecimal("100"))
                .setSecondLevel(null));

        when(balanceService.query(ArgumentMatchers.argThat(dto ->
                dto != null && Integer.valueOf(21).equals(dto.getBalanceRelationId()) && dto.getBalanceType() == BalanceTypeEnum.ELECTRIC_METER)))
                .thenReturn(new BalanceBo().setBalance(new BigDecimal("50")));

        when(repository.batchUpdate(any(ElectricMeterBatchUpdateQo.class))).thenAnswer(invocation -> {
            ElectricMeterBatchUpdateQo qo = invocation.getArgument(0);
            return qo.getMeterIds() == null ? 0 : qo.getMeterIds().size();
        });

        ElectricMeterWarnPlanDto dto = new ElectricMeterWarnPlanDto()
                .setWarnPlanId(1)
                .setMeterIds(meterIds);

        assertDoesNotThrow(() -> electricMeterService.setMeterWarnPlan(dto));

        ArgumentCaptor<ElectricMeterBatchUpdateQo> captor = ArgumentCaptor.forClass(ElectricMeterBatchUpdateQo.class);
        verify(repository, times(2)).batchUpdate(captor.capture());
        List<ElectricMeterBatchUpdateQo> updates = captor.getAllValues();
        assertTrue(updates.stream().anyMatch(q -> "FIRST".equals(q.getWarnType()) && q.getMeterIds().equals(List.of(21))));
    }

    @Test
    void testSetMeterWarnPlan_OnlySecondLevel_ShouldNotThrow() {
        List<Integer> meterIds = List.of(22);
        ElectricMeterBo meter = new ElectricMeterBo().setId(22).setMeterNo("EM022").setAccountId(100).setIsOnline(true).setIsPrepay(true);
        when(electricMeterInfoService.findList(any(ElectricMeterQueryDto.class))).thenReturn(List.of(meter));

        when(warnPlanService.getDetail(1)).thenReturn(new WarnPlanBo()
                .setFirstLevel(null)
                .setSecondLevel(new BigDecimal("80")));

        when(balanceService.query(ArgumentMatchers.argThat(dto ->
                dto != null && Integer.valueOf(22).equals(dto.getBalanceRelationId()) && dto.getBalanceType() == BalanceTypeEnum.ELECTRIC_METER)))
                .thenReturn(new BalanceBo().setBalance(new BigDecimal("50")));

        when(repository.batchUpdate(any(ElectricMeterBatchUpdateQo.class))).thenAnswer(invocation -> {
            ElectricMeterBatchUpdateQo qo = invocation.getArgument(0);
            return qo.getMeterIds() == null ? 0 : qo.getMeterIds().size();
        });

        ElectricMeterWarnPlanDto dto = new ElectricMeterWarnPlanDto()
                .setWarnPlanId(1)
                .setMeterIds(meterIds);

        assertDoesNotThrow(() -> electricMeterService.setMeterWarnPlan(dto));

        ArgumentCaptor<ElectricMeterBatchUpdateQo> captor = ArgumentCaptor.forClass(ElectricMeterBatchUpdateQo.class);
        verify(repository, times(2)).batchUpdate(captor.capture());
        List<ElectricMeterBatchUpdateQo> updates = captor.getAllValues();
        assertTrue(updates.stream().anyMatch(q -> "SECOND".equals(q.getWarnType()) && q.getMeterIds().equals(List.of(22))));
    }

    /**
     * 测试余额缺失：默认归为 NONE 预警等级
     */
    @Test
    void testSetMeterWarnPlan_MissingBalance_DefaultToNone() {
        // Given
        List<Integer> meterIds = List.of(11);
        ElectricMeterBo m1 = new ElectricMeterBo().setId(11).setMeterNo("EM011").setAccountId(100).setIsOnline(true).setIsPrepay(true);

        Map<Integer, ElectricMeterBo> idMap = Map.of(11, m1);
        when(electricMeterInfoService.findList(any(ElectricMeterQueryDto.class))).thenAnswer(invocation -> {
            ElectricMeterQueryDto query = invocation.getArgument(0);
            return query.getInIds().stream().map(idMap::get).toList();
        });

        when(warnPlanService.getDetail(1)).thenReturn(new WarnPlanBo()
                .setFirstLevel(new BigDecimal("1000"))
                .setSecondLevel(new BigDecimal("400")));

        when(balanceService.query(ArgumentMatchers.argThat(dto ->
                dto != null && Integer.valueOf(11).equals(dto.getBalanceRelationId()) && dto.getBalanceType() == BalanceTypeEnum.ELECTRIC_METER)))
                .thenThrow(new NotFoundException("余额不存在"));

        when(repository.batchUpdate(any(ElectricMeterBatchUpdateQo.class))).thenAnswer(invocation -> {
            ElectricMeterBatchUpdateQo qo = invocation.getArgument(0);
            return qo.getMeterIds() == null ? 0 : qo.getMeterIds().size();
        });

        ElectricMeterWarnPlanDto dto = new ElectricMeterWarnPlanDto()
                .setWarnPlanId(1)
                .setMeterIds(meterIds);

        // When
        assertDoesNotThrow(() -> electricMeterService.setMeterWarnPlan(dto));

        // Then
        ArgumentCaptor<ElectricMeterBatchUpdateQo> captor = ArgumentCaptor.forClass(ElectricMeterBatchUpdateQo.class);
        verify(repository, times(2)).batchUpdate(captor.capture());

        List<ElectricMeterBatchUpdateQo> updates = captor.getAllValues();
        // 预警等级设置应为 NONE
        assertTrue(updates.stream().anyMatch(q -> "NONE".equals(q.getWarnType()) && q.getMeterIds().equals(List.of(11))));
    }

    @Test
    void testCalculateHistoryPowerTotal_ShouldIncludeHistoryOffset() {
        ElectricMeterDetailDto meterDto = new ElectricMeterDetailDto()
                .setStepStartValue(new BigDecimal("50"))
                .setHistoryPowerOffset(new BigDecimal("300"));
        ElectricMeterPowerRecordDto recordDto = new ElectricMeterPowerRecordDto()
                .setPower(new BigDecimal("120"));

        BigDecimal result = ReflectionTestUtils.invokeMethod(
                electricMeterService,
                "calculateHistoryPowerTotal",
                meterDto,
                recordDto);

        assertEquals(new BigDecimal("370"), result);
    }

    @Test
    void testResetCurrentYearMeterStepRecord_ShouldInsertNewStepRecord() {
        MeterStepResetDto resetDto = new MeterStepResetDto().setMeterId(1);
        ElectricMeterBo meterBo = new ElectricMeterBo()
                .setId(1)
                .setMeterNo("EM001")
                .setAccountId(200)
                .setOwnAreaId(300)
                .setIotId(400);
        when(electricMeterInfoService.getDetail(1)).thenReturn(meterBo);
        when(accountMeterStepRepository.getOne(any(AccountMeterStepQo.class))).thenReturn(null);
        when(deviceModuleContext.getService(eq(EnergyService.class), eq(300))).thenReturn(energyService);
        when(energyService.getMeterEnergy(any())).thenThrow(new RuntimeException("mock energy failure"));
        BigDecimal snapshotPower = new BigDecimal("150.50");
        when(electricMeterPowerRecordService.findLatestPower(anyInt())).thenReturn(snapshotPower);

        int expectedYear = LocalDateTime.now().getYear();
        assertDoesNotThrow(() -> electricMeterService.resetCurrentYearMeterStepRecord(resetDto));

        ArgumentCaptor<MeterStepEntity> captor = ArgumentCaptor.forClass(MeterStepEntity.class);
        verify(accountMeterStepRepository).insert(captor.capture());
        MeterStepEntity inserted = captor.getValue();
        assertEquals(meterBo.getAccountId(), inserted.getAccountId());
        assertEquals(resetDto.getMeterId(), inserted.getMeterId());
        assertEquals(snapshotPower, inserted.getStepStartValue());
        assertEquals(BigDecimal.ZERO, inserted.getHistoryPowerOffset());
        assertEquals(expectedYear, inserted.getCurrentYear());
        assertEquals(Boolean.TRUE, inserted.getIsLatest());
        verify(accountMeterStepRepository).clearLatestFlag(any(AccountMeterStepQo.class));
    }

    @Test
    void testResetCurrentYearMeterStepRecord_ShouldSkipWhenMeterUnbound() {
        MeterStepResetDto resetDto = new MeterStepResetDto().setMeterId(2);
        ElectricMeterBo meterBo = new ElectricMeterBo()
                .setId(2)
                .setMeterNo("EM002")
                .setAccountId(null);
        when(electricMeterInfoService.getDetail(2)).thenReturn(meterBo);

        electricMeterService.resetCurrentYearMeterStepRecord(resetDto);

        verify(accountMeterStepRepository, never()).insert(any(MeterStepEntity.class));
        verify(electricMeterPowerRecordService, never()).findLatestPower(any());
    }

    @Test
    void testResetCurrentYearMeterStepRecord_ShouldSkipWhenCurrentYearExists() {
        MeterStepResetDto resetDto = new MeterStepResetDto().setMeterId(3);
        ElectricMeterBo meterBo = new ElectricMeterBo()
                .setId(3)
                .setMeterNo("EM003")
                .setAccountId(999)
                .setOwnAreaId(111);
        when(electricMeterInfoService.getDetail(3)).thenReturn(meterBo);
        MeterStepEntity existing = new MeterStepEntity().setCurrentYear(LocalDateTime.now().getYear());
        when(accountMeterStepRepository.getOne(any(AccountMeterStepQo.class))).thenReturn(existing);

        electricMeterService.resetCurrentYearMeterStepRecord(resetDto);

        verify(accountMeterStepRepository, never()).insert(any(MeterStepEntity.class));
        verify(electricMeterPowerRecordService, never()).findLatestPower(any());
    }

}
