package info.zhihui.ems.business.plan.mapper;

import info.zhihui.ems.business.plan.bo.WarnPlanBo;
import info.zhihui.ems.business.plan.dto.WarnPlanQueryDto;
import info.zhihui.ems.business.plan.dto.WarnPlanSaveDto;
import info.zhihui.ems.business.plan.entity.WarnPlanEntity;
import info.zhihui.ems.business.plan.qo.WarnPlanQo;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WarningPlanMapper {

    WarnPlanEntity saveDtoToEntity(WarnPlanSaveDto dto);

    WarnPlanBo detailEntityToBo(WarnPlanEntity entity);

    List<WarnPlanBo> listEntityToBo(List<WarnPlanEntity> entityList);

    WarnPlanQo queryDtoToQo(WarnPlanQueryDto query);
}
