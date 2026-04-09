package info.zhihui.ems.business.report.service.build.impl;

import info.zhihui.ems.business.account.entity.AccountOpenRecordEntity;
import info.zhihui.ems.business.billing.entity.AccountBalanceConsumeRecordEntity;
import info.zhihui.ems.business.billing.entity.ElectricMeterBalanceConsumeRecordEntity;
import info.zhihui.ems.business.billing.entity.ElectricMeterPowerConsumeRecordEntity;
import info.zhihui.ems.business.billing.entity.OrderFlowEntity;
import info.zhihui.ems.business.billing.enums.ConsumeTypeEnum;
import info.zhihui.ems.business.report.dto.DailyAccountBuildContextDto;
import info.zhihui.ems.business.report.dto.DailyMeterBuildContextDto;
import info.zhihui.ems.business.report.entity.DailyAccountReportEntity;
import info.zhihui.ems.business.report.entity.DailyMeterReportEntity;
import info.zhihui.ems.business.report.dto.DailyReportBuildRequestDto;
import info.zhihui.ems.business.report.entity.ReportJobLogEntity;
import info.zhihui.ems.business.report.enums.ReportJobStatusEnum;
import info.zhihui.ems.business.report.enums.ReportTriggerTypeEnum;
import info.zhihui.ems.business.report.qo.DailyMeterCandidateQo;
import info.zhihui.ems.business.report.qo.ElectricBillAccountSummaryQo;
import info.zhihui.ems.business.report.qo.ElectricBillMeterCountQo;
import info.zhihui.ems.business.report.qo.ElectricBillReportQueryQo;
import info.zhihui.ems.business.report.qo.MeterSnapshotSourceQo;
import info.zhihui.ems.business.report.qo.PowerRecordSnapshotSourceQo;
import info.zhihui.ems.business.report.qo.RechargeSourceItemQo;
import info.zhihui.ems.business.report.qo.ReportDateRangeQo;
import info.zhihui.ems.business.report.repository.report.DailyAccountReportRepository;
import info.zhihui.ems.business.report.repository.report.DailyMeterReportRepository;
import info.zhihui.ems.business.report.repository.report.ReportJobLogRepository;
import info.zhihui.ems.business.report.repository.source.ReportAccountSourceRepository;
import info.zhihui.ems.business.report.repository.source.ReportMeterSourceRepository;
import info.zhihui.ems.business.report.repository.source.ReportRechargeSourceRepository;
import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.components.lock.core.LockTemplate;
import info.zhihui.ems.business.device.entity.MeterCancelRecordEntity;
import info.zhihui.ems.business.device.entity.OpenMeterEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("日报统一构建服务测试")
class DailyReportBuildServiceImplTest {

    @Test
    @DisplayName("开始日期晚于结束日期应报错")
    void testBuildDailyReport_StartDateAfterEndDate_ShouldThrow() {
        DailyReportBuildServiceImpl service = createFixture().service();

        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> service.buildDailyReport(new DailyReportBuildRequestDto()
                        .setStartDate(LocalDate.of(2026, 4, 7))
                        .setEndDate(LocalDate.of(2026, 4, 6))));

