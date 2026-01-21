package info.zhihui.ems.web.plan.mapstruct;

import info.zhihui.ems.business.plan.bo.ElectricPricePlanBo;
import info.zhihui.ems.business.plan.bo.ElectricPricePlanDetailBo;
import info.zhihui.ems.business.plan.bo.StepPriceBo;
import info.zhihui.ems.business.plan.dto.ElectricPricePlanQueryDto;
import info.zhihui.ems.business.plan.dto.ElectricPricePlanSaveDto;
import info.zhihui.ems.business.plan.dto.ElectricPriceTimeDto;
import info.zhihui.ems.business.plan.dto.ElectricPriceTypeDto;
import info.zhihui.ems.web.plan.vo.ElectricPricePlanDetailVo;
import info.zhihui.ems.web.plan.vo.ElectricPricePlanQueryVo;
import info.zhihui.ems.web.plan.vo.ElectricPricePlanSaveVo;
import info.zhihui.ems.web.plan.vo.ElectricPricePlanVo;
import info.zhihui.ems.web.plan.vo.ElectricPriceTimeSettingVo;
import info.zhihui.ems.web.plan.vo.ElectricPriceTypeVo;
import info.zhihui.ems.web.plan.vo.StepPriceVo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import info.zhihui.ems.common.enums.ElectricPricePeriodEnum;
import java.util.Optional;
import java.util.Objects;
import java.util.List;

/**
 * 电价方案 Web 层映射
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ElectricPricePlanWebMapper {

    ElectricPricePlanQueryDto toElectricPricePlanQueryDto(ElectricPricePlanQueryVo queryVo);

    ElectricPricePlanSaveDto toElectricPricePlanSaveDto(ElectricPricePlanSaveVo saveVo);

    ElectricPricePlanVo toElectricPricePlanVo(ElectricPricePlanBo bo);

    List<ElectricPricePlanVo> toElectricPricePlanVoList(List<ElectricPricePlanBo> bos);

    ElectricPricePlanDetailVo toElectricPricePlanDetailVo(ElectricPricePlanDetailBo bo);

    StepPriceBo toStepPriceBo(StepPriceVo vo);

    StepPriceVo toStepPriceVo(StepPriceBo bo);

    List<StepPriceBo> toStepPriceBoList(List<StepPriceVo> list);

    List<StepPriceVo> toStepPriceVoList(List<StepPriceBo> list);

    @Mapping(target = "type", expression = "java(mapElectricDegreeType(vo.getType()).orElse(null))")
    ElectricPriceTimeDto toElectricPriceTimeDto(ElectricPriceTimeSettingVo vo);

    @Mapping(target = "type", expression = "java(mapElectricDegreeTypeCode(dto.getType()))")
    ElectricPriceTimeSettingVo toElectricPriceTimeSettingVo(ElectricPriceTimeDto dto);

    List<ElectricPriceTimeDto> toElectricPriceTimeDtoList(List<ElectricPriceTimeSettingVo> list);

    List<ElectricPriceTimeSettingVo> toElectricPriceTimeSettingVoList(List<ElectricPriceTimeDto> list);

    @Mapping(target = "type", expression = "java(mapElectricDegreeType(vo.getType()).orElse(null))")
    ElectricPriceTypeDto toElectricPriceTypeDto(ElectricPriceTypeVo vo);

    @Mapping(target = "type", expression = "java(mapElectricDegreeTypeCode(dto.getType()))")
    ElectricPriceTypeVo toElectricPriceTypeVo(ElectricPriceTypeDto dto);

    List<ElectricPriceTypeDto> toElectricPriceTypeDtoList(List<ElectricPriceTypeVo> list);

    List<ElectricPriceTypeVo> toElectricPriceTypeVoList(List<ElectricPriceTypeDto> list);

    // ---- helpers for enum/code conversion ----
    default Optional<ElectricPricePeriodEnum> mapElectricDegreeType(Integer code) {
        if (code == null) {
            return Optional.empty();
        }
        for (ElectricPricePeriodEnum value : ElectricPricePeriodEnum.values()) {
            if (Objects.equals(value.getCode(), code)) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }

    default Integer mapElectricDegreeTypeCode(ElectricPricePeriodEnum enumVal) {
        return enumVal == null ? null : enumVal.getCode();
    }
}
