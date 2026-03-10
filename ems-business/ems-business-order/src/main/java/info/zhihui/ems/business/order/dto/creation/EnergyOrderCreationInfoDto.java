package info.zhihui.ems.business.order.dto.creation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author jerryxiaosa
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class EnergyOrderCreationInfoDto extends OrderCreationInfoDto {
    @Valid
    @NotNull(message = "能耗充值信息不能为空")
    private EnergyTopUpDto energyTopUpDto;
}
