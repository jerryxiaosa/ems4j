package info.zhihui.ems.business.plan.dto;

import info.zhihui.ems.common.enums.ElectricPricePeriodEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 电费价格
 */
@Data
@Accessors(chain = true)
public class ElectricPriceTypeDto {

    /**
     * 类型
     */
    private ElectricPricePeriodEnum type;

    /**
     * 价格
     */
    private BigDecimal price;
}
