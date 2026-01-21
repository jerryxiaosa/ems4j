package info.zhihui.ems.foundation.organization.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import info.zhihui.ems.components.datasource.entity.AreaBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@TableName("sys_organization")
public class OrganizationEntity extends AreaBaseEntity {
    /**
     * 主键ID
     */
    private Integer id;
    /**
     * 机构名称
     */
    private String organizationName;
    /**
     * 机构类型（枚举code）
     */
    private Integer organizationType;
    /**
     * 机构地址
     */
    private String organizationAddress;
    /**
     * 负责人名称
     */
    private String managerName;
    /**
     * 负责人电话
     */
    private String managerPhone;
    /**
     * 统一社会信用代码/机构编码
     */
    private String creditCode;
    /**
     * 入驻日期
     */
    private LocalDate entryDate;
    /**
     * 备注
     */
    private String remark;
}