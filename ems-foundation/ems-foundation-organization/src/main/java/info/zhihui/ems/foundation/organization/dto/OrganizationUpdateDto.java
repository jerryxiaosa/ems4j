package info.zhihui.ems.foundation.organization.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import info.zhihui.ems.foundation.organization.enums.OrganizationTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;
import java.time.LocalDate;

@Data
@Accessors(chain = true)
public class OrganizationUpdateDto {
    /**
     * 主键ID（必填）
     */
    @NotNull
    private Integer id;

    /**
     * 机构名称（≤100）
     */
    @Size(max = 100)
    private String organizationName;

    /**
     * 统一社会信用代码/机构编码（≤64）
     */
    @Size(max = 64)
    private String creditCode;

    /**
     * 机构类型（枚举）
     */
    private OrganizationTypeEnum organizationType;

    /**
     * 机构地址（≤255）
     */
    @Size(max = 255)
    private String organizationAddress;

    /**
     * 负责人名称（≤50）
     */
    @Size(max = 50)
    private String managerName;

    /**
     * 负责人电话（≤40）
     */
    @Size(max = 40)
    private String managerPhone;

    /**
     * 入驻日期（如有则填）
     */
    private LocalDate entryDate;

    /**
     * 备注（≤500，若有）
     */
    @Size(max = 500)
    private String remark;

    /**
     * 所属区域ID（若更新区域则填）
     */
    private Integer ownAreaId;
}
