package info.zhihui.ems.business.account.service.impl;

import info.zhihui.ems.business.account.bo.AccountBo;
import info.zhihui.ems.business.account.dto.*;
import info.zhihui.ems.business.account.entity.AccountCancelRecordEntity;
import info.zhihui.ems.business.account.entity.AccountEntity;
import info.zhihui.ems.business.account.enums.CleanBalanceTypeEnum;
import info.zhihui.ems.business.account.mapper.AccountInfoMapper;
import info.zhihui.ems.business.account.mapper.AccountManagerMapper;
import info.zhihui.ems.business.account.repository.AccountCancelRecordRepository;
import info.zhihui.ems.business.account.repository.AccountRepository;
import info.zhihui.ems.business.account.service.AccountInfoService;
import info.zhihui.ems.business.account.service.AccountManagerService;
import info.zhihui.ems.business.device.bo.ElectricMeterBo;
import info.zhihui.ems.business.device.dto.*;
import info.zhihui.ems.business.device.service.ElectricMeterInfoService;
import info.zhihui.ems.business.device.service.ElectricMeterManagerService;
import info.zhihui.ems.business.finance.bo.BalanceBo;
import info.zhihui.ems.business.finance.dto.BalanceQueryDto;
import info.zhihui.ems.business.finance.dto.MonthlyConsumeDto;
import info.zhihui.ems.business.finance.dto.order.creation.TerminationOrderCreationInfoDto;
import info.zhihui.ems.business.finance.dto.order.creation.TerminationSettlementDto;
import info.zhihui.ems.business.finance.enums.PaymentChannelEnum;
import info.zhihui.ems.business.finance.service.balance.BalanceService;
import info.zhihui.ems.business.finance.service.consume.AccountConsumeService;
import info.zhihui.ems.business.finance.service.order.core.OrderService;
import info.zhihui.ems.business.finance.utils.MoneyUtil;
import info.zhihui.ems.business.plan.service.ElectricPricePlanService;
import info.zhihui.ems.business.plan.service.WarnPlanService;
import info.zhihui.ems.common.constant.SerialNumberConstant;
import info.zhihui.ems.common.enums.BalanceTypeEnum;
import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import info.zhihui.ems.common.enums.OwnerTypeEnum;
import info.zhihui.ems.common.enums.WarnTypeEnum;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.utils.SerialNumberGeneratorUtil;
import info.zhihui.ems.components.context.RequestContext;
import info.zhihui.ems.components.lock.core.LockTemplate;
import info.zhihui.ems.foundation.organization.service.OrganizationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