        assertEquals("开始日期不能晚于结束日期", exception.getMessage());
    }

    @Test
    @DisplayName("应在代码中合并前一日账户日报与当日事实构造账户批次")
    void testBuildDailyReport_ShouldBuildAccountBatchInCode() {
        TestFixture fixture = createFixture();
        fixture.dailyAccountReportRepository.previousReportListToReturn = List.of(
                new DailyAccountReportEntity().setAccountId(10)
        );
        fixture.meterSourceRepository.powerRecordAccountIdList = List.of(20, 20);
        fixture.accountSourceRepository.accountOrderFlowAccountIdList = List.of(30);
        fixture.meterSourceRepository.openAccountIdList = List.of(40);
        fixture.meterSourceRepository.cancelAccountIdList = List.of(50);
        fixture.dailyMeterReportRepository.previousActiveReportListToReturn = List.of(
                new DailyMeterReportEntity().setAccountId(10).setMeterId(100).setGenerateType(0)
        );
        fixture.meterSourceRepository.powerRecordSnapshotListByAccountIdList = List.of(
                new PowerRecordSnapshotSourceQo()
                        .setRecordId(1)
                        .setAccountId(20)
                        .setMeterId(200)
                        .setMeterName("电表200")
                        .setDeviceNo("METER-200")
                        .setElectricAccountType(0)
        );
        fixture.meterSourceRepository.openRecordListByAccountIdList = List.of(
                new OpenMeterEntity().setAccountId(40).setMeterId(400)
        );
        fixture.meterSourceRepository.cancelRecordListByAccountIdList = List.of(
                new MeterCancelRecordEntity().setAccountId(50).setMeterId(500)
        );
        fixture.accountSourceRepository.accountOpenRecordList = List.of(
                new AccountOpenRecordEntity().setAccountId(20).setElectricAccountType(0)
        );
        fixture.meterSourceRepository.currentMeterSnapshotListByAccountIdList = List.of(
                new MeterSnapshotSourceQo().setAccountId(20).setMeterId(200).setMeterName("当前电表200")
        );
        fixture.accountSourceRepository.accountOrderFlowListByAccountIdList = List.of(
                new OrderFlowEntity()
                        .setAccountId(30)
                        .setBalanceType(0)
                        .setBalanceRelationId(30)
                        .setAmount(new BigDecimal("10.00"))
        );

        fixture.service().buildDailyReport(new DailyReportBuildRequestDto()
                .setStartDate(LocalDate.of(2026, 4, 6))
                .setEndDate(LocalDate.of(2026, 4, 6))
                .setTriggerType(ReportTriggerTypeEnum.MANUAL.getCode()));

        assertEquals(List.of(List.of(10, 20, 30, 40, 50)), fixture.dailyAccountReportRepository.accountIdQueryList);
        List<DailyMeterCandidateQo> candidateList = fixture.meterBuildService.lastBuildContext.getCandidateList();
        assertEquals(4, candidateList.size());
        assertEquals(10, candidateList.get(0).getAccountId());
        assertEquals(100, candidateList.get(0).getMeterId());
        assertEquals(50, candidateList.get(3).getAccountId());
        assertEquals(500, candidateList.get(3).getMeterId());
    }

    @Test
    @DisplayName("统计日开始前已全量销户且当日无事实的账户不应继续承接")
    void testBuildDailyReport_WhenAccountFullCancelledBeforeDayStart_ShouldNotCarryPreviousAccount() {
        TestFixture fixture = createFixture();
        fixture.dailyAccountReportRepository.previousReportListToReturn = List.of(
                new DailyAccountReportEntity().setAccountId(10)
        );
        fixture.accountSourceRepository.fullCancelledAccountIdList = List.of(10);

        fixture.service().buildDailyReport(new DailyReportBuildRequestDto()
                .setStartDate(LocalDate.of(2026, 4, 6))
                .setEndDate(LocalDate.of(2026, 4, 6))
                .setTriggerType(ReportTriggerTypeEnum.MANUAL.getCode()));

        assertEquals(List.of(), fixture.dailyAccountReportRepository.accountIdQueryList);
        assertEquals(List.of(), fixture.meterBuildService.buildContextList);
        assertEquals(List.of(), fixture.accountBuildService.buildContextList);
    }

    @Test
    @DisplayName("补正应按账户类型分流到电表层或账户层")
    void testBuildDailyReport_ShouldSplitCorrectionByElectricAccountType() {
        TestFixture fixture = createFixture();
        fixture.accountSourceRepository.accountOrderFlowAccountIdList = List.of(10, 20);
        fixture.accountSourceRepository.accountOpenRecordList = List.of(
                new AccountOpenRecordEntity().setAccountId(10).setElectricAccountType(ElectricAccountTypeEnum.QUANTITY.getCode()),
                new AccountOpenRecordEntity().setAccountId(20).setElectricAccountType(ElectricAccountTypeEnum.MERGED.getCode())
        );
        fixture.meterSourceRepository.correctionRecordList = List.of(
                new ElectricMeterBalanceConsumeRecordEntity()
                        .setAccountId(10)
                        .setMeterId(100)
                        .setConsumeAmount(new BigDecimal("5.00")),
                new ElectricMeterBalanceConsumeRecordEntity()
                        .setAccountId(20)
                        .setMeterId(200)
                        .setConsumeAmount(new BigDecimal("7.00"))
        );

        fixture.service().buildDailyReport(new DailyReportBuildRequestDto()
                .setStartDate(LocalDate.of(2026, 4, 6))
                .setEndDate(LocalDate.of(2026, 4, 6))
                .setTriggerType(ReportTriggerTypeEnum.MANUAL.getCode()));

        List<ElectricMeterBalanceConsumeRecordEntity> meterCorrectionRecordList =
                fixture.meterBuildService.lastBuildContext.getCorrectionRecordList();
        List<ElectricMeterBalanceConsumeRecordEntity> accountCorrectionRecordList =
                fixture.accountBuildService.lastBuildContext.getAccountCorrectionRecordList();

        assertEquals(1, meterCorrectionRecordList.size());
        assertEquals(10, meterCorrectionRecordList.get(0).getAccountId());
        assertEquals(1, fixture.meterBuildService.lastBuildContext.getCandidateList().size());
        assertEquals(10, fixture.meterBuildService.lastBuildContext.getCandidateList().get(0).getAccountId());

        assertEquals(1, accountCorrectionRecordList.size());
        assertEquals(20, accountCorrectionRecordList.get(0).getAccountId());
    }

    @Test
    @DisplayName("全局锁被占用时应直接拒绝重算")
    void testBuildDailyReport_WhenGlobalLockOccupied_ShouldThrow() {
        TestFixture fixture = createFixture();
        fixture.lockTemplate.lock.tryLockResult = false;

        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> fixture.service().buildDailyReport(new DailyReportBuildRequestDto()
                        .setStartDate(LocalDate.of(2026, 4, 6))
                        .setEndDate(LocalDate.of(2026, 4, 6))
                        .setTriggerType(ReportTriggerTypeEnum.MANUAL.getCode())));

        assertEquals("日报重算任务正在执行，请稍后重试", exception.getMessage());
        assertEquals(1, fixture.lockTemplate.lock.tryLockCount);
        assertEquals(0, fixture.lockTemplate.lock.unlockCount);
        assertEquals(List.of(), fixture.operationList);
    }

    @Test
    @DisplayName("账户数超过批次大小时应拆成多批构建")
    void testBuildDailyReport_WhenAccountCountExceedsBatchSize_ShouldBuildInBatches() {
        TestFixture fixture = createFixture();
        List<Integer> accountIdList = new ArrayList<>();
        for (int index = 1; index <= 201; index++) {
            accountIdList.add(index);
        }
        fixture.dailyAccountReportRepository.accountIdListToReturn = accountIdList;

        fixture.service().buildDailyReport(new DailyReportBuildRequestDto()
                .setStartDate(LocalDate.of(2026, 4, 6))
                .setEndDate(LocalDate.of(2026, 4, 6))
                .setTriggerType(ReportTriggerTypeEnum.MANUAL.getCode()));

        assertEquals(2, fixture.dailyAccountReportRepository.accountIdQueryList.size());
        assertEquals(200, fixture.dailyAccountReportRepository.accountIdQueryList.get(0).size());
        assertEquals(1, fixture.dailyAccountReportRepository.accountIdQueryList.get(1).size());
        assertEquals(2, fixture.meterBuildService.buildContextList.size());
        assertEquals(2, fixture.accountBuildService.buildContextList.size());
    }

    @Test
    @DisplayName("应将充值服务费事实直接转换为账户日报输入")
    void testBuildDailyReport_ShouldBuildRechargeServiceFeeListDirectly() {
        TestFixture fixture = createFixture();
        fixture.dailyAccountReportRepository.accountIdListToReturn = List.of(10);
        fixture.rechargeSourceRepository.accountRechargeList = List.of(
                new RechargeSourceItemQo()
                        .setOrderSn("ORDER-1")
                        .setAccountId(10)
                        .setBalanceType(0)
                        .setAmount(new java.math.BigDecimal("50.00"))
                        .setServiceAmount(new java.math.BigDecimal("5.00"))
                        .setCreateTime(LocalDateTime.of(2026, 4, 6, 10, 0)),
                new RechargeSourceItemQo()
                        .setOrderSn("ORDER-1")
                        .setAccountId(10)
                        .setBalanceType(1)
                        .setMeterId(101)
                        .setAmount(new java.math.BigDecimal("20.00"))
                        .setServiceAmount(new java.math.BigDecimal("5.00"))
                        .setCreateTime(LocalDateTime.of(2026, 4, 6, 9, 0)),
                new RechargeSourceItemQo()
                        .setOrderSn("ORDER-2")
                        .setAccountId(10)
                        .setBalanceType(0)
                        .setAmount(new java.math.BigDecimal("30.00"))
                        .setServiceAmount(new java.math.BigDecimal("3.00"))
                        .setCreateTime(LocalDateTime.of(2026, 4, 6, 11, 0))
        );

        fixture.service().buildDailyReport(new DailyReportBuildRequestDto()
                .setStartDate(LocalDate.of(2026, 4, 6))
                .setEndDate(LocalDate.of(2026, 4, 6))
                .setTriggerType(ReportTriggerTypeEnum.MANUAL.getCode()));

        List<RechargeSourceItemQo> accountRechargeList = fixture.accountBuildService.lastBuildContext.getAccountRechargeList();
        List<RechargeSourceItemQo> rechargeServiceFeeList = fixture.accountBuildService.lastBuildContext.getRechargeServiceFeeList();
        assertEquals(3, accountRechargeList.size());
        assertEquals(3, rechargeServiceFeeList.size());
        assertEquals(LocalDateTime.of(2026, 4, 6, 9, 0), rechargeServiceFeeList.get(0).getCreateTime());
        assertEquals("ORDER-1", rechargeServiceFeeList.get(0).getOrderSn());
        assertEquals("ORDER-1", rechargeServiceFeeList.get(1).getOrderSn());
        assertEquals("ORDER-2", rechargeServiceFeeList.get(2).getOrderSn());
    }

    @Test
    @DisplayName("应按天顺序删除旧日报并重建")
    void testBuildDailyReport_ShouldBuildDayByDay() {
        TestFixture fixture = createFixture();
        fixture.dailyAccountReportRepository.accountIdListToReturn = List.of(10);
        fixture.meterBuildService.reportListToReturn = List.of(new DailyMeterReportEntity());
        fixture.accountBuildService.reportListToReturn = List.of(new DailyAccountReportEntity());

        fixture.service().buildDailyReport(new DailyReportBuildRequestDto()
                .setStartDate(LocalDate.of(2026, 4, 6))
                .setEndDate(LocalDate.of(2026, 4, 7))
                .setTriggerType(ReportTriggerTypeEnum.MANUAL.getCode()));

        assertEquals(List.of(
                "job.insert",
                "job.updateStatus:2026-04-06",
                "meter.delete:2026-04-06",
                "account.delete:2026-04-06",
                "meter.build:2026-04-06",
                "meter.insert:1",
                "account.build:2026-04-06",
                "account.insert:1",
                "job.updateStatus:2026-04-07",
                "meter.delete:2026-04-07",
                "account.delete:2026-04-07",
                "meter.build:2026-04-07",
                "meter.insert:1",
                "account.build:2026-04-07",
                "account.insert:1",
                "job.updateSuccess"
        ), fixture.operationList);
        assertEquals(ReportTriggerTypeEnum.MANUAL.getCode(), fixture.jobLogRepository.insertedEntity.getTriggerType());
    }

    @Test
    @DisplayName("某天构建失败应写失败日志并记录当前日期")
    void testBuildDailyReport_WhenDayBuildFailed_ShouldUpdateFailureLog() {
        TestFixture fixture = createFixture();
        fixture.dailyAccountReportRepository.accountIdListToReturn = List.of(10);
        fixture.meterBuildService.reportListToReturn = List.of(new DailyMeterReportEntity());
        fixture.accountBuildService.reportListToReturn = List.of(new DailyAccountReportEntity());
        fixture.meterBuildService.failedReportDate = LocalDate.of(2026, 4, 7);

        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> fixture.service().buildDailyReport(new DailyReportBuildRequestDto()
                        .setStartDate(LocalDate.of(2026, 4, 6))
                        .setEndDate(LocalDate.of(2026, 4, 7))
                        .setTriggerType(ReportTriggerTypeEnum.MANUAL.getCode())));

        assertEquals("构建电表日报失败", exception.getMessage());
        assertEquals("job.updateFailure:2026-04-07", fixture.operationList.get(fixture.operationList.size() - 1));
        assertEquals(LocalDate.of(2026, 4, 7), fixture.jobLogRepository.failureCurrentDate);
        assertEquals("构建电表日报失败", fixture.jobLogRepository.failureMessage);
    }

    @Test
    @DisplayName("直接请求构建时应支持SCHEDULED触发类型")
    void testBuildDailyReport_RequestWithScheduledTrigger_ShouldUseScheduled() {
        TestFixture fixture = createFixture();
        fixture.dailyAccountReportRepository.accountIdListToReturn = List.of(10);

        fixture.service().buildDailyReport(new DailyReportBuildRequestDto()
                .setStartDate(LocalDate.of(2026, 4, 6))
                .setEndDate(LocalDate.of(2026, 4, 6))
                .setTriggerType(ReportTriggerTypeEnum.SCHEDULED.getCode()));

        assertEquals(ReportTriggerTypeEnum.SCHEDULED.getCode(), fixture.jobLogRepository.insertedEntity.getTriggerType());
    }

    @Test
    @DisplayName("直接请求构建且触发类型为空时应默认使用MANUAL")
    void testBuildDailyReport_RequestWithoutTrigger_ShouldUseManual() {
        TestFixture fixture = createFixture();
        fixture.dailyAccountReportRepository.accountIdListToReturn = List.of(10);

        fixture.service().buildDailyReport(new DailyReportBuildRequestDto()
                .setStartDate(LocalDate.of(2026, 4, 6))
                .setEndDate(LocalDate.of(2026, 4, 6)));

        assertEquals(ReportTriggerTypeEnum.MANUAL.getCode(), fixture.jobLogRepository.insertedEntity.getTriggerType());
    }

    @Test
    @DisplayName("非法触发类型应报错")
    void testBuildDailyReport_InvalidTriggerType_ShouldThrow() {
        DailyReportBuildServiceImpl service = createFixture().service();

        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> service.buildDailyReport(new DailyReportBuildRequestDto()
                        .setStartDate(LocalDate.of(2026, 4, 6))
                        .setEndDate(LocalDate.of(2026, 4, 6))
                        .setTriggerType(99)));

        assertEquals("触发类型不合法", exception.getMessage());
    }

    @Test
    @DisplayName("失败日志写入异常时应保留原始构建异常")
    void testBuildDailyReport_WhenUpdateFailureThrows_ShouldKeepOriginalException() {
        TestFixture fixture = createFixture();
        fixture.dailyAccountReportRepository.accountIdListToReturn = List.of(10);
        fixture.meterBuildService.failedReportDate = LocalDate.of(2026, 4, 6);
        fixture.jobLogRepository.updateFailureException = new RuntimeException("日志更新失败");

        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> fixture.service().buildDailyReport(new DailyReportBuildRequestDto()
                        .setStartDate(LocalDate.of(2026, 4, 6))
                        .setEndDate(LocalDate.of(2026, 4, 6))
                        .setTriggerType(ReportTriggerTypeEnum.MANUAL.getCode())));

        assertEquals("构建电表日报失败", exception.getMessage());
        assertEquals(1, exception.getSuppressed().length);
        assertEquals("日志更新失败", exception.getSuppressed()[0].getMessage());
    }

    private TestFixture createFixture() {
        return new TestFixture();
    }

    private static class TestFixture {
        private final List<String> operationList = new ArrayList<>();
        private final FakeDailyMeterReportRepository dailyMeterReportRepository = new FakeDailyMeterReportRepository(operationList);
        private final FakeDailyAccountReportRepository dailyAccountReportRepository = new FakeDailyAccountReportRepository(operationList);
        private final FakeReportJobLogRepository jobLogRepository = new FakeReportJobLogRepository(operationList);
        private final FakeReportMeterSourceRepository meterSourceRepository = new FakeReportMeterSourceRepository();
        private final FakeReportAccountSourceRepository accountSourceRepository = new FakeReportAccountSourceRepository();
        private final FakeReportRechargeSourceRepository rechargeSourceRepository = new FakeReportRechargeSourceRepository();
        private final FakeDailyMeterReportBuilder meterBuildService = new FakeDailyMeterReportBuilder(operationList);
        private final FakeDailyAccountReportBuilder accountBuildService = new FakeDailyAccountReportBuilder(operationList);
        private final FakeLockTemplate lockTemplate = new FakeLockTemplate();

        private DailyReportBuildServiceImpl service() {
            return new DailyReportBuildServiceImpl(
                    dailyMeterReportRepository,
                    dailyAccountReportRepository,
                    jobLogRepository,
                    meterSourceRepository,
                    accountSourceRepository,
                    rechargeSourceRepository,
                    meterBuildService,
                    accountBuildService,
                    lockTemplate
            );
        }
    }

    private static class FakeLockTemplate implements LockTemplate {
        private final FakeLock lock = new FakeLock();

        @Override
        public Lock getLock(String name) {
            return lock;
        }

        @Override
        public java.util.concurrent.locks.ReadWriteLock getReadWriteLock(String name) {
            throw new UnsupportedOperationException();
        }
    }

    private static class FakeLock implements Lock {
        private boolean tryLockResult = true;
        private int tryLockCount;
        private int unlockCount;

        @Override
        public void lock() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void lockInterruptibly() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean tryLock() {
            tryLockCount++;
            return tryLockResult;
        }

        @Override
        public boolean tryLock(long time, TimeUnit unit) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void unlock() {
            unlockCount++;
        }

        @Override
        public Condition newCondition() {
            throw new UnsupportedOperationException();
        }
    }

    private abstract static class BaseMapperStub<T> implements com.baomidou.mybatisplus.core.mapper.BaseMapper<T> {

        @Override
        public int insert(T entity) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int deleteById(T entity) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int delete(com.baomidou.mybatisplus.core.conditions.Wrapper<T> queryWrapper) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int updateById(T entity) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int update(T entity, com.baomidou.mybatisplus.core.conditions.Wrapper<T> updateWrapper) {
            throw new UnsupportedOperationException();
        }

        @Override
        public T selectById(java.io.Serializable id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<T> selectByIds(Collection<? extends java.io.Serializable> idList) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void selectByIds(Collection<? extends java.io.Serializable> idList,
                                org.apache.ibatis.session.ResultHandler<T> resultHandler) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Long selectCount(com.baomidou.mybatisplus.core.conditions.Wrapper<T> queryWrapper) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<T> selectList(com.baomidou.mybatisplus.core.conditions.Wrapper<T> queryWrapper) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void selectList(com.baomidou.mybatisplus.core.conditions.Wrapper<T> queryWrapper,
                               org.apache.ibatis.session.ResultHandler<T> resultHandler) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<T> selectList(com.baomidou.mybatisplus.core.metadata.IPage<T> page,
                                  com.baomidou.mybatisplus.core.conditions.Wrapper<T> queryWrapper) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void selectList(com.baomidou.mybatisplus.core.metadata.IPage<T> page,
                               com.baomidou.mybatisplus.core.conditions.Wrapper<T> queryWrapper,
                               org.apache.ibatis.session.ResultHandler<T> resultHandler) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Map<String, Object>> selectMaps(com.baomidou.mybatisplus.core.conditions.Wrapper<T> queryWrapper) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void selectMaps(com.baomidou.mybatisplus.core.conditions.Wrapper<T> queryWrapper,
                               org.apache.ibatis.session.ResultHandler<Map<String, Object>> resultHandler) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Map<String, Object>> selectMaps(com.baomidou.mybatisplus.core.metadata.IPage<? extends Map<String, Object>> page,
                                                    com.baomidou.mybatisplus.core.conditions.Wrapper<T> queryWrapper) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void selectMaps(com.baomidou.mybatisplus.core.metadata.IPage<? extends Map<String, Object>> page,
                               com.baomidou.mybatisplus.core.conditions.Wrapper<T> queryWrapper,
                               org.apache.ibatis.session.ResultHandler<Map<String, Object>> resultHandler) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <E> List<E> selectObjs(com.baomidou.mybatisplus.core.conditions.Wrapper<T> queryWrapper) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <E> void selectObjs(com.baomidou.mybatisplus.core.conditions.Wrapper<T> queryWrapper,
                                   org.apache.ibatis.session.ResultHandler<E> resultHandler) {
            throw new UnsupportedOperationException();
        }
    }

    private static class FakeDailyMeterReportRepository extends BaseMapperStub<DailyMeterReportEntity> implements DailyMeterReportRepository {
        private final List<String> operationList;

        private List<DailyMeterReportEntity> previousReportListToReturn = Collections.emptyList();

        private List<DailyMeterReportEntity> previousActiveReportListToReturn = Collections.emptyList();

        private FakeDailyMeterReportRepository(List<String> operationList) {
            this.operationList = operationList;
        }

        @Override
        public int deleteByReportDate(LocalDate reportDate) {
            operationList.add("meter.delete:" + reportDate);
            return 1;
        }

        @Override
        public List<DailyMeterReportEntity> findListByReportDate(LocalDate reportDate) {
            return previousReportListToReturn;
        }

        @Override
        public List<DailyMeterReportEntity> findActiveListByReportDateAndAccountIdList(LocalDate reportDate,
                                                                                        List<Integer> accountIdList) {
            return previousActiveReportListToReturn;
        }

        @Override
        public List<ElectricBillMeterCountQo> findMeterCountListByAccountIdList(LocalDate startDate,
                                                                                LocalDate endDate,
                                                                                List<Integer> accountIdList) {
            return Collections.emptyList();
        }

        @Override
        public List<DailyMeterReportEntity> findListByAccountIdAndDateRange(Integer accountId,
                                                                            LocalDate startDate,
                                                                            LocalDate endDate) {
            return Collections.emptyList();
        }

        @Override
        public List<org.apache.ibatis.executor.BatchResult> insert(Collection<DailyMeterReportEntity> entityList) {
            operationList.add("meter.insert:" + entityList.size());
            return List.of();
        }
    }

    private static class FakeDailyAccountReportRepository extends BaseMapperStub<DailyAccountReportEntity> implements DailyAccountReportRepository {
        private final List<String> operationList;

        private List<DailyAccountReportEntity> previousReportListToReturn = Collections.emptyList();

        private List<Integer> accountIdListToReturn;

        private final List<List<Integer>> accountIdQueryList = new ArrayList<>();

        private FakeDailyAccountReportRepository(List<String> operationList) {
            this.operationList = operationList;
        }

        @Override
        public int deleteByReportDate(LocalDate reportDate) {
            operationList.add("account.delete:" + reportDate);
            return 1;
        }

        @Override
        public List<DailyAccountReportEntity> findListByReportDate(LocalDate reportDate) {
            return previousReportListToReturn;
        }

        @Override
        public List<Integer> findAccountIdListByReportDate(LocalDate reportDate) {
            if (accountIdListToReturn != null) {
                return accountIdListToReturn;
            }
            List<Integer> accountIdList = new ArrayList<>(previousReportListToReturn.size());
            for (DailyAccountReportEntity previousReportEntity : previousReportListToReturn) {
                accountIdList.add(previousReportEntity.getAccountId());
            }
            return accountIdList;
        }

        @Override
        public List<DailyAccountReportEntity> findListByReportDateAndAccountIdList(LocalDate reportDate, List<Integer> accountIdList) {
            accountIdQueryList.add(new ArrayList<>(accountIdList));
            return previousReportListToReturn;
        }

        @Override
        public List<ElectricBillAccountSummaryQo> findElectricBillAccountPageList(ElectricBillReportQueryQo queryQo) {
            return Collections.emptyList();
        }

        @Override
        public ElectricBillAccountSummaryQo getElectricBillAccountSummary(Integer accountId,
                                                                          LocalDate startDate,
                                                                          LocalDate endDate) {
            return null;
        }

        @Override
        public DailyAccountReportEntity getLatestByAccountIdAndDateRange(Integer accountId,
                                                                         LocalDate startDate,
                                                                         LocalDate endDate) {
            return null;
        }

        @Override
        public List<org.apache.ibatis.executor.BatchResult> insert(Collection<DailyAccountReportEntity> entityList) {
            operationList.add("account.insert:" + entityList.size());
            return List.of();
        }
    }

    private static class FakeReportJobLogRepository extends BaseMapperStub<ReportJobLogEntity> implements ReportJobLogRepository {
        private final List<String> operationList;

        private ReportJobLogEntity insertedEntity;

        private LocalDate failureCurrentDate;

        private String failureMessage;

        private RuntimeException updateFailureException;

        private FakeReportJobLogRepository(List<String> operationList) {
            this.operationList = operationList;
        }

        @Override
        public int insert(ReportJobLogEntity entity) {
            entity.setId(1);
            insertedEntity = entity;
            operationList.add("job.insert");
            return 1;
        }

        @Override
        public int updateStatus(Integer id, Integer status, LocalDate currentReportDate) {
            operationList.add("job.updateStatus:" + currentReportDate);
            return 1;
        }

        @Override
        public int updateFailure(Integer id, Integer status, LocalDate currentReportDate, String errorMessage, LocalDateTime finishTime) {
            if (updateFailureException != null) {
                throw updateFailureException;
            }
            failureCurrentDate = currentReportDate;
            failureMessage = errorMessage;
            operationList.add("job.updateFailure:" + currentReportDate);
            return 1;
        }

        @Override
        public int updateSuccess(Integer id, Integer status, LocalDateTime finishTime) {
            operationList.add("job.updateSuccess");
            return 1;
        }
    }

    private static class FakeReportMeterSourceRepository implements ReportMeterSourceRepository {

        private List<ElectricMeterPowerConsumeRecordEntity> powerConsumeRecordList = Collections.emptyList();

        private List<Integer> powerRecordAccountIdList = Collections.emptyList();

        private List<PowerRecordSnapshotSourceQo> powerRecordSnapshotListByAccountIdList = Collections.emptyList();

        private List<ElectricMeterBalanceConsumeRecordEntity> electricChargeRecordList = Collections.emptyList();

        private List<ElectricMeterBalanceConsumeRecordEntity> correctionRecordList = Collections.emptyList();

        private List<Integer> openAccountIdList = Collections.emptyList();

        private List<OpenMeterEntity> openRecordListByAccountIdList = Collections.emptyList();

        private List<Integer> cancelAccountIdList = Collections.emptyList();

        private List<MeterCancelRecordEntity> cancelRecordListByAccountIdList = Collections.emptyList();

        private List<ElectricMeterPowerConsumeRecordEntity> powerConsumeRecordListByAccountIdList = Collections.emptyList();

        private List<MeterSnapshotSourceQo> currentMeterSnapshotListByAccountIdList = Collections.emptyList();

        @Override
        public List<Integer> findDailyPowerRecordAccountIdList(ReportDateRangeQo query) {
            return powerRecordAccountIdList;
        }

        @Override
        public List<PowerRecordSnapshotSourceQo> findDailyPowerRecordSnapshotListByAccountIdList(ReportDateRangeQo query, List<Integer> accountIdList) {
            return powerRecordSnapshotListByAccountIdList;
        }

        @Override
        public List<MeterSnapshotSourceQo> findCurrentMeterSnapshotListByAccountIdList(List<Integer> accountIdList) {
            return currentMeterSnapshotListByAccountIdList;
        }

        @Override
        public List<ElectricMeterBalanceConsumeRecordEntity> findDailyBalanceConsumeRecordListByAccountIdList(ReportDateRangeQo query,
                                                                                                               Integer consumeType,
                                                                                                               List<Integer> accountIdList) {
            if (Objects.equals(consumeType, ConsumeTypeEnum.ELECTRIC.getCode())) {
                return electricChargeRecordList;
            }
            if (Objects.equals(consumeType, ConsumeTypeEnum.CORRECTION.getCode())) {
                return correctionRecordList;
            }
            return Collections.emptyList();
        }

        @Override
        public List<Integer> findDailyOpenAccountIdList(ReportDateRangeQo query, Integer meterType) {
            return openAccountIdList;
        }

        @Override
        public List<OpenMeterEntity> findDailyOpenRecordListByAccountIdList(ReportDateRangeQo query, Integer meterType, List<Integer> accountIdList) {
            return openRecordListByAccountIdList;
        }

        @Override
        public List<Integer> findDailyCancelAccountIdList(ReportDateRangeQo query, Integer meterType) {
            return cancelAccountIdList;
        }

        @Override
        public List<MeterCancelRecordEntity> findDailyCancelRecordListByAccountIdList(ReportDateRangeQo query, Integer meterType, List<Integer> accountIdList) {
            return cancelRecordListByAccountIdList;
        }

        @Override
        public List<ElectricMeterPowerConsumeRecordEntity> findDailyPowerConsumeRecordListByAccountIdList(ReportDateRangeQo query,
                                                                                                           List<Integer> accountIdList) {
            return powerConsumeRecordListByAccountIdList;
        }
    }

    private static class FakeReportAccountSourceRepository implements ReportAccountSourceRepository {

        private List<Integer> accountOrderFlowAccountIdList = Collections.emptyList();

        private List<Integer> fullCancelledAccountIdList = Collections.emptyList();

        private List<AccountBalanceConsumeRecordEntity> monthlyConsumeRecordListByAccountIdList = Collections.emptyList();

        private List<OrderFlowEntity> accountOrderFlowListByAccountIdList = Collections.emptyList();

        private List<AccountOpenRecordEntity> accountOpenRecordList = Collections.emptyList();

        @Override
        public List<AccountBalanceConsumeRecordEntity> findDailyMonthlyConsumeRecordListByAccountIdList(ReportDateRangeQo query,
                                                                                                         Integer monthlyConsumeType,
                                                                                                         List<Integer> accountIdList) {
            return monthlyConsumeRecordListByAccountIdList;
        }

        @Override
        public List<Integer> findDailyAccountOrderFlowAccountIdList(ReportDateRangeQo query) {
            return accountOrderFlowAccountIdList;
        }

        @Override
        public List<Integer> findFullCancelledAccountIdListBeforeTime(LocalDateTime beginTime, List<Integer> accountIdList) {
            return fullCancelledAccountIdList;
        }

        @Override
        public List<OrderFlowEntity> findDailyAccountOrderFlowListByAccountIdList(ReportDateRangeQo query,
                                                                                   Integer balanceType,
                                                                                   List<Integer> accountIdList) {
            return accountOrderFlowListByAccountIdList;
        }

        @Override
        public List<AccountOpenRecordEntity> findAccountOpenRecordListByAccountIdList(List<Integer> accountIdList) {
            return accountOpenRecordList;
        }
    }

    private static class FakeReportRechargeSourceRepository implements ReportRechargeSourceRepository {

        private List<RechargeSourceItemQo> meterRechargeList = Collections.emptyList();

        private List<RechargeSourceItemQo> accountRechargeList = Collections.emptyList();

        @Override
        public List<RechargeSourceItemQo> findDailyMeterRechargeListByAccountIdList(ReportDateRangeQo query,
                                                                                     Integer orderType,
                                                                                     String orderStatus,
                                                                                     Integer meterBalanceType,
                                                                                     List<Integer> accountIdList) {
            return meterRechargeList;
        }

        @Override
        public List<RechargeSourceItemQo> findDailyAccountRechargeListByAccountIdList(ReportDateRangeQo query,
                                                                                       Integer orderType,
                                                                                       String orderStatus,
                                                                                       List<Integer> accountIdList) {
            return accountRechargeList;
        }
    }

    private static class FakeDailyMeterReportBuilder extends DailyMeterReportBuilder {
        private final List<String> operationList;

        private List<DailyMeterReportEntity> reportListToReturn = Collections.emptyList();

        private LocalDate failedReportDate;

        private DailyMeterBuildContextDto lastBuildContext;

        private final List<DailyMeterBuildContextDto> buildContextList = new ArrayList<>();

        private FakeDailyMeterReportBuilder(List<String> operationList) {
            this.operationList = operationList;
        }

        @Override
        public List<DailyMeterReportEntity> buildDailyMeterReportList(DailyMeterBuildContextDto buildContext) {
            LocalDate reportDate = buildContext.getReportDateRange().getReportDate();
            lastBuildContext = buildContext;
            buildContextList.add(buildContext);
            operationList.add("meter.build:" + reportDate);
            if (reportDate.equals(failedReportDate)) {
                throw new BusinessRuntimeException("构建电表日报失败");
            }
            return reportListToReturn;
        }
    }

    private static class FakeDailyAccountReportBuilder extends DailyAccountReportBuilder {
        private final List<String> operationList;

        private List<DailyAccountReportEntity> reportListToReturn = Collections.emptyList();

        private DailyAccountBuildContextDto lastBuildContext;

        private final List<DailyAccountBuildContextDto> buildContextList = new ArrayList<>();

        private FakeDailyAccountReportBuilder(List<String> operationList) {
            this.operationList = operationList;
        }

        @Override
        public List<DailyAccountReportEntity> buildDailyAccountReportList(DailyAccountBuildContextDto buildContext) {
            lastBuildContext = buildContext;
            buildContextList.add(buildContext);
            operationList.add("account.build:" + buildContext.getReportDateRange().getReportDate());
            return reportListToReturn;
        }
    }
}
