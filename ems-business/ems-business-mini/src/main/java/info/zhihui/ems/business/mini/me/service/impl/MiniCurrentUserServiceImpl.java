package info.zhihui.ems.business.mini.me.service.impl;

import info.zhihui.ems.business.account.bo.AccountBo;
import info.zhihui.ems.business.account.dto.AccountElectricBalanceAggregateItemDto;
import info.zhihui.ems.business.account.dto.AccountQueryDto;
import info.zhihui.ems.business.account.service.AccountAdditionalInfoService;
import info.zhihui.ems.business.account.service.AccountInfoService;
import info.zhihui.ems.business.device.dto.ElectricMeterQueryDto;
import info.zhihui.ems.business.device.service.ElectricMeterInfoService;
import info.zhihui.ems.business.mini.me.bo.MiniCurrentUserBo;
import info.zhihui.ems.business.mini.me.service.MiniCurrentUserService;
import info.zhihui.ems.common.constant.ResultCode;
import info.zhihui.ems.common.enums.OwnerTypeEnum;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.components.context.RequestContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 小程序当前用户服务实现。
 */
@Service
@RequiredArgsConstructor
public class MiniCurrentUserServiceImpl implements MiniCurrentUserService {

    private final RequestContext requestContext;
    private final AccountInfoService accountInfoService;
    private final AccountAdditionalInfoService accountAdditionalInfoService;
    private final ElectricMeterInfoService electricMeterInfoService;

    @Override
    public MiniCurrentUserBo getCurrentUser() {
        AccountBo account = getEnterpriseAccount(requestContext.getOrganizationId());
        BigDecimal balance = getBalance(account);
        int meterCount = electricMeterInfoService.findList(new ElectricMeterQueryDto()
                .setAccountIds(List.of(account.getId()))).size();

        return new MiniCurrentUserBo()
                .setUserPhone(requestContext.getUserPhone())
                .setElectricAccountId(account.getId())
                .setElectricAccountName(account.getOwnerName())
                .setElectricAccountType(account.getElectricAccountType())
                .setBalance(balance)
                .setMeterCount(meterCount);
    }

    private AccountBo getEnterpriseAccount(Integer organizationId) {
        if (organizationId == null) {
            throw new BusinessRuntimeException(ResultCode.MINI_ACCOUNT_NOT_FOUND.getCode(), ResultCode.MINI_ACCOUNT_NOT_FOUND.getMessage());
        }
        List<AccountBo> accountList = accountInfoService.findList(new AccountQueryDto()
                .setOwnerType(OwnerTypeEnum.ENTERPRISE)
                .setOwnerIds(List.of(organizationId)));
        if (CollectionUtils.isEmpty(accountList)) {
            throw new BusinessRuntimeException(ResultCode.MINI_ACCOUNT_NOT_FOUND.getCode(), ResultCode.MINI_ACCOUNT_NOT_FOUND.getMessage());
        }
        if (accountList.size() > 1 || accountList.get(0).getElectricAccountType() == null) {
            throw new BusinessRuntimeException(ResultCode.MINI_ACCOUNT_ABNORMAL.getCode(), ResultCode.MINI_ACCOUNT_ABNORMAL.getMessage());
        }
        return accountList.get(0);
    }

    private BigDecimal getBalance(AccountBo account) {
        Map<Integer, BigDecimal> balanceMap = accountAdditionalInfoService.findElectricBalanceAmountMap(List.of(
                new AccountElectricBalanceAggregateItemDto()
                        .setAccountId(account.getId())
                        .setElectricAccountType(account.getElectricAccountType())
        ));
        return balanceMap.getOrDefault(account.getId(), BigDecimal.ZERO);
    }
}
