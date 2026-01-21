package info.zhihui.ems.foundation.integration.core.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 模块区域配置信息
 *
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
public class DeviceModuleAreaConfigBo {
    private Integer areaId;
    private List<DeviceModuleConfigBo> deviceConfigList;
}
