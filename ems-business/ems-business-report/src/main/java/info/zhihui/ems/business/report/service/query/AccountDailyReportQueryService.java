package info.zhihui.ems.business.report.service.query;

import info.zhihui.ems.business.report.bo.AccountDailyReportBo;
import info.zhihui.ems.business.report.bo.AccountDailyReportSummaryBo;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

/**
 * 账户日报查询服务。
 */
public interface AccountDailyReportQueryService {

    /**
     * 查询账户指定日期区间内的日报列表。
     *
     * @param accountId 账户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 账户日报列表
     */
    List<AccountDailyReportBo> findAccountDailyReportList(@NotNull Integer accountId,
                                                          @NotNull LocalDate startDate,
                                                          @NotNull LocalDate endDate);

    /**
     * 汇总账户指定日期区间内的日报数据。
     *
     * @param accountId 账户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 账户日报区间汇总
     */
    AccountDailyReportSummaryBo getAccountDailyReportSummary(@NotNull Integer accountId,
                                                             @NotNull LocalDate startDate,
                                                             @NotNull LocalDate endDate);
}
