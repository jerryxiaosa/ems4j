package info.zhihui.ems.business.report.service.build.impl;

import info.zhihui.ems.business.account.entity.AccountOpenRecordEntity;
import info.zhihui.ems.business.billing.entity.AccountBalanceConsumeRecordEntity;
import info.zhihui.ems.business.billing.entity.ElectricMeterBalanceConsumeRecordEntity;
import info.zhihui.ems.business.billing.entity.OrderFlowEntity;
import info.zhihui.ems.business.report.dto.DailyAccountBuildContextDto;
import info.zhihui.ems.business.report.entity.DailyAccountReportEntity;
import info.zhihui.ems.business.report.entity.DailyMeterReportEntity;
import info.zhihui.ems.business.report.qo.RechargeSourceItemQo;
import info.zhihui.ems.business.report.qo.ReportDateRangeQo;
import info.zhihui.ems.common.enums.BalanceTypeEnum;
import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("账户日报构建服务测试")
class DailyAccountReportBuilderTest {

    private final DailyAccountReportBuilder dailyAccountReportBuildService = new DailyAccountReportBuilder();

    @Test
    @DisplayName("按需账户应按电表日报汇总并基于前一日报计算累计值")
    void testBuildDailyAccountReportList_Quantity_ShouldAggregateMeterReportAndAccumulate() {
        DailyAccountBuildContextDto buildContext = createBaseContext()
                .setPreviousReportList(List.of(
                        new DailyAccountReportEntity()
                                .setReportDate(LocalDate.of(2026, 4, 5))
                                .setAccountId(10)
                                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY.getCode())
                                .setEndBalance(new BigDecimal("100.00"))
                                .setAccumulateConsumePower(new BigDecimal("50.00"))
                                .setAccumulateElectricChargeAmount(new BigDecimal("60.00"))
                                .setAccumulateCorrectionPayAmount(new BigDecimal("5.00"))
                                .setAccumulateCorrectionRefundAmount(new BigDecimal("2.00"))
                                .setAccumulateRechargeAmount(new BigDecimal("80.00"))
                                .setAccumulateRechargeServiceFeeAmount(new BigDecimal("4.00"))
                                .setAccumulateTotalDebitAmount(new BigDecimal("67.00"))
                ))
                .setAccountOpenRecordList(List.of(createAccountOpenRecord(10, ElectricAccountTypeEnum.QUANTITY.getCode(), "按需账户")))
                .setDailyMeterReportList(List.of(
                        createDailyMeterReport(10, 100, ElectricAccountTypeEnum.QUANTITY.getCode(),
                                "3.00", "4.50", "2.00", "1.00", "1.00", "20.00", "15.50"),
                        createDailyMeterReport(10, 101, ElectricAccountTypeEnum.QUANTITY.getCode(),
                                "2.00", "3.00", "0.50", "0", "0.50", "30.00", "27.00"),
                        createDailyMeterReport(10, 101, ElectricAccountTypeEnum.QUANTITY.getCode(),
                                "2.00", "3.00", "0.50", "0", "0.50", "30.00", "27.00")
                ))
                .setRechargeServiceFeeList(List.of(
                        new RechargeSourceItemQo()
                                .setOrderSn("ORDER-Q-1")
                                .setAccountId(10)
                                .setServiceAmount(new BigDecimal("2.00"))
                                .setCreateTime(LocalDateTime.of(2026, 4, 6, 9, 0))
                ));

        List<DailyAccountReportEntity> reportList = dailyAccountReportBuildService.buildDailyAccountReportList(buildContext);

