package info.zhihui.ems.business.plan.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import info.zhihui.ems.components.datasource.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@TableName("energy_electric_price_plan")
public class ElectricPricePlanEntity extends BaseEntity {

    /**
     * 主键
     */
    private Integer id;

    /**
     * 电价方案名称
     */
    private String name;

    /**
     * 尖
     */
    private BigDecimal priceHigher;

    /**
     * 峰
     */
    private BigDecimal priceHigh;

    /**
     * 平
     */
    private BigDecimal priceLow;

    /**
     * 谷
     */
    private BigDecimal priceLower;

    /**
     * 深谷
     */
    private BigDecimal priceDeepLow;

    /**
     * 是否启用阶梯计费
     */
    private Boolean isStep;

    /**
     * 阶梯配置，json数组，{start:, end:, rate:},
     */
    private String stepPrice;

    /**
     * 是否自定义价格
     */
    private Boolean isCustomPrice;

    /**
     * 尖标准电价
     */
    private BigDecimal priceHigherBase;

    /**
     * 峰标准电价
     */
    private BigDecimal priceHighBase;

    /**
     * 平标准电价
     */
    private BigDecimal priceLowBase;

    /**
     * 谷标准电价
     */
    private BigDecimal priceLowerBase;

    /**
     * 深谷标准电价
     */
    private BigDecimal priceDeepLowBase;

    /**
     * 尖电价倍率
     */
    private BigDecimal priceHigherMultiply;

    /**
     * 峰电价倍率
     */
    private BigDecimal priceHighMultiply;

    /**
     * 平电价倍率
     */
    private BigDecimal priceLowMultiply;

    /**
     * 谷电价倍率
     */
    private BigDecimal priceLowerMultiply;

    /**
     * 深谷电价倍率
     */
    private BigDecimal priceDeepLowMultiply;

}
