package info.zhihui.ems.business.account.dto;

import info.zhihui.ems.business.device.dto.MeterOpenDetailDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 账户追加电表开户请求
 */
@Data
@Accessors(chain = true)
public class AccountMetersOpenDto {

    /**
     * 账户ID
     */
    @NotNull(message = "账户ID不能为空")
    private Integer accountId;

    /**
     * 待绑定的电表列表
     */
    @NotEmpty(message = "电表列表不能为空")
    @Valid
    private List<MeterOpenDetailDto> electricMeterList;

    /**
     * 是否继承历史阶梯量
     */
    private Boolean inheritHistoryPower;
}
