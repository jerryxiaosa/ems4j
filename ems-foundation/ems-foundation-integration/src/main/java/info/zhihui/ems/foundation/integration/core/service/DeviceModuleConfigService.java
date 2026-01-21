package info.zhihui.ems.foundation.integration.core.service;



import info.zhihui.ems.foundation.integration.core.bo.DeviceModuleConfigBo;

import java.util.List;

/**
 * 模块配置信息接口
 * <br/>
 * 通过参数获取区域下某个模块的配置信息
 *
 * @author jerryxiaosa
 */
public interface DeviceModuleConfigService {

    /**
     * 获取指定 区域下某个模块的设备配置信息
     * 同一个区域下只能配置一个模块的实现
     *
     * @param interfaceType 接口类型
     * @param areaId        区域id
     * @return DeviceConfigBo 指定的园区的设备配置信息
     */
    <T extends CommonDeviceModule> DeviceModuleConfigBo getDeviceConfigByModule(Class<T> interfaceType, Integer areaId);

    /**
     * 获取区域下某个模块的设备配置信息的具体结构值
     *
     * @param interfaceType 接口类型
     * @param returnObject  返回类型
     * @param areaId        区域id
     * @param <T>           模块接口类型
     * @param <E>           配置信息类型
     * @return E 指定的园区的设备配置信息，类型指定
     */
    <T extends CommonDeviceModule, E> E getDeviceConfigValue(Class<T> interfaceType, Class<E> returnObject, Integer areaId);

    /**
     * 设置区域下某个模块的设备配置信息
     *
     * @param deviceModuleConfigBoList 设备配置信息组
     * @param areaId                   区域id
     */
    void setDeviceConfigByArea(List<DeviceModuleConfigBo> deviceModuleConfigBoList, Integer areaId);
}
