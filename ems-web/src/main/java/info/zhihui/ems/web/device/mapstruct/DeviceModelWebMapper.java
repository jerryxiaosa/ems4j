package info.zhihui.ems.web.device.mapstruct;

import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.foundation.integration.core.bo.DeviceModelBo;
import info.zhihui.ems.foundation.integration.core.dto.DeviceModelQueryDto;
import info.zhihui.ems.web.device.vo.DeviceModelQueryVo;
import info.zhihui.ems.web.device.vo.DeviceModelVo;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.Collections;
import java.util.List;

/**
 * 设备型号 Web 层对象转换器。
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DeviceModelWebMapper {

    DeviceModelQueryDto toDeviceModelQueryDto(DeviceModelQueryVo queryVo);

    DeviceModelVo toDeviceModelVo(DeviceModelBo deviceModelBo);

    List<DeviceModelVo> toDeviceModelVoList(List<DeviceModelBo> deviceModelBoList);

    default PageResult<DeviceModelVo> toDeviceModelVoPage(PageResult<DeviceModelBo> pageResult) {
        if (pageResult == null) {
            return new PageResult<DeviceModelVo>()
                    .setPageNum(0)
                    .setPageSize(0)
                    .setTotal(0L)
                    .setList(Collections.emptyList());
        }

        List<DeviceModelVo> deviceModelVoList = toDeviceModelVoList(pageResult.getList());
        return new PageResult<DeviceModelVo>()
                .setPageNum(pageResult.getPageNum())
                .setPageSize(pageResult.getPageSize())
                .setTotal(pageResult.getTotal())
                .setList(deviceModelVoList == null ? Collections.emptyList() : deviceModelVoList);
    }
}
