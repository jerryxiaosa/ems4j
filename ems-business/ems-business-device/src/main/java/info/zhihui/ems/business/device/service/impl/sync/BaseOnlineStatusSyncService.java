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

    /**
     * 构造在线状态同步模板。
     *
     * @param deviceModuleContext 设备模块上下文，用于按区域获取对应的 IoT 服务
     */
    protected BaseOnlineStatusSyncService(DeviceModuleContext deviceModuleContext) {
        this.deviceModuleContext = deviceModuleContext;
    }

    /**
     * 同步设备在线状态。
     *
     * @param device 设备对象
     * @param request 同步请求
     */
    @Override
    public void syncOnlineStatus(T device, DeviceStatusSyncRequestDto request) {
        Boolean targetStatus = determineTargetStatus(device, request);
        if (!shouldPersist(device, targetStatus)) {
            return;
        }
        persistStatus(device, targetStatus);
    }

    /**
     * 计算本次同步的目标在线状态。
     * 优先使用强制同步参数，否则从 IoT 平台查询。
     *
     * @param device 设备对象
     * @param request 同步请求
     * @return 目标在线状态
     */
    private Boolean determineTargetStatus(T device, DeviceStatusSyncRequestDto request) {
        // 有强制值，就用强制值
        if (request != null && Boolean.TRUE.equals(request.getForce()) && request.getOnlineStatus() != null) {
            return request.getOnlineStatus();
        }
        return queryStatusFromIot(device);
    }

    /**
     * 从 IoT 平台查询设备在线状态。
     *
     * @param device 设备对象
     * @return 在线状态；当缺少 IoT 标识或所属区域时返回 {@code false}
     */
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
     * 判断是否需要持久化状态。
     */
    protected boolean shouldPersist(T device, Boolean targetStatus) {
        return Boolean.TRUE.equals(targetStatus) || !Objects.equals(getCurrentStatus(device), targetStatus);
    }

    /**
     * 状态持久化。
     */
    protected abstract void persistStatus(T device, Boolean status);
}
