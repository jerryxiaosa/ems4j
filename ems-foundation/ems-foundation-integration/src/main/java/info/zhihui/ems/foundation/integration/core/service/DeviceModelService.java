package info.zhihui.ems.foundation.integration.core.service;

import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.foundation.integration.core.bo.DeviceModelBo;
import info.zhihui.ems.foundation.integration.core.dto.DeviceModelQueryDto;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 设备型号服务接口。
 */
public interface DeviceModelService {

    /**
     * 分页查询设备型号列表。
     *
     * @param query 查询条件
     * @param pageParam 分页参数
     * @return 设备型号分页结果
     */
    PageResult<DeviceModelBo> findPage(@NotNull DeviceModelQueryDto query, @NotNull PageParam pageParam);

    /**
     * 查询设备型号列表。
     *
     * @param query 查询条件
     * @return 设备型号列表
     */
    List<DeviceModelBo> findList(@NotNull DeviceModelQueryDto query);

    /**
     * 根据主键查询设备型号详情。
     *
     * @param id 设备型号主键
     * @return 设备型号详情
     */
    DeviceModelBo getDetail(@NotNull Integer id);
}
