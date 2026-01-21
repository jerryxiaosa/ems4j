package info.zhihui.ems.web.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import info.zhihui.ems.config.satoken.SaWebConfig;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderBiz orderBiz;

    @MockitoBean
    private SaWebConfig saWebConfig;

    @Test
    @DisplayName("查询订单列表")
    void testFindOrders() throws Exception {
        OrderVo orderVo = new OrderVo()
                .setOrderSn("ORDER123")
                .setOrderAmount(BigDecimal.TEN);
        when(orderBiz.findOrders(any())).thenReturn(List.of(orderVo));

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].orderSn").value("ORDER123"));
    }

    @Test
    @DisplayName("创建能耗充值订单")
    void testCreateEnergyTopUpOrder() throws Exception {
        OrderCreationResponseVo responseVo = new OrderCreationResponseVo().setOrderSn("SN123");
        when(orderBiz.createEnergyTopUpOrder(any())).thenReturn(responseVo);

        String body = "{" +
                "\"userId\":1,\"userPhone\":\"13800000000\",\"userRealName\":\"张三\",\"thirdPartyUserId\":\"tp-1\",\"orderAmount\":100,\"paymentChannel\":\"WX_MINI\",\"energyTopUp\":{" +
                "\"accountId\":10,\"balanceType\":\"ELECTRIC_METER\",\"ownerType\":\"ENTERPRISE\",\"ownerId\":20,\"ownerName\":\"某企业\",\"electricAccountType\":\"QUANTITY\"}}";

        mockMvc.perform(post("/orders/energy-top-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderSn").value("SN123"));
    }

    @Test
    @DisplayName("创建销户结算订单")
    void testCreateTerminationOrder() throws Exception {
        OrderCreationResponseVo responseVo = new OrderCreationResponseVo().setOrderSn("SN456");
        when(orderBiz.createTerminationOrder(any())).thenReturn(responseVo);

        String body = "{" +
                "\"userId\":1,\"userPhone\":\"13800000000\",\"userRealName\":\"李四\",\"thirdPartyUserId\":\"tp-2\",\"orderAmount\":200,\"terminationInfo\":{" +
                "\"cancelNo\":\"C-001\",\"accountId\":11,\"ownerId\":21,\"ownerType\":\"ENTERPRISE\",\"ownerName\":\"张三\",\"settlementAmount\":200,\"electricAccountType\":\"QUANTITY\",\"electricMeterAmount\":2,\"fullCancel\":true,\"meterIdList\":[1,2]}}";

        mockMvc.perform(post("/orders/termination")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderSn").value("SN456"));
    }

    @Test
    @DisplayName("获取订单详情")
    void testGetOrderDetail() throws Exception {
        OrderDetailVo detailVo = new OrderDetailVo();
        detailVo.setOrderSn("ORDER456");
        when(orderBiz.getOrderDetail("ORDER456")).thenReturn(detailVo);

        mockMvc.perform(get("/orders/ORDER456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderSn").value("ORDER456"));
    }

    @Test
    @DisplayName("关闭订单")
    void testCloseOrder() throws Exception {
        doNothing().when(orderBiz).closeOrder("ORDER789");

        mockMvc.perform(post("/orders/ORDER789/close"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("处理微信支付通知")
    void testAnswerWeiXinPayNotify() throws Exception {
        doNothing().when(orderBiz).answerWeiXinPayNotify(any(HttpServletRequest.class));

        String body = objectMapper.writeValueAsString(java.util.Map.of("notifyData", "raw-json"));

        mockMvc.perform(post("/orders/weixin/pay-notify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(orderBiz).answerWeiXinPayNotify(any(HttpServletRequest.class));
    }
}
