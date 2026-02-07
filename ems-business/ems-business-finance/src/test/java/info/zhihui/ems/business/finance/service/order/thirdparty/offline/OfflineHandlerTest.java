package info.zhihui.ems.business.finance.service.order.thirdparty.offline;

import info.zhihui.ems.business.finance.bo.OrderBo;
import info.zhihui.ems.business.finance.dto.order.OrderCreationResponseDto;
import info.zhihui.ems.business.finance.enums.OrderTypeEnum;
import info.zhihui.ems.business.finance.enums.PaymentChannelEnum;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.mq.api.model.MqMessage;
import info.zhihui.ems.mq.api.constant.order.OrderConstant;
import info.zhihui.ems.mq.api.message.order.OrderCompleteMessage;
import info.zhihui.ems.mq.api.service.MqService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * OfflineHandler单元测试
 *
 * @author jerryxiaosa
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("线下支付第三方处理器测试")
class OfflineHandlerTest {

    @Mock
    private MqService mqService;

    private OfflineHandler offlineHandler;

    @BeforeEach
    void setUp() {
        offlineHandler = new OfflineHandler(mqService);
    }

    @Test
    @DisplayName("获取支付渠道 - 应返回OFFLINE")
    void getPaymentChannel_ShouldReturnOffline_WhenCalled() {
        // When
        PaymentChannelEnum result = offlineHandler.getPaymentChannel();

        // Then
        assertThat(result).isEqualTo(PaymentChannelEnum.OFFLINE);
    }

    @Test
    @DisplayName("创建订单 - 应发送MQ消息并返回正确的响应")
    void onCreate_ShouldSendMqMessageAndReturnCorrectResponse_WhenGivenOrderBo() {
        // Given
        String orderSn = "TEST_OFFLINE_ORDER_001";
        LocalDateTime orderPayStopTime = LocalDateTime.now().plusHours(24);
        OrderBo orderBo = new OrderBo()
                .setOrderSn(orderSn)
                .setOrderType(OrderTypeEnum.ENERGY_TOP_UP)
                .setOrderPayStopTime(orderPayStopTime);

        // When
        OrderCreationResponseDto result = offlineHandler.onCreate(orderBo);

        // Then
        // 验证MQ消息发送
        ArgumentCaptor<MqMessage> mqMessageCaptor = ArgumentCaptor.forClass(MqMessage.class);
        verify(mqService, times(1)).sendMessageAfterCommit(mqMessageCaptor.capture());
        
        MqMessage capturedMessage = mqMessageCaptor.getValue();
        assertThat(capturedMessage.getMessageDestination()).isEqualTo(OrderConstant.ORDER_DESTINATION);
        assertThat(capturedMessage.getRoutingIdentifier()).isEqualTo(OrderConstant.ROUTING_KEY_ORDER_TRY_COMPLETE);
        
        // 验证MQ消息payload
        assertThat(capturedMessage.getPayload()).isInstanceOf(OrderCompleteMessage.class);
        OrderCompleteMessage payload = (OrderCompleteMessage) capturedMessage.getPayload();
        assertThat(payload.getOrderSn()).isEqualTo(orderSn);
        
        // 验证返回的响应
        assertThat(result).isNotNull();
        assertThat(result.getOrderSn()).isEqualTo(orderSn);
        assertThat(result.getOrderTypeEnum()).isEqualTo(OrderTypeEnum.ENERGY_TOP_UP);
        assertThat(result.getPaymentChannel()).isEqualTo(PaymentChannelEnum.OFFLINE);
        assertThat(result.getOrderPayStopTime()).isEqualTo(orderPayStopTime);
    }

    @Test
    @DisplayName("关闭订单 - 应抛出BusinessRuntimeException异常")
    void onClose_ShouldThrowBusinessRuntimeException_WhenGivenOrderBo() {
        // Given
        OrderBo orderBo = new OrderBo()
                .setOrderSn("TEST_OFFLINE_ORDER_002")
                .setOrderType(OrderTypeEnum.ENERGY_TOP_UP);

        // When & Then
        assertThatThrownBy(() -> offlineHandler.onClose(orderBo))
                .isInstanceOf(BusinessRuntimeException.class)
                .hasMessage("线下订单默认通过，无法关闭");
        
        // 验证没有调用MQ服务
        verifyNoInteractions(mqService);
    }

    @Test
    @DisplayName("创建订单 - 验证不同订单类型的处理")
    void onCreate_ShouldHandleDifferentOrderTypes_WhenGivenDifferentOrderBo() {
        // Given
        String orderSn = "TEST_TERMINATION_ORDER_001";
        LocalDateTime orderPayStopTime = LocalDateTime.now().plusDays(1);
        OrderBo orderBo = new OrderBo()
                .setOrderSn(orderSn)
                .setOrderType(OrderTypeEnum.ACCOUNT_TERMINATION_SETTLEMENT)
                .setOrderPayStopTime(orderPayStopTime);

        // When
        OrderCreationResponseDto result = offlineHandler.onCreate(orderBo);

        // Then
        assertThat(result.getOrderTypeEnum()).isEqualTo(OrderTypeEnum.ACCOUNT_TERMINATION_SETTLEMENT);
        assertThat(result.getOrderSn()).isEqualTo(orderSn);
        assertThat(result.getPaymentChannel()).isEqualTo(PaymentChannelEnum.OFFLINE);
        
        // 验证MQ消息仍然正确发送
        verify(mqService, times(1)).sendMessageAfterCommit(any(MqMessage.class));
    }

    @Test
    @DisplayName("校验订单完成 - 应记录日志且不抛出异常")
    void onCheckComplete_ShouldLogAndNotThrowException_WhenGivenOrderBo() {
        // Given
        OrderBo orderBo = new OrderBo()
                .setOrderSn("TEST_OFFLINE_ORDER_003")
                .setOrderType(OrderTypeEnum.ENERGY_TOP_UP);

        // When & Then
        // 验证方法执行不抛出异常
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> offlineHandler.onCheckComplete(orderBo));
        
        // 验证没有调用MQ服务
        verifyNoInteractions(mqService);
    }

    @Test
    @DisplayName("支付通知回调 - 应抛出BusinessRuntimeException异常")
    void onPayNotify_ShouldThrowBusinessRuntimeException_WhenGivenNotification() {
        // Given
        Object notification = new Object();

        // When & Then
        assertThatThrownBy(() -> offlineHandler.onPayNotify(notification))
                .isInstanceOf(BusinessRuntimeException.class)
                .hasMessage("线下订单没有回调通知");
        
        // 验证没有调用MQ服务
        verifyNoInteractions(mqService);
    }
}