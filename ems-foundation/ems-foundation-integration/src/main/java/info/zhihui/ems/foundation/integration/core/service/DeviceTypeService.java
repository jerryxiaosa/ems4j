package info.zhihui.ems.foundation.integration.core.service;

import info.zhihui.ems.foundation.integration.core.bo.DeviceTypeBo;
import info.zhihui.ems.foundation.integration.core.dto.DeviceTypeQueryDto;
import info.zhihui.ems.foundation.integration.core.dto.DeviceTypeSaveDto;

import java.util.List;

/**
 * 设备类型服务接口。
 */
public interface DeviceTypeService {

    /**
     * 查询设备类型列表。
     *
     * @param query 查询条件
     * @return 设备类型列表
     */
    List<DeviceTypeBo> findList(DeviceTypeQueryDto query);

    /**
     * 根据主键查询设备类型详情。
     *
     * @param id 设备类型主键
     * @return 设备类型详情
     */
    DeviceTypeBo getDetail(Integer id);

    /**
     * 根据类型标识查询设备类型。
     *
     * @param typeKey 类型标识
     * @return 设备类型
     */
    DeviceTypeBo getByKey(String typeKey);

    /**
     * 新增设备类型。
     *
     * @param updateBo 设备类型保存参数
     */
    void add(DeviceTypeSaveDto updateBo);

    /**
     * 更新设备类型。
     *
     * @param updateBo 设备类型保存参数
     */
    void update(DeviceTypeSaveDto updateBo);

    /**
     * 删除设备类型。
     *
     * @param id 设备类型主键
     */
    void delete(Integer id);
}
