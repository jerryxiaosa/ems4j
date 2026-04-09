package info.zhihui.ems.web.report.biz;

import info.zhihui.ems.business.report.bo.ElectricBillReportAccountDetailBo;
import info.zhihui.ems.business.report.bo.ElectricBillReportDetailBo;
import info.zhihui.ems.business.report.bo.ElectricBillReportMeterDetailBo;
import info.zhihui.ems.business.report.bo.ElectricBillReportPageItemBo;
import info.zhihui.ems.business.report.dto.ElectricBillReportQueryDto;
import info.zhihui.ems.business.report.service.query.ElectricBillReportQueryService;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.web.report.vo.ElectricBillReportAccountDetailVo;
import info.zhihui.ems.web.report.vo.ElectricBillReportDetailVo;
import info.zhihui.ems.web.report.vo.ElectricBillReportMeterDetailVo;
import info.zhihui.ems.web.report.vo.ElectricBillReportPageVo;
import info.zhihui.ems.web.report.vo.ElectricBillReportQueryVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 电费报表业务编排。
 */
@Service
@RequiredArgsConstructor
public class ElectricBillReportBiz {

    private static final int MAX_QUERY_DAY_COUNT = 65;

    private final ElectricBillReportQueryService electricBillReportQueryService;

    /**
     * 分页查询电费报表列表。
     */
    public PageResult<ElectricBillReportPageVo> findPage(ElectricBillReportQueryVo queryVo, Integer pageNum, Integer pageSize) {
        validateQueryDate(queryVo.getStartDate(), queryVo.getEndDate());
        PageParam pageParam = new PageParam()
                .setPageNum(Objects.requireNonNullElse(pageNum, 1))
                .setPageSize(Objects.requireNonNullElse(pageSize, 10));
        PageResult<ElectricBillReportPageItemBo> pageResult = electricBillReportQueryService.findPage(toQueryBo(queryVo), pageParam);
        return toPageVo(pageResult);
    }

    /**
     * 查询电费报表详情。
     */
    public ElectricBillReportDetailVo getDetail(Integer accountId, ElectricBillReportQueryVo queryVo) {
        validateQueryDate(queryVo.getStartDate(), queryVo.getEndDate());
        ElectricBillReportDetailBo detailBo = electricBillReportQueryService.getDetail(accountId, toQueryBo(queryVo));
        return toDetailVo(detailBo);
    }

    private ElectricBillReportQueryDto toQueryBo(ElectricBillReportQueryVo queryVo) {
        return new ElectricBillReportQueryDto()
                .setAccountNameLike(queryVo.getAccountNameLike())
                .setStartDate(queryVo.getStartDate())
                .setEndDate(queryVo.getEndDate());
    }

