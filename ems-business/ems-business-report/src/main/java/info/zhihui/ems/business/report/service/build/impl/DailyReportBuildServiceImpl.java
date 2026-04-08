package info.zhihui.ems.business.report.service.build.impl;

import info.zhihui.ems.business.account.entity.AccountOpenRecordEntity;
import info.zhihui.ems.business.billing.entity.AccountBalanceConsumeRecordEntity;
import info.zhihui.ems.business.billing.entity.ElectricMeterBalanceConsumeRecordEntity;
import info.zhihui.ems.business.billing.entity.ElectricMeterPowerConsumeRecordEntity;
import info.zhihui.ems.business.billing.entity.OrderFlowEntity;
import info.zhihui.ems.business.billing.enums.ConsumeTypeEnum;
import info.zhihui.ems.business.device.entity.MeterCancelRecordEntity;
import info.zhihui.ems.business.device.entity.OpenMeterEntity;
import info.zhihui.ems.business.order.enums.OrderStatusEnum;
import info.zhihui.ems.business.order.enums.OrderTypeEnum;
import info.zhihui.ems.business.report.dto.DailyAccountBuildContextDto;
import info.zhihui.ems.business.report.dto.DailyMeterBuildContextDto;
import info.zhihui.ems.business.report.dto.DailyReportBuildRequestDto;
import info.zhihui.ems.business.report.entity.DailyAccountReportEntity;
import info.zhihui.ems.business.report.entity.DailyMeterReportEntity;
import info.zhihui.ems.business.report.entity.ReportJobLogEntity;
import info.zhihui.ems.business.report.enums.ReportJobStatusEnum;
import info.zhihui.ems.business.report.enums.ReportTriggerTypeEnum;
import info.zhihui.ems.business.report.qo.DailyMeterCandidateQo;
import info.zhihui.ems.business.report.qo.MeterSnapshotSourceQo;
import info.zhihui.ems.business.report.qo.PowerRecordSnapshotSourceQo;
import info.zhihui.ems.business.report.qo.RechargeSourceItemQo;
import info.zhihui.ems.business.report.qo.ReportDateRangeQo;
import info.zhihui.ems.business.report.repository.report.DailyAccountReportRepository;
import info.zhihui.ems.business.report.repository.report.DailyMeterReportRepository;
import info.zhihui.ems.business.report.repository.report.ReportJobLogRepository;
import info.zhihui.ems.business.report.repository.source.ReportAccountSourceRepository;
import info.zhihui.ems.business.report.repository.source.ReportMeterSourceRepository;
import info.zhihui.ems.business.report.repository.source.ReportRechargeSourceRepository;
import info.zhihui.ems.business.report.service.build.DailyReportBuildService;
import info.zhihui.ems.common.enums.BalanceTypeEnum;
import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import info.zhihui.ems.common.enums.MeterTypeEnum;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.components.lock.core.LockTemplate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.locks.Lock;

@Service
@Slf4j
@Validated
@RequiredArgsConstructor
public class DailyReportBuildServiceImpl implements DailyReportBuildService {

    private static final int ACCOUNT_BATCH_SIZE = 200;
    private static final String LOCK_DAILY_REPORT_BUILD = "LOCK:REPORT:DAILY-BUILD";

    private final DailyMeterReportRepository dailyMeterReportRepository;
    private final DailyAccountReportRepository dailyAccountReportRepository;
    private final ReportJobLogRepository reportJobLogRepository;
    private final ReportMeterSourceRepository reportMeterSourceRepository;
    private final ReportAccountSourceRepository reportAccountSourceRepository;
    private final ReportRechargeSourceRepository reportRechargeSourceRepository;
    private final DailyMeterReportBuilder dailyMeterReportBuilder;
    private final DailyAccountReportBuilder dailyAccountReportBuilder;
    private final LockTemplate lockTemplate;

