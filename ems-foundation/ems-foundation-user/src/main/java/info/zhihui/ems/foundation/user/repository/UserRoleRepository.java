package info.zhihui.ems.foundation.user.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import info.zhihui.ems.foundation.user.entity.UserRoleEntity;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户角色关联Repository接口
 */
@Repository
public interface UserRoleRepository extends BaseMapper<UserRoleEntity> {

    /**
     * 根据用户ID查询用户角色关联列表
     *
     * @param userId 用户ID
     * @return 用户角色关联列表
     */
    List<UserRoleEntity> selectByUserId(@Param("userId") Integer userId);

    /**
     * 根据用户ID列表查询用户角色关联列表
     *
     * @param userIds 用户ID列表
     * @return 用户角色关联列表
     */
    List<UserRoleEntity> selectByUserIds(@Param("userIds") List<Integer> userIds);

    /**
     * 根据用户ID删除用户角色关联
     *
     * @param userId 用户ID
     * @return 删除的记录数
     */
    int deleteByUserId(@Param("userId") Integer userId);
}