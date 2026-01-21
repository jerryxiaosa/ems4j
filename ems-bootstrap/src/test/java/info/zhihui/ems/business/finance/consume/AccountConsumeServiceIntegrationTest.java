package info.zhihui.ems.business.finance.consume;

import info.zhihui.ems.business.finance.bo.BalanceBo;
import info.zhihui.ems.business.finance.dto.AccountConsumeQueryDto;
import info.zhihui.ems.business.finance.dto.AccountConsumeRecordDto;
import info.zhihui.ems.business.finance.dto.BalanceQueryDto;
import info.zhihui.ems.business.finance.dto.MonthlyConsumeDto;
import info.zhihui.ems.business.finance.entity.AccountBalanceConsumeRecordEntity;
import info.zhihui.ems.business.finance.enums.ConsumeTypeEnum;
import info.zhihui.ems.business.finance.repository.AccountBalanceConsumeRecordRepository;
import info.zhihui.ems.business.finance.service.balance.BalanceService;
import info.zhihui.ems.business.finance.service.consume.AccountConsumeService;
import info.zhihui.ems.common.enums.BalanceTypeEnum;
import info.zhihui.ems.common.enums.OwnerTypeEnum;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AccountConsumeService 集成测试
 */
@SpringBootTest
@ActiveProfiles("integrationtest")
@Transactional
class AccountConsumeServiceIntegrationTest {

    @Autowired
    private AccountConsumeService accountConsumeService;

    @Autowired
    private AccountBalanceConsumeRecordRepository accountBalanceConsumeRecordRepository;

    @Autowired
    private BalanceService balanceService;

    private MonthlyConsumeDto monthlyConsumeDto;

