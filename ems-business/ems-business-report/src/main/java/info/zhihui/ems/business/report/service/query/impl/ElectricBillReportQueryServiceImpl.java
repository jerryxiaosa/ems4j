package info.zhihui.ems.business.report.service.query.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import info.zhihui.ems.business.account.bo.AccountBo;
import info.zhihui.ems.business.account.service.AccountInfoService;
import info.zhihui.ems.business.report.bo.ElectricBillReportAccountDetailBo;
import info.zhihui.ems.business.report.bo.ElectricBillReportDetailBo;
import info.zhihui.ems.business.report.bo.ElectricBillReportMeterDetailBo;
import info.zhihui.ems.business.report.bo.ElectricBillReportPageItemBo;
import info.zhihui.ems.business.report.dto.ElectricBillReportQueryDto;
import info.zhihui.ems.business.report.entity.DailyAccountReportEntity;
import info.zhihui.ems.business.report.entity.DailyMeterReportEntity;
import info.zhihui.ems.business.report.qo.ElectricBillAccountSummaryQo;
import info.zhihui.ems.business.report.qo.ElectricBillMeterCountQo;
import info.zhihui.ems.business.report.qo.ElectricBillReportQueryQo;
import info.zhihui.ems.business.report.repository.report.DailyAccountReportRepository;
import info.zhihui.ems.business.report.repository.report.DailyMeterReportRepository;
import info.zhihui.ems.business.report.service.query.ElectricBillReportQueryService;
import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 电费报表查询实现。
 */
@Service
@Validated
@RequiredArgsConstructor
public class ElectricBillReportQueryServiceImpl implements ElectricBillReportQueryService {

    private final DailyAccountReportRepository dailyAccountReportRepository;
    private final DailyMeterReportRepository dailyMeterReportRepository;
    private final AccountInfoService accountInfoService;

    /**
     * 按账户分页查询电费报表列表。
     */
    @Override
    public PageResult<ElectricBillReportPageItemBo> findPage(@NotNull ElectricBillReportQueryDto query,
                                                             @NotNull PageParam pageParam) {
        ElectricBillReportQueryQo queryQo = new ElectricBillReportQueryQo()
                .setAccountNameLike(query.getAccountNameLike())
                .setStartDate(query.getStartDate())
                .setEndDate(query.getEndDate());

        try (Page<ElectricBillAccountSummaryQo> page = PageHelper.startPage(pageParam.getPageNum(), pageParam.getPageSize())) {
            PageInfo<ElectricBillAccountSummaryQo> pageInfo =
                    page.doSelectPageInfo(() -> dailyAccountReportRepository.findElectricBillAccountPageList(queryQo));
            List<ElectricBillAccountSummaryQo> accountSummaryList = pageInfo.getList();
            if (CollectionUtils.isEmpty(accountSummaryList)) {
                return new PageResult<ElectricBillReportPageItemBo>()
                        .setPageNum(pageInfo.getPageNum())
                        .setPageSize(pageInfo.getPageSize())
                        .setTotal(pageInfo.getTotal())
                        .setList(Collections.emptyList());
            }

            Map<Integer, Integer> meterCountMap = buildMeterCountMap(accountSummaryList, query);
            List<ElectricBillReportPageItemBo> pageItemBoList = new ArrayList<>(accountSummaryList.size());
            for (ElectricBillAccountSummaryQo accountSummary : accountSummaryList) {
                pageItemBoList.add(toPageItemBo(accountSummary, meterCountMap));
            }
            return new PageResult<ElectricBillReportPageItemBo>()
                    .setPageNum(pageInfo.getPageNum())
                    .setPageSize(pageInfo.getPageSize())
                    .setTotal(pageInfo.getTotal())
                    .setList(pageItemBoList);
        }
    }

