package info.zhihui.ems.web.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 角色创建 VO
 */
@Data
@Accessors(chain = true)
@Schema(name = "RoleCreateVo", description = "角色创建数据")
public class RoleCreateVo {

    @NotBlank
    @Size(max = 64)
    @Schema(description = "角色名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String roleName;

    @NotBlank
    @Size(max = 64)
    @Schema(description = "角色唯一标识", requiredMode = Schema.RequiredMode.REQUIRED)
    private String roleKey;

    @Schema(description = "显示排序号")
    private Integer sortNum;

    @Size(max = 255)
    @Schema(description = "角色备注")
    private String remark;

    @Schema(description = "是否系统内置 0否 1是")
    private Boolean isSystem;

    @Schema(description = "是否禁用 0否 1是")
    private Boolean isDisabled;
}