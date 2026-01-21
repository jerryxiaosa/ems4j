package info.zhihui.ems.foundation.user.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import info.zhihui.ems.foundation.user.entity.MenuPathEntity;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * 菜单路径闭包表Repository接口
 */
@Repository
public interface MenuPathRepository extends BaseMapper<MenuPathEntity> {

    /**
     * 从父级继承闭包路径
     */
    int insertPathsFromParent(@Param("menuId") Integer menuId, @Param("pid") Integer pid);

    /**
     * 删除菜单子树对应的闭包记录
     */
    int deleteSubtreePaths(@Param("menuId") Integer menuId);

}
