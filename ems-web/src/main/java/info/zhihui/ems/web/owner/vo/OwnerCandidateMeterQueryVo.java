package info.zhihui.ems.web.owner.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 主体候选电表查询条件
 */
@Data
@Accessors(chain = true)
@Schema(name = "OwnerCandidateMeterQueryVo", description = "主体候选电表查询条件")
public class OwnerCandidateMeterQueryVo {

    @NotNull(message = "主体类型不能为空")
    @Schema(description = "主体类型（0企业，1个人）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer ownerType;

    @NotNull(message = "主体ID不能为空")
    @Schema(description = "主体ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer ownerId;

    @Size(max = 100)
    @Schema(description = "租赁空间名称模糊查询")
    private String spaceNameLike;
}
