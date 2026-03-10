package info.zhihui.ems.web.order;

import info.zhihui.ems.config.satoken.SaWebConfig;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.web.order.biz.OrderBiz;
import info.zhihui.ems.web.order.controller.OrderController;
import info.zhihui.ems.web.order.vo.OrderCreationResponseVo;
import info.zhihui.ems.web.order.vo.OrderDetailVo;
import info.zhihui.ems.web.order.vo.OrderVo;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderBiz orderBiz;

    @MockitoBean
    private SaWebConfig saWebConfig;

    @Test
    @DisplayName("查询订单列表")
    void testFindOrders() throws Exception {
        OrderVo orderVo = new OrderVo()
                .setOrderSn("ORDER123")
                .setOrderAmount(BigDecimal.TEN)
                .setPaymentChannel("WX_MINI")
                .setPaymentChannelName("微信小程序")
                .setMeterName("1号电表")
                .setDeviceNo("D-1001")
                .setBeginBalance(new BigDecimal("500.00"))
                .setEndBalance(new BigDecimal("510.00"));
        PageResult<OrderVo> pageResult = new PageResult<OrderVo>()
                .setPageNum(1)
                .setPageSize(10)
                .setTotal(1L)
                .setList(List.of(orderVo));
        when(orderBiz.findOrdersPage(any(), any(), any())).thenReturn(pageResult);

        mockMvc.perform(get("/v1/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.list[0].orderSn").value("ORDER123"))
                .andExpect(jsonPath("$.data.list[0].meterName").value("1号电表"))
                .andExpect(jsonPath("$.data.list[0].deviceNo").value("D-1001"))
                .andExpect(jsonPath("$.data.list[0].paymentChannel").value("WX_MINI"))
                .andExpect(jsonPath("$.data.list[0].paymentChannelName").value("微信小程序"))
                .andExpect(jsonPath("$.data.list[0].beginBalance").value(500.00))
                .andExpect(jsonPath("$.data.list[0].endBalance").value(510.00));
    }

    @Test
    @DisplayName("创建能耗充值订单")
    void testCreateEnergyTopUpOrder() throws Exception {
        OrderCreationResponseVo responseVo = new OrderCreationResponseVo().setOrderSn("SN123");
        when(orderBiz.createEnergyTopUpOrder(any())).thenReturn(responseVo);

        String body = "{" +
                "\"userId\":1,\"userPhone\":\"13800000000\",\"userRealName\":\"张三\",\"thirdPartyUserId\":\"tp-1\",\"orderAmount\":100,\"paymentChannel\":\"WX_MINI\",\"energyTopUp\":{" +
                "\"accountId\":10,\"balanceType\":1,\"ownerType\":0,\"ownerId\":20,\"ownerName\":\"某企业\",\"electricAccountType\":0}}";

        mockMvc.perform(post("/v1/orders/energy-top-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderSn").value("SN123"));
    }

    @Test
    @DisplayName("创建能耗充值订单-订单金额小于等于0时返回参数错误")
    void testCreateEnergyTopUpOrder_WhenOrderAmountLessThanOrEqualZero() throws Exception {
        String body = "{" +
                "\"userId\":1,\"userPhone\":\"13800000000\",\"userRealName\":\"张三\",\"thirdPartyUserId\":\"tp-1\",\"orderAmount\":0,\"paymentChannel\":\"WX_MINI\",\"energyTopUp\":{" +
                "\"accountId\":10,\"balanceType\":1,\"ownerType\":0,\"ownerId\":20,\"ownerName\":\"某企业\",\"electricAccountType\":0}}";

        mockMvc.perform(post("/v1/orders/energy-top-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(-102001));

        verify(orderBiz, never()).createEnergyTopUpOrder(any());
    }

    @Test
    @DisplayName("创建能耗充值订单-订单金额超过两位小数时返回参数错误")
    void testCreateEnergyTopUpOrder_WhenOrderAmountHasMoreThanTwoFractionDigits() throws Exception {
        String body = "{" +
                "\"userId\":1,\"userPhone\":\"13800000000\",\"userRealName\":\"张三\",\"thirdPartyUserId\":\"tp-1\",\"orderAmount\":100.009,\"paymentChannel\":\"WX_MINI\",\"energyTopUp\":{" +
                "\"accountId\":10,\"balanceType\":1,\"ownerType\":0,\"ownerId\":20,\"ownerName\":\"某企业\",\"electricAccountType\":0}}";

        mockMvc.perform(post("/v1/orders/energy-top-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(-102001));

        verify(orderBiz, never()).createEnergyTopUpOrder(any());
    }

    @Test
    @DisplayName("创建能耗充值订单-必填编码为null时返回参数错误")
    void testCreateEnergyTopUpOrder_WhenRequiredCodeIsNull() throws Exception {
        String body = "{" +
                "\"userId\":1,\"userPhone\":\"13800000000\",\"userRealName\":\"张三\",\"thirdPartyUserId\":\"tp-1\",\"orderAmount\":100,\"paymentChannel\":\"WX_MINI\",\"energyTopUp\":{" +
                "\"accountId\":10,\"balanceType\":null,\"ownerType\":0,\"ownerId\":20,\"ownerName\":\"某企业\",\"electricAccountType\":0}}";

        mockMvc.perform(post("/v1/orders/energy-top-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(-102001));

        verify(orderBiz, never()).createEnergyTopUpOrder(any());
    }

    @Test
    @DisplayName("创建能耗充值订单-电费账户类型为null时返回参数错误")
    void testCreateEnergyTopUpOrder_WhenElectricAccountTypeNull() throws Exception {
        String body = "{" +
                "\"userId\":1,\"userPhone\":\"13800000000\",\"userRealName\":\"张三\",\"thirdPartyUserId\":\"tp-1\",\"orderAmount\":100,\"paymentChannel\":\"WX_MINI\",\"energyTopUp\":{" +
                "\"accountId\":10,\"balanceType\":1,\"ownerType\":0,\"ownerId\":20,\"ownerName\":\"某企业\",\"electricAccountType\":null}}";

        mockMvc.perform(post("/v1/orders/energy-top-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(-102001));

        verify(orderBiz, never()).createEnergyTopUpOrder(any());
    }

    @Test
    @DisplayName("获取订单详情")
    void testGetOrderDetail() throws Exception {
        OrderDetailVo detailVo = new OrderDetailVo();
        detailVo.setOrderSn("ORDER456");
        detailVo.setTopUpAmount(new BigDecimal("95.00"));
        detailVo.setBeginBalance(new BigDecimal("500.00"));
        detailVo.setEndBalance(new BigDecimal("510.00"));
        when(orderBiz.getOrderDetail("ORDER456")).thenReturn(detailVo);

        mockMvc.perform(get("/v1/orders/ORDER456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderSn").value("ORDER456"))
                .andExpect(jsonPath("$.data.topUpAmount").value(95.00))
                .andExpect(jsonPath("$.data.beginBalance").value(500.00))
                .andExpect(jsonPath("$.data.endBalance").value(510.00));
    }

    @Test
    @DisplayName("关闭订单")
    void testCloseOrder() throws Exception {
        doNothing().when(orderBiz).closeOrder("ORDER789");

        mockMvc.perform(post("/v1/orders/ORDER789/close"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("处理微信支付通知")
    void testAnswerWeiXinPayNotify() throws Exception {
        doNothing().when(orderBiz).answerWeiXinPayNotify(any(HttpServletRequest.class));

        mockMvc.perform(post("/v1/orders/weixin/pay-notify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(orderBiz).answerWeiXinPayNotify(any(HttpServletRequest.class));
    }

    @Test
    @DisplayName("处理微信支付通知-异常时返回500")
    void testAnswerWeiXinPayNotify_WhenException() throws Exception {
        doThrow(new RuntimeException("mock error")).when(orderBiz).answerWeiXinPayNotify(any(HttpServletRequest.class));

        mockMvc.perform(post("/v1/orders/weixin/pay-notify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isInternalServerError());

        verify(orderBiz).answerWeiXinPayNotify(any(HttpServletRequest.class));
    }
}
