package info.zhihui.ems.business.finance.dto;

import info.zhihui.ems.common.enums.OwnerTypeEnum;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
public class MonthlyConsumeDto {
    /**
     * 账户ID
     */
    @NotNull(message = "账户ID不能为空")
    private Integer accountId;

    /**
     * 账户归属者id
     */
    @NotNull(message = "账户归属者id不能为空")
    private Integer ownerId;

    /**
     * 账户归属者类型
     */
    @NotNull(message = "账户归属者类型不能为空")
    private OwnerTypeEnum ownerType;

    /**
     * 账户归属者名称
     */
    @NotEmpty(message = "账户归属者名称不能为空")
    private String ownerName;

    /**
     * 包月费用
     */
    @NotNull(message = "包月费用不能为空")
    private BigDecimal monthlyPayAmount;

    /**
     * 消费时间
     */
    @NotNull(message = "消费时间不能为空")
    private LocalDateTime consumeTime;

}
