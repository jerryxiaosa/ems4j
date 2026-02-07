package info.zhihui.ems.business.finance.service.order.thirdparty.wx;

import com.fasterxml.jackson.core.type.TypeReference;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.payments.model.TransactionAmount;
import info.zhihui.ems.business.finance.bo.OrderBo;
import info.zhihui.ems.business.finance.dto.order.WxOrderCreationResponseDto;
import info.zhihui.ems.business.finance.dto.order.thirdparty.wx.WxPayConfig;
import info.zhihui.ems.business.finance.dto.order.thirdparty.wx.WxPrepayQuery;
import info.zhihui.ems.business.finance.entity.order.OrderThirdPartyNotificationResponseDto;
import info.zhihui.ems.business.finance.entity.order.OrderThirdPartyPrepayEntity;
import info.zhihui.ems.business.finance.enums.OrderStatusEnum;
import info.zhihui.ems.business.finance.enums.OrderTypeEnum;
import info.zhihui.ems.business.finance.enums.PaymentChannelEnum;
import info.zhihui.ems.business.finance.exception.PayAmountException;
import info.zhihui.ems.business.finance.repository.order.OrderThirdPartyPrepayRepository;
import info.zhihui.ems.business.finance.service.order.thirdparty.wx.sdk.WxMiniProgramPaySdk;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.mq.api.model.MqMessage;
import info.zhihui.ems.foundation.system.service.ConfigService;
import info.zhihui.ems.mq.api.service.MqService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static info.zhihui.ems.foundation.system.constant.SystemConfigConstant.WX_PAY_CONFIG;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 微信小程序支付处理器单元测试类
 *
 * @author jerryxiaosa
 */
@ExtendWith(MockitoExtension.class)
class WxMiniProgramHandlerTest {

    @Mock
    private WxMiniProgramPaySdk sdk;

    @Mock
    private ConfigService configService;

    @Mock
    private OrderThirdPartyPrepayRepository orderThirdPartyPrepayRepository;

    @Mock
    private MqService mqService;

    @InjectMocks
    private WxMiniProgramHandler wxMiniProgramHandler;

    private OrderBo orderBo;
    private WxPayConfig wxPayConfig;
    private PrepayWithRequestPaymentResponse prepayResponse;

    /**
     * 初始化测试数据
     */
    @BeforeEach
    void setUp() {
        // 初始化订单业务对象
        orderBo = new OrderBo();
        orderBo.setOrderSn("TEST_ORDER_001")
                .setOrderStatus(OrderStatusEnum.NOT_PAY)
                .setOrderType(OrderTypeEnum.ENERGY_TOP_UP)
                .setOrderAmount(new BigDecimal("100.00"))
                .setUserPayAmount(new BigDecimal("110.00"))
                .setThirdPartyUserId("test_openid_123")
                .setOrderPayStopTime(LocalDateTime.now().plusMinutes(15));

        // 初始化微信支付配置
        wxPayConfig = new WxPayConfig();

        // 初始化预支付响应
        prepayResponse = new PrepayWithRequestPaymentResponse();
    }

    /**
     * 测试获取支付渠道
     */
    @Test
    void testGetPaymentChannel() {
        // 执行测试
        PaymentChannelEnum result = wxMiniProgramHandler.getPaymentChannel();

        // 验证结果
        assertEquals(PaymentChannelEnum.WX_MINI, result);
    }