    /**
     * 查询单个账户在统计区间内的电费报表详情。
     */
    @Override
    public ElectricBillReportDetailBo getDetail(@NotNull Integer accountId, @NotNull ElectricBillReportQueryDto query) {
        ElectricBillAccountSummaryQo accountSummary = dailyAccountReportRepository.getElectricBillAccountSummary(
                accountId, query.getStartDate(), query.getEndDate()
        );
        if (accountSummary == null) {
            throw new NotFoundException("电费报表不存在");
        }

        DailyAccountReportEntity latestAccountReport = dailyAccountReportRepository.getLatestByAccountIdAndDateRange(
                accountId, query.getStartDate(), query.getEndDate()
        );
        List<DailyMeterReportEntity> meterReportList = dailyMeterReportRepository.findListByAccountIdAndDateRange(
                accountId, query.getStartDate(), query.getEndDate()
        );
        List<ElectricBillReportMeterDetailBo> meterDetailBoList = buildMeterDetailBoList(
                accountSummary.getElectricAccountType(), meterReportList
        );

        AccountBo accountBo = findCurrentAccount(accountId);
        ElectricBillReportAccountDetailBo accountDetailBo = new ElectricBillReportAccountDetailBo()
                .setAccountId(accountSummary.getAccountId())
                .setAccountName(accountSummary.getAccountName())
                .setContactName(accountBo.getContactName())
                .setContactPhone(accountBo.getContactPhone())
                .setElectricAccountType(accountSummary.getElectricAccountType())
                .setMonthlyPayAmount(normalizeZero(accountBo.getMonthlyPayAmount()))
                .setAccountBalance(normalizeZero(latestAccountReport == null ? null : latestAccountReport.getEndBalance()))
                .setMeterCount(meterDetailBoList.size())
                .setPeriodConsumePower(normalizeZero(accountSummary.getPeriodConsumePower()))
                .setPeriodElectricChargeAmount(resolvePeriodElectricChargeAmount(accountSummary))
                .setPeriodRechargeAmount(normalizeZero(accountSummary.getPeriodRechargeAmount()))
                .setPeriodCorrectionAmount(normalizeZero(accountSummary.getPeriodCorrectionAmount()))
                .setDateRangeText(query.getStartDate() + " ~ " + query.getEndDate());

        return new ElectricBillReportDetailBo()
                .setAccountInfo(accountDetailBo)
                .setMeterList(meterDetailBoList);
    }

