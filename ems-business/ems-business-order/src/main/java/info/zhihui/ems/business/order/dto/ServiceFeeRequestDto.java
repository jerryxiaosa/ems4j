package info.zhihui.ems.business.order.dto;

import info.zhihui.ems.business.order.enums.OrderTypeEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
public class ServiceFeeRequestDto {
    /**
     * 订单分类
     */
    @NotNull(message = "订单类型不能为空")
    private OrderTypeEnum orderType;

    /**
     * 订单总金额
     */
    @NotNull(message = "订单金额不能为空")
    private BigDecimal orderOriginalAmount;
}
