package info.zhihui.ems.web.plan.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import info.zhihui.ems.common.serializer.BigDecimalScaleSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;

/**
 * 电价方案保存请求
 */
@Data
@Accessors(chain = true)
@Schema(name = "ElectricPricePlanSaveVo", description = "电价方案保存参数")
public class ElectricPricePlanSaveVo {

    @Schema(description = "方案ID，编辑时必填")
    private Integer id;

    @NotBlank
    @Schema(description = "方案名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotNull
    @JsonSerialize(using = BigDecimalScaleSerializer.class)
    @Schema(description = "尖电价", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal priceHigher;

    @NotNull
    @JsonSerialize(using = BigDecimalScaleSerializer.class)
    @Schema(description = "峰电价", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal priceHigh;

    @NotNull
    @JsonSerialize(using = BigDecimalScaleSerializer.class)
    @Schema(description = "平电价", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal priceLow;

    @NotNull
    @JsonSerialize(using = BigDecimalScaleSerializer.class)
    @Schema(description = "谷电价", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal priceLower;

    @NotNull
    @JsonSerialize(using = BigDecimalScaleSerializer.class)
    @Schema(description = "深谷电价", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal priceDeepLow;

    @Schema(description = "是否启用阶梯电价")
    private Boolean isStep;

    @Valid
    @Schema(description = "阶梯电价配置列表")
    private List<StepPriceVo> stepPrices;

    @NotNull
    @Schema(description = "是否自定义价格", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean isCustomPrice;

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

    @Schema(description = "深谷电价倍率")
    private BigDecimal priceDeepLowMultiply;
}