    /**
     * 按请求区间顺序构建日报，并维护任务日志状态。
     *
     * @param buildRequest 构建请求
     */
    @Override
    public void buildDailyReport(@NotNull @Valid DailyReportBuildRequestDto buildRequest) {
        if (buildRequest.getStartDate().isAfter(buildRequest.getEndDate())) {
            throw new BusinessRuntimeException("开始日期不能晚于结束日期");
        }
        Lock lock = lockTemplate.getLock(LOCK_DAILY_REPORT_BUILD);
        if (!lock.tryLock()) {
            throw new BusinessRuntimeException("日报重算任务正在执行，请稍后重试");
        }

        try {
            // 保存任务信息
            ReportJobLogEntity jobLogEntity = new ReportJobLogEntity()
                    .setTriggerType(defaultTriggerType(buildRequest.getTriggerType()))
                    .setStartDate(buildRequest.getStartDate())
                    .setEndDate(buildRequest.getEndDate())
                    .setStatus(ReportJobStatusEnum.RUNNING.getCode())
                    .setTriggerBy(buildRequest.getTriggerBy());
            reportJobLogRepository.insert(jobLogEntity);

            LocalDate currentDate = buildRequest.getStartDate();
            try {
                while (!currentDate.isAfter(buildRequest.getEndDate())) {
                    buildSingleDay(jobLogEntity.getId(), currentDate);
                    currentDate = currentDate.plusDays(1);
                }
                reportJobLogRepository.updateSuccess(jobLogEntity.getId(),
                        ReportJobStatusEnum.SUCCESS.getCode(), LocalDateTime.now());
            } catch (RuntimeException exception) {
                try {
                    reportJobLogRepository.updateFailure(jobLogEntity.getId(),
                            ReportJobStatusEnum.FAILED.getCode(),
                            currentDate,
                            exception.getMessage(),
                            LocalDateTime.now());
                } catch (RuntimeException updateFailureException) {
                    exception.addSuppressed(updateFailureException);
                }
                throw exception;
            }
        } finally {
            lock.unlock();
            log.debug("日报重算任务结束释放全局锁");
        }
    }

    /**
     * 构建单日的电表日报和账户日报。
     *
     * @param jobLogId 任务日志ID
     * @param reportDate 报表日期
     */
    private void buildSingleDay(Integer jobLogId, LocalDate reportDate) {
        // 单日重建前先更新任务进度，并清空当天旧快照。
        reportJobLogRepository.updateStatus(jobLogId, ReportJobStatusEnum.RUNNING.getCode(), reportDate);
        dailyMeterReportRepository.deleteByReportDate(reportDate);
        dailyAccountReportRepository.deleteByReportDate(reportDate);

        ReportDateRangeQo reportDateRange = new ReportDateRangeQo()
                .setReportDate(reportDate)
                .setPreviousReportDate(reportDate.minusDays(1))
                .setBeginTime(reportDate.atStartOfDay())
                .setEndTime(reportDate.plusDays(1).atStartOfDay());

        // 找到要处理的候选账户，然后分批处理
        List<Integer> candidateAccountIdList = findDailyCandidateAccountIdList(reportDateRange);
        for (List<Integer> accountIdBatchList : partitionAccountIdList(candidateAccountIdList)) {
            buildSingleAccountBatch(reportDateRange, reportDate, accountIdBatchList);
        }
    }

    /**
     * 规范化触发类型，未指定时默认按手工触发处理。
     *
     * @param triggerType 触发类型
     * @return 合法的触发类型编码
     */
    private Integer defaultTriggerType(Integer triggerType) {
        if (triggerType == null) {
            return ReportTriggerTypeEnum.MANUAL.getCode();
        }
        if (triggerType.equals(ReportTriggerTypeEnum.MANUAL.getCode())
                || triggerType.equals(ReportTriggerTypeEnum.SCHEDULED.getCode())) {
            return triggerType;
        }
        throw new BusinessRuntimeException("触发类型不合法");
    }

