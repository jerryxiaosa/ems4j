package info.zhihui.ems.web.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 订单详情
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(name = "OrderDetailVo", description = "订单详情")
public class OrderDetailVo extends OrderVo {
    @Schema(description = "实际充值到账金额")
    private BigDecimal topUpAmount;
}
