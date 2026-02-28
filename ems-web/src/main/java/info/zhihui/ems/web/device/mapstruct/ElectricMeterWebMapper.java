package info.zhihui.ems.web.device.mapstruct;

import info.zhihui.ems.business.device.bo.ElectricMeterBo;
import info.zhihui.ems.business.device.dto.*;
import info.zhihui.ems.business.finance.dto.ElectricMeterLatestPowerRecordDto;
import info.zhihui.ems.business.device.enums.ElectricSwitchStatusEnum;
import info.zhihui.ems.business.plan.dto.ElectricPriceTimeDto;
import info.zhihui.ems.common.enums.*;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.web.device.vo.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 电表 Web 层对象转换器
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ElectricMeterWebMapper {

    @Mapping(target = "calculateType", expression = "java(intToCalculateTypeEnum(queryVo.getCalculateType()))")
    ElectricMeterQueryDto toElectricMeterQueryDto(ElectricMeterQueryVo queryVo);

    @Mapping(target = "calculateType", expression = "java(intToCalculateTypeEnum(createVo.getCalculateType()))")
    ElectricMeterCreateDto toElectricMeterAddDto(ElectricMeterCreateVo createVo);

    @Mapping(target = "calculateType", expression = "java(intToCalculateTypeEnum(updateVo.getCalculateType()))")
    ElectricMeterUpdateDto toElectricMeterUpdateDto(ElectricMeterUpdateVo updateVo);

    @Mapping(target = "switchStatus", expression = "java(mapSwitchStatus(vo.getSwitchStatus()))")
    ElectricMeterSwitchStatusDto toSwitchStatusDto(ElectricMeterSwitchStatusVo vo);

    @Mapping(target = "timeList", expression = "java(toElectricPriceTimeDtoList(vo.getTimeList()))")
    ElectricMeterTimeDto toElectricMeterTimeDto(ElectricMeterTimeVo vo);

    ElectricMeterCtDto toElectricMeterCtDto(ElectricMeterCtVo vo);

    ElectricMeterOnlineStatusDto toElectricMeterOnlineStatusDto(ElectricMeterOnlineStatusVo vo);

    @Mapping(target = "meterOpenDetail", expression = "java(toMeterOpenDetailDtoList(vo.getMeterOpenDetail()))")
    @Mapping(target = "ownerType", expression = "java(mapOwnerType(vo.getOwnerType()))")
    @Mapping(target = "electricAccountType", expression = "java(mapElectricAccountType(vo.getElectricAccountType()))")
    MeterOpenDto toMeterOpenDto(MeterOpenVo vo);

    MeterOpenDetailDto toMeterOpenDetailDto(MeterOpenDetailVo vo);

    @Mapping(target = "meterCloseDetail", expression = "java(toMeterCancelDetailDtoList(vo.getMeterCloseDetail()))")
    @Mapping(target = "ownerType", expression = "java(mapOwnerType(vo.getOwnerType()))")
    @Mapping(target = "electricAccountType", expression = "java(mapElectricAccountType(vo.getElectricAccountType()))")
    MeterCancelDto toMeterCancelDto(MeterCancelVo vo);

    MeterCancelDetailDto toMeterCancelDetailDto(MeterCancelDetailVo vo);

    @Mapping(target = "calculateType", expression = "java(calculateTypeEnumToInt(bo.getCalculateType()))")
    @Mapping(target = "warnType", expression = "java(mapWarnTypeEnumToString(bo.getWarnType()))")
    ElectricMeterVo toElectricMeterVo(ElectricMeterBo bo);

    List<ElectricMeterVo> toElectricMeterVoList(List<ElectricMeterBo> bos);

    @Mapping(target = "calculateType", expression = "java(calculateTypeEnumToInt(bo.getCalculateType()))")
    @Mapping(target = "warnType", expression = "java(mapWarnTypeEnumToString(bo.getWarnType()))")
    ElectricMeterDetailVo toElectricMeterDetailVo(ElectricMeterBo bo);

    ElectricMeterLatestPowerRecordVo toElectricMeterLatestPowerRecordVo(ElectricMeterLatestPowerRecordDto dto);

    CanceledMeterVo toCanceledMeterVo(CanceledMeterDto dto);

    List<CanceledMeterVo> toCanceledMeterVoList(List<CanceledMeterDto> list);

    default PageResult<ElectricMeterVo> toElectricMeterVoPage(PageResult<ElectricMeterBo> pageResult) {
        if (pageResult == null) {
            return new PageResult<ElectricMeterVo>()
                    .setPageNum(0)
                    .setPageSize(0)
                    .setTotal(0L)
                    .setList(Collections.emptyList());
        }

        List<ElectricMeterVo> list = toElectricMeterVoList(pageResult.getList());
        return new PageResult<ElectricMeterVo>()
                .setPageNum(pageResult.getPageNum())
                .setPageSize(pageResult.getPageSize())
                .setTotal(pageResult.getTotal())
                .setList(list == null ? Collections.emptyList() : list);
    }

    default List<MeterOpenDetailDto> toMeterOpenDetailDtoList(List<MeterOpenDetailVo> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream().map(this::toMeterOpenDetailDto).collect(Collectors.toList());
    }

    default List<MeterCancelDetailDto> toMeterCancelDetailDtoList(List<MeterCancelDetailVo> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream().map(this::toMeterCancelDetailDto).collect(Collectors.toList());
    }

    default List<ElectricPriceTimeDto> toElectricPriceTimeDtoList(List<ElectricPriceTimeVo> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream().map(vo -> new ElectricPriceTimeDto()
                .setType(mapElectricDegreeType(vo.getType()).orElse(null))
                .setStart(vo.getStart()))
                .collect(Collectors.toList());
    }

    default List<MeterCancelBalanceVo> toMeterCancelBalanceVoList(List<MeterCancelResultDto> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream()
                .map(balance -> new MeterCancelBalanceVo()
                        .setMeterId(balance.getMeterId())
                        .setBalance(balance.getBalance())
                        .setHistoryPowerTotal(balance.getHistoryPowerTotal()))
                .collect(Collectors.toList());
    }

    default List<ElectricMeterPowerVo> toElectricMeterPowerVoList(Map<ElectricPricePeriodEnum, BigDecimal> powerMap) {
        if (powerMap == null || powerMap.isEmpty()) {
            return Collections.emptyList();
        }
        return powerMap.entrySet().stream()
                .map(entry -> new ElectricMeterPowerVo()
                        .setType(entry.getKey() == null ? null : entry.getKey().getCode())
                        .setValue(entry.getValue()))
                .sorted((left, right) -> {
                    Integer leftType = left.getType();
                    Integer rightType = right.getType();
                    if (leftType == null && rightType == null) {
                        return 0;
                    }
                    if (leftType == null) {
                        return 1;
                    }
                    if (rightType == null) {
                        return -1;
                    }
                    return leftType.compareTo(rightType);
                })
                .collect(Collectors.toList());
    }

    default List<ElectricPricePeriodEnum> toElectricDegreeTypeEnumList(List<Integer> types) {
        if (types == null) {
            return Collections.emptyList();
        }
        EnumSet<ElectricPricePeriodEnum> enumSet = EnumSet.noneOf(ElectricPricePeriodEnum.class);
        for (Integer code : types) {
            mapElectricDegreeType(code).ifPresent(enumSet::add);
        }
        return new ArrayList<>(enumSet);
    }

    default ElectricSwitchStatusEnum mapSwitchStatus(Integer code) {
        if (code == null) {
            return null;
        }
        for (ElectricSwitchStatusEnum value : ElectricSwitchStatusEnum.values()) {
            if (Objects.equals(value.getCode(), code)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Invalid electric switch status code: " + code);
    }

    default OwnerTypeEnum mapOwnerType(Integer code) {
        return code == null ? null : CodeEnum.fromCode(code, OwnerTypeEnum.class);
    }

    default ElectricAccountTypeEnum mapElectricAccountType(Integer code) {
        return code == null ? null : CodeEnum.fromCode(code, ElectricAccountTypeEnum.class);
    }

    default WarnTypeEnum mapWarnType(String name) {
        return name == null ? null : CodeEnum.fromCode(name, WarnTypeEnum.class);
    }

    default String mapWarnTypeEnumToString(WarnTypeEnum warnTypeEnum) {
        return warnTypeEnum == null ? null : warnTypeEnum.getCode();
    }

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

    default Integer mapMeterTypeCode(MeterTypeEnum meterTypeEnum) {
        return meterTypeEnum == null ? null : meterTypeEnum.getCode();
    }

    default CalculateTypeEnum intToCalculateTypeEnum(Integer code) {
        return code == null ? null : CodeEnum.fromCode(code, CalculateTypeEnum.class);
    }

    default Integer calculateTypeEnumToInt(CalculateTypeEnum calculateTypeEnum) {
        return calculateTypeEnum == null ? null : calculateTypeEnum.getCode();
    }
}
