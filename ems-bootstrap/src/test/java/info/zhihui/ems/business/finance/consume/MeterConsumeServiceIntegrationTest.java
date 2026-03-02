package info.zhihui.ems.business.finance.consume;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import info.zhihui.ems.business.finance.dto.ElectricMeterDetailDto;
import info.zhihui.ems.business.finance.dto.ElectricMeterPowerRecordDto;
import info.zhihui.ems.business.finance.dto.PowerConsumeDetailDto;
import info.zhihui.ems.business.finance.dto.PowerConsumeQueryDto;
import info.zhihui.ems.business.finance.dto.PowerConsumeRecordDto;
import info.zhihui.ems.business.finance.entity.ElectricMeterBalanceConsumeRecordEntity;
import info.zhihui.ems.business.finance.entity.ElectricMeterPowerConsumeRecordEntity;
import info.zhihui.ems.business.finance.entity.ElectricMeterPowerRecordEntity;
import info.zhihui.ems.business.finance.entity.ElectricMeterPowerRelationEntity;
import info.zhihui.ems.business.finance.enums.ConsumeTypeEnum;
import info.zhihui.ems.business.finance.qo.ElectricMeterPowerRecordQo;
import info.zhihui.ems.business.finance.repository.ElectricMeterBalanceConsumeRecordRepository;
import info.zhihui.ems.business.finance.repository.ElectricMeterPowerConsumeRecordRepository;
import info.zhihui.ems.business.finance.repository.ElectricMeterPowerRecordRepository;
import info.zhihui.ems.business.finance.repository.ElectricMeterPowerRelationRepository;
import info.zhihui.ems.business.finance.service.consume.MeterConsumeService;
import info.zhihui.ems.common.enums.CalculateTypeEnum;
import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import info.zhihui.ems.common.enums.OwnerTypeEnum;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MeterConsumeService 相关集成测试
 */
@SpringBootTest
@ActiveProfiles("integrationtest")
@Transactional
class MeterConsumeServiceIntegrationTest {

    @Autowired
    private MeterConsumeService meterConsumeService;

    @Autowired
    private ElectricMeterPowerRecordRepository electricMeterPowerRecordRepository;

    @Autowired
    private ElectricMeterPowerRelationRepository electricMeterPowerRelationRepository;

    @Autowired
    private ElectricMeterPowerConsumeRecordRepository electricMeterPowerConsumeRecordRepository;

    @Autowired
    private ElectricMeterBalanceConsumeRecordRepository electricMeterBalanceConsumeRecordRepository;

    private ElectricMeterPowerRecordDto testDto;
    private ElectricMeterDetailDto meterDetailDto;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        meterDetailDto = new ElectricMeterDetailDto()
                .setMeterId(1)
                .setMeterName("1号楼电表")
                .setDeviceNo("EM001")
                .setSpaceId(101)
                .setIsCalculate(true)
                .setCalculateType(CalculateTypeEnum.AIR_CONDITIONING)
                .setIsPrepay(false)
                .setPricePlanId(1)
                .setCt(1)
                .setStepStartValue(BigDecimal.ZERO);

