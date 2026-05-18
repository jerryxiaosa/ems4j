package info.zhihui.ems.business.report.service.query.impl;

import info.zhihui.ems.business.report.bo.AccountDailyReportBo;
import info.zhihui.ems.business.report.bo.AccountDailyReportSummaryBo;
import info.zhihui.ems.business.report.entity.DailyAccountReportEntity;
import info.zhihui.ems.business.report.repository.report.DailyAccountReportRepository;
import info.zhihui.ems.business.report.service.query.AccountDailyReportQueryService;
import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

import static info.zhihui.ems.common.utils.BigDecimalUtils.zeroIfNull;

/**
 * 账户日报查询服务实现。
 */
@Service
@RequiredArgsConstructor
public class AccountDailyReportQueryServiceImpl implements AccountDailyReportQueryService {

    private static final long MAX_SUMMARY_DAY_COUNT = 366;

    private final DailyAccountReportRepository dailyAccountReportRepository;

    /**
     * 查询账户每日汇总报表。
     * 返回对象会同时保留原始按量电费和按账户类型解析后的费用，便于上层直接展示费用趋势。
     *
     * @param accountId 账户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 账户日报业务对象列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<AccountDailyReportBo> findAccountDailyReportList(@NotNull Integer accountId,
                                                                 @NotNull LocalDate startDate,
                                                                 @NotNull LocalDate endDate) {
        return dailyAccountReportRepository.findListByAccountIdAndDateRange(accountId, startDate, endDate)
                .stream()
                .map(this::toBo)
                .toList();
    }

    /**
     * 汇总账户指定日期区间内的用电量和费用。
     * resolvedChargeAmount 在 Java 中按账户类型聚合，避免 SQL 层重复理解日报字段差异。
     *
     * @param accountId 账户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 账户日报区间汇总
     */
    @Override
    @Transactional(readOnly = true)
    public AccountDailyReportSummaryBo getAccountDailyReportSummary(@NotNull Integer accountId,
                                                                    @NotNull LocalDate startDate,
                                                                    @NotNull LocalDate endDate) {
        validateSummaryDateRange(startDate, endDate);
        List<DailyAccountReportEntity> reportList = dailyAccountReportRepository.findListByAccountIdAndDateRange(accountId, startDate, endDate);

        BigDecimal consumePower = BigDecimal.ZERO;
        BigDecimal electricChargeAmount = BigDecimal.ZERO;
        BigDecimal resolvedChargeAmount = BigDecimal.ZERO;
        if (reportList != null) {
            for (DailyAccountReportEntity report : reportList) {
                consumePower = consumePower.add(zeroIfNull(report.getConsumePower()));
                electricChargeAmount = electricChargeAmount.add(zeroIfNull(report.getElectricChargeAmount()));
                resolvedChargeAmount = resolvedChargeAmount.add(resolveChargeAmount(report));
            }
        }

        return new AccountDailyReportSummaryBo()
                .setAccountId(accountId)
                .setConsumePower(consumePower)
                .setElectricChargeAmount(electricChargeAmount)
                .setResolvedChargeAmount(resolvedChargeAmount);
    }

    /**
     * 将账户日报实体转换为业务对象。
     * electricChargeAmount 保留原始按量电费语义，resolvedChargeAmount 用于统一展示账户费用。
     *
     * @param entity 账户日报实体
     * @return 账户日报业务对象
     */
    private AccountDailyReportBo toBo(DailyAccountReportEntity entity) {
        return new AccountDailyReportBo()
                .setReportDate(entity.getReportDate())
                .setAccountId(entity.getAccountId())
                .setConsumePower(zeroIfNull(entity.getConsumePower()))
                .setElectricChargeAmount(zeroIfNull(entity.getElectricChargeAmount()))
                .setResolvedChargeAmount(resolveChargeAmount(entity));
    }

    /**
     * 解析账户费用。
     * 包月账户的扣费写入 monthlyChargeAmount，其余账户的电费写入 electricChargeAmount。
     *
     * @param entity 账户日报实体
     * @return 按账户类型解析后的费用金额
     */
    private BigDecimal resolveChargeAmount(DailyAccountReportEntity entity) {
        if (Objects.equals(entity.getElectricAccountType(), ElectricAccountTypeEnum.MONTHLY.getCode())) {
            return zeroIfNull(entity.getMonthlyChargeAmount());
        }
        return zeroIfNull(entity.getElectricChargeAmount());
    }

    private void validateSummaryDateRange(LocalDate startDate, LocalDate endDate) {
        long dayCount = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        if (dayCount > MAX_SUMMARY_DAY_COUNT) {
            throw new BusinessRuntimeException("账户日报汇总日期范围不能超过一年");
        }
    }

}
