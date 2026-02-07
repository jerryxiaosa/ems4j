package info.zhihui.ems.business.finance.service.order.handler.completion;

import info.zhihui.ems.business.finance.bo.OrderBo;
import info.zhihui.ems.business.finance.entity.order.OrderDetailTerminationEntity;
import info.zhihui.ems.business.finance.enums.OrderStatusEnum;
import info.zhihui.ems.business.finance.enums.OrderTypeEnum;
import info.zhihui.ems.business.finance.mapstruct.OrderMapper;
import info.zhihui.ems.business.finance.repository.order.OrderDetailTerminationRepository;
import info.zhihui.ems.business.finance.repository.order.OrderRepository;
import info.zhihui.ems.business.finance.service.order.handler.impl.OrderTerminationHandler;
import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import info.zhihui.ems.mq.api.model.MqMessage;
import info.zhihui.ems.common.utils.JacksonUtil;
import info.zhihui.ems.mq.api.constant.order.OrderConstant;
import info.zhihui.ems.mq.api.message.order.status.TerminationSuccessMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * OrderTerminationHandler单元测试
 *
 * @author jerryxiaosa
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("账户终止结算订单处理器测试")
class OrderTerminationHandlerTest {

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderDetailTerminationRepository orderDetailTerminationRepository;

    private OrderTerminationHandler orderTerminationHandler;

    @BeforeEach
    void setUp() {
        orderTerminationHandler = new OrderTerminationHandler(
                orderMapper,
                orderRepository,
                orderDetailTerminationRepository
        );
    }

    @Test
    @DisplayName("获取订单类型 - 应返回ACCOUNT_TERMINATION_SETTLEMENT")
    void getOrderType_ShouldReturnAccountTerminationSettlement_WhenCalled() {
        // When
        OrderTypeEnum result = orderTerminationHandler.getOrderType();

        // Then
        assertThat(result).isEqualTo(OrderTypeEnum.ACCOUNT_TERMINATION_SETTLEMENT);
    }

    @Test
    @DisplayName("创建订单成功后的MQ消息 - 应返回正确的消息格式")
    void createMessageAfterOrderSuccess_ShouldReturnCorrectMessage_WhenGivenOrderBo() {
        // Given
        OrderBo orderBo = new OrderBo()
                .setOrderSn("TEST_TERMINATION_ORDER_001")
                .setOrderStatus(OrderStatusEnum.SUCCESS);

        List<Integer> meterIdList = List.of(1, 2, 4);
        OrderDetailTerminationEntity terminationEntity = new OrderDetailTerminationEntity()
                .setOrderSn(orderBo.getOrderSn())
                .setAccountId(1)
                .setElectricAccountType(ElectricAccountTypeEnum.MONTHLY.getCode())
                .setFullCancel(true)
                .setElectricMeterAmount(3)
                .setSnapshotPayload(JacksonUtil.toJson(meterIdList));
        when(orderDetailTerminationRepository.selectByOrderSn(orderBo.getOrderSn())).thenReturn(terminationEntity);

        // When
        MqMessage result = orderTerminationHandler.createMessageAfterOrderSuccess(orderBo);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMessageDestination()).isEqualTo(OrderConstant.ORDER_DESTINATION);
        assertThat(result.getRoutingIdentifier()).isEqualTo(OrderConstant.ROUTING_KEY_ORDER_STATUS_SUCCESS_TERMINATION);

        // 验证payload内容
        assertThat(result.getPayload()).isInstanceOf(TerminationSuccessMessage.class);
        TerminationSuccessMessage payload = (TerminationSuccessMessage) result.getPayload();
        assertThat(payload.getOrderSn()).isEqualTo("TEST_TERMINATION_ORDER_001");
        assertThat(payload.getOrderStatus()).isEqualTo("SUCCESS");
        assertThat(payload.getAccountId()).isEqualTo(1);
        assertThat(payload.getElectricAccountType()).isEqualTo(ElectricAccountTypeEnum.MONTHLY);
        assertThat(payload.getFullCancel()).isEqualTo(true);
        assertThat(payload.getElectricMeterAmount()).isEqualTo(3);
        assertThat(payload.getMeterIdList()).isEqualTo(meterIdList);
    }
}