    private PageResult<ElectricBillReportPageVo> toPageVo(PageResult<ElectricBillReportPageItemBo> pageResult) {
        if (pageResult == null) {
            return new PageResult<ElectricBillReportPageVo>()
                    .setPageNum(0)
                    .setPageSize(0)
                    .setTotal(0L)
                    .setList(Collections.emptyList());
        }
        List<ElectricBillReportPageVo> pageVoList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(pageResult.getList())) {
            for (ElectricBillReportPageItemBo pageItemBo : pageResult.getList()) {
                pageVoList.add(new ElectricBillReportPageVo()
                        .setAccountId(pageItemBo.getAccountId())
                        .setAccountName(pageItemBo.getAccountName())
                        .setElectricAccountType(pageItemBo.getElectricAccountType())
                        .setMeterCount(pageItemBo.getMeterCount())
                        .setPeriodConsumePower(pageItemBo.getPeriodConsumePower())
                        .setPeriodElectricChargeAmount(pageItemBo.getPeriodElectricChargeAmount())
                        .setPeriodRechargeAmount(pageItemBo.getPeriodRechargeAmount())
                        .setPeriodCorrectionAmount(pageItemBo.getPeriodCorrectionAmount())
                        .setTotalDebitAmount(pageItemBo.getTotalDebitAmount()));
            }
        }
        return new PageResult<ElectricBillReportPageVo>()
                .setPageNum(pageResult.getPageNum())
                .setPageSize(pageResult.getPageSize())
                .setTotal(pageResult.getTotal())
                .setList(pageVoList);
    }

    private ElectricBillReportDetailVo toDetailVo(ElectricBillReportDetailBo detailBo) {
        List<ElectricBillReportMeterDetailVo> meterDetailVoList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(detailBo.getMeterList())) {
            for (ElectricBillReportMeterDetailBo meterDetailBo : detailBo.getMeterList()) {
                meterDetailVoList.add(new ElectricBillReportMeterDetailVo()
                        .setMeterId(meterDetailBo.getMeterId())
                        .setDeviceNo(meterDetailBo.getDeviceNo())
                        .setMeterName(meterDetailBo.getMeterName())
                        .setConsumePowerHigher(meterDetailBo.getConsumePowerHigher())
                        .setConsumePowerHigh(meterDetailBo.getConsumePowerHigh())
                        .setConsumePowerLow(meterDetailBo.getConsumePowerLow())
                        .setConsumePowerLower(meterDetailBo.getConsumePowerLower())
                        .setConsumePowerDeepLow(meterDetailBo.getConsumePowerDeepLow())
                        .setDisplayPriceHigher(meterDetailBo.getDisplayPriceHigher())
                        .setDisplayPriceHigh(meterDetailBo.getDisplayPriceHigh())
                        .setDisplayPriceLow(meterDetailBo.getDisplayPriceLow())
                        .setDisplayPriceLower(meterDetailBo.getDisplayPriceLower())
                        .setDisplayPriceDeepLow(meterDetailBo.getDisplayPriceDeepLow())
                        .setElectricChargeAmountHigher(meterDetailBo.getElectricChargeAmountHigher())
                        .setElectricChargeAmountHigh(meterDetailBo.getElectricChargeAmountHigh())
                        .setElectricChargeAmountLow(meterDetailBo.getElectricChargeAmountLow())
                        .setElectricChargeAmountLower(meterDetailBo.getElectricChargeAmountLower())
                        .setElectricChargeAmountDeepLow(meterDetailBo.getElectricChargeAmountDeepLow())
                        .setTotalConsumePower(meterDetailBo.getTotalConsumePower())
                        .setTotalElectricChargeAmount(meterDetailBo.getTotalElectricChargeAmount())
                        .setTotalRechargeAmount(meterDetailBo.getTotalRechargeAmount())
                        .setTotalCorrectionAmount(meterDetailBo.getTotalCorrectionAmount()));
            }
        }

        ElectricBillReportAccountDetailBo accountInfo = detailBo.getAccountInfo();
        ElectricBillReportAccountDetailVo accountDetailVo = new ElectricBillReportAccountDetailVo()
                .setAccountId(accountInfo.getAccountId())
                .setAccountName(accountInfo.getAccountName())
                .setContactName(accountInfo.getContactName())
                .setContactPhone(accountInfo.getContactPhone())
                .setElectricAccountType(accountInfo.getElectricAccountType())
                .setMonthlyPayAmount(accountInfo.getMonthlyPayAmount())
                .setAccountBalance(accountInfo.getAccountBalance())
                .setMeterCount(accountInfo.getMeterCount())
                .setPeriodConsumePower(accountInfo.getPeriodConsumePower())
                .setPeriodElectricChargeAmount(accountInfo.getPeriodElectricChargeAmount())
                .setPeriodRechargeAmount(accountInfo.getPeriodRechargeAmount())
                .setPeriodCorrectionAmount(accountInfo.getPeriodCorrectionAmount())
                .setDateRangeText(accountInfo.getDateRangeText());
        return new ElectricBillReportDetailVo()
                .setAccountInfo(accountDetailVo)
                .setMeterList(meterDetailVoList);
    }

    private void validateQueryDate(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new BusinessRuntimeException("统计开始日期不能大于结束日期");
        }
        if (!endDate.isBefore(LocalDate.now())) {
            throw new BusinessRuntimeException("统计结束日期不能选择今天");
        }
        long dayCount = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        if (dayCount > MAX_QUERY_DAY_COUNT) {
            throw new BusinessRuntimeException("统计日期跨度不能超过65天");
        }
    }
}
