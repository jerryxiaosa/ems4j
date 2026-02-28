package info.zhihui.ems.web.account;

import info.zhihui.ems.business.device.dto.ElectricMeterQueryDto;
import info.zhihui.ems.business.device.service.ElectricMeterInfoService;
import info.zhihui.ems.common.enums.BalanceTypeEnum;
import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.web.account.biz.AccountBiz;
import info.zhihui.ems.web.account.vo.AccountDetailVo;
import info.zhihui.ems.web.account.vo.AccountMeterVo;
import info.zhihui.ems.web.account.vo.AccountQueryVo;
import info.zhihui.ems.web.account.vo.AccountVo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("integrationtest")
@Transactional
class AccountBizIntegrationTest {

    @Autowired
    private AccountBiz accountBiz;

    @Autowired
    private ElectricMeterInfoService electricMeterInfoService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("分页查询应填充每个账户的已开户电表数量与可开户电表总数")
    void testFindAccountPage_DefaultQuery_ShouldFillMeterCount() {
        PageResult<AccountVo> pageResult = accountBiz.findAccountPage(new AccountQueryVo(), 1, 10);

        assertNotNull(pageResult);
        assertNotNull(pageResult.getList());
        assertFalse(pageResult.getList().isEmpty());

        for (AccountVo accountVo : pageResult.getList()) {
            if (accountVo == null || accountVo.getId() == null) {
                continue;
            }
            int expectedMeterCount = electricMeterInfoService
                    .findList(new ElectricMeterQueryDto().setAccountIds(List.of(accountVo.getId())))
                    .size();
            assertEquals(expectedMeterCount, accountVo.getOpenedMeterCount());

            int expectedTotalOpenableMeterCount = jdbcTemplate.queryForObject(
                    "select count(1) from energy_electric_meter m " +
                            "join energy_account a on a.id = ? and a.is_deleted = false " +
                            "join energy_owner_space_rel r on r.space_id = m.space_id and r.owner_type = a.owner_type and r.owner_id = a.owner_id " +
                            "where m.is_deleted = false",
                    Integer.class,
                    accountVo.getId()
            );
            assertEquals(expectedTotalOpenableMeterCount, accountVo.getTotalOpenableMeterCount());

            Integer balanceTypeCode = ElectricAccountTypeEnum.QUANTITY.getCode().equals(accountVo.getElectricAccountType())
                    ? BalanceTypeEnum.ELECTRIC_METER.getCode()
                    : BalanceTypeEnum.ACCOUNT.getCode();
            BigDecimal expectedElectricBalanceAmount = jdbcTemplate.queryForObject(
                    "select coalesce(sum(balance), 0) from energy_account_balance " +
                            "where account_id = ? and balance_type = ? and is_deleted = false",
                    BigDecimal.class,
                    accountVo.getId(),
                    balanceTypeCode
            );
            assertNotNull(accountVo.getElectricBalanceAmount());
            assertEquals(0, expectedElectricBalanceAmount.compareTo(accountVo.getElectricBalanceAmount()));
        }
    }

    @Test
    @DisplayName("分页查询遇到无法识别的账户计费类型时应降级返回0余额而不是报错")
    void testFindAccountPage_WithUnknownElectricAccountType_ShouldFallbackZeroBalance() {
        Integer accountId = 1;
        Integer updateRows = jdbcTemplate.update(
                "update energy_account set electric_account_type = ? where id = ? and is_deleted = false",
                99,
                accountId
        );
        assertEquals(1, updateRows);

        PageResult<AccountVo> pageResult = accountBiz.findAccountPage(new AccountQueryVo(), 1, 10);

        assertNotNull(pageResult);
        assertNotNull(pageResult.getList());
        AccountVo targetAccountVo = pageResult.getList().stream()
                .filter(item -> item != null && accountId.equals(item.getId()))
                .findFirst()
                .orElse(null);
        assertNotNull(targetAccountVo);
        assertEquals(0, BigDecimal.ZERO.compareTo(targetAccountVo.getElectricBalanceAmount()));
    }

    @Test
    @DisplayName("账户详情应返回对应账户的电表列表")
    void testGetAccount_ExistingAccount_ShouldContainMeterList() {
        Integer accountId = 1;

        AccountDetailVo detailVo = accountBiz.getAccount(accountId);

        assertNotNull(detailVo);
        assertEquals(accountId, detailVo.getId());
        BigDecimal expectedAccountElectricBalanceAmount = jdbcTemplate.queryForObject(
                "select balance from energy_account_balance " +
                        "where account_id = ? and balance_type = ? and is_deleted = false",
                BigDecimal.class,
                accountId,
                BalanceTypeEnum.ACCOUNT.getCode()
        );
        assertNotNull(detailVo.getElectricBalanceAmount());
        assertEquals(0, expectedAccountElectricBalanceAmount.compareTo(detailVo.getElectricBalanceAmount()));

        List<AccountMeterVo> meterList = detailVo.getMeterList();
        int actualMeterCount = meterList == null ? 0 : meterList.size();
        int expectedMeterCount = electricMeterInfoService
                .findList(new ElectricMeterQueryDto().setAccountIds(List.of(accountId)))
                .size();
        assertEquals(expectedMeterCount, actualMeterCount);
        assertEquals(expectedMeterCount, detailVo.getOpenedMeterCount());

        int expectedTotalOpenableMeterCount = jdbcTemplate.queryForObject(
                "select count(1) from energy_electric_meter m " +
                        "join energy_account a on a.id = ? and a.is_deleted = false " +
                        "join energy_owner_space_rel r on r.space_id = m.space_id and r.owner_type = a.owner_type and r.owner_id = a.owner_id " +
                        "where m.is_deleted = false",
                Integer.class,
                accountId
        );
        assertEquals(expectedTotalOpenableMeterCount, detailVo.getTotalOpenableMeterCount());

        assertNotNull(meterList);
        AccountMeterVo meter1 = meterList.stream()
                .filter(item -> item != null && Integer.valueOf(1).equals(item.getId()))
                .findFirst()
                .orElse(null);
        assertNotNull(meter1);
        assertEquals(Integer.valueOf(1), meter1.getCt());
        assertEquals("NONE", meter1.getWarnType());
        assertNull(meter1.getWarnTypeName());
        assertEquals("测试空间2", meter1.getSpaceName());
        assertNotNull(meter1.getSpaceParentNames());
        assertNull(meter1.getMeterBalanceAmount());
        assertNull(meter1.getMeterBalanceAmountText());

        AccountMeterVo meter7 = meterList.stream()
                .filter(item -> item != null && Integer.valueOf(7).equals(item.getId()))
                .findFirst()
                .orElse(null);
        assertNotNull(meter7);
        assertNull(meter7.getSpaceName());
        assertNull(meter7.getMeterBalanceAmount());
    }

