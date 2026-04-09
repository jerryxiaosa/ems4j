package info.zhihui.ems.business.report.service.build.impl;

import info.zhihui.ems.business.account.entity.AccountOpenRecordEntity;
import info.zhihui.ems.business.billing.entity.ElectricMeterBalanceConsumeRecordEntity;
import info.zhihui.ems.business.billing.entity.ElectricMeterPowerConsumeRecordEntity;
import info.zhihui.ems.business.device.entity.MeterCancelRecordEntity;
import info.zhihui.ems.business.device.entity.OpenMeterEntity;
import info.zhihui.ems.business.report.dto.DailyMeterBuildContextDto;
import info.zhihui.ems.business.report.entity.DailyMeterReportEntity;
import info.zhihui.ems.business.report.enums.MeterReportGenerateTypeEnum;
import info.zhihui.ems.business.report.qo.DailyMeterCandidateQo;
import info.zhihui.ems.business.report.qo.MeterSnapshotSourceQo;
import info.zhihui.ems.business.report.qo.PowerRecordSnapshotSourceQo;
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
class DailyMeterReportBuilder {

    /**
     * 根据单日构建上下文生成电表日报列表。
     */
    public List<DailyMeterReportEntity> buildDailyMeterReportList(@NotNull @Valid DailyMeterBuildContextDto buildContext) {
        List<DailyMeterCandidateQo> candidateList = defaultList(buildContext.getCandidateList());
        if (candidateList.isEmpty()) {
            return Collections.emptyList();
        }

        // 构建以AccountMeterKey索引的一级或者二级的map
        Map<AccountMeterKey, DailyMeterReportEntity> previousReportMap = indexByKey(
                buildContext.getPreviousReportList(),
                reportEntity -> buildAccountMeterKey(reportEntity.getAccountId(), reportEntity.getMeterId()),
                Objects::nonNull
        );
        Map<AccountMeterKey, List<ElectricMeterPowerConsumeRecordEntity>> powerConsumeMap = groupByKey(
                buildContext.getPowerConsumeRecordList(),
                recordEntity -> buildAccountMeterKey(recordEntity.getAccountId(), recordEntity.getMeterId()),
                Objects::nonNull
        );
        Map<AccountMeterKey, List<ElectricMeterBalanceConsumeRecordEntity>> electricChargeMap = groupByKey(
                buildContext.getElectricChargeRecordList(),
                recordEntity -> buildAccountMeterKey(recordEntity.getAccountId(), recordEntity.getMeterId()),
                Objects::nonNull
        );
        Map<AccountMeterKey, List<ElectricMeterBalanceConsumeRecordEntity>> correctionMap = groupByKey(
                buildContext.getCorrectionRecordList(),
                recordEntity -> buildAccountMeterKey(recordEntity.getAccountId(), recordEntity.getMeterId()),
                Objects::nonNull
        );
        Map<AccountMeterKey, List<RechargeSourceItemQo>> rechargeMap = groupByKey(
                buildContext.getMeterRechargeList(),
                rechargeSourceItem -> buildAccountMeterKey(rechargeSourceItem.getAccountId(), rechargeSourceItem.getMeterId()),
                Objects::nonNull
        );
        Map<AccountMeterKey, List<OpenMeterEntity>> openRecordMap = groupByKey(
                buildContext.getOpenRecordList(),
                openRecordEntity -> buildAccountMeterKey(openRecordEntity.getAccountId(), openRecordEntity.getMeterId()),
                Objects::nonNull
        );
        Map<AccountMeterKey, List<MeterCancelRecordEntity>> cancelRecordMap = groupByKey(
                buildContext.getCancelRecordList(),
                cancelRecordEntity -> buildAccountMeterKey(cancelRecordEntity.getAccountId(), cancelRecordEntity.getMeterId()),
                Objects::nonNull
        );
        Map<AccountMeterKey, List<PowerRecordSnapshotSourceQo>> powerRecordSnapshotMap = groupByKey(
                buildContext.getPowerRecordSnapshotList(),
                powerRecordSnapshot -> buildAccountMeterKey(powerRecordSnapshot.getAccountId(), powerRecordSnapshot.getMeterId()),
                Objects::nonNull
        );
        Map<Integer, AccountOpenRecordEntity> accountOpenRecordMap = indexByKey(
                buildContext.getAccountOpenRecordList(),
                AccountOpenRecordEntity::getAccountId,
                Objects::nonNull
        );
        Map<AccountMeterKey, MeterSnapshotSourceQo> currentMeterSnapshotMap = indexByKey(
                buildContext.getCurrentMeterSnapshotList(),
                meterSnapshot -> buildAccountMeterKey(meterSnapshot.getAccountId(), meterSnapshot.getMeterId()),
                Objects::nonNull
        );

        // 构建候选电表
        List<AccountMeterKey> accountMeterKeyList = buildCandidateKeyList(candidateList);

        List<DailyMeterReportEntity> reportEntityList = new ArrayList<>(accountMeterKeyList.size());
        for (AccountMeterKey accountMeterKey : accountMeterKeyList) {
            reportEntityList.add(buildReportEntity(buildContext, accountMeterKey,
                    previousReportMap.get(accountMeterKey),
                    powerConsumeMap.get(accountMeterKey),
                    electricChargeMap.get(accountMeterKey),
                    correctionMap.get(accountMeterKey),
                    rechargeMap.get(accountMeterKey),
                    openRecordMap.get(accountMeterKey),
                    cancelRecordMap.get(accountMeterKey),
                    powerRecordSnapshotMap.get(accountMeterKey),
                    accountOpenRecordMap.get(accountMeterKey.accountId()),
                    currentMeterSnapshotMap.get(accountMeterKey)));
        }
        return reportEntityList;
    }

