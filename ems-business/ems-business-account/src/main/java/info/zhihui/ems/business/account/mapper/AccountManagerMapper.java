package info.zhihui.ems.business.account.mapper;

import info.zhihui.ems.business.account.dto.OpenAccountDto;
import info.zhihui.ems.business.account.entity.AccountEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * @author jerryxiaosa
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AccountManagerMapper {
    AccountEntity openAccountDtoToEntity(OpenAccountDto openAccountDto);
}
