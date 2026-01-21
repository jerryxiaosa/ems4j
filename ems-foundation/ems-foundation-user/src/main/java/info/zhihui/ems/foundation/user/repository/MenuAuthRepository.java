package info.zhihui.ems.foundation.user.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import info.zhihui.ems.foundation.user.entity.MenuAuthEntity;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 菜单权限 Repository 接口
 */
@Repository
public interface MenuAuthRepository extends BaseMapper<MenuAuthEntity> {

    /**
     * 根据菜单ID查询权限标识列表
     */
    List<String> selectPermissionCodesByMenuId(@Param("menuId") Integer menuId);

    /**
     * 根据菜单ID删除权限
     */
    int deleteByMenuId(@Param("menuId") Integer menuId);

}
