package info.zhihui.ems.foundation.integration.core.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import info.zhihui.ems.foundation.integration.core.entity.DeviceTypeEntity;
import info.zhihui.ems.foundation.integration.core.qo.DeviceTypeQueryQo;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceTypeRepository extends BaseMapper<DeviceTypeEntity> {

    List<DeviceTypeEntity> findList(DeviceTypeQueryQo query);
}