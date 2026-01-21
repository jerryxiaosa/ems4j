package info.zhihui.ems.web.plan.mapstruct;

import info.zhihui.ems.business.plan.bo.WarnPlanBo;
import info.zhihui.ems.business.plan.dto.WarnPlanQueryDto;
import info.zhihui.ems.business.plan.dto.WarnPlanSaveDto;
import info.zhihui.ems.web.plan.vo.WarnPlanQueryVo;
import info.zhihui.ems.web.plan.vo.WarnPlanSaveVo;
import info.zhihui.ems.web.plan.vo.WarnPlanVo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.Collections;
import java.util.List;

/**
 * 预警方案 Web 层映射
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WarnPlanWebMapper {

    WarnPlanQueryDto toWarnPlanQueryDto(WarnPlanQueryVo queryVo);

    @Mapping(target = "id", source = "id")
    WarnPlanSaveDto toWarnPlanSaveDto(WarnPlanSaveVo saveVo);

    WarnPlanVo toWarnPlanVo(WarnPlanBo bo);

    List<WarnPlanVo> toWarnPlanVoList(List<WarnPlanBo> bos);

    default List<WarnPlanVo> safeWarnPlanVoList(List<WarnPlanBo> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        List<WarnPlanVo> result = toWarnPlanVoList(list);
        return result == null ? Collections.emptyList() : result;
    }
}
