package info.zhihui.ems.business.report.entity;

import info.zhihui.ems.business.report.enums.MeterReportGenerateTypeEnum;
import info.zhihui.ems.business.report.enums.ReportJobStatusEnum;
import info.zhihui.ems.business.report.enums.ReportTriggerTypeEnum;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DailyReportEntityTest {

    @Test
    void testDailyMeterReport_ShouldKeepGenerateTypeCode() {
        DailyMeterReportEntity entity = new DailyMeterReportEntity()
                .setReportDate(LocalDate.of(2026, 4, 6))
                .setAccountId(10)
                .setMeterId(20)
                .setGenerateType(MeterReportGenerateTypeEnum.CANCEL.getCode());

        assertEquals(LocalDate.of(2026, 4, 6), entity.getReportDate());
        assertEquals(10, entity.getAccountId());
        assertEquals(20, entity.getMeterId());
        assertEquals(MeterReportGenerateTypeEnum.CANCEL.getCode(), entity.getGenerateType());
    }

    @Test
    void testDailyAccountReport_ShouldInitializeAccumulateAmountToZero() {
        DailyAccountReportEntity entity = new DailyAccountReportEntity();

        assertEquals(BigDecimal.ZERO, entity.getAccumulateConsumePower());
        assertEquals(BigDecimal.ZERO, entity.getAccumulateElectricChargeAmount());
        assertEquals(BigDecimal.ZERO, entity.getAccumulateMonthlyChargeAmount());
        assertEquals(BigDecimal.ZERO, entity.getAccumulateCorrectionPayAmount());
        assertEquals(BigDecimal.ZERO, entity.getAccumulateCorrectionRefundAmount());
        assertEquals(BigDecimal.ZERO, entity.getAccumulateRechargeAmount());
        assertEquals(BigDecimal.ZERO, entity.getAccumulateRechargeServiceFeeAmount());
        assertEquals(BigDecimal.ZERO, entity.getAccumulateTotalDebitAmount());
    }

    @Test
    void testReportEnums_ShouldExposeStableCodes() {
        assertEquals(0, MeterReportGenerateTypeEnum.NORMAL.getCode());
        assertEquals(1, MeterReportGenerateTypeEnum.ZERO.getCode());
        assertEquals(2, MeterReportGenerateTypeEnum.CANCEL.getCode());

        assertEquals(0, ReportTriggerTypeEnum.SCHEDULED.getCode());
        assertEquals(1, ReportTriggerTypeEnum.MANUAL.getCode());

        assertEquals(0, ReportJobStatusEnum.RUNNING.getCode());
        assertEquals(1, ReportJobStatusEnum.SUCCESS.getCode());
        assertEquals(2, ReportJobStatusEnum.FAILED.getCode());
    }
}
