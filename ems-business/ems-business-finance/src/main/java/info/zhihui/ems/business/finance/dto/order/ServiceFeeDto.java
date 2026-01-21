package info.zhihui.ems.business.finance.dto.order;

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
     * 订单原始金额
     */
    private BigDecimal orderOriginalAmount;
}
