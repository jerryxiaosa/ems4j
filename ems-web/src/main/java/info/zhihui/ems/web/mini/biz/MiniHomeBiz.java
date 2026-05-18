package info.zhihui.ems.web.mini.biz;

import info.zhihui.ems.business.account.bo.AccountBo;
import info.zhihui.ems.business.account.dto.AccountElectricBalanceAggregateItemDto;
import info.zhihui.ems.business.account.service.AccountAdditionalInfoService;
import info.zhihui.ems.business.account.service.AccountInfoService;
import info.zhihui.ems.business.device.dto.ElectricMeterQueryDto;
import info.zhihui.ems.business.device.service.ElectricMeterInfoService;
import info.zhihui.ems.business.order.dto.OrderListDto;
import info.zhihui.ems.business.order.dto.OrderQueryDto;
import info.zhihui.ems.business.order.enums.OrderStatusEnum;
import info.zhihui.ems.business.order.enums.OrderTypeEnum;
import info.zhihui.ems.business.order.service.core.OrderQueryService;
import info.zhihui.ems.business.report.bo.AccountDailyReportBo;
import info.zhihui.ems.business.report.bo.AccountDailyReportSummaryBo;
import info.zhihui.ems.business.report.service.query.AccountDailyReportQueryService;
import info.zhihui.ems.common.constant.ResultCode;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.components.context.RequestContext;
import info.zhihui.ems.web.mini.vo.MiniHomeSummaryVo;
import info.zhihui.ems.web.mini.vo.MiniHomeTrendVo;
import info.zhihui.ems.web.mini.vo.MiniLatestRechargeOrderVo;
import info.zhihui.ems.web.mini.vo.MiniTrendPointVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static info.zhihui.ems.common.utils.BigDecimalUtils.zeroIfNull;

/**
 * 小程序首页 Web 编排。
 */
@Service
@RequiredArgsConstructor
public class MiniHomeBiz {

    private static final String METRIC_ENERGY = "energy";
    private static final String METRIC_FEE = "fee";
    private static final String UNIT_ENERGY = "kWh";
    private static final String UNIT_FEE = "元";
    private static final String NO_TREND_TIP = "暂无趋势数据";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final RequestContext requestContext;
    private final AccountInfoService accountInfoService;
    private final AccountAdditionalInfoService accountAdditionalInfoService;
    private final ElectricMeterInfoService electricMeterInfoService;
    private final AccountDailyReportQueryService accountDailyReportQueryService;
    private final OrderQueryService orderQueryService;

    /**
     * 查询小程序首页摘要。
     * 费用字段使用报表查询服务提供的 resolvedChargeAmount，统一兼容包月和非包月账户。
     *
     * @return 首页摘要视图对象
     */
    public MiniHomeSummaryVo getSummary() {
        Integer accountId = requestContext.getAccountId();
        AccountBo account = getAccount(accountId);
        BigDecimal balance = getBalance(account);
        int meterCount = electricMeterInfoService.findList(new ElectricMeterQueryDto()
                .setAccountIds(List.of(accountId))).size();
        AccountDailyReportSummaryBo lastMonthSummary = getLastMonthSummary(accountId);

        return new MiniHomeSummaryVo()
                .setElectricAccountName(account.getOwnerName())
                .setElectricAccountType(account.getElectricAccountType().getCode())
                .setBalance(balance)
                .setBalanceText(formatAmount(balance))
                .setMeterCount(meterCount)
                .setLastMonthEnergy(lastMonthSummary.getConsumePower())
                .setLastMonthEnergyText(formatAmount(lastMonthSummary.getConsumePower()))
                .setLastMonthFee(lastMonthSummary.getResolvedChargeAmount())
                .setLastMonthFeeText(formatAmount(lastMonthSummary.getResolvedChargeAmount()))
                .setLatestRechargeOrder(getLatestRechargeOrder(accountId));
    }

    /**
     * 查询小程序首页近七个完整自然日趋势。
     * 今天数据尚未完成汇总，因此趋势区间固定截止到昨天。
     *
     * @param metric 指标类型，energy 表示电量，fee 表示费用
     * @return 首页趋势视图对象
     */
    public MiniHomeTrendVo getTrend(String metric) {
        assertValidMetric(metric);
        Integer accountId = requestContext.getAccountId();
        LocalDate endDate = LocalDate.now().minusDays(1);
        LocalDate startDate = endDate.minusDays(6);
        Map<LocalDate, AccountDailyReportBo> reportMap = accountDailyReportQueryService
                .findAccountDailyReportList(accountId, startDate, endDate)
                .stream()
                .collect(Collectors.toMap(AccountDailyReportBo::getReportDate, Function.identity(), (left, right) -> left));

        List<MiniTrendPointVo> pointList = startDate.datesUntil(endDate.plusDays(1))
                .map(date -> new MiniTrendPointVo()
                        .setDate(date.toString())
                        .setValue(getTrendValue(metric, reportMap.get(date))))
                .toList();

        return new MiniHomeTrendVo()
                .setMetric(metric)
                .setUnit(METRIC_ENERGY.equals(metric) ? UNIT_ENERGY : UNIT_FEE)
                .setList(pointList)
                .setTip(isAllZero(pointList) ? NO_TREND_TIP : null);
    }

