package info.zhihui.ems.web.organization.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

/**
 * 组织查询条件 VO
 */
@Data
@Schema(name = "OrganizationQueryVo", description = "组织查询条件")
public class OrganizationQueryVo {

    @Schema(description = "组织ID集合")
    private Set<Integer> ids;

    @Size(max = 100)
    @Schema(description = "组织名称模糊查询条件")
    private String organizationNameLike;

    @Size(max = 64)
    @Schema(description = "统一社会信用代码/机构编码")
    private String creditCode;

    @Size(max = 50)
    @Schema(description = "负责人名称模糊查询条件")
    private String managerNameLike;
}
