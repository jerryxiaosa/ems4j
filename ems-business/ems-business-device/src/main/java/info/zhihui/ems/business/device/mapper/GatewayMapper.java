package info.zhihui.ems.business.device.mapper;

import com.github.pagehelper.PageInfo;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.business.device.bo.GatewayBo;
import info.zhihui.ems.business.device.dto.GatewayQueryDto;
import info.zhihui.ems.business.device.dto.GatewayCreateDto;
import info.zhihui.ems.business.device.dto.GatewayUpdateDto;
import info.zhihui.ems.business.device.entity.GatewayEntity;
import info.zhihui.ems.business.device.qo.GatewayQo;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GatewayMapper {

    PageResult<GatewayBo> pageEntityToBo(PageInfo<GatewayEntity> page);

    List<GatewayBo> listEntityToBo(List<GatewayEntity> list);

    GatewayQo queryDtoToQo(GatewayQueryDto dto);

    GatewayEntity createDtoToEntity(GatewayCreateDto dto);

    GatewayEntity updateDtoToEntity(GatewayUpdateDto dto);

    GatewayBo entityToBo(GatewayEntity entity);

}
