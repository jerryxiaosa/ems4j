package info.zhihui.ems.foundation.system.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import info.zhihui.ems.foundation.system.entity.ConfigEntity;
import info.zhihui.ems.foundation.system.qo.ConfigQueryQo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConfigRepository extends BaseMapper<ConfigEntity> {

    ConfigEntity getByKey(String key);

    List<ConfigEntity> selectByQo(@Param("qo") ConfigQueryQo qo);
}
