package info.zhihui.ems.business.plan.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
public class WarnPlanSaveDto {

    private Integer id;

    /**
     * 名称
     */
    @NotBlank
    private String name;

    /**
     * 第一报警余额
     */
    @NotNull
    private BigDecimal firstLevel;

    /**
     * 第二报警余额
     */
    @NotNull
    private BigDecimal secondLevel;

    /**
     * 欠费自动断闸
     */
    @NotNull
    private Boolean autoClose;

    /**
     * 备注
     */
    private String remark;

}
