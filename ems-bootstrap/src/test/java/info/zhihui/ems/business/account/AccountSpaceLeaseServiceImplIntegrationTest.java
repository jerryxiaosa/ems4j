package info.zhihui.ems.business.account;

import info.zhihui.ems.business.account.dto.AccountSpaceRentDto;
import info.zhihui.ems.business.account.dto.AccountSpaceUnrentDto;
import info.zhihui.ems.business.account.service.AccountSpaceLeaseService;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.components.context.model.UserRequestData;
import info.zhihui.ems.components.context.setter.RequestContextSetter;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AccountSpaceLeaseService 集成测试
 */
@SpringBootTest
@ActiveProfiles("integrationtest")
@Transactional
class AccountSpaceLeaseServiceImplIntegrationTest {

    @Autowired
    private AccountSpaceLeaseService accountSpaceLeaseService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void tearDown() {
        RequestContextSetter.clear();
    }

    @Test
    @DisplayName("参数校验：spaceIds为空时抛出约束异常")
    void testRentSpaces_Validation_EmptySpaceIds() {
        AccountSpaceRentDto rentDto = new AccountSpaceRentDto()
                .setAccountId(2)
                .setSpaceIds(Collections.emptyList());

        assertThrows(ConstraintViolationException.class, () -> accountSpaceLeaseService.rentSpaces(rentDto));
    }

    @Test
    @DisplayName("租赁空间成功")
    void testRentSpaces_Success() {
        RequestContextSetter.doSet(1, new UserRequestData("测试用户", "13800000001"));

        AccountSpaceRentDto rentDto = new AccountSpaceRentDto()
                .setAccountId(2)
                .setSpaceIds(List.of(1));

        accountSpaceLeaseService.rentSpaces(rentDto);

        assertEquals(1, countAccountSpaceRel(2, 1));
    }

    @Test
    @DisplayName("空间已被其他账户租赁时禁止租赁")
    void testRentSpaces_Conflict() {
        RequestContextSetter.doSet(1, new UserRequestData("测试用户", "13800000001"));

        AccountSpaceRentDto rentDto = new AccountSpaceRentDto()
                .setAccountId(2)
                .setSpaceIds(List.of(101));

        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> accountSpaceLeaseService.rentSpaces(rentDto));

        assertTrue(exception.getMessage().contains("空间已被其他账户租赁"));
        assertEquals(0, countAccountSpaceRel(2, 101));
    }

    @Test
    @DisplayName("空间下存在已开户电表时禁止退租")
    void testUnrentSpaces_BlockedByMeter() {
        RequestContextSetter.doSet(1, new UserRequestData("测试用户", "13800000001"));

        AccountSpaceUnrentDto unrentDto = new AccountSpaceUnrentDto()
                .setAccountId(1)
                .setSpaceIds(List.of(101));

        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> accountSpaceLeaseService.unrentSpaces(unrentDto));

        assertTrue(exception.getMessage().contains("空间下存在已开户电表"));
        assertEquals(1, countAccountSpaceRel(1, 101));
    }

    @Test
    @DisplayName("退租空间成功")
    void testUnrentSpaces_Success() {
        RequestContextSetter.doSet(1, new UserRequestData("测试用户", "13800000001"));
        jdbcTemplate.update("insert into energy_account_space_rel(account_id, space_id) values (?, ?)", 2, 1);
        assertEquals(1, countAccountSpaceRel(2, 1));

        AccountSpaceUnrentDto unrentDto = new AccountSpaceUnrentDto()
                .setAccountId(2)
                .setSpaceIds(List.of(1));

        accountSpaceLeaseService.unrentSpaces(unrentDto);

        assertEquals(0, countAccountSpaceRel(2, 1));
    }

    private Integer countAccountSpaceRel(Integer accountId, Integer spaceId) {
        return jdbcTemplate.queryForObject(
                "select count(1) from energy_account_space_rel where account_id = ? and space_id = ?",
                Integer.class,
                accountId,
                spaceId
        );
    }
}
