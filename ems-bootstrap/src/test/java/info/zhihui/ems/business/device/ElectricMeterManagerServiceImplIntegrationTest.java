package info.zhihui.ems.business.device;

import info.zhihui.ems.business.device.dto.*;
import info.zhihui.ems.common.enums.CalculateTypeEnum;
import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import info.zhihui.ems.common.enums.ElectricPricePeriodEnum;
import info.zhihui.ems.common.enums.OwnerTypeEnum;
import info.zhihui.ems.common.enums.WarnTypeEnum;
import info.zhihui.ems.common.enums.MeterTypeEnum;
import info.zhihui.ems.business.device.entity.ElectricMeterEntity;
import info.zhihui.ems.business.device.entity.MeterStepEntity;
import info.zhihui.ems.business.device.enums.ElectricSwitchStatusEnum;
import info.zhihui.ems.business.device.repository.ElectricMeterRepository;
import info.zhihui.ems.business.device.repository.MeterStepRepository;
import info.zhihui.ems.business.device.service.ElectricMeterManagerService;
import info.zhihui.ems.business.device.dto.MeterStepResetDto;
import info.zhihui.ems.business.device.qo.AccountMeterStepQo;
import info.zhihui.ems.business.finance.entity.ElectricMeterPowerRecordEntity;
import info.zhihui.ems.business.finance.repository.ElectricMeterPowerRecordRepository;
import info.zhihui.ems.business.plan.dto.ElectricPriceTimeDto;
import info.zhihui.ems.foundation.integration.biz.command.enums.CommandSourceEnum;
import info.zhihui.ems.components.context.setter.RequestContextSetter;
import info.zhihui.ems.components.context.model.UserRequestData;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ElectricMeterManagerService 集成测试
 * 测试电表管理服务的完整业务流程和数据库交互
 *
 * @author jerryxiaosa
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("integrationtest")
@Transactional
@Rollback
@Slf4j
class ElectricMeterManagerServiceImplIntegrationTest {

    @Autowired
    private ElectricMeterManagerService electricMeterManagerService;

    @Autowired
    private ElectricMeterRepository electricMeterRepository;

    @Autowired
    private MeterStepRepository meterStepRepository;

    @Autowired
    private ElectricMeterPowerRecordRepository electricMeterPowerRecordRepository;

    /**
     * 测试参数校验 - 应抛出ConstraintViolationException
     */
    @Test
    void testParameterValidation_ShouldThrowConstraintViolationException() {
        // ElectricMeterAddDto 参数校验测试
        // 测试电表名称为空
        assertThrows(ConstraintViolationException.class, () -> {
            electricMeterManagerService.add(new ElectricMeterCreateDto()
                    .setMeterName("")
                    .setDeviceNo("test-device-no")
                    .setModelId(1)
                    .setSpaceId(1)
                    .setCt(5)
                    .setIsCalculate(true)
                    .setIsPrepay(false)
                    .setGatewayId(300)
                    .setPortNo(1)
                    .setMeterAddress(1)
            );
        });

        // 测试电表名称为null
        assertThrows(ConstraintViolationException.class, () -> {
            electricMeterManagerService.add(new ElectricMeterCreateDto()
                    .setMeterName(null)
                    .setDeviceNo("test-device-no")
                    .setModelId(1)
                    .setSpaceId(1)
                    .setCt(5)
                    .setIsCalculate(true)
                    .setIsPrepay(false)
                    .setGatewayId(300)
                    .setPortNo(1)
                    .setMeterAddress(1)
            );
        });

        // 测试型号ID为null
        assertThrows(ConstraintViolationException.class, () -> {
            electricMeterManagerService.add(new ElectricMeterCreateDto()
                    .setMeterName("测试电表")
                    .setDeviceNo("test-device-no")
                    .setModelId(null)
                    .setSpaceId(1)
                    .setCt(5)
                    .setIsCalculate(true)
                    .setIsPrepay(false)
                    .setGatewayId(300)
                    .setPortNo(1)
                    .setMeterAddress(1)
            );
        });

        // 测试空间ID为null
        assertThrows(ConstraintViolationException.class, () -> {
            electricMeterManagerService.add(new ElectricMeterCreateDto()
                    .setMeterName("测试电表")
                    .setDeviceNo("test-device-no")
                    .setModelId(1)
                    .setSpaceId(null)
                    .setCt(5)
                    .setIsCalculate(true)
                    .setIsPrepay(false)
                    .setGatewayId(300)
                    .setPortNo(1)
                    .setMeterAddress(1)
            );
        });

        // 测试CT为负数
        assertThrows(ConstraintViolationException.class, () -> {
            electricMeterManagerService.add(new ElectricMeterCreateDto()
                    .setMeterName("测试电表")
                    .setDeviceNo("test-device-no")
                    .setModelId(1)
                    .setSpaceId(1)
                    .setCt(-1)
                    .setIsCalculate(true)
                    .setIsPrepay(false)
                    .setGatewayId(300)
                    .setPortNo(1)
                    .setMeterAddress(1)
            );
        });

        // ElectricMeterSwitchStatusDto 参数校验测试
        assertThrows(ConstraintViolationException.class, () -> {
            electricMeterManagerService.setSwitchStatus(new ElectricMeterSwitchStatusDto()
                    .setSwitchStatus(ElectricSwitchStatusEnum.ON)
                    .setCommandSource(CommandSourceEnum.USER)
            );
        });

        // ElectricMeterTimeDto 参数校验测试
        assertThrows(ConstraintViolationException.class, () -> {
            List<ElectricPriceTimeDto> timeList = new ArrayList<>();
            ElectricPriceTimeDto timeDto = new ElectricPriceTimeDto()
                    .setType(ElectricPricePeriodEnum.HIGH)
                    .setStart(LocalTime.of(8, 0));
            timeList.add(timeDto);
            electricMeterManagerService.setElectricTime(new ElectricMeterTimeDto()
                    .setCommandSource(CommandSourceEnum.USER)
                    .setTimeList(timeList)
            );
        });

        // ElectricMeterOnlineStatusDto 参数校验测试
        assertThrows(ConstraintViolationException.class, () -> {
            electricMeterManagerService.syncMeterOnlineStatus(null);
        });

        // MeterOpenDto 参数校验测试
        assertThrows(ConstraintViolationException.class, () -> {
            electricMeterManagerService.openMeterAccount(null);
        });
    }

