package info.zhihui.ems.web.organization.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 组织下拉查询条件
 */
@Data
@Accessors(chain = true)
@Schema(name = "OrganizationOptionQueryVo", description = "组织下拉查询条件")
public class OrganizationOptionQueryVo {

    @Size(max = 100)
    @Schema(description = "组织名称模糊查询条件")
    private String organizationNameLike;

    @NotNull
    @Min(1)
    @Max(200)
    @Schema(description = "返回数量限制", defaultValue = "20")
    private Integer limit = 20;
}
