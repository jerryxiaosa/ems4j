package info.zhihui.ems.business.finance.entity.order;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 第三方支付预订单
 */
@Data
@Accessors(chain = true)
@TableName("order_third_party_prepay")
public class OrderThirdPartyPrepayEntity {
    /**
     * 主键ID
     */
    private Integer id;
    /**
     * 订单号
     */
    private String orderSn;
    /**
     * 预订单id
     */
    private String prepayId;
    /**
     * 第三方用户号
     */
    private String thirdPartyUserId;
    /**
     * 第三方订单编号
     */
    private String thirdPartySn;
    /**
     * 预订单生成时间
     */
    private LocalDateTime prepayAt;
    /**
     * 是否删除
     */
    private Boolean isDeleted;
}