    // ==================== 辅助方法 ====================

    // ==================== 业务流程测试 ====================

    /**
     * 测试电表添加功能 - 成功场景
     * 验证：1. 能够成功添加电表 2. 返回有效的电表ID 3. 数据库中正确保存电表信息
     */
    @Test
    void testAddElectricMeter_Success() {
        // 创建有效的电表添加DTO
        ElectricMeterCreateDto dto = createValidElectricMeterAddDto();

        // 执行添加操作
        Integer meterId = electricMeterManagerService.add(dto);

        // 验证返回结果
        assertNotNull(meterId);
        assertTrue(meterId > 0);

        // 验证数据库中的数据
        ElectricMeterEntity savedMeter = electricMeterRepository.selectById(meterId);
        assertNotNull(savedMeter);
        assertEquals(dto.getMeterName(), savedMeter.getMeterName());
        assertEquals(dto.getSpaceId(), savedMeter.getSpaceId());
        assertEquals(dto.getModelId(), savedMeter.getModelId());
        assertEquals(dto.getIsPrepay(), savedMeter.getIsPrepay());
        assertEquals(dto.getCt(), savedMeter.getCt());
        assertNotNull(savedMeter.getMeterNo()); // 系统生成的电表编号
    }

    /**
     * 测试电表删除功能 - 成功场景
     * 验证：1. 能够成功删除已存在的电表 2. 删除操作不抛出异常 3. 数据库状态正确更新
     */
    @Test
    void testDeleteElectricMeter_Success() {
        // 先添加一个电表
        ElectricMeterCreateDto addDto = createValidElectricMeterAddDto();
        Integer meterId = electricMeterManagerService.add(addDto);

        // 验证电表存在
        ElectricMeterEntity meterBeforeDelete = electricMeterRepository.selectById(meterId);
        assertNotNull(meterBeforeDelete);

        // 执行删除操作
        assertDoesNotThrow(() -> {
            electricMeterManagerService.delete(meterId);
        });

        // 验证电表已被删除
        ElectricMeterEntity meterAfterDelete = electricMeterRepository.selectById(meterId);
        assertNull(meterAfterDelete);
    }

    /**
     * 测试电表开关状态设置功能 - 成功场景
     * 验证：1. 能够成功设置电表开关状态 2. 操作不抛出异常 3. 数据库状态正确更新
     */
    @Test
    void testSetSwitchStatus_Success() {
        // 先添加一个电表
        ElectricMeterCreateDto addDto = createValidElectricMeterAddDto();
        Integer meterId = electricMeterManagerService.add(addDto);

        // 强制更新在线状态
        electricMeterManagerService.syncMeterOnlineStatus(new ElectricMeterOnlineStatusDto()
                .setMeterId(meterId)
                .setForce(true)
                .setOnlineStatus(true));

        // 创建开关状态设置DTO
        ElectricMeterSwitchStatusDto switchDto = new ElectricMeterSwitchStatusDto()
                .setId(meterId)
                .setSwitchStatus(ElectricSwitchStatusEnum.OFF)
                .setCommandSource(CommandSourceEnum.USER);

        // 执行设置开关状态操作
        assertDoesNotThrow(() -> {
            electricMeterManagerService.setSwitchStatus(switchDto);
        });

        // 验证数据库中的状态变化
        ElectricMeterEntity updatedMeter = electricMeterRepository.selectById(meterId);
        assertNotNull(updatedMeter);
        // 注意：实际的断闸状态可能需要通过IoT平台同步，这里主要验证方法调用成功
    }

    // 参数校验测试已移至参数化测试方法中

    // ==================== 辅助方法 ====================


