package info.zhihui.ems.business.report.service.build.impl;

import info.zhihui.ems.business.account.entity.AccountOpenRecordEntity;
import info.zhihui.ems.business.billing.entity.ElectricMeterBalanceConsumeRecordEntity;
import info.zhihui.ems.business.billing.entity.ElectricMeterPowerConsumeRecordEntity;
import info.zhihui.ems.business.report.dto.DailyMeterBuildContextDto;
import info.zhihui.ems.business.report.entity.DailyMeterReportEntity;
import info.zhihui.ems.business.report.enums.MeterReportGenerateTypeEnum;
import info.zhihui.ems.business.report.qo.DailyMeterCandidateQo;
import info.zhihui.ems.business.report.qo.MeterSnapshotSourceQo;
import info.zhihui.ems.business.report.qo.PowerRecordSnapshotSourceQo;
import info.zhihui.ems.business.report.qo.RechargeSourceItemQo;
import info.zhihui.ems.business.report.qo.ReportDateRangeQo;
import info.zhihui.ems.business.device.entity.MeterCancelRecordEntity;
import info.zhihui.ems.business.device.entity.OpenMeterEntity;
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

@DisplayName("电表日报构建服务测试")
class DailyMeterReportBuilderTest {

    private final DailyMeterReportBuilder dailyMeterReportBuildService = new DailyMeterReportBuilder();

    @Test
    @DisplayName("按需账户正常用电应生成正常日报并独立统计补正")
    void testBuildDailyMeterReportList_QuantityNormal_ShouldBuildNormalReport() {
        DailyMeterBuildContextDto buildContext = createBaseContext()
                .setCandidateList(List.of(new DailyMeterCandidateQo().setAccountId(10).setMeterId(100)))
                .setPowerConsumeRecordList(List.of(
                        createPowerConsumeRecord(10, 100, LocalDateTime.of(2026, 4, 6, 8, 0),
                                "100.00", "103.00", "3.00"),
                        createPowerConsumeRecord(10, 100, LocalDateTime.of(2026, 4, 6, 20, 0),
                                "103.00", "105.00", "2.00")
                ))
                .setElectricChargeRecordList(List.of(
                        createChargeRecord(10, 100, ElectricAccountTypeEnum.QUANTITY.getCode(),
                                LocalDateTime.of(2026, 4, 6, 21, 0), "6.50", "20.00", "13.50")
                                .setConsumeAmountHigher(new BigDecimal("1.50"))
                                .setConsumeAmountHigh(new BigDecimal("2.00"))
                                .setConsumeAmountLow(new BigDecimal("3.00"))
                                .setPriceHigher(new BigDecimal("1.20"))
                                .setPriceHigh(new BigDecimal("1.00"))
                                .setPriceLow(new BigDecimal("0.80"))
                ))
                .setCorrectionRecordList(List.of(
                        createChargeRecord(10, 100, ElectricAccountTypeEnum.QUANTITY.getCode(),
                                LocalDateTime.of(2026, 4, 6, 12, 0), "5.00", null, null),
                        createChargeRecord(10, 100, ElectricAccountTypeEnum.QUANTITY.getCode(),
                                LocalDateTime.of(2026, 4, 6, 13, 0), "-2.00", null, null)
                ))
                .setMeterRechargeList(List.of(
                        new RechargeSourceItemQo()
                                .setOrderSn("ORDER-100")
                                .setAccountId(10)
                                .setMeterId(100)
                                .setBalanceType(BalanceTypeEnum.ELECTRIC_METER.getCode())
                                .setAmount(new BigDecimal("20.00"))
                                .setCreateTime(LocalDateTime.of(2026, 4, 6, 7, 0))
                ))
                .setPowerRecordSnapshotList(List.of(
                        new PowerRecordSnapshotSourceQo()
                                .setRecordId(1)
                                .setAccountId(10)
                                .setMeterId(100)
                                .setOwnerId(1000)
                                .setOwnerType(1)
                                .setOwnerName("企业A")
                                .setSpaceId(10000)
                                .setSpaceName("一层")
                                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY.getCode())
                                .setRecordTime(LocalDateTime.of(2026, 4, 6, 8, 0))
                ))
                .setAccountOpenRecordList(List.of(createAccountOpenRecord(10, ElectricAccountTypeEnum.QUANTITY.getCode(), "企业A")));