    /**
     * 构建单个账户-电表维度在统计日内的日报数据。
     */
    private DailyMeterReportEntity buildReportEntity(DailyMeterBuildContextDto buildContext,
                                                     AccountMeterKey accountMeterKey,
                                                     DailyMeterReportEntity previousReportEntity,
                                                     List<ElectricMeterPowerConsumeRecordEntity> powerConsumeRecordList,
                                                     List<ElectricMeterBalanceConsumeRecordEntity> electricChargeRecordList,
                                                     List<ElectricMeterBalanceConsumeRecordEntity> correctionRecordList,
                                                     List<RechargeSourceItemQo> rechargeSourceItemList,
                                                     List<OpenMeterEntity> openRecordList,
                                                     List<MeterCancelRecordEntity> cancelRecordList,
                                                     List<PowerRecordSnapshotSourceQo> powerRecordSnapshotList,
                                                     AccountOpenRecordEntity accountOpenRecordEntity,
                                                     MeterSnapshotSourceQo currentMeterSnapshot) {
        // 用电记录需要稳定排序，后续会直接取首尾记录作为日电表期初、期末读数来源。
        List<ElectricMeterPowerConsumeRecordEntity> sortedPowerConsumeRecordList = sortedCopy(
                powerConsumeRecordList,
                ElectricMeterPowerConsumeRecordEntity::getMeterConsumeTime,
                ElectricMeterPowerConsumeRecordEntity::getId
        );
        // 扣费记录需要按时间排序，后续会通过 lastOrNull 读取“当天最后一条”来补展示单价和账户类型。
        List<ElectricMeterBalanceConsumeRecordEntity> sortedElectricChargeRecordList = sortedCopy(
                electricChargeRecordList,
                ElectricMeterBalanceConsumeRecordEntity::getMeterConsumeTime,
                ElectricMeterBalanceConsumeRecordEntity::getId
        );
        // 补正记录同样依赖“最后一条即最新记录”的语义，不排序会影响账户类型与快照承接判断。
        List<ElectricMeterBalanceConsumeRecordEntity> sortedCorrectionRecordList = sortedCopy(
                correctionRecordList,
                ElectricMeterBalanceConsumeRecordEntity::getMeterConsumeTime,
                ElectricMeterBalanceConsumeRecordEntity::getId
        );
        // 销户记录需要先排好序，因为快照填充阶段会单独取最后一条销户记录补齐电表与空间信息。
        List<MeterCancelRecordEntity> sortedCancelRecordList = sortedCopy(
                cancelRecordList,
                MeterCancelRecordEntity::getShowTime,
                MeterCancelRecordEntity::getId
        );
        // 电量快照需要按记录时间排序，后续会取最后一条作为“当天最新快照”参与归属与账户类型判定。
        List<PowerRecordSnapshotSourceQo> sortedPowerRecordSnapshotList = sortedCopy(
                powerRecordSnapshotList,
                PowerRecordSnapshotSourceQo::getRecordTime,
                PowerRecordSnapshotSourceQo::getRecordId
        );

        // 解析对应账户的类型
        Integer electricAccountType = resolveElectricAccountType(
                previousReportEntity,
                accountOpenRecordEntity);
        boolean hasCancelRecord = !sortedCancelRecordList.isEmpty();

        DailyMeterReportEntity reportEntity = new DailyMeterReportEntity()
                .setReportDate(buildContext.getReportDateRange().getReportDate())
                .setAccountId(accountMeterKey.accountId())
                .setMeterId(accountMeterKey.meterId())
                .setElectricAccountType(electricAccountType)
                .setGenerateType(resolveGenerateType(hasCancelRecord, !sortedPowerConsumeRecordList.isEmpty()))
                .setConsumePower(sumPower(sortedPowerConsumeRecordList, ElectricPricePeriodEnum.TOTAL))
                .setConsumePowerHigher(sumPower(sortedPowerConsumeRecordList, ElectricPricePeriodEnum.HIGHER))
                .setConsumePowerHigh(sumPower(sortedPowerConsumeRecordList, ElectricPricePeriodEnum.HIGH))
                .setConsumePowerLow(sumPower(sortedPowerConsumeRecordList, ElectricPricePeriodEnum.LOW))
                .setConsumePowerLower(sumPower(sortedPowerConsumeRecordList, ElectricPricePeriodEnum.LOWER))
                .setConsumePowerDeepLow(sumPower(sortedPowerConsumeRecordList, ElectricPricePeriodEnum.DEEP_LOW));
        reportEntity.setCreateTime(LocalDateTime.now());

        // 填充相关的信息
        applySnapshotFields(reportEntity, previousReportEntity, accountOpenRecordEntity, sortedPowerRecordSnapshotList,
                sortedCancelRecordList, currentMeterSnapshot);
        applyPowerFields(reportEntity, previousReportEntity, sortedPowerConsumeRecordList, openRecordList, sortedCancelRecordList);
        applyElectricChargeFields(reportEntity, previousReportEntity, electricAccountType, sortedElectricChargeRecordList);
        applyCorrectionFields(reportEntity, electricAccountType, sortedCorrectionRecordList);
        applyBalanceAndRechargeFields(reportEntity, previousReportEntity, electricAccountType,
                sortedElectricChargeRecordList, sortedCorrectionRecordList, rechargeSourceItemList);
        return reportEntity;
    }

