package info.zhihui.ems.foundation.integration.core.service;

import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.foundation.integration.core.bo.DeviceModelBo;
import info.zhihui.ems.foundation.integration.core.dto.DeviceModelQueryDto;
import jakarta.validation.constraints.NotNull;

import java.util.List;

// 需要对接设备，手动增加
public interface DeviceModelService {

    PageResult<DeviceModelBo> findPage(@NotNull DeviceModelQueryDto query, @NotNull PageParam pageParam);

    List<DeviceModelBo> findList(@NotNull DeviceModelQueryDto query);

    DeviceModelBo getDetail(@NotNull Integer id);
}
