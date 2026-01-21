package info.zhihui.ems.web.device.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 开户电表详情
 */
@Data
@Schema(name = "MeterOpenDetailVo", description = "电表开户明细")
public class MeterOpenDetailVo {

    @NotNull
    @Schema(description = "电表ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer meterId;

}