    /**
     * 填充电表归属与空间快照。
     * 账户归属使用稳定的账户开户快照，电表/空间展示字段优先读取当天事实上报，再回退当前电表快照和前一日报。
     */
    private void applySnapshotFields(DailyMeterReportEntity reportEntity,
                                     DailyMeterReportEntity previousReportEntity,
                                     AccountOpenRecordEntity accountOpenRecordEntity,
                                     List<PowerRecordSnapshotSourceQo> powerRecordSnapshotList,
                                     List<MeterCancelRecordEntity> cancelRecordList,
                                     MeterSnapshotSourceQo currentMeterSnapshot) {
        PowerRecordSnapshotSourceQo latestPowerRecordSnapshot = lastOrNull(powerRecordSnapshotList);
        MeterCancelRecordEntity latestCancelRecordEntity = lastOrNull(cancelRecordList);

        if (accountOpenRecordEntity != null) {
            reportEntity.setOwnerId(accountOpenRecordEntity.getOwnerId());
            reportEntity.setOwnerType(accountOpenRecordEntity.getOwnerType());
            reportEntity.setOwnerName(accountOpenRecordEntity.getOwnerName());
        }
        if (latestPowerRecordSnapshot != null) {
            reportEntity.setMeterName(latestPowerRecordSnapshot.getMeterName());
            reportEntity.setDeviceNo(latestPowerRecordSnapshot.getDeviceNo());
            reportEntity.setSpaceId(latestPowerRecordSnapshot.getSpaceId());
            reportEntity.setSpaceName(latestPowerRecordSnapshot.getSpaceName());
        }
        if (latestCancelRecordEntity != null) {
            reportEntity.setMeterName(defaultValue(reportEntity.getMeterName(), latestCancelRecordEntity.getMeterName()));
            reportEntity.setDeviceNo(defaultValue(reportEntity.getDeviceNo(), latestCancelRecordEntity.getDeviceNo()));
            reportEntity.setSpaceId(defaultValue(reportEntity.getSpaceId(), latestCancelRecordEntity.getSpaceId()));
            reportEntity.setSpaceName(defaultValue(reportEntity.getSpaceName(), latestCancelRecordEntity.getSpaceName()));
        }
        if (currentMeterSnapshot != null) {
            reportEntity.setMeterName(defaultValue(reportEntity.getMeterName(), currentMeterSnapshot.getMeterName()));
            reportEntity.setDeviceNo(defaultValue(reportEntity.getDeviceNo(), currentMeterSnapshot.getDeviceNo()));
            reportEntity.setSpaceId(defaultValue(reportEntity.getSpaceId(), currentMeterSnapshot.getSpaceId()));
            reportEntity.setSpaceName(defaultValue(reportEntity.getSpaceName(), currentMeterSnapshot.getSpaceName()));
        }
        if (previousReportEntity != null) {
            reportEntity.setOwnerId(defaultValue(reportEntity.getOwnerId(), previousReportEntity.getOwnerId()));
            reportEntity.setOwnerType(defaultValue(reportEntity.getOwnerType(), previousReportEntity.getOwnerType()));
            reportEntity.setOwnerName(defaultValue(reportEntity.getOwnerName(), previousReportEntity.getOwnerName()));
            reportEntity.setMeterName(defaultValue(reportEntity.getMeterName(), previousReportEntity.getMeterName()));
            reportEntity.setDeviceNo(defaultValue(reportEntity.getDeviceNo(), previousReportEntity.getDeviceNo()));
            reportEntity.setSpaceId(defaultValue(reportEntity.getSpaceId(), previousReportEntity.getSpaceId()));
            reportEntity.setSpaceName(defaultValue(reportEntity.getSpaceName(), previousReportEntity.getSpaceName()));
        }
    }

