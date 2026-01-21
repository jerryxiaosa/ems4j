package info.zhihui.ems.web.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单信息
 */
@Data
@Accessors(chain = true)
@Schema(name = "OrderVo", description = "订单信息")
public class OrderVo {

    @Schema(description = "订单编号")
    private String orderSn;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "用户真实姓名")
    private String userRealName;

    @Schema(description = "用户联系方式")
    private String userPhone;

    @Schema(description = "第三方用户ID")
    private String thirdPartyUserId;

    @Schema(description = "订单类型编码，参考 orderType")
    private Integer orderType;

    @Schema(description = "订单金额")
    private BigDecimal orderAmount;

    @Schema(description = "币种")
    private String currency;

    @Schema(description = "服务费比例(%)")
    private BigDecimal serviceRate;

    @Schema(description = "服务费金额")
    private BigDecimal serviceAmount;

    @Schema(description = "用户实际支付金额")
    private BigDecimal userPayAmount;

    @Schema(description = "支付渠道标识，参考 paymentChannel")
    private String paymentChannel;

    @Schema(description = "订单状态标识，参考 orderStatus")
    private String orderStatus;

    @Schema(description = "订单创建时间")
    private LocalDateTime orderCreateTime;

    @Schema(description = "订单支付截止时间")
    private LocalDateTime orderPayStopTime;

    @Schema(description = "支付完成时间")
    private LocalDateTime orderSuccessTime;

    @Schema(description = "订单备注")
    private String remark;

    @Schema(description = "票据号")
    private String ticketNo;
}
