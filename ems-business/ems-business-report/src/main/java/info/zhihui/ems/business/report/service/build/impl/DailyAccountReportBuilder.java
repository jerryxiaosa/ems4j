package info.zhihui.ems.business.report.service.build.impl;

import info.zhihui.ems.business.account.entity.AccountOpenRecordEntity;
import info.zhihui.ems.business.billing.entity.AccountBalanceConsumeRecordEntity;
import info.zhihui.ems.business.billing.entity.ElectricMeterBalanceConsumeRecordEntity;
import info.zhihui.ems.business.billing.entity.OrderFlowEntity;
import info.zhihui.ems.business.report.dto.DailyAccountBuildContextDto;
import info.zhihui.ems.business.report.entity.DailyAccountReportEntity;
import info.zhihui.ems.business.report.entity.DailyMeterReportEntity;
import info.zhihui.ems.business.report.qo.RechargeSourceItemQo;
import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import info.zhihui.ems.common.enums.ElectricPricePeriodEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static info.zhihui.ems.business.report.service.build.impl.ReportBuildSupport.*;

@Service
@Validated
class DailyAccountReportBuilder {

    /**
     * 根据单日构建上下文生成账户日报列表。
     */
    public List<DailyAccountReportEntity> buildDailyAccountReportList(@NotNull @Valid DailyAccountBuildContextDto buildContext) {
        // 构建以账户ID索引的一级或者二级的map
        Map<Integer, DailyAccountReportEntity> previousReportMap = indexByKey(buildContext.getPreviousReportList(), DailyAccountReportEntity::getAccountId);
        Map<Integer, List<DailyMeterReportEntity>> dailyMeterReportMap = groupByKey(buildContext.getDailyMeterReportList(), DailyMeterReportEntity::getAccountId);
        Map<Integer, List<AccountBalanceConsumeRecordEntity>> monthlyConsumeRecordMap = groupByKey(buildContext.getMonthlyConsumeRecordList(), AccountBalanceConsumeRecordEntity::getAccountId);
        Map<Integer, List<OrderFlowEntity>> accountOrderFlowMap = groupByKey(buildContext.getAccountOrderFlowList(), OrderFlowEntity::getAccountId);
        Map<Integer, List<ElectricMeterBalanceConsumeRecordEntity>> accountCorrectionRecordMap = groupByKey(
                buildContext.getAccountCorrectionRecordList(),
                ElectricMeterBalanceConsumeRecordEntity::getAccountId);
        Map<Integer, List<RechargeSourceItemQo>> accountRechargeMap = groupByKey(buildContext.getAccountRechargeList(), RechargeSourceItemQo::getAccountId);
        Map<Integer, List<RechargeSourceItemQo>> rechargeServiceFeeMap = groupByKey(buildContext.getRechargeServiceFeeList(), RechargeSourceItemQo::getAccountId);
        Map<Integer, AccountOpenRecordEntity> accountOpenRecordMap = indexByKey(buildContext.getAccountOpenRecordList(), AccountOpenRecordEntity::getAccountId);

        Set<Integer> accountIdSet = buildAccountIdSet(previousReportMap, dailyMeterReportMap,
                monthlyConsumeRecordMap, accountOrderFlowMap, accountCorrectionRecordMap, accountRechargeMap, rechargeServiceFeeMap);
        if (accountIdSet.isEmpty()) {
            return Collections.emptyList();
        }

        List<DailyAccountReportEntity> reportEntityList = new ArrayList<>(accountIdSet.size());
        for (Integer accountId : accountIdSet) {
            reportEntityList.add(buildReportEntity(buildContext, accountId,
                    previousReportMap.get(accountId),
                    dailyMeterReportMap.get(accountId),
                    monthlyConsumeRecordMap.get(accountId),
                    accountOrderFlowMap.get(accountId),
                    accountCorrectionRecordMap.get(accountId),
                    accountRechargeMap.get(accountId),
                    rechargeServiceFeeMap.get(accountId),
                    accountOpenRecordMap.get(accountId)));
        }
        return reportEntityList;
    }

