package info.zhihui.ems.web.mini.biz;

import info.zhihui.ems.business.account.bo.AccountBo;
import info.zhihui.ems.business.account.dto.AccountElectricBalanceAggregateItemDto;
import info.zhihui.ems.business.account.service.AccountAdditionalInfoService;
import info.zhihui.ems.business.account.service.AccountInfoService;
import info.zhihui.ems.business.device.bo.ElectricMeterBo;
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
import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.components.context.RequestContext;
import info.zhihui.ems.web.mini.vo.MiniHomeSummaryVo;
import info.zhihui.ems.web.mini.vo.MiniHomeTrendVo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MiniHomeBizTest {

    @Mock
    private RequestContext requestContext;
    @Mock
    private AccountInfoService accountInfoService;
    @Mock
    private AccountAdditionalInfoService accountAdditionalInfoService;
    @Mock
    private ElectricMeterInfoService electricMeterInfoService;
    @Mock
    private AccountDailyReportQueryService accountDailyReportQueryService;
    @Mock
    private OrderQueryService orderQueryService;
    @InjectMocks
    private MiniHomeBiz miniHomeBiz;

    @Test
    @SuppressWarnings("unchecked")
    void getSummary_ShouldAssembleHomeSummaryFromCurrentAccount() {
        when(requestContext.getAccountId()).thenReturn(20);
        when(accountInfoService.getById(20)).thenReturn(new AccountBo()
                .setId(20)
                .setOwnerName("星河家园 2 栋住户账")
                .setElectricAccountType(ElectricAccountTypeEnum.MERGED));
        when(accountAdditionalInfoService.findElectricBalanceAmountMap(any()))
                .thenReturn(Map.of(20, new BigDecimal("328.6")));
        when(electricMeterInfoService.findList(any()))
                .thenReturn(List.of(new ElectricMeterBo(), new ElectricMeterBo()));
        when(accountDailyReportQueryService.getAccountDailyReportSummary(any(), any(), any()))
                .thenReturn(new AccountDailyReportSummaryBo()
                        .setAccountId(20)
                        .setConsumePower(new BigDecimal("286.4"))
                        .setElectricChargeAmount(BigDecimal.ZERO)
                        .setResolvedChargeAmount(new BigDecimal("173.8")));
        when(orderQueryService.findOrdersPage(any(), any())).thenReturn(new PageResult<OrderListDto>()
                .setPageNum(1)
                .setPageSize(1)
                .setTotal(1L)
                .setList(List.of(new OrderListDto()
                        .setOrderSn("RC202605110001")
                        .setUserPayAmount(new BigDecimal("200"))
                        .setOrderAmount(new BigDecimal("200"))
                        .setServiceAmount(new BigDecimal("4"))
                        .setOrderStatus(OrderStatusEnum.SUCCESS)
                        .setOrderCreateTime(LocalDateTime.of(2026, 5, 10, 18, 24)))));

        MiniHomeSummaryVo result = miniHomeBiz.getSummary();

        assertThat(result.getElectricAccountName()).isEqualTo("星河家园 2 栋住户账");
        assertThat(result.getElectricAccountType()).isEqualTo(ElectricAccountTypeEnum.MERGED.getCode());
        assertThat(result.getBalance()).isEqualByComparingTo("328.6");
        assertThat(result.getBalanceText()).isEqualTo("328.60");
        assertThat(result.getMeterCount()).isEqualTo(2);
        assertThat(result.getLastMonthEnergy()).isEqualByComparingTo("286.4");
        assertThat(result.getLastMonthEnergyText()).isEqualTo("286.40");
        assertThat(result.getLastMonthFee()).isEqualByComparingTo("173.8");
        assertThat(result.getLastMonthFeeText()).isEqualTo("173.80");
        assertThat(result.getLatestRechargeOrder().getOrderSn()).isEqualTo("RC202605110001");
        assertThat(result.getLatestRechargeOrder().getPayAmountText()).isEqualTo("200.00");
        assertThat(result.getLatestRechargeOrder().getTopUpAmountText()).isEqualTo("196.00");
        assertThat(result.getLatestRechargeOrder().getServiceFeeAmountText()).isEqualTo("4.00");
        assertThat(result.getLatestRechargeOrder().getStatus()).isEqualTo("SUCCESS");
        assertThat(result.getLatestRechargeOrder().getStatusName()).isEqualTo("支付成功");
        assertThat(result.getLatestRechargeOrder().getCreateTime()).isEqualTo("2026-05-10 18:24:00");

        ArgumentCaptor<List<AccountElectricBalanceAggregateItemDto>> balanceCaptor = ArgumentCaptor.forClass(List.class);
        verify(accountAdditionalInfoService).findElectricBalanceAmountMap(balanceCaptor.capture());
        assertThat(balanceCaptor.getValue().get(0).getAccountId()).isEqualTo(20);
        assertThat(balanceCaptor.getValue().get(0).getElectricAccountType()).isEqualTo(ElectricAccountTypeEnum.MERGED);

        ArgumentCaptor<ElectricMeterQueryDto> meterQueryCaptor = ArgumentCaptor.forClass(ElectricMeterQueryDto.class);
        verify(electricMeterInfoService).findList(meterQueryCaptor.capture());
        assertThat(meterQueryCaptor.getValue().getAccountIds()).containsExactly(20);

        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        verify(accountDailyReportQueryService).getAccountDailyReportSummary(
                20,
                lastMonth.atDay(1),
                lastMonth.atEndOfMonth());

        ArgumentCaptor<OrderQueryDto> orderQueryCaptor = ArgumentCaptor.forClass(OrderQueryDto.class);
        ArgumentCaptor<PageParam> pageParamCaptor = ArgumentCaptor.forClass(PageParam.class);
        verify(orderQueryService).findOrdersPage(orderQueryCaptor.capture(), pageParamCaptor.capture());
        assertThat(orderQueryCaptor.getValue().getAccountId()).isEqualTo(20);
        assertThat(orderQueryCaptor.getValue().getOrderType()).isEqualTo(OrderTypeEnum.ENERGY_TOP_UP);
        assertThat(pageParamCaptor.getValue().getPageNum()).isEqualTo(1);
        assertThat(pageParamCaptor.getValue().getPageSize()).isEqualTo(1);
    }

    @Test
    void getTrend_ShouldReturnLastSevenCompletedDaysAndFillMissingDates() {
        when(requestContext.getAccountId()).thenReturn(20);
        LocalDate endDate = LocalDate.now().minusDays(1);
        LocalDate startDate = endDate.minusDays(6);
        when(accountDailyReportQueryService.findAccountDailyReportList(20, startDate, endDate))
                .thenReturn(List.of(
                        new AccountDailyReportBo()
                                .setReportDate(startDate.plusDays(1))
                                .setConsumePower(new BigDecimal("10.12"))
                                .setElectricChargeAmount(new BigDecimal("5.1"))
                                .setResolvedChargeAmount(new BigDecimal("5.1")),
                        new AccountDailyReportBo()
                                .setReportDate(endDate)
                                .setConsumePower(new BigDecimal("20"))
                                .setElectricChargeAmount(new BigDecimal("9.88"))
                                .setResolvedChargeAmount(new BigDecimal("9.88"))
                ));

        MiniHomeTrendVo result = miniHomeBiz.getTrend("energy");

        assertThat(result.getMetric()).isEqualTo("energy");
        assertThat(result.getUnit()).isEqualTo("kWh");
        assertThat(result.getList()).hasSize(7);
        assertThat(result.getList().get(0).getDate()).isEqualTo(startDate.toString());
        assertThat(result.getList().get(0).getValue()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getList().get(1).getValue()).isEqualByComparingTo("10.12");
        assertThat(result.getList().get(6).getDate()).isEqualTo(endDate.toString());
        assertThat(result.getList().get(6).getValue()).isEqualByComparingTo("20");
        assertThat(result.getTip()).isNull();
    }

    @Test
    void getTrend_WhenMetricIsFee_ShouldReturnFeeUnitAndFeeValues() {
        when(requestContext.getAccountId()).thenReturn(20);
        LocalDate endDate = LocalDate.now().minusDays(1);
        LocalDate startDate = endDate.minusDays(6);
        when(accountDailyReportQueryService.findAccountDailyReportList(20, startDate, endDate))
                .thenReturn(List.of(new AccountDailyReportBo()
                        .setReportDate(startDate)
                        .setConsumePower(new BigDecimal("99.99"))
                        .setElectricChargeAmount(BigDecimal.ZERO)
                        .setResolvedChargeAmount(new BigDecimal("12.34"))));

        MiniHomeTrendVo result = miniHomeBiz.getTrend("fee");

        assertThat(result.getMetric()).isEqualTo("fee");
        assertThat(result.getUnit()).isEqualTo("元");
        assertThat(result.getList()).hasSize(7);
        assertThat(result.getList().get(0).getValue()).isEqualByComparingTo("12.34");
    }
}
