package info.zhihui.ems.web.finance.biz;

import info.zhihui.ems.business.account.bo.AccountBo;
import info.zhihui.ems.business.account.service.AccountInfoService;
import info.zhihui.ems.business.device.bo.ElectricMeterBo;
import info.zhihui.ems.business.device.service.ElectricMeterInfoService;
import info.zhihui.ems.business.finance.dto.*;
import info.zhihui.ems.business.finance.enums.CorrectionTypeEnum;
import info.zhihui.ems.business.finance.service.consume.AccountConsumeService;
import info.zhihui.ems.business.finance.service.consume.MeterConsumeService;
import info.zhihui.ems.business.finance.service.consume.MeterCorrectionService;
import info.zhihui.ems.common.enums.CodeEnum;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.web.finance.mapstruct.FinanceWebMapper;
import info.zhihui.ems.web.finance.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 财务消费业务编排
 */
@Service
@RequiredArgsConstructor
public class FinanceBiz {

    private final AccountConsumeService accountConsumeService;
    private final AccountInfoService accountInfoService;
    private final ElectricMeterInfoService electricMeterInfoService;
    private final MeterConsumeService meterConsumeService;
    private final MeterCorrectionService meterCorrectionService;
    private final FinanceWebMapper financeWebMapper;

    public PageResult<AccountConsumeRecordVo> findAccountConsumePage(AccountConsumeQueryVo queryVo, Integer pageNum, Integer pageSize) {
        AccountConsumeQueryDto queryDto = financeWebMapper.toAccountConsumeQueryDto(queryVo);
        PageParam pageParam = buildPageParam(pageNum, pageSize);
        PageResult<AccountConsumeRecordDto> page = accountConsumeService.findAccountConsumePage(queryDto, pageParam);
        return financeWebMapper.toAccountConsumeRecordVoPage(page);
    }

    public PageResult<PowerConsumeRecordVo> findPowerConsumePage(PowerConsumeQueryVo queryVo, Integer pageNum, Integer pageSize) {
        PowerConsumeQueryDto queryDto = financeWebMapper.toPowerConsumeQueryDto(queryVo);
        PageParam pageParam = buildPageParam(pageNum, pageSize);
        PageResult<PowerConsumeRecordDto> page = meterConsumeService.findPowerConsumePage(queryDto, pageParam);
        return financeWebMapper.toPowerConsumeRecordVoPage(page);
    }

    public PowerConsumeDetailVo getPowerConsumeDetail(Integer id) {
        PowerConsumeDetailDto detailDto = meterConsumeService.getPowerConsumeDetail(id);
        return financeWebMapper.toPowerConsumeDetailVo(detailDto);
    }

    public PageResult<CorrectionRecordVo> findCorrectionRecordPage(CorrectionRecordQueryVo queryVo, Integer pageNum, Integer pageSize) {
        MeterCorrectionRecordQueryDto queryDto = financeWebMapper.toCorrectionRecordQueryDto(queryVo);
        PageParam pageParam = buildPageParam(pageNum, pageSize);
        PageResult<MeterCorrectionRecordDto> page = meterCorrectionService.findCorrectionRecordPage(queryDto, pageParam);
        return financeWebMapper.toCorrectionRecordVoPage(page);
    }

    public void correctByAmount(CorrectionMeterAmountVo correctionMeterAmountVo) {
        CorrectMeterAmountDto dto = financeWebMapper.toCorrectAmountDto(correctionMeterAmountVo);
        AccountBo accountBo = accountInfoService.getById(dto.getAccountId());
        ElectricMeterBo electricMeterBo = electricMeterInfoService.getDetail(dto.getMeterId());

        dto.setElectricAccountType(accountBo.getElectricAccountType());
        dto.setOwnerId(accountBo.getOwnerId());
        dto.setOwnerName(accountBo.getOwnerName());
        dto.setOwnerType(accountBo.getOwnerType());

        dto.setMeterName(electricMeterBo.getMeterName());
        dto.setDeviceNo(electricMeterBo.getDeviceNo());

        dto.setCorrectionType(CodeEnum.fromCode(correctionMeterAmountVo.getCorrectionType(), CorrectionTypeEnum.class));
        dto.setCorrectionTime(LocalDateTime.now());
        meterCorrectionService.correctByAmount(dto);
    }

    private PageParam buildPageParam(Integer pageNum, Integer pageSize) {
        return new PageParam()
                .setPageNum(pageNum == null ? 1 : pageNum)
                .setPageSize(pageSize == null ? 10 : pageSize);
    }
}