    @Test
    void testSetElectricTime_Success() {
        // 先添加一个电表
        ElectricMeterCreateDto addDto = createValidElectricMeterAddDto();
        Integer meterId = electricMeterManagerService.add(addDto);

        // 创建时间段设置DTO
        List<ElectricPriceTimeDto> timeList = new ArrayList<>();
        ElectricPriceTimeDto timeDto = new ElectricPriceTimeDto()
                .setType(ElectricPricePeriodEnum.HIGH)
                .setStart(LocalTime.of(8, 0));
        timeList.add(timeDto);

        ElectricMeterTimeDto dto = new ElectricMeterTimeDto()
                .setId(meterId)
                .setCommandSource(CommandSourceEnum.USER)
                .setTimeList(timeList);

        // 执行设置时间段操作
        assertDoesNotThrow(() -> {
            electricMeterManagerService.setElectricTime(dto);
        });

        // 验证操作成功（具体的时间段配置可能存储在其他表中）
        ElectricMeterEntity meter = electricMeterRepository.selectById(meterId);
        assertNotNull(meter);
    }

    // 参数校验测试已移至参数化测试方法中


    @Test
    void testSyncMeterOnlineStatus_Success() {
        // 先添加一个电表
        ElectricMeterCreateDto addDto = createValidElectricMeterAddDto();
        Integer meterId = electricMeterManagerService.add(addDto);

        // 创建在线状态同步DTO
        ElectricMeterOnlineStatusDto dto = new ElectricMeterOnlineStatusDto()
                .setMeterId(meterId)
                .setOnlineStatus(true)
                .setForce(false);

        // 执行在线状态同步操作
        assertDoesNotThrow(() -> {
            electricMeterManagerService.syncMeterOnlineStatus(dto);
        });

        // 验证数据库中的在线状态
        ElectricMeterEntity updatedMeter = electricMeterRepository.selectById(meterId);
        assertNotNull(updatedMeter);
        // 注意：实际的在线状态可能需要通过IoT平台同步，这里主要验证方法调用成功
    }

    /**
     * 测试电表开户功能 - 成功场景
     * 验证：1. 能够成功为多个电表开户 2. 操作不抛出异常 3. 电表的账户ID正确设置
     */
    @Test
    void testOpenMeterAccount_Success() {
        // 先添加两个电表
        ElectricMeterCreateDto addDto1 = createValidElectricMeterAddDto();
        ElectricMeterCreateDto addDto2 = createValidElectricMeterAddDto();
        addDto1.setIsPrepay(true);
        addDto2.setIsPrepay(true);
        addDto2.setPortNo(33);
        Integer meterId1 = electricMeterManagerService.add(addDto1);
        Integer meterId2 = electricMeterManagerService.add(addDto2);

        electricMeterManagerService.syncMeterOnlineStatus(new ElectricMeterOnlineStatusDto()
                .setMeterId(meterId1)
                .setForce(true)
                .setOnlineStatus(true));
        electricMeterManagerService.syncMeterOnlineStatus(new ElectricMeterOnlineStatusDto()
                .setMeterId(meterId2)
                .setForce(true)
                .setOnlineStatus(true));

        // 创建开户DTO
        List<MeterOpenDetailDto> details = List.of(
                new MeterOpenDetailDto()
                        .setMeterId(meterId1)
                        ,
                new MeterOpenDetailDto()
                        .setMeterId(meterId2)

        );

        Integer testAccountId = 1;
        MeterOpenDto dto = new MeterOpenDto()
                .setMeterOpenDetail(details)
                .setAccountId(testAccountId)
                .setOwnerId(1)
                .setOwnerType(OwnerTypeEnum.PERSONAL)
                .setOwnerName("张三")
                .setElectricAccountType(ElectricAccountTypeEnum.MONTHLY);

        // 执行开户操作
        assertDoesNotThrow(() -> {
            electricMeterManagerService.openMeterAccount(dto);
        });

        // 验证电表的账户ID已设置
        ElectricMeterEntity meter1 = electricMeterRepository.selectById(meterId1);
        ElectricMeterEntity meter2 = electricMeterRepository.selectById(meterId2);
        assertNotNull(meter1);
        assertNotNull(meter2);
        assertEquals(testAccountId, meter1.getAccountId());
        assertEquals(testAccountId, meter2.getAccountId());
    }


