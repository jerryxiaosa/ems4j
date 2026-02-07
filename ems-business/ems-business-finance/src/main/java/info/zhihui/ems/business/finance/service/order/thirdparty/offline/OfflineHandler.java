package info.zhihui.ems.business.finance.service.order.thirdparty.offline;

import info.zhihui.ems.business.finance.bo.OrderBo;
import info.zhihui.ems.business.finance.dto.order.OrderCreationResponseDto;
import info.zhihui.ems.business.finance.entity.order.OrderThirdPartyNotificationResponseDto;
import info.zhihui.ems.business.finance.enums.PaymentChannelEnum;
import info.zhihui.ems.business.finance.service.order.thirdparty.OrderThirdPartyHandler;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.mq.api.model.MqMessage;
import info.zhihui.ems.common.utils.TransactionUtil;
import info.zhihui.ems.mq.api.constant.order.OrderConstant;
import info.zhihui.ems.mq.api.message.order.OrderCompleteMessage;
import info.zhihui.ems.mq.api.service.MqService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 线下支付第三方处理。
 *
 * @author jerryxiaosa
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OfflineHandler implements OrderThirdPartyHandler<OrderCreationResponseDto> {
    private final MqService mqService;

    /**
     * 获取支付渠道类型
     *
     * @return 支付渠道枚举
     */
    @Override
    public PaymentChannelEnum getPaymentChannel() {
        return PaymentChannelEnum.OFFLINE;
    }

    /**
     * 创建订单时的处理逻辑
     * 在第三方支付平台创建对应的支付订单
     *
     * @param orderBo 订单业务对象
     * @return 订单创建响应DTO
     */
    @Override
    public OrderCreationResponseDto onCreate(OrderBo orderBo) {
        OrderCompleteMessage orderCompleteEvent = new OrderCompleteMessage().setOrderSn(orderBo.getOrderSn());

        // 线下渠道直接出发订单完成
        mqService.sendMessageAfterCommit(new MqMessage()
                .setMessageDestination(OrderConstant.ORDER_DESTINATION)
                .setRoutingIdentifier(OrderConstant.ROUTING_KEY_ORDER_TRY_COMPLETE)
                .setPayload(orderCompleteEvent));

        // 线下渠道暂无额外信息，直接返回基础响应
        return new OrderCreationResponseDto()
                .setOrderSn(orderBo.getOrderSn())
                .setOrderTypeEnum(orderBo.getOrderType())
                .setPaymentChannel(PaymentChannelEnum.OFFLINE)
                .setOrderPayStopTime(orderBo.getOrderPayStopTime());
    }

    /**
     * 订单完成检查时的处理逻辑
     * 用于验证订单状态和执行完成后的业务逻辑
     *
     * @param orderBo 订单业务对象
     */
    @Override
    public void onCheckComplete(OrderBo orderBo) {
        log.info("校验线下订单[{}]：默认处理通过", orderBo.getOrderSn());
    }

    /**
     * 处理第三方支付平台的支付通知
     * 接收并处理来自第三方支付平台的异步通知
     *
     * @param notification 支付通知对象，具体类型由实现类定义
     * @return 订单状态对象
     */
    @Override
    public OrderThirdPartyNotificationResponseDto onPayNotify(Object notification) {
        throw new BusinessRuntimeException("线下订单没有回调通知");
    }

    /**
     * 关闭订单时的处理逻辑
     * 在第三方支付平台关闭对应的支付订单
     *
     * @param orderBo 订单业务对象
     */
    @Override
    public void onClose(OrderBo orderBo) {
        throw new BusinessRuntimeException("线下订单默认通过，无法关闭");
    }
}
