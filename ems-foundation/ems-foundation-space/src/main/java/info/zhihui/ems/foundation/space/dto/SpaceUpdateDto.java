package info.zhihui.ems.foundation.space.dto;

import info.zhihui.ems.foundation.space.enums.SpaceTypeEnum;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 空间更新DTO
 *
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
public class SpaceUpdateDto {

    /**
     * 空间ID
     */
    @NotNull(message = "空间ID不能为空")
    private Integer id;

    /**
     * 名称
     */
    @NotBlank(message = "空间名称不能为空")
    private String name;

    /**
     * 父ID
     */
    @NotNull(message = "父ID不能为空")
    private Integer pid;

    /**
     * 空间类型
     */
    @NotNull(message = "空间类型不能为空")
    private SpaceTypeEnum type;

    /**
     * 面积
     */
    @DecimalMin(value = "0", message = "面积不能小于0")
    private BigDecimal area;

    /**
     * 排序索引
     */
    private Integer sortIndex;
}
