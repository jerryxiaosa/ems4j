package info.zhihui.ems.business.report;

import info.zhihui.ems.business.report.entity.DailyAccountReportEntity;
import info.zhihui.ems.business.report.entity.DailyMeterReportEntity;
import info.zhihui.ems.business.report.entity.ReportJobLogEntity;
import info.zhihui.ems.business.report.enums.MeterReportGenerateTypeEnum;
import info.zhihui.ems.business.report.enums.ReportJobStatusEnum;
import info.zhihui.ems.business.report.enums.ReportTriggerTypeEnum;
import info.zhihui.ems.business.report.qo.ElectricBillAccountSummaryQo;
import info.zhihui.ems.business.report.qo.ElectricBillMeterCountQo;
import info.zhihui.ems.business.report.qo.ElectricBillReportQueryQo;
import info.zhihui.ems.business.report.repository.report.DailyAccountReportRepository;
import info.zhihui.ems.business.report.repository.report.DailyMeterReportRepository;
import info.zhihui.ems.business.report.repository.report.ReportJobLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("integrationtest")
@Transactional
@DisplayName("日报仓储集成测试")
class DailyReportRepositoryIntegrationTest {

    @Autowired
    private DailyMeterReportRepository dailyMeterReportRepository;

    @Autowired
    private DailyAccountReportRepository dailyAccountReportRepository;

    @Autowired
    private ReportJobLogRepository reportJobLogRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("电表日报应完整持久化分时读数金额与余额字段")
    void testDailyMeterReportRepository_ShouldPersistAllFields() {
        DailyMeterReportEntity entity = new DailyMeterReportEntity()
                .setReportDate(LocalDate.of(2026, 4, 6))
                .setAccountId(10)
                .setOwnerId(1000)
                .setOwnerType(1)
                .setOwnerName("企业A")
                .setMeterId(100)
                .setMeterName("一号电表")
                .setDeviceNo("METER-100")
                .setSpaceId(2000)
                .setSpaceName("一层")
                .setElectricAccountType(0)
                .setGenerateType(MeterReportGenerateTypeEnum.NORMAL.getCode())
                .setBeginPower(new BigDecimal("100.00"))
                .setBeginPowerHigher(new BigDecimal("10.00"))
                .setBeginPowerHigh(new BigDecimal("20.00"))
                .setBeginPowerLow(new BigDecimal("30.00"))
                .setBeginPowerLower(new BigDecimal("40.00"))
                .setBeginPowerDeepLow(new BigDecimal("0.00"))
                .setEndPower(new BigDecimal("110.00"))
                .setEndPowerHigher(new BigDecimal("12.00"))
                .setEndPowerHigh(new BigDecimal("22.00"))
                .setEndPowerLow(new BigDecimal("33.00"))
                .setEndPowerLower(new BigDecimal("43.00"))
                .setEndPowerDeepLow(new BigDecimal("0.00"))
                .setConsumePower(new BigDecimal("10.00"))
                .setConsumePowerHigher(new BigDecimal("2.00"))
                .setConsumePowerHigh(new BigDecimal("2.00"))
                .setConsumePowerLow(new BigDecimal("3.00"))
                .setConsumePowerLower(new BigDecimal("3.00"))
                .setConsumePowerDeepLow(BigDecimal.ZERO)
                .setElectricChargeAmount(new BigDecimal("12.00"))
                .setElectricChargeAmountHigher(new BigDecimal("2.40"))
                .setElectricChargeAmountHigh(new BigDecimal("2.50"))
                .setElectricChargeAmountLow(new BigDecimal("3.30"))
                .setElectricChargeAmountLower(new BigDecimal("3.80"))
                .setElectricChargeAmountDeepLow(BigDecimal.ZERO)
                .setDisplayPriceHigher(new BigDecimal("1.20"))
                .setDisplayPriceHigh(new BigDecimal("1.25"))
                .setDisplayPriceLow(new BigDecimal("1.10"))
                .setDisplayPriceLower(new BigDecimal("0.95"))
                .setDisplayPriceDeepLow(new BigDecimal("0.60"))
                .setCorrectionPayAmount(new BigDecimal("5.00"))
                .setCorrectionRefundAmount(new BigDecimal("2.00"))
                .setCorrectionNetAmount(new BigDecimal("3.00"))
                .setBeginBalance(new BigDecimal("50.00"))
                .setEndBalance(new BigDecimal("61.00"))
                .setRechargeAmount(new BigDecimal("20.00"));
        entity.setCreateTime(LocalDateTime.of(2026, 4, 7, 1, 0));

        List<org.apache.ibatis.executor.BatchResult> insertResultList = dailyMeterReportRepository.insert(List.of(entity));
        List<DailyMeterReportEntity> reportList = dailyMeterReportRepository.findListByReportDate(LocalDate.of(2026, 4, 6));

        assertEquals(1, insertResultList.size());
        assertEquals(1, reportList.size());
        DailyMeterReportEntity savedEntity = reportList.get(0);
        assertEquals(0, new BigDecimal("10.00").compareTo(savedEntity.getBeginPowerHigher()));
        assertEquals(0, new BigDecimal("43.00").compareTo(savedEntity.getEndPowerLower()));
        assertEquals(0, new BigDecimal("3.80").compareTo(savedEntity.getElectricChargeAmountLower()));
        assertEquals(0, new BigDecimal("0.95").compareTo(savedEntity.getDisplayPriceLower()));
        assertEquals(0, new BigDecimal("5.00").compareTo(savedEntity.getCorrectionPayAmount()));
        assertEquals(0, new BigDecimal("2.00").compareTo(savedEntity.getCorrectionRefundAmount()));
        assertEquals(0, new BigDecimal("3.00").compareTo(savedEntity.getCorrectionNetAmount()));
        assertEquals(0, new BigDecimal("50.00").compareTo(savedEntity.getBeginBalance()));
        assertEquals(0, new BigDecimal("61.00").compareTo(savedEntity.getEndBalance()));
        assertEquals(0, new BigDecimal("20.00").compareTo(savedEntity.getRechargeAmount()));
    }

