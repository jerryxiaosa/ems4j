package info.zhihui.ems.foundation.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 菜单移动DTO
 */
@Data
@Accessors(chain = true)
public class MenuMoveDto {

    /**
     * 要移动的菜单ID
     */
    @NotNull
    private Integer menuId;

    /**
     * 目标父级菜单ID（0表示顶级）
     */
    @NotNull
    private Integer targetPid;

    /**
     * 目标排序号
     */
    private Integer targetSortNum;
}