package info.zhihui.ems.foundation.user.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import info.zhihui.ems.foundation.user.entity.MenuEntity;
import info.zhihui.ems.foundation.user.qo.MenuQueryQo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 菜单Repository接口
 */
@Repository
public interface MenuRepository extends BaseMapper<MenuEntity> {

    /**
     * 根据查询条件查询菜单列表
     */
    List<MenuEntity> selectByQo(@Param("qo") MenuQueryQo qo);

}