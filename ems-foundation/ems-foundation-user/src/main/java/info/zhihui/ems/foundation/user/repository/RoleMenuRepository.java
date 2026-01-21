package info.zhihui.ems.foundation.user.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import info.zhihui.ems.foundation.user.entity.RoleMenuEntity;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 角色菜单关联Repository接口
 */
@Repository
public interface RoleMenuRepository extends BaseMapper<RoleMenuEntity> {

    /**
     * 根据角色ID集合查询菜单ID列表
     */
    List<Integer> selectMenuIdsByRoleIds(@Param("roleIds") List<Integer> roleIds);

    /**
     * 根据角色ID删除关联关系
     */
    int deleteByRoleId(@Param("roleId") Integer roleId);

}
