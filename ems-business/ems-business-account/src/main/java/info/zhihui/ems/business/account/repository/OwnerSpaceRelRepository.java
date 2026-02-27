package info.zhihui.ems.business.account.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import info.zhihui.ems.business.account.entity.OwnerSpaceRelEntity;
import info.zhihui.ems.business.account.qo.OwnerSpaceRelQueryQo;
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
     * 根据主体类型集合与主体ID集合查询租赁关系
     */
    List<OwnerSpaceRelEntity> findListByOwnerTypesAndOwnerIds(@Param("ownerTypes") Collection<Integer> ownerTypes,
                                                              @Param("ownerIds") Collection<Integer> ownerIds);

    /**
     * 根据主体与空间ID集合查询租赁关系。
     *
     * <p>当 ownerType、ownerId 同时为空时，仅按 spaceIds 查询。
     * 当 ownerType、ownerId 同时非空时，按 owner + spaceIds 查询。</p>
     */
    List<OwnerSpaceRelEntity> findListByOwnerAndSpaceIds(OwnerSpaceRelQueryQo queryQo);

    /**
     * 按主体与空间ID集合批量删除租赁关系
     */
    int deleteByOwnerAndSpaceIds(@Param("ownerType") Integer ownerType,
                                 @Param("ownerId") Integer ownerId,
                                 @Param("spaceIds") Collection<Integer> spaceIds);
}
