package info.zhihui.ems.web.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 订单查询条件
 */
@Data
@Schema(name = "OrderQueryVo", description = "订单查询条件")
public class OrderQueryVo {

    @Schema(description = "订单状态标识，参考 orderStatus")
    private String orderStatus;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Schema(description = "订单创建开始时间，格式yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createStartTime;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Schema(description = "订单创建结束时间，格式yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createEndTime;

    @Schema(description = "支付渠道标识，参考 paymentChannel")
    private String paymentChannel;

    @Schema(description = "用户ID")
    private Integer userId;
}
