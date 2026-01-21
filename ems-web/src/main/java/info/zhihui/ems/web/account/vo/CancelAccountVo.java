package info.zhihui.ems.web.account.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 销户请求 VO
 */
@Data
@Accessors(chain = true)
@Schema(name = "CancelAccountVo", description = "账户销户请求参数")
public class CancelAccountVo {

    @NotNull
    @Schema(description = "账户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer accountId;

    @Schema(description = "备注")
    private String remark;

    @Valid
    @Schema(description = "销户表计列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<MeterCancelDetailVo> meterList;
}
