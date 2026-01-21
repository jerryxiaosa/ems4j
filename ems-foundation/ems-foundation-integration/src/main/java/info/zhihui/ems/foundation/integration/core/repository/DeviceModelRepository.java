package info.zhihui.ems.foundation.integration.core.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import info.zhihui.ems.foundation.integration.core.entity.DeviceModelEntity;
import info.zhihui.ems.foundation.integration.core.qo.DeviceModelQueryQo;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceModelRepository extends BaseMapper<DeviceModelEntity> {

    List<DeviceModelEntity> findList(DeviceModelQueryQo query);
}