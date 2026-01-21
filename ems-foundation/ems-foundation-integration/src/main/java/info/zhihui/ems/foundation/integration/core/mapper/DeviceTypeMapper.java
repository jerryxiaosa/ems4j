package info.zhihui.ems.foundation.integration.core.mapper;


import info.zhihui.ems.foundation.integration.core.bo.DeviceTypeBo;
import info.zhihui.ems.foundation.integration.core.dto.DeviceTypeQueryDto;
import info.zhihui.ems.foundation.integration.core.dto.DeviceTypeSaveDto;
import info.zhihui.ems.foundation.integration.core.entity.DeviceTypeEntity;
import info.zhihui.ems.foundation.integration.core.qo.DeviceTypeQueryQo;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DeviceTypeMapper {

    List<DeviceTypeBo> listEntityToBo(List<DeviceTypeEntity> entity);

    DeviceTypeBo entityToBo(DeviceTypeEntity entity);

    DeviceTypeQueryQo queryDtoToQo(DeviceTypeQueryDto dto);

    DeviceTypeEntity saveDtoEntity(DeviceTypeSaveDto saveDto);

}
