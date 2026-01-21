package info.zhihui.ems.business.device.mapper;


import com.github.pagehelper.PageInfo;
import info.zhihui.ems.business.device.bo.ElectricMeterBo;
import info.zhihui.ems.business.device.dto.CanceledMeterDto;
import info.zhihui.ems.business.device.dto.ElectricMeterCreateDto;
import info.zhihui.ems.business.device.dto.ElectricMeterQueryDto;
import info.zhihui.ems.business.device.dto.ElectricMeterUpdateDto;
import info.zhihui.ems.business.device.entity.ElectricMeterEntity;
import info.zhihui.ems.business.device.entity.MeterCancelRecordEntity;
import info.zhihui.ems.business.device.qo.ElectricMeterQo;
import info.zhihui.ems.common.enums.CalculateTypeEnum;
import info.zhihui.ems.common.enums.CodeEnum;
import info.zhihui.ems.common.enums.MeterTypeEnum;
import info.zhihui.ems.common.enums.WarnTypeEnum;
import info.zhihui.ems.common.paging.PageResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ElectricMeterMapper {

    @Mapping(target = "calculateType", source = "calculateType", qualifiedByName = "calculateTypeEnumToInt")
    ElectricMeterQo queryDtoToQo(ElectricMeterQueryDto dto);

    PageResult<ElectricMeterBo> pageEntityToBo(PageInfo<ElectricMeterEntity> entity);

    List<ElectricMeterBo> listEntityToBo(List<ElectricMeterEntity> entityList);

    @Mapping(target = "calculateType", source = "calculateType", qualifiedByName = "intToCalculateTypeEnum")
    @Mapping(target = "warnType", source = "warnType", qualifiedByName = "stringToWarnTypeEnum")
    ElectricMeterBo entityToBo(ElectricMeterEntity entity);

    @Mapping(target = "calculateType", source = "calculateType", qualifiedByName = "calculateTypeEnumToInt")
    @Mapping(target = "warnType", source = "warnType", qualifiedByName = "warnTypeEnumToString")
    ElectricMeterEntity boToEntity(ElectricMeterBo bo);

    @Mapping(target = "calculateType", source = "calculateType", qualifiedByName = "calculateTypeEnumToInt")
    ElectricMeterEntity saveDtoToEntity(ElectricMeterCreateDto dto);

    @Mapping(target = "calculateType", source = "calculateType", qualifiedByName = "calculateTypeEnumToInt")
    ElectricMeterEntity updateDtoToEntity(ElectricMeterUpdateDto dto);

    List<CanceledMeterDto> listMeterEntityToDto(List<MeterCancelRecordEntity> entities);


    /**
     * 销表记录实体转换为DTO
     */
    @Mapping(target = "meterType", source = "meterType", qualifiedByName = "intToMeterTypeEnum")
    CanceledMeterDto meterCancelEntityToDto(MeterCancelRecordEntity entity);

    /**
     * 整型转换为表类型枚举
     */
    @Named("intToMeterTypeEnum")
    default MeterTypeEnum intToMeterTypeEnum(Integer meterType) {
        return CodeEnum.fromCode(meterType, MeterTypeEnum.class);
    }

    /**
     * 整型转换为计量类型枚举
     */
    @Named("intToCalculateTypeEnum")
    default CalculateTypeEnum intToCalculateTypeEnum(Integer calculateType) {
        return CodeEnum.fromCode(calculateType, CalculateTypeEnum.class);
    }

    /**
     * 计量类型枚举转换为整型
     */
    @Named("calculateTypeEnumToInt")
    default Integer calculateTypeEnumToInt(CalculateTypeEnum calculateType) {
        return calculateType == null ? null : calculateType.getCode();
    }

    @Named("stringToWarnTypeEnum")
    default WarnTypeEnum stringToWarnTypeEnum(String warnType) {
        if (warnType == null) {
            return null;
        }
        for (WarnTypeEnum value : WarnTypeEnum.values()) {
            if (value.name().equals(warnType)) {
                return value;
            }
        }
        return null;
    }

    @Named("warnTypeEnumToString")
    default String warnTypeEnumToString(WarnTypeEnum warnTypeEnum) {
        return warnTypeEnum == null ? null : warnTypeEnum.name();
    }

}
