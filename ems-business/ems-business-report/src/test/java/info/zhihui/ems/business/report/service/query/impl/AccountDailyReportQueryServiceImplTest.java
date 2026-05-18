package info.zhihui.ems.business.report.service.query.impl;

import info.zhihui.ems.business.report.bo.AccountDailyReportBo;
import info.zhihui.ems.business.report.bo.AccountDailyReportSummaryBo;
import info.zhihui.ems.business.report.entity.DailyAccountReportEntity;
import info.zhihui.ems.business.report.repository.report.DailyAccountReportRepository;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountDailyReportQueryServiceImplTest {

    @Test
    void findAccountDailyReportList_ShouldMapRepositoryEntities() {
        LocalDate startDate = LocalDate.of(2026, 5, 1);
        LocalDate endDate = LocalDate.of(2026, 5, 7);
        FakeRepository fakeRepository = new FakeRepository()
                .setDailyReportList(List.of(new DailyAccountReportEntity()
                        .setReportDate(LocalDate.of(2026, 5, 2))
                        .setAccountId(20)
                        .setElectricAccountType(ElectricAccountTypeEnum.MERGED.getCode())
                        .setConsumePower(new BigDecimal("12.34"))
                        .setElectricChargeAmount(new BigDecimal("8.88"))));
        AccountDailyReportQueryServiceImpl service = new AccountDailyReportQueryServiceImpl(fakeRepository.create());

        List<AccountDailyReportBo> result = service.findAccountDailyReportList(20, startDate, endDate);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAccountId()).isEqualTo(20);
        assertThat(result.get(0).getReportDate()).isEqualTo(LocalDate.of(2026, 5, 2));
        assertThat(result.get(0).getConsumePower()).isEqualByComparingTo("12.34");
        assertThat(result.get(0).getElectricChargeAmount()).isEqualByComparingTo("8.88");
        assertThat(result.get(0).getResolvedChargeAmount()).isEqualByComparingTo("8.88");
        assertThat(fakeRepository.getCapturedAccountId()).isEqualTo(20);
        assertThat(fakeRepository.getCapturedStartDate()).isEqualTo(startDate);
        assertThat(fakeRepository.getCapturedEndDate()).isEqualTo(endDate);
    }

    @Test
    void findAccountDailyReportList_WhenMonthlyAccount_ShouldResolveMonthlyChargeAmount() {
        LocalDate startDate = LocalDate.of(2026, 5, 1);
        LocalDate endDate = LocalDate.of(2026, 5, 7);
        FakeRepository fakeRepository = new FakeRepository()
                .setDailyReportList(List.of(new DailyAccountReportEntity()
                        .setReportDate(LocalDate.of(2026, 5, 2))
                        .setAccountId(20)
                        .setElectricAccountType(ElectricAccountTypeEnum.MONTHLY.getCode())
                        .setConsumePower(new BigDecimal("12.34"))
                        .setElectricChargeAmount(null)
                        .setMonthlyChargeAmount(new BigDecimal("99.00"))));
        AccountDailyReportQueryServiceImpl service = new AccountDailyReportQueryServiceImpl(fakeRepository.create());

        List<AccountDailyReportBo> result = service.findAccountDailyReportList(20, startDate, endDate);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getElectricChargeAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.get(0).getResolvedChargeAmount()).isEqualByComparingTo("99.00");
    }

    @Test
    void getAccountDailyReportSummary_WhenRepositoryReturnsEmptyList_ShouldReturnZeroSummary() {
        LocalDate startDate = LocalDate.of(2026, 4, 1);
        LocalDate endDate = LocalDate.of(2026, 4, 30);
        AccountDailyReportQueryServiceImpl service = new AccountDailyReportQueryServiceImpl(new FakeRepository().create());

        AccountDailyReportSummaryBo result = service.getAccountDailyReportSummary(20, startDate, endDate);

        assertThat(result.getAccountId()).isEqualTo(20);
        assertThat(result.getConsumePower()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getElectricChargeAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getResolvedChargeAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getAccountDailyReportSummary_ShouldAggregateDailyReportsInJava() {
        LocalDate startDate = LocalDate.of(2026, 4, 1);
        LocalDate endDate = LocalDate.of(2026, 4, 30);
        AccountDailyReportQueryServiceImpl service = new AccountDailyReportQueryServiceImpl(new FakeRepository()
                .setDailyReportList(List.of(
                        new DailyAccountReportEntity()
                                .setAccountId(20)
                                .setElectricAccountType(ElectricAccountTypeEnum.MERGED.getCode())
                                .setConsumePower(new BigDecimal("100.10"))
                                .setElectricChargeAmount(new BigDecimal("60.20")),
                        new DailyAccountReportEntity()
                                .setAccountId(20)
                                .setElectricAccountType(ElectricAccountTypeEnum.MONTHLY.getCode())
                                .setConsumePower(new BigDecimal("186.30"))
                                .setElectricChargeAmount(null)
                                .setMonthlyChargeAmount(new BigDecimal("99.00"))
                ))
                .create());

        AccountDailyReportSummaryBo result = service.getAccountDailyReportSummary(20, startDate, endDate);

        assertThat(result.getAccountId()).isEqualTo(20);
        assertThat(result.getConsumePower()).isEqualByComparingTo("286.40");
        assertThat(result.getElectricChargeAmount()).isEqualByComparingTo("60.20");
        assertThat(result.getResolvedChargeAmount()).isEqualByComparingTo("159.20");
    }

    @Test
    void getAccountDailyReportSummary_WhenDateRangeExceeds366Days_ShouldThrowException() {
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 1);
        AccountDailyReportQueryServiceImpl service = new AccountDailyReportQueryServiceImpl(new FakeRepository().create());

        assertThatThrownBy(() -> service.getAccountDailyReportSummary(20, startDate, endDate))
                .isInstanceOf(BusinessRuntimeException.class)
                .hasMessage("账户日报汇总日期范围不能超过一年");
    }

    private static class FakeRepository {
        private List<DailyAccountReportEntity> dailyReportList = Collections.emptyList();
        private Integer capturedAccountId;
        private LocalDate capturedStartDate;
        private LocalDate capturedEndDate;

        private FakeRepository setDailyReportList(List<DailyAccountReportEntity> dailyReportList) {
            this.dailyReportList = dailyReportList;
            return this;
        }

        private Integer getCapturedAccountId() {
            return capturedAccountId;
        }

        private LocalDate getCapturedStartDate() {
            return capturedStartDate;
        }

        private LocalDate getCapturedEndDate() {
            return capturedEndDate;
        }

        private DailyAccountReportRepository create() {
            return (DailyAccountReportRepository) Proxy.newProxyInstance(
                    DailyAccountReportRepository.class.getClassLoader(),
                    new Class<?>[]{DailyAccountReportRepository.class},
                    (proxy, method, args) -> {
                        if ("findListByAccountIdAndDateRange".equals(method.getName())) {
                            capture(args);
                            return dailyReportList;
                        }
                        if ("toString".equals(method.getName())) {
                            return "FakeDailyAccountReportRepository";
                        }
                        throw new UnsupportedOperationException(method.getName());
                    });
        }

        private void capture(Object[] args) {
            capturedAccountId = (Integer) args[0];
            capturedStartDate = (LocalDate) args[1];
            capturedEndDate = (LocalDate) args[2];
        }
    }
}