    /**
     * 填充电表期初期末读数，优先使用当天用电记录，其次承接开户/销户事实或前一日报。
     */
    private void applyPowerFields(DailyMeterReportEntity reportEntity,
                                  DailyMeterReportEntity previousReportEntity,
                                  List<ElectricMeterPowerConsumeRecordEntity> powerConsumeRecordList,
                                  List<OpenMeterEntity> openRecordList,
                                  List<MeterCancelRecordEntity> cancelRecordList) {
        if (!powerConsumeRecordList.isEmpty()) {
            // 当天存在正常用电时，期初/默认期末先以首尾用电记录为准。
            ElectricMeterPowerConsumeRecordEntity firstPowerConsumeRecordEntity = powerConsumeRecordList.get(0);
            ElectricMeterPowerConsumeRecordEntity lastPowerConsumeRecordEntity = powerConsumeRecordList.get(powerConsumeRecordList.size() - 1);
            copyBeginPowerFromConsumeRecord(reportEntity, firstPowerConsumeRecordEntity);
            copyEndPowerFromConsumeRecord(reportEntity, lastPowerConsumeRecordEntity);
        } else {
            if (previousReportEntity != null) {
                // 无用电时优先承接前一日报期末，保证零日报能够连续滚动。
                copyBeginPowerFromPreviousReport(reportEntity, previousReportEntity);
            } else {
                OpenMeterEntity firstOpenRecordEntity = firstOrNull(openRecordList);
                if (firstOpenRecordEntity != null) {
                    // 开户首日且没有前序日报时，期初读数直接取开户读数。
                    copyBeginPowerFromOpenRecord(reportEntity, firstOpenRecordEntity);
                } else {
                    MeterCancelRecordEntity firstCancelRecordEntity = firstOrNull(cancelRecordList);
                    if (firstCancelRecordEntity != null) {
                        // 只出现销户事实时，期初只能回退到销户快照读数。
                        copyBeginPowerFromCancelRecord(reportEntity, firstCancelRecordEntity);
                    }
                }
            }
        }

        MeterCancelRecordEntity lastCancelRecordEntity = lastOrNull(cancelRecordList);
        if (lastCancelRecordEntity != null) {
            // 销户读数代表当天最终表底，即使白天已有用电记录，期末也必须被销户快照覆盖。
            copyEndPowerFromCancelRecord(reportEntity, lastCancelRecordEntity);
            return;
        }
        if (powerConsumeRecordList.isEmpty()) {
            // 既无用电又无销户时，期末与期初保持一致，形成零变动日报。
            copyEndPowerFromBegin(reportEntity);
        }
    }

