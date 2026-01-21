package info.zhihui.ems.foundation.integration.biz.command.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import info.zhihui.ems.foundation.integration.biz.command.entity.DeviceCommandRecordEntity;
import info.zhihui.ems.foundation.integration.biz.command.qo.DeviceCommandCancelQo;
import info.zhihui.ems.foundation.integration.biz.command.qo.DeviceCommandQo;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author jerryxiaosa
 */
@Repository
public interface DeviceCommandRecordRepository extends BaseMapper<DeviceCommandRecordEntity> {
    List<DeviceCommandRecordEntity> findList(DeviceCommandQo query);

    void cancelDeviceCommand(DeviceCommandCancelQo qo);

    void updateCommandExecuteInfo(DeviceCommandRecordEntity entity);
}
