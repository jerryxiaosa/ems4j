package info.zhihui.ems.web.organization.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;
import java.time.LocalDate;

/**
 * 组织创建 VO
 */
@Data
@Accessors(chain = true)
@Schema(name = "OrganizationCreateVo", description = "组织创建参数")
public class OrganizationCreateVo {

    @NotBlank
    @Size(max = 100)
    @Schema(description = "组织名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String organizationName;

    @NotBlank
    @Size(max = 64)
    @Schema(description = "统一社会信用代码/机构编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String creditCode;

    @NotNull
    @Schema(description = "组织类型编码，参考 organizationType", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer organizationType;

    @Size(max = 255)
    @Schema(description = "组织地址")
    private String organizationAddress;

    @NotBlank
    @Size(max = 50)
    @Schema(description = "负责人名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String managerName;

    @NotBlank
    @Size(max = 40)
    @Schema(description = "负责人电话", requiredMode = Schema.RequiredMode.REQUIRED)
    private String managerPhone;

    @Schema(description = "入驻日期，格式yyyy-MM-dd")
    private LocalDate entryDate;

    @Schema(description = "备注说明")
    private String remark;

    @NotNull
    @Schema(description = "所属区域ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer ownAreaId;
}
