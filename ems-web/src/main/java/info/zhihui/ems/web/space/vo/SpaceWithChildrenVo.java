package info.zhihui.ems.web.space.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;

/**
 * 空间信息 VO
 */
@Data
@Accessors(chain = true)
@Schema(name = "SpaceVo", description = "空间信息")
public class SpaceWithChildrenVo {

    @Schema(description = "空间ID")
    private Integer id;

    @Schema(description = "空间名称")
    private String name;

    @Schema(description = "父空间ID")
    private Integer pid;

    @Schema(description = "空间全路径，逗号分隔")
    private String fullPath;

    @Schema(description = "空间类型编码，参考 spaceType")
    private Integer type;

    @Schema(description = "空间面积")
    private BigDecimal area;

    @Schema(description = "排序索引")
    private Integer sortIndex;

    @Schema(description = "所属区域ID")
    private Integer ownAreaId;

    @Schema(description = "父空间ID集合")
    private List<Integer> parentsIds;

    @Schema(description = "父空间名称集合")
    private List<String> parentsNames;

    @Schema(description = "子空间列表，仅在树形查询中返回")
    private List<SpaceWithChildrenVo> children;
}
