package info.zhihui.ems.business.finance.dto.order.creation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 销户/销表结算订单创建参数。
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class TerminationOrderCreationInfoDto extends OrderCreationInfoDto {

    /** 结算详情 */
    @Valid
    @NotNull(message = "结算详情不能为空")
    private TerminationSettlementDto terminationInfo;
}
