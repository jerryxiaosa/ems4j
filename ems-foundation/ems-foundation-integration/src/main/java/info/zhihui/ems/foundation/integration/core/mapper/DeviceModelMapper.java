package info.zhihui.ems.foundation.integration.core.mapper;


import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.pagehelper.PageInfo;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.common.utils.JacksonUtil;
import info.zhihui.ems.foundation.integration.core.bo.DeviceModelBo;
import info.zhihui.ems.foundation.integration.core.dto.DeviceModelQueryDto;
import info.zhihui.ems.foundation.integration.core.entity.DeviceModelEntity;
import info.zhihui.ems.foundation.integration.core.qo.DeviceModelQueryQo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.HashMap;
import java.util.List;

@Mapper(componentModel = "spring")
public interface DeviceModelMapper {

    DeviceModelQueryQo queryDtoToQo(DeviceModelQueryDto dto);

    PageResult<DeviceModelBo> pageEntityToBo(PageInfo<DeviceModelEntity> pageEntity);

    List<DeviceModelBo> listEntityToBo(List<DeviceModelEntity> entity);

    @Mapping(target = "modelProperty", expression = "java(DeviceModelMapper.toModelProperty(entity.getModelProperty()))")
    DeviceModelBo entityToBo(DeviceModelEntity entity);

    static HashMap<String, Object> toModelProperty(String modelPropertyString) {
        if (StrUtil.isBlank(modelPropertyString)) {
            return new HashMap<>();
        }
        return JacksonUtil.fromJson(modelPropertyString, new TypeReference<>() {
        });
    }
}