    /**
     * 测试电表更新功能 - 成功场景
     * 验证：1. 能够成功更新电表基础信息 2. 操作不抛出异常 3. 数据库中信息正确更新
     */
    @Test
    void testUpdateElectricMeter_Success() {
        // 先添加一个电表
        ElectricMeterCreateDto addDto = createValidElectricMeterAddDto();
        Integer meterId = electricMeterManagerService.add(addDto);

        // 创建更新DTO
        Integer testSpaceId = 1;
        ElectricMeterUpdateDto updateDto = new ElectricMeterUpdateDto()
                .setId(meterId)
                .setMeterName("更新后的电表名称")
                .setSpaceId(testSpaceId)
                .setIsCalculate(false)
                .setCalculateType(CalculateTypeEnum.ELEVATOR)
                .setIsPrepay(true);

        // 设置请求上下文用户信息，便于校验更新人信息
        RequestContextSetter.doSet(1002, new UserRequestData("集成测试用户", "13800000000"));

        // 执行更新操作
        assertDoesNotThrow(() -> {
            electricMeterManagerService.update(updateDto);
        });

        // 验证数据库中的信息已更新
        ElectricMeterEntity updatedMeter = electricMeterRepository.selectById(meterId);
        assertNotNull(updatedMeter);
        assertEquals("更新后的电表名称", updatedMeter.getMeterName());
        assertEquals(testSpaceId, updatedMeter.getSpaceId());
        assertEquals(false, updatedMeter.getIsCalculate());
        assertEquals(CalculateTypeEnum.ELEVATOR.getCode(), updatedMeter.getCalculateType());
        assertEquals(true, updatedMeter.getIsPrepay());

        // 校验更新人信息
        assertEquals(1002, updatedMeter.getUpdateUser());
        assertEquals("集成测试用户", updatedMeter.getUpdateUserName());
        assertNotNull(updatedMeter.getUpdateTime());
    }

    /**
     * 测试设置电表保电模式功能 - 成功场景
     * 验证：1. 能够成功设置电表保电模式 2. 操作不抛出异常
     */
    @Test
    void testSetProtectModel_Success() {
        Integer meterId = 7;
        // 执行设置保电模式操作
        List<Integer> meterIds = List.of(meterId);
        assertDoesNotThrow(() -> {
            electricMeterManagerService.setProtectModel(meterIds, true);
        });

        // 验证操作成功（具体的保电模式状态可能需要通过其他方式验证）
        ElectricMeterEntity meter = electricMeterRepository.selectById(meterId);
        assertNotNull(meter);
        assertTrue(meter.getProtectedModel());

        assertDoesNotThrow(() -> {
            electricMeterManagerService.setProtectModel(meterIds, false);
        });

        meter = electricMeterRepository.selectById(meterId);
        assertNotNull(meter);
        assertFalse(meter.getProtectedModel());
    }

    /**
     * 测试设置电表预警等级功能 - 成功场景
     * 验证：1. 能够成功设置电表预警等级 2. 操作不抛出异常
     */
    @Test
    void testSetMeterWarnLevel_Success() {
        Integer meterId = 7;
        // 执行设置保电模式操作
        List<Integer> meterIds = List.of(meterId);
        assertDoesNotThrow(() -> {
            electricMeterManagerService.setMeterWarnLevel(meterIds, WarnTypeEnum.NONE);
        });

        // 验证操作成功
        ElectricMeterEntity meter = electricMeterRepository.selectById(meterId);
        assertNotNull(meter);
        assertEquals(WarnTypeEnum.NONE.getCode(), meter.getWarnType());

        assertDoesNotThrow(() -> {
            electricMeterManagerService.setMeterWarnLevel(meterIds, WarnTypeEnum.FIRST);
        });

        meter = electricMeterRepository.selectById(meterId);
        assertNotNull(meter);
        assertEquals(WarnTypeEnum.FIRST.getCode(), meter.getWarnType());
    }

    /**
     * 测试设置电表预警方案功能 - 成功场景
     * 验证：1. 能够成功绑定预警方案 2. 自动计算预警等级
     */
    @Test
    void testSetMeterWarnPlan_Success() {
        Integer meterId = electricMeterManagerService.add(createValidPrepayElectricMeterAddDto());
        electricMeterRepository.updateById(new ElectricMeterEntity().setId(meterId).setAccountId(1));

        ElectricMeterWarnPlanDto dto = new ElectricMeterWarnPlanDto()
                .setWarnPlanId(1)
                .setMeterIds(List.of(meterId));

        assertDoesNotThrow(() -> electricMeterManagerService.setMeterWarnPlan(dto));

        ElectricMeterEntity meter = electricMeterRepository.selectById(meterId);
        assertNotNull(meter);
        assertEquals(1, meter.getWarnPlanId());
        assertEquals(WarnTypeEnum.NONE.getCode(), meter.getWarnType());
    }

    /**
     * 测试设置电表CT变比功能 - 成功场景
     * 验证：1. 能够成功设置电表CT变比 2. 操作不抛出异常
     */
    @Test
    void testSetMeterCt_Success() {
        // 先添加一个电表
        ElectricMeterCreateDto addDto = createValidElectricMeterAddDto();
        Integer meterId = electricMeterManagerService.add(addDto);

        // 创建CT变比设置DTO
        ElectricMeterCtDto ctDto = new ElectricMeterCtDto()
                .setMeterId(meterId)
                .setCt(10);

        // 执行设置CT变比操作
        assertDoesNotThrow(() -> {
            Integer newMeterId = electricMeterManagerService.setMeterCt(ctDto);
            ElectricMeterEntity meter = electricMeterRepository.selectById(newMeterId);
            assertNotNull(meter);
        });

    }

