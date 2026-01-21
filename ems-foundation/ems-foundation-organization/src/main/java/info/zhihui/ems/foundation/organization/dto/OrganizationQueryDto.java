package info.zhihui.ems.foundation.organization.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Set;

/**
 * 组织查询参数
 */
@Data
@Accessors(chain = true)
public class OrganizationQueryDto {
    /**
     * 主键ID集合（精确匹配）
     */
    private Set<Integer> ids;

    /**
     * 机构名称（模糊匹配，≤100）
     */
    @Size(max = 100)
    private String organizationNameLike;

    /**
     * 统一社会信用代码/机构编码（精确匹配，≤64）
     */
    @Size(max = 64)
    private String creditCode;

    /**
     * 负责人名称（模糊匹配，≤50）
     */
    @Size(max = 50)
    private String managerNameLike;
}
