package info.zhihui.ems.business.finance.bo;

import info.zhihui.ems.business.finance.enums.OrderStatusEnum;
import info.zhihui.ems.business.finance.enums.OrderTypeEnum;
import info.zhihui.ems.business.finance.enums.PaymentChannelEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
public class OrderBo {
    /**
     * 订单号
     */
    private String orderSn;
    /**
     * 用户id
     */
    private Integer userId;
    /**
     * 用户真实名称
     */
    private String userRealName;
    /**
     * 用户联系方式
     */
    private String userPhone;
    /**
     * 第三方用户id
     */
    private String thirdPartyUserId;
    /**
     * 订单分类
     */
    private OrderTypeEnum orderType;
    /**
     * 订单金额，订单的实际价值
     */
    private BigDecimal orderAmount;
    /**
     * 币种（默认 CNY）
     */
    private String currency;
    /**
     * 服务费比例(%)
     */
    private BigDecimal serviceRate;
    /**
     * 服务费金额
     */
    private BigDecimal serviceAmount;
    /**
     * 用户实际支付金额
     * 可能比订单金额多，因为加了服务费
     * 可能比订单金额少，因为折扣
     */
    private BigDecimal userPayAmount;
    /**
     * 支付渠道
     */
    private PaymentChannelEnum paymentChannel;
    /**
     * 订单状态
     */
    private OrderStatusEnum orderStatus;
    /**
     * 订单生成时间
     */
    private LocalDateTime orderCreateTime;
    /**
     * 订单截止支付时间
     */
    private LocalDateTime orderPayStopTime;
    /**
     * 支付完成时间
     */
    private LocalDateTime orderSuccessTime;
    /**
     * 订单备注
     */
    private String remark;
    /**
     * 票据号
     */
    private String ticketNo;
}
