package info.zhihui.ems.business.account.mapper;

import com.github.pagehelper.PageInfo;
import info.zhihui.ems.business.account.dto.AccountCancelDetailDto;
import info.zhihui.ems.business.account.dto.AccountCancelQueryDto;
import info.zhihui.ems.business.account.dto.AccountCancelRecordDto;
import info.zhihui.ems.business.account.entity.AccountCancelRecordEntity;
import info.zhihui.ems.business.account.enums.CleanBalanceTypeEnum;
import info.zhihui.ems.business.account.qo.AccountCancelRecordQo;
import info.zhihui.ems.common.enums.CodeEnum;
import info.zhihui.ems.common.paging.PageResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

/**
 * 账户销户相关映射器
 *
 * @author jerryxiaosa
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AccountCancelMapper {

    /**
     * 查询DTO转换为查询对象
     */
    @Mapping(target = "cleanBalanceType", source = "cleanBalanceType", qualifiedByName = "enumToCode")
    AccountCancelRecordQo queryDtoToQo(AccountCancelQueryDto dto);

    /**
     * 分页实体转换为分页Dto
     */
    PageResult<AccountCancelRecordDto> pageEntityToBo(PageInfo<AccountCancelRecordEntity> pageInfo);

    /**
     * 实体转换为列表DTO
     */
    @Mapping(target = "operatorName", source = "createUserName")
    @Mapping(target = "cancelTime", source = "createTime")
    @Mapping(target = "cleanBalanceType", source = "cleanBalanceType", qualifiedByName = "codeToEnum")
    AccountCancelRecordDto entityToDto(AccountCancelRecordEntity entity);

    /**
     * 实体转换为详情DTO
     */
    @Mapping(target = "operatorName", source = "createUserName")
    @Mapping(target = "cancelTime", source = "createTime")
    @Mapping(target = "cleanBalanceType", source = "cleanBalanceType", qualifiedByName = "codeToEnum")
    AccountCancelDetailDto entityToDetailDto(AccountCancelRecordEntity entity);

    /**
     * 枚举转换为code值
     */
    @Named("enumToCode")
    default Integer enumToCode(CleanBalanceTypeEnum cleanBalanceType) {
        return cleanBalanceType != null ? cleanBalanceType.getCode() : null;
    }

    /**
     * code值转换为枚举
     */
    @Named("codeToEnum")
    default CleanBalanceTypeEnum codeToEnum(Integer code) {
        return code != null ? CodeEnum.fromCode(code, CleanBalanceTypeEnum.class) : null;
    }

}