package info.zhihui.ems.business.order.qo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 订单查询对象
 *
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
public class OrderQueryQo {

    /**
     * 订单类型
     */
    private Integer orderType;

    /**
     * 订单状态
     */
    private String orderStatus;

    /**
     * 订单号模糊搜索
     */
    private String orderSnLike;

    /**
     * 第三方订单号模糊搜索
     */
    private String thirdPartySnLike;

    /**
     * 企业名称模糊搜索
     */
    private String enterpriseNameLike;

    /**
     * 主体类型
     */
    private Integer ownerType;

    /**
     * 订单创建开始时间
     */
    private LocalDateTime createStartTime;

    /**
     * 订单创建结束时间
     */
    private LocalDateTime createEndTime;

    /**
     * 支付渠道
     */
    private String paymentChannel;
}
