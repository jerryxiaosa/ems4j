package info.zhihui.ems.web.space.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * 空间查询条件 VO
 */
@Data
@Schema(name = "SpaceQueryVo", description = "空间查询条件")
public class SpaceQueryVo {

    @Schema(description = "空间ID集合")
    private Set<Integer> ids;

    @Schema(description = "父空间ID")
    private Integer pid;

    @Size(max = 100)
    @Schema(description = "空间名称，模糊查询条件")
    private String name;

    @Schema(description = "空间类型编码集合，参考 spaceType")
    private List<Integer> type;
}
