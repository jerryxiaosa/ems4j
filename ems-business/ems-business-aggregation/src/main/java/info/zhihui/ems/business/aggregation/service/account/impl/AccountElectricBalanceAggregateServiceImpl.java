package info.zhihui.ems.business.aggregation.service.account.impl;

import info.zhihui.ems.business.aggregation.dto.AccountElectricBalanceAggregateItemDto;
import info.zhihui.ems.business.aggregation.service.account.AccountElectricBalanceAggregateService;
import info.zhihui.ems.business.finance.bo.BalanceBo;
import info.zhihui.ems.business.finance.service.balance.BalanceService;
import info.zhihui.ems.common.enums.BalanceTypeEnum;
import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 账户电费余额聚合服务实现
 */
@Service
@Slf4j
@Validated
@RequiredArgsConstructor
public class AccountElectricBalanceAggregateServiceImpl implements AccountElectricBalanceAggregateService {

    private final BalanceService balanceService;

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Integer, BigDecimal> findElectricBalanceAmountMap(
            @NotEmpty List<@Valid @NotNull AccountElectricBalanceAggregateItemDto> itemDtoList) {
        // 同一账户只保留一条计费类型配置，避免后续重复计算。
        Map<Integer, ElectricAccountTypeEnum> accountElectricAccountTypeMap = new LinkedHashMap<>();
        for (AccountElectricBalanceAggregateItemDto itemDto : itemDtoList) {
            Integer accountId = itemDto.getAccountId();
            ElectricAccountTypeEnum newElectricAccountType = itemDto.getElectricAccountType();
            if (!accountElectricAccountTypeMap.containsKey(accountId)) {
                accountElectricAccountTypeMap.put(accountId, newElectricAccountType);
                continue;
            }

            ElectricAccountTypeEnum oldElectricAccountType = accountElectricAccountTypeMap.get(accountId);
            if (!Objects.equals(oldElectricAccountType, newElectricAccountType)) {
                log.warn("账户计费类型不一致，accountId={}, oldType={}, newType={}",
                        accountId, oldElectricAccountType, newElectricAccountType);
            }
        }
        if (accountElectricAccountTypeMap.isEmpty()) {
            return Collections.emptyMap();
        }

        // 复用 finance 现有批量查询接口，在 aggregation 层做规则聚合。
        List<Integer> accountIdList = new ArrayList<>(accountElectricAccountTypeMap.keySet());
        List<BalanceBo> balanceBoList = balanceService.findListByAccountIds(accountIdList);
        Map<Integer, BigDecimal> accountBalanceAmountMap = new LinkedHashMap<>();
        Map<Integer, BigDecimal> meterBalanceAmountSumMap = new LinkedHashMap<>();
        if (balanceBoList != null) {
            for (BalanceBo balanceBo : balanceBoList) {
                if (balanceBo == null || balanceBo.getAccountId() == null || balanceBo.getBalanceType() == null) {
                    continue;
                }
                BigDecimal balanceAmount = Objects.requireNonNullElse(balanceBo.getBalance(), BigDecimal.ZERO);
                if (BalanceTypeEnum.ACCOUNT.equals(balanceBo.getBalanceType())) {
                    accountBalanceAmountMap.put(balanceBo.getAccountId(), balanceAmount);
                    continue;
                }
                if (BalanceTypeEnum.ELECTRIC_METER.equals(balanceBo.getBalanceType())) {
                    meterBalanceAmountSumMap.merge(balanceBo.getAccountId(), balanceAmount, BigDecimal::add);
                }
            }
        }

        // 按账户计费类型选择展示余额来源：按需取电表余额，包月/合并取账户余额。
        Map<Integer, BigDecimal> electricBalanceAmountMap = new LinkedHashMap<>();
        for (Map.Entry<Integer, ElectricAccountTypeEnum> entry : accountElectricAccountTypeMap.entrySet()) {
            Integer accountId = entry.getKey();
            ElectricAccountTypeEnum electricAccountType = entry.getValue();
            BalanceTypeEnum balanceType = toBalanceType(electricAccountType);
            BigDecimal electricBalanceAmount = BigDecimal.ZERO;
            if (BalanceTypeEnum.ACCOUNT.equals(balanceType)) {
                electricBalanceAmount = accountBalanceAmountMap.getOrDefault(accountId, BigDecimal.ZERO);
            } else if (BalanceTypeEnum.ELECTRIC_METER.equals(balanceType)) {
                electricBalanceAmount = meterBalanceAmountSumMap.getOrDefault(accountId, BigDecimal.ZERO);
            }
            electricBalanceAmountMap.put(accountId, electricBalanceAmount);
        }
        return electricBalanceAmountMap;
    }

    private BalanceTypeEnum toBalanceType(ElectricAccountTypeEnum electricAccountType) {
        if (ElectricAccountTypeEnum.QUANTITY.equals(electricAccountType)) {
            return BalanceTypeEnum.ELECTRIC_METER;
        }
        if (ElectricAccountTypeEnum.MONTHLY.equals(electricAccountType)
                || ElectricAccountTypeEnum.MERGED.equals(electricAccountType)) {
            return BalanceTypeEnum.ACCOUNT;
        }
        return null;
    }
}
