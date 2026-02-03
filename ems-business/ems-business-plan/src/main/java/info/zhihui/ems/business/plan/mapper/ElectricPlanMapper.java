package info.zhihui.ems.business.plan.mapper;


import info.zhihui.ems.business.plan.bo.ElectricPricePlanBo;
import info.zhihui.ems.business.plan.bo.ElectricPricePlanDetailBo;
import info.zhihui.ems.business.plan.dto.ElectricPricePlanQueryDto;
import info.zhihui.ems.business.plan.dto.ElectricPricePlanSaveDto;
import info.zhihui.ems.business.plan.entity.ElectricPricePlanEntity;
import info.zhihui.ems.business.plan.qo.ElectricPricePlanQo;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ElectricPlanMapper {
    ElectricPricePlanQo queryDtoToQo(ElectricPricePlanQueryDto dto);

    ElectricPricePlanEntity saveDtoToEntity(ElectricPricePlanSaveDto bo);

    ElectricPricePlanDetailBo detailEntityToBo(ElectricPricePlanEntity entity);

    List<ElectricPricePlanBo> listEntityToBo(List<ElectricPricePlanEntity> entityList);
}
