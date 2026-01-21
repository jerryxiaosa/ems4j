package info.zhihui.ems.business.plan.bo;

import info.zhihui.ems.common.model.OperatorInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class WarnPlanBo extends OperatorInfo {

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