    /**
     * 构建单个账户在统计日内的日报数据。
     */
    private DailyAccountReportEntity buildReportEntity(DailyAccountBuildContextDto buildContext,
                                                       Integer accountId,
                                                       DailyAccountReportEntity previousReportEntity,
                                                       List<DailyMeterReportEntity> dailyMeterReportList,
                                                       List<AccountBalanceConsumeRecordEntity> monthlyConsumeRecordList,
                                                       List<OrderFlowEntity> accountOrderFlowList,
                                                       List<ElectricMeterBalanceConsumeRecordEntity> accountCorrectionRecordList,
                                                       List<RechargeSourceItemQo> accountRechargeList,
                                                       List<RechargeSourceItemQo> rechargeServiceFeeList,
                                                       AccountOpenRecordEntity accountOpenRecordEntity) {
        List<DailyMeterReportEntity> safeDailyMeterReportList = defaultList(dailyMeterReportList);
        List<AccountBalanceConsumeRecordEntity> safeMonthlyConsumeRecordList = defaultList(monthlyConsumeRecordList);
        List<OrderFlowEntity> safeAccountOrderFlowList = defaultList(accountOrderFlowList);
        List<ElectricMeterBalanceConsumeRecordEntity> safeAccountCorrectionRecordList = defaultList(accountCorrectionRecordList);

        // 获取账户类型
        Integer electricAccountType = resolveElectricAccountType(previousReportEntity, accountOpenRecordEntity);
        DailyAccountReportEntity reportEntity = new DailyAccountReportEntity()
                .setReportDate(buildContext.getReportDateRange().getReportDate())
                .setAccountId(accountId)
                .setElectricAccountType(electricAccountType)
                .setMeterCount(countDistinctMeter(safeDailyMeterReportList))
                .setConsumePower(sumMeterPower(safeDailyMeterReportList, ElectricPricePeriodEnum.TOTAL))
                .setConsumePowerHigher(sumMeterPower(safeDailyMeterReportList, ElectricPricePeriodEnum.HIGHER))
                .setConsumePowerHigh(sumMeterPower(safeDailyMeterReportList, ElectricPricePeriodEnum.HIGH))
                .setConsumePowerLow(sumMeterPower(safeDailyMeterReportList, ElectricPricePeriodEnum.LOW))
                .setConsumePowerLower(sumMeterPower(safeDailyMeterReportList, ElectricPricePeriodEnum.LOWER))
                .setConsumePowerDeepLow(sumMeterPower(safeDailyMeterReportList, ElectricPricePeriodEnum.DEEP_LOW))
                .setRechargeServiceFeeAmount(sumRechargeServiceFee(rechargeServiceFeeList));
        reportEntity.setCreateTime(LocalDateTime.now());

        // 填充信息
        applySnapshotFields(reportEntity, previousReportEntity, accountOpenRecordEntity);
        applyElectricChargeFields(reportEntity, electricAccountType, safeDailyMeterReportList);
        applyMonthlyChargeFields(reportEntity, electricAccountType, safeMonthlyConsumeRecordList);
        applyCorrectionFields(reportEntity, electricAccountType, safeDailyMeterReportList, safeAccountCorrectionRecordList);
        applyRechargeFields(reportEntity, electricAccountType, safeDailyMeterReportList, accountRechargeList);
        applyBalanceFields(reportEntity, previousReportEntity, electricAccountType, safeDailyMeterReportList, safeAccountOrderFlowList);
        applyTotalDebitAmount(reportEntity);
        applyAccumulateFields(reportEntity, previousReportEntity);
        return reportEntity;
    }

    /**
     * 填充账户归属快照，优先取账户开户快照，其次承接前一日报。
     */
    private void applySnapshotFields(DailyAccountReportEntity reportEntity,
                                     DailyAccountReportEntity previousReportEntity,
                                     AccountOpenRecordEntity accountOpenRecordEntity) {
        if (accountOpenRecordEntity != null) {
            reportEntity.setOwnerId(accountOpenRecordEntity.getOwnerId());
            reportEntity.setOwnerType(accountOpenRecordEntity.getOwnerType());
            reportEntity.setOwnerName(accountOpenRecordEntity.getOwnerName());
        }
        if (previousReportEntity != null) {
            if (reportEntity.getOwnerId() == null) {
                reportEntity.setOwnerId(previousReportEntity.getOwnerId());
            }
            if (reportEntity.getOwnerType() == null) {
                reportEntity.setOwnerType(previousReportEntity.getOwnerType());
            }
            if (reportEntity.getOwnerName() == null) {
                reportEntity.setOwnerName(previousReportEntity.getOwnerName());
            }
        }
    }

