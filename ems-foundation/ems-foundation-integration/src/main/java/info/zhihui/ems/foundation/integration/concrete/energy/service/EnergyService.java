package info.zhihui.ems.foundation.integration.concrete.energy.service;


import info.zhihui.ems.foundation.integration.core.service.CommonDeviceModule;
import info.zhihui.ems.foundation.integration.concrete.energy.dto.*;
import info.zhihui.ems.common.model.energy.DailyEnergySlot;
import info.zhihui.ems.common.model.energy.DatePlanItem;

import java.math.BigDecimal;
import java.util.List;

public interface EnergyService extends CommonDeviceModule {
    /**
     * 增加设备
     *
     * @return iotId
     */
    String addDevice(ElectricDeviceAddDto addDto);

    /**
     * 修改设备
     *
     * @param updateDto 修改信息
     */
    void editDevice(ElectricDeviceUpdateDto updateDto);

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
    void setDuration(DailyEnergyPlanUpdateDto durationUpdateDto);

    /**
     * 读取电表尖峰平谷时间段
     */
    List<DailyEnergySlot> getDuration(DailyEnergyPlanQueryDto durationQueryDto);

    /**
     * 下发电表日期电价方案
     */
    void setDateDuration(DateEnergyPlanUpdateDto dateDurationUpdateDto);

    /**
     * 读取电表日期电价方案
     */
    List<DatePlanItem> getDateDuration(BaseElectricDeviceDto deviceDto);

    /**
     * 获取电表用电量（总尖峰平谷深谷）
     */
    BigDecimal getMeterEnergy(ElectricDeviceDegreeDto degreeDto);

    /**
     * 设备是否在线，是返回true
     */
    Boolean isOnline(BaseElectricDeviceDto deviceDto);

    /**
     * 设置电表CT
     * @param ctDto ct变比参数
     */
    void setElectricCt(ElectricDeviceCTDto ctDto);

    /**
     * 获取电表CT
     */
    Integer getElectricCt(BaseElectricDeviceDto deviceDto);

}
