package info.zhihui.ems.web.owner.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 主体空间退租请求
 */
@Data
@Accessors(chain = true)
@Schema(name = "OwnerSpaceUnrentVo", description = "主体空间退租请求")
public class OwnerSpaceUnrentVo {

    @NotNull(message = "主体类型不能为空")
    @Schema(description = "主体类型（0企业，1个人）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer ownerType;

    @NotNull(message = "主体ID不能为空")
    @Schema(description = "主体ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer ownerId;

    @NotEmpty(message = "空间ID列表不能为空")
    @Schema(description = "空间ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<@NotNull(message = "空间ID不能为空") Integer> spaceIds;
}