    /**
     * 测试正常创建订单支付
     */
    @Test
    void testOnCreate_Normal() {
        // 准备测试数据
        String prepayId = "wx_prepay_123456";

        // 模拟依赖服务调用
        when(configService.getValueByKey(eq(WX_PAY_CONFIG), ArgumentMatchers.<TypeReference<WxPayConfig>>any()))
                .thenReturn(wxPayConfig);
        when(sdk.getPrepayId(any(WxPrepayQuery.class), eq(wxPayConfig)))
                .thenReturn(prepayId);
        when(sdk.getPrepayResponseByPrePayId(eq(prepayId), eq(wxPayConfig)))
                .thenReturn(prepayResponse);
        when(orderThirdPartyPrepayRepository.insert(any(OrderThirdPartyPrepayEntity.class)))
                .thenReturn(1);
        doNothing().when(mqService).sendMessageAfterCommit(any(MqMessage.class));

        // 执行测试
        WxOrderCreationResponseDto result = wxMiniProgramHandler.onCreate(orderBo);

        // 验证结果
        assertNotNull(result);
        assertEquals(orderBo.getOrderSn(), result.getOrderSn());
        assertEquals(orderBo.getOrderType(), result.getOrderTypeEnum());
        assertEquals(PaymentChannelEnum.WX_MINI, result.getPaymentChannel());
        assertEquals(orderBo.getOrderPayStopTime(), result.getOrderPayStopTime());
        assertEquals(prepayResponse, result.getPrepayWithRequestPaymentResponse());

        // 验证方法调用
        verify(configService, times(1)).getValueByKey(eq(WX_PAY_CONFIG), ArgumentMatchers.<TypeReference<WxPayConfig>>any());
        verify(sdk, times(1)).getPrepayId(any(WxPrepayQuery.class), eq(wxPayConfig));
        verify(sdk, times(1)).getPrepayResponseByPrePayId(eq(prepayId), eq(wxPayConfig));
        verify(orderThirdPartyPrepayRepository, times(1)).insert(any(OrderThirdPartyPrepayEntity.class));
        verify(mqService, times(1)).sendMessageAfterCommit(any(MqMessage.class));
    }

    /**
     * 测试订单状态不是未支付时抛出异常
     */
    @Test
    void testOnCreate_OrderStatusNotNotPay() {
        // 准备测试数据 - 设置订单状态为已支付
        orderBo.setOrderStatus(OrderStatusEnum.SUCCESS);

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(
                BusinessRuntimeException.class,
                () -> wxMiniProgramHandler.onCreate(orderBo)
        );

        // 验证异常信息
        assertTrue(exception.getMessage().contains("不是未支付状态，无法支付"));
        assertTrue(exception.getMessage().contains(orderBo.getOrderSn()));

        // 验证没有调用其他服务
        verify(configService, never()).getValueByKey(any(), ArgumentMatchers.<TypeReference<WxPayConfig>>any());
        verify(sdk, never()).getPrepayId(any(), any());
        verify(orderThirdPartyPrepayRepository, never()).insert(any(OrderThirdPartyPrepayEntity.class));
        verify(mqService, never()).sendMessage(any());
    }

    /**
     * 测试配置服务异常情况
     */
    @Test
    void testOnCreate_ConfigServiceException() {
        // 模拟配置服务抛出异常
        when(configService.getValueByKey(eq(WX_PAY_CONFIG), ArgumentMatchers.<TypeReference<WxPayConfig>>any()))
                .thenThrow(new RuntimeException("配置获取失败"));

        // 执行测试并验证异常
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> wxMiniProgramHandler.onCreate(orderBo)
        );

        // 验证异常信息
        assertEquals("配置获取失败", exception.getMessage());