    /**
     * 填充按量电费字段，包月账户不适用时写入 null。
     */
    private void applyElectricChargeFields(DailyAccountReportEntity reportEntity,
                                           Integer electricAccountType,
                                           List<DailyMeterReportEntity> dailyMeterReportList) {
        if (Objects.equals(electricAccountType, ElectricAccountTypeEnum.MONTHLY.getCode())) {
            reportEntity.setElectricChargeAmount(null);
            reportEntity.setElectricChargeAmountHigher(null);
            reportEntity.setElectricChargeAmountHigh(null);
            reportEntity.setElectricChargeAmountLow(null);
            reportEntity.setElectricChargeAmountLower(null);
            reportEntity.setElectricChargeAmountDeepLow(null);
            return;
        }
        reportEntity.setElectricChargeAmount(sumElectricChargeAmount(dailyMeterReportList, ElectricPricePeriodEnum.TOTAL));
        reportEntity.setElectricChargeAmountHigher(sumElectricChargeAmount(dailyMeterReportList, ElectricPricePeriodEnum.HIGHER));
        reportEntity.setElectricChargeAmountHigh(sumElectricChargeAmount(dailyMeterReportList, ElectricPricePeriodEnum.HIGH));
        reportEntity.setElectricChargeAmountLow(sumElectricChargeAmount(dailyMeterReportList, ElectricPricePeriodEnum.LOW));
        reportEntity.setElectricChargeAmountLower(sumElectricChargeAmount(dailyMeterReportList, ElectricPricePeriodEnum.LOWER));
        reportEntity.setElectricChargeAmountDeepLow(sumElectricChargeAmount(dailyMeterReportList, ElectricPricePeriodEnum.DEEP_LOW));
    }

    /**
     * 填充包月扣费金额，仅包月账户生效。
     */
    private void applyMonthlyChargeFields(DailyAccountReportEntity reportEntity,
                                          Integer electricAccountType,
                                          List<AccountBalanceConsumeRecordEntity> monthlyConsumeRecordList) {
        if (!Objects.equals(electricAccountType, ElectricAccountTypeEnum.MONTHLY.getCode())) {
            reportEntity.setMonthlyChargeAmount(null);
            return;
        }
        BigDecimal monthlyChargeAmount = BigDecimal.ZERO;
        for (AccountBalanceConsumeRecordEntity monthlyConsumeRecordEntity : monthlyConsumeRecordList) {
            monthlyChargeAmount = monthlyChargeAmount.add(defaultDecimal(monthlyConsumeRecordEntity.getPayAmount()));
        }
        reportEntity.setMonthlyChargeAmount(monthlyChargeAmount);
    }

    /**
     * 按账户类型汇总补正金额到账户层。
     * 按量账户补正来自电表日报，合并账户补正直接来自账户级补正事实。
     */
    private void applyCorrectionFields(DailyAccountReportEntity reportEntity,
                                       Integer electricAccountType,
                                       List<DailyMeterReportEntity> dailyMeterReportList,
                                       List<ElectricMeterBalanceConsumeRecordEntity> accountCorrectionRecordList) {
        if (Objects.equals(electricAccountType, ElectricAccountTypeEnum.MONTHLY.getCode())) {
            reportEntity.setCorrectionPayAmount(null);
            reportEntity.setCorrectionRefundAmount(null);
            reportEntity.setCorrectionNetAmount(null);
            return;
        }

        BigDecimal correctionPayAmount = BigDecimal.ZERO;
        BigDecimal correctionRefundAmount = BigDecimal.ZERO;
        if (Objects.equals(electricAccountType, ElectricAccountTypeEnum.MERGED.getCode())) {
            for (ElectricMeterBalanceConsumeRecordEntity correctionRecordEntity : accountCorrectionRecordList) {
                BigDecimal consumeAmount = defaultDecimal(correctionRecordEntity.getConsumeAmount());
                if (consumeAmount.signum() >= 0) {
                    correctionPayAmount = correctionPayAmount.add(consumeAmount);
                } else {
                    correctionRefundAmount = correctionRefundAmount.add(consumeAmount.abs());
                }
            }
        } else {
            for (DailyMeterReportEntity dailyMeterReportEntity : dailyMeterReportList) {
                correctionPayAmount = correctionPayAmount.add(defaultDecimal(dailyMeterReportEntity.getCorrectionPayAmount()));
                correctionRefundAmount = correctionRefundAmount.add(defaultDecimal(dailyMeterReportEntity.getCorrectionRefundAmount()));
            }
        }

        reportEntity.setCorrectionPayAmount(correctionPayAmount);
        reportEntity.setCorrectionRefundAmount(correctionRefundAmount);
        reportEntity.setCorrectionNetAmount(correctionPayAmount.subtract(correctionRefundAmount));
    }

