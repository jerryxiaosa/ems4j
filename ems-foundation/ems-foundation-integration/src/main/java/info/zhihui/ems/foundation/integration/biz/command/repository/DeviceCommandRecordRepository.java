package info.zhihui.ems.foundation.integration.biz.command.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import info.zhihui.ems.foundation.integration.biz.command.entity.DeviceCommandRecordEntity;
import info.zhihui.ems.foundation.integration.biz.command.qo.DeviceCommandCancelQo;
import info.zhihui.ems.foundation.integration.biz.command.qo.DeviceCommandQo;
import info.zhihui.ems.foundation.integration.biz.command.qo.DeviceCommandRetryQo;
import info.zhihui.ems.foundation.integration.biz.command.qo.DeviceCommandStartExecuteQo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author jerryxiaosa
 */
@Repository
public interface DeviceCommandRecordRepository extends BaseMapper<DeviceCommandRecordEntity> {
    List<DeviceCommandRecordEntity> findList(DeviceCommandQo query);

    List<DeviceCommandRecordEntity> findAutoRetryList(DeviceCommandRetryQo query);

    void cancelDeviceCommand(DeviceCommandCancelQo qo);

    int startExecute(DeviceCommandStartExecuteQo qo);

    int recoverTimeoutRunningCommands(@Param("timeoutTime") LocalDateTime timeoutTime);

    void updateCommandExecuteInfo(DeviceCommandRecordEntity entity);
}
