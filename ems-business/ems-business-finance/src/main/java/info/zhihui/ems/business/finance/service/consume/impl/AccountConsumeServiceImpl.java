package info.zhihui.ems.business.finance.service.consume.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import info.zhihui.ems.business.finance.dto.BalanceDto;
import info.zhihui.ems.business.finance.dto.BalanceQueryDto;
import info.zhihui.ems.business.finance.dto.MonthlyConsumeDto;
import info.zhihui.ems.business.finance.dto.AccountConsumeQueryDto;
import info.zhihui.ems.business.finance.dto.AccountConsumeRecordDto;
import info.zhihui.ems.business.finance.entity.AccountBalanceConsumeRecordEntity;
import info.zhihui.ems.business.finance.enums.ConsumeTypeEnum;
import info.zhihui.ems.business.finance.qo.AccountConsumeRecordQo;
import info.zhihui.ems.business.finance.repository.AccountBalanceConsumeRecordRepository;
import info.zhihui.ems.business.finance.service.balance.BalanceService;
import info.zhihui.ems.business.finance.service.consume.AccountConsumeService;
import info.zhihui.ems.common.enums.BalanceTypeEnum;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.common.utils.SerialNumberGeneratorUtil;
import info.zhihui.ems.components.lock.core.LockTemplate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Validated
public class AccountConsumeServiceImpl implements AccountConsumeService {

    private final BalanceService balanceService;
    private final AccountBalanceConsumeRecordRepository accountBalanceConsumeRecordRepository;
    private final LockTemplate lockTemplate;

    private static final String LOCK_ACCOUNT = "LOCK:ACCOUNT:%d";

    /**
     * 包月消费，账户每月只能扣除一次
     * @param monthlyConsumeDto 包月消费参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void monthlyConsume(@Valid @NotNull MonthlyConsumeDto monthlyConsumeDto) {
        Lock lock = getAccountLock(monthlyConsumeDto.getAccountId());
        if (!lock.tryLock()) {
            log.warn("账户{}的锁被占用，请稍微重试", monthlyConsumeDto.getAccountId());
            throw new BusinessRuntimeException("账户正在操作，请稍后重试");
        }

        try {
            checkMonthlyConsumeRecord(monthlyConsumeDto.getAccountId(), monthlyConsumeDto.getConsumeTime());

            String consumeNo = SerialNumberGeneratorUtil.genMonthlyOrderNo(
                    monthlyConsumeDto.getAccountId(), monthlyConsumeDto.getConsumeTime());
            balanceService.deduct(new BalanceDto()
                    .setBalanceRelationId(monthlyConsumeDto.getAccountId())
                    .setBalanceType(BalanceTypeEnum.ACCOUNT)
                    .setAccountId(monthlyConsumeDto.getAccountId())
                    .setOrderNo(consumeNo)
                    .setAmount(monthlyConsumeDto.getMonthlyPayAmount()));

            BigDecimal endBalance = balanceService.query(new BalanceQueryDto()
                    .setBalanceRelationId(monthlyConsumeDto.getAccountId())
                    .setBalanceType(BalanceTypeEnum.ACCOUNT)).getBalance();

            accountBalanceConsumeRecordRepository.insert(new AccountBalanceConsumeRecordEntity()
                    .setConsumeNo(consumeNo)
                    .setConsumeType(ConsumeTypeEnum.MONTHLY.getCode())
                    .setAccountId(monthlyConsumeDto.getAccountId())
                    .setOwnerId(monthlyConsumeDto.getOwnerId())
                    .setOwnerType(monthlyConsumeDto.getOwnerType().getCode())
                    .setOwnerName(monthlyConsumeDto.getOwnerName())
                    .setPayAmount(monthlyConsumeDto.getMonthlyPayAmount())
                    .setBeginBalance(endBalance.add(monthlyConsumeDto.getMonthlyPayAmount()))
                    .setEndBalance(endBalance)
                    .setConsumeTime(monthlyConsumeDto.getConsumeTime())
                    .setCreateTime(LocalDateTime.now()));
        } finally {
            lock.unlock();
        }
    }

    private Lock getAccountLock(Integer accountId) {
        return lockTemplate.getLock(String.format(LOCK_ACCOUNT, accountId));
    }

    private void checkMonthlyConsumeRecord(Integer accountId, LocalDateTime consumeTime) {
        if (accountId == null || consumeTime == null) {
            return;
        }
        LocalDate firstDay = consumeTime.toLocalDate().withDayOfMonth(1);
        LocalDateTime monthStart = LocalDateTime.of(firstDay, LocalTime.MIN);
        LocalDateTime monthEnd = monthStart.plusMonths(1);

        Integer count = accountBalanceConsumeRecordRepository.countMonthlyConsume(accountId, monthStart, monthEnd);
        if (count != null && count > 0) {
            throw new BusinessRuntimeException("当前账期已扣除月租费，请勿重复操作");
        }
    }

    /**
     * 分页查询账户消费记录（包月等）
     * @param queryDto 查询参数
     * @param pageParam 分页参数
     * @return 账户消费记录
     */
    @Override
    public PageResult<AccountConsumeRecordDto> findAccountConsumePage(@NotNull AccountConsumeQueryDto queryDto,
                                                                      @NotNull PageParam pageParam) {
        AccountConsumeRecordQo qo = new AccountConsumeRecordQo()
                .setAccountId(queryDto.getAccountId())
                .setConsumeTimeStart(queryDto.getConsumeTimeStart())
                .setConsumeTimeEnd(queryDto.getConsumeTimeEnd())
                .setConsumeNoLike(queryDto.getConsumeNo());

        try (Page<AccountBalanceConsumeRecordEntity> page = PageHelper.startPage(pageParam.getPageNum(), pageParam.getPageSize())) {
            PageInfo<AccountBalanceConsumeRecordEntity> pageInfo = page.doSelectPageInfo(() -> accountBalanceConsumeRecordRepository.selectByQo(qo));
            List<AccountConsumeRecordDto> records = pageInfo.getList().stream()
                    .map(this::convertAccountConsumeToDto)
                    .collect(Collectors.toList());

            return new PageResult<AccountConsumeRecordDto>()
                    .setPageNum(pageParam.getPageNum())
                    .setPageSize(pageParam.getPageSize())
                    .setTotal(pageInfo.getTotal())
                    .setList(records);
        }
    }

    private AccountConsumeRecordDto convertAccountConsumeToDto(AccountBalanceConsumeRecordEntity entity) {
        return new AccountConsumeRecordDto()
                .setId(entity.getId())
                .setAccountId(entity.getAccountId())
                .setConsumeNo(entity.getConsumeNo())
                .setPayAmount(entity.getPayAmount())
                .setBeginBalance(entity.getBeginBalance())
                .setEndBalance(entity.getEndBalance())
                .setConsumeTime(entity.getConsumeTime())
                .setCreateTime(entity.getCreateTime());
    }
}
