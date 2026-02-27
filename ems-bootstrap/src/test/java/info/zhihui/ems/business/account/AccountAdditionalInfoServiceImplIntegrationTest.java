package info.zhihui.ems.business.account;

import info.zhihui.ems.business.account.dto.AccountCandidateMeterDto;
import info.zhihui.ems.business.account.dto.AccountOwnerInfoDto;
import info.zhihui.ems.business.account.dto.OwnerCandidateMeterQueryDto;
import info.zhihui.ems.business.account.service.AccountAdditionalInfoService;
import info.zhihui.ems.common.enums.OwnerTypeEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * AccountAdditionalInfoService 集成测试
 */
@SpringBootTest
@ActiveProfiles("integrationtest")
@Transactional
class AccountAdditionalInfoServiceImplIntegrationTest {

    @Autowired
    private AccountAdditionalInfoService accountAdditionalInfoService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("批量统计可开户电表总数应返回正确结果")
    void testCountTotalOpenableMeterByAccountOwnerInfoList_WithMixedAccountIds_ShouldReturnExpectedResult() {
        List<AccountOwnerInfoDto> accountOwnerInfoDtoList = Arrays.asList(
                new AccountOwnerInfoDto().setAccountId(1).setOwnerType(OwnerTypeEnum.ENTERPRISE).setOwnerId(1001),
                new AccountOwnerInfoDto().setAccountId(1).setOwnerType(OwnerTypeEnum.ENTERPRISE).setOwnerId(1001),
                new AccountOwnerInfoDto().setAccountId(2).setOwnerType(OwnerTypeEnum.ENTERPRISE).setOwnerId(1002),
                new AccountOwnerInfoDto().setAccountId(3).setOwnerType(OwnerTypeEnum.PERSONAL).setOwnerId(1003),
                new AccountOwnerInfoDto().setAccountId(4).setOwnerType(OwnerTypeEnum.ENTERPRISE).setOwnerId(1004),
                new AccountOwnerInfoDto().setAccountId(999).setOwnerType(null).setOwnerId(null),
                new AccountOwnerInfoDto().setAccountId(null).setOwnerType(OwnerTypeEnum.ENTERPRISE).setOwnerId(1001)
        );

        Map<Integer, Integer> result = accountAdditionalInfoService.countTotalOpenableMeterByAccountOwnerInfoList(accountOwnerInfoDtoList);

        assertNotNull(result);
        assertEquals(5, result.size());

        List<Integer> expectedAccountIdList = List.of(1, 2, 3, 4, 999);
        for (Integer accountId : expectedAccountIdList) {
            Integer expectedCount = jdbcTemplate.queryForObject(
                    "select count(1) " +
                            "from energy_electric_meter m " +
                            "join energy_account a on a.id = ? and a.is_deleted = false " +
                            "join energy_owner_space_rel r on r.space_id = m.space_id and r.owner_type = a.owner_type and r.owner_id = a.owner_id " +
                            "where m.is_deleted = false",
                    Integer.class,
                    accountId
            );
            assertEquals(expectedCount, result.get(accountId));
        }
    }

    @Test
    @DisplayName("查询账户候选电表列表应按租赁空间模糊搜索并过滤预付费与未开户")
    void testFindCandidateMeterList_WithSpaceNameLike_ShouldFilterByRentedSpaceAndMeterStatus() {
        Integer ownerId = 1001;
        Integer ownerType = OwnerTypeEnum.ENTERPRISE.getCode();
        Integer matchedSpaceId = 9001;
        Integer unmatchedSpaceId = 9002;

        jdbcTemplate.update(
                "insert into sys_space(id, name, pid, full_path, type, own_area_id) values (?, ?, ?, ?, ?, ?)",
                matchedSpaceId, "候选空间-A1", 1, "1,9001", 3, 1
        );
        jdbcTemplate.update(
                "insert into sys_space(id, name, pid, full_path, type, own_area_id) values (?, ?, ?, ?, ?, ?)",
                unmatchedSpaceId, "其他空间-B1", 1, "1,9002", 3, 1
        );
        jdbcTemplate.update(
                "insert into energy_owner_space_rel(owner_type, owner_id, space_id) values (?, ?, ?)",
                ownerType, ownerId, matchedSpaceId
        );
        jdbcTemplate.update(
                "insert into energy_owner_space_rel(owner_type, owner_id, space_id) values (?, ?, ?)",
                ownerType, ownerId, unmatchedSpaceId
        );

        insertMeter(9001, matchedSpaceId, "候选离线表", "EM-IT-9001", "DEV-IT-9001", false, true, null);
        insertMeter(9002, matchedSpaceId, "已开户表", "EM-IT-9002", "DEV-IT-9002", true, true, 1);
        insertMeter(9003, matchedSpaceId, "非预付费表", "EM-IT-9003", "DEV-IT-9003", true, false, null);
        insertMeter(9004, unmatchedSpaceId, "其他空间候选表", "EM-IT-9004", "DEV-IT-9004", true, true, null);

        List<AccountCandidateMeterDto> result = accountAdditionalInfoService.findCandidateMeterList(
                new OwnerCandidateMeterQueryDto()
                        .setOwnerType(OwnerTypeEnum.ENTERPRISE)
                        .setOwnerId(ownerId)
                        .setSpaceNameLike("A1")
        );

        assertNotNull(result);
        assertEquals(1, result.size());

        AccountCandidateMeterDto candidateMeterDto = result.get(0);
        assertEquals(9001, candidateMeterDto.getId());
        assertEquals("候选离线表", candidateMeterDto.getMeterName());
        assertEquals("EM-IT-9001", candidateMeterDto.getMeterNo());
        assertEquals(matchedSpaceId, candidateMeterDto.getSpaceId());
        assertEquals("候选空间-A1", candidateMeterDto.getSpaceName());
        assertFalse(candidateMeterDto.getIsOnline());
        assertTrue(candidateMeterDto.getIsPrepay());
        assertNotNull(candidateMeterDto.getSpaceParentNames());
        assertFalse(candidateMeterDto.getSpaceParentNames().isEmpty());
        assertTrue(candidateMeterDto.getSpaceParentNames().contains("测试空间"));
    }

    private void insertMeter(Integer id,
                             Integer spaceId,
                             String meterName,
                             String meterNo,
                             String deviceNo,
                             Boolean isOnline,
                             Boolean isPrepay,
                             Integer accountId) {
        jdbcTemplate.update(
                "insert into energy_electric_meter(" +
                        "id, space_id, meter_name, meter_no, device_no, model_id, product_code, communicate_model, " +
                        "is_online, is_prepay, account_id, own_area_id" +
                        ") values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                id,
                spaceId,
                meterName,
                meterNo,
                deviceNo,
                1,
                "DDS102",
                "Modbus",
                isOnline,
                isPrepay,
                accountId,
                1
        );
    }
}