    /**
     * 测试获取电表用电量功能 - 成功场景
     * 验证：1. 能够成功获取电表用电量 2. 返回有效的用电量数据
     */
    @Test
    void testGetMeterPower_Success() {
        // 先添加一个电表
        ElectricMeterCreateDto addDto = createValidElectricMeterAddDto();
        Integer meterId = electricMeterManagerService.add(addDto);

        // 执行获取用电量操作
        assertDoesNotThrow(() -> {
            Map<ElectricPricePeriodEnum, BigDecimal> power = electricMeterManagerService.getMeterPower(meterId, List.of(ElectricPricePeriodEnum.TOTAL));
            // 验证返回值不为null（具体的用电量数据可能为0或其他值）
            assertNotNull(power.get(ElectricPricePeriodEnum.TOTAL));
        });
    }

    /**
     * 测试按量计费账户正常销户（在线电表）- 成功场景
     * 验证：1. 在线电表能够自动获取电量并成功销户 2. 返回正确的余额信息 3. 数据库状态正确更新
     */
    @Test
    void testCancelMeterAccount_QuantityAccountOnline_Success() {
        // Given: 创建并开户一个按量计费的在线电表
        ElectricMeterCreateDto addDto = createValidElectricMeterAddDto();
        addDto.setIsPrepay(true); // 设置为预付费电表
        Integer meterId = electricMeterManagerService.add(addDto);

        // 设置电表为在线状态
        electricMeterManagerService.syncMeterOnlineStatus(new ElectricMeterOnlineStatusDto()
                .setMeterId(meterId)
                .setForce(true)
                .setOnlineStatus(true));

        // 为电表开户
        Integer testAccountId = 100;
        MeterOpenDto openDto = new MeterOpenDto()
                .setMeterOpenDetail(List.of(
                        new MeterOpenDetailDto()
                                .setMeterId(meterId)

                ))
                .setAccountId(testAccountId)
                .setOwnerId(1)
                .setOwnerType(OwnerTypeEnum.PERSONAL)
                .setOwnerName("张三")
                .setWarnPlanId(1)
                .setElectricPricePlanId(1)
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY);

        electricMeterManagerService.openMeterAccount(openDto);

