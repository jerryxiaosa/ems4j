package info.zhihui.ems.foundation.user.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import info.zhihui.ems.foundation.user.entity.RoleEntity;
import info.zhihui.ems.foundation.user.qo.RoleQueryQo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 角色Repository接口
 */
@Repository
public interface RoleRepository extends BaseMapper<RoleEntity> {

    /**
     * 根据查询条件查询角色列表
     */
    List<RoleEntity> selectByQo(@Param("qo") RoleQueryQo qo);

    /**
     * 根据角色ID集合查询权限列表
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    List<String> selectPermissionsByRoleId(@Param("roleId") Integer roleId);

    /**
     * 判断角色集合是否拥有指定权限
     *
     * @param roleIds    角色ID集合
     * @param permission 权限标识
     * @return 是否存在该权限
     */
    boolean existsPermission(@Param("roleIds") List<Integer> roleIds, @Param("permission") String permission);

}
