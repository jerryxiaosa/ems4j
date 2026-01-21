package info.zhihui.ems.web.organization.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;

/**
 * 组织信息 VO
 */
@Data
@Accessors(chain = true)
@Schema(name = "OrganizationVo", description = "组织信息")
public class OrganizationVo {

    @Schema(description = "组织ID")
    private Integer id;

    @Schema(description = "组织名称")
    private String organizationName;

    @Schema(description = "统一社会信用代码/机构编码")
    private String creditCode;

    @Schema(description = "组织类型编码，参考 organizationType")
    private Integer organizationType;

    @Schema(description = "组织地址")
    private String organizationAddress;

    @Schema(description = "入驻日期，格式yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate entryDate;

    @Schema(description = "备注说明")
    private String remark;

    @Schema(description = "所属区域ID")
    private Integer ownAreaId;

    @Schema(description = "负责人名称")
    private String managerName;

    @Schema(description = "负责人电话")
    private String managerPhone;
}
