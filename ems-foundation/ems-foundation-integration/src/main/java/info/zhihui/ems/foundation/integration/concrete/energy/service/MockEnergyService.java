package info.zhihui.ems.foundation.integration.concrete.energy.service;

import info.zhihui.ems.common.model.energy.DailyEnergySlot;
import info.zhihui.ems.common.model.energy.DatePlanItem;
import info.zhihui.ems.foundation.integration.core.enums.ModuleEnum;
import info.zhihui.ems.foundation.integration.concrete.energy.dto.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author jerryxiaosa
 */
@Service
public class MockEnergyService implements EnergyService {
    @Override
    public String addDevice(ElectricDeviceAddDto addDto) {
        return "1";
    }

    @Override
    public void editDevice(ElectricDeviceUpdateDto updateDto) {
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
    public void setDuration(DailyEnergyPlanUpdateDto durationUpdateDto) {

    }

    @Override
    public List<DailyEnergySlot> getDuration(DailyEnergyPlanQueryDto durationQueryDto) {
        return List.of();
    }

    @Override
    public void setDateDuration(DateEnergyPlanUpdateDto dateDurationUpdateDto) {

    }

    @Override
    public List<DatePlanItem> getDateDuration(BaseElectricDeviceDto deviceDto) {
        return List.of();
    }

    @Override
    public BigDecimal getMeterEnergy(ElectricDeviceDegreeDto degreeDto) {
        return new BigDecimal("100");
    }

    @Override
    public Boolean isOnline(BaseElectricDeviceDto deviceDto) {
        return true;
    }

    @Override
    public void setElectricCt(ElectricDeviceCTDto ctDto) {

    }

    @Override
    public Integer getElectricCt(BaseElectricDeviceDto deviceDto) {
        return 1;
    }

    @Override
    public ModuleEnum getModuleName() {
        return ModuleEnum.ENERGY;
    }
}
