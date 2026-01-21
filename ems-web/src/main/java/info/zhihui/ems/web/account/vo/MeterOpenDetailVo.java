package info.zhihui.ems.web.account.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 开户表计明细 VO
 */
@Data
@Accessors(chain = true)
@Schema(name = "MeterOpenDetailVo", description = "开户表计明细")
public class MeterOpenDetailVo {

    @NotNull
    @Schema(description = "表ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer meterId;

}
