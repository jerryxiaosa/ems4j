package info.zhihui.ems.web.owner.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 主体账户状态查询条件
 */
@Data
@Accessors(chain = true)
@Schema(name = "OwnerAccountStatusQueryVo", description = "主体账户状态查询条件")
public class OwnerAccountStatusQueryVo {

    @NotNull(message = "主体类型不能为空")
    @Schema(description = "主体类型（0企业，1个人）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer ownerType;

    @NotNull(message = "主体ID不能为空")
    @Schema(description = "主体ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer ownerId;
}

