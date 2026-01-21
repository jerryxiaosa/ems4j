package info.zhihui.ems.foundation.integration.biz.command.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import info.zhihui.ems.foundation.integration.biz.command.entity.DeviceCommandExecuteRecordEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author jerryxiaosa
 */
@Repository
public interface DeviceCommandExecuteRecordRepository extends BaseMapper<DeviceCommandExecuteRecordEntity> {
    List<DeviceCommandExecuteRecordEntity> findList(Integer commandId);
}
