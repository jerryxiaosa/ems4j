package info.zhihui.ems.business.device.service.impl.sync;

import info.zhihui.ems.business.device.service.DeviceStatusSynchronizer;
import info.zhihui.ems.foundation.integration.concrete.energy.dto.BaseElectricDeviceDto;
import info.zhihui.ems.foundation.integration.concrete.energy.service.EnergyService;
import info.zhihui.ems.foundation.integration.core.service.DeviceModuleContext;

import info.zhihui.ems.business.device.dto.DeviceStatusSyncRequestDto;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * 在线状态同步模板，实现通用的查询与持久化流程。
 *
 * @param <T> 设备类型
 */
public abstract class BaseOnlineStatusSyncService<T> implements DeviceStatusSynchronizer<T> {

    private final DeviceModuleContext deviceModuleContext;

    protected BaseOnlineStatusSyncService(DeviceModuleContext deviceModuleContext) {
        this.deviceModuleContext = deviceModuleContext;
    }

    @Override
    public void syncOnlineStatus(T device, DeviceStatusSyncRequestDto request) {
        Boolean targetStatus = determineTargetStatus(device, request);
        if (Objects.equals(getCurrentStatus(device), targetStatus)) {
            return;
        }
        persistStatus(device, targetStatus);
    }

    private Boolean determineTargetStatus(T device, DeviceStatusSyncRequestDto request) {
        if (request != null && Boolean.TRUE.equals(request.getForce()) && request.getOnlineStatus() != null) {
            return request.getOnlineStatus();
        }
        return queryStatusFromIot(device);
    }

    private Boolean queryStatusFromIot(T device) {
        Integer ownAreaId = getOwnAreaId(device);
        String iotId = getIotId(device);
        if (StringUtils.isBlank(iotId) || ownAreaId == null) {
            return Boolean.FALSE;
        }
        EnergyService energyService = deviceModuleContext.getService(EnergyService.class, ownAreaId);
        return energyService.isOnline(new BaseElectricDeviceDto()
                .setDeviceId(iotId)
                .setAreaId(ownAreaId));
    }

    /**
     * 获取设备所属区域。
     */
    protected abstract Integer getOwnAreaId(T device);

    /**
     * 获取设备 IOT ID。
     */
    protected abstract String getIotId(T device);

    /**
     * 获取当前状态。
     */
    protected abstract Boolean getCurrentStatus(T device);

    /**
     * 状态持久化。
     */
    protected abstract void persistStatus(T device, Boolean status);
}