    /**
     * 填充充值金额，按量账户直接汇总电表充值，其余账户使用账户充值事实。
     */
    private void applyRechargeFields(DailyAccountReportEntity reportEntity,
                                     Integer electricAccountType,
                                     List<DailyMeterReportEntity> dailyMeterReportList,
                                     List<RechargeSourceItemQo> accountRechargeList) {
        if (Objects.equals(electricAccountType, ElectricAccountTypeEnum.QUANTITY.getCode())) {
            reportEntity.setRechargeAmount(sumMeterRechargeAmount(dailyMeterReportList));
            return;
        }
        BigDecimal rechargeAmount = BigDecimal.ZERO;
        for (RechargeSourceItemQo rechargeSourceItemQo : defaultList(accountRechargeList)) {
            rechargeAmount = rechargeAmount.add(defaultDecimal(rechargeSourceItemQo.getAmount()));
        }
        reportEntity.setRechargeAmount(rechargeAmount);
    }

    /**
     * 填充账户余额，按量账户从电表日报汇总，其余账户从账户流水承接。
     */
    private void applyBalanceFields(DailyAccountReportEntity reportEntity,
                                    DailyAccountReportEntity previousReportEntity,
                                    Integer electricAccountType,
                                    List<DailyMeterReportEntity> dailyMeterReportList,
                                    List<OrderFlowEntity> accountOrderFlowList) {
        if (Objects.equals(electricAccountType, ElectricAccountTypeEnum.QUANTITY.getCode())) {
            BigDecimal beginBalance = BigDecimal.ZERO;
            BigDecimal endBalance = BigDecimal.ZERO;
            boolean hasMeterBalance = false;
            for (DailyMeterReportEntity dailyMeterReportEntity : dailyMeterReportList) {
                if (dailyMeterReportEntity.getBeginBalance() != null) {
                    beginBalance = beginBalance.add(dailyMeterReportEntity.getBeginBalance());
                    hasMeterBalance = true;
                }
                if (dailyMeterReportEntity.getEndBalance() != null) {
                    endBalance = endBalance.add(dailyMeterReportEntity.getEndBalance());
                    hasMeterBalance = true;
                }
            }
            if (hasMeterBalance) {
                reportEntity.setBeginBalance(beginBalance);
                reportEntity.setEndBalance(endBalance);
                return;
            }
            if (previousReportEntity != null) {
                reportEntity.setBeginBalance(previousReportEntity.getEndBalance());
                reportEntity.setEndBalance(previousReportEntity.getEndBalance());
                return;
            }
            reportEntity.setBeginBalance(null);
            reportEntity.setEndBalance(null);
            return;
        }

        if (!accountOrderFlowList.isEmpty()) {
            reportEntity.setBeginBalance(accountOrderFlowList.get(0).getBeginBalance());
            reportEntity.setEndBalance(accountOrderFlowList.get(accountOrderFlowList.size() - 1).getEndBalance());
            return;
        }
        if (previousReportEntity != null) {
            reportEntity.setBeginBalance(previousReportEntity.getEndBalance());
            reportEntity.setEndBalance(previousReportEntity.getEndBalance());
            return;
        }
        reportEntity.setBeginBalance(null);
        reportEntity.setEndBalance(null);
    }

