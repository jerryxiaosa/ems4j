package info.zhihui.ems.business.plan.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import info.zhihui.ems.components.datasource.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("energy_warn_plan")
public class WarnPlanEntity extends BaseEntity {

    private Integer id;

    /**
     * 名称
     */
    private String name;

    /**
     * 第一报警余额
     */
    private BigDecimal firstLevel;

    /**
     * 第二报警余额
     */
    private BigDecimal secondLevel;

    /**
     * 欠费自动断闸
     */
    private Boolean autoClose;

    /**
     * 备注
     */
    private String remark;

}
