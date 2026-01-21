package info.zhihui.ems.business.finance.service.order.thirdparty;

import info.zhihui.ems.business.finance.bo.OrderBo;
import info.zhihui.ems.business.finance.dto.order.OrderCreationResponseDto;
import info.zhihui.ems.business.finance.entity.order.OrderThirdPartyNotificationResponseDto;
import info.zhihui.ems.business.finance.enums.PaymentChannelEnum;

/**
 * 订单第三方支付处理器接口
 * 定义了订单在第三方支付平台的生命周期管理方法
 *
 * @param <R> 订单创建响应DTO类型，继承自OrderCreationResponseDto
 * @author jerryxiaosa
 */
public interface OrderThirdPartyHandler<R extends OrderCreationResponseDto> {

    /**
     * 获取支付渠道类型
     *
     * @return 支付渠道枚举
     */
    PaymentChannelEnum getPaymentChannel();

    /**
     * 创建订单时的处理逻辑
     * 在第三方支付平台创建对应的支付订单
     *
     * @param orderBo 订单业务对象
     * @return 订单创建响应DTO
     */
    R onCreate(OrderBo orderBo);

    /**
     * 订单完成检查时的处理逻辑
     * 用于验证订单状态和执行完成后的业务逻辑
     *
     * @param orderBo 订单业务对象
     */
    void onCheckComplete(OrderBo orderBo);

    /**
     * 处理第三方支付平台的支付通知
     * 接收并处理来自第三方支付平台的异步通知
     *
     * @param notification 支付通知对象，具体类型由实现类定义
     * @return 订单状态对象
     */
    OrderThirdPartyNotificationResponseDto onPayNotify(Object notification);

    /**
     * 关闭订单时的处理逻辑
     * 在第三方支付平台关闭对应的支付订单
     *
     * @param orderBo 订单业务对象
     */
    void onClose(OrderBo orderBo);
}