    /**
     * 根据各类费用字段计算账户总支出。
     */
    private void applyTotalDebitAmount(DailyAccountReportEntity reportEntity) {
        BigDecimal totalDebitAmount = defaultDecimal(reportEntity.getElectricChargeAmount())
                .add(defaultDecimal(reportEntity.getMonthlyChargeAmount()))
                .add(defaultDecimal(reportEntity.getRechargeServiceFeeAmount()))
                .add(defaultDecimal(reportEntity.getCorrectionNetAmount()));
        reportEntity.setTotalDebitAmount(totalDebitAmount);
    }

    /**
     * 基于前一日报承接累计值，并叠加当日报值。
     */
    private void applyAccumulateFields(DailyAccountReportEntity reportEntity,
                                       DailyAccountReportEntity previousReportEntity) {
        BigDecimal previousConsumePower = previousReportEntity == null ? BigDecimal.ZERO : defaultDecimal(previousReportEntity.getAccumulateConsumePower());
        BigDecimal previousElectricChargeAmount = previousReportEntity == null ? BigDecimal.ZERO : defaultDecimal(previousReportEntity.getAccumulateElectricChargeAmount());
        BigDecimal previousMonthlyChargeAmount = previousReportEntity == null ? BigDecimal.ZERO : defaultDecimal(previousReportEntity.getAccumulateMonthlyChargeAmount());
        BigDecimal previousCorrectionPayAmount = previousReportEntity == null ? BigDecimal.ZERO : defaultDecimal(previousReportEntity.getAccumulateCorrectionPayAmount());
        BigDecimal previousCorrectionRefundAmount = previousReportEntity == null ? BigDecimal.ZERO : defaultDecimal(previousReportEntity.getAccumulateCorrectionRefundAmount());
        BigDecimal previousRechargeAmount = previousReportEntity == null ? BigDecimal.ZERO : defaultDecimal(previousReportEntity.getAccumulateRechargeAmount());
        BigDecimal previousRechargeServiceFeeAmount = previousReportEntity == null ? BigDecimal.ZERO : defaultDecimal(previousReportEntity.getAccumulateRechargeServiceFeeAmount());
        BigDecimal previousTotalDebitAmount = previousReportEntity == null ? BigDecimal.ZERO : defaultDecimal(previousReportEntity.getAccumulateTotalDebitAmount());

        reportEntity.setAccumulateConsumePower(previousConsumePower.add(defaultDecimal(reportEntity.getConsumePower())));
        reportEntity.setAccumulateElectricChargeAmount(previousElectricChargeAmount.add(defaultDecimal(reportEntity.getElectricChargeAmount())));
        reportEntity.setAccumulateMonthlyChargeAmount(previousMonthlyChargeAmount.add(defaultDecimal(reportEntity.getMonthlyChargeAmount())));
        reportEntity.setAccumulateCorrectionPayAmount(previousCorrectionPayAmount.add(defaultDecimal(reportEntity.getCorrectionPayAmount())));
        reportEntity.setAccumulateCorrectionRefundAmount(previousCorrectionRefundAmount.add(defaultDecimal(reportEntity.getCorrectionRefundAmount())));
        reportEntity.setAccumulateRechargeAmount(previousRechargeAmount.add(defaultDecimal(reportEntity.getRechargeAmount())));
        reportEntity.setAccumulateRechargeServiceFeeAmount(previousRechargeServiceFeeAmount.add(defaultDecimal(reportEntity.getRechargeServiceFeeAmount())));
        reportEntity.setAccumulateTotalDebitAmount(previousTotalDebitAmount.add(defaultDecimal(reportEntity.getTotalDebitAmount())));
    }

    /**
     * 合并所有来源中的账户 ID，得到当天需要生成日报的账户集合。
     */
    private Set<Integer> buildAccountIdSet(Map<Integer, DailyAccountReportEntity> previousReportMap,
                                           Map<Integer, List<DailyMeterReportEntity>> dailyMeterReportMap,
                                           Map<Integer, List<AccountBalanceConsumeRecordEntity>> monthlyConsumeRecordMap,
                                           Map<Integer, List<OrderFlowEntity>> accountOrderFlowMap,
                                           Map<Integer, List<ElectricMeterBalanceConsumeRecordEntity>> accountCorrectionRecordMap,
                                           Map<Integer, List<RechargeSourceItemQo>> accountRechargeMap,
                                           Map<Integer, List<RechargeSourceItemQo>> rechargeServiceFeeMap) {
        Set<Integer> accountIdSet = new TreeSet<>();
        accountIdSet.addAll(previousReportMap.keySet());
        accountIdSet.addAll(dailyMeterReportMap.keySet());
        accountIdSet.addAll(monthlyConsumeRecordMap.keySet());
        accountIdSet.addAll(accountOrderFlowMap.keySet());
        accountIdSet.addAll(accountCorrectionRecordMap.keySet());
        accountIdSet.addAll(accountRechargeMap.keySet());
        accountIdSet.addAll(rechargeServiceFeeMap.keySet());
        return accountIdSet;
    }

