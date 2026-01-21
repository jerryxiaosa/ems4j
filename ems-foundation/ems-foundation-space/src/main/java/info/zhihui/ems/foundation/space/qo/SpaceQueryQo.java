package info.zhihui.ems.foundation.space.qo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Collection;
import java.util.List;

/**
 * 空间查询对象
 *
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
public class SpaceQueryQo {

    /**
     * 空间ID集合
     */
    private Collection<Integer> ids;

    /**
     * 父空间ID
     */
    private Integer pid;

    /**
     * 空间名称模糊查询
     */
    private String nameLike;

    /**
     * 空间类型列表（存储枚举的code值）
     */
    private List<Integer> types;

    /**
     * 全路径前缀，用于查询子树
     */
    private String fullPathPrefix;
}