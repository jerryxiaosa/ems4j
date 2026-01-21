package info.zhihui.ems.web.account.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 账户追加电表绑定请求
 */
@Data
@Accessors(chain = true)
@Schema(name = "AccountMetersOpenVo", description = "账户追加电表绑定请求参数")
public class AccountMetersOpenVo {

    @Valid
    @NotEmpty
    @Schema(description = "待绑定的电表列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<MeterOpenDetailVo> electricMeterList;

    @Schema(description = "是否继承历史阶梯量，默认 false")
    private Boolean inheritHistoryPower;
}
