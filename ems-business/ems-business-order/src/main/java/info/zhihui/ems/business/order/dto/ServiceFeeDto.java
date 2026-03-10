package info.zhihui.ems.business.order.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
public class ServiceFeeDto {
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
}