/**
 * 账户业务管理服务
 *
 * @author jerryxiaosa
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Validated
public class AccountManagerServiceImpl implements AccountManagerService {

    private final AccountInfoService accountInfoService;
    private final AccountManagerMapper mapper;
    private final AccountInfoMapper infoMapper;
    private final AccountRepository repository;
    private final AccountCancelRecordRepository cancelRecordRepository;
    private final ElectricMeterManagerService electricMeterManagerService;
    private final ElectricMeterInfoService electricMeterInfoService;
    private final AccountConsumeService accountConsumeService;
    private final BalanceService balanceService;
    private final OrderService orderService;
    private final ElectricPricePlanService electricPricePlanService;
    private final WarnPlanService warnPlanService;
    private final OrganizationService organizationService;
    private final LockTemplate lockTemplate;
    private final RequestContext requestContext;

    private static final String LOCK_OWNER = "LOCK:OWNER:%d:%d";
    private static final String LOCK_ACCOUNT = "LOCK:ACCOUNT:%d";

    /**
     * 开户
     *
     * @param openAccountDto 开户数据
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer openAccount(@Valid @NotNull OpenAccountDto openAccountDto) {
        log.info("开始开户流程，业主ID: {}, 业主类型: {}, 账户类型: {}",
                openAccountDto.getOwnerId(), openAccountDto.getOwnerType(), openAccountDto.getElectricAccountType());

        Lock lock = getAccountLock(openAccountDto.getOwnerId(), openAccountDto.getOwnerType());
        if (!lock.tryLock()) {
            log.warn("账户锁定失败，业主ID: {}, 业主类型: {}", openAccountDto.getOwnerId(), openAccountDto.getOwnerType());
            throw new BusinessRuntimeException("账户正在操作，请稍后重试");
        }

        try {
            // 处理开户数据，进行开户操作
            AccountBo accountBo = processAccountCreation(openAccountDto);
            log.info("账户创建完成，账户ID: {}", accountBo.getId());

            // 构建电表开户数据
            MeterOpenDto meterOpenDto = buildMeterOpenDto(accountBo, openAccountDto.getElectricMeterList(), openAccountDto.getInheritHistoryPower());
            electricMeterManagerService.openMeterAccount(meterOpenDto);

            log.info("开户流程完成，业主ID: {}, 账户ID: {}", openAccountDto.getOwnerId(), accountBo.getId());
            return accountBo.getId();
        } catch (Exception e) {
            log.error("开户系统异常，业主ID: {}", openAccountDto.getOwnerId(), e);
            throw new BusinessRuntimeException("开户失败：" + e.getMessage());
        } finally {
            lock.unlock();
            log.debug("开户结束释放账户锁，业主ID: {}", openAccountDto.getOwnerId());
        }
    }

    /**
     * 追加电表开户
     *
     * @param accountMetersOpenDto 追加绑定数据
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void appendMeters(@Valid @NotNull AccountMetersOpenDto accountMetersOpenDto) {
        Lock lock = getAccountLock(accountMetersOpenDto.getAccountId());
        if (!lock.tryLock()) {
            throw new BusinessRuntimeException("账户正在操作，请稍后重试");
        }

        try {
            AccountBo accountBo = accountInfoService.getById(accountMetersOpenDto.getAccountId());

            MeterOpenDto meterOpenDto = buildMeterOpenDto(accountBo, accountMetersOpenDto.getElectricMeterList(), accountMetersOpenDto.getInheritHistoryPower());

            electricMeterManagerService.openMeterAccount(meterOpenDto);
        } catch (Exception e) {
            log.error("追加绑定电表系统异常，账户ID: {}", accountMetersOpenDto.getAccountId(), e);
            throw new BusinessRuntimeException("追加电表失败：" + e.getMessage());
        } finally {
            lock.unlock();
            log.debug("追加电表结束释放账户锁，账户ID: {}", accountMetersOpenDto.getAccountId());
        }
    }

    private Lock getAccountLock(Integer ownerId, OwnerTypeEnum ownerType) {
        return lockTemplate.getLock(String.format(LOCK_OWNER, ownerType.getCode(), ownerId));
    }

    private Lock getAccountLock(Integer accountId) {
        return lockTemplate.getLock(String.format(LOCK_ACCOUNT, accountId));
    }

    private AccountBo processAccountCreation(OpenAccountDto openAccountDto) {
        // 校验开户数据逻辑
        checkAccount(openAccountDto);
        AccountBo newAccountBo = saveAccount(openAccountDto);

        // 初始化账户余额
        balanceService.initAccountBalance(newAccountBo.getId());

        // 开户扣费
        payForOpenAccount(newAccountBo);

        return newAccountBo;
    }

    /**
     * 构建电表开户数据
     */
    private MeterOpenDto buildMeterOpenDto(AccountBo accountBo, List<MeterOpenDetailDto> meterOpenDetail, Boolean inheritHistoryPower) {

        MeterOpenDto meterOpenDto = new MeterOpenDto()
                .setAccountId(accountBo.getId())
                .setOwnerId(accountBo.getOwnerId())
                .setOwnerType(accountBo.getOwnerType())
                .setOwnerName(accountBo.getOwnerName())
                .setElectricAccountType(accountBo.getElectricAccountType())
                .setMeterOpenDetail(meterOpenDetail);

        if (ElectricAccountTypeEnum.MONTHLY.equals(accountBo.getElectricAccountType())) {
            meterOpenDto.setElectricPricePlanId(null);
            meterOpenDto.setWarnPlanId(null);
        } else {
            if (accountBo.getElectricPricePlanId() == null) {
                throw new BusinessRuntimeException("账户信息异常，请检查电价方案配置");
            }
            if (ElectricAccountTypeEnum.QUANTITY.equals(accountBo.getElectricAccountType())
                    && accountBo.getWarnPlanId() == null) {
                throw new BusinessRuntimeException("账户信息异常，请检查预警方案配置");
            }

            meterOpenDto.setElectricPricePlanId(accountBo.getElectricPricePlanId());
            if (ElectricAccountTypeEnum.QUANTITY.equals(accountBo.getElectricAccountType())) {
                meterOpenDto.setWarnPlanId(accountBo.getWarnPlanId());
            } else {
                // 合并计量电表上不用记录告警，记录在了账户上
                meterOpenDto.setWarnPlanId(null);
            }
        }

        meterOpenDto.setInheritHistoryPower(inheritHistoryPower != null && inheritHistoryPower);

        return meterOpenDto;
    }

    private void checkAccount(OpenAccountDto openAccountDto) {
        validateAccountExist(openAccountDto);

        if (OwnerTypeEnum.ENTERPRISE.equals(openAccountDto.getOwnerType())) {
            Objects.requireNonNull(organizationService.getDetail(openAccountDto.getOwnerId()));
        }

        validateAccountTypeSpecificParams(openAccountDto);
    }

    private void validateAccountExist(OpenAccountDto openAccountDto) {
        List<AccountBo> accountList = accountInfoService.findList(new AccountQueryDto()
                .setOwnerType(openAccountDto.getOwnerType())
                .setOwnerId(openAccountDto.getOwnerId()));
        if (!CollectionUtils.isEmpty(accountList)) {
            throw new BusinessRuntimeException("账户已存在，请勿重复开户");
        }
    }


    private void validateAccountTypeSpecificParams(OpenAccountDto openAccountDto) {
        ElectricAccountTypeEnum accountType = openAccountDto.getElectricAccountType();
        // 包月
        if (ElectricAccountTypeEnum.MONTHLY.equals(accountType)) {
            if (openAccountDto.getMonthlyPayAmount() == null) {
                throw new BusinessRuntimeException("包月计费月租费不能为空");
            }
            // 保留两位小数
            openAccountDto.setMonthlyPayAmount(MoneyUtil.scaleToCent(openAccountDto.getMonthlyPayAmount()));
        } else {
            // 非包月
            if (openAccountDto.getElectricPricePlanId() == null) {
                throw new BusinessRuntimeException("请配置电价方案");
            }
            Objects.requireNonNull(electricPricePlanService.getDetail(openAccountDto.getElectricPricePlanId()));

            if (openAccountDto.getWarnPlanId() == null) {
                throw new BusinessRuntimeException("非包月计费预警方案不能为空");
            }
            Objects.requireNonNull(warnPlanService.getDetail(openAccountDto.getWarnPlanId()));
        }

    }

    private AccountBo saveAccount(OpenAccountDto openAccountDto) {
        AccountEntity entity = mapper.openAccountDtoToEntity(openAccountDto);

        if (ElectricAccountTypeEnum.MONTHLY.equals(openAccountDto.getElectricAccountType())) {
            // 包月没有电价方案和预警方案
            entity.setElectricPricePlanId(null);
            entity.setWarnPlanId(null);
        } else {
            // 非包月没有月租费
            entity.setMonthlyPayAmount(null);
        }

        // 重置警告等级
        entity.setElectricWarnType(WarnTypeEnum.NONE.name());
        entity.setCreateTime(LocalDateTime.now());

        repository.insert(entity);
        return infoMapper.entityToBo(entity);
    }

    private void payForOpenAccount(AccountBo accountBo) {
        // 如果是包月，扣除首月费用
        if (ElectricAccountTypeEnum.MONTHLY.equals(accountBo.getElectricAccountType())) {
            MonthlyConsumeDto monthlyConsumeDto = new MonthlyConsumeDto()
                    .setAccountId(accountBo.getId())
                    .setOwnerId(accountBo.getOwnerId())
                    .setOwnerName(accountBo.getOwnerName())
                    .setOwnerType(accountBo.getOwnerType())
                    .setMonthlyPayAmount(accountBo.getMonthlyPayAmount())
                    .setConsumeTime(LocalDateTime.now());
            accountConsumeService.monthlyConsume(monthlyConsumeDto);
        }
    }

    /**
     * 更新账户配置
     *
     * @param accountConfigUpdateDto 配置信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAccountConfig(@NotNull @Valid AccountConfigUpdateDto accountConfigUpdateDto) {
        Integer accountId = accountConfigUpdateDto.getAccountId();

        Lock lock = getAccountLock(accountId);
        if (!lock.tryLock()) {
            throw new BusinessRuntimeException("账户正在操作，请稍后重试");
        }

        try {
            checkUpdateInfo(accountConfigUpdateDto);

            AccountBo accountBo = accountInfoService.getById(accountId);

            ElectricAccountTypeEnum accountType = accountBo.getElectricAccountType();
            if (accountType == null) {
                throw new BusinessRuntimeException("账户计费类型缺失，请联系管理员");
            }

            AccountEntity updateEntity = new AccountEntity().setId(accountId);
            boolean needUpdate = ElectricAccountTypeEnum.MONTHLY.equals(accountType)
                    ? applyMonthlyUpdate(accountBo, accountConfigUpdateDto, updateEntity)
                    : applyNotMonthlyUpdate(accountBo, accountConfigUpdateDto, updateEntity);

            if (!needUpdate) {
                log.info("账户配置未发生变化，accountId={}", accountId);
                return;
            }

            repository.updateById(updateEntity);

            // 按需类型的账户还需要更新电表的配置
            updateMeterAccount(accountConfigUpdateDto, accountType);

        } catch (Exception e) {
            log.error("更新账户配置失败，accountId={}", accountId, e);
            throw new BusinessRuntimeException("更新账户配置失败：" + e.getMessage());
        } finally {
            lock.unlock();
            log.debug("更新账户配置结束释放账户锁，账户ID: {}", accountId);
        }
    }

    private void checkUpdateInfo(AccountConfigUpdateDto dto) {
        if (dto.getElectricPricePlanId() == null
                && dto.getWarnPlanId() == null
                && dto.getMonthlyPayAmount() == null) {
            throw new BusinessRuntimeException("请至少提供一个需要更新的配置项");
        }
    }

    private boolean applyMonthlyUpdate(AccountBo accountBo,
                                       AccountConfigUpdateDto dto,
                                       AccountEntity updateEntity) {
        Integer electricPricePlanId = dto.getElectricPricePlanId();
        Integer warnPlanId = dto.getWarnPlanId();
        BigDecimal monthlyPayAmount = dto.getMonthlyPayAmount();

        if (electricPricePlanId != null || warnPlanId != null) {
            throw new BusinessRuntimeException("包月账户不支持设置电价方案或预警方案");
        }
        if (monthlyPayAmount == null) {
            throw new BusinessRuntimeException("请填写包月账户月租费");
        }

        BigDecimal normalizedMonthlyPay = MoneyUtil.scaleToCent(monthlyPayAmount);
        BigDecimal currentMonthlyPay = accountBo.getMonthlyPayAmount();
        if (currentMonthlyPay == null || normalizedMonthlyPay.compareTo(currentMonthlyPay) != 0) {
            updateEntity.setMonthlyPayAmount(normalizedMonthlyPay);
            return true;
        }
        return false;
    }

    private boolean applyNotMonthlyUpdate(AccountBo accountBo,
                                          AccountConfigUpdateDto dto,
                                          AccountEntity updateEntity) {
        Integer electricPricePlanId = dto.getElectricPricePlanId();
        Integer warnPlanId = dto.getWarnPlanId();
        BigDecimal monthlyPayAmount = dto.getMonthlyPayAmount();

        if (monthlyPayAmount != null) {
            throw new BusinessRuntimeException("按量计费账户不支持设置月租费");
        }
        if (electricPricePlanId == null && warnPlanId == null) {
            throw new BusinessRuntimeException("请至少提供电价方案或预警方案更新");
        }

        Integer currentPricePlanId = accountBo.getElectricPricePlanId();
        Integer currentWarnPlanId = accountBo.getWarnPlanId();
        boolean needUpdate = false;

        if (electricPricePlanId != null && !Objects.equals(electricPricePlanId, currentPricePlanId)) {
            electricPricePlanService.getDetail(electricPricePlanId);
            updateEntity.setElectricPricePlanId(electricPricePlanId);
            needUpdate = true;
        }
        if (warnPlanId != null && !Objects.equals(warnPlanId, currentWarnPlanId)) {
            warnPlanService.getDetail(warnPlanId);
            updateEntity.setWarnPlanId(warnPlanId);
            needUpdate = true;
        }

        return needUpdate;
    }

    /**
     * 按需类型的账户，需要更新电表账户配置
     */
    private void updateMeterAccount(AccountConfigUpdateDto accountConfigUpdateDto, ElectricAccountTypeEnum accountType) {
        if (ElectricAccountTypeEnum.QUANTITY.equals(accountType)) {
            List<ElectricMeterBo> meterList = electricMeterInfoService.findList(new ElectricMeterQueryDto().setAccountId(accountConfigUpdateDto.getAccountId()));
            if (CollectionUtils.isEmpty(meterList)) {
                return;
            }

            List<Integer> meterIds = meterList.stream().map(ElectricMeterBo::getId).toList();
            if (accountConfigUpdateDto.getElectricPricePlanId() != null) {
                electricMeterManagerService.setMeterPricePlan(meterIds, accountConfigUpdateDto.getElectricPricePlanId());
            }
            if (accountConfigUpdateDto.getWarnPlanId() != null) {
                electricMeterManagerService.setMeterWarnPlan(new ElectricMeterWarnPlanDto()
                        .setMeterIds(meterIds)
                        .setWarnPlanId(accountConfigUpdateDto.getWarnPlanId())
                );
            }
        }
    }

    /**
     * 销户
     *
     * @param cancelAccountDto 销户数据
     * @return 销户结果
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public CancelAccountResponseDto cancelAccount(@Valid @NotNull CancelAccountDto cancelAccountDto) {
        Lock lock = getAccountLock(cancelAccountDto.getAccountId());
        if (!lock.tryLock()) {
            throw new BusinessRuntimeException("账户正在操作，请稍后重试");
        }

        try {
            // 获取账户信息
            AccountBo accountBo = accountInfoService.getById(cancelAccountDto.getAccountId());

            // 校验销户电表信息
            validateMeterInfo(cancelAccountDto);

            // 销户编号
            String cancelNo = SerialNumberGeneratorUtil.genNoByTime(SerialNumberConstant.CANCEL_ACCOUNT_NO_PREFIX);

            // 执行销表操作
            List<MeterCancelResultDto> meterCancelResultList = closeMeterAccounts(cancelAccountDto, accountBo, cancelNo);

            // 检查是否全部销户
            boolean fullCancel = checkFullCancel(cancelAccountDto.getAccountId());

            // 计算清算余额
            BalanceCalculationResultDto balanceResult = calculateCleanBalance(accountBo, meterCancelResultList, fullCancel);

            // 创建销户记录
            createCancelRecord(cancelAccountDto, accountBo, fullCancel, balanceResult, cancelNo);

            // 创建销户订单
            createOfflineCancelOrder(cancelNo, accountBo, balanceResult, cancelAccountDto, fullCancel);

            // 如果全部销户，删除账户
            if (fullCancel) {
                repository.deleteById(cancelAccountDto.getAccountId());
            }

            return new CancelAccountResponseDto()
                    .setCancelNo(cancelNo)
                    .setCleanBalanceType(balanceResult.getCleanBalanceType())
                    .setAmount(balanceResult.getRealBalance());
        } catch (Exception e) {
            log.error("销户系统异常，账户ID: {}", cancelAccountDto.getAccountId(), e);
            throw new BusinessRuntimeException("销户失败：" + e.getMessage());
        } finally {
            lock.unlock();
            log.debug("销户结束释放账户锁，账户ID: {}", cancelAccountDto.getAccountId());
        }
    }

    /**
     * 校验销户电表信息
     *
     * @param cancelAccountDto 销户请求参数
     * @throws BusinessRuntimeException 当电表信息校验失败时抛出异常
     */
    private void validateMeterInfo(CancelAccountDto cancelAccountDto) {
        List<Integer> meterIds = cancelAccountDto.getMeterList().stream()
                .map(MeterCancelDetailDto::getMeterId)
                .toList();

        List<ElectricMeterBo> electricMeterBoList = electricMeterInfoService.findList(
                new ElectricMeterQueryDto()
                        .setAccountId(cancelAccountDto.getAccountId())
                        .setInIds(meterIds)
        );

        if (CollectionUtils.isEmpty(electricMeterBoList) || electricMeterBoList.size() != meterIds.size()) {
            throw new BusinessRuntimeException("输入的销户电表信息有误，请检查电表ID是否正确");
        }

        // 校验每个电表是否属于当前账户
        for (ElectricMeterBo electricMeterBo : electricMeterBoList) {
            if (!cancelAccountDto.getAccountId().equals(electricMeterBo.getAccountId()) ||
                    !meterIds.contains(electricMeterBo.getId())) {
                throw new BusinessRuntimeException("输入的销户电表账户信息有误，电表ID: " + electricMeterBo.getId());
            }
        }
    }

    /**
     * 执行销表操作
     *
     * @param cancelAccountDto 销户请求参数
     * @param accountBo        账户信息
     * @return 销表后的余额信息列表
     */
    private List<MeterCancelResultDto> closeMeterAccounts(CancelAccountDto cancelAccountDto, AccountBo accountBo, String cancelNo) {
        MeterCancelDto closeMeterDto = new MeterCancelDto()
                .setMeterCloseDetail(cancelAccountDto.getMeterList())
                .setCancelNo(cancelNo)
                .setAccountId(cancelAccountDto.getAccountId())
                .setOwnerId(accountBo.getOwnerId())
                .setOwnerType(accountBo.getOwnerType())
                .setOwnerName(accountBo.getOwnerName())
                .setElectricAccountType(accountBo.getElectricAccountType());

        return electricMeterManagerService.cancelMeterAccount(closeMeterDto);
    }

    /**
     * 检查是否全部销户
     *
     * @param accountId 账户ID
     * @return 是否全部销户
     */
    private boolean checkFullCancel(Integer accountId) {
        List<ElectricMeterBo> existMeterList = electricMeterInfoService.findList(
                new ElectricMeterQueryDto().setAccountId(accountId)
        );
        return CollectionUtils.isEmpty(existMeterList);
    }

    /**
     * 计算清算余额
     *
     * @param accountBo              账户信息
     * @param meterCancelBalanceList 销表余额列表
     * @param fullCancel             是否全部销户
     * @return 余额计算结果
     */
    private BalanceCalculationResultDto calculateCleanBalance(AccountBo accountBo,
                                                              List<MeterCancelResultDto> meterCancelBalanceList,
                                                              boolean fullCancel) {
        BigDecimal allCleanBalance = BigDecimal.ZERO;

        if (ElectricAccountTypeEnum.QUANTITY.equals(accountBo.getElectricAccountType())) {
            // 按量计费：汇总所有电表余额
            if (meterCancelBalanceList != null) {
                allCleanBalance = meterCancelBalanceList.stream()
                        .map(MeterCancelResultDto::getBalance)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
            }
        } else if (fullCancel) {
            // 非按量计费且全部销户：查询账户余额
            BalanceBo balanceBo = balanceService.query(new BalanceQueryDto()
                    .setBalanceRelationId(accountBo.getId())
                    .setBalanceType(BalanceTypeEnum.ACCOUNT)
            );
            if (balanceBo == null || balanceBo.getBalance() == null) {
                throw new BusinessRuntimeException("账户余额数据缺失");
            }
            allCleanBalance = balanceBo.getBalance();
        }

        BigDecimal realBalance = allCleanBalance.setScale(2, RoundingMode.DOWN);
        BigDecimal ignoreBalance = allCleanBalance.subtract(realBalance);
        CleanBalanceTypeEnum cleanBalanceType;
        int realCompare = realBalance.compareTo(BigDecimal.ZERO);
        if (realCompare == 0) {
            cleanBalanceType = CleanBalanceTypeEnum.SKIP;
        } else if (realCompare > 0) {
            cleanBalanceType = CleanBalanceTypeEnum.REFUND;
        } else {
            cleanBalanceType = CleanBalanceTypeEnum.PAY;
        }

        return new BalanceCalculationResultDto(cleanBalanceType, realBalance, ignoreBalance);
    }

    /**
     * 创建销户记录
     *
     * @param cancelAccountDto 销户请求参数
     * @param accountBo        账户信息
     * @param fullCancel       是否全部销户
     * @param balanceResult    余额计算结果
     */
    private void createCancelRecord(CancelAccountDto cancelAccountDto,
                                    AccountBo accountBo,
                                    boolean fullCancel,
                                    BalanceCalculationResultDto balanceResult,
                                    String cancelNo) {
        AccountCancelRecordEntity accountCancelRecordEntity = new AccountCancelRecordEntity()
                .setCancelNo(cancelNo)
                .setAccountId(cancelAccountDto.getAccountId())
                .setElectricMeterAmount(cancelAccountDto.getMeterList().size())
                .setRemark(cancelAccountDto.getRemark())
                .setOwnerId(accountBo.getOwnerId())
                .setOwnerType(accountBo.getOwnerType().getCode())
                .setOwnerName(accountBo.getOwnerName())
                .setElectricAccountType(accountBo.getElectricAccountType().getCode())
                .setFullCancel(fullCancel)
                .setCleanBalanceType(balanceResult.getCleanBalanceType().getCode())
                .setCleanBalanceReal(balanceResult.getRealBalance())
                .setCleanBalanceIgnore(balanceResult.getIgnoreBalance());

        cancelRecordRepository.insert(accountCancelRecordEntity);
    }

    private void createOfflineCancelOrder(String cancelNo, AccountBo accountBo, BalanceCalculationResultDto balanceResult, CancelAccountDto cancelAccountDto, boolean fullCancel) {
        TerminationSettlementDto terminationSettlementDto = new TerminationSettlementDto()
                .setCancelNo(cancelNo)
                .setAccountId(accountBo.getId())
                .setOwnerId(accountBo.getOwnerId())
                .setOwnerType(accountBo.getOwnerType())
                .setOwnerName(accountBo.getOwnerName())
                .setSettlementAmount(balanceResult.getRealBalance())
                .setFullCancel(fullCancel)
                .setElectricAccountType(accountBo.getElectricAccountType())
                .setElectricMeterAmount(cancelAccountDto.getMeterList().size())
                .setMeterIdList(cancelAccountDto.getMeterList().stream().map(MeterCancelDetailDto::getMeterId).collect(Collectors.toList()))
                .setCloseReason(cancelAccountDto.getRemark());

        TerminationOrderCreationInfoDto orderCreationInfoDto = new TerminationOrderCreationInfoDto();
        orderCreationInfoDto.setTerminationInfo(terminationSettlementDto)
                .setUserId(requestContext.getUserId())
                .setUserPhone(requestContext.getUserPhone())
                .setUserRealName(requestContext.getUserRealName())
                .setThirdPartyUserId(requestContext.getUserId().toString())
                .setOrderAmount(balanceResult.getRealBalance())
                .setPaymentChannel(PaymentChannelEnum.OFFLINE);
        orderService.createOrder(orderCreationInfoDto);
    }

}
