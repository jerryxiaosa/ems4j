package info.zhihui.ems.business.aggregation.service.account.impl;

import info.zhihui.ems.business.aggregation.dto.AccountElectricBalanceAggregateItemDto;
import info.zhihui.ems.business.finance.bo.BalanceBo;
import info.zhihui.ems.business.finance.dto.BalanceDeleteDto;
import info.zhihui.ems.business.finance.dto.BalanceDto;
import info.zhihui.ems.business.finance.dto.BalanceQueryDto;
import info.zhihui.ems.business.finance.service.balance.BalanceService;
import info.zhihui.ems.common.enums.BalanceTypeEnum;
import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * AccountElectricBalanceAggregateServiceImpl 单元测试
 */
class AccountElectricBalanceAggregateServiceImplTest {

    @Test
    @DisplayName("按余额来源类型聚合电费余额")
    void testFindElectricBalanceAmountMap_Normal() {
        AccountElectricBalanceAggregateServiceImpl service = new AccountElectricBalanceAggregateServiceImpl(
                new StubBalanceService(List.of(
                        new BalanceBo()
                                .setAccountId(1)
                                .setBalanceType(BalanceTypeEnum.ACCOUNT)
                                .setBalance(new BigDecimal("100.00")),
                        new BalanceBo()
                                .setAccountId(1)
                                .setBalanceType(BalanceTypeEnum.ELECTRIC_METER)
                                .setBalance(new BigDecimal("10.00")),
                        new BalanceBo()
                                .setAccountId(1)
                                .setBalanceType(BalanceTypeEnum.ELECTRIC_METER)
                                .setBalance(new BigDecimal("5.50")),
                        new BalanceBo()
                                .setAccountId(2)
                                .setBalanceType(BalanceTypeEnum.ACCOUNT)
                                .setBalance(new BigDecimal("88.00"))
                ))
        );

        Map<Integer, BigDecimal> result = service.findElectricBalanceAmountMap(List.of(
                new AccountElectricBalanceAggregateItemDto()
                        .setAccountId(1)
                        .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY),
                new AccountElectricBalanceAggregateItemDto()
                        .setAccountId(2)
                        .setElectricAccountType(ElectricAccountTypeEnum.MONTHLY),
                new AccountElectricBalanceAggregateItemDto()
                        .setAccountId(3)
                        .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY)
        ));

        assertEquals(0, new BigDecimal("15.50").compareTo(result.get(1)));
        assertEquals(0, new BigDecimal("88.00").compareTo(result.get(2)));
        assertEquals(0, BigDecimal.ZERO.compareTo(result.get(3)));
    }

    @Test
    @DisplayName("合并计费应取账户余额")
    void testFindElectricBalanceAmountMap_MergedType_ShouldUseAccountBalance() {
        AccountElectricBalanceAggregateServiceImpl service = new AccountElectricBalanceAggregateServiceImpl(
                new StubBalanceService(List.of(
                        new BalanceBo()
                                .setAccountId(2)
                                .setBalanceType(BalanceTypeEnum.ACCOUNT)
                                .setBalance(new BigDecimal("66.00")),
                        new BalanceBo()
                                .setAccountId(2)
                                .setBalanceType(BalanceTypeEnum.ELECTRIC_METER)
                                .setBalance(new BigDecimal("11.00"))
                ))
        );

        Map<Integer, BigDecimal> result = service.findElectricBalanceAmountMap(List.of(
                new AccountElectricBalanceAggregateItemDto()
                        .setAccountId(2)
                        .setElectricAccountType(ElectricAccountTypeEnum.MERGED)
        ));

        assertEquals(0, new BigDecimal("66.00").compareTo(result.get(2)));
    }

    @Test
    @DisplayName("重复账户首次计费类型为null时应仍以首次为准并返回0")
    void testFindElectricBalanceAmountMap_DuplicateAccountWithFirstNullType_ShouldKeepFirst() {
        AccountElectricBalanceAggregateServiceImpl service = new AccountElectricBalanceAggregateServiceImpl(
                new StubBalanceService(List.of(
                        new BalanceBo()
                                .setAccountId(1)
                                .setBalanceType(BalanceTypeEnum.ACCOUNT)
                                .setBalance(new BigDecimal("99.00"))
                ))
        );

        Map<Integer, BigDecimal> result = service.findElectricBalanceAmountMap(List.of(
                new AccountElectricBalanceAggregateItemDto()
                        .setAccountId(1)
                        .setElectricAccountType(null),
                new AccountElectricBalanceAggregateItemDto()
                        .setAccountId(1)
                        .setElectricAccountType(ElectricAccountTypeEnum.MONTHLY)
        ));

        assertEquals(0, BigDecimal.ZERO.compareTo(result.get(1)));
    }

    private static class StubBalanceService implements BalanceService {

        private final List<BalanceBo> balanceBoList;

        private StubBalanceService(List<BalanceBo> balanceBoList) {
            this.balanceBoList = balanceBoList;
        }

        @Override
        public void topUp(BalanceDto topUpDto) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void deduct(BalanceDto deductDto) {
            throw new UnsupportedOperationException();
        }

        @Override
        public BalanceBo getByQuery(BalanceQueryDto queryDto) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<BalanceBo> findListByAccountIds(List<Integer> accountIds) {
            return balanceBoList;
        }

        @Override
        public void initAccountBalance(Integer accountId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void initElectricMeterBalance(Integer electricMeterId, Integer accountId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void deleteBalance(BalanceDeleteDto deleteDto) {
            throw new UnsupportedOperationException();
        }
    }
}