        testDto = new ElectricMeterPowerRecordDto()
                .setElectricMeterDetailDto(meterDetailDto)
                .setAccountId(1)
                .setOwnerId(1001)
                .setOwnerType(OwnerTypeEnum.PERSONAL)
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY)
                .setPower(new BigDecimal("200.50"))
                .setPowerHigher(new BigDecimal("20.0"))
                .setPowerHigh(new BigDecimal("30.0"))
                .setPowerLow(new BigDecimal("40.0"))
                .setPowerLower(new BigDecimal("50.0"))
                .setPowerDeepLow(new BigDecimal("60.50"))
                .setOriginalReportId("TEST_REPORT_001")
                .setRecordTime(LocalDateTime.now())
                .setNeedConsume(true);

    }

    @Test
    @DisplayName("参数校验测试 - 覆盖所有@NotNull注解字段")
    void testSavePowerRecord_ValidationTests_ShouldThrowException() {
        // 测试1: null参数
        assertThrows(ConstraintViolationException.class, () -> {
            meterConsumeService.savePowerRecord(null);
        }, "null参数应抛出ConstraintViolationException");

        // 测试2: electricMeterDetailAndPowerDto为null
        ElectricMeterPowerRecordDto dto1 = createValidTestDto();
        dto1.setElectricMeterDetailDto(null);
        assertThrows(ConstraintViolationException.class, () -> {
            meterConsumeService.savePowerRecord(dto1);
        }, "electricMeterDetailAndPowerDto为null应抛出异常");

        // 测试3: originalReportId为null
        ElectricMeterPowerRecordDto dto2 = createValidTestDto();
        dto2.setOriginalReportId(null);
        assertThrows(ConstraintViolationException.class, () -> {
            meterConsumeService.savePowerRecord(dto2);
        }, "originalReportId为null应抛出异常");

        // 测试8: recordTime为null
        ElectricMeterPowerRecordDto dto7 = createValidTestDto();
        dto7.setRecordTime(null);
        assertThrows(ConstraintViolationException.class, () -> {
            meterConsumeService.savePowerRecord(dto7);
        }, "recordTime为null应抛出异常");

        // 测试9: needConsume为null
        ElectricMeterPowerRecordDto dto8 = createValidTestDto();
        dto8.setNeedConsume(null);
        assertThrows(ConstraintViolationException.class, () -> {
            meterConsumeService.savePowerRecord(dto8);
        }, "needConsume为null应抛出异常");

        // 测试10: ElectricMeterDetailAndPowerDto.meterId为null
        ElectricMeterPowerRecordDto dto9 = createValidTestDto();
        dto9.getElectricMeterDetailDto().setMeterId(null);
        assertThrows(ConstraintViolationException.class, () -> {
            meterConsumeService.savePowerRecord(dto9);
        }, "meterId为null应抛出异常");

        // 测试11: ElectricMeterDetailAndPowerDto.meterName为null
        ElectricMeterPowerRecordDto dto10 = createValidTestDto();
        dto10.getElectricMeterDetailDto().setMeterName(null);
        assertThrows(ConstraintViolationException.class, () -> {
            meterConsumeService.savePowerRecord(dto10);
        }, "meterName为null应抛出异常");

        // 测试12: ElectricMeterDetailAndPowerDto.deviceNo为null
        ElectricMeterPowerRecordDto dto11 = createValidTestDto();
        dto11.getElectricMeterDetailDto().setDeviceNo(null);
        assertThrows(ConstraintViolationException.class, () -> {
            meterConsumeService.savePowerRecord(dto11);
        }, "deviceNo为null应抛出异常");

        // 测试13: ElectricMeterDetailAndPowerDto.spaceId为null
        ElectricMeterPowerRecordDto dto12 = createValidTestDto();
        dto12.getElectricMeterDetailDto().setSpaceId(null);
        assertThrows(ConstraintViolationException.class, () -> {
            meterConsumeService.savePowerRecord(dto12);
        }, "spaceId为null应抛出异常");

        // 测试14: ElectricMeterDetailAndPowerDto.isCalculate为null
        ElectricMeterPowerRecordDto dto13 = createValidTestDto();
        dto13.getElectricMeterDetailDto().setIsCalculate(null);
        assertThrows(ConstraintViolationException.class, () -> {
            meterConsumeService.savePowerRecord(dto13);
        }, "isCalculate为null应抛出异常");

        // 测试15: ElectricMeterDetailAndPowerDto.isPrepay为null
        ElectricMeterPowerRecordDto dto14 = createValidTestDto();
        dto14.getElectricMeterDetailDto().setIsPrepay(null);
        assertThrows(ConstraintViolationException.class, () -> {
            meterConsumeService.savePowerRecord(dto14);
        }, "isPrepay为null应抛出异常");
    }

    @Test
    @DisplayName("正常保存电表记录测试")
    void testSavePowerRecord_NormalSave_ShouldSuccess() {
        // 执行保存
        assertDoesNotThrow(() -> {
            meterConsumeService.savePowerRecord(testDto);
        });

        // 验证电表记录已保存
        List<ElectricMeterPowerRecordEntity> records = electricMeterPowerRecordRepository.findRecordList(
                new ElectricMeterPowerRecordQo()
                        .setMeterId(1)
                        .setAccountId(1)
                        .setLimit(10)
        );

        assertFalse(records.isEmpty());
        ElectricMeterPowerRecordEntity savedRecord = records.get(0);
        assertEquals(testDto.getElectricMeterDetailDto().getMeterId(), savedRecord.getMeterId());
        assertEquals(testDto.getPower(), savedRecord.getPower());
        assertEquals(testDto.getOriginalReportId(), savedRecord.getOriginalReportId());

        // 验证电表关系记录已保存
        List<ElectricMeterPowerRelationEntity> relations = electricMeterPowerRelationRepository.selectList(null);
        assertFalse(relations.isEmpty());

        ElectricMeterPowerRelationEntity relation = relations.stream()
                .filter(r -> r.getRecordId().equals(savedRecord.getId()))
                .findFirst()
                .orElse(null);
        assertNotNull(relation);
        assertEquals(testDto.getElectricMeterDetailDto().getMeterId(), relation.getMeterId());
        assertEquals(testDto.getAccountId(), relation.getAccountId());
    }

    @Test
    @DisplayName("首次上报数据测试 - 应跳过消费计算")
    void testSavePowerRecord_FirstTimeReport_ShouldSkipConsumeCalculation() {
        // 使用新的电表ID确保是首次上报
        meterDetailDto.setMeterId(999);
        testDto.setElectricMeterDetailDto(meterDetailDto);

        // 执行保存
        assertDoesNotThrow(() -> {
            meterConsumeService.savePowerRecord(testDto);
        });

        // 验证电表记录已保存
        List<ElectricMeterPowerRecordEntity> records = electricMeterPowerRecordRepository.findRecordList(
                new ElectricMeterPowerRecordQo()
                        .setMeterId(999)
                        .setAccountId(1)
                        .setLimit(10)
        );
        assertFalse(records.isEmpty());

        // 验证没有生成消费记录（首次上报应跳过）
        List<ElectricMeterPowerConsumeRecordEntity> consumeRecords =
                electricMeterPowerConsumeRecordRepository.selectList(null);

        boolean hasConsumeRecord = consumeRecords.stream()
                .anyMatch(record -> record.getMeterId().equals(999));
        assertFalse(hasConsumeRecord, "首次上报不应生成消费记录");
    }

    @Test
    @DisplayName("消费计算测试 - 第二次上报应生成消费记录")
    void testSavePowerRecord_ConsumeCalculation_ShouldGenerateConsumeRecord() {
        // 第一次上报
        ElectricMeterPowerRecordDto firstDto = createTestDto(888, new BigDecimal("100.0"));
        meterConsumeService.savePowerRecord(firstDto);

        // 第二次上报（更大的电量值）
        ElectricMeterPowerRecordDto secondDto = createTestDto(888, new BigDecimal("150.0"));
        secondDto.setRecordTime(LocalDateTime.now().plusHours(1));
        meterConsumeService.savePowerRecord(secondDto);

        // 验证生成了消费记录
        List<ElectricMeterPowerConsumeRecordEntity> consumeRecords =
                electricMeterPowerConsumeRecordRepository.selectList(null);

        ElectricMeterPowerConsumeRecordEntity consumeRecord = consumeRecords.stream()
                .filter(record -> record.getMeterId().equals(888))
                .findFirst()
                .orElse(null);

        assertNotNull(consumeRecord, "应该生成消费记录");
        assertEquals(new BigDecimal("50.00"), consumeRecord.getConsumePower());
        assertEquals(new BigDecimal("100.00"), consumeRecord.getBeginPower());
        assertEquals(new BigDecimal("150.00"), consumeRecord.getEndPower());
    }

    @Test
    @DisplayName("预付费电表余额扣费测试")
    void testSavePowerRecord_PrepayMeter_ShouldDeductBalance() {
        // 设置为预付费电表
        meterDetailDto.setIsPrepay(true);
        meterDetailDto.setMeterId(777);
        testDto.setElectricMeterDetailDto(meterDetailDto);
        testDto.setElectricAccountType(ElectricAccountTypeEnum.QUANTITY);

        // 第一次上报
        ElectricMeterPowerRecordDto firstDto = createTestDto(777, new BigDecimal("100.0"));
        firstDto.getElectricMeterDetailDto().setIsPrepay(true);
        meterConsumeService.savePowerRecord(firstDto);

        // 第二次上报（更大的电量值）
        ElectricMeterPowerRecordDto secondDto = createTestDto(777, new BigDecimal("150.0"));
        secondDto.getElectricMeterDetailDto().setIsPrepay(true);
        secondDto.setRecordTime(LocalDateTime.now().plusHours(1));
        secondDto.setElectricAccountType(ElectricAccountTypeEnum.QUANTITY);

        meterConsumeService.savePowerRecord(secondDto);

        // 验证生成了余额消费记录
        List<ElectricMeterBalanceConsumeRecordEntity> balanceRecords = electricMeterBalanceConsumeRecordRepository.selectList(null);

        ElectricMeterBalanceConsumeRecordEntity balanceRecord = balanceRecords.stream()
                .filter(record -> record.getMeterId().equals(777))
                .findFirst()
                .orElse(null);

        assertNotNull(balanceRecord, "预付费电表应该生成余额消费记录");
        assertNotNull(balanceRecord.getConsumeAmount());
        assertTrue(balanceRecord.getConsumeAmount().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("非预付费电表测试 - 应跳过余额扣费")
    void testSavePowerRecord_NonPrepayMeter_ShouldSkipBalanceDeduction() {
        // 设置为非预付费电表
        meterDetailDto.setIsPrepay(false);
        meterDetailDto.setMeterId(666);
        testDto.setElectricMeterDetailDto(meterDetailDto);

        // 第一次上报
        ElectricMeterPowerRecordDto firstDto = createTestDto(666, new BigDecimal("100.0"));
        firstDto.getElectricMeterDetailDto().setIsPrepay(false);
        meterConsumeService.savePowerRecord(firstDto);

        // 第二次上报
        ElectricMeterPowerRecordDto secondDto = createTestDto(666, new BigDecimal("150.0"));
        secondDto.getElectricMeterDetailDto().setIsPrepay(false);
        secondDto.setRecordTime(LocalDateTime.now().plusHours(1));
        meterConsumeService.savePowerRecord(secondDto);

        // 验证没有生成余额消费记录
        List<ElectricMeterBalanceConsumeRecordEntity> balanceRecords = electricMeterBalanceConsumeRecordRepository.selectList(null);

        boolean hasBalanceRecord = balanceRecords.stream()
                .anyMatch(record -> record.getMeterId().equals(666));
        assertFalse(hasBalanceRecord, "非预付费电表不应生成余额消费记录");
    }

    @Test
    @DisplayName("跳过消费计算测试 - needConsume为false")
    void testSavePowerRecord_SkipConsume_WhenNeedConsumeIsFalse() {
        List<ElectricMeterPowerConsumeRecordEntity> consumeBeginRecords =
                electricMeterPowerConsumeRecordRepository.selectList(null);

        testDto.setNeedConsume(false);
        meterDetailDto.setMeterId(555);
        testDto.setElectricMeterDetailDto(meterDetailDto);

        // 执行保存
        assertDoesNotThrow(() -> {
            meterConsumeService.savePowerRecord(testDto);
        });

        // 验证电表记录已保存
        List<ElectricMeterPowerRecordEntity> records = electricMeterPowerRecordRepository.findRecordList(
                new ElectricMeterPowerRecordQo()
                        .setMeterId(555)
                        .setAccountId(1)
                        .setLimit(10)
        );
        assertFalse(records.isEmpty());

        // 验证没有生成消费记录
        List<ElectricMeterPowerConsumeRecordEntity> consumeEndRecords =
                electricMeterPowerConsumeRecordRepository.selectList(null);

        assertEquals(consumeBeginRecords, consumeEndRecords, "needConsume为false时不应生成消费记录");
    }

    @Test
    @DisplayName("并发锁测试 - 同一电表并发保存应有序执行")
    void testSavePowerRecord_ConcurrentLock_ShouldExecuteSequentially() throws InterruptedException {
        final int threadCount = 5;
        final Integer meterId = 444;
        final CountDownLatch latch = new CountDownLatch(threadCount);
        final AtomicInteger successCount = new AtomicInteger(0);
        final AtomicInteger failCount = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        // 创建多个并发任务
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    ElectricMeterPowerRecordDto concurrentDto = createTestDto(meterId,
                            new BigDecimal("100.0").add(new BigDecimal(index)));
                    concurrentDto.setOriginalReportId("CONCURRENT_" + index);
                    concurrentDto.setRecordTime(LocalDateTime.now().plusMinutes(index));

                    meterConsumeService.savePowerRecord(concurrentDto);
                    successCount.incrementAndGet();
                } catch (BusinessRuntimeException e) {
                    if (e.getMessage().contains("正在保存电表记录，请稍后重试")) {
                        failCount.incrementAndGet();
                    } else {
                        throw e;
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        // 等待所有任务完成
        latch.await();
        executor.shutdown();

        // 验证结果
        assertTrue(successCount.get() > 0, "至少应有一个任务成功");
        assertEquals(threadCount, successCount.get() + failCount.get(), "成功数 + 失败数应等于总任务数");

        // 验证成功保存的记录数
        List<ElectricMeterPowerRecordEntity> records = electricMeterPowerRecordRepository.findRecordList(
                new ElectricMeterPowerRecordQo()
                        .setMeterId(meterId)
                        .setAccountId(1)
                        .setLimit(10)
        );

        assertEquals(successCount.get(), records.size(), "保存的记录数应等于成功任务数");
    }

    /**
     * 创建有效的测试DTO（用于参数校验测试）
     */
    private ElectricMeterPowerRecordDto createValidTestDto() {
        ElectricMeterDetailDto meterDetail = new ElectricMeterDetailDto()
                .setMeterId(1)
                .setMeterName("测试电表")
                .setDeviceNo("EM001")
                .setSpaceId(101)
                .setIsCalculate(true)
                .setCalculateType(CalculateTypeEnum.AIR_CONDITIONING)
                .setIsPrepay(false)
                .setPricePlanId(1)
                .setCt(1)
                .setStepStartValue(BigDecimal.ZERO);

        return new ElectricMeterPowerRecordDto()
                .setElectricMeterDetailDto(meterDetail)
                .setAccountId(1)
                .setOwnerId(1001)
                .setOwnerType(OwnerTypeEnum.PERSONAL)
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY)
                .setPower(new BigDecimal("100.0"))
                .setPowerHigher(new BigDecimal("10.0"))
                .setPowerHigh(new BigDecimal("15.0"))
                .setPowerLow(new BigDecimal("20.0"))
                .setPowerLower(new BigDecimal("25.0"))
                .setPowerDeepLow(new BigDecimal("30.0"))
                .setOriginalReportId("TEST_REPORT_VALID")
                .setRecordTime(LocalDateTime.now())
                .setNeedConsume(true);
    }

    /**
     * 创建测试DTO的辅助方法
     */
    private ElectricMeterPowerRecordDto createTestDto(Integer meterId, BigDecimal power) {
        ElectricMeterDetailDto meterDetail = new ElectricMeterDetailDto()
                .setMeterId(meterId)
                .setMeterName("测试电表_" + meterId)
                .setDeviceNo("EM" + String.format("%03d", meterId))
                .setSpaceId(101)
                .setIsCalculate(true)
                .setCalculateType(CalculateTypeEnum.AIR_CONDITIONING)
                .setIsPrepay(false)
                .setPricePlanId(1)
                .setCt(1)
                .setStepStartValue(BigDecimal.ZERO);

        return new ElectricMeterPowerRecordDto()
                .setElectricMeterDetailDto(meterDetail)
                .setAccountId(1)
                .setOwnerId(1001)
                .setOwnerType(OwnerTypeEnum.PERSONAL)
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY)
                .setPower(power)
                .setPowerHigher(power.multiply(new BigDecimal("0.1")))
                .setPowerHigh(power.multiply(new BigDecimal("0.15")))
                .setPowerLow(power.multiply(new BigDecimal("0.2")))
                .setPowerLower(power.multiply(new BigDecimal("0.25")))
                .setPowerDeepLow(power.multiply(new BigDecimal("0.3")))
                .setOriginalReportId("TEST_REPORT_" + meterId)
                .setRecordTime(LocalDateTime.now())
                .setNeedConsume(true);
    }

    @Test
    @DisplayName("findPowerConsumePage方法集成测试 - 参数校验测试")
    void testFindPowerConsumePage_ValidationTests_ShouldThrowException() {
        // 测试1: queryDto为null
        assertThrows(ConstraintViolationException.class, () -> {
            meterConsumeService.findPowerConsumePage(null, new PageParam().setPageNum(1).setPageSize(10));
        }, "queryDto为null应抛出ConstraintViolationException");

        // 测试2: pageParam为null
        assertThrows(ConstraintViolationException.class, () -> {
            meterConsumeService.findPowerConsumePage(new PowerConsumeQueryDto(), null);
        }, "pageParam为null应抛出ConstraintViolationException");

        // 测试3: 有效参数（不应抛出参数校验异常）
        PowerConsumeQueryDto queryDto = new PowerConsumeQueryDto().setBeginTime(LocalDateTime.now().minusHours(1)).setEndTime(LocalDateTime.now());
        PageParam pageParam = new PageParam().setPageNum(1).setPageSize(10);
        try {
            meterConsumeService.findPowerConsumePage(queryDto, pageParam);
        } catch (ConstraintViolationException e) {
            throw new AssertionError("有效参数不应抛出ConstraintViolationException", e);
        } catch (Exception e) {
            // 其他异常（如业务异常）是可以接受的，我们只关心参数校验
        }
    }

    @Test
    @DisplayName("findPowerConsumePage方法集成测试 - 正常分页查询测试")
    void testFindPowerConsumePage_Success() {
        // 准备测试数据
        prepareTestDataForPowerConsumePage();

        // 准备查询参数
        PowerConsumeQueryDto queryDto = new PowerConsumeQueryDto()
                .setBeginTime(LocalDateTime.now().minusYears(1))
                .setEndTime(LocalDateTime.now().plusYears(1));
        PageParam pageParam = new PageParam().setPageNum(1).setPageSize(10);

        // 执行测试
        PageResult<PowerConsumeRecordDto> result = meterConsumeService.findPowerConsumePage(queryDto, pageParam);

        // 验证分页结果
        assertNotNull(result, "分页查询结果不应为null");
        assertNotNull(result.getList(), "分页数据列表不应为null");
        assertFalse(result.getList().isEmpty(), "分页数据列表不应为空");
        assertEquals(3L, result.getTotal(), "总数应为3");
        assertEquals(1, result.getPageNum(), "页码应为1");
        assertEquals(10, result.getPageSize(), "页大小应为10");

        // 验证数据转换的正确性
        PowerConsumeRecordDto firstRecord = result.getList().get(0);
        assertNotNull(firstRecord.getMeterName(), "电表名称不应为null");
        assertNotNull(firstRecord.getDeviceNo(), "电表编号不应为null");
        assertNotNull(firstRecord.getSpaceName(), "空间名称不应为null");
        assertNotNull(firstRecord.getBeginBalance(), "开始余额不应为null");
        assertNotNull(firstRecord.getConsumeAmount(), "消费金额不应为null");
        assertNotNull(firstRecord.getEndBalance(), "结束余额不应为null");
        assertNotNull(firstRecord.getConsumeTime(), "消费时间不应为null");
        assertNotNull(firstRecord.getMergedMeasure(), "合并计量标识不应为null");

        // 验证字符串字段不为空
        assertFalse(firstRecord.getMeterName().trim().isEmpty(), "电表名称不应为空字符串");
        assertFalse(firstRecord.getDeviceNo().trim().isEmpty(), "电表编号不应为空字符串");
        assertFalse(firstRecord.getSpaceName().trim().isEmpty(), "空间名称不应为空字符串");

        // 验证mergedMeasure字段的派生逻辑
        boolean foundQuantityType = false;
        boolean foundMergedType = false;
        for (PowerConsumeRecordDto record : result.getList()) {
            if (!record.getMergedMeasure()) {
                foundQuantityType = true;
            } else {
                foundMergedType = true;
            }
        }
        assertTrue(foundQuantityType, "应该找到QUANTITY类型的记录（mergedMeasure=false）");
        assertTrue(foundMergedType, "应该找到MERGED类型的记录（mergedMeasure=true）");
    }

    @Test
    @DisplayName("findPowerConsumePage方法集成测试 - 空结果查询测试")
    void testFindPowerConsumePage_EmptyResult() {
        // 准备测试数据
        prepareTestDataForPowerConsumePage();

        // 准备查询参数 - 使用不存在的查询条件
        PowerConsumeQueryDto queryDto = new PowerConsumeQueryDto()
                .setBeginTime(LocalDateTime.now().minusYears(1))
                .setEndTime(LocalDateTime.now().plusYears(1))
                .setMeterName("不存在的电表名称")
                .setSpaceName("不存在的空间名称");
        PageParam pageParam = new PageParam().setPageNum(1).setPageSize(10);

        // 执行测试
        PageResult<PowerConsumeRecordDto> result = meterConsumeService.findPowerConsumePage(queryDto, pageParam);

        // 验证空结果
        assertNotNull(result, "分页查询结果不应为null");
        assertNotNull(result.getList(), "分页数据列表不应为null");
        assertTrue(result.getList().isEmpty(), "应该返回空列表");
        assertEquals(0L, result.getTotal(), "总数应为0");
        assertEquals(1, result.getPageNum(), "页码应为1");
        assertEquals(10, result.getPageSize(), "页大小应为10");
    }

    @Test
    @DisplayName("findPowerConsumePage方法集成测试 - 查询条件测试")
    void testFindPowerConsumePage_QueryConditions() {
        // 准备测试数据
        prepareTestDataForPowerConsumePage();

        // 测试1: 按电表名称模糊查询
        PowerConsumeQueryDto queryDto1 = new PowerConsumeQueryDto()
                .setBeginTime(LocalDateTime.now().minusYears(1))
                .setEndTime(LocalDateTime.now().plusYears(1))
                .setMeterName("1号楼");
        PageParam pageParam = new PageParam().setPageNum(1).setPageSize(10);

        PageResult<PowerConsumeRecordDto> result1 = meterConsumeService.findPowerConsumePage(queryDto1, pageParam);
        assertNotNull(result1, "查询结果不应为null");
        assertEquals(1L, result1.getTotal(), "按电表名称查询应返回1条记录");
        assertEquals("1号楼电表", result1.getList().get(0).getMeterName(), "应该查询到正确的电表");

        // 测试2: 按空间名称模糊查询
        PowerConsumeQueryDto queryDto2 = new PowerConsumeQueryDto()
                .setBeginTime(LocalDateTime.now().minusYears(1))
                .setEndTime(LocalDateTime.now().plusYears(1))
                .setSpaceName("办公室");
        PageResult<PowerConsumeRecordDto> result2 = meterConsumeService.findPowerConsumePage(queryDto2, pageParam);
        assertNotNull(result2, "查询结果不应为null");
        assertEquals(1L, result2.getTotal(), "按空间名称查询应返回1条记录");
        assertEquals("2号楼办公室", result2.getList().get(0).getSpaceName(), "应该查询到正确的空间");

        // 测试3: 按时间范围查询
        PowerConsumeQueryDto queryDto3 = new PowerConsumeQueryDto()
                .setBeginTime(LocalDateTime.now().minusYears(2))
                .setEndTime(LocalDateTime.now().plusYears(1));
        PageResult<PowerConsumeRecordDto> result3 = meterConsumeService.findPowerConsumePage(queryDto3, pageParam);
        assertNotNull(result3, "查询结果不应为null");
        assertEquals(3L, result3.getTotal(), "按时间范围查询应返回2条记录");

    }

    @Test
    @DisplayName("findPowerConsumePage方法集成测试 - 分页参数测试")
    void testFindPowerConsumePage_Pagination() {
        // 准备测试数据
        prepareTestDataForPowerConsumePage();

        // 测试第一页
        PowerConsumeQueryDto queryDto = new PowerConsumeQueryDto()
                .setBeginTime(LocalDateTime.now().minusYears(1))
                .setEndTime(LocalDateTime.now().plusYears(1));
        PageParam firstPageParam = new PageParam().setPageNum(1).setPageSize(2);

        PageResult<PowerConsumeRecordDto> firstPageResult = meterConsumeService.findPowerConsumePage(queryDto, firstPageParam);

        // 验证第一页结果
        assertNotNull(firstPageResult, "第一页结果不应为null");
        assertNotNull(firstPageResult.getList(), "第一页数据列表不应为null");
        assertEquals(2, firstPageResult.getList().size(), "第一页应返回2条记录");
        assertEquals(3L, firstPageResult.getTotal(), "总数应为3");
        assertEquals(1, firstPageResult.getPageNum(), "页码应为1");
        assertEquals(2, firstPageResult.getPageSize(), "页大小应为2");

        // 测试第二页
        PageParam secondPageParam = new PageParam().setPageNum(2).setPageSize(2);
        PageResult<PowerConsumeRecordDto> secondPageResult = meterConsumeService.findPowerConsumePage(queryDto, secondPageParam);

        // 验证第二页结果
        assertNotNull(secondPageResult, "第二页结果不应为null");
        assertNotNull(secondPageResult.getList(), "第二页数据列表不应为null");
        assertEquals(1, secondPageResult.getList().size(), "第二页应返回1条记录");
        assertEquals(3L, secondPageResult.getTotal(), "总数应为3");
        assertEquals(2, secondPageResult.getPageNum(), "页码应为2");
        assertEquals(2, secondPageResult.getPageSize(), "页大小应为2");

        // 验证两页的总数相同
        assertEquals(firstPageResult.getTotal(), secondPageResult.getTotal(), "两页的总数应该相同");

        // 测试超出范围的页码
        PageParam thirdPageParam = new PageParam().setPageNum(3).setPageSize(2);
        PageResult<PowerConsumeRecordDto> thirdPageResult = meterConsumeService.findPowerConsumePage(queryDto, thirdPageParam);

        assertNotNull(thirdPageResult, "第三页结果不应为null");
        assertNotNull(thirdPageResult.getList(), "第三页数据列表不应为null");
        assertTrue(thirdPageResult.getList().isEmpty(), "第三页应返回空列表");
        assertEquals(3L, thirdPageResult.getTotal(), "总数应为3");
    }

    @Test
    @DisplayName("findPowerConsumePage方法集成测试 - mergedMeasure字段派生逻辑验证")
    void testFindPowerConsumePage_MergedMeasureLogic() {
        // 准备测试数据
        prepareTestDataForPowerConsumePage();

        // 查询所有数据
        PowerConsumeQueryDto queryDto = new PowerConsumeQueryDto()
                .setBeginTime(LocalDateTime.now().minusYears(1))
                .setEndTime(LocalDateTime.now().plusYears(1));
        PageParam pageParam = new PageParam().setPageNum(1).setPageSize(10);

        PageResult<PowerConsumeRecordDto> result = meterConsumeService.findPowerConsumePage(queryDto, pageParam);

        // 验证mergedMeasure字段的派生逻辑
        for (PowerConsumeRecordDto record : result.getList()) {
            if ("1号楼电表".equals(record.getMeterName()) || "3号楼智能电表".equals(record.getMeterName())) {
                // QUANTITY类型应该是非合并计量
                assertFalse(record.getMergedMeasure(),
                    "QUANTITY类型的电表(" + record.getMeterName() + ")应该是非合并计量(mergedMeasure=false)");
            } else if ("2号楼电表".equals(record.getMeterName())) {
                // MERGED类型应该是合并计量
                assertTrue(record.getMergedMeasure(),
                    "MERGED类型的电表(" + record.getMeterName() + ")应该是合并计量(mergedMeasure=true)");
            }
        }
    }

    @Test
    @DisplayName("getPowerConsumeDetail方法集成测试 - 参数校验测试")
    void testGetPowerConsumeDetail_Validation_ShouldThrowException() {
        assertThrows(ConstraintViolationException.class, () -> meterConsumeService.getPowerConsumeDetail(null));
    }

    @Test
    @DisplayName("getPowerConsumeDetail方法集成测试 - 正常查询测试")
    void testGetPowerConsumeDetail_Success() {
        prepareTestDataForPowerConsumeDetail();

        PowerConsumeDetailDto detailDto = meterConsumeService.getPowerConsumeDetail(3001);

        assertNotNull(detailDto);
        assertEquals(3001, detailDto.getId());
        assertEquals(4001, detailDto.getMeterConsumeRecordId());
        assertEquals("DETAIL_CONSUME_001", detailDto.getConsumeNo());
        assertEquals(101, detailDto.getMeterId());
        assertEquals("明细测试电表", detailDto.getMeterName());
        assertEquals(0, detailDto.getConsumePower().compareTo(new BigDecimal("12.50")));
        assertEquals(0, detailDto.getConsumeAmount().compareTo(new BigDecimal("8.75")));
        assertEquals(0, detailDto.getEndPower().compareTo(new BigDecimal("105.00")));
        assertEquals(0, detailDto.getEndBalance().compareTo(new BigDecimal("191.25")));
        assertNotNull(detailDto.getConsumeTime());
    }

    @Test
    @DisplayName("getPowerConsumeDetail方法集成测试 - 记录不存在测试")
    void testGetPowerConsumeDetail_NotFound_ShouldThrowException() {
        prepareTestDataForPowerConsumeDetail();

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> meterConsumeService.getPowerConsumeDetail(999999));
        assertEquals("电量消费记录不存在", exception.getMessage());
    }

    @Test
    @DisplayName("getPowerConsumeDetail方法集成测试 - 非电量消费记录测试")
    void testGetPowerConsumeDetail_NotElectricConsume_ShouldThrowException() {
        prepareTestDataForPowerConsumeDetail();

        ElectricMeterBalanceConsumeRecordEntity nonElectricRecord = new ElectricMeterBalanceConsumeRecordEntity()
                .setId(3002)
                .setMeterConsumeRecordId(4001)
                .setConsumeNo("DETAIL_CONSUME_002")
                .setConsumeType(ConsumeTypeEnum.CORRECTION.getCode())
                .setMeterType(1)
                .setAccountId(1)
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY.getCode())
                .setMeterId(101)
                .setMeterName("明细测试电表")
                .setDeviceNo("EM-DETAIL-001")
                .setCreateTime(LocalDateTime.now())
                .setIsDeleted(false);
        electricMeterBalanceConsumeRecordRepository.insert(nonElectricRecord);

        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> meterConsumeService.getPowerConsumeDetail(3002));
        assertEquals("当前记录不是电量消费记录", exception.getMessage());
    }

    @Test
    @DisplayName("getPowerConsumeDetail方法集成测试 - 明细记录不存在测试")
    void testGetPowerConsumeDetail_PowerConsumeNotFound_ShouldThrowException() {
        prepareTestDataForPowerConsumeDetail();

        electricMeterPowerConsumeRecordRepository.deleteById(4001);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> meterConsumeService.getPowerConsumeDetail(3001));
        assertEquals("电量明细记录不存在", exception.getMessage());
    }

    /**
     * 准备测试数据到ElectricMeterBalanceConsumeRecordEntity表
     */
    private void prepareTestDataForPowerConsumePage() {
        // 清理现有数据
        electricMeterBalanceConsumeRecordRepository.delete(new QueryWrapper<>());

        // 准备测试数据
        LocalDateTime baseTime = LocalDateTime.of(LocalDate.now().getYear(), 1, 15, 10, 0);

        // 测试数据1 - QUANTITY类型（非合并计量）
        ElectricMeterBalanceConsumeRecordEntity record1 = new ElectricMeterBalanceConsumeRecordEntity()
                .setId(1001)
                .setMeterConsumeRecordId(2001)
                .setConsumeNo("CONSUME_001")
                .setConsumeType(ConsumeTypeEnum.ELECTRIC.getCode())
                .setMeterType(1)
                .setAccountId(1)
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY.getCode())
                .setMeterId(101)
                .setMeterName("1号楼电表")
                .setDeviceNo("EM001")
                .setOwnerId(1001)
                .setOwnerType(OwnerTypeEnum.PERSONAL.getCode())
                .setOwnerName("张三")
                .setSpaceId(201)
                .setSpaceName("1号楼101室")
                .setPricePlanId(1)
                .setPricePlanName("居民用电")
                .setStepStartValue(BigDecimal.ZERO)
                .setHistoryPowerOffset(BigDecimal.ZERO)
                .setStepRate(new BigDecimal("1.00"))
                .setConsumeAmount(new BigDecimal("50.50"))
                .setConsumeAmountHigher(new BigDecimal("10.00"))
                .setConsumeAmountHigh(new BigDecimal("15.00"))
                .setConsumeAmountLow(new BigDecimal("20.00"))
                .setConsumeAmountLower(new BigDecimal("5.50"))
                .setConsumeAmountDeepLow(BigDecimal.ZERO)
                .setPriceHigher(new BigDecimal("0.8"))
                .setPriceHigh(new BigDecimal("0.6"))
                .setPriceLow(new BigDecimal("0.4"))
                .setPriceLower(new BigDecimal("0.3"))
                .setPriceDeepLow(new BigDecimal("0.2"))
                .setBeginBalance(new BigDecimal("200.00"))
                .setEndBalance(new BigDecimal("175.25"))
                .setRemark("测试消费记录1")
                .setMeterConsumeTime(baseTime)
                .setCreateTime(baseTime)
                .setIsDeleted(false);

        // 测试数据2 - MERGED类型（合并计量）
        ElectricMeterBalanceConsumeRecordEntity record2 = new ElectricMeterBalanceConsumeRecordEntity()
                .setId(1002)
                .setMeterConsumeRecordId(2002)
                .setConsumeNo("CONSUME_002")
                .setConsumeType(ConsumeTypeEnum.ELECTRIC.getCode())
                .setMeterType(1)
                .setAccountId(2)
                .setElectricAccountType(ElectricAccountTypeEnum.MERGED.getCode())
                .setMeterId(102)
                .setMeterName("2号楼电表")
                .setDeviceNo("EM002")
                .setOwnerId(1002)
                .setOwnerType(OwnerTypeEnum.ENTERPRISE.getCode())
                .setOwnerName("测试企业")
                .setSpaceId(202)
                .setSpaceName("2号楼办公室")
                .setPricePlanId(2)
                .setPricePlanName("商业用电")
                .setStepStartValue(new BigDecimal("150.00"))
                .setHistoryPowerOffset(new BigDecimal("20.00"))
                .setStepRate(new BigDecimal("1.20"))
                .setConsumeAmount(new BigDecimal("100.75"))
                .setConsumeAmountHigher(new BigDecimal("20.00"))
                .setConsumeAmountHigh(new BigDecimal("30.00"))
                .setConsumeAmountLow(new BigDecimal("40.00"))
                .setConsumeAmountLower(new BigDecimal("10.75"))
                .setConsumeAmountDeepLow(BigDecimal.ZERO)
                .setPriceHigher(new BigDecimal("1.2"))
                .setPriceHigh(new BigDecimal("1.0"))
                .setPriceLow(new BigDecimal("0.8"))
                .setPriceLower(new BigDecimal("0.6"))
                .setPriceDeepLow(new BigDecimal("0.4"))
                .setBeginBalance(new BigDecimal("500.00"))
                .setEndBalance(new BigDecimal("420.50"))
                .setRemark("测试消费记录2")
                .setMeterConsumeTime(baseTime.plusHours(2))
                .setCreateTime(baseTime.plusHours(2))
                .setIsDeleted(false);

        // 测试数据3 - 用于测试查询条件
        ElectricMeterBalanceConsumeRecordEntity record3 = new ElectricMeterBalanceConsumeRecordEntity()
                .setId(1003)
                .setMeterConsumeRecordId(2003)
                .setConsumeNo("CONSUME_003")
                .setConsumeType(ConsumeTypeEnum.ELECTRIC.getCode())
                .setMeterType(1)
                .setAccountId(3)
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY.getCode())
                .setMeterId(103)
                .setMeterName("3号楼智能电表")
                .setDeviceNo("EM003")
                .setOwnerId(1003)
                .setOwnerType(OwnerTypeEnum.PERSONAL.getCode())
                .setOwnerName("李四")
                .setSpaceId(203)
                .setSpaceName("3号楼201室")
                .setPricePlanId(1)
                .setPricePlanName("居民用电")
                .setStepStartValue(new BigDecimal("60.00"))
                .setHistoryPowerOffset(new BigDecimal("10.00"))
                .setStepRate(new BigDecimal("0.90"))
                .setConsumeAmount(new BigDecimal("40.25"))
                .setConsumeAmountHigher(new BigDecimal("8.00"))
                .setConsumeAmountHigh(new BigDecimal("12.00"))
                .setConsumeAmountLow(new BigDecimal("16.00"))
                .setConsumeAmountLower(new BigDecimal("4.25"))
                .setConsumeAmountDeepLow(BigDecimal.ZERO)
                .setPriceHigher(new BigDecimal("0.8"))
                .setPriceHigh(new BigDecimal("0.6"))
                .setPriceLow(new BigDecimal("0.4"))
                .setPriceLower(new BigDecimal("0.3"))
                .setPriceDeepLow(new BigDecimal("0.2"))
                .setBeginBalance(new BigDecimal("150.00"))
                .setEndBalance(new BigDecimal("125.75"))
                .setRemark("测试消费记录3")
                .setMeterConsumeTime(baseTime.plusDays(1))
                .setCreateTime(baseTime.plusDays(1))
                .setIsDeleted(false);

        // 保存测试数据
        electricMeterBalanceConsumeRecordRepository.insert(record1);
        electricMeterBalanceConsumeRecordRepository.insert(record2);
        electricMeterBalanceConsumeRecordRepository.insert(record3);
    }

    /**
     * 准备电量消费明细测试数据
     */
    private void prepareTestDataForPowerConsumeDetail() {
        electricMeterBalanceConsumeRecordRepository.delete(new QueryWrapper<>());
        electricMeterPowerConsumeRecordRepository.delete(new QueryWrapper<>());

        LocalDateTime now = LocalDateTime.now();

        ElectricMeterPowerConsumeRecordEntity powerConsumeRecord = new ElectricMeterPowerConsumeRecordEntity()
                .setId(4001)
                .setMeterId(101)
                .setIsCalculate(true)
                .setCalculateType(CalculateTypeEnum.AIR_CONDITIONING.getCode())
                .setAccountId(1)
                .setSpaceId(201)
                .setBeginRecordId(5001)
                .setBeginPower(new BigDecimal("92.50"))
                .setBeginPowerHigher(new BigDecimal("10.00"))
                .setBeginPowerHigh(new BigDecimal("15.00"))
                .setBeginPowerLow(new BigDecimal("20.00"))
                .setBeginPowerLower(new BigDecimal("25.00"))
                .setBeginPowerDeepLow(new BigDecimal("22.50"))
                .setBeginRecordTime(now.minusHours(1))
                .setEndRecordId(5002)
                .setEndPower(new BigDecimal("105.00"))
                .setEndPowerHigher(new BigDecimal("11.50"))
                .setEndPowerHigh(new BigDecimal("17.00"))
                .setEndPowerLow(new BigDecimal("22.00"))
                .setEndPowerLower(new BigDecimal("27.00"))
                .setEndPowerDeepLow(new BigDecimal("27.50"))
                .setEndRecordTime(now)
                .setConsumePower(new BigDecimal("12.50"))
                .setConsumePowerHigher(new BigDecimal("1.50"))
                .setConsumePowerHigh(new BigDecimal("2.00"))
                .setConsumePowerLow(new BigDecimal("2.00"))
                .setConsumePowerLower(new BigDecimal("2.00"))
                .setConsumePowerDeepLow(new BigDecimal("5.00"))
                .setMeterConsumeTime(now)
                .setCreateTime(now)
                .setIsDeleted(false);
        electricMeterPowerConsumeRecordRepository.insert(powerConsumeRecord);

        ElectricMeterBalanceConsumeRecordEntity balanceConsumeRecord = new ElectricMeterBalanceConsumeRecordEntity()
                .setId(3001)
                .setMeterConsumeRecordId(4001)
                .setConsumeNo("DETAIL_CONSUME_001")
                .setConsumeType(ConsumeTypeEnum.ELECTRIC.getCode())
                .setMeterType(1)
                .setAccountId(1)
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY.getCode())
                .setOwnerId(1001)
                .setOwnerType(OwnerTypeEnum.PERSONAL.getCode())
                .setOwnerName("张三")
                .setMeterId(101)
                .setMeterName("明细测试电表")
                .setDeviceNo("EM-DETAIL-001")
                .setSpaceId(201)
                .setSpaceName("测试房间")
                .setPricePlanId(1)
                .setPricePlanName("居民用电")
                .setStepStartValue(BigDecimal.ZERO)
                .setHistoryPowerOffset(BigDecimal.ZERO)
                .setStepRate(BigDecimal.ONE)
                .setConsumeAmount(new BigDecimal("8.75"))
                .setConsumeAmountHigher(new BigDecimal("1.20"))
                .setConsumeAmountHigh(new BigDecimal("1.50"))
                .setConsumeAmountLow(new BigDecimal("2.00"))
                .setConsumeAmountLower(new BigDecimal("1.80"))
                .setConsumeAmountDeepLow(new BigDecimal("2.25"))
                .setPriceHigher(new BigDecimal("0.80"))
                .setPriceHigh(new BigDecimal("0.75"))
                .setPriceLow(new BigDecimal("0.60"))
                .setPriceLower(new BigDecimal("0.45"))
                .setPriceDeepLow(new BigDecimal("0.45"))
                .setBeginBalance(new BigDecimal("200.00"))
                .setEndBalance(new BigDecimal("191.25"))
                .setMeterConsumeTime(now)
                .setCreateTime(now)
                .setIsDeleted(false);
        electricMeterBalanceConsumeRecordRepository.insert(balanceConsumeRecord);
    }

}