    private List<DailyMeterCandidateQo> buildDailyMeterCandidateList(List<DailyMeterReportEntity> previousDailyMeterReportList,
                                                                     List<PowerRecordSnapshotSourceQo> dailyPowerRecordSnapshotList,
                                                                     List<OpenMeterEntity> dailyOpenRecordList,
                                                                     List<MeterCancelRecordEntity> dailyCancelRecordList,
                                                                     List<RechargeSourceItemQo> dailyMeterRechargeList,
                                                                     List<ElectricMeterBalanceConsumeRecordEntity> dailyCorrectionRecordList) {
        // 先用有序去重集合收集候选键，避免同一账户电表组合在不同来源里重复出现。
        Set<AccountMeterKey> candidateKeySet = new TreeSet<>(Comparator
                .comparing(AccountMeterKey::accountId, Integer::compareTo)
                .thenComparing(AccountMeterKey::meterId, Integer::compareTo));

        // 候选集合来源是“前一日报 + 当日事实”并集：
        // 前一日报负责承接连续零日报，当日电表上报/开户/销户/充值/补正负责补进当天新增或变更的账户电表关系。
        for (DailyMeterReportEntity previousReportEntity : previousDailyMeterReportList) {
            addCandidate(candidateKeySet, previousReportEntity.getAccountId(), previousReportEntity.getMeterId());
        }

        // 电表上报
        for (PowerRecordSnapshotSourceQo powerRecordSnapshotSource : dailyPowerRecordSnapshotList) {
            addCandidate(candidateKeySet, powerRecordSnapshotSource.getAccountId(), powerRecordSnapshotSource.getMeterId());
        }

        // 开户
        for (OpenMeterEntity openMeterEntity : dailyOpenRecordList) {
            addCandidate(candidateKeySet, openMeterEntity.getAccountId(), openMeterEntity.getMeterId());
        }

        // 销户
        for (MeterCancelRecordEntity cancelRecordEntity : dailyCancelRecordList) {
            addCandidate(candidateKeySet, cancelRecordEntity.getAccountId(), cancelRecordEntity.getMeterId());
        }

        // 电表充值到账
        for (RechargeSourceItemQo rechargeSourceItemQo : dailyMeterRechargeList) {
            addCandidate(candidateKeySet, rechargeSourceItemQo.getAccountId(), rechargeSourceItemQo.getMeterId());
        }

        // 补正
        for (ElectricMeterBalanceConsumeRecordEntity correctionRecordEntity : dailyCorrectionRecordList) {
            addCandidate(candidateKeySet, correctionRecordEntity.getAccountId(), correctionRecordEntity.getMeterId());
        }

        // 最后把内部去重键转换成下游构建服务使用的候选 DTO 列表。
        List<DailyMeterCandidateQo> candidateList = new ArrayList<>(candidateKeySet.size());
        for (AccountMeterKey candidateKey : candidateKeySet) {
            candidateList.add(new DailyMeterCandidateQo()
                    .setAccountId(candidateKey.accountId())
                    .setMeterId(candidateKey.meterId()));
        }
        return candidateList;
    }

    /**
     * 构造单日候选账户列表。
     *
     * @param reportDateRange 单日时间范围
     * @return 候选账户列表
     */
    private List<Integer> findDailyCandidateAccountIdList(ReportDateRangeQo reportDateRange) {
        Set<Integer> accountIdSet = new TreeSet<>(Integer::compareTo);
        // 前一日汇总数据仅在账户尚未于统计日开始前全量销户时才继续承接。
        List<Integer> previousAccountIdList = dailyAccountReportRepository.findAccountIdListByReportDate(reportDateRange.getPreviousReportDate());
        if (!previousAccountIdList.isEmpty()) {
            Set<Integer> fullCancelledAccountIdSet = new HashSet<>(
                    reportAccountSourceRepository.findFullCancelledAccountIdListBeforeTime(
                            reportDateRange.getBeginTime(), previousAccountIdList));
            for (Integer accountId : previousAccountIdList) {
                if (!fullCancelledAccountIdSet.contains(accountId)) {
                    accountIdSet.add(accountId);
                }
            }
        }

        // 用电
        accountIdSet.addAll(reportMeterSourceRepository.findDailyPowerRecordAccountIdList(reportDateRange));

        // 金额变动
        accountIdSet.addAll(reportAccountSourceRepository.findDailyAccountOrderFlowAccountIdList(reportDateRange));

        // 开户
        accountIdSet.addAll(reportMeterSourceRepository.findDailyOpenAccountIdList(reportDateRange, MeterTypeEnum.ELECTRIC.getCode()));

        // 销户
        accountIdSet.addAll(reportMeterSourceRepository.findDailyCancelAccountIdList(reportDateRange, MeterTypeEnum.ELECTRIC.getCode()));
        return new ArrayList<>(accountIdSet);
    }

