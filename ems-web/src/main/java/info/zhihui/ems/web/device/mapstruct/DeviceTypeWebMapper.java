package info.zhihui.ems.web.device.mapstruct;

import info.zhihui.ems.foundation.integration.core.bo.DeviceTypeBo;
import info.zhihui.ems.web.device.vo.DeviceTypeTreeVo;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * 设备品类 Web 层对象转换器。
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DeviceTypeWebMapper {

    DeviceTypeTreeVo toDeviceTypeTreeVo(DeviceTypeBo deviceTypeBo);

    List<DeviceTypeTreeVo> toDeviceTypeTreeVoList(List<DeviceTypeBo> deviceTypeBoList);
}