        List<DailyMeterReportEntity> reportList = dailyMeterReportBuildService.buildDailyMeterReportList(buildContext);

        assertEquals(1, reportList.size());
        DailyMeterReportEntity reportEntity = reportList.get(0);
        assertEquals(MeterReportGenerateTypeEnum.NORMAL.getCode(), reportEntity.getGenerateType());
        assertEquals(0, new BigDecimal("100.00").compareTo(reportEntity.getBeginPower()));
        assertEquals(0, new BigDecimal("105.00").compareTo(reportEntity.getEndPower()));
        assertEquals(0, new BigDecimal("5.00").compareTo(reportEntity.getConsumePower()));
        assertEquals(0, new BigDecimal("6.50").compareTo(reportEntity.getElectricChargeAmount()));
        assertEquals(0, new BigDecimal("5.00").compareTo(reportEntity.getCorrectionPayAmount()));
        assertEquals(0, new BigDecimal("2.00").compareTo(reportEntity.getCorrectionRefundAmount()));
        assertEquals(0, new BigDecimal("3.00").compareTo(reportEntity.getCorrectionNetAmount()));
        assertEquals(0, new BigDecimal("20.00").compareTo(reportEntity.getRechargeAmount()));
        assertEquals(0, new BigDecimal("20.00").compareTo(reportEntity.getBeginBalance()));
        assertEquals(0, new BigDecimal("13.50").compareTo(reportEntity.getEndBalance()));
        assertEquals(0, new BigDecimal("1.20").compareTo(reportEntity.getDisplayPriceHigher()));
    }

    @Test
    @DisplayName("包月账户零用电日报应承接读数且金额字段为null")
    void testBuildDailyMeterReportList_MonthlyZero_ShouldBuildZeroReportWithNullAmountFields() {
        DailyMeterBuildContextDto buildContext = createBaseContext()
                .setCandidateList(List.of(new DailyMeterCandidateQo().setAccountId(20).setMeterId(200)))
                .setPreviousReportList(List.of(
                        new DailyMeterReportEntity()
                                .setReportDate(LocalDate.of(2026, 4, 5))
                                .setAccountId(20)
                                .setMeterId(200)
                                .setElectricAccountType(ElectricAccountTypeEnum.MONTHLY.getCode())
                                .setGenerateType(MeterReportGenerateTypeEnum.NORMAL.getCode())
                                .setEndPower(new BigDecimal("300.00"))
                                .setEndPowerHigher(new BigDecimal("60.00"))
                                .setEndPowerHigh(new BigDecimal("80.00"))
                                .setEndPowerLow(new BigDecimal("90.00"))
                                .setEndPowerLower(new BigDecimal("70.00"))
                                .setEndPowerDeepLow(BigDecimal.ZERO)
                ))
                .setAccountOpenRecordList(List.of(createAccountOpenRecord(20, ElectricAccountTypeEnum.MONTHLY.getCode(), "包月企业")));

        List<DailyMeterReportEntity> reportList = dailyMeterReportBuildService.buildDailyMeterReportList(buildContext);

        assertEquals(1, reportList.size());
        DailyMeterReportEntity reportEntity = reportList.get(0);
        assertEquals(MeterReportGenerateTypeEnum.ZERO.getCode(), reportEntity.getGenerateType());
        assertEquals(0, new BigDecimal("300.00").compareTo(reportEntity.getBeginPower()));
        assertEquals(0, new BigDecimal("300.00").compareTo(reportEntity.getEndPower()));
        assertEquals(0, BigDecimal.ZERO.compareTo(reportEntity.getConsumePower()));
        assertNull(reportEntity.getElectricChargeAmount());
        assertNull(reportEntity.getCorrectionPayAmount());
        assertNull(reportEntity.getCorrectionRefundAmount());
        assertNull(reportEntity.getCorrectionNetAmount());
        assertNull(reportEntity.getRechargeAmount());
        assertNull(reportEntity.getBeginBalance());
        assertNull(reportEntity.getEndBalance());
    }

    @Test
    @DisplayName("按需账户开户首日只有开户记录时应直接使用账户开户类型")
    void testBuildDailyMeterReportList_QuantityOpenDayOnly_ShouldUseAccountOpenType() {
        DailyMeterBuildContextDto buildContext = createBaseContext()
                .setCandidateList(List.of(new DailyMeterCandidateQo().setAccountId(60).setMeterId(600)))
                .setOpenRecordList(List.of(
                        new OpenMeterEntity()
                                .setAccountId(60)
                                .setMeterId(600)
                                .setPower(new BigDecimal("10.00"))
                                .setShowTime(LocalDateTime.of(2026, 4, 6, 8, 0))
                ))
                .setAccountOpenRecordList(List.of(
                        new AccountOpenRecordEntity()
                                .setAccountId(60)
                                .setOwnerId(6000)
                                .setOwnerType(1)
                                .setOwnerName("企业开户")
                                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY.getCode())
                ))
                .setCurrentMeterSnapshotList(List.of(
                        new MeterSnapshotSourceQo()
                                .setAccountId(60)
                                .setMeterId(600)
                                .setMeterName("当前电表")
                                .setDeviceNo("METER-600")
                                .setSpaceId(66)
                                .setSpaceName("一层西")
                ));

        List<DailyMeterReportEntity> reportList = dailyMeterReportBuildService.buildDailyMeterReportList(buildContext);

        assertEquals(1, reportList.size());
        DailyMeterReportEntity reportEntity = reportList.get(0);
        assertEquals(ElectricAccountTypeEnum.QUANTITY.getCode(), reportEntity.getElectricAccountType());
        assertEquals("企业开户", reportEntity.getOwnerName());
        assertEquals("当前电表", reportEntity.getMeterName());
        assertEquals("METER-600", reportEntity.getDeviceNo());
        assertEquals("一层西", reportEntity.getSpaceName());
        assertEquals(0, BigDecimal.ZERO.compareTo(reportEntity.getElectricChargeAmount()));
        assertEquals(0, BigDecimal.ZERO.compareTo(reportEntity.getCorrectionPayAmount()));
    }

    @Test
    @DisplayName("包月账户开户首日只有开户记录时按量金额字段应为null")
    void testBuildDailyMeterReportList_MonthlyOpenDayOnly_ShouldUseAccountOpenType() {
        DailyMeterBuildContextDto buildContext = createBaseContext()
                .setCandidateList(List.of(new DailyMeterCandidateQo().setAccountId(61).setMeterId(610)))
                .setOpenRecordList(List.of(
                        new OpenMeterEntity()
                                .setAccountId(61)
                                .setMeterId(610)
                                .setPower(new BigDecimal("20.00"))
                                .setShowTime(LocalDateTime.of(2026, 4, 6, 9, 0))
                ))
                .setAccountOpenRecordList(List.of(
                        new AccountOpenRecordEntity()
                                .setAccountId(61)
                                .setOwnerId(6100)
                                .setOwnerType(1)
                                .setOwnerName("包月开户")
                                .setElectricAccountType(ElectricAccountTypeEnum.MONTHLY.getCode())
                ));

        List<DailyMeterReportEntity> reportList = dailyMeterReportBuildService.buildDailyMeterReportList(buildContext);

        assertEquals(1, reportList.size());
        DailyMeterReportEntity reportEntity = reportList.get(0);
        assertEquals(ElectricAccountTypeEnum.MONTHLY.getCode(), reportEntity.getElectricAccountType());
        assertNull(reportEntity.getElectricChargeAmount());
        assertNull(reportEntity.getCorrectionPayAmount());
        assertNull(reportEntity.getRechargeAmount());
    }

    @Test
    @DisplayName("候选电表列表为空时应直接返回空结果")
    void testBuildDailyMeterReportList_NullCandidateList_ShouldReturnEmptyList() {
        DailyMeterBuildContextDto buildContext = createBaseContext()
                .setCandidateList(null);

        List<DailyMeterReportEntity> reportList = dailyMeterReportBuildService.buildDailyMeterReportList(buildContext);

        assertEquals(Collections.emptyList(), reportList);
    }

    @Test
    @DisplayName("同账户同天开户后销户应生成销户日报")
    void testBuildDailyMeterReportList_OpenThenCancelSameDay_ShouldBuildCancelReport() {
        OpenMeterEntity openRecord = new OpenMeterEntity()
                .setId(1)
                .setAccountId(30)
                .setMeterId(300)
                .setPower(new BigDecimal("500.00"))
                .setShowTime(LocalDateTime.of(2026, 4, 6, 8, 0));
        openRecord.setCreateTime(LocalDateTime.of(2026, 4, 6, 8, 1));

        MeterCancelRecordEntity cancelRecord = new MeterCancelRecordEntity()
                .setId(2)
                .setAccountId(30)
                .setMeterId(300)
                .setPower(new BigDecimal("503.00"))
                .setShowTime(LocalDateTime.of(2026, 4, 6, 10, 0));
        cancelRecord.setCreateTime(LocalDateTime.of(2026, 4, 6, 10, 1));

        DailyMeterBuildContextDto buildContext = createBaseContext()
                .setCandidateList(List.of(new DailyMeterCandidateQo().setAccountId(30).setMeterId(300)))
                .setOpenRecordList(List.of(openRecord))
                .setCancelRecordList(List.of(cancelRecord))
                .setPowerRecordSnapshotList(List.of(
                        new PowerRecordSnapshotSourceQo()
                                .setRecordId(4)
                                .setAccountId(30)
                                .setMeterId(300)
                                .setElectricAccountType(ElectricAccountTypeEnum.MERGED.getCode())
                                .setRecordTime(LocalDateTime.of(2026, 4, 6, 8, 0))
                ))
                .setAccountOpenRecordList(List.of(createAccountOpenRecord(30, ElectricAccountTypeEnum.MERGED.getCode(), "合并企业")));

        List<DailyMeterReportEntity> reportList = dailyMeterReportBuildService.buildDailyMeterReportList(buildContext);

        assertEquals(1, reportList.size());
        DailyMeterReportEntity reportEntity = reportList.get(0);
        assertEquals(MeterReportGenerateTypeEnum.CANCEL.getCode(), reportEntity.getGenerateType());
        assertEquals(0, new BigDecimal("500.00").compareTo(reportEntity.getBeginPower()));
        assertEquals(0, new BigDecimal("503.00").compareTo(reportEntity.getEndPower()));
        assertNull(reportEntity.getBeginBalance());
        assertNull(reportEntity.getEndBalance());
        assertNull(reportEntity.getRechargeAmount());
    }

    @Test
    @DisplayName("同天跨账户迁移应生成两条日报且原账户最终销户")
    void testBuildDailyMeterReportList_MigrateAcrossAccount_ShouldBuildCancelAndZeroReports() {
        DailyMeterBuildContextDto buildContext = createBaseContext()
                .setCandidateList(List.of(
                        new DailyMeterCandidateQo().setAccountId(40).setMeterId(400),
                        new DailyMeterCandidateQo().setAccountId(41).setMeterId(400)
                ))
                .setPreviousReportList(List.of(
                        new DailyMeterReportEntity()
                                .setReportDate(LocalDate.of(2026, 4, 5))
                                .setAccountId(40)
                                .setMeterId(400)
                                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY.getCode())
                                .setGenerateType(MeterReportGenerateTypeEnum.NORMAL.getCode())
                                .setEndPower(new BigDecimal("500.00"))
                ))
                .setOpenRecordList(List.of(
                        new OpenMeterEntity()
                                .setId(11)
                                .setAccountId(41)
                                .setMeterId(400)
                                .setPower(new BigDecimal("505.00"))
                                .setShowTime(LocalDateTime.of(2026, 4, 6, 10, 0))
                ))
                .setCancelRecordList(List.of(
                        new MeterCancelRecordEntity()
                                .setId(10)
                                .setAccountId(40)
                                .setMeterId(400)
                                .setPower(new BigDecimal("505.00"))
                                .setMeterName("迁移电表")
                                .setDeviceNo("METER-400")
                                .setShowTime(LocalDateTime.of(2026, 4, 6, 9, 0))
                ))
                .setPowerRecordSnapshotList(List.of(
                        new PowerRecordSnapshotSourceQo()
                                .setRecordId(12)
                                .setAccountId(41)
                                .setMeterId(400)
                                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY.getCode())
                                .setRecordTime(LocalDateTime.of(2026, 4, 6, 10, 0))
                ))
                .setAccountOpenRecordList(List.of(
                        createAccountOpenRecord(40, ElectricAccountTypeEnum.QUANTITY.getCode(), "迁出账户"),
                        createAccountOpenRecord(41, ElectricAccountTypeEnum.QUANTITY.getCode(), "迁入账户")
                ));

        List<DailyMeterReportEntity> reportList = dailyMeterReportBuildService.buildDailyMeterReportList(buildContext);

        assertEquals(2, reportList.size());
        DailyMeterReportEntity cancelReportEntity = reportList.get(0);
        DailyMeterReportEntity zeroReportEntity = reportList.get(1);

        assertEquals(40, cancelReportEntity.getAccountId());
        assertEquals(MeterReportGenerateTypeEnum.CANCEL.getCode(), cancelReportEntity.getGenerateType());
        assertEquals(0, new BigDecimal("500.00").compareTo(cancelReportEntity.getBeginPower()));
        assertEquals(0, new BigDecimal("505.00").compareTo(cancelReportEntity.getEndPower()));

        assertEquals(41, zeroReportEntity.getAccountId());
        assertEquals(MeterReportGenerateTypeEnum.ZERO.getCode(), zeroReportEntity.getGenerateType());
        assertEquals(0, new BigDecimal("505.00").compareTo(zeroReportEntity.getBeginPower()));
        assertEquals(0, new BigDecimal("505.00").compareTo(zeroReportEntity.getEndPower()));
    }

    @Test
    @DisplayName("按需账户仅充值日应使用充值流水余额作为日电表余额")
    void testBuildDailyMeterReportList_OnlyRecharge_ShouldUseRechargeBalance() {
        RechargeSourceItemQo rechargeSourceItemQo = new RechargeSourceItemQo()
                .setOrderSn("ORDER-ONLY-RECHARGE")
                .setAccountId(50)
                .setMeterId(500)
                .setBalanceType(BalanceTypeEnum.ELECTRIC_METER.getCode())
                .setAmount(new BigDecimal("20.00"))
                .setBeginBalance(new BigDecimal("10.00"))
                .setEndBalance(new BigDecimal("30.00"))
                .setCreateTime(LocalDateTime.of(2026, 4, 6, 9, 0));
        DailyMeterBuildContextDto buildContext = createBaseContext()
                .setCandidateList(List.of(new DailyMeterCandidateQo().setAccountId(50).setMeterId(500)))
                .setPreviousReportList(List.of(
                        new DailyMeterReportEntity()
                                .setReportDate(LocalDate.of(2026, 4, 5))
                                .setAccountId(50)
                                .setMeterId(500)
                                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY.getCode())
                                .setGenerateType(MeterReportGenerateTypeEnum.NORMAL.getCode())
                                .setEndPower(new BigDecimal("800.00"))
                                .setEndBalance(new BigDecimal("10.00"))
                ))
                .setAccountOpenRecordList(List.of(createAccountOpenRecord(50, ElectricAccountTypeEnum.QUANTITY.getCode(), "充值账户")))
                .setMeterRechargeList(List.of(rechargeSourceItemQo));

        List<DailyMeterReportEntity> reportList = dailyMeterReportBuildService.buildDailyMeterReportList(buildContext);

        assertEquals(1, reportList.size());
        DailyMeterReportEntity reportEntity = reportList.get(0);
        assertEquals(MeterReportGenerateTypeEnum.ZERO.getCode(), reportEntity.getGenerateType());
        assertEquals(0, new BigDecimal("10.00").compareTo(reportEntity.getBeginBalance()));
        assertEquals(0, new BigDecimal("30.00").compareTo(reportEntity.getEndBalance()));
        assertEquals(0, new BigDecimal("20.00").compareTo(reportEntity.getRechargeAmount()));
    }

    private DailyMeterBuildContextDto createBaseContext() {
        return new DailyMeterBuildContextDto()
                .setReportDateRange(new ReportDateRangeQo()
                        .setReportDate(LocalDate.of(2026, 4, 6))
                        .setPreviousReportDate(LocalDate.of(2026, 4, 5))
                        .setBeginTime(LocalDateTime.of(2026, 4, 6, 0, 0))
                        .setEndTime(LocalDateTime.of(2026, 4, 7, 0, 0)))
                .setCandidateList(Collections.emptyList())
                .setPreviousReportList(Collections.emptyList())
                .setPowerConsumeRecordList(Collections.emptyList())
                .setElectricChargeRecordList(Collections.emptyList())
                .setCorrectionRecordList(Collections.emptyList())
                .setMeterRechargeList(Collections.emptyList())
                .setOpenRecordList(Collections.emptyList())
                .setCancelRecordList(Collections.emptyList())
                .setPowerRecordSnapshotList(Collections.emptyList())
                .setAccountOpenRecordList(Collections.emptyList())
                .setCurrentMeterSnapshotList(Collections.emptyList());
    }

    private ElectricMeterPowerConsumeRecordEntity createPowerConsumeRecord(Integer accountId,
                                                                           Integer meterId,
                                                                           LocalDateTime consumeTime,
                                                                           String beginPower,
                                                                           String endPower,
                                                                           String consumePower) {
        return new ElectricMeterPowerConsumeRecordEntity()
                .setAccountId(accountId)
                .setMeterId(meterId)
                .setBeginPower(new BigDecimal(beginPower))
                .setEndPower(new BigDecimal(endPower))
                .setConsumePower(new BigDecimal(consumePower))
                .setMeterConsumeTime(consumeTime);
    }

    private ElectricMeterBalanceConsumeRecordEntity createChargeRecord(Integer accountId,
                                                                       Integer meterId,
                                                                       Integer electricAccountType,
                                                                       LocalDateTime consumeTime,
                                                                       String consumeAmount,
                                                                       String beginBalance,
                                                                       String endBalance) {
        ElectricMeterBalanceConsumeRecordEntity recordEntity = new ElectricMeterBalanceConsumeRecordEntity()
                .setAccountId(accountId)
                .setMeterId(meterId)
                .setElectricAccountType(electricAccountType)
                .setConsumeAmount(new BigDecimal(consumeAmount))
                .setMeterConsumeTime(consumeTime);
        if (beginBalance != null) {
            recordEntity.setBeginBalance(new BigDecimal(beginBalance));
        }
        if (endBalance != null) {
            recordEntity.setEndBalance(new BigDecimal(endBalance));
        }
        return recordEntity;
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
}