    @Test
    @DisplayName("账户日报应完整持久化补正与累计字段")
    void testDailyAccountReportRepository_ShouldPersistCorrectionAndAccumulateFields() {
        DailyAccountReportEntity previousEntity = new DailyAccountReportEntity()
                .setReportDate(LocalDate.of(2026, 4, 5))
                .setAccountId(20)
                .setOwnerId(2000)
                .setOwnerType(1)
                .setOwnerName("企业B")
                .setElectricAccountType(0)
                .setMeterCount(2)
                .setConsumePower(new BigDecimal("10.50"))
                .setConsumePowerHigher(new BigDecimal("2.00"))
                .setConsumePowerHigh(new BigDecimal("2.50"))
                .setConsumePowerLow(new BigDecimal("3.00"))
                .setConsumePowerLower(new BigDecimal("3.00"))
                .setElectricChargeAmount(new BigDecimal("12.30"))
                .setElectricChargeAmountHigher(new BigDecimal("2.30"))
                .setElectricChargeAmountHigh(new BigDecimal("2.60"))
                .setElectricChargeAmountLow(new BigDecimal("3.70"))
                .setElectricChargeAmountLower(new BigDecimal("3.70"))
                .setMonthlyChargeAmount(BigDecimal.ZERO)
                .setCorrectionPayAmount(new BigDecimal("3.00"))
                .setCorrectionRefundAmount(new BigDecimal("1.00"))
                .setCorrectionNetAmount(new BigDecimal("2.00"))
                .setRechargeAmount(new BigDecimal("30.00"))
                .setRechargeServiceFeeAmount(new BigDecimal("2.00"))
                .setTotalDebitAmount(new BigDecimal("16.30"))
                .setBeginBalance(new BigDecimal("100.00"))
                .setEndBalance(new BigDecimal("113.70"))
                .setAccumulateConsumePower(new BigDecimal("100.50"))
                .setAccumulateElectricChargeAmount(new BigDecimal("120.30"))
                .setAccumulateMonthlyChargeAmount(BigDecimal.ZERO)
                .setAccumulateCorrectionPayAmount(new BigDecimal("5.00"))
                .setAccumulateCorrectionRefundAmount(new BigDecimal("2.00"))
                .setAccumulateRechargeAmount(new BigDecimal("300.00"))
                .setAccumulateRechargeServiceFeeAmount(new BigDecimal("20.00"))
                .setAccumulateTotalDebitAmount(new BigDecimal("140.30"));
        previousEntity.setCreateTime(LocalDateTime.of(2026, 4, 6, 1, 0));
        DailyAccountReportEntity currentEntity = new DailyAccountReportEntity()
                .setReportDate(LocalDate.of(2026, 4, 6))
                .setAccountId(20)
                .setOwnerId(2000)
                .setOwnerType(1)
                .setOwnerName("企业B")
                .setElectricAccountType(0)
                .setMeterCount(1)
                .setConsumePower(new BigDecimal("1.50"))
                .setConsumePowerHigher(new BigDecimal("0.50"))
                .setConsumePowerHigh(new BigDecimal("0.50"))
                .setConsumePowerLow(new BigDecimal("0.30"))
                .setConsumePowerLower(new BigDecimal("0.20"))
                .setElectricChargeAmount(new BigDecimal("2.30"))
                .setElectricChargeAmountHigher(new BigDecimal("0.60"))
                .setElectricChargeAmountHigh(new BigDecimal("0.70"))
                .setElectricChargeAmountLow(new BigDecimal("0.50"))
                .setElectricChargeAmountLower(new BigDecimal("0.50"))
                .setMonthlyChargeAmount(BigDecimal.ZERO)
                .setCorrectionPayAmount(new BigDecimal("1.00"))
                .setCorrectionRefundAmount(new BigDecimal("0.20"))
                .setCorrectionNetAmount(new BigDecimal("0.80"))
                .setRechargeAmount(new BigDecimal("10.00"))
                .setRechargeServiceFeeAmount(new BigDecimal("1.00"))
                .setTotalDebitAmount(new BigDecimal("4.10"))
                .setBeginBalance(new BigDecimal("113.70"))
                .setEndBalance(new BigDecimal("119.60"))
                .setAccumulateConsumePower(new BigDecimal("102.00"))
                .setAccumulateElectricChargeAmount(new BigDecimal("122.60"))
                .setAccumulateMonthlyChargeAmount(BigDecimal.ZERO)
                .setAccumulateCorrectionPayAmount(new BigDecimal("6.00"))
                .setAccumulateCorrectionRefundAmount(new BigDecimal("2.20"))
                .setAccumulateRechargeAmount(new BigDecimal("310.00"))
                .setAccumulateRechargeServiceFeeAmount(new BigDecimal("21.00"))
                .setAccumulateTotalDebitAmount(new BigDecimal("144.40"));
        currentEntity.setCreateTime(LocalDateTime.of(2026, 4, 7, 1, 0));

        List<org.apache.ibatis.executor.BatchResult> insertResultList = dailyAccountReportRepository.insert(List.of(previousEntity, currentEntity));
        List<DailyAccountReportEntity> previousList = dailyAccountReportRepository.findListByReportDate(LocalDate.of(2026, 4, 5));
        List<DailyAccountReportEntity> currentList = dailyAccountReportRepository.findListByReportDate(LocalDate.of(2026, 4, 6));

        assertEquals(1, insertResultList.size());
        assertEquals(1, previousList.size());
        assertEquals(1, currentList.size());
        DailyAccountReportEntity savedEntity = currentList.get(0);
        assertEquals(0, new BigDecimal("0.50").compareTo(savedEntity.getConsumePowerHigher()));
        assertEquals(0, new BigDecimal("0.50").compareTo(savedEntity.getElectricChargeAmountLower()));
        assertEquals(0, new BigDecimal("1.00").compareTo(savedEntity.getCorrectionPayAmount()));
        assertEquals(0, new BigDecimal("0.20").compareTo(savedEntity.getCorrectionRefundAmount()));
        assertEquals(0, new BigDecimal("0.80").compareTo(savedEntity.getCorrectionNetAmount()));
        assertEquals(0, new BigDecimal("6.00").compareTo(savedEntity.getAccumulateCorrectionPayAmount()));
        assertEquals(0, new BigDecimal("2.20").compareTo(savedEntity.getAccumulateCorrectionRefundAmount()));
        assertEquals(0, new BigDecimal("144.40").compareTo(savedEntity.getAccumulateTotalDebitAmount()));
    }

