package info.zhihui.ems.iot.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.iot.infrastructure.persistence.entity.DeviceEntity;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceRepository extends BaseMapper<DeviceEntity> {

    DeviceEntity getByDeviceNo(@Param("deviceNo") String deviceNo) throws NotFoundException;

    DeviceEntity getByParentIdAndPortNoAndMeterAddress(@Param("parentId") Integer parentId,
                                                       @Param("portNo") Integer portNo,
                                                       @Param("meterAddress") Integer meterAddress) throws NotFoundException;

}
