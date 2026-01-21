package info.zhihui.ems.business.finance.entity.order;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单表
 */
@Data
@Accessors(chain = true)
@TableName("purchase_order")
public class OrderEntity {
    /**
     * 订单id
     */
    private Integer id;
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
    private Integer orderType;
    /**
     * 订单金额
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
     * 是否删除
     */
    private Boolean isDeleted;
}
