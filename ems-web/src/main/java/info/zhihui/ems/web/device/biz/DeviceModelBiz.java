package info.zhihui.ems.web.device.biz;

import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.foundation.integration.core.bo.DeviceModelBo;
import info.zhihui.ems.foundation.integration.core.dto.DeviceModelQueryDto;
import info.zhihui.ems.foundation.integration.core.service.DeviceModelService;
import info.zhihui.ems.web.device.mapstruct.DeviceModelWebMapper;
import info.zhihui.ems.web.device.vo.DeviceModelQueryVo;
import info.zhihui.ems.web.device.vo.DeviceModelVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 设备型号业务编排。
 */
@Service
@RequiredArgsConstructor
public class DeviceModelBiz {

    private final DeviceModelService deviceModelService;
    private final DeviceModelWebMapper deviceModelWebMapper;

    /**
     * 分页查询设备型号。
     *
     * @param queryVo  查询条件
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 设备型号分页结果
     */
    public PageResult<DeviceModelVo> findDeviceModelPage(DeviceModelQueryVo queryVo, Integer pageNum, Integer pageSize) {
        PageParam pageParam = new PageParam()
                .setPageNum(Objects.requireNonNullElse(pageNum, 1))
                .setPageSize(Objects.requireNonNullElse(pageSize, 10));
        DeviceModelQueryDto queryDto = deviceModelWebMapper.toDeviceModelQueryDto(queryVo);
        if (queryDto == null) {
            queryDto = new DeviceModelQueryDto();
        }

        PageResult<DeviceModelBo> pageResult = deviceModelService.findPage(queryDto, pageParam);
        return deviceModelWebMapper.toDeviceModelVoPage(pageResult);
    }
}
