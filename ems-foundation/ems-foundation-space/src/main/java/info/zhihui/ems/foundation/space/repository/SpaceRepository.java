package info.zhihui.ems.foundation.space.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import info.zhihui.ems.foundation.space.entity.SpaceEntity;
import info.zhihui.ems.foundation.space.qo.SpaceQueryQo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 空间数据访问接口
 *
 * @author jerryxiaosa
 */
@Repository
public interface SpaceRepository extends BaseMapper<SpaceEntity> {

    /**
     * 根据查询条件查询空间列表
     */
    List<SpaceEntity> selectByQo(@Param("qo") SpaceQueryQo qo);

    /**
     * 统计同一父节点下指定名称的空间数量
     */
    int countByParentAndName(@Param("pid") Integer pid, @Param("name") String name);

    /**
     * 统计指定父节点下未删除的子空间数量
     */
    int countChildrenByPid(@Param("pid") Integer pid);

    /**
     * 批量更新全路径
     */
    void updateFullPathBatch(@Param("entities") List<SpaceEntity> entities);
}