package info.zhihui.ems.business.account.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import info.zhihui.ems.business.account.entity.OwnerSpaceRelEntity;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * 主体空间租赁关系仓储
 */
@Repository
public interface OwnerSpaceRelRepository extends BaseMapper<OwnerSpaceRelEntity> {

    /**
     * 根据空间ID集合查询租赁关系
     */
    List<OwnerSpaceRelEntity> findListBySpaceIds(@Param("spaceIds") Collection<Integer> spaceIds);

    /**
     * 根据主体类型与主体ID集合查询租赁关系
     */
    List<OwnerSpaceRelEntity> findListByOwnerTypeAndOwnerIds(@Param("ownerType") Integer ownerType,
                                                             @Param("ownerIds") Collection<Integer> ownerIds);

    /**
     * 根据主体与空间ID集合查询租赁关系
     */
    List<OwnerSpaceRelEntity> findListByOwnerAndSpaceIds(@Param("ownerType") Integer ownerType,
                                                         @Param("ownerId") Integer ownerId,
                                                         @Param("spaceIds") Collection<Integer> spaceIds);

    /**
     * 按主体与空间ID集合批量删除租赁关系
     */
    int deleteByOwnerAndSpaceIds(@Param("ownerType") Integer ownerType,
                                 @Param("ownerId") Integer ownerId,
                                 @Param("spaceIds") Collection<Integer> spaceIds);
}
