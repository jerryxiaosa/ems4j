package info.zhihui.ems.web.organization.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;
import java.time.LocalDate;

/**
 * 组织更新 VO
 */
@Data
@Accessors(chain = true)
@Schema(name = "OrganizationUpdateVo", description = "组织更新参数")
public class OrganizationUpdateVo {

    @Size(max = 100)
    @Schema(description = "组织名称")
    private String organizationName;

    @Size(max = 64)
    @Schema(description = "统一社会信用代码/机构编码")
    private String creditCode;

    @Schema(description = "组织类型编码，参考 organizationType")
    private Integer organizationType;

    @Size(max = 255)
    @Schema(description = "组织地址")
    private String organizationAddress;

    @Size(max = 50)
    @Schema(description = "负责人名称")
    private String managerName;

    @Size(max = 40)
    @Schema(description = "负责人电话")
    private String managerPhone;

    @Schema(description = "入驻日期，格式yyyy-MM-dd")
    private LocalDate entryDate;

    @Schema(description = "备注说明")
    private String remark;

    @Schema(description = "所属区域ID")
    private Integer ownAreaId;
}