    @Test
    @DisplayName("任务日志应支持插入运行态并更新为成功和失败")
    void testReportJobLogRepository_ShouldInsertAndUpdateStatus() {
        ReportJobLogEntity entity = new ReportJobLogEntity()
                .setTriggerType(ReportTriggerTypeEnum.MANUAL.getCode())
                .setStartDate(LocalDate.of(2026, 4, 1))
                .setEndDate(LocalDate.of(2026, 4, 6))
                .setStatus(ReportJobStatusEnum.RUNNING.getCode())
                .setTriggerBy("tester");
        entity.setCreateTime(LocalDateTime.of(2026, 4, 6, 10, 0));

        int insertCount = reportJobLogRepository.insert(entity);

        assertEquals(1, insertCount);
        assertNotNull(entity.getId());

        int runningUpdateCount = reportJobLogRepository.updateStatus(
                entity.getId(),
                ReportJobStatusEnum.RUNNING.getCode(),
                LocalDate.of(2026, 4, 5)
        );
        int successUpdateCount = reportJobLogRepository.updateSuccess(
                entity.getId(),
                ReportJobStatusEnum.SUCCESS.getCode(),
                LocalDateTime.of(2026, 4, 6, 12, 0)
        );
        int failedUpdateCount = reportJobLogRepository.updateFailure(
                entity.getId(),
                ReportJobStatusEnum.FAILED.getCode(),
                LocalDate.of(2026, 4, 6),
                "执行失败",
                LocalDateTime.of(2026, 4, 6, 13, 0)
        );

        Integer currentStatus = jdbcTemplate.queryForObject(
                "select status from energy_report_job_log where id = ?",
                Integer.class,
                entity.getId()
        );

        assertEquals(1, runningUpdateCount);
        assertEquals(1, successUpdateCount);
        assertEquals(1, failedUpdateCount);
        assertEquals(ReportJobStatusEnum.FAILED.getCode(), currentStatus);
    }

