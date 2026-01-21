package info.zhihui.ems.foundation.integration.core.service;

import info.zhihui.ems.foundation.integration.core.bo.DeviceTypeBo;
import info.zhihui.ems.foundation.integration.core.dto.DeviceTypeQueryDto;
import info.zhihui.ems.foundation.integration.core.dto.DeviceTypeSaveDto;

import java.util.List;

public interface DeviceTypeService {

    List<DeviceTypeBo> findList(DeviceTypeQueryDto query);

    DeviceTypeBo getDetail(Integer id);

    DeviceTypeBo getByKey(String typeKey);

    void add(DeviceTypeSaveDto updateBo);

    void update(DeviceTypeSaveDto updateBo);

    void delete(Integer id);
}
