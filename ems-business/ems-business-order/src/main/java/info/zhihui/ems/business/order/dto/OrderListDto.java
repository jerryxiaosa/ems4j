package info.zhihui.ems.business.order.dto;

import info.zhihui.ems.business.order.enums.OrderStatusEnum;
import info.zhihui.ems.business.order.enums.OrderTypeEnum;
import info.zhihui.ems.business.order.enums.PaymentChannelEnum;
import info.zhihui.ems.common.enums.OwnerTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单列表查询结果
 */
@Data
@Accessors(chain = true)
public class OrderListDto {
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
     * 第三方订单号
     */
    private String thirdPartySn;
    /**
     * 电表名称
     */
    private String meterName;
    /**
     * 电表编号
     */
    private String deviceNo;
    /**
     * 账户id
     */
    private Integer accountId;
    /**
     * 账户归属者id
     */
    private Integer ownerId;
    /**
     * 账户类型
     */
    private OwnerTypeEnum ownerType;
    /**
     * 企业/个人名称
     */
    private String ownerName;
    /**
     * 订单分类
     */
    private OrderTypeEnum orderType;
    /**
     * 订单总金额（系统资金方向：收入为正，支出为负）
     */
    private BigDecimal orderAmount;
    /**
     * 币种（默认 CNY）
     */
    private String currency;
    /**
     * 服务费占订单总金额的比例
     */
    private BigDecimal serviceRate;
    /**
     * 服务费金额
     */
    private BigDecimal serviceAmount;
    /**
     * 用户实际支付金额
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

    /**
     * 处理前余额
     */
    private BigDecimal beginBalance;

    /**
     * 处理后余额
     */
    private BigDecimal endBalance;
}
