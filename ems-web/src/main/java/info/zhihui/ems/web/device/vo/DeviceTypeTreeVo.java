package info.zhihui.ems.web.device.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 设备品类树节点。
 */
@Data
@Accessors(chain = true)
public class DeviceTypeTreeVo {

    private Integer id;
    private Integer pid;
    private String typeName;
    private String typeKey;
    private Integer level;
    private List<DeviceTypeTreeVo> children;
}