    /**
     * 解析账户计费类型，优先级为开户快照、前一日报。
     */
    private Integer resolveElectricAccountType(DailyAccountReportEntity previousReportEntity,
                                               AccountOpenRecordEntity accountOpenRecordEntity) {
        if (accountOpenRecordEntity != null && accountOpenRecordEntity.getElectricAccountType() != null) {
            return accountOpenRecordEntity.getElectricAccountType();
        }
        return previousReportEntity == null ? null : previousReportEntity.getElectricAccountType();
    }

    /**
     * 统计账户当天参与日报汇总的去重电表数量。
     */
    private Integer countDistinctMeter(List<DailyMeterReportEntity> dailyMeterReportList) {
        Set<Integer> meterIdSet = new TreeSet<>();
        for (DailyMeterReportEntity dailyMeterReportEntity : dailyMeterReportList) {
            if (dailyMeterReportEntity.getMeterId() != null) {
                meterIdSet.add(dailyMeterReportEntity.getMeterId());
            }
        }
        return meterIdSet.size();
    }

    /**
     * 汇总充值服务费金额。
     */
    private BigDecimal sumRechargeServiceFee(List<RechargeSourceItemQo> rechargeServiceFeeList) {
        BigDecimal rechargeServiceFeeAmount = BigDecimal.ZERO;
        for (RechargeSourceItemQo rechargeSourceItemQo : defaultList(rechargeServiceFeeList)) {
            rechargeServiceFeeAmount = rechargeServiceFeeAmount.add(defaultDecimal(rechargeSourceItemQo.getServiceAmount()));
        }
        return rechargeServiceFeeAmount;
    }

    /**
     * 汇总电表日报中的用电量字段。
     */
    private BigDecimal sumMeterPower(List<DailyMeterReportEntity> dailyMeterReportList, ElectricPricePeriodEnum pricePeriod) {
        return sumByPricePeriod(dailyMeterReportList, pricePeriod,
                DailyMeterReportEntity::getConsumePower,
                DailyMeterReportEntity::getConsumePowerHigher,
                DailyMeterReportEntity::getConsumePowerHigh,
                DailyMeterReportEntity::getConsumePowerLow,
                DailyMeterReportEntity::getConsumePowerLower,
                DailyMeterReportEntity::getConsumePowerDeepLow);
    }

    /**
     * 汇总电表日报中的按量电费字段。
     */
    private BigDecimal sumElectricChargeAmount(List<DailyMeterReportEntity> dailyMeterReportList, ElectricPricePeriodEnum pricePeriod) {
        return sumByPricePeriod(dailyMeterReportList, pricePeriod,
                DailyMeterReportEntity::getElectricChargeAmount,
                DailyMeterReportEntity::getElectricChargeAmountHigher,
                DailyMeterReportEntity::getElectricChargeAmountHigh,
                DailyMeterReportEntity::getElectricChargeAmountLow,
                DailyMeterReportEntity::getElectricChargeAmountLower,
                DailyMeterReportEntity::getElectricChargeAmountDeepLow);
    }

    /**
     * 汇总电表日报中的充值金额字段。
     */
    private BigDecimal sumMeterRechargeAmount(List<DailyMeterReportEntity> dailyMeterReportList) {
        BigDecimal totalRechargeAmount = BigDecimal.ZERO;
        for (DailyMeterReportEntity dailyMeterReportEntity : dailyMeterReportList) {
            totalRechargeAmount = totalRechargeAmount.add(defaultDecimal(dailyMeterReportEntity.getRechargeAmount()));
        }
        return totalRechargeAmount;
    }

}
