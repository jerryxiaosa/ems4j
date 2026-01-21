package info.zhihui.ems.foundation.integration.concrete.energy.service.impl;

import info.zhihui.ems.foundation.integration.core.enums.ModuleEnum;
import info.zhihui.ems.foundation.integration.concrete.energy.dto.*;
import info.zhihui.ems.foundation.integration.concrete.energy.service.EnergyService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * @author jerryxiaosa
 */
@Service
public class DefaultEnergyServiceImpl implements EnergyService {
    @Override
    public Integer addDevice(ElectricDeviceAddDto addDto) {
        return 1;
    }

    @Override
    public Integer editDevice(ElectricDeviceUpdateDto updateDto) {
        return 1;
    }

    @Override
    public void delDevice(BaseElectricDeviceDto deleteDto) {

    }

    @Override
    public void cutOff(BaseElectricDeviceDto cutOffDto) {

    }

    @Override
    public void recover(BaseElectricDeviceDto recoverDto) {

    }

    @Override
    public void setElectricTime(ElectricPriceTimeUpdateDto timeUpdateDto) {

    }

    @Override
    public BigDecimal getMeterEnergy(ElectricDeviceDegreeDto degreeDto) {
        return new BigDecimal("100");
    }

    @Override
    public Boolean isOnline(BaseElectricDeviceDto deviceDto) {
        return null;
    }

    @Override
    public Boolean isCutOff(BaseElectricDeviceDto deviceDto) {
        return null;
    }

    @Override
    public void setElectricCt(ElectricDeviceCTDto ctDto) {

    }

    @Override
    public ModuleEnum getModuleName() {
        return ModuleEnum.ENERGY;
    }
}