    @BeforeEach
    void setUp() {
        monthlyConsumeDto = new MonthlyConsumeDto()
                .setAccountId(1)
                .setOwnerId(1001)
                .setOwnerType(OwnerTypeEnum.PERSONAL)
                .setOwnerName("测试用户")
                .setMonthlyPayAmount(new BigDecimal("50.00"))
                .setConsumeTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("包月消费测试 - 正常包月消费场景")
    void testMonthlyConsume_Normal() {
        BalanceQueryDto queryDto = new BalanceQueryDto()
                .setBalanceRelationId(1)
                .setBalanceType(BalanceTypeEnum.ACCOUNT);
        BalanceBo balanceBefore = balanceService.query(queryDto);
        int recordCountBefore = accountBalanceConsumeRecordRepository.selectList(null).size();

        assertDoesNotThrow(() -> accountConsumeService.monthlyConsume(monthlyConsumeDto));

        BalanceBo balanceAfter = balanceService.query(queryDto);
        assertEquals(balanceBefore.getBalance().subtract(monthlyConsumeDto.getMonthlyPayAmount()),
                balanceAfter.getBalance(), "余额应减少包月金额");

        List<AccountBalanceConsumeRecordEntity> recordsAfter = accountBalanceConsumeRecordRepository.selectList(null);
        assertEquals(recordCountBefore + 1, recordsAfter.size(), "应新增一条包月消费记录");
        AccountBalanceConsumeRecordEntity newRecord = recordsAfter.stream()
                .filter(record -> record.getAccountId().equals(monthlyConsumeDto.getAccountId()))
                .findFirst()
                .orElse(null);
        assertNotNull(newRecord);
        assertEquals(monthlyConsumeDto.getMonthlyPayAmount(),
                newRecord.getPayAmount().setScale(2, RoundingMode.FLOOR));
    }

    @Test
    @DisplayName("包月消费测试 - 同账户同月重复调用应抛异常并不重复变更")
    void testMonthlyConsume_DuplicateSameMonth_ShouldThrowException() {
        Integer accountId = 2099;
        balanceService.initAccountBalance(accountId);
        monthlyConsumeDto.setAccountId(accountId);

        BalanceQueryDto queryDto = new BalanceQueryDto()
                .setBalanceRelationId(accountId)
                .setBalanceType(BalanceTypeEnum.ACCOUNT);
        BalanceBo balanceBefore = balanceService.query(queryDto);

        assertDoesNotThrow(() -> accountConsumeService.monthlyConsume(monthlyConsumeDto));

        BalanceBo balanceAfterFirst = balanceService.query(queryDto);
        assertEquals(balanceBefore.getBalance().subtract(monthlyConsumeDto.getMonthlyPayAmount()),
                balanceAfterFirst.getBalance(), "第一次调用后余额应减少一次");

        BusinessRuntimeException ex = assertThrows(BusinessRuntimeException.class,
                () -> accountConsumeService.monthlyConsume(monthlyConsumeDto));
        assertTrue(ex.getMessage() == null || ex.getMessage().contains("当前账期已扣除月租费，请勿重复操作"));

        BalanceBo balanceAfterSecond = balanceService.query(queryDto);
        assertEquals(balanceAfterFirst.getBalance(), balanceAfterSecond.getBalance(),
                "重复调用不应再次扣费");
    }

    @Test
    @DisplayName("包月消费测试 - 同月存在非包月记录不影响扣费")
    void testMonthlyConsume_WithNonMonthlyRecords_ShouldPass() {
        Integer accountId = 2101;
        balanceService.initAccountBalance(accountId);
        monthlyConsumeDto
                .setAccountId(accountId)
                .setConsumeTime(LocalDateTime.of(2024, 6, 10, 10, 0));

        AccountBalanceConsumeRecordEntity correctionRecord = new AccountBalanceConsumeRecordEntity()
                .setConsumeNo("CORR-" + System.nanoTime())
                .setConsumeType(ConsumeTypeEnum.CORRECTION.getCode())
                .setAccountId(accountId)
                .setOwnerId(monthlyConsumeDto.getOwnerId())
                .setOwnerType(monthlyConsumeDto.getOwnerType().getCode())
                .setOwnerName(monthlyConsumeDto.getOwnerName())
                .setPayAmount(new BigDecimal("5.00"))
                .setBeginBalance(new BigDecimal("5.00"))
                .setEndBalance(BigDecimal.ZERO)
                .setConsumeTime(monthlyConsumeDto.getConsumeTime().withDayOfMonth(1))
                .setCreateTime(LocalDateTime.now())
                .setIsDeleted(false);
        accountBalanceConsumeRecordRepository.insert(correctionRecord);

        assertDoesNotThrow(() -> accountConsumeService.monthlyConsume(monthlyConsumeDto));

        BusinessRuntimeException ex = assertThrows(BusinessRuntimeException.class,
                () -> accountConsumeService.monthlyConsume(monthlyConsumeDto));
        assertTrue(ex.getMessage() == null || ex.getMessage().contains("当前账期已扣除月租费"));
    }

    @Test
    @DisplayName("包月消费测试 - 参数校验失败场景")
    void testMonthlyConsume_ValidationFailed() {
        assertThrows(ConstraintViolationException.class,
                () -> accountConsumeService.monthlyConsume(null));

        MonthlyConsumeDto dto1 = new MonthlyConsumeDto()
                .setOwnerId(1001)
                .setOwnerType(OwnerTypeEnum.PERSONAL)
                .setOwnerName("测试用户")
                .setMonthlyPayAmount(new BigDecimal("30.00"))
                .setConsumeTime(LocalDateTime.now());
        assertThrows(ConstraintViolationException.class,
                () -> accountConsumeService.monthlyConsume(dto1), "accountId为空应报错");

        MonthlyConsumeDto dto2 = new MonthlyConsumeDto()
                .setAccountId(1)
                .setOwnerId(1001)
                .setOwnerType(OwnerTypeEnum.PERSONAL)
                .setOwnerName("测试用户")
                .setConsumeTime(LocalDateTime.now());
        assertThrows(ConstraintViolationException.class,
                () -> accountConsumeService.monthlyConsume(dto2), "monthlyPayAmount为空应报错");

        MonthlyConsumeDto dto3 = new MonthlyConsumeDto()
                .setAccountId(1)
                .setOwnerId(1001)
                .setOwnerType(OwnerTypeEnum.PERSONAL)
                .setOwnerName("测试用户")
                .setMonthlyPayAmount(new BigDecimal("30.00"));
        assertThrows(ConstraintViolationException.class,
                () -> accountConsumeService.monthlyConsume(dto3), "consumeTime为空应报错");
    }

    @Test
    @DisplayName("findMonthlyConsumePage方法集成测试 - 参数校验测试")
    void testFindMonthlyConsumePage_ValidationTests_ShouldThrowException() {
        assertThrows(ConstraintViolationException.class,
                () -> accountConsumeService.findAccountConsumePage(null, new PageParam().setPageNum(1).setPageSize(10)));

        assertThrows(ConstraintViolationException.class,
                () -> accountConsumeService.findAccountConsumePage(new AccountConsumeQueryDto(), null));

        assertDoesNotThrow(() ->
                accountConsumeService.findAccountConsumePage(new AccountConsumeQueryDto(),
                        new PageParam().setPageNum(1).setPageSize(10)));
    }

    @Test
    @DisplayName("findMonthlyConsumePage方法集成测试 - 正常分页查询测试")
    void testFindMonthlyConsumePage_Success() {
        prepareTestDataForMonthlyConsumePage();
        PageResult<AccountConsumeRecordDto> result = accountConsumeService.findAccountConsumePage(
                new AccountConsumeQueryDto(),
                new PageParam().setPageNum(1).setPageSize(10));

        assertNotNull(result);
        assertEquals(3L, result.getTotal());
        assertFalse(result.getList().isEmpty());
    }

    @Test
    @DisplayName("findMonthlyConsumePage方法集成测试 - 空结果查询测试")
    void testFindMonthlyConsumePage_EmptyResult() {
        prepareTestDataForMonthlyConsumePage();
        AccountConsumeQueryDto queryDto = new AccountConsumeQueryDto()
                .setAccountId(999)
                .setConsumeNo("NOT_EXIST");
        PageResult<AccountConsumeRecordDto> result = accountConsumeService.findAccountConsumePage(
                queryDto,
                new PageParam().setPageNum(1).setPageSize(10));

        assertNotNull(result);
        assertTrue(result.getList().isEmpty());
        assertEquals(0L, result.getTotal());
    }

    @Test
    @DisplayName("findMonthlyConsumePage方法集成测试 - 查询条件测试")
    void testFindMonthlyConsumePage_QueryConditions() {
        prepareTestDataForMonthlyConsumePage();
        PageParam pageParam = new PageParam().setPageNum(1).setPageSize(10);

        PageResult<AccountConsumeRecordDto> accountIdResult =
                accountConsumeService.findAccountConsumePage(new AccountConsumeQueryDto().setAccountId(1), pageParam);
        assertEquals(2L, accountIdResult.getTotal());

        PageResult<AccountConsumeRecordDto> consumeNoResult =
                accountConsumeService.findAccountConsumePage(new AccountConsumeQueryDto().setConsumeNo("AM_002"), pageParam);
        assertEquals(1L, consumeNoResult.getTotal());

        AccountConsumeQueryDto timeQuery = new AccountConsumeQueryDto()
                .setConsumeTimeStart(LocalDateTime.of(2024, 1, 15, 0, 0))
                .setConsumeTimeEnd(LocalDateTime.of(2024, 1, 16, 23, 59));
        PageResult<AccountConsumeRecordDto> timeResult =
                accountConsumeService.findAccountConsumePage(timeQuery, pageParam);
        assertEquals(2L, timeResult.getTotal());
    }

    @Test
    @DisplayName("findMonthlyConsumePage方法集成测试 - 分页参数测试")
    void testFindMonthlyConsumePage_Pagination() {
        prepareTestDataForMonthlyConsumePage();
        AccountConsumeQueryDto queryDto = new AccountConsumeQueryDto();

        PageResult<AccountConsumeRecordDto> firstPage = accountConsumeService.findAccountConsumePage(
                queryDto, new PageParam().setPageNum(1).setPageSize(2));
        assertEquals(2, firstPage.getList().size());
        assertEquals(3L, firstPage.getTotal());

        PageResult<AccountConsumeRecordDto> secondPage = accountConsumeService.findAccountConsumePage(
                queryDto, new PageParam().setPageNum(2).setPageSize(2));
        assertEquals(1, secondPage.getList().size());
        assertEquals(3L, secondPage.getTotal());
    }

    private void prepareTestDataForMonthlyConsumePage() {
        accountBalanceConsumeRecordRepository.delete(null);
        LocalDateTime baseTime = LocalDateTime.of(2024, 1, 15, 10, 0);

        AccountBalanceConsumeRecordEntity record1 = new AccountBalanceConsumeRecordEntity()
                .setAccountId(1)
                .setConsumeNo("AM_001")
                .setConsumeType(ConsumeTypeEnum.MONTHLY.getCode())
                .setOwnerId(1001)
                .setOwnerType(OwnerTypeEnum.PERSONAL.getCode())
                .setOwnerName("张三")
                .setPayAmount(new BigDecimal("50.00"))
                .setBeginBalance(new BigDecimal("100.00"))
                .setEndBalance(new BigDecimal("50.00"))
                .setConsumeTime(baseTime)
                .setCreateTime(baseTime)
                .setIsDeleted(false);

        AccountBalanceConsumeRecordEntity record2 = new AccountBalanceConsumeRecordEntity()
                .setAccountId(1)
                .setConsumeNo("AM_002")
                .setConsumeType(ConsumeTypeEnum.MONTHLY.getCode())
                .setOwnerId(1002)
                .setOwnerType(OwnerTypeEnum.ENTERPRISE.getCode())
                .setOwnerName("测试企业")
                .setPayAmount(new BigDecimal("60.00"))
                .setBeginBalance(new BigDecimal("150.00"))
                .setEndBalance(new BigDecimal("90.00"))
                .setConsumeTime(baseTime.plusMonths(1))
                .setCreateTime(baseTime.plusMonths(1))
                .setIsDeleted(false);

        AccountBalanceConsumeRecordEntity record3 = new AccountBalanceConsumeRecordEntity()
                .setAccountId(2)
                .setConsumeNo("AM_003")
                .setConsumeType(ConsumeTypeEnum.MONTHLY.getCode())
                .setOwnerId(1003)
                .setOwnerType(OwnerTypeEnum.PERSONAL.getCode())
                .setOwnerName("李四")
                .setPayAmount(new BigDecimal("70.00"))
                .setBeginBalance(new BigDecimal("200.00"))
                .setEndBalance(new BigDecimal("130.00"))
                .setConsumeTime(baseTime.plusDays(1))
                .setCreateTime(baseTime.plusDays(1))
                .setIsDeleted(false);

        accountBalanceConsumeRecordRepository.insert(record1);
        accountBalanceConsumeRecordRepository.insert(record2);
        accountBalanceConsumeRecordRepository.insert(record3);
    }
}
