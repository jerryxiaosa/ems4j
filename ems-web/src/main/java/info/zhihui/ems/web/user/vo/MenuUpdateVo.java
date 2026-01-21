package info.zhihui.ems.web.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 菜单更新 VO
 */
@Data
@Accessors(chain = true)
@Schema(name = "MenuUpdateVo", description = "菜单更新信息")
public class MenuUpdateVo {

    @Schema(description = "菜单名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "菜单名称不能为空")
    @Size(max = 64, message = "菜单名称长度不能超过64个字符")
    private String menuName;

    @Schema(description = "菜单唯一标识", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "菜单唯一标识不能为空")
    @Size(max = 64, message = "菜单唯一标识长度不能超过64个字符")
    private String menuKey;

    @Schema(description = "菜单排序号")
    private Integer sortNum;

    @Schema(description = "路由路径")
    @Size(max = 128, message = "路由路径长度不能超过128个字符")
    private String path;

    @Schema(description = "菜单来源：1 web, 2 mobile 参见menuSource", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "菜单来源不能为空")
    private Integer menuSource;

    @Schema(description = "菜单类型：1 菜单, 2 按钮 参见menuType", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "菜单类型不能为空")
    private Integer menuType;

    @Schema(description = "菜单图标")
    @Size(max = 64, message = "菜单图标长度不能超过64个字符")
    private String icon;

    @Schema(description = "菜单备注")
    @Size(max = 255, message = "菜单备注长度不能超过255个字符")
    private String remark;

    @Schema(description = "是否隐藏")
    private Boolean hidden;

    @Schema(description = "接口权限标识列表")
    private List<String> permissionCodes;
}