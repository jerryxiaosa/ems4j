package info.zhihui.ems.business.plan.dto;

import info.zhihui.ems.common.enums.ElectricPricePeriodEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalTime;

/**
 * 电费时间段
 */
@Data
@Accessors(chain = true)
public class ElectricPriceTimeDto {
    /**
     * 电价类型
     */
    private ElectricPricePeriodEnum type;

    /**
     * 开始时间点
     */
    @NotNull
    private LocalTime start;
}
