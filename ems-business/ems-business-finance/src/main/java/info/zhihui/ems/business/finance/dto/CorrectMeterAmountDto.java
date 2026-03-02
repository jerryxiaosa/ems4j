package info.zhihui.ems.business.finance.dto;

import info.zhihui.ems.business.finance.enums.CorrectionTypeEnum;
import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import info.zhihui.ems.common.enums.OwnerTypeEnum;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 指定金额补正参数
 */
@Data
@Accessors(chain = true)
public class CorrectMeterAmountDto {

    /**
     * 账户ID
     */
    @NotNull(message = "账户ID不能为空")
    private Integer accountId;

    /**
     * 账户计费类型
     */
    @NotNull(message = "账户类型不能为空")
    private ElectricAccountTypeEnum electricAccountType;

    /**
     * 归属者ID
     */
    @NotNull(message = "归属者ID不能为空")
    private Integer ownerId;

    /**
     * 归属者类型
     */
    @NotNull(message = "归属者类型不能为空")
    private OwnerTypeEnum ownerType;

    /**
     * 归属者名称
     */
    @NotEmpty(message = "归属者名称不能为空")
    private String ownerName;

    /**
     * 电表ID（按需计费必填）
     */
    @NotNull(message = "电表ID不能为空")
    private Integer meterId;

    /**
     * 电表名称
     */
    private String meterName;

    /**
     * 电表编号
     */
    private String deviceNo;

    /**
     * 补正类型
     */
    @NotNull(message = "补正类型不能为空")
    private CorrectionTypeEnum correctionType;

    /**
     * 补正金额（大于0）
     */
    @NotNull(message = "补正金额不能为空")
    @DecimalMin(value = "0", inclusive = false, message = "补正金额必须大于0")
    private BigDecimal amount;

    /**
     * 补正原因
     */
    @NotEmpty(message = "补正原因不能为空")
    private String reason;

    /**
     * 补正对应的时间
     */
    private LocalDateTime correctionTime;

}
