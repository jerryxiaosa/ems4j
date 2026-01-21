package info.zhihui.ems.foundation.integration.core.service.testdata;

import info.zhihui.ems.foundation.integration.core.enums.ModuleEnum;

/**
 * @author jerryxiaosa
 */
public class TestEnergy1Impl implements TestEnergy1 {

    @Override
    public ModuleEnum getModuleName() {
        return ModuleEnum.ENERGY;
    }
}
