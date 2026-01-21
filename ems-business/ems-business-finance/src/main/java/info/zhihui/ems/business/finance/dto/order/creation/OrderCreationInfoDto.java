package info.zhihui.ems.business.finance.dto.order.creation;

import info.zhihui.ems.business.finance.enums.PaymentChannelEnum;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
public abstract class OrderCreationInfoDto {
    /**
     * 创建用户id
     */
    @NotNull(message = "用户id不能为空")
    private Integer userId;

    /**
     * 用户手机号
     */
    @NotEmpty(message = "用户手机号不能为空")
    private String userPhone;

    /**
     * 用户真实姓名
     */
    @NotEmpty(message = "用户真实姓名不能为空")
    private String userRealName;

    /**
     * 第三方用户id
     */
    @NotEmpty(message = "第三方用户id不能为空")
    private String thirdPartyUserId;

    /**
     * 订单金额
     */
    @NotNull(message = "订单金额不能为空")
    private BigDecimal orderAmount;

    /**
     * 支付渠道
     */
    @NotNull(message = "支付渠道不能为空")
    private PaymentChannelEnum paymentChannel;

}
