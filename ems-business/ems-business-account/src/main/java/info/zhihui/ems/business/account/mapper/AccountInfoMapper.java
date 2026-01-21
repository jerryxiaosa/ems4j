package info.zhihui.ems.business.account.mapper;


import info.zhihui.ems.common.enums.CodeEnum;
import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import info.zhihui.ems.business.account.bo.AccountBo;
import info.zhihui.ems.business.account.dto.AccountQueryDto;
import info.zhihui.ems.business.account.entity.AccountEntity;
import info.zhihui.ems.common.enums.OwnerTypeEnum;
import info.zhihui.ems.common.enums.WarnTypeEnum;
import info.zhihui.ems.business.account.qo.AccountQo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AccountInfoMapper {

    List<AccountBo> listEntityToBo(List<AccountEntity> list);

    @Mapping(target = "ownerType", source = "ownerType", qualifiedByName = "getOwnerTypeEnum")
    @Mapping(target = "electricAccountType", source = "electricAccountType", qualifiedByName = "getElectricAccountTypeEnum")
    @Mapping(target = "electricWarnType", source = "electricWarnType", qualifiedByName = "getWarnTypeEnum")
    AccountBo entityToBo(AccountEntity entity);

    @Mapping(target = "ownerType", source = "ownerType", qualifiedByName = "getOwnerTypeCode")
    @Mapping(target = "electricAccountType", source = "electricAccountType", qualifiedByName = "getElectricAccountTypeCode")
    AccountQo queryToQo(AccountQueryDto query);

    @Named("getOwnerTypeCode")
    default Integer getOwnerTypeCode(OwnerTypeEnum ownerType) {
        return ownerType == null ? null : ownerType.getCode();
    }

    @Named("getOwnerTypeEnum")
    default OwnerTypeEnum getOwnerTypeEnum(Integer ownerType) {
        return ownerType == null ? null : CodeEnum.fromCode(ownerType, OwnerTypeEnum.class);
    }

    @Named("getElectricAccountTypeCode")
    default Integer getElectricAccountTypeCode(ElectricAccountTypeEnum electricAccountType) {
        return electricAccountType == null ? null : electricAccountType.getCode();
    }

    @Named("getElectricAccountTypeEnum")
    default ElectricAccountTypeEnum getElectricAccountTypeEnum(Integer electricAccountType) {
        return electricAccountType == null ? null : CodeEnum.fromCode(electricAccountType, ElectricAccountTypeEnum.class);
    }

    @Named("getWarnTypeEnum")
    default WarnTypeEnum getWarnTypeEnum(String warnType) {
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
}