    /**
     * 填充按量电费与展示单价，包月账户不适用时统一置空。
     */
    private void applyElectricChargeFields(DailyMeterReportEntity reportEntity,
                                           DailyMeterReportEntity previousReportEntity,
                                           Integer electricAccountType,
                                           List<ElectricMeterBalanceConsumeRecordEntity> electricChargeRecordList) {
        if (Objects.equals(electricAccountType, ElectricAccountTypeEnum.MONTHLY.getCode())) {
            reportEntity.setElectricChargeAmount(null);
            reportEntity.setElectricChargeAmountHigher(null);
            reportEntity.setElectricChargeAmountHigh(null);
            reportEntity.setElectricChargeAmountLow(null);
            reportEntity.setElectricChargeAmountLower(null);
            reportEntity.setElectricChargeAmountDeepLow(null);
            reportEntity.setDisplayPriceHigher(null);
            reportEntity.setDisplayPriceHigh(null);
            reportEntity.setDisplayPriceLow(null);
            reportEntity.setDisplayPriceLower(null);
            reportEntity.setDisplayPriceDeepLow(null);
            return;
        }

        reportEntity.setElectricChargeAmount(sumConsumeAmount(electricChargeRecordList, ElectricPricePeriodEnum.TOTAL));
        reportEntity.setElectricChargeAmountHigher(sumConsumeAmount(electricChargeRecordList, ElectricPricePeriodEnum.HIGHER));
        reportEntity.setElectricChargeAmountHigh(sumConsumeAmount(electricChargeRecordList, ElectricPricePeriodEnum.HIGH));
        reportEntity.setElectricChargeAmountLow(sumConsumeAmount(electricChargeRecordList, ElectricPricePeriodEnum.LOW));
        reportEntity.setElectricChargeAmountLower(sumConsumeAmount(electricChargeRecordList, ElectricPricePeriodEnum.LOWER));
        reportEntity.setElectricChargeAmountDeepLow(sumConsumeAmount(electricChargeRecordList, ElectricPricePeriodEnum.DEEP_LOW));

        ElectricMeterBalanceConsumeRecordEntity lastElectricChargeRecordEntity = lastOrNull(electricChargeRecordList);
        if (lastElectricChargeRecordEntity != null) {
            reportEntity.setDisplayPriceHigher(lastElectricChargeRecordEntity.getPriceHigher());
            reportEntity.setDisplayPriceHigh(lastElectricChargeRecordEntity.getPriceHigh());
            reportEntity.setDisplayPriceLow(lastElectricChargeRecordEntity.getPriceLow());
            reportEntity.setDisplayPriceLower(lastElectricChargeRecordEntity.getPriceLower());
            reportEntity.setDisplayPriceDeepLow(lastElectricChargeRecordEntity.getPriceDeepLow());
            return;
        }
        if (previousReportEntity != null) {
            reportEntity.setDisplayPriceHigher(previousReportEntity.getDisplayPriceHigher());
            reportEntity.setDisplayPriceHigh(previousReportEntity.getDisplayPriceHigh());
            reportEntity.setDisplayPriceLow(previousReportEntity.getDisplayPriceLow());
            reportEntity.setDisplayPriceLower(previousReportEntity.getDisplayPriceLower());
            reportEntity.setDisplayPriceDeepLow(previousReportEntity.getDisplayPriceDeepLow());
        }
    }

    /**
     * 汇总补正记录中的补缴与退费金额，包月账户不适用时统一置空。
     */
    private void applyCorrectionFields(DailyMeterReportEntity reportEntity,
                                       Integer electricAccountType,
                                       List<ElectricMeterBalanceConsumeRecordEntity> correctionRecordList) {
        if (Objects.equals(electricAccountType, ElectricAccountTypeEnum.MONTHLY.getCode())) {
            reportEntity.setCorrectionPayAmount(null);
            reportEntity.setCorrectionRefundAmount(null);
            reportEntity.setCorrectionNetAmount(null);
            return;
        }

        BigDecimal correctionPayAmount = BigDecimal.ZERO;
        BigDecimal correctionRefundAmount = BigDecimal.ZERO;
        for (ElectricMeterBalanceConsumeRecordEntity correctionRecordEntity : correctionRecordList) {
            BigDecimal consumeAmount = defaultDecimal(correctionRecordEntity.getConsumeAmount());
            if (consumeAmount.signum() >= 0) {
                correctionPayAmount = correctionPayAmount.add(consumeAmount);
            } else {
                correctionRefundAmount = correctionRefundAmount.add(consumeAmount.abs());
            }
        }

        reportEntity.setCorrectionPayAmount(correctionPayAmount);
        reportEntity.setCorrectionRefundAmount(correctionRefundAmount);
        reportEntity.setCorrectionNetAmount(correctionPayAmount.subtract(correctionRefundAmount));
    }

