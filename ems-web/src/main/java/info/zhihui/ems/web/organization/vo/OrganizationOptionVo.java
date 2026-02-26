package info.zhihui.ems.web.organization.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 组织下拉选项
 */
@Data
@Accessors(chain = true)
@Schema(name = "OrganizationOptionVo", description = "组织下拉选项")
public class OrganizationOptionVo {

    @Schema(description = "组织ID")
    private Integer id;

    @Schema(description = "组织名称")
    private String organizationName;

    @Schema(description = "组织类型编码，参考 organizationType")
    private Integer organizationType;

    @Schema(description = "负责人名称")
    private String managerName;

    @Schema(description = "负责人电话")
    private String managerPhone;
}
