package info.zhihui.ems.business.finance.dto.order;

import info.zhihui.ems.business.finance.enums.OrderTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
public class ServiceFeeRequestDto {
    /**
     * 订单分类
     */
    private OrderTypeEnum orderType;
    /**
     * 订单金额
     */
    private BigDecimal orderOriginalAmount;
}
