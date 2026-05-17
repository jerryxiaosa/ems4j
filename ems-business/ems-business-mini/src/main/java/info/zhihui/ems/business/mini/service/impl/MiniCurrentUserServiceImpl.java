package info.zhihui.ems.business.mini.service.impl;

import info.zhihui.ems.business.account.bo.AccountBo;
import info.zhihui.ems.business.account.dto.AccountElectricBalanceAggregateItemDto;
import info.zhihui.ems.business.account.service.AccountAdditionalInfoService;
import info.zhihui.ems.business.account.service.AccountInfoService;
import info.zhihui.ems.business.device.dto.ElectricMeterQueryDto;
import info.zhihui.ems.business.device.service.ElectricMeterInfoService;
import info.zhihui.ems.business.mini.bo.MiniCurrentUserBo;
import info.zhihui.ems.business.mini.service.MiniCurrentUserService;
import info.zhihui.ems.common.constant.ResultCode;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.components.context.RequestContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
        AccountBo account = getAccount(requestContext.getAccountId());
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

    private AccountBo getAccount(Integer accountId) {
        try {
            AccountBo account = accountInfoService.getById(accountId);

            // 账户类型为空，认为是账户异常
            if (account.getElectricAccountType() == null) {
                throw new BusinessRuntimeException(ResultCode.MINI_ACCOUNT_ABNORMAL.getCode(), ResultCode.MINI_ACCOUNT_ABNORMAL.getMessage());
            }

            return account;
        } catch (NotFoundException e) {
            throw new BusinessRuntimeException(ResultCode.MINI_ACCOUNT_ABNORMAL.getCode(), ResultCode.MINI_ACCOUNT_ABNORMAL.getMessage());
        }

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
