package info.zhihui.ems.business.device.service.impl.sync;

import info.zhihui.ems.business.device.entity.GatewayEntity;
import info.zhihui.ems.business.device.repository.GatewayRepository;
import info.zhihui.ems.foundation.integration.core.service.DeviceModuleContext;
import org.springframework.stereotype.Component;

/**
 * 网关在线状态同步器。
 */
@Component
public class GatewayOnlineStatusSynchronizer extends BaseOnlineStatusSyncService<GatewayEntity> {

    private final GatewayRepository gatewayRepository;

    public GatewayOnlineStatusSynchronizer(DeviceModuleContext deviceModuleContext,
                                           GatewayRepository gatewayRepository) {
        super(deviceModuleContext);
        this.gatewayRepository = gatewayRepository;
    }

    @Override
    protected Integer getOwnAreaId(GatewayEntity device) {
        return device.getOwnAreaId();
    }

    @Override
    protected String getIotId(GatewayEntity device) {
        return device.getIotId();
    }

    @Override
    protected Boolean getCurrentStatus(GatewayEntity device) {
        return device.getIsOnline();
    }

    @Override
    protected void persistStatus(GatewayEntity device, Boolean status) {
        GatewayEntity updateEntity = new GatewayEntity()
                .setId(device.getId())
                .setIsOnline(status);
        gatewayRepository.updateById(updateEntity);
    }
}
