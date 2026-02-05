package info.zhihui.ems.foundation.integration.core.service.testdata;

import info.zhihui.ems.foundation.integration.core.enums.ModuleEnum;

/**
 * 实现模块接口 + 额外接口（不应触发异常）
 */
public class TestEnergy1ExtraInterfaceImpl implements TestEnergy1, ExtraCapability {

    @Override
    public ModuleEnum getModuleName() {
        return ModuleEnum.ENERGY;
    }
}