    /**
     * 批量统计区间内各账户参与统计的去重电表数量。
     */
    private Map<Integer, Integer> buildMeterCountMap(List<ElectricBillAccountSummaryQo> accountSummaryList,
                                                     ElectricBillReportQueryDto query) {
        List<Integer> accountIdList = accountSummaryList.stream()
                .map(ElectricBillAccountSummaryQo::getAccountId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (accountIdList.isEmpty()) {
            return Collections.emptyMap();
        }

        List<ElectricBillMeterCountQo> meterCountList = dailyMeterReportRepository.findMeterCountListByAccountIdList(
                query.getStartDate(), query.getEndDate(), accountIdList
        );
        if (CollectionUtils.isEmpty(meterCountList)) {
            return Collections.emptyMap();
        }

        Map<Integer, Integer> meterCountMap = new LinkedHashMap<>();
        for (ElectricBillMeterCountQo meterCountQo : meterCountList) {
            if (meterCountQo != null && meterCountQo.getAccountId() != null) {
                meterCountMap.put(meterCountQo.getAccountId(), meterCountQo.getMeterCount());
            }
        }
        return meterCountMap;
    }

    /**
     * 将账户汇总查询结果组装成列表页项。
     */
    private ElectricBillReportPageItemBo toPageItemBo(ElectricBillAccountSummaryQo accountSummary,
                                                      Map<Integer, Integer> meterCountMap) {
        return new ElectricBillReportPageItemBo()
                .setAccountId(accountSummary.getAccountId())
                .setAccountName(accountSummary.getAccountName())
                .setElectricAccountType(accountSummary.getElectricAccountType())
                .setMeterCount(meterCountMap.getOrDefault(accountSummary.getAccountId(), 0))
                .setPeriodConsumePower(normalizeZero(accountSummary.getPeriodConsumePower()))
                .setPeriodElectricChargeAmount(resolvePeriodElectricChargeAmount(accountSummary))
                .setPeriodRechargeAmount(normalizeZero(accountSummary.getPeriodRechargeAmount()))
                .setPeriodCorrectionAmount(normalizeZero(accountSummary.getPeriodCorrectionAmount()))
                .setTotalDebitAmount(normalizeZero(accountSummary.getTotalDebitAmount()));
    }

    /**
     * 解析账户本期电费。
     * 包月账户取区间内包月费用累计，其余账户取电费累计。
     */
    private BigDecimal resolvePeriodElectricChargeAmount(ElectricBillAccountSummaryQo accountSummary) {
        if (Objects.equals(accountSummary.getElectricAccountType(), ElectricAccountTypeEnum.MONTHLY.getCode())) {
            return normalizeZero(accountSummary.getPeriodMonthlyChargeAmount());
        }
        return normalizeZero(accountSummary.getPeriodElectricChargeAmount());
    }

    /**
     * 查询当前账户信息。
     * 账户已被删除时返回空对象，便于详情页统一取值。
     */
    private AccountBo findCurrentAccount(Integer accountId) {
        try {
            return accountInfoService.getById(accountId);
        } catch (NotFoundException ex) {
            return new AccountBo();
        }
    }

    /**
     * 按电表聚合统计区间内的日电表报表，生成详情页下半部分数据。
     */
    private List<ElectricBillReportMeterDetailBo> buildMeterDetailBoList(Integer electricAccountType,
                                                                         List<DailyMeterReportEntity> meterReportList) {
        if (CollectionUtils.isEmpty(meterReportList)) {
            return Collections.emptyList();
        }

        Map<Integer, List<DailyMeterReportEntity>> meterReportMap = new LinkedHashMap<>();
        for (DailyMeterReportEntity meterReport : meterReportList) {
            if (meterReport == null || meterReport.getMeterId() == null) {
                continue;
            }
            meterReportMap.computeIfAbsent(meterReport.getMeterId(), key -> new ArrayList<>()).add(meterReport);
        }

        List<ElectricBillReportMeterDetailBo> meterDetailBoList = new ArrayList<>(meterReportMap.size());
        for (Map.Entry<Integer, List<DailyMeterReportEntity>> entry : meterReportMap.entrySet()) {
            List<DailyMeterReportEntity> currentMeterReportList = entry.getValue();
            DailyMeterReportEntity latestMeterReport = currentMeterReportList.get(currentMeterReportList.size() - 1);
            // 按需正常显示
            // 合并计量补正、充值都在账户上所以电表列上不显示
            // 包月电表不直接扣费，所以电表列上不都显示
            boolean hidePriceAndChargeFields = Objects.equals(electricAccountType, ElectricAccountTypeEnum.MONTHLY.getCode());
            boolean hideRechargeAndCorrectionFields = !Objects.equals(electricAccountType, ElectricAccountTypeEnum.QUANTITY.getCode());

            ElectricBillReportMeterDetailBo meterDetailBo = new ElectricBillReportMeterDetailBo()
                    .setMeterId(entry.getKey())
                    .setDeviceNo(latestMeterReport.getDeviceNo())
                    .setMeterName(latestMeterReport.getMeterName())
                    .setConsumePowerHigher(normalizeZero(sumMeterField(currentMeterReportList, MeterFieldType.CONSUME_HIGHER)))
                    .setConsumePowerHigh(normalizeZero(sumMeterField(currentMeterReportList, MeterFieldType.CONSUME_HIGH)))
                    .setConsumePowerLow(normalizeZero(sumMeterField(currentMeterReportList, MeterFieldType.CONSUME_LOW)))
                    .setConsumePowerLower(normalizeZero(sumMeterField(currentMeterReportList, MeterFieldType.CONSUME_LOWER)))
                    .setConsumePowerDeepLow(normalizeZero(sumMeterField(currentMeterReportList, MeterFieldType.CONSUME_DEEP_LOW)))
                    .setTotalConsumePower(normalizeZero(sumMeterField(currentMeterReportList, MeterFieldType.CONSUME_TOTAL)));

            if (!hidePriceAndChargeFields) {
                meterDetailBo
                        .setDisplayPriceHigher(normalizeZero(latestMeterReport.getDisplayPriceHigher()))
                        .setDisplayPriceHigh(normalizeZero(latestMeterReport.getDisplayPriceHigh()))
                        .setDisplayPriceLow(normalizeZero(latestMeterReport.getDisplayPriceLow()))
                        .setDisplayPriceLower(normalizeZero(latestMeterReport.getDisplayPriceLower()))
                        .setDisplayPriceDeepLow(normalizeZero(latestMeterReport.getDisplayPriceDeepLow()))
                        .setElectricChargeAmountHigher(normalizeZero(sumMeterField(currentMeterReportList, MeterFieldType.CHARGE_HIGHER)))
                        .setElectricChargeAmountHigh(normalizeZero(sumMeterField(currentMeterReportList, MeterFieldType.CHARGE_HIGH)))
                        .setElectricChargeAmountLow(normalizeZero(sumMeterField(currentMeterReportList, MeterFieldType.CHARGE_LOW)))
                        .setElectricChargeAmountLower(normalizeZero(sumMeterField(currentMeterReportList, MeterFieldType.CHARGE_LOWER)))
                        .setElectricChargeAmountDeepLow(normalizeZero(sumMeterField(currentMeterReportList, MeterFieldType.CHARGE_DEEP_LOW)))
                        .setTotalElectricChargeAmount(normalizeZero(sumMeterField(currentMeterReportList, MeterFieldType.CHARGE_TOTAL)));
            }
            if (!hideRechargeAndCorrectionFields) {
                meterDetailBo
                        .setTotalRechargeAmount(normalizeZero(sumMeterField(currentMeterReportList, MeterFieldType.RECHARGE_TOTAL)))
                        .setTotalCorrectionAmount(normalizeZero(sumMeterField(currentMeterReportList, MeterFieldType.CORRECTION_TOTAL)));
            }

            meterDetailBoList.add(meterDetailBo);
        }
        return meterDetailBoList;
    }

    /**
     * 对单块电表在统计区间内的指定字段做累加。
     */
    private BigDecimal sumMeterField(List<DailyMeterReportEntity> meterReportList, MeterFieldType fieldType) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (DailyMeterReportEntity meterReport : meterReportList) {
            if (meterReport == null) {
                continue;
            }
            BigDecimal currentAmount = switch (fieldType) {
                case CONSUME_TOTAL -> meterReport.getConsumePower();
                case CONSUME_HIGHER -> meterReport.getConsumePowerHigher();
                case CONSUME_HIGH -> meterReport.getConsumePowerHigh();
                case CONSUME_LOW -> meterReport.getConsumePowerLow();
                case CONSUME_LOWER -> meterReport.getConsumePowerLower();
                case CONSUME_DEEP_LOW -> meterReport.getConsumePowerDeepLow();
                case CHARGE_TOTAL -> meterReport.getElectricChargeAmount();
                case CHARGE_HIGHER -> meterReport.getElectricChargeAmountHigher();
                case CHARGE_HIGH -> meterReport.getElectricChargeAmountHigh();
                case CHARGE_LOW -> meterReport.getElectricChargeAmountLow();
                case CHARGE_LOWER -> meterReport.getElectricChargeAmountLower();
                case CHARGE_DEEP_LOW -> meterReport.getElectricChargeAmountDeepLow();
                case RECHARGE_TOTAL -> meterReport.getRechargeAmount();
                case CORRECTION_TOTAL -> meterReport.getCorrectionNetAmount();
            };
            if (currentAmount != null) {
                totalAmount = totalAmount.add(currentAmount);
            }
        }
        return totalAmount;
    }

    /**
     * 将零值统一规范为 BigDecimal.ZERO，保持返回值风格稳定。
     */
    private BigDecimal normalizeZero(BigDecimal amount) {
        if (amount == null) {
            return null;
        }
        return amount.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : amount;
    }

    private enum MeterFieldType {
        CONSUME_TOTAL,
        CONSUME_HIGHER,
        CONSUME_HIGH,
        CONSUME_LOW,
        CONSUME_LOWER,
        CONSUME_DEEP_LOW,
        CHARGE_TOTAL,
        CHARGE_HIGHER,
        CHARGE_HIGH,
        CHARGE_LOW,
        CHARGE_LOWER,
        CHARGE_DEEP_LOW,
        RECHARGE_TOTAL,
        CORRECTION_TOTAL
    }
}
