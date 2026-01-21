package info.zhihui.ems.business.finance.service.order.core;

import info.zhihui.ems.business.finance.bo.OrderBo;
import info.zhihui.ems.business.finance.dto.order.OrderCreationResponseDto;
import info.zhihui.ems.business.finance.dto.order.creation.OrderCreationInfoDto;
import info.zhihui.ems.business.finance.entity.order.OrderThirdPartyNotificationDto;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * @author jerryxiaosa
 */
public interface OrderService {

    /**
     * 获取订单详情
     *
     * @param orderSn 订单编号
     * @return 订单详情
     */
    OrderBo getDetail(@NotEmpty String orderSn);

    /**
     * 创建订单
     *
     * <p>该方法实现了订单创建的完整流程：</p>
     * <ol>
     *   <li>根据订单创建信息的类型，选择对应的订单创建处理器</li>
     *   <li>调用处理器处理订单创建逻辑，生成订单业务对象</li>
     *   <li>根据订单的支付渠道，选择对应的第三方支付处理器</li>
     *   <li>调用第三方支付处理器，完成支付相关处理并返回结果</li>
     * </ol>
     *
     * @param orderCreationInfoDto 订单创建信息DTO，包含创建订单所需的所有信息
     *                             不同类型的订单对应不同的DTO子类（如能耗订单、房屋租赁订单等）
     * @return 订单创建响应DTO，包含订单创建结果和支付相关信息
     * @throws BusinessRuntimeException 当不支持的订单类型或支付渠道时抛出业务异常
     */
    OrderCreationResponseDto createOrder(@Valid @NotNull OrderCreationInfoDto orderCreationInfoDto);

    /**
     * 响应三方支付回调通知
     *
     * @param notification 三方支付回调通知DTO，包含支付渠道、支付渠道返回的支付结果等信息
     */
    void answerPayNotify(@Valid @NotNull OrderThirdPartyNotificationDto notification);

    /**
     * 完成订单
     * 会根据订单的支付渠道进行校验
     *
     * @param orderSn 订单编号
     */
    void complete(@NotEmpty String orderSn);

    /**
     * 关闭订单
     *
     * @param orderSn 订单编号
     */
    void close(@NotEmpty String orderSn);
}
