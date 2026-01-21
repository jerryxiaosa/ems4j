package info.zhihui.ems.foundation.integration.concrete.energy.service;


import info.zhihui.ems.foundation.integration.core.service.CommonDeviceModule;
import info.zhihui.ems.foundation.integration.concrete.energy.dto.*;

import java.math.BigDecimal;

public interface EnergyService extends CommonDeviceModule {
    /**
     * 增加设备
     *
     * @return iotId
     */
    Integer addDevice(ElectricDeviceAddDto addDto);

    /**
     * 修改设备
     *
     * @param updateDto 修改信息
     * @return iotId
     */
    Integer editDevice(ElectricDeviceUpdateDto updateDto);

    /**
     * 删除设备
     *
     * @param deleteDto 删除信息
     */
    void delDevice(BaseElectricDeviceDto deleteDto);

    /**
     * 断闸
     *
     */
    void cutOff(BaseElectricDeviceDto cutOffDto);

    /**
     * 合闸
     */
    void recover(BaseElectricDeviceDto recoverDto);

    /**
     * 设置电表尖峰平谷时间段
     */
    void setElectricTime(ElectricPriceTimeUpdateDto timeUpdateDto);

    /**
     * 获取电表用电量（总尖峰平谷深谷）
     */
    BigDecimal getMeterEnergy(ElectricDeviceDegreeDto degreeDto);

    /**
     * 设备是否在线，是返回true
     */
    Boolean isOnline(BaseElectricDeviceDto deviceDto);

    /**
     * 设备是否断闸
     */
    Boolean isCutOff(BaseElectricDeviceDto deviceDto);

    /**
     * 设置电表CT
     * @param ctDto ct变比参数
     */
    void setElectricCt(ElectricDeviceCTDto ctDto);

}