    @Test
    @DisplayName("按需账户详情应回填电表余额")
    void testGetAccount_QuantityAccount_ShouldFillMeterBalanceAmount() {
        Integer accountId = 3;
        LocalDateTime lastOnlineTime = LocalDateTime.now().minusDays(2).minusMinutes(5);
        jdbcTemplate.update(
                "update energy_electric_meter set last_online_time = ? where id = ? and is_deleted = false",
                Timestamp.valueOf(lastOnlineTime),
                3
        );

        AccountDetailVo detailVo = accountBiz.getAccount(accountId);

        assertNotNull(detailVo);
        assertEquals(accountId, detailVo.getId());
        assertEquals(ElectricAccountTypeEnum.QUANTITY.getCode(), detailVo.getElectricAccountType());
        BigDecimal expectedAccountElectricBalanceAmount = jdbcTemplate.queryForObject(
                "select coalesce(sum(balance), 0) from energy_account_balance " +
                        "where account_id = ? and balance_type = ? and is_deleted = false",
                BigDecimal.class,
                accountId,
                BalanceTypeEnum.ELECTRIC_METER.getCode()
        );
        assertNotNull(detailVo.getElectricBalanceAmount());
        assertEquals(0, expectedAccountElectricBalanceAmount.compareTo(detailVo.getElectricBalanceAmount()));
        assertNotNull(detailVo.getMeterList());
        assertEquals(1, detailVo.getMeterList().size());

        AccountMeterVo meterVo = detailVo.getMeterList().get(0);
        assertNotNull(meterVo);
        assertEquals(Integer.valueOf(3), meterVo.getId());
        assertEquals(Integer.valueOf(1), meterVo.getCt());
        assertEquals("测试空间3", meterVo.getSpaceName());
        assertNotNull(meterVo.getSpaceParentNames());

        BigDecimal expectedMeterBalanceAmount = jdbcTemplate.queryForObject(
                "select balance from energy_account_balance " +
                        "where account_id = ? and balance_type = ? and balance_relation_id = ? and is_deleted = false",
                BigDecimal.class,
                accountId,
                BalanceTypeEnum.ELECTRIC_METER.getCode(),
                meterVo.getId()
        );
        assertNotNull(expectedMeterBalanceAmount);
        assertNotNull(meterVo.getMeterBalanceAmount());
        assertEquals(0, expectedMeterBalanceAmount.compareTo(meterVo.getMeterBalanceAmount()));
        assertNull(meterVo.getMeterBalanceAmountText());
        assertEquals("2天", meterVo.getOfflineDurationText());
    }

    @Test
    @DisplayName("按账户名称模糊搜索应直接匹配账户名称")
    void testFindAccountPage_ByOwnerNameLike_ShouldMatchAccountOwnerName() {
        String ownerNameLike = "账户";
        Set<Integer> expectedAccountIdSet = jdbcTemplate.queryForList(
                        "select id from energy_account where is_deleted = false and owner_name like ?",
                        Integer.class,
                        "%" + ownerNameLike + "%"
                )
                .stream()
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        assertFalse(expectedAccountIdSet.isEmpty());

        AccountQueryVo queryVo = new AccountQueryVo();
        queryVo.setOwnerNameLike(ownerNameLike);
        PageResult<AccountVo> pageResult = accountBiz.findAccountPage(
                queryVo,
                1,
                10
        );

        assertNotNull(pageResult);
        assertNotNull(pageResult.getList());
        assertFalse(pageResult.getList().isEmpty());
        assertTrue(pageResult.getList().stream()
                .allMatch(item -> item != null
                        && item.getId() != null
                        && expectedAccountIdSet.contains(item.getId())));
    }

    @Test
    @DisplayName("按账户名称模糊搜索未命中账户时应返回空分页")
    void testFindAccountPage_ByOwnerNameLike_WhenNoMatch_ShouldReturnEmptyPage() {
        AccountQueryVo queryVo = new AccountQueryVo();
        queryVo.setOwnerNameLike("不存在的账户名称");
        PageResult<AccountVo> pageResult = accountBiz.findAccountPage(
                queryVo,
                1,
                10
        );

        assertNotNull(pageResult);
        assertNotNull(pageResult.getList());
        assertTrue(pageResult.getList().isEmpty());
        assertEquals(0L, pageResult.getTotal());
    }
}
