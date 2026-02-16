package info.zhihui.ems.business.account.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import info.zhihui.ems.business.account.entity.AccountSpaceRelEntity;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * 账户空间租赁关系仓储
 */
@Repository
public interface AccountSpaceRelRepository extends BaseMapper<AccountSpaceRelEntity> {

    /**
     * 根据空间ID集合查询租赁关系
     */
    List<AccountSpaceRelEntity> findListBySpaceIds(@Param("spaceIds") Collection<Integer> spaceIds);

    /**
     * 根据账户ID集合查询租赁关系
     */
    List<AccountSpaceRelEntity> findListByAccountIds(@Param("accountIds") Collection<Integer> accountIds);

    /**
     * 根据账户与空间ID集合查询租赁关系
     */
    List<AccountSpaceRelEntity> findListByAccountIdAndSpaceIds(@Param("accountId") Integer accountId,
                                                               @Param("spaceIds") Collection<Integer> spaceIds);

    /**
     * 按账户与空间ID集合批量删除租赁关系
     */
    int deleteByAccountIdAndSpaceIds(@Param("accountId") Integer accountId,
                                     @Param("spaceIds") Collection<Integer> spaceIds);
}
