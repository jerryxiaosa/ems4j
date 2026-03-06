package info.zhihui.ems.business.finance.qo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单列表查询结果。
 */
@Data
@Accessors(chain = true)
public class OrderListItemQo {

    /**
     * 订单号
     */
    private String orderSn;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 用户真实姓名
     */
    private String userRealName;

    /**
     * 用户联系方式
     */
    private String userPhone;

    /**
     * 第三方用户ID
     */
    private String thirdPartyUserId;

    /**
     * 账户ID
     */
    private Integer accountId;

    /**
     * 账户归属者ID
     */
    private Integer ownerId;

    /**
     * 账户归属者类型
     */
    private Integer ownerType;

    /**
     * 账户归属者名称
     */
    private String ownerName;

    /**
     * 订单类型
     */
    private Integer orderType;

    /**
     * 订单金额
     */
    private BigDecimal orderAmount;

    /**
     * 币种
     */
    private String currency;

    /**
     * 服务费比例
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
    private String paymentChannel;

    /**
     * 订单状态
     */
    private String orderStatus;

    /**
     * 订单创建时间
     */
    private LocalDateTime orderCreateTime;

    /**
     * 订单支付截止时间
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

    /**
     * 第三方订单号
     */
    private String thirdPartySn;
}