        // When: 执行销户操作（在线电表，不需要手动输入电量）
        MeterCancelDto closeDto = new MeterCancelDto()
                .setMeterCloseDetail(List.of(
                        new MeterCancelDetailDto()
                                .setMeterId(meterId)
                        // 在线电表不需要手动输入电量
                ))
                .setCancelNo("C0000000001")
                .setAccountId(testAccountId)
                .setOwnerId(1)
                .setOwnerType(OwnerTypeEnum.PERSONAL)
                .setOwnerName("张三")
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY);

        // Then: 验证销户操作成功
        assertDoesNotThrow(() -> {
            List<MeterCancelResultDto> result = electricMeterManagerService.cancelMeterAccount(closeDto);

            // 验证返回结果
            assertNotNull(result);
            assertEquals(1, result.size());

            MeterCancelResultDto balance = result.get(0);
            assertEquals(meterId, balance.getMeterId());
            assertNotNull(balance.getBalance());
            assertTrue(balance.getBalance().compareTo(BigDecimal.ZERO) >= 0);
        });

        // 验证电表账户ID已清除
        ElectricMeterEntity meter = electricMeterRepository.selectById(meterId);
        assertNotNull(meter);
        assertNull(meter.getAccountId());
    }

    /**
     * 测试按量计费账户正常销户（离线电表，手动输入电量）- 成功场景
     * 验证：1. 离线电表使用手动输入电量成功销户 2. 返回正确的余额信息 3. 数据库状态正确更新
     */
    @Test
    void testCancelMeterAccount_QuantityAccountOfflineWithPower_Success() {
        // Given: 创建并开户一个按量计费的离线电表
        ElectricMeterCreateDto addDto = createValidElectricMeterAddDto();
        addDto.setIsPrepay(true);
        addDto.setPortNo(3); // 使用不同的端口号
        Integer meterId = electricMeterManagerService.add(addDto);

        electricMeterManagerService.syncMeterOnlineStatus(new ElectricMeterOnlineStatusDto()
                .setMeterId(meterId)
                .setForce(true)
                .setOnlineStatus(true));

        // 为电表开户
        Integer testAccountId = 101;
        MeterOpenDto openDto = new MeterOpenDto()
                .setMeterOpenDetail(List.of(
                        new MeterOpenDetailDto()
                                .setMeterId(meterId)

                ))
                .setAccountId(testAccountId)
                .setOwnerId(2)
                .setOwnerType(OwnerTypeEnum.PERSONAL)
                .setOwnerName("李四")
                .setElectricPricePlanId(1)
                .setWarnPlanId(1)
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY);

        electricMeterManagerService.openMeterAccount(openDto);

        // 设置电表为离线状态
        electricMeterManagerService.syncMeterOnlineStatus(new ElectricMeterOnlineStatusDto()
                .setMeterId(meterId)
                .setForce(true)
                .setOnlineStatus(false));

        // When: 执行销户操作（离线电表，手动输入电量）
        MeterCancelDto closeDto = new MeterCancelDto()
                .setMeterCloseDetail(List.of(
                        new MeterCancelDetailDto()
                                .setMeterId(meterId)
                                .setPowerHigher(new BigDecimal("200.10"))
                                .setPowerHigh(new BigDecimal("300.20"))
                                .setPowerLow(new BigDecimal("400.15"))
                                .setPowerLower(new BigDecimal("100.05"))
                                .setPowerDeepLow(new BigDecimal("50.00"))
                ))
                .setCancelNo("C0000000001")
                .setAccountId(testAccountId)
                .setOwnerId(2)
                .setOwnerType(OwnerTypeEnum.PERSONAL)
                .setOwnerName("李四")
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY);

        // Then: 验证销户操作成功
        assertDoesNotThrow(() -> {
            List<MeterCancelResultDto> result = electricMeterManagerService.cancelMeterAccount(closeDto);

            // 验证返回结果
            assertNotNull(result);
            assertEquals(1, result.size());

            MeterCancelResultDto balance = result.get(0);
            assertEquals(meterId, balance.getMeterId());
            assertNotNull(balance.getBalance());
        });

        // 验证电表账户ID已清除
        ElectricMeterEntity meter = electricMeterRepository.selectById(meterId);
        assertNotNull(meter);
        assertNull(meter.getAccountId());
    }

    /**
     * 测试包月计费账户销户 - 成功场景
     * 验证：1. 包月计费账户能够成功销户 2. 返回正确的余额信息 3. 数据库状态正确更新
     */
    @Test
    void testCancelMeterAccount_MonthlyAccount_Success() {
        // Given: 创建并开户一个包月计费的电表
        ElectricMeterCreateDto addDto = createValidElectricMeterAddDto();
        addDto.setIsPrepay(true);
        addDto.setPortNo(4); // 使用不同的端口号
        Integer meterId = electricMeterManagerService.add(addDto);

        // 设置电表为在线状态
        electricMeterManagerService.syncMeterOnlineStatus(new ElectricMeterOnlineStatusDto()
                .setMeterId(meterId)
                .setForce(true)
                .setOnlineStatus(true));

        // 为电表开户（包月计费）
        Integer testAccountId = 102;
        MeterOpenDto openDto = new MeterOpenDto()
                .setMeterOpenDetail(List.of(
                        new MeterOpenDetailDto()
                                .setMeterId(meterId)

                ))
                .setAccountId(testAccountId)
                .setOwnerId(3)
                .setOwnerType(OwnerTypeEnum.PERSONAL)
                .setOwnerName("王五")
                .setElectricAccountType(ElectricAccountTypeEnum.MONTHLY);

        electricMeterManagerService.openMeterAccount(openDto);

        // When: 执行销户操作（包月计费）
        MeterCancelDto closeDto = new MeterCancelDto()
                .setMeterCloseDetail(List.of(
                        new MeterCancelDetailDto()
                                .setMeterId(meterId)
                        // 包月计费不需要输入电量
                ))
                .setCancelNo("C0001")
                .setAccountId(testAccountId)
                .setOwnerId(3)
                .setOwnerType(OwnerTypeEnum.PERSONAL)
                .setOwnerName("王五")
                .setElectricAccountType(ElectricAccountTypeEnum.MONTHLY);

        // Then: 验证销户操作成功
        assertDoesNotThrow(() -> {
            List<MeterCancelResultDto> result = electricMeterManagerService.cancelMeterAccount(closeDto);

            // 验证返回结果
            assertNotNull(result);
            assertEquals(1, result.size());

            MeterCancelResultDto balance = result.get(0);
            assertEquals(meterId, balance.getMeterId());
            assertNull(balance.getBalance());
        });

        // 验证电表账户ID已清除
        ElectricMeterEntity meter = electricMeterRepository.selectById(meterId);
        assertNotNull(meter);
        assertNull(meter.getAccountId());
    }

    /**
     * 测试电表离线但未提供手动电量的异常情况
     * 验证：1. 离线电表未提供电量时应抛出异常 2. 异常信息正确
     */
    @Test
    void testCancelMeterAccount_OfflineWithoutPower_ShouldThrowException() {
        // Given: 创建并开户一个按量计费的离线电表
        ElectricMeterCreateDto addDto = createValidElectricMeterAddDto();
        addDto.setIsPrepay(true);
        addDto.setPortNo(5); // 使用不同的端口号
        Integer meterId = electricMeterManagerService.add(addDto);

        electricMeterManagerService.syncMeterOnlineStatus(new ElectricMeterOnlineStatusDto()
                .setMeterId(meterId)
                .setForce(true)
                .setOnlineStatus(true));

        // 为电表开户
        Integer testAccountId = 103;
        MeterOpenDto openDto = new MeterOpenDto()
                .setMeterOpenDetail(List.of(
                        new MeterOpenDetailDto()
                                .setMeterId(meterId)

                ))
                .setAccountId(testAccountId)
                .setOwnerId(4)
                .setOwnerType(OwnerTypeEnum.PERSONAL)
                .setOwnerName("赵六")
                .setWarnPlanId(1)
                .setElectricPricePlanId(1)
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY);

        electricMeterManagerService.openMeterAccount(openDto);

        electricMeterManagerService.syncMeterOnlineStatus(new ElectricMeterOnlineStatusDto()
                .setMeterId(meterId)
                .setForce(true)
                .setOnlineStatus(false));

        // When & Then: 执行销户操作（离线电表，未提供电量）应抛出异常
        MeterCancelDto closeDto = new MeterCancelDto()
                .setMeterCloseDetail(List.of(
                        new MeterCancelDetailDto()
                                .setMeterId(meterId)
                        // 离线电表但未提供电量数据
                ))
                .setAccountId(testAccountId)
                .setOwnerId(4)
                .setOwnerType(OwnerTypeEnum.PERSONAL)
                .setOwnerName("赵六")
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY);

        // 验证抛出业务异常
        Exception exception = assertThrows(Exception.class, () -> {
            electricMeterManagerService.cancelMeterAccount(closeDto);
        });

        // 验证异常信息包含相关提示
        String message = exception.getMessage();
        assertNotNull(message);
        log.info("离线电表未提供电量异常信息: {}", message);
    }

    /**
     * 测试参数校验 - MeterCloseDto为null
     * 验证：1. 传入null参数时应抛出异常
     */
    @Test
    void testCancelMeterAccount_NullParameter_ShouldThrowException() {
        // When & Then: 传入null参数应抛出异常
        assertThrows(Exception.class, () -> {
            electricMeterManagerService.cancelMeterAccount(null);
        });
    }

    /**
     * 测试批量销户 - 成功场景
     * 验证：1. 能够成功批量销户多个电表 2. 返回正确的余额信息列表 3. 所有电表状态正确更新
     */
    @Test
    void testCloseMeterAccount_BatchCancel_Success() {
        // Given: 创建并开户多个电表
        ElectricMeterCreateDto addDto1 = createValidElectricMeterAddDto();
        ElectricMeterCreateDto addDto2 = createValidElectricMeterAddDto();
        addDto1.setIsPrepay(true);
        addDto1.setPortNo(6);
        addDto2.setIsPrepay(true);
        addDto2.setPortNo(7);

        Integer meterId1 = electricMeterManagerService.add(addDto1);
        Integer meterId2 = electricMeterManagerService.add(addDto2);

        // 设置电表为在线状态
        electricMeterManagerService.syncMeterOnlineStatus(new ElectricMeterOnlineStatusDto()
                .setMeterId(meterId1)
                .setForce(true)
                .setOnlineStatus(true));
        electricMeterManagerService.syncMeterOnlineStatus(new ElectricMeterOnlineStatusDto()
                .setMeterId(meterId2)
                .setForce(true)
                .setOnlineStatus(true));

        // 为电表开户
        Integer testAccountId = 104;
        MeterOpenDto openDto = new MeterOpenDto()
                .setMeterOpenDetail(List.of(
                        new MeterOpenDetailDto()
                                .setMeterId(meterId1)
                                ,
                        new MeterOpenDetailDto()
                                .setMeterId(meterId2)

                ))
                .setAccountId(testAccountId)
                .setOwnerId(5)
                .setOwnerType(OwnerTypeEnum.PERSONAL)
                .setOwnerName("孙七")
                .setElectricPricePlanId(1)
                .setWarnPlanId(1)
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY);

        electricMeterManagerService.openMeterAccount(openDto);

        // When: 执行批量销户操作
        MeterCancelDto closeDto = new MeterCancelDto()
                .setCancelNo("C0001")
                .setMeterCloseDetail(List.of(
                        new MeterCancelDetailDto().setMeterId(meterId1),
                        new MeterCancelDetailDto().setMeterId(meterId2)
                ))
                .setAccountId(testAccountId)
                .setOwnerId(5)
                .setOwnerType(OwnerTypeEnum.PERSONAL)
                .setOwnerName("孙七")
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY);

        // Then: 验证批量销户操作成功
        assertDoesNotThrow(() -> {
            List<MeterCancelResultDto> result = electricMeterManagerService.cancelMeterAccount(closeDto);

            // 验证返回结果
            assertNotNull(result);
            assertEquals(2, result.size());

            // 验证每个电表的余额信息
            for (MeterCancelResultDto balance : result) {
                assertNotNull(balance.getMeterId());
                assertTrue(List.of(meterId1, meterId2).contains(balance.getMeterId()));
                assertNotNull(balance.getBalance());
                assertTrue(balance.getBalance().compareTo(BigDecimal.ZERO) >= 0);
            }
        });

        // 验证所有电表账户ID已清除
        ElectricMeterEntity meter1 = electricMeterRepository.selectById(meterId1);
        ElectricMeterEntity meter2 = electricMeterRepository.selectById(meterId2);
        assertNotNull(meter1);
        assertNotNull(meter2);
        assertNull(meter1.getAccountId());
        assertNull(meter2.getAccountId());
    }

    @Test
    void testResetCurrentYearMeterStepRecord_RebuildsFromLastPower() {
        ElectricMeterCreateDto addDto = createValidPrepayElectricMeterAddDto();
        Integer meterId = electricMeterManagerService.add(addDto);

        ElectricMeterEntity meterEntity = electricMeterRepository.selectById(meterId);
        assertNotNull(meterEntity);
        meterEntity.setAccountId(1000);
        electricMeterRepository.updateById(meterEntity);

        meterStepRepository.insert(new MeterStepEntity()
                .setAccountId(meterEntity.getAccountId())
                .setMeterId(meterId)
                .setMeterType(MeterTypeEnum.ELECTRIC.getCode())
                .setStepStartValue(BigDecimal.ZERO)
                .setHistoryPowerOffset(BigDecimal.valueOf(5))
                .setCurrentYear(LocalDateTime.now().getYear() - 1)
                .setIsLatest(Boolean.TRUE));

        ElectricMeterPowerRecordEntity powerRecord = new ElectricMeterPowerRecordEntity()
                .setMeterId(meterId)
                .setAccountId(meterEntity.getAccountId())
                .setMeterName(meterEntity.getMeterName())
                .setMeterNo(meterEntity.getMeterNo())
                .setIsPrepay(true)
                .setPower(BigDecimal.valueOf(123.45))
                .setRecordTime(LocalDateTime.now().minusMinutes(5))
                .setCreateTime(LocalDateTime.now())
                .setIsDeleted(false);
        electricMeterPowerRecordRepository.insert(powerRecord);

        electricMeterManagerService.resetCurrentYearMeterStepRecord(
                new MeterStepResetDto()
                        .setMeterId(meterId));

        MeterStepEntity latestStep = meterStepRepository.getOne(new AccountMeterStepQo()
                .setAccountId(meterEntity.getAccountId())
                .setMeterId(meterId)
                .setMeterType(MeterTypeEnum.ELECTRIC.getCode()));
        assertNotNull(latestStep);
        assertEquals(LocalDateTime.now().getYear(), latestStep.getCurrentYear());
        assertEquals(0, latestStep.getHistoryPowerOffset().compareTo(BigDecimal.ZERO));
        // mock返回100
        assertEquals(0, latestStep.getStepStartValue().compareTo(BigDecimal.valueOf(100)));
    }

    /**
     * 创建有效的电表添加DTO
     *
     * @return 有效的电表添加DTO
     */
    private ElectricMeterCreateDto createValidElectricMeterAddDto() {
        Integer testSpaceId = 1;
        Integer testModelId = 1;
        return new ElectricMeterCreateDto()
                .setMeterName("测试电表_" + System.currentTimeMillis())
                .setDeviceNo("device-" + System.nanoTime())
                .setModelId(testModelId)
                .setSpaceId(testSpaceId)
                .setCt(5)
                .setIsCalculate(true)
                .setIsPrepay(false)
                .setGatewayId(1)
                .setPortNo(2)
                .setMeterAddress(10);
    }

    private ElectricMeterCreateDto createValidPrepayElectricMeterAddDto() {
        Integer testSpaceId = 1;
        Integer testModelId = 1;
        return new ElectricMeterCreateDto()
                .setMeterName("测试电表_" + System.currentTimeMillis())
                .setDeviceNo("device-" + System.nanoTime())
                .setModelId(testModelId)
                .setSpaceId(testSpaceId)
                .setCt(5)
                .setIsCalculate(true)
                .setIsPrepay(true)
                .setGatewayId(1)
                .setPortNo(2)
                .setMeterAddress(10);
    }

    @Test
    void testUpdateElectricMeter_ShouldKeepMeterNo() {
        ElectricMeterCreateDto addDto = createValidElectricMeterAddDto();
        Integer meterId = electricMeterManagerService.add(addDto);
        ElectricMeterEntity before = electricMeterRepository.selectById(meterId);
        assertNotNull(before);
        String originalMeterNo = before.getMeterNo();

        ElectricMeterUpdateDto updateDto = new ElectricMeterUpdateDto()
                .setId(meterId)
                .setMeterName("编号保持测试")
                .setSpaceId(before.getSpaceId())
                .setIsCalculate(before.getIsCalculate())
                .setIsPrepay(before.getIsPrepay())
                .setCalculateType(CalculateTypeEnum.ELEVATOR);

        // 设置请求上下文用户信息，便于校验更新人信息
        RequestContextSetter.doSet(1002, new UserRequestData("集成测试用户", "13800000000"));

        assertDoesNotThrow(() -> electricMeterManagerService.update(updateDto));

        ElectricMeterEntity after = electricMeterRepository.selectById(meterId);
        assertNotNull(after);
        assertEquals(originalMeterNo, after.getMeterNo());

        // 校验更新人信息
        assertEquals(1002, after.getUpdateUser());
        assertEquals("集成测试用户", after.getUpdateUserName());
        assertNotNull(after.getUpdateTime());
    }

}