    @Test
    @DisplayName("电费报表账户汇总查询应支持区间聚合与名称过滤")
    void testDailyAccountReportRepository_ShouldAggregateElectricBillAccountSummary() {
        dailyAccountReportRepository.insert(List.of(
                createDailyAccountReportEntity(10, "企业A", 0, LocalDate.of(2026, 4, 5),
                        new BigDecimal("10.00"), new BigDecimal("12.30"), BigDecimal.ZERO,
                        new BigDecimal("20.00"), new BigDecimal("1.50"), new BigDecimal("14.80")),
                createDailyAccountReportEntity(10, "企业A", 0, LocalDate.of(2026, 4, 6),
                        new BigDecimal("8.50"), new BigDecimal("10.20"), BigDecimal.ZERO,
                        new BigDecimal("5.00"), new BigDecimal("-0.50"), new BigDecimal("10.70")),
                createDailyAccountReportEntity(20, "企业B", 1, LocalDate.of(2026, 4, 6),
                        new BigDecimal("0.00"), BigDecimal.ZERO, new BigDecimal("50.00"),
                        BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("50.00"))
        ));

        List<ElectricBillAccountSummaryQo> accountSummaryList = dailyAccountReportRepository.findElectricBillAccountPageList(
                new ElectricBillReportQueryQo()
                        .setAccountNameLike("企业A")
                        .setStartDate(LocalDate.of(2026, 4, 5))
                        .setEndDate(LocalDate.of(2026, 4, 6))
        );
        ElectricBillAccountSummaryQo accountSummary = dailyAccountReportRepository.getElectricBillAccountSummary(
                20, LocalDate.of(2026, 4, 5), LocalDate.of(2026, 4, 6)
        );

        assertEquals(1, accountSummaryList.size());
        assertEquals(10, accountSummaryList.get(0).getAccountId());
        assertEquals(0, new BigDecimal("18.50").compareTo(accountSummaryList.get(0).getPeriodConsumePower()));
        assertEquals(0, new BigDecimal("22.50").compareTo(accountSummaryList.get(0).getPeriodElectricChargeAmount()));
        assertEquals(0, new BigDecimal("25.00").compareTo(accountSummaryList.get(0).getPeriodRechargeAmount()));
        assertEquals(0, new BigDecimal("1.00").compareTo(accountSummaryList.get(0).getPeriodCorrectionAmount()));
        assertEquals(0, new BigDecimal("25.50").compareTo(accountSummaryList.get(0).getTotalDebitAmount()));

        assertNotNull(accountSummary);
        assertEquals(20, accountSummary.getAccountId());
        assertEquals(0, new BigDecimal("50.00").compareTo(accountSummary.getPeriodMonthlyChargeAmount()));
    }