    /**
     * 填充按需账户的余额与充值金额，非按需账户相关字段统一置空。
     */
    private void applyBalanceAndRechargeFields(DailyMeterReportEntity reportEntity,
                                               DailyMeterReportEntity previousReportEntity,
                                               Integer electricAccountType,
                                               List<ElectricMeterBalanceConsumeRecordEntity> electricChargeRecordList,
                                               List<ElectricMeterBalanceConsumeRecordEntity> correctionRecordList,
                                               List<RechargeSourceItemQo> rechargeSourceItemList) {
        if (!Objects.equals(electricAccountType, ElectricAccountTypeEnum.QUANTITY.getCode())) {
            reportEntity.setBeginBalance(null);
            reportEntity.setEndBalance(null);
            reportEntity.setRechargeAmount(null);
            return;
        }

        reportEntity.setRechargeAmount(sumRechargeAmount(rechargeSourceItemList));
        List<BalanceEvent> balanceEventList = buildBalanceEventList(electricChargeRecordList, correctionRecordList, rechargeSourceItemList);
        if (!balanceEventList.isEmpty()) {
            reportEntity.setBeginBalance(balanceEventList.get(0).beginBalance());
            reportEntity.setEndBalance(balanceEventList.get(balanceEventList.size() - 1).endBalance());
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
     * 构建余额事件时间线，用于确定日电表的期初期末余额。
     */
    private List<BalanceEvent> buildBalanceEventList(List<ElectricMeterBalanceConsumeRecordEntity> electricChargeRecordList,
                                                     List<ElectricMeterBalanceConsumeRecordEntity> correctionRecordList,
                                                     List<RechargeSourceItemQo> rechargeSourceItemList) {
        List<BalanceEvent> balanceEventList = new ArrayList<>();
        for (RechargeSourceItemQo rechargeSourceItemQo : defaultList(rechargeSourceItemList)) {
            if (rechargeSourceItemQo.getBeginBalance() == null || rechargeSourceItemQo.getEndBalance() == null) {
                continue;
            }
            balanceEventList.add(new BalanceEvent(rechargeSourceItemQo.getCreateTime(), 0,
                    rechargeSourceItemQo.getBeginBalance(), rechargeSourceItemQo.getEndBalance()));
        }
        for (ElectricMeterBalanceConsumeRecordEntity electricChargeRecordEntity : defaultList(electricChargeRecordList)) {
            if (electricChargeRecordEntity.getBeginBalance() == null || electricChargeRecordEntity.getEndBalance() == null) {
                continue;
            }
            balanceEventList.add(new BalanceEvent(electricChargeRecordEntity.getMeterConsumeTime(), 1,
                    electricChargeRecordEntity.getBeginBalance(), electricChargeRecordEntity.getEndBalance()));
        }
        for (ElectricMeterBalanceConsumeRecordEntity correctionRecordEntity : defaultList(correctionRecordList)) {
            if (correctionRecordEntity.getBeginBalance() == null || correctionRecordEntity.getEndBalance() == null) {
                continue;
            }
            balanceEventList.add(new BalanceEvent(correctionRecordEntity.getMeterConsumeTime(), 2,
                    correctionRecordEntity.getBeginBalance(), correctionRecordEntity.getEndBalance()));
        }
        balanceEventList.sort(Comparator
                .comparing(BalanceEvent::eventTime, Comparator.nullsLast(LocalDateTime::compareTo))
                .thenComparingInt(BalanceEvent::eventOrder));
        return balanceEventList;
    }

    /**
     * 汇总电表充值到账金额。
     */
    private BigDecimal sumRechargeAmount(List<RechargeSourceItemQo> rechargeSourceItemList) {
        BigDecimal rechargeAmount = BigDecimal.ZERO;
        for (RechargeSourceItemQo rechargeSourceItemQo : defaultList(rechargeSourceItemList)) {
            rechargeAmount = rechargeAmount.add(defaultDecimal(rechargeSourceItemQo.getAmount()));
        }
        return rechargeAmount;
    }

    /**
     * 解析电表所属账户类型，优先取账户开户快照，其次承接前一日报。
     */
    private Integer resolveElectricAccountType(DailyMeterReportEntity previousReportEntity,
                                               AccountOpenRecordEntity accountOpenRecordEntity) {
        if (accountOpenRecordEntity != null && accountOpenRecordEntity.getElectricAccountType() != null) {
            return accountOpenRecordEntity.getElectricAccountType();
        }

        return previousReportEntity == null ? null : previousReportEntity.getElectricAccountType();
    }

    /**
     * 解析日报生成类型，优先判断当天是否销户，其次判断是否存在正常用电。
     */
    private Integer resolveGenerateType(boolean hasCancelRecord, boolean hasPowerConsumeRecord) {
        if (hasCancelRecord) {
            return MeterReportGenerateTypeEnum.CANCEL.getCode();
        }
        if (hasPowerConsumeRecord) {
            return MeterReportGenerateTypeEnum.NORMAL.getCode();
        }
        return MeterReportGenerateTypeEnum.ZERO.getCode();
    }

    /**
     * 将候选 DTO 列表转换成去重且有序的账户-电表键列表。
     */
    private List<AccountMeterKey> buildCandidateKeyList(List<DailyMeterCandidateQo> candidateList) {
        Set<AccountMeterKey> accountMeterKeySet = new TreeSet<>(Comparator
                .comparing(AccountMeterKey::accountId)
                .thenComparing(AccountMeterKey::meterId));
        for (DailyMeterCandidateQo candidateQo : candidateList) {
            if (candidateQo == null) {
                continue;
            }
            AccountMeterKey accountMeterKey = buildAccountMeterKey(candidateQo.getAccountId(), candidateQo.getMeterId());
            if (accountMeterKey != null) {
                accountMeterKeySet.add(accountMeterKey);
            }
        }
        return new ArrayList<>(accountMeterKeySet);
    }

    /**
     * 构建账户-电表联合键，主键缺失时返回 null。
     */
    private AccountMeterKey buildAccountMeterKey(Integer accountId, Integer meterId) {
        if (accountId == null || meterId == null) {
            return null;
        }
        return new AccountMeterKey(accountId, meterId);
    }

    /**
     * 按电价时段汇总电量记录中的用电量。
     */
    private BigDecimal sumPower(List<ElectricMeterPowerConsumeRecordEntity> powerConsumeRecordList, ElectricPricePeriodEnum pricePeriod) {
        return sumByPricePeriod(powerConsumeRecordList, pricePeriod,
                ElectricMeterPowerConsumeRecordEntity::getConsumePower,
                ElectricMeterPowerConsumeRecordEntity::getConsumePowerHigher,
                ElectricMeterPowerConsumeRecordEntity::getConsumePowerHigh,
                ElectricMeterPowerConsumeRecordEntity::getConsumePowerLow,
                ElectricMeterPowerConsumeRecordEntity::getConsumePowerLower,
                ElectricMeterPowerConsumeRecordEntity::getConsumePowerDeepLow);
    }

    /**
     * 按电价时段汇总扣费记录中的金额。
     */
    private BigDecimal sumConsumeAmount(List<ElectricMeterBalanceConsumeRecordEntity> balanceConsumeRecordList, ElectricPricePeriodEnum pricePeriod) {
        return sumByPricePeriod(balanceConsumeRecordList, pricePeriod,
                ElectricMeterBalanceConsumeRecordEntity::getConsumeAmount,
                ElectricMeterBalanceConsumeRecordEntity::getConsumeAmountHigher,
                ElectricMeterBalanceConsumeRecordEntity::getConsumeAmountHigh,
                ElectricMeterBalanceConsumeRecordEntity::getConsumeAmountLow,
                ElectricMeterBalanceConsumeRecordEntity::getConsumeAmountLower,
                ElectricMeterBalanceConsumeRecordEntity::getConsumeAmountDeepLow);
    }

    /**
     * 从用电记录中复制期初读数字段。
     */
    private void copyBeginPowerFromConsumeRecord(DailyMeterReportEntity reportEntity,
                                                 ElectricMeterPowerConsumeRecordEntity powerConsumeRecordEntity) {
        applyBeginPowerValues(reportEntity,
                powerConsumeRecordEntity.getBeginPower(),
                powerConsumeRecordEntity.getBeginPowerHigher(),
                powerConsumeRecordEntity.getBeginPowerHigh(),
                powerConsumeRecordEntity.getBeginPowerLow(),
                powerConsumeRecordEntity.getBeginPowerLower(),
                powerConsumeRecordEntity.getBeginPowerDeepLow());
    }

    /**
     * 从用电记录中复制期末读数字段。
     */
    private void copyEndPowerFromConsumeRecord(DailyMeterReportEntity reportEntity,
                                               ElectricMeterPowerConsumeRecordEntity powerConsumeRecordEntity) {
        applyEndPowerValues(reportEntity,
                powerConsumeRecordEntity.getEndPower(),
                powerConsumeRecordEntity.getEndPowerHigher(),
                powerConsumeRecordEntity.getEndPowerHigh(),
                powerConsumeRecordEntity.getEndPowerLow(),
                powerConsumeRecordEntity.getEndPowerLower(),
                powerConsumeRecordEntity.getEndPowerDeepLow());
    }

    /**
     * 从前一日电表日报承接期初读数字段。
     */
    private void copyBeginPowerFromPreviousReport(DailyMeterReportEntity reportEntity,
                                                  DailyMeterReportEntity previousReportEntity) {
        applyBeginPowerValues(reportEntity,
                previousReportEntity.getEndPower(),
                previousReportEntity.getEndPowerHigher(),
                previousReportEntity.getEndPowerHigh(),
                previousReportEntity.getEndPowerLow(),
                previousReportEntity.getEndPowerLower(),
                previousReportEntity.getEndPowerDeepLow());
    }

    /**
     * 从开户记录中复制期初读数字段。
     */
    private void copyBeginPowerFromOpenRecord(DailyMeterReportEntity reportEntity,
                                              OpenMeterEntity openRecordEntity) {
        applyBeginPowerValues(reportEntity,
                openRecordEntity.getPower(),
                openRecordEntity.getPowerHigher(),
                openRecordEntity.getPowerHigh(),
                openRecordEntity.getPowerLow(),
                openRecordEntity.getPowerLower(),
                openRecordEntity.getPowerDeepLow());
    }

    /**
     * 从销户记录中复制期初读数字段。
     */
    private void copyBeginPowerFromCancelRecord(DailyMeterReportEntity reportEntity,
                                                MeterCancelRecordEntity cancelRecordEntity) {
        applyBeginPowerValues(reportEntity,
                cancelRecordEntity.getPower(),
                cancelRecordEntity.getPowerHigher(),
                cancelRecordEntity.getPowerHigh(),
                cancelRecordEntity.getPowerLow(),
                cancelRecordEntity.getPowerLower(),
                cancelRecordEntity.getPowerDeepLow());
    }

    /**
     * 从销户记录中复制期末读数字段。
     */
    private void copyEndPowerFromCancelRecord(DailyMeterReportEntity reportEntity,
                                              MeterCancelRecordEntity cancelRecordEntity) {
        applyEndPowerValues(reportEntity,
                cancelRecordEntity.getPower(),
                cancelRecordEntity.getPowerHigher(),
                cancelRecordEntity.getPowerHigh(),
                cancelRecordEntity.getPowerLow(),
                cancelRecordEntity.getPowerLower(),
                cancelRecordEntity.getPowerDeepLow());
    }

    /**
     * 当天无期末来源时，使用期初读数补齐期末读数。
     */
    private void copyEndPowerFromBegin(DailyMeterReportEntity reportEntity) {
        applyEndPowerValues(reportEntity,
                reportEntity.getBeginPower(),
                reportEntity.getBeginPowerHigher(),
                reportEntity.getBeginPowerHigh(),
                reportEntity.getBeginPowerLow(),
                reportEntity.getBeginPowerLower(),
                reportEntity.getBeginPowerDeepLow());
    }

    private record AccountMeterKey(Integer accountId, Integer meterId) {
    }

    private record BalanceEvent(LocalDateTime eventTime,
                                int eventOrder,
                                BigDecimal beginBalance,
                                BigDecimal endBalance) {
    }

}