    /**
     * 按账户批次构建单日电表日报和账户日报。
     *
     * @param reportDateRange 单日时间范围
     * @param reportDate 报表日期
     * @param accountIdBatchList 账户批次
     */
    private void buildSingleAccountBatch(ReportDateRangeQo reportDateRange, LocalDate reportDate, List<Integer> accountIdBatchList) {
        Integer startAccountId = accountIdBatchList.get(0);
        Integer endAccountId = accountIdBatchList.get(accountIdBatchList.size() - 1);
        log.info("开始构建日报批次，reportDate={}, accountCount={}, startAccountId={}, endAccountId={}",
                reportDate, accountIdBatchList.size(), startAccountId, endAccountId);

        // ----- 先计算电表的日报 ------
        // 上一日没有销户的电表
        List<DailyMeterReportEntity> previousDailyMeterReportList =
                dailyMeterReportRepository.findActiveListByReportDateAndAccountIdList(reportDate.minusDays(1), accountIdBatchList);
        // 上一日的账户日报
        List<DailyAccountReportEntity> previousDailyAccountReportList =
                dailyAccountReportRepository.findListByReportDateAndAccountIdList(reportDate.minusDays(1), accountIdBatchList);
        // 开户记录
        List<AccountOpenRecordEntity> accountOpenRecordList = reportAccountSourceRepository.findAccountOpenRecordListByAccountIdList(accountIdBatchList);
        Map<Integer, Integer> accountElectricAccountTypeMap = buildAccountElectricAccountTypeMap(accountOpenRecordList, previousDailyAccountReportList);

        // 选定日有用电的电表
        List<PowerRecordSnapshotSourceQo> dailyPowerRecordSnapshotList =
                reportMeterSourceRepository.findDailyPowerRecordSnapshotListByAccountIdList(reportDateRange, accountIdBatchList);

        // 选定日的用电消耗的电表
        List<ElectricMeterPowerConsumeRecordEntity> dailyPowerConsumeRecordList =
                reportMeterSourceRepository.findDailyPowerConsumeRecordListByAccountIdList(reportDateRange, accountIdBatchList);

        // 选定日电表账户有金额变动
        List<ElectricMeterBalanceConsumeRecordEntity> dailyElectricChargeRecordList =
                reportMeterSourceRepository.findDailyBalanceConsumeRecordListByAccountIdList(
                        reportDateRange, ConsumeTypeEnum.ELECTRIC.getCode(), accountIdBatchList);
        // 选定日有补正记录的电表
        List<ElectricMeterBalanceConsumeRecordEntity> dailyCorrectionRecordList =
                reportMeterSourceRepository.findDailyBalanceConsumeRecordListByAccountIdList(
                        reportDateRange, ConsumeTypeEnum.CORRECTION.getCode(), accountIdBatchList);
        CorrectionRecordGroup correctionRecordGroup = splitDailyCorrectionRecordList(dailyCorrectionRecordList, accountElectricAccountTypeMap);
        List<ElectricMeterBalanceConsumeRecordEntity> dailyMeterCorrectionRecordList = correctionRecordGroup.meterCorrectionRecordList();
        List<ElectricMeterBalanceConsumeRecordEntity> dailyAccountCorrectionRecordList = correctionRecordGroup.accountCorrectionRecordList();

        // 选定日有充值的电表
        List<RechargeSourceItemQo> dailyMeterRechargeList =
                reportRechargeSourceRepository.findDailyMeterRechargeListByAccountIdList(
                        reportDateRange,
                        OrderTypeEnum.ENERGY_TOP_UP.getCode(),
                        OrderStatusEnum.SUCCESS.name(),
                        BalanceTypeEnum.ELECTRIC_METER.getCode(),
                        accountIdBatchList);

        // 选定日有开户记录
        List<OpenMeterEntity> dailyOpenRecordList =
                reportMeterSourceRepository.findDailyOpenRecordListByAccountIdList(
                        reportDateRange, MeterTypeEnum.ELECTRIC.getCode(), accountIdBatchList);

        // 选定日有销户记录
        List<MeterCancelRecordEntity> dailyCancelRecordList = reportMeterSourceRepository.findDailyCancelRecordListByAccountIdList(
                        reportDateRange, MeterTypeEnum.ELECTRIC.getCode(), accountIdBatchList);
        // 当前电表快照
        List<MeterSnapshotSourceQo> currentMeterSnapshotList = reportMeterSourceRepository.findCurrentMeterSnapshotListByAccountIdList(accountIdBatchList);
        log.info("电表日报源数据装载结束，reportDate={}, accountCount={}, previousMeterReportCount={}, previousAccountReportCount={}, powerRecordSnapshotCount={}, powerConsumeCount={}, electricChargeCount={}, correctionCount={}, meterCorrectionCount={}, accountCorrectionCount={}, meterRechargeCount={}, openCount={}, cancelCount={}, accountOpenCount={}, currentMeterSnapshotCount={}",
                reportDate,
                accountIdBatchList.size(),
                previousDailyMeterReportList.size(),
                previousDailyAccountReportList.size(),
                dailyPowerRecordSnapshotList.size(),
                dailyPowerConsumeRecordList.size(),
                dailyElectricChargeRecordList.size(),
                dailyCorrectionRecordList.size(),
                dailyMeterCorrectionRecordList.size(),
                dailyAccountCorrectionRecordList.size(),
                dailyMeterRechargeList.size(),
                dailyOpenRecordList.size(),
                dailyCancelRecordList.size(),
                accountOpenRecordList.size(),
                currentMeterSnapshotList.size());

        DailyMeterBuildContextDto dailyMeterBuildContext = new DailyMeterBuildContextDto()
                .setReportDateRange(reportDateRange)
                .setPreviousReportList(previousDailyMeterReportList)
                .setCandidateList(buildDailyMeterCandidateList(previousDailyMeterReportList,
                        dailyPowerRecordSnapshotList,
                        dailyOpenRecordList,
                        dailyCancelRecordList,
                        dailyMeterRechargeList,
                        dailyMeterCorrectionRecordList))
                .setPowerConsumeRecordList(dailyPowerConsumeRecordList)
                .setElectricChargeRecordList(dailyElectricChargeRecordList)
                .setCorrectionRecordList(dailyMeterCorrectionRecordList)
                .setMeterRechargeList(dailyMeterRechargeList)
                .setOpenRecordList(dailyOpenRecordList)
                .setCancelRecordList(dailyCancelRecordList)
                .setPowerRecordSnapshotList(dailyPowerRecordSnapshotList)
                .setAccountOpenRecordList(accountOpenRecordList)
                .setCurrentMeterSnapshotList(currentMeterSnapshotList);

        // 构建单日电表日报
        List<DailyMeterReportEntity> dailyMeterReportList =
                dailyMeterReportBuilder.buildDailyMeterReportList(dailyMeterBuildContext);
        if (!dailyMeterReportList.isEmpty()) {
            dailyMeterReportRepository.insert(dailyMeterReportList);
        }
        log.info("电表日报构建完成，reportDate={}, accountCount={}, meterCandidateCount={}, meterReportCount={}",
                reportDate, accountIdBatchList.size(), dailyMeterBuildContext.getCandidateList().size(), dailyMeterReportList.size());

        // ----- 再计算账户的日报 ------
        // 指定日的账户充值
        List<RechargeSourceItemQo> dailyAccountRechargeSourceList =
                reportRechargeSourceRepository.findDailyAccountRechargeListByAccountIdList(
                        reportDateRange,
                        OrderTypeEnum.ENERGY_TOP_UP.getCode(),
                        OrderStatusEnum.SUCCESS.name(),
                        accountIdBatchList);

        // 指定日的包月消费
        List<AccountBalanceConsumeRecordEntity> dailyMonthlyConsumeRecordList =
                reportAccountSourceRepository.findDailyMonthlyConsumeRecordListByAccountIdList(
                        reportDateRange, ConsumeTypeEnum.MONTHLY.getCode(), accountIdBatchList);


        // 指定日账户余额的变动
        List<OrderFlowEntity> dailyAccountOrderFlowList =
                reportAccountSourceRepository.findDailyAccountOrderFlowListByAccountIdList(
                        reportDateRange, BalanceTypeEnum.ACCOUNT.getCode(), accountIdBatchList);

        log.info("账户日报源数据装载结束，reportDate={}, accountCount={}, previousAccountReportCount={}, accountRechargeSourceCount={}, monthlyConsumeCount={}, accountOrderFlowCount={}, accountOpenCount={}",
                reportDate,
                accountIdBatchList.size(),
                previousDailyAccountReportList.size(),
                dailyAccountRechargeSourceList.size(),
                dailyMonthlyConsumeRecordList.size(),
                dailyAccountOrderFlowList.size(),
                accountOpenRecordList.size());

        DailyAccountBuildContextDto dailyAccountBuildContext = new DailyAccountBuildContextDto()
                .setReportDateRange(reportDateRange)
                .setPreviousReportList(previousDailyAccountReportList)
                .setDailyMeterReportList(dailyMeterReportList)
                .setMonthlyConsumeRecordList(dailyMonthlyConsumeRecordList)
                .setAccountOrderFlowList(dailyAccountOrderFlowList)
                .setAccountCorrectionRecordList(dailyAccountCorrectionRecordList)
                .setAccountRechargeList(buildDailyAccountRechargeList(dailyAccountRechargeSourceList))
                .setRechargeServiceFeeList(buildDailyRechargeServiceFeeList(dailyAccountRechargeSourceList))
                .setAccountOpenRecordList(accountOpenRecordList);
        List<DailyAccountReportEntity> dailyAccountReportList =
                dailyAccountReportBuilder.buildDailyAccountReportList(dailyAccountBuildContext);
        if (!dailyAccountReportList.isEmpty()) {
            dailyAccountReportRepository.insert(dailyAccountReportList);
        }

        log.info("账户日报构建完成，reportDate={}, accountCount={}, meterReportCount={}, accountReportCount={}",
                reportDate, accountIdBatchList.size(), dailyMeterReportList.size(), dailyAccountReportList.size());
    }

