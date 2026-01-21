package info.zhihui.ems.web.space.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 空间创建 VO
 */
@Data
@Accessors(chain = true)
@Schema(name = "SpaceCreateVo", description = "空间创建参数")
public class SpaceCreateVo {

    @NotBlank
    @Schema(description = "空间名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotNull
    @Schema(description = "父空间ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer pid;

    @NotNull
    @Schema(description = "空间类型编码，参考 spaceType", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer type;

    @DecimalMin(value = "0", message = "面积不能小于0")
    @Schema(description = "空间面积", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal area;

    @Schema(description = "排序索引")
    private Integer sortIndex;
}
