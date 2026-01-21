package info.zhihui.ems.business.finance.service.order.handler.completion;

import info.zhihui.ems.business.finance.bo.OrderBo;
import info.zhihui.ems.business.finance.entity.order.OrderDetailEnergyTopUpEntity;
import info.zhihui.ems.business.finance.enums.OrderStatusEnum;
import info.zhihui.ems.business.finance.enums.OrderTypeEnum;
import info.zhihui.ems.business.finance.mapstruct.OrderMapper;
import info.zhihui.ems.business.finance.repository.order.OrderDetailEnergyTopUpRepository;
import info.zhihui.ems.business.finance.repository.order.OrderRepository;
import info.zhihui.ems.business.finance.service.order.fee.ServiceRateService;
import info.zhihui.ems.business.finance.service.order.handler.impl.OrderEnergyTopUpHandler;
import info.zhihui.ems.common.enums.BalanceTypeEnum;
import info.zhihui.ems.common.model.MqMessage;
import info.zhihui.ems.foundation.space.service.SpaceService;
import info.zhihui.ems.mq.api.constant.order.OrderConstant;
import info.zhihui.ems.mq.api.message.order.status.EnergyTopUpSuccessMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * OrderEnergyTopUpHandler单元测试
 *
 * @author jerryxiaosa
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("能耗充值订单处理器测试")
class OrderEnergyTopUpHandlerTest {

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderDetailEnergyTopUpRepository orderDetailEnergyTopUpRepository;

    @Mock
    private ServiceRateService serviceRateService;

    @Mock
    private SpaceService spaceService;

    private OrderEnergyTopUpHandler orderEnergyTopUpHandler;

    @BeforeEach
    void setUp() {
        orderEnergyTopUpHandler = new OrderEnergyTopUpHandler(
                orderMapper,
                orderRepository,
                orderDetailEnergyTopUpRepository,
                serviceRateService,
                spaceService
        );
    }

    @Test
    @DisplayName("获取订单类型 - 应返回ENERGY_TOP_UP")
    void getOrderType_ShouldReturnEnergyTopUp_WhenCalled() {
        // When
        OrderTypeEnum result = orderEnergyTopUpHandler.getOrderType();

        // Then
        assertThat(result).isEqualTo(OrderTypeEnum.ENERGY_TOP_UP);
    }

    @Test
    @DisplayName("创建订单成功后的MQ消息 - 应返回正确的消息格式")
    void createMessageAfterOrderSuccess_ShouldReturnCorrectMessage_WhenGivenOrderBo() {
        // Given
        OrderBo orderBo = new OrderBo()
                .setOrderAmount(BigDecimal.valueOf(101))
                .setOrderSn("TEST_ORDER_001")
                .setOrderStatus(OrderStatusEnum.SUCCESS);
        OrderDetailEnergyTopUpEntity orderDetail = new OrderDetailEnergyTopUpEntity()
                .setAccountId(1)
                .setMeterId(1001)
                .setBalanceType(BalanceTypeEnum.ELECTRIC_METER.getCode());
        when(orderDetailEnergyTopUpRepository.selectByOrderSn(orderBo.getOrderSn())).thenReturn(orderDetail);

        // When
        MqMessage result = orderEnergyTopUpHandler.createMessageAfterOrderSuccess(orderBo);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMessageDestination()).isEqualTo(OrderConstant.ORDER_DESTINATION);
        assertThat(result.getRoutingIdentifier()).isEqualTo(OrderConstant.ROUTING_KEY_ORDER_STATUS_SUCCESS_ENERGY_TOP_UP);

        // 验证payload内容
        assertThat(result.getPayload()).isInstanceOf(EnergyTopUpSuccessMessage.class);
        EnergyTopUpSuccessMessage payload = (EnergyTopUpSuccessMessage) result.getPayload();
        assertThat(payload.getOrderSn()).isEqualTo("TEST_ORDER_001");
        assertThat(payload.getOrderStatus()).isEqualTo("SUCCESS");
        assertThat(payload.getOrderAmount()).isEqualTo(new BigDecimal("101"));
        assertThat(payload.getBalanceType()).isEqualTo(BalanceTypeEnum.ELECTRIC_METER);
        assertThat(payload.getAccountId()).isEqualTo(1);
        assertThat(payload.getMeterId()).isEqualTo(1001);
    }
}