package info.zhihui.ems.business.finance.service.consume;

import info.zhihui.ems.business.finance.bo.BalanceBo;
import info.zhihui.ems.business.finance.dto.BalanceDto;
import info.zhihui.ems.business.finance.dto.BalanceQueryDto;
import info.zhihui.ems.business.finance.dto.MonthlyConsumeDto;
import info.zhihui.ems.business.finance.entity.AccountBalanceConsumeRecordEntity;
import info.zhihui.ems.business.finance.repository.AccountBalanceConsumeRecordRepository;
import info.zhihui.ems.business.finance.service.balance.BalanceService;
import info.zhihui.ems.business.finance.service.consume.impl.AccountConsumeServiceImpl;
import info.zhihui.ems.common.enums.BalanceTypeEnum;
import info.zhihui.ems.common.enums.OwnerTypeEnum;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.utils.SerialNumberGeneratorUtil;
import info.zhihui.ems.components.lock.core.LockTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.locks.Lock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountConsumeServiceImplTest {

    @Mock
    private BalanceService balanceService;

    @Mock
    private AccountBalanceConsumeRecordRepository accountBalanceConsumeRecordRepository;

    @Mock
    private LockTemplate lockTemplate;

    @Mock
    private Lock lock;

    @InjectMocks
    private AccountConsumeServiceImpl accountConsumeService;

    @Test
    void testMonthlyConsume_Success() {
        MonthlyConsumeDto dto = new MonthlyConsumeDto()
                .setAccountId(1)
                .setOwnerId(1)
                .setOwnerType(OwnerTypeEnum.ENTERPRISE)
                .setOwnerName("测试企业")
                .setMonthlyPayAmount(BigDecimal.valueOf(500))
                .setConsumeTime(LocalDateTime.now());

        when(lockTemplate.getLock(any(String.class))).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
        doNothing().when(lock).unlock();

        when(accountBalanceConsumeRecordRepository.countMonthlyConsume(anyInt(), any(), any()))
                .thenReturn(0);
        when(balanceService.query(any(BalanceQueryDto.class)))
                .thenReturn(new BalanceBo().setBalance(BigDecimal.valueOf(1000)));

        assertDoesNotThrow(() -> accountConsumeService.monthlyConsume(dto));

        String expectedOrderNo = SerialNumberGeneratorUtil.genMonthlyOrderNo(1, dto.getConsumeTime());
        verify(balanceService).deduct(argThat((BalanceDto balanceDto) ->
                balanceDto.getBalanceRelationId().equals(1)
                        && balanceDto.getBalanceType() == BalanceTypeEnum.ACCOUNT
                        && balanceDto.getAccountId().equals(1)
                        && expectedOrderNo.equals(balanceDto.getOrderNo())
                        && balanceDto.getAmount().compareTo(BigDecimal.valueOf(500)) == 0));

        verify(balanceService).query(argThat(query ->
                query.getBalanceRelationId().equals(1)
                        && query.getBalanceType() == BalanceTypeEnum.ACCOUNT));

        verify(accountBalanceConsumeRecordRepository).insert(ArgumentMatchers.<AccountBalanceConsumeRecordEntity>argThat(record ->
                expectedOrderNo.equals(record.getConsumeNo())
                        && record.getAccountId().equals(1)
                        && record.getOwnerId().equals(1)
                        && OwnerTypeEnum.ENTERPRISE.getCode().equals(record.getOwnerType())
                        && "测试企业".equals(record.getOwnerName())
                        && record.getPayAmount().compareTo(BigDecimal.valueOf(500)) == 0
                        && record.getBeginBalance().compareTo(BigDecimal.valueOf(1500)) == 0
                        && record.getEndBalance().compareTo(BigDecimal.valueOf(1000)) == 0));
    }

    @Test
    void testMonthlyConsume_DuplicateMonth_ShouldThrowException() {
        MonthlyConsumeDto dto = new MonthlyConsumeDto()
                .setAccountId(2)
                .setOwnerId(2)
                .setOwnerType(OwnerTypeEnum.PERSONAL)
                .setOwnerName("张三")
                .setMonthlyPayAmount(BigDecimal.valueOf(200))
                .setConsumeTime(LocalDateTime.of(2024, 5, 15, 10, 0));

        when(lockTemplate.getLock(any(String.class))).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
        doNothing().when(lock).unlock();

        when(accountBalanceConsumeRecordRepository.countMonthlyConsume(anyInt(), any(), any()))
                .thenReturn(1);

        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> accountConsumeService.monthlyConsume(dto));
        assertEquals("当前账期已扣除月租费，请勿重复操作", exception.getMessage());

        verify(balanceService, never()).deduct(any());
        verify(accountBalanceConsumeRecordRepository, never()).insert(any(AccountBalanceConsumeRecordEntity.class));
    }
}
