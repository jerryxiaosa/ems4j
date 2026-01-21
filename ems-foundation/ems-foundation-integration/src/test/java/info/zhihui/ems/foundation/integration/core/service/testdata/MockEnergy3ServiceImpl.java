package info.zhihui.ems.foundation.integration.core.service.testdata;

import info.zhihui.ems.foundation.integration.core.enums.ModuleEnum;

/**
 * @author jerryxiaosa
 */
public class MockEnergy3ServiceImpl implements TestEnergy3Service {

    @Override
    public ModuleEnum getModuleName() {
        return ModuleEnum.ENERGY;
    }
}