        assertEquals(1, reportList.size());
        DailyAccountReportEntity reportEntity = reportList.get(0);
        assertEquals(2, reportEntity.getMeterCount());
        assertEquals(0, new BigDecimal("7.00").compareTo(reportEntity.getConsumePower()));
        assertEquals(0, new BigDecimal("10.50").compareTo(reportEntity.getElectricChargeAmount()));
        assertEquals(0, new BigDecimal("3.00").compareTo(reportEntity.getCorrectionPayAmount()));
        assertEquals(0, new BigDecimal("1.00").compareTo(reportEntity.getCorrectionRefundAmount()));
        assertEquals(0, new BigDecimal("2.00").compareTo(reportEntity.getCorrectionNetAmount()));
        assertEquals(0, new BigDecimal("2.00").compareTo(reportEntity.getRechargeAmount()));
        assertEquals(0, new BigDecimal("80.00").compareTo(reportEntity.getBeginBalance()));
        assertEquals(0, new BigDecimal("69.50").compareTo(reportEntity.getEndBalance()));
        assertEquals(0, new BigDecimal("2.00").compareTo(reportEntity.getRechargeServiceFeeAmount()));
        assertEquals(0, new BigDecimal("14.50").compareTo(reportEntity.getTotalDebitAmount()));
        assertEquals(0, new BigDecimal("57.00").compareTo(reportEntity.getAccumulateConsumePower()));
        assertEquals(0, new BigDecimal("70.50").compareTo(reportEntity.getAccumulateElectricChargeAmount()));
        assertEquals(0, new BigDecimal("8.00").compareTo(reportEntity.getAccumulateCorrectionPayAmount()));
        assertEquals(0, new BigDecimal("3.00").compareTo(reportEntity.getAccumulateCorrectionRefundAmount()));
        assertEquals(0, new BigDecimal("82.00").compareTo(reportEntity.getAccumulateRechargeAmount()));
        assertEquals(0, new BigDecimal("6.00").compareTo(reportEntity.getAccumulateRechargeServiceFeeAmount()));
        assertEquals(0, new BigDecimal("81.50").compareTo(reportEntity.getAccumulateTotalDebitAmount()));
    }

    @Test
    @DisplayName("合并账户余额应取账户流水且充值到账来自账户级流水")
    void testBuildDailyAccountReportList_Merged_ShouldUseAccountOrderFlowBalance() {
        DailyAccountBuildContextDto buildContext = createBaseContext()
                .setDailyMeterReportList(List.of(
                        createDailyMeterReport(20, 200, ElectricAccountTypeEnum.MERGED.getCode(),
                                "4.00", "8.00", null, null, null, null, null)
                ))
                .setAccountOpenRecordList(List.of(createAccountOpenRecord(20, ElectricAccountTypeEnum.MERGED.getCode(), "合并账户")))
                .setAccountOrderFlowList(List.of(
                        createOrderFlow(20, LocalDateTime.of(2026, 4, 6, 8, 0), "100.00", "120.00"),
                        createOrderFlow(20, LocalDateTime.of(2026, 4, 6, 20, 0), "120.00", "110.00")
                ))
                .setAccountRechargeList(List.of(
                        new RechargeSourceItemQo()
                                .setOrderSn("ORDER-M-1")
                                .setAccountId(20)
                                .setBalanceType(BalanceTypeEnum.ACCOUNT.getCode())
                                .setAmount(new BigDecimal("20.00"))
                                .setCreateTime(LocalDateTime.of(2026, 4, 6, 8, 0))
                ))
                .setRechargeServiceFeeList(List.of(
                        new RechargeSourceItemQo()
                                .setOrderSn("ORDER-M-1")
                                .setAccountId(20)
                                .setServiceAmount(new BigDecimal("3.00"))
                                .setCreateTime(LocalDateTime.of(2026, 4, 6, 8, 0))
                ));

        List<DailyAccountReportEntity> reportList = dailyAccountReportBuildService.buildDailyAccountReportList(buildContext);

        assertEquals(1, reportList.size());
        DailyAccountReportEntity reportEntity = reportList.get(0);
        assertEquals(0, new BigDecimal("100.00").compareTo(reportEntity.getBeginBalance()));
        assertEquals(0, new BigDecimal("110.00").compareTo(reportEntity.getEndBalance()));
        assertEquals(0, new BigDecimal("20.00").compareTo(reportEntity.getRechargeAmount()));
        assertEquals(0, new BigDecimal("3.00").compareTo(reportEntity.getRechargeServiceFeeAmount()));
        assertEquals(0, new BigDecimal("11.00").compareTo(reportEntity.getTotalDebitAmount()));
    }

    @Test
    @DisplayName("合并账户补正应直接汇总账户级补正事实")
    void testBuildDailyAccountReportList_Merged_ShouldUseAccountCorrectionRecord() {
        DailyAccountBuildContextDto buildContext = createBaseContext()
                .setDailyMeterReportList(List.of(
                        createDailyMeterReport(21, 210, ElectricAccountTypeEnum.MERGED.getCode(),
                                "4.00", "8.00", null, null, null, null, null)
                ))
                .setAccountOpenRecordList(List.of(createAccountOpenRecord(21, ElectricAccountTypeEnum.MERGED.getCode(), "合并账户")))
                .setAccountCorrectionRecordList(List.of(
                        createCorrectionRecord(21, 210, "5.00"),
                        createCorrectionRecord(21, 210, "-2.00")
                ));

        List<DailyAccountReportEntity> reportList = dailyAccountReportBuildService.buildDailyAccountReportList(buildContext);

        assertEquals(1, reportList.size());
        DailyAccountReportEntity reportEntity = reportList.get(0);
        assertEquals(0, new BigDecimal("5.00").compareTo(reportEntity.getCorrectionPayAmount()));
        assertEquals(0, new BigDecimal("2.00").compareTo(reportEntity.getCorrectionRefundAmount()));
        assertEquals(0, new BigDecimal("3.00").compareTo(reportEntity.getCorrectionNetAmount()));
        assertEquals(0, new BigDecimal("11.00").compareTo(reportEntity.getTotalDebitAmount()));
    }

    @Test
    @DisplayName("包月账户无扣费日应生成零日报并承接前一日账户余额")
    void testBuildDailyAccountReportList_MonthlyWithoutCharge_ShouldCarryPreviousBalance() {
        DailyAccountBuildContextDto buildContext = createBaseContext()
                .setPreviousReportList(List.of(
                        new DailyAccountReportEntity()
                                .setReportDate(LocalDate.of(2026, 4, 5))
                                .setAccountId(30)
                                .setElectricAccountType(ElectricAccountTypeEnum.MONTHLY.getCode())
                                .setEndBalance(new BigDecimal("50.00"))
                                .setAccumulateConsumePower(new BigDecimal("10.00"))
                                .setAccumulateMonthlyChargeAmount(new BigDecimal("30.00"))
                                .setAccumulateTotalDebitAmount(new BigDecimal("30.00"))
                ))
                .setAccountOpenRecordList(List.of(createAccountOpenRecord(30, ElectricAccountTypeEnum.MONTHLY.getCode(), "包月账户")));

        List<DailyAccountReportEntity> reportList = dailyAccountReportBuildService.buildDailyAccountReportList(buildContext);

        assertEquals(1, reportList.size());
        DailyAccountReportEntity reportEntity = reportList.get(0);
        assertEquals(ElectricAccountTypeEnum.MONTHLY.getCode(), reportEntity.getElectricAccountType());
        assertEquals(0, reportEntity.getMeterCount());
        assertNull(reportEntity.getElectricChargeAmount());
        assertNull(reportEntity.getCorrectionPayAmount());
        assertEquals(0, BigDecimal.ZERO.compareTo(reportEntity.getMonthlyChargeAmount()));
        assertEquals(0, BigDecimal.ZERO.compareTo(reportEntity.getRechargeAmount()));
        assertEquals(0, new BigDecimal("50.00").compareTo(reportEntity.getBeginBalance()));
        assertEquals(0, new BigDecimal("50.00").compareTo(reportEntity.getEndBalance()));
        assertEquals(0, new BigDecimal("10.00").compareTo(reportEntity.getAccumulateConsumePower()));
        assertEquals(0, new BigDecimal("30.00").compareTo(reportEntity.getAccumulateMonthlyChargeAmount()));
        assertEquals(0, new BigDecimal("30.00").compareTo(reportEntity.getAccumulateTotalDebitAmount()));
    }

    @Test
    @DisplayName("包月账户扣费日应只统计包月金额不统计按需金额")
    void testBuildDailyAccountReportList_MonthlyChargeDay_ShouldOnlySetMonthlyAmount() {
        DailyAccountBuildContextDto buildContext = createBaseContext()
                .setDailyMeterReportList(List.of(
                        createDailyMeterReport(31, 310, ElectricAccountTypeEnum.MONTHLY.getCode(),
                                "6.00", null, null, null, null, null, null)
                ))
                .setAccountOpenRecordList(List.of(createAccountOpenRecord(31, ElectricAccountTypeEnum.MONTHLY.getCode(), "包月账户")))
                .setMonthlyConsumeRecordList(List.of(
                        new AccountBalanceConsumeRecordEntity()
                                .setAccountId(31)
                                .setPayAmount(new BigDecimal("30.00"))
                                .setConsumeTime(LocalDateTime.of(2026, 4, 6, 8, 0))
                ));

        List<DailyAccountReportEntity> reportList = dailyAccountReportBuildService.buildDailyAccountReportList(buildContext);

        assertEquals(1, reportList.size());
        DailyAccountReportEntity reportEntity = reportList.get(0);
        assertNull(reportEntity.getElectricChargeAmount());
        assertEquals(0, new BigDecimal("30.00").compareTo(reportEntity.getMonthlyChargeAmount()));
        assertEquals(0, new BigDecimal("30.00").compareTo(reportEntity.getTotalDebitAmount()));
    }

    @Test
    @DisplayName("账户日报应优先使用账户开户快照中的类型和主体信息")
    void testBuildDailyAccountReportList_ShouldUseAccountOpenRecordSnapshot() {
        DailyAccountBuildContextDto buildContext = createBaseContext()
                .setDailyMeterReportList(List.of(
                        new DailyMeterReportEntity()
                                .setAccountId(40)
                                .setMeterId(400)
                                .setConsumePower(new BigDecimal("5.00"))
                                .setElectricChargeAmount(new BigDecimal("6.00"))
                ))
                .setAccountOpenRecordList(List.of(
                        new AccountOpenRecordEntity()
                                .setAccountId(40)
                                .setOwnerId(4000)
                                .setOwnerType(1)
                                .setOwnerName("稳定业主")
                                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY.getCode())
                ));

        List<DailyAccountReportEntity> reportList = dailyAccountReportBuildService.buildDailyAccountReportList(buildContext);

        assertEquals(1, reportList.size());
        DailyAccountReportEntity reportEntity = reportList.get(0);
        assertEquals(ElectricAccountTypeEnum.QUANTITY.getCode(), reportEntity.getElectricAccountType());
        assertEquals(4000, reportEntity.getOwnerId());
        assertEquals(1, reportEntity.getOwnerType());
        assertEquals("稳定业主", reportEntity.getOwnerName());
    }

    private DailyAccountBuildContextDto createBaseContext() {
        return new DailyAccountBuildContextDto()
                .setReportDateRange(new ReportDateRangeQo()
                        .setReportDate(LocalDate.of(2026, 4, 6))
                        .setPreviousReportDate(LocalDate.of(2026, 4, 5))
                        .setBeginTime(LocalDateTime.of(2026, 4, 6, 0, 0))
                        .setEndTime(LocalDateTime.of(2026, 4, 7, 0, 0)))
                .setPreviousReportList(Collections.emptyList())
                .setDailyMeterReportList(Collections.emptyList())
                .setMonthlyConsumeRecordList(Collections.emptyList())
                .setAccountOrderFlowList(Collections.emptyList())
                .setAccountCorrectionRecordList(Collections.emptyList())
                .setAccountRechargeList(Collections.emptyList())
                .setRechargeServiceFeeList(Collections.emptyList())
                .setAccountOpenRecordList(Collections.emptyList());
    }

    private DailyMeterReportEntity createDailyMeterReport(Integer accountId,
                                                          Integer meterId,
                                                          Integer electricAccountType,
                                                          String consumePower,
                                                          String electricChargeAmount,
                                                          String correctionPayAmount,
                                                          String correctionRefundAmount,
                                                          String rechargeAmount,
                                                          String beginBalance,
                                                          String endBalance) {
        DailyMeterReportEntity reportEntity = new DailyMeterReportEntity()
                .setReportDate(LocalDate.of(2026, 4, 6))
                .setAccountId(accountId)
                .setMeterId(meterId)
                .setElectricAccountType(electricAccountType)
                .setConsumePower(new BigDecimal(consumePower));
        if (electricChargeAmount != null) {
            reportEntity.setElectricChargeAmount(new BigDecimal(electricChargeAmount));
        }
        if (correctionPayAmount != null) {
            reportEntity.setCorrectionPayAmount(new BigDecimal(correctionPayAmount));
        }
        if (correctionRefundAmount != null) {
            reportEntity.setCorrectionRefundAmount(new BigDecimal(correctionRefundAmount));
            reportEntity.setCorrectionNetAmount(reportEntity.getCorrectionPayAmount().subtract(reportEntity.getCorrectionRefundAmount()));
        }
        if (rechargeAmount != null) {
            reportEntity.setRechargeAmount(new BigDecimal(rechargeAmount));
        }
        if (beginBalance != null) {
            reportEntity.setBeginBalance(new BigDecimal(beginBalance));
        }
        if (endBalance != null) {
            reportEntity.setEndBalance(new BigDecimal(endBalance));
        }
        return reportEntity;
    }

    private OrderFlowEntity createOrderFlow(Integer accountId,
                                            LocalDateTime createTime,
                                            String beginBalance,
                                            String endBalance) {
        return new OrderFlowEntity()
                .setAccountId(accountId)
                .setBalanceType(BalanceTypeEnum.ACCOUNT.getCode())
                .setBeginBalance(new BigDecimal(beginBalance))
                .setEndBalance(new BigDecimal(endBalance))
                .setCreateTime(createTime);
    }

    private AccountOpenRecordEntity createAccountOpenRecord(Integer accountId,
                                                            Integer electricAccountType,
                                                            String ownerName) {
        return new AccountOpenRecordEntity()
                .setAccountId(accountId)
                .setOwnerId(accountId * 100)
                .setOwnerType(1)
                .setOwnerName(ownerName)
                .setElectricAccountType(electricAccountType);
    }

    private ElectricMeterBalanceConsumeRecordEntity createCorrectionRecord(Integer accountId,
                                                                           Integer meterId,
                                                                           String consumeAmount) {
        return new ElectricMeterBalanceConsumeRecordEntity()
                .setAccountId(accountId)
                .setMeterId(meterId)
                .setConsumeAmount(new BigDecimal(consumeAmount));
    }
}
