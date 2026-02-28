package info.zhihui.ems.business.device.service.impl.sync;

import info.zhihui.ems.business.device.bo.ElectricMeterBo;
import info.zhihui.ems.business.device.entity.ElectricMeterEntity;
import info.zhihui.ems.business.device.repository.ElectricMeterRepository;
import info.zhihui.ems.foundation.integration.core.service.DeviceModuleContext;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 电表在线状态同步器。
 */
@Component
public class ElectricMeterOnlineStatusSynchronizer extends BaseOnlineStatusSyncService<ElectricMeterBo> {

    private final ElectricMeterRepository electricMeterRepository;

    public ElectricMeterOnlineStatusSynchronizer(DeviceModuleContext deviceModuleContext,
                                                 ElectricMeterRepository electricMeterRepository) {
        super(deviceModuleContext);
        this.electricMeterRepository = electricMeterRepository;
    }

    @Override
    protected Integer getOwnAreaId(ElectricMeterBo device) {
        return device.getOwnAreaId();
    }

    @Override
    protected String getIotId(ElectricMeterBo device) {
        return device.getIotId();
    }

    @Override
    protected Boolean getCurrentStatus(ElectricMeterBo device) {
        return device.getIsOnline();
    }

    @Override
    protected void persistStatus(ElectricMeterBo device, Boolean status) {
        ElectricMeterEntity updateEntity = new ElectricMeterEntity()
                .setId(device.getId())
                .setIsOnline(status);
        // 如果是离线则不更新最近在线时间
        if (Boolean.TRUE.equals(status)) {
            updateEntity.setLastOnlineTime(LocalDateTime.now());
        }
        electricMeterRepository.updateById(updateEntity);
    }
}
