package info.zhihui.ems.foundation.integration.core.service;


import info.zhihui.ems.foundation.integration.core.enums.ModuleEnum;

/**
 * 模块通用信息处理接口
 * 抽取公共的方法放在这里
 * <br/>
 * 每个模块有自己具体的业务接口，具体的业务类需要实现自身模块的接口
 *
 * @author jerryxiaosa
 */
public interface CommonDeviceModule {

    /**
     * 获取模块名称
     *
     * @return 模块名称
     */
    ModuleEnum getModuleName();

}