    private AccountBo getAccount(Integer accountId) {
        try {
            AccountBo account = accountInfoService.getById(accountId);
            if (account.getElectricAccountType() == null) {
                throw accountAbnormal();
            }
            return account;
        } catch (NotFoundException e) {
            throw accountAbnormal();
        }
    }

    private BigDecimal getBalance(AccountBo account) {
        Map<Integer, BigDecimal> balanceMap = accountAdditionalInfoService.findElectricBalanceAmountMap(List.of(
                new AccountElectricBalanceAggregateItemDto()
                        .setAccountId(account.getId())
                        .setElectricAccountType(account.getElectricAccountType())
        ));
        return zeroIfNull(balanceMap.get(account.getId()));
    }

    private AccountDailyReportSummaryBo getLastMonthSummary(Integer accountId) {
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        return accountDailyReportQueryService.getAccountDailyReportSummary(accountId, lastMonth.atDay(1), lastMonth.atEndOfMonth());
    }

    /**
     * 查询最近一笔充值缴费订单。
     * topUpAmount 当前按订单金额扣减服务费计算，后续可切换为订单明细中的到账金额。
     *
     * @param accountId 账户ID
     * @return 最近充值订单视图对象
     */
    private MiniLatestRechargeOrderVo getLatestRechargeOrder(Integer accountId) {
        PageResult<OrderListDto> orderPage = orderQueryService.findOrdersPage(new OrderQueryDto()
                .setAccountId(accountId)
                .setOrderType(OrderTypeEnum.ENERGY_TOP_UP), new PageParam().setPageNum(1).setPageSize(1));
        if (orderPage == null || CollectionUtils.isEmpty(orderPage.getList())) {
            return null;
        }

        OrderListDto order = orderPage.getList().get(0);
        BigDecimal topUpAmount = resolveTopUpAmount(order);
        return new MiniLatestRechargeOrderVo()
                .setOrderSn(order.getOrderSn())
                .setPayAmount(zeroIfNull(order.getUserPayAmount()))
                .setPayAmountText(formatAmount(order.getUserPayAmount()))
                .setTopUpAmount(zeroIfNull(topUpAmount))
                .setTopUpAmountText(formatAmount(topUpAmount))
                .setServiceFeeAmount(zeroIfNull(order.getServiceAmount()))
                .setServiceFeeAmountText(formatAmount(order.getServiceAmount()))
                .setStatus(order.getOrderStatus() == null ? null : order.getOrderStatus().name())
                .setStatusName(getOrderStatusName(order.getOrderStatus()))
                .setCreateTime(formatDateTime(order.getOrderCreateTime()));
    }

    /**
     * 解析充值到账金额。
     * 优先使用订单查询服务已经计算好的 topUpAmount，缺失时再按订单金额减服务费兜底。
     *
     * @param order 订单列表业务对象
     * @return 充值到账金额
     */
    private BigDecimal resolveTopUpAmount(OrderListDto order) {
        if (order.getTopUpAmount() != null) {
            return order.getTopUpAmount();
        }
        if (order.getOrderAmount() == null) {
            return null;
        }
        return order.getOrderAmount().subtract(zeroIfNull(order.getServiceAmount()));
    }

    private void assertValidMetric(String metric) {
        if (!METRIC_ENERGY.equals(metric) && !METRIC_FEE.equals(metric)) {
            throw new BusinessRuntimeException(ResultCode.PARAMETER_ERROR.getCode(), ResultCode.PARAMETER_ERROR.getMessage());
        }
    }

    /**
     * 解析趋势点金额。
     * 费用趋势使用 resolvedChargeAmount，避免包月账户因 electricChargeAmount 为空而显示 0。
     *
     * @param metric 指标类型
     * @param report 账户日报业务对象
     * @return 趋势点数值
     */
    private BigDecimal getTrendValue(String metric, AccountDailyReportBo report) {
        if (report == null) {
            return BigDecimal.ZERO;
        }
        if (METRIC_ENERGY.equals(metric)) {
            return zeroIfNull(report.getConsumePower());
        }
        return zeroIfNull(report.getResolvedChargeAmount());
    }

    private boolean isAllZero(List<MiniTrendPointVo> pointList) {
        return pointList.stream()
                .map(MiniTrendPointVo::getValue)
                .allMatch(value -> zeroIfNull(value).compareTo(BigDecimal.ZERO) == 0);
    }

    private String formatAmount(BigDecimal amount) {
        return zeroIfNull(amount).setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? null : DATE_TIME_FORMATTER.format(dateTime);
    }

    private String getOrderStatusName(OrderStatusEnum status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case NOT_PAY -> "待支付";
            case SUCCESS -> "支付成功";
            case CLOSED -> "已关闭";
            case PAY_ERROR -> "支付异常";
            case REFUND_PROCESSING -> "退款申请中";
            case FULL_REFUND -> "已全额退款";
            case REFUND_CLOSED -> "退款关闭";
            case REFUND_ERROR -> "退款异常";
        };
    }

    private BusinessRuntimeException accountAbnormal() {
        return new BusinessRuntimeException(ResultCode.MINI_ACCOUNT_ABNORMAL.getCode(), ResultCode.MINI_ACCOUNT_ABNORMAL.getMessage());
    }
}
