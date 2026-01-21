package info.zhihui.ems.business.plan.dto;

import info.zhihui.ems.business.plan.bo.StepPriceBo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;

@Data
@Accessors(chain = true)
public class ElectricPricePlanSaveDto {

    /**
     * 主键
     */
    private Integer id;

     /**
     * 电价方案名称
     */
    @NotBlank(message = "名称不能为空")
    private String name;

    /**
     * 尖
     */
    @NotNull(message = "尖价格不能为空")
    private BigDecimal priceHigher;

    /**
     * 峰
     */
    @NotNull(message = "峰价格不能为空")
    private BigDecimal priceHigh;

    /**
     * 平
     */
    @NotNull(message = "平价格不能为空")
    private BigDecimal priceLow;

    /**
     * 谷
     */
    @NotNull(message = "谷价格不能为空")
    private BigDecimal priceLower;

    /**
     * 深谷
     */
    @NotNull(message = "深谷价格不能为空")
    private BigDecimal priceDeepLow;

    /**
     * 是否启用阶梯计费
     */
    private Boolean isStep;

    /**
     * 阶梯配置，json数组，{start:, end:, rate:},
     */
    private List<StepPriceBo> stepPrices;

    /**
     * 是否自定义价格
     */
    @NotNull(message = "是否自定义价格不能为空")
    private Boolean isCustomPrice;

    /**
     * 尖价格倍数
     */
    private BigDecimal priceHigherMultiply;

    /**
     * 峰价格倍数
     */
    private BigDecimal priceHighMultiply;

    /**
     * 平价格倍数
     */
    private BigDecimal priceLowMultiply;

    /**
     * 谷价格倍数
     */
    private BigDecimal priceLowerMultiply;

    /**
     * 深谷价格倍数
     */
    private BigDecimal priceDeepLowMultiply;

}
