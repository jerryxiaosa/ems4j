package info.zhihui.ems.foundation.organization.bo;

import info.zhihui.ems.foundation.organization.enums.OrganizationTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;

/**
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
public class OrganizationBo {
    /**
     * 主键ID
     */
    private Integer id;
    /**
     * 机构名称
     */
    private String name;
    /**
     * 机构统一社会信用代码/机构编码
     */
    private String creditCode;
    /**
     * 机构类型
     */
    private OrganizationTypeEnum organizationType;
    /**
     * 机构地址
     */
    private String organizationAddress;
    /**
     * 入驻日期
     */
    private LocalDate entryDate;
    /**
     * 备注
     */
    private String remark;
    /**
     * 所属区域ID
     */
    private Integer ownAreaId;

    /**
     * 负责人名称
     */
    private String managerName;

    /**
     * 负责人电话
     */
    private String managerPhone;
}
