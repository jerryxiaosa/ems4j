package info.zhihui.ems.business.finance.entity.order;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款记录表
 */
@Data
@Accessors(chain = true)
@TableName("order_refund")
public class OrderRefundEntity {
    /**
     * 退款id
     */
    private Integer id;
    /**
     * 退款号
     */
    private String refundSn;
    /**
     * 订单号
     */
    private String orderSn;
    /**
     * 退款方式：线下；微信
     */
    private String refundType;
    /**
     * 退款金额
     */
    private BigDecimal refundAmount;
    /**
     * 退款状态
     */
    private String status;
    /**
     * 退款原因
     */
    private String reason;
    /**
     * 退款入账账户
     */
    private String userReceivedAccount;
    /**
     * 申请人ID
     */
    private Integer applicantUserId;
    /**
     * 申请人真实名称
     */
    private String userRealName;
    /**
     * 申请人联系方式
     */
    private String userPhone;
    /**
     * 退款完成时间
     */
    private LocalDateTime refundSuccessTime;
    /**
     * 第三方的退款完成时间
     */
    private String thirdPartySuccessTime;
    /**
     * 是否删除
     */
    private Boolean isDeleted;
}
