package info.zhihui.ems.web.plan.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import info.zhihui.ems.common.serializer.BigDecimalScaleSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 电价方案列表展示
 */
@Data
@Accessors(chain = true)
@Schema(name = "ElectricPricePlanVo", description = "电价方案信息")
public class ElectricPricePlanVo {

    @Schema(description = "方案ID")
    private Integer id;

    @Schema(description = "方案名称")
    private String name;

    @JsonSerialize(using = BigDecimalScaleSerializer.class)
    @Schema(description = "尖电价")
    private BigDecimal priceHigher;

    @JsonSerialize(using = BigDecimalScaleSerializer.class)
    @Schema(description = "峰电价")
    private BigDecimal priceHigh;

    @JsonSerialize(using = BigDecimalScaleSerializer.class)
    @Schema(description = "平电价")
    private BigDecimal priceLow;

    @JsonSerialize(using = BigDecimalScaleSerializer.class)
    @Schema(description = "谷电价")
    private BigDecimal priceLower;

    @JsonSerialize(using = BigDecimalScaleSerializer.class)
    @Schema(description = "深谷电价")
    private BigDecimal priceDeepLow;

    @Schema(description = "是否启用阶梯电价")
    private Boolean isStep;

    @Schema(description = "阶梯配置JSON")
    private String stepPrice;

    @Schema(description = "是否自定义价格")
    private Boolean isCustomPrice;

    @JsonSerialize(using = BigDecimalScaleSerializer.class)
    @Schema(description = "尖基准价")
    private BigDecimal priceHigherBase;

    @JsonSerialize(using = BigDecimalScaleSerializer.class)
    @Schema(description = "峰基准价")
    private BigDecimal priceHighBase;

    @JsonSerialize(using = BigDecimalScaleSerializer.class)
    @Schema(description = "平基准价")
    private BigDecimal priceLowBase;

    @JsonSerialize(using = BigDecimalScaleSerializer.class)
    @Schema(description = "谷基准价")
    private BigDecimal priceLowerBase;

    @JsonSerialize(using = BigDecimalScaleSerializer.class)
    @Schema(description = "深谷基准价")
    private BigDecimal priceDeepLowBase;

    @JsonSerialize(using = BigDecimalScaleSerializer.class)
    @Schema(description = "尖电价倍率")
    private BigDecimal priceHigherMultiply;

    @JsonSerialize(using = BigDecimalScaleSerializer.class)
    @Schema(description = "峰电价倍率")
    private BigDecimal priceHighMultiply;

    @JsonSerialize(using = BigDecimalScaleSerializer.class)
    @Schema(description = "平电价倍率")
    private BigDecimal priceLowMultiply;

    @JsonSerialize(using = BigDecimalScaleSerializer.class)
    @Schema(description = "谷电价倍率")
    private BigDecimal priceLowerMultiply;

    @JsonSerialize(using = BigDecimalScaleSerializer.class)
    @Schema(description = "深谷电价倍率")
    private BigDecimal priceDeepLowMultiply;

    @Schema(description = "创建人ID")
    private Integer createUser;

    @Schema(description = "创建人名称")
    private String createUserName;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新人ID")
    private Integer updateUser;

    @Schema(description = "更新人名称")
    private String updateUserName;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
