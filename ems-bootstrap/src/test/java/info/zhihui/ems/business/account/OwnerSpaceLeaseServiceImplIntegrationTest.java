package info.zhihui.ems.business.account;

import info.zhihui.ems.business.account.dto.OwnerSpaceRentDto;
import info.zhihui.ems.business.account.dto.OwnerSpaceUnrentDto;
import info.zhihui.ems.business.account.service.OwnerSpaceLeaseService;
import info.zhihui.ems.common.enums.OwnerTypeEnum;
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
 * OwnerSpaceLeaseService 集成测试
 */
@SpringBootTest
@ActiveProfiles("integrationtest")
@Transactional
class OwnerSpaceLeaseServiceImplIntegrationTest {

    @Autowired
    private OwnerSpaceLeaseService ownerSpaceLeaseService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void tearDown() {
        RequestContextSetter.clear();
    }

    @Test
    @DisplayName("参数校验：spaceIds为空时抛出约束异常")
    void testRentSpaces_Validation_EmptySpaceIds() {
        OwnerSpaceRentDto rentDto = new OwnerSpaceRentDto()
                .setOwnerType(OwnerTypeEnum.PERSONAL)
                .setOwnerId(1002)
                .setSpaceIds(Collections.emptyList());

        assertThrows(ConstraintViolationException.class, () -> ownerSpaceLeaseService.rentSpaces(rentDto));
    }

    @Test
    @DisplayName("租赁空间成功")
    void testRentSpaces_Success() {
        RequestContextSetter.doSet(1, new UserRequestData("测试用户", "13800000001"));

        OwnerSpaceRentDto rentDto = new OwnerSpaceRentDto()
                .setOwnerType(OwnerTypeEnum.PERSONAL)
                .setOwnerId(1002)
                .setSpaceIds(List.of(1));

        ownerSpaceLeaseService.rentSpaces(rentDto);

        assertEquals(1, countOwnerSpaceRel(OwnerTypeEnum.PERSONAL.getCode(), 1002, 1));
    }

    @Test
    @DisplayName("空间已被其他账户租赁时禁止租赁")
    void testRentSpaces_Conflict() {
        RequestContextSetter.doSet(1, new UserRequestData("测试用户", "13800000001"));

        OwnerSpaceRentDto rentDto = new OwnerSpaceRentDto()
                .setOwnerType(OwnerTypeEnum.PERSONAL)
                .setOwnerId(1002)
                .setSpaceIds(List.of(101));

        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> ownerSpaceLeaseService.rentSpaces(rentDto));

        assertTrue(exception.getMessage().contains("空间已被其他主体租赁"));
        assertEquals(0, countOwnerSpaceRel(OwnerTypeEnum.PERSONAL.getCode(), 1002, 101));
    }

    @Test
    @DisplayName("空间下存在已开户电表时禁止退租")
    void testUnrentSpaces_BlockedByMeter() {
        RequestContextSetter.doSet(1, new UserRequestData("测试用户", "13800000001"));

        OwnerSpaceUnrentDto unrentDto = new OwnerSpaceUnrentDto()
                .setOwnerType(OwnerTypeEnum.ENTERPRISE)
                .setOwnerId(1001)
                .setSpaceIds(List.of(101));

        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> ownerSpaceLeaseService.unrentSpaces(unrentDto));

        assertTrue(exception.getMessage().contains("空间下存在已开户电表"));
        assertEquals(1, countOwnerSpaceRel(OwnerTypeEnum.ENTERPRISE.getCode(), 1001, 101));
    }

    @Test
    @DisplayName("退租空间成功")
    void testUnrentSpaces_Success() {
        RequestContextSetter.doSet(1, new UserRequestData("测试用户", "13800000001"));
        jdbcTemplate.update("insert into energy_owner_space_rel(owner_type, owner_id, space_id) values (?, ?, ?)",
                OwnerTypeEnum.PERSONAL.getCode(), 1002, 1);
        assertEquals(1, countOwnerSpaceRel(OwnerTypeEnum.PERSONAL.getCode(), 1002, 1));

        OwnerSpaceUnrentDto unrentDto = new OwnerSpaceUnrentDto()
                .setOwnerType(OwnerTypeEnum.PERSONAL)
                .setOwnerId(1002)
                .setSpaceIds(List.of(1));

        ownerSpaceLeaseService.unrentSpaces(unrentDto);

        assertEquals(0, countOwnerSpaceRel(OwnerTypeEnum.PERSONAL.getCode(), 1002, 1));
    }

    private Integer countOwnerSpaceRel(Integer ownerType, Integer ownerId, Integer spaceId) {
        return jdbcTemplate.queryForObject(
                "select count(1) from energy_owner_space_rel where owner_type = ? and owner_id = ? and space_id = ?",
                Integer.class,
                ownerType,
                ownerId,
                spaceId
        );
    }
}
