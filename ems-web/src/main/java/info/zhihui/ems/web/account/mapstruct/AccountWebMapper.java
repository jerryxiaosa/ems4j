package info.zhihui.ems.web.account.mapstruct;

import info.zhihui.ems.business.account.bo.AccountBo;
import info.zhihui.ems.business.account.dto.*;
import info.zhihui.ems.business.account.enums.CleanBalanceTypeEnum;
import info.zhihui.ems.business.device.bo.ElectricMeterBo;
import info.zhihui.ems.business.device.dto.CanceledMeterDto;
import info.zhihui.ems.business.device.dto.MeterCancelDetailDto;
import info.zhihui.ems.business.device.dto.MeterOpenDetailDto;
import info.zhihui.ems.common.enums.CodeEnum;
import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import info.zhihui.ems.common.enums.MeterTypeEnum;
import info.zhihui.ems.common.enums.OwnerTypeEnum;
import info.zhihui.ems.common.enums.WarnTypeEnum;
import info.zhihui.ems.web.account.vo.*;
import info.zhihui.ems.common.paging.PageResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 账户 Web 映射器
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AccountWebMapper {

    @Mapping(target = "ownerType", expression = "java(mapOwnerType(queryVo.getOwnerType()))")
    @Mapping(target = "electricAccountType", expression = "java(mapElectricType(queryVo.getElectricAccountType()))")
    AccountQueryDto toAccountQueryDto(AccountQueryVo queryVo);

    @Mapping(target = "cleanBalanceType", expression = "java(mapCleanBalanceType(queryVo.getCleanBalanceType()))")
    AccountCancelQueryDto toAccountCancelQueryDto(AccountCancelQueryVo queryVo);

    @Mapping(target = "ownerType", expression = "java(mapOwnerTypeCode(bo.getOwnerType()))")
    @Mapping(target = "electricAccountType", expression = "java(mapElectricTypeCode(bo.getElectricAccountType()))")
    @Mapping(target = "electricWarnType", expression = "java(mapWarnTypeCode(bo.getElectricWarnType()))")
    AccountVo toAccountVo(AccountBo bo);

    List<AccountVo> toAccountVoList(List<AccountBo> bos);

    @Mapping(target = "cleanBalanceType", expression = "java(mapCleanBalanceTypeCode(dto.getCleanBalanceType()))")
    AccountCancelRecordVo toAccountCancelRecordVo(AccountCancelRecordDto dto);

    List<AccountCancelRecordVo> toAccountCancelRecordVoList(List<AccountCancelRecordDto> list);

    @Mapping(target = "cleanBalanceType", expression = "java(mapCleanBalanceTypeCode(dto.getCleanBalanceType()))")
    AccountCancelDetailVo toAccountCancelDetailVo(AccountCancelDetailDto dto);

    @Mapping(target = "meterType", expression = "java(mapMeterTypeCode(dto.getMeterType()))")
    CanceledMeterVo toCanceledMeterVo(CanceledMeterDto dto);

    List<CanceledMeterVo> toCanceledMeterVoList(List<CanceledMeterDto> list);

    AccountMeterVo toAccountMeterVo(ElectricMeterBo bo);

    default List<AccountMeterVo> toAccountMeterVoList(List<ElectricMeterBo> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream().map(this::toAccountMeterVo).collect(Collectors.toList());
    }

    @Mapping(target = "ownerType", expression = "java(mapOwnerType(openAccountVo.getOwnerType()))")
    @Mapping(target = "electricAccountType", expression = "java(mapElectricType(openAccountVo.getElectricAccountType()))")
    @Mapping(target = "inheritHistoryPower", source = "openAccountVo.inheritHistoryPower")
    OpenAccountDto toOpenAccountDto(OpenAccountVo openAccountVo);

    @Mapping(target = "electricMeterList", expression = "java(toMeterOpenDetailDtoList(accountMetersOpenVo.getElectricMeterList()))")
    @Mapping(target = "inheritHistoryPower", source = "accountMetersOpenVo.inheritHistoryPower")
    AccountMetersOpenDto toAccountMetersOpenDto(AccountMetersOpenVo accountMetersOpenVo);

    AccountConfigUpdateDto toAccountConfigUpdateDto(AccountConfigUpdateVo accountConfigUpdateVo);

    MeterOpenDetailDto toMeterOpenDetailDto(MeterOpenDetailVo vo);

    @Mapping(target = "meterList", expression = "java(toMeterCancelDetailDtoList(cancelAccountVo.getMeterList()))")
    CancelAccountDto toCancelAccountDto(CancelAccountVo cancelAccountVo);

    default List<MeterCancelDetailDto> toMeterCancelDetailDtoList(List<MeterCancelDetailVo> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream().map(this::toMeterCancelDetailDto).collect(Collectors.toList());
    }

    MeterCancelDetailDto toMeterCancelDetailDto(MeterCancelDetailVo vo);

    default List<MeterOpenDetailDto> toMeterOpenDetailDtoList(List<MeterOpenDetailVo> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream().map(this::toMeterOpenDetailDto).collect(Collectors.toList());
    }

    @Mapping(target = "cleanBalanceType", expression = "java(mapCleanBalanceTypeCode(dto.getCleanBalanceType()))")
    CancelAccountResponseVo toCancelAccountResponseVo(CancelAccountResponseDto dto);

    default PageResult<AccountCancelRecordVo> toAccountCancelRecordVoPage(PageResult<AccountCancelRecordDto> pageResult) {
        if (pageResult == null) {
            return new PageResult<AccountCancelRecordVo>().setList(Collections.emptyList()).setPageNum(0).setPageSize(0).setTotal(0L);
        }
        List<AccountCancelRecordVo> list = toAccountCancelRecordVoList(pageResult.getList());
        return new PageResult<AccountCancelRecordVo>()
                .setPageNum(pageResult.getPageNum())
                .setPageSize(pageResult.getPageSize())
                .setTotal(pageResult.getTotal())
                .setList(list == null ? Collections.emptyList() : list);
    }

    default OwnerTypeEnum mapOwnerType(Integer code) {
        return code == null ? null : CodeEnum.fromCode(code, OwnerTypeEnum.class);
    }

    default Integer mapOwnerTypeCode(OwnerTypeEnum ownerTypeEnum) {
        return ownerTypeEnum == null ? null : ownerTypeEnum.getCode();
    }

    default ElectricAccountTypeEnum mapElectricType(Integer code) {
        return code == null ? null : CodeEnum.fromCode(code, ElectricAccountTypeEnum.class);
    }

    default Integer mapElectricTypeCode(ElectricAccountTypeEnum typeEnum) {
        return typeEnum == null ? null : typeEnum.getCode();
    }

    default String mapWarnTypeCode(WarnTypeEnum warnTypeEnum) {
        return warnTypeEnum == null ? null : warnTypeEnum.name();
    }

    default CleanBalanceTypeEnum mapCleanBalanceType(Integer code) {
        return code == null ? null : CodeEnum.fromCode(code, CleanBalanceTypeEnum.class);
    }

    default Integer mapCleanBalanceTypeCode(CleanBalanceTypeEnum typeEnum) {
        return typeEnum == null ? null : typeEnum.getCode();
    }

    default Integer mapMeterTypeCode(MeterTypeEnum meterTypeEnum) {
        return meterTypeEnum == null ? null : meterTypeEnum.getCode();
    }

    default PageResult<AccountVo> toAccountVoPage(PageResult<AccountBo> pageResult) {
        if (pageResult == null) {
            return new PageResult<AccountVo>().setPageNum(0).setPageSize(0).setTotal(0L).setList(Collections.emptyList());
        }
        List<AccountVo> list = toAccountVoList(pageResult.getList());
        return new PageResult<AccountVo>()
                .setPageNum(pageResult.getPageNum())
                .setPageSize(pageResult.getPageSize())
                .setTotal(pageResult.getTotal())
                .setList(list == null ? Collections.emptyList() : list);
    }

    default List<AccountCancelRecordVo> safeCancelRecordList(List<AccountCancelRecordDto> list) {
        return toAccountCancelRecordVoList(list == null ? Collections.emptyList() : list);
    }

    default List<AccountVo> safeAccountVoList(List<AccountBo> list) {
        return list == null ? Collections.emptyList() : toAccountVoList(list);
    }
}