    /**
     * 将候选账户列表按固定批次大小拆分。
     *
     * @param candidateAccountIdList 候选账户列表
     * @return 批次列表
     */
    private List<List<Integer>> partitionAccountIdList(List<Integer> candidateAccountIdList) {
        List<List<Integer>> accountBatchList = new ArrayList<>();
        for (int startIndex = 0; startIndex < candidateAccountIdList.size(); startIndex += ACCOUNT_BATCH_SIZE) {
            int endIndex = Math.min(startIndex + ACCOUNT_BATCH_SIZE, candidateAccountIdList.size());
            accountBatchList.add(new ArrayList<>(candidateAccountIdList.subList(startIndex, endIndex)));
        }
        return accountBatchList;
    }

    /**
     * 将基础充值到账事实转换为账户日报使用的充值列表。
     *
     * @param rechargeSourceItemList 基础充值到账事实
     * @return 账户充值列表
     */
    private List<RechargeSourceItemQo> buildDailyAccountRechargeList(List<RechargeSourceItemQo> rechargeSourceItemList) {
        List<RechargeSourceItemQo> accountRechargeList = new ArrayList<>(rechargeSourceItemList.size());
        for (RechargeSourceItemQo rechargeSourceItemQo : rechargeSourceItemList) {
            accountRechargeList.add(new RechargeSourceItemQo()
                    .setOrderSn(rechargeSourceItemQo.getOrderSn())
                    .setAccountId(rechargeSourceItemQo.getAccountId())
                    .setMeterId(Objects.equals(rechargeSourceItemQo.getBalanceType(), BalanceTypeEnum.ELECTRIC_METER.getCode())
                            ? rechargeSourceItemQo.getMeterId()
                            : null)
                    .setBalanceType(rechargeSourceItemQo.getBalanceType())
                    .setAmount(rechargeSourceItemQo.getAmount())
                    .setServiceAmount(rechargeSourceItemQo.getServiceAmount())
                    .setBeginBalance(rechargeSourceItemQo.getBeginBalance())
                    .setEndBalance(rechargeSourceItemQo.getEndBalance())
                    .setCreateTime(rechargeSourceItemQo.getCreateTime()));
        }
        // 充值明细统一按“账户 -> 到账时间”排序，保证同一账户下的输出顺序稳定。
        accountRechargeList.sort(Comparator
                .comparing(RechargeSourceItemQo::getAccountId, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(RechargeSourceItemQo::getCreateTime, Comparator.nullsLast(LocalDateTime::compareTo)));
        return accountRechargeList;
    }

    /**
     * 将基础充值到账事实转换为账户日报使用的服务费列表。
     *
     * @param rechargeSourceItemList 基础充值到账事实
     * @return 充值服务费列表
     */
    private List<RechargeSourceItemQo> buildDailyRechargeServiceFeeList(List<RechargeSourceItemQo> rechargeSourceItemList) {
        List<RechargeSourceItemQo> rechargeServiceFeeList = new ArrayList<>(rechargeSourceItemList.size());
        for (RechargeSourceItemQo rechargeSourceItemQo : rechargeSourceItemList) {
            rechargeServiceFeeList.add(new RechargeSourceItemQo()
                    .setOrderSn(rechargeSourceItemQo.getOrderSn())
                    .setAccountId(rechargeSourceItemQo.getAccountId())
                    .setServiceAmount(rechargeSourceItemQo.getServiceAmount())
                    .setCreateTime(rechargeSourceItemQo.getCreateTime()));
        }
        // 服务费明细统一按“账户 -> 到账时间”排序，保证同一账户下的输出顺序稳定。
        rechargeServiceFeeList.sort(Comparator
                .comparing(RechargeSourceItemQo::getAccountId, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(RechargeSourceItemQo::getCreateTime, Comparator.nullsLast(LocalDateTime::compareTo)));
        return rechargeServiceFeeList;
    }

    private void addCandidate(Set<AccountMeterKey> candidateKeySet, Integer accountId, Integer meterId) {
        if (accountId == null || meterId == null) {
            return;
        }
        candidateKeySet.add(new AccountMeterKey(accountId, meterId));
    }

    private Map<Integer, Integer> buildAccountElectricAccountTypeMap(List<AccountOpenRecordEntity> accountOpenRecordList,
                                                                     List<DailyAccountReportEntity> previousDailyAccountReportList) {
        Map<Integer, Integer> accountElectricAccountTypeMap = new HashMap<>();
        for (AccountOpenRecordEntity accountOpenRecordEntity : accountOpenRecordList) {
            if (accountOpenRecordEntity.getAccountId() != null && accountOpenRecordEntity.getElectricAccountType() != null) {
                accountElectricAccountTypeMap.put(accountOpenRecordEntity.getAccountId(), accountOpenRecordEntity.getElectricAccountType());
            }
        }
        for (DailyAccountReportEntity previousDailyAccountReportEntity : previousDailyAccountReportList) {
            if (previousDailyAccountReportEntity.getAccountId() != null
                    && previousDailyAccountReportEntity.getElectricAccountType() != null) {
                accountElectricAccountTypeMap.putIfAbsent(
                        previousDailyAccountReportEntity.getAccountId(),
                        previousDailyAccountReportEntity.getElectricAccountType());
            }
        }
        return accountElectricAccountTypeMap;
    }

    private CorrectionRecordGroup splitDailyCorrectionRecordList(List<ElectricMeterBalanceConsumeRecordEntity> dailyCorrectionRecordList,
                                                                 Map<Integer, Integer> accountElectricAccountTypeMap) {
        List<ElectricMeterBalanceConsumeRecordEntity> meterCorrectionRecordList = new ArrayList<>();
        List<ElectricMeterBalanceConsumeRecordEntity> accountCorrectionRecordList = new ArrayList<>();
        for (ElectricMeterBalanceConsumeRecordEntity correctionRecordEntity : dailyCorrectionRecordList) {
            Integer electricAccountType = accountElectricAccountTypeMap.get(correctionRecordEntity.getAccountId());
            if (Objects.equals(electricAccountType, ElectricAccountTypeEnum.QUANTITY.getCode())) {
                meterCorrectionRecordList.add(correctionRecordEntity);
                continue;
            }
            if (Objects.equals(electricAccountType, ElectricAccountTypeEnum.MERGED.getCode())) {
                accountCorrectionRecordList.add(correctionRecordEntity);
                continue;
            }
            if (Objects.equals(electricAccountType, ElectricAccountTypeEnum.MONTHLY.getCode())) {
                log.warn("包月账户存在补正记录，reportDate={}, accountId={}, meterId={}",
                        correctionRecordEntity.getMeterConsumeTime() == null ? null : correctionRecordEntity.getMeterConsumeTime().toLocalDate(),
                        correctionRecordEntity.getAccountId(),
                        correctionRecordEntity.getMeterId());
                continue;
            }
            log.warn("补正记录缺少账户类型，跳过处理，reportDate={}, accountId={}, meterId={}",
                    correctionRecordEntity.getMeterConsumeTime() == null ? null : correctionRecordEntity.getMeterConsumeTime().toLocalDate(),
                    correctionRecordEntity.getAccountId(),
                    correctionRecordEntity.getMeterId());
        }
        return new CorrectionRecordGroup(meterCorrectionRecordList, accountCorrectionRecordList);
    }

    private record AccountMeterKey(Integer accountId, Integer meterId) {
    }

    private record CorrectionRecordGroup(List<ElectricMeterBalanceConsumeRecordEntity> meterCorrectionRecordList,
                                         List<ElectricMeterBalanceConsumeRecordEntity> accountCorrectionRecordList) {
    }
}
