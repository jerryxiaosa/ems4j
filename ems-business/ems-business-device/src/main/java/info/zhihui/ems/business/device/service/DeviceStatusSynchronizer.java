package info.zhihui.ems.business.device.service;

import info.zhihui.ems.business.device.dto.DeviceStatusSyncRequestDto;

import java.util.function.Supplier;

/**
 * 设备状态同步器抽象接口。
 *
 * @param <T> 设备类型
 */
public interface DeviceStatusSynchronizer<T> {

    /**
     * 同步设备在线状态。
     *
     * @param device 设备对象
     * @param request 请求参数，当为 null 时按 IoT 状态同步
     */
    void syncOnlineStatus(T device, DeviceStatusSyncRequestDto request);

    /**
     * 使用设备提供器同步状态，避免调用方显式加载设备。
     *
     * @param deviceSupplier 设备加载函数
     * @param request 请求参数
     */
    default void syncStatus(Supplier<T> deviceSupplier, DeviceStatusSyncRequestDto request) {
        T device = deviceSupplier.get();
        syncOnlineStatus(device, request);
    }
}
