package info.zhihui.ems.foundation.space.bo;

import info.zhihui.ems.foundation.space.enums.SpaceTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
public class SpaceBo {
    private Integer id;

    /**
     * 空间名称
     */
    private String name;

    /**
     * 父空间ID
     */
    private Integer pid;

    /**
     * 包括自身的全路径
     */
    private String fullPath;

    /**
     * 空间类型
     */
    private SpaceTypeEnum type;

    /**
     * 父空间ID列表
     */
    private List<Integer> parentsIds;

    /**
     * 父空间名称列表
     */
    private List<String> parentsNames;

    /**
     * 面积大小
     */
    private BigDecimal area;

    /**
     * 排序索引
     */
    private Integer sortIndex;

    /**
     * 所属区域
     */
    private Integer ownAreaId;
}