    @Test
    @DisplayName("电费报表电表数量与区间明细查询应返回稳定结果")
    void testDailyMeterReportRepository_ShouldFindMeterCountAndDateRangeList() {
        dailyMeterReportRepository.insert(List.of(
                createDailyMeterReportEntity(10, 100, "设备100", "电表100", LocalDate.of(2026, 4, 5)),
                createDailyMeterReportEntity(10, 100, "设备100", "电表100", LocalDate.of(2026, 4, 6)),
                createDailyMeterReportEntity(10, 101, "设备101", "电表101", LocalDate.of(2026, 4, 6)),
                createDailyMeterReportEntity(20, 200, "设备200", "电表200", LocalDate.of(2026, 4, 6))
        ));
        DailyAccountReportEntity latestReport = new DailyAccountReportEntity()
                .setReportDate(LocalDate.of(2026, 4, 6))
                .setAccountId(10)
                .setOwnerName("企业A")
                .setElectricAccountType(0)
                .setEndBalance(new BigDecimal("88.80"));
        latestReport.setCreateTime(LocalDateTime.of(2026, 4, 7, 1, 0));
        dailyAccountReportRepository.insert(List.of(latestReport));

        List<ElectricBillMeterCountQo> meterCountList = dailyMeterReportRepository.findMeterCountListByAccountIdList(
                LocalDate.of(2026, 4, 5), LocalDate.of(2026, 4, 6), List.of(10, 20)
        );
        List<DailyMeterReportEntity> meterReportList = dailyMeterReportRepository.findListByAccountIdAndDateRange(
                10, LocalDate.of(2026, 4, 5), LocalDate.of(2026, 4, 6)
        );
        DailyAccountReportEntity latestAccountReport = dailyAccountReportRepository.getLatestByAccountIdAndDateRange(
                10, LocalDate.of(2026, 4, 5), LocalDate.of(2026, 4, 6)
        );

        Map<Integer, Integer> meterCountMap = meterCountList.stream()
                .collect(Collectors.toMap(ElectricBillMeterCountQo::getAccountId,
                        ElectricBillMeterCountQo::getMeterCount,
                        (left, right) -> right));

        assertEquals(2, meterCountMap.get(10));
        assertEquals(1, meterCountMap.get(20));
        assertEquals(3, meterReportList.size());
        assertEquals(100, meterReportList.get(0).getMeterId());
        assertEquals(100, meterReportList.get(1).getMeterId());
        assertEquals(101, meterReportList.get(2).getMeterId());
        assertNotNull(latestAccountReport);
        assertEquals(0, new BigDecimal("88.80").compareTo(latestAccountReport.getEndBalance()));
    }

    private DailyAccountReportEntity createDailyAccountReportEntity(Integer accountId,
                                                                    String ownerName,
                                                                    Integer electricAccountType,
                                                                    LocalDate reportDate,
                                                                    BigDecimal consumePower,
                                                                    BigDecimal electricChargeAmount,
                                                                    BigDecimal monthlyChargeAmount,
                                                                    BigDecimal rechargeAmount,
                                                                    BigDecimal correctionNetAmount,
                                                                    BigDecimal totalDebitAmount) {
        DailyAccountReportEntity entity = new DailyAccountReportEntity()
                .setReportDate(reportDate)
                .setAccountId(accountId)
                .setOwnerName(ownerName)
                .setElectricAccountType(electricAccountType)
                .setConsumePower(consumePower)
                .setElectricChargeAmount(electricChargeAmount)
                .setMonthlyChargeAmount(monthlyChargeAmount)
                .setRechargeAmount(rechargeAmount)
                .setCorrectionNetAmount(correctionNetAmount)
                .setTotalDebitAmount(totalDebitAmount);
        entity.setCreateTime(LocalDateTime.of(2026, 4, 7, 1, 0));
        return entity;
    }

    private DailyMeterReportEntity createDailyMeterReportEntity(Integer accountId,
                                                                Integer meterId,
                                                                String deviceNo,
                                                                String meterName,
                                                                LocalDate reportDate) {
        DailyMeterReportEntity entity = new DailyMeterReportEntity()
                .setReportDate(reportDate)
                .setAccountId(accountId)
                .setOwnerName("企业")
                .setMeterId(meterId)
                .setDeviceNo(deviceNo)
                .setMeterName(meterName)
                .setElectricAccountType(0)
                .setGenerateType(MeterReportGenerateTypeEnum.NORMAL.getCode());
        entity.setCreateTime(LocalDateTime.of(2026, 4, 7, 1, 0));
        return entity;
    }
}
