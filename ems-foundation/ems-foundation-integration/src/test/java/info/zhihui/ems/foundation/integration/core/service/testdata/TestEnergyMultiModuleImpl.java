package info.zhihui.ems.foundation.integration.core.service.testdata;

import info.zhihui.ems.foundation.integration.core.enums.ModuleEnum;

/**
 * 实现多个模块接口（应触发异常）
 */
public class TestEnergyMultiModuleImpl implements TestEnergy1, TestEnergy2Service {

    @Override
    public ModuleEnum getModuleName() {
        return ModuleEnum.ENERGY;
    }
}
