package info.zhihui.ems.foundation.integration.core.service.testdata;

import info.zhihui.ems.foundation.integration.core.enums.ModuleEnum;

/**
 * @author jerryxiaosa
 */
public class TestEnergy2ServiceImpl implements TestEnergy2Service {
    @Override
    public ModuleEnum getModuleName() {
        return ModuleEnum.ENERGY;
    }
}