        // 验证只调用了配置服务
        verify(configService, times(1)).getValueByKey(eq(WX_PAY_CONFIG), ArgumentMatchers.<TypeReference<WxPayConfig>>any());
        verify(sdk, never()).getPrepayId(any(), any());
    }

    /**
     * 测试SDK获取预支付ID异常情况
     */
    @Test
    void testOnCreate_SdkGetPrepayIdException() {
        // 模拟配置服务正常返回
        when(configService.getValueByKey(eq(WX_PAY_CONFIG), ArgumentMatchers.<TypeReference<WxPayConfig>>any()))
                .thenReturn(wxPayConfig);
        // 模拟SDK抛出异常
        when(sdk.getPrepayId(any(WxPrepayQuery.class), eq(wxPayConfig)))
                .thenThrow(new RuntimeException("获取预支付ID失败"));

        // 执行测试并验证异常
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> wxMiniProgramHandler.onCreate(orderBo)
        );

        // 验证异常信息
        assertEquals("获取预支付ID失败", exception.getMessage());

        // 验证方法调用
        verify(configService, times(1)).getValueByKey(eq(WX_PAY_CONFIG), ArgumentMatchers.<TypeReference<WxPayConfig>>any());
        verify(sdk, times(1)).getPrepayId(any(WxPrepayQuery.class), eq(wxPayConfig));
        verify(sdk, never()).getPrepayResponseByPrePayId(any(), any());
    }

    /**
     * 测试数据库插入异常情况
     */
    @Test
    void testOnCreate_DatabaseInsertException() {
        // 准备测试数据
        String prepayId = "wx_prepay_123456";

        // 模拟前面的服务正常调用
        when(configService.getValueByKey(eq(WX_PAY_CONFIG), ArgumentMatchers.<TypeReference<WxPayConfig>>any()))
                .thenReturn(wxPayConfig);
        when(sdk.getPrepayId(any(WxPrepayQuery.class), eq(wxPayConfig)))
                .thenReturn(prepayId);

        // 模拟数据库插入异常
        when(orderThirdPartyPrepayRepository.insert(any(OrderThirdPartyPrepayEntity.class)))
                .thenThrow(new RuntimeException("数据库插入失败"));

        // 执行测试并验证异常
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> wxMiniProgramHandler.onCreate(orderBo)
        );

        // 验证异常信息
        assertEquals("数据库插入失败", exception.getMessage());

        // 验证方法调用
        verify(orderThirdPartyPrepayRepository, times(1)).insert(any(OrderThirdPartyPrepayEntity.class));
        verify(mqService, never()).sendMessage(any());
    }

    /**
     * 测试正常关闭订单
     */
    @Test
    void testOnClose_Normal() {
        // 准备测试数据
        Transaction transaction = mock(Transaction.class);
        when(transaction.getTradeState()).thenReturn(Transaction.TradeStateEnum.NOTPAY);

        // 模拟依赖服务调用
        when(configService.getValueByKey(eq(WX_PAY_CONFIG), ArgumentMatchers.<TypeReference<WxPayConfig>>any()))
                .thenReturn(wxPayConfig);
        when(sdk.queryOrderByOutTradeNo(eq(orderBo.getOrderSn()), eq(wxPayConfig)))
                .thenReturn(transaction);
        doNothing().when(sdk).closePay(eq(orderBo.getOrderSn()), eq(wxPayConfig));

        // 执行测试
        assertDoesNotThrow(() -> wxMiniProgramHandler.onClose(orderBo));

        // 验证方法调用
        verify(configService, times(1)).getValueByKey(eq(WX_PAY_CONFIG), ArgumentMatchers.<TypeReference<WxPayConfig>>any());
        verify(sdk, times(1)).queryOrderByOutTradeNo(eq(orderBo.getOrderSn()), eq(wxPayConfig));
        verify(sdk, times(1)).closePay(eq(orderBo.getOrderSn()), eq(wxPayConfig));
    }

    /**
     * 测试订单已关闭状态
     */
    @Test
    void testOnClose_AlreadyClosed() {
        // 准备测试数据
        Transaction transaction = mock(Transaction.class);
        when(transaction.getTradeState()).thenReturn(Transaction.TradeStateEnum.CLOSED);

        // 模拟依赖服务调用
        when(configService.getValueByKey(eq(WX_PAY_CONFIG), ArgumentMatchers.<TypeReference<WxPayConfig>>any()))
                .thenReturn(wxPayConfig);
        when(sdk.queryOrderByOutTradeNo(eq(orderBo.getOrderSn()), eq(wxPayConfig)))
                .thenReturn(transaction);

        // 执行测试
        assertDoesNotThrow(() -> wxMiniProgramHandler.onClose(orderBo));

        // 验证方法调用
        verify(configService, times(1)).getValueByKey(eq(WX_PAY_CONFIG), ArgumentMatchers.<TypeReference<WxPayConfig>>any());
        verify(sdk, times(1)).queryOrderByOutTradeNo(eq(orderBo.getOrderSn()), eq(wxPayConfig));
        verify(sdk, never()).closePay(any(), any());
    }

    /**
     * 测试微信API异常情况
     */
    @Test
    void testOnClose_ServiceException() {
        // 准备测试数据
        BusinessRuntimeException serviceException = new BusinessRuntimeException("微信API调用失败");
        Transaction transaction = mock(Transaction.class);
        when(transaction.getTradeState()).thenReturn(Transaction.TradeStateEnum.NOTPAY);

        // 模拟依赖服务调用
        when(configService.getValueByKey(eq(WX_PAY_CONFIG), ArgumentMatchers.<TypeReference<WxPayConfig>>any()))
                .thenReturn(wxPayConfig);
        when(sdk.queryOrderByOutTradeNo(eq(orderBo.getOrderSn()), eq(wxPayConfig)))
                .thenReturn(transaction);
        doThrow(serviceException).when(sdk).closePay(eq(orderBo.getOrderSn()), eq(wxPayConfig));

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(
                BusinessRuntimeException.class,
                () -> wxMiniProgramHandler.onClose(orderBo)
        );

        // 验证异常信息
        assertTrue(exception.getMessage().contains("关闭微信订单"));
        assertTrue(exception.getMessage().contains(orderBo.getOrderSn()));
        assertTrue(exception.getMessage().contains("异常"));
        assertTrue(exception.getMessage().contains(serviceException.getMessage()));

        // 验证方法调用
        verify(configService, times(1)).getValueByKey(eq(WX_PAY_CONFIG), ArgumentMatchers.<TypeReference<WxPayConfig>>any());
        verify(sdk, times(1)).queryOrderByOutTradeNo(eq(orderBo.getOrderSn()), eq(wxPayConfig));
        verify(sdk, times(1)).closePay(eq(orderBo.getOrderSn()), eq(wxPayConfig));
    }

    /**
     * 测试查询订单异常情况
     */
    @Test
    void testOnClose_QueryOrderException() {
        // 准备测试数据
        BusinessRuntimeException serviceException = new BusinessRuntimeException("查询订单失败");

        // 模拟依赖服务调用
        when(configService.getValueByKey(eq(WX_PAY_CONFIG), ArgumentMatchers.<TypeReference<WxPayConfig>>any()))
                .thenReturn(wxPayConfig);
        when(sdk.queryOrderByOutTradeNo(eq(orderBo.getOrderSn()), eq(wxPayConfig)))
                .thenThrow(serviceException);

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(
                BusinessRuntimeException.class,
                () -> wxMiniProgramHandler.onClose(orderBo)
        );

        // 验证异常信息
        assertTrue(exception.getMessage().contains("关闭微信订单"));
        assertTrue(exception.getMessage().contains(orderBo.getOrderSn()));
        assertTrue(exception.getMessage().contains("异常"));
        assertTrue(exception.getMessage().contains(serviceException.getMessage()));

        // 验证方法调用
        verify(configService, times(1)).getValueByKey(eq(WX_PAY_CONFIG), ArgumentMatchers.<TypeReference<WxPayConfig>>any());
        verify(sdk, times(1)).queryOrderByOutTradeNo(eq(orderBo.getOrderSn()), eq(wxPayConfig));
        verify(sdk, never()).closePay(any(), any());
    }

    /**
     * 测试配置获取异常情况
     */
    @Test
    void testOnClose_ConfigException() {
        // 准备测试数据
        RuntimeException configException = new RuntimeException("配置获取失败");

        // 模拟配置服务抛出异常
        when(configService.getValueByKey(eq(WX_PAY_CONFIG), ArgumentMatchers.<TypeReference<WxPayConfig>>any()))
                .thenThrow(configException);

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(
                BusinessRuntimeException.class,
                () -> wxMiniProgramHandler.onClose(orderBo)
        );

        // 验证异常信息
        assertTrue(exception.getMessage().contains("关闭微信订单"));
        assertTrue(exception.getMessage().contains(orderBo.getOrderSn()));
        assertTrue(exception.getMessage().contains("异常"));
        assertTrue(exception.getMessage().contains(configException.getMessage()));

        // 验证方法调用
        verify(configService, times(1)).getValueByKey(eq(WX_PAY_CONFIG), ArgumentMatchers.<TypeReference<WxPayConfig>>any());
        verify(sdk, never()).queryOrderByOutTradeNo(any(), any());
        verify(sdk, never()).closePay(any(), any());
    }

    /**
     * 测试校验订单完成 - 正常情况
     */
    @Test
    void testOnCheckComplete_Normal() {
        // 准备测试数据
        Transaction transaction = mock(Transaction.class);
        TransactionAmount amount = mock(TransactionAmount.class);
        when(transaction.getTradeState()).thenReturn(Transaction.TradeStateEnum.SUCCESS);
        when(transaction.getAmount()).thenReturn(amount);
        when(amount.getTotal()).thenReturn(11000); // 110元，单位分
        when(transaction.getTransactionId()).thenReturn("wx_transaction_123456");

        // 模拟依赖服务调用
        when(configService.getValueByKey(eq(WX_PAY_CONFIG), ArgumentMatchers.<TypeReference<WxPayConfig>>any()))
                .thenReturn(wxPayConfig);
        when(sdk.queryOrderByOutTradeNo(eq(orderBo.getOrderSn()), eq(wxPayConfig)))
                .thenReturn(transaction);
        when(orderThirdPartyPrepayRepository.updateByOrderSn(any(OrderThirdPartyPrepayEntity.class)))
                .thenReturn(1);

        // 执行测试
        assertDoesNotThrow(() -> wxMiniProgramHandler.onCheckComplete(orderBo));

        // 验证方法调用
        verify(configService, times(1)).getValueByKey(eq(WX_PAY_CONFIG), ArgumentMatchers.<TypeReference<WxPayConfig>>any());
        verify(sdk, times(1)).queryOrderByOutTradeNo(eq(orderBo.getOrderSn()), eq(wxPayConfig));
        verify(orderThirdPartyPrepayRepository, times(1)).updateByOrderSn(any(OrderThirdPartyPrepayEntity.class));
    }

    /**
     * 测试校验订单完成 - 微信订单状态非SUCCESS
     */
    @Test
    void testOnCheckComplete_OrderNotSuccess() {
        // 准备测试数据
        Transaction transaction = mock(Transaction.class);
        when(transaction.getTradeState()).thenReturn(Transaction.TradeStateEnum.NOTPAY);

        // 模拟依赖服务调用
        when(configService.getValueByKey(eq(WX_PAY_CONFIG), ArgumentMatchers.<TypeReference<WxPayConfig>>any()))
                .thenReturn(wxPayConfig);
        when(sdk.queryOrderByOutTradeNo(eq(orderBo.getOrderSn()), eq(wxPayConfig)))
                .thenReturn(transaction);

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(
                BusinessRuntimeException.class,
                () -> wxMiniProgramHandler.onCheckComplete(orderBo)
        );

        // 验证异常信息
        assertTrue(exception.getMessage().contains("微信订单"));
        assertTrue(exception.getMessage().contains(orderBo.getOrderSn()));
        assertTrue(exception.getMessage().contains("没有支付完成"));

        // 验证方法调用
        verify(configService, times(1)).getValueByKey(eq(WX_PAY_CONFIG), ArgumentMatchers.<TypeReference<WxPayConfig>>any());
        verify(sdk, times(1)).queryOrderByOutTradeNo(eq(orderBo.getOrderSn()), eq(wxPayConfig));
        verify(orderThirdPartyPrepayRepository, never()).updateByOrderSn(any());
    }

    /**
     * 测试校验订单完成 - 订单金额不一致
     */
    @Test
    void testOnCheckComplete_AmountMismatch() {
        // 准备测试数据
        Transaction transaction = mock(Transaction.class);
        TransactionAmount amount = mock(TransactionAmount.class);
        when(transaction.getTradeState()).thenReturn(Transaction.TradeStateEnum.SUCCESS);
        when(transaction.getAmount()).thenReturn(amount);
        when(amount.getTotal()).thenReturn(20000); // 200元，与订单金额不一致

        // 模拟依赖服务调用
        when(configService.getValueByKey(eq(WX_PAY_CONFIG), ArgumentMatchers.<TypeReference<WxPayConfig>>any()))
                .thenReturn(wxPayConfig);
        when(sdk.queryOrderByOutTradeNo(eq(orderBo.getOrderSn()), eq(wxPayConfig)))
                .thenReturn(transaction);

        // 执行测试并验证异常
        PayAmountException exception = assertThrows(
                PayAmountException.class,
                () -> wxMiniProgramHandler.onCheckComplete(orderBo)
        );

        // 验证异常信息
        assertTrue(exception.getMessage().contains("微信订单"));
        assertTrue(exception.getMessage().contains(orderBo.getOrderSn()));
        assertTrue(exception.getMessage().contains("支付金额不一致"));

        // 验证方法调用
        verify(configService, times(1)).getValueByKey(eq(WX_PAY_CONFIG), ArgumentMatchers.<TypeReference<WxPayConfig>>any());
        verify(sdk, times(1)).queryOrderByOutTradeNo(eq(orderBo.getOrderSn()), eq(wxPayConfig));
        verify(orderThirdPartyPrepayRepository,  times(1))
                .updateByOrderSn(new OrderThirdPartyPrepayEntity().setOrderSn(orderBo.getOrderSn()).setThirdPartySn(transaction.getTransactionId()));
    }

    /**
     * 测试校验订单完成 - 配置获取异常
     */
    @Test
    void testOnCheckComplete_ConfigException() {
        // 准备测试数据
        RuntimeException configException = new RuntimeException("配置获取失败");

        // 模拟配置服务抛出异常
        when(configService.getValueByKey(eq(WX_PAY_CONFIG), ArgumentMatchers.<TypeReference<WxPayConfig>>any()))
                .thenThrow(configException);

        // 执行测试并验证异常
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> wxMiniProgramHandler.onCheckComplete(orderBo)
        );

        // 验证异常信息
        assertEquals("配置获取失败", exception.getMessage());

        // 验证方法调用
        verify(configService, times(1)).getValueByKey(eq(WX_PAY_CONFIG), ArgumentMatchers.<TypeReference<WxPayConfig>>any());
        verify(sdk, never()).queryOrderByOutTradeNo(any(), any());
        verify(orderThirdPartyPrepayRepository, never()).updateByOrderSn(any());
    }

    /**
     * 测试校验订单完成 - SDK查询订单异常
     */
    @Test
    void testOnCheckComplete_SdkQueryException() {
        // 准备测试数据
        RuntimeException sdkException = new RuntimeException("SDK查询订单失败");

        // 模拟依赖服务调用
        when(configService.getValueByKey(eq(WX_PAY_CONFIG), ArgumentMatchers.<TypeReference<WxPayConfig>>any()))
                .thenReturn(wxPayConfig);
        when(sdk.queryOrderByOutTradeNo(eq(orderBo.getOrderSn()), eq(wxPayConfig)))
                .thenThrow(sdkException);

        // 执行测试并验证异常
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> wxMiniProgramHandler.onCheckComplete(orderBo)
        );

        // 验证异常信息
        assertEquals("SDK查询订单失败", exception.getMessage());

        // 验证方法调用
        verify(configService, times(1)).getValueByKey(eq(WX_PAY_CONFIG), ArgumentMatchers.<TypeReference<WxPayConfig>>any());
        verify(sdk, times(1)).queryOrderByOutTradeNo(eq(orderBo.getOrderSn()), eq(wxPayConfig));
        verify(orderThirdPartyPrepayRepository, never()).updateByOrderSn(any());
    }

    /**
     * 测试支付通知 - 正常情况
     */
    @Test
    void testOnPayNotify_Normal() {
        // 准备测试数据
        String notifyData = "{\"id\":\"wx_notify_123\",\"out_trade_no\":\"" + orderBo.getOrderSn() + "\"}";
        RequestParam requestParam = new RequestParam.Builder()
                .body(notifyData)
                .build();
        Transaction transaction = mock(Transaction.class);
        TransactionAmount amount = mock(TransactionAmount.class);
        when(transaction.getTradeState()).thenReturn(Transaction.TradeStateEnum.SUCCESS);
        when(transaction.getAmount()).thenReturn(amount);
        when(amount.getCurrency()).thenReturn("CNY");
        when(amount.getTotal()).thenReturn(11000);
        when(transaction.getTransactionId()).thenReturn("wx_transaction_123456");
        when(transaction.getOutTradeNo()).thenReturn(orderBo.getOrderSn());

        // 模拟依赖服务调用
        when(configService.getValueByKey(eq(WX_PAY_CONFIG), ArgumentMatchers.<TypeReference<WxPayConfig>>any()))
                .thenReturn(wxPayConfig);
        when(sdk.parseWeiXinNotification(requestParam, wxPayConfig, Transaction.class))
                .thenReturn(transaction);
        when(orderThirdPartyPrepayRepository.updateByOrderSn(any(OrderThirdPartyPrepayEntity.class)))
                .thenReturn(1);

        // 执行测试
        OrderThirdPartyNotificationResponseDto result = assertDoesNotThrow(() -> wxMiniProgramHandler.onPayNotify(requestParam));

        // 验证返回结果
        assertNotNull(result);
        assertEquals(orderBo.getOrderSn(), result.getOrderSn());
        assertEquals(OrderStatusEnum.SUCCESS, result.getOrderStatus());
        assertEquals(orderBo.getUserPayAmount(), result.getPayOrderAmount());

        // 验证方法调用
        verify(configService, times(1)).getValueByKey(eq(WX_PAY_CONFIG), ArgumentMatchers.<TypeReference<WxPayConfig>>any());
        verify(sdk, times(1)).parseWeiXinNotification(requestParam, wxPayConfig, Transaction.class);
        verify(orderThirdPartyPrepayRepository, times(1)).updateByOrderSn(any(OrderThirdPartyPrepayEntity.class));
    }

    /**
     * 测试支付通知 - 支付币种异常
     */
    @Test
    void testOnPayNotify_CurrencyException() {
        // 准备测试数据
        String notifyData = "{\"id\":\"wx_notify_123\",\"out_trade_no\":\"" + orderBo.getOrderSn() + "\"}";
        RequestParam requestParam = new RequestParam.Builder()
                .body(notifyData)
                .build();
        Transaction transaction = mock(Transaction.class);
        TransactionAmount amount = mock(TransactionAmount.class);
        when(transaction.getAmount()).thenReturn(amount);
        when(amount.getCurrency()).thenReturn("USD"); // 非CNY币种

        // 模拟依赖服务调用
        when(configService.getValueByKey(eq(WX_PAY_CONFIG), ArgumentMatchers.<TypeReference<WxPayConfig>>any()))
                .thenReturn(wxPayConfig);
        when(sdk.parseWeiXinNotification(requestParam, wxPayConfig, Transaction.class))
                .thenReturn(transaction);

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(
                BusinessRuntimeException.class,
                () -> wxMiniProgramHandler.onPayNotify(requestParam)
        );

        // 验证异常信息
        assertTrue(exception.getMessage().contains("支付币种异常"));

        // 验证方法调用
        verify(configService, times(1)).getValueByKey(eq(WX_PAY_CONFIG), ArgumentMatchers.<TypeReference<WxPayConfig>>any());
        verify(sdk, times(1)).parseWeiXinNotification(requestParam, wxPayConfig, Transaction.class);
        verify(orderThirdPartyPrepayRepository, never()).updateByOrderSn(any());
    }

    /**
     * 测试支付通知 - SDK解析异常
     */
    @Test
    void testOnPayNotify_SdkParseException() {
        // 准备测试数据
        String notifyData = "{\"invalid\":\"data\"}";
        RequestParam requestParam = new RequestParam.Builder()
                .body(notifyData)
                .build();
        RuntimeException sdkException = new RuntimeException("SDK解析通知失败");

        // 模拟依赖服务调用
        when(configService.getValueByKey(eq(WX_PAY_CONFIG), ArgumentMatchers.<TypeReference<WxPayConfig>>any()))
                .thenReturn(wxPayConfig);
        when(sdk.parseWeiXinNotification(requestParam, wxPayConfig, Transaction.class))
                .thenThrow(sdkException);

        // 执行测试并验证异常
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> wxMiniProgramHandler.onPayNotify(requestParam)
        );

        // 验证异常信息
        assertEquals("SDK解析通知失败", exception.getMessage());

        // 验证方法调用
        verify(configService, times(1)).getValueByKey(eq(WX_PAY_CONFIG), ArgumentMatchers.<TypeReference<WxPayConfig>>any());
        verify(sdk, times(1)).parseWeiXinNotification(requestParam, wxPayConfig, Transaction.class);
        verify(orderThirdPartyPrepayRepository, never()).updateByOrderSn(any());
    }

    /**
     * 测试支付通知 - 订单状态转换测试（支付失败）
     */
    @Test
    void testOnPayNotify_PaymentFailed() {
        // 准备测试数据
        String notifyData = "{\"id\":\"wx_notify_123\",\"out_trade_no\":\"" + orderBo.getOrderSn() + "\"}";
        RequestParam requestParam = new RequestParam.Builder()
                .body(notifyData)
                .build();
        Transaction transaction = mock(Transaction.class);
        TransactionAmount amount = mock(TransactionAmount.class);
        when(transaction.getTradeState()).thenReturn(Transaction.TradeStateEnum.CLOSED);
        when(transaction.getAmount()).thenReturn(amount);
        when(amount.getCurrency()).thenReturn("CNY");
        when(amount.getTotal()).thenReturn(10000);
        when(transaction.getTransactionId()).thenReturn("wx_transaction_123456");
        when(transaction.getOutTradeNo()).thenReturn(orderBo.getOrderSn());

        // 模拟依赖服务调用
        when(configService.getValueByKey(eq(WX_PAY_CONFIG), ArgumentMatchers.<TypeReference<WxPayConfig>>any()))
                .thenReturn(wxPayConfig);
        when(sdk.parseWeiXinNotification(requestParam, wxPayConfig, Transaction.class))
                .thenReturn(transaction);
        when(orderThirdPartyPrepayRepository.updateByOrderSn(any(OrderThirdPartyPrepayEntity.class)))
                .thenReturn(1);

        // 执行测试
        OrderThirdPartyNotificationResponseDto result = assertDoesNotThrow(() -> wxMiniProgramHandler.onPayNotify(requestParam));

        // 验证返回结果
        assertNotNull(result);
        assertEquals(orderBo.getOrderSn(), result.getOrderSn());
        assertEquals(OrderStatusEnum.CLOSED, result.getOrderStatus()); // 支付失败应转为关闭状态

        // 验证方法调用
        verify(configService, times(1)).getValueByKey(eq(WX_PAY_CONFIG), ArgumentMatchers.<TypeReference<WxPayConfig>>any());
        verify(sdk, times(1)).parseWeiXinNotification(requestParam, wxPayConfig, Transaction.class);
        verify(orderThirdPartyPrepayRepository, times(1)).updateByOrderSn(any(OrderThirdPartyPrepayEntity.class));
    }

    /**
     * 测试支付通知 - 配置获取异常
     */
    @Test
    void testOnPayNotify_ConfigException() {
        // 准备测试数据
        String notifyData = "{\"id\":\"wx_notify_123\"}";
        RequestParam requestParam = new RequestParam.Builder()
                .body(notifyData)
                .build();
        RuntimeException configException = new RuntimeException("配置获取失败");

        // 模拟配置服务抛出异常
        when(configService.getValueByKey(eq(WX_PAY_CONFIG), ArgumentMatchers.<TypeReference<WxPayConfig>>any()))
                .thenThrow(configException);

        // 执行测试并验证异常
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> wxMiniProgramHandler.onPayNotify(requestParam)
        );

        // 验证异常信息
        assertEquals("配置获取失败", exception.getMessage());

        // 验证方法调用
        verify(configService, times(1)).getValueByKey(eq(WX_PAY_CONFIG), ArgumentMatchers.<TypeReference<WxPayConfig>>any());
        verify(sdk, never()).parseWeiXinNotification(any(), any(), any());
        verify(orderThirdPartyPrepayRepository, never()).updateByOrderSn(any());
    }
}