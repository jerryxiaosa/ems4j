package info.zhihui.ems.business.account.dto;

import info.zhihui.ems.business.device.dto.MeterCancelDetailDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
public class CancelAccountDto {
    @NotNull(message = "账户ID不能为空")
    private Integer accountId;

    private String remark;

    @Valid
    @NotEmpty(message = "销户电表列表不能为空")
    private List<MeterCancelDetailDto> meterList;
}
