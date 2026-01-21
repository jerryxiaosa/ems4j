package info.zhihui.ems.business.finance.service.order.handler.creation;

import info.zhihui.ems.business.finance.bo.OrderBo;
import info.zhihui.ems.business.finance.dto.order.creation.OrderCreationInfoDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * @author jerryxiaosa
 */
public interface OrderCreationHandler {
    /**
     * 获取支持的订单参数
     *
     * @return 订单参数
     */
    Class<? extends OrderCreationInfoDto> getSupportedParam();

    /**
     * 创建订单
     *
     * @param orderCreationInfoDto 订单参数
     * @return 订单信息
     */
    OrderBo createOrder(@Valid @NotNull OrderCreationInfoDto orderCreationInfoDto);
}
