package info.zhihui.ems.web.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 订单创建响应
 */
@Data
@Accessors(chain = true)
@Schema(name = "OrderCreationResponseVo", description = "订单创建结果")
public class OrderCreationResponseVo {

    @Schema(description = "订单编号")
    private String orderSn;

    @Schema(description = "订单类型编码，参考 orderType")
    private Integer orderType;

    @Schema(description = "支付渠道标识，参考 paymentChannel")
    private String paymentChannel;

    @Schema(description = "订单支付截止时间")
    private LocalDateTime orderPayStopTime;
}
