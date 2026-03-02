package info.zhihui.ems.web.device.biz;

import info.zhihui.ems.business.account.bo.AccountBo;
import info.zhihui.ems.business.account.service.AccountInfoService;
import info.zhihui.ems.business.device.bo.ElectricMeterBo;
import info.zhihui.ems.business.device.service.ElectricMeterInfoService;
import info.zhihui.ems.business.finance.dto.ElectricMeterDetailDto;
import info.zhihui.ems.business.finance.dto.ElectricMeterPowerRecordDto;
import info.zhihui.ems.business.finance.service.consume.MeterConsumeService;
import info.zhihui.ems.web.device.vo.StandardEnergyReportSaveVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * 电量上报业务编排
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EnergyReportBiz {

    private static final String DEFAULT_REPORT_SOURCE = "STANDARD";

    private final ElectricMeterInfoService electricMeterInfoService;
    private final AccountInfoService accountInfoService;
    private final MeterConsumeService meterConsumeService;

    /**
     * 保存标准电量上报
     *
     * @param saveVo 上报参数
     */
    public void addStandardReport(StandardEnergyReportSaveVo saveVo) {
        log.info("收到上报请求，来源：{}，流水号: {}，详细信息：{}", saveVo.getSource(), saveVo.getSourceReportId(), saveVo);
        ElectricMeterBo meterBo = electricMeterInfoService.getByDeviceNo(saveVo.getDeviceNo());
        AccountBo accountBo = resolveAccount(meterBo.getAccountId());
        ElectricMeterPowerRecordDto recordDto = buildPowerRecordDto(saveVo, meterBo, accountBo);
        meterConsumeService.savePowerRecord(recordDto);
    }

    private AccountBo resolveAccount(Integer accountId) {
        if (accountId == null) {
            return null;
        }
        return accountInfoService.getById(accountId);
    }

    private ElectricMeterPowerRecordDto buildPowerRecordDto(StandardEnergyReportSaveVo saveVo,
                                                            ElectricMeterBo meterBo,
                                                            AccountBo accountBo) {
        return new ElectricMeterPowerRecordDto()
                .setOriginalReportId(buildOriginalReportId(saveVo))
                .setElectricMeterDetailDto(buildMeterDetailDto(meterBo))
                .setAccountId(meterBo.getAccountId())
                .setOwnerId(accountBo == null ? null : accountBo.getOwnerId())
                .setOwnerType(accountBo == null ? null : accountBo.getOwnerType())
                .setOwnerName(accountBo == null ? null : accountBo.getOwnerName())
                .setElectricAccountType(accountBo == null ? null : accountBo.getElectricAccountType())
                .setPower(saveVo.getTotalEnergy())
                .setPowerHigher(saveVo.getHigherEnergy())
                .setPowerHigh(saveVo.getHighEnergy())
                .setPowerLow(saveVo.getLowEnergy())
                .setPowerLower(saveVo.getLowerEnergy())
                .setPowerDeepLow(saveVo.getDeepLowEnergy())
                .setRecordTime(saveVo.getRecordTime())
                .setNeedConsume(true);
    }

    private ElectricMeterDetailDto buildMeterDetailDto(ElectricMeterBo meterBo) {
        return new ElectricMeterDetailDto()
                .setMeterId(meterBo.getId())
                .setMeterName(meterBo.getMeterName())
                .setDeviceNo(meterBo.getDeviceNo())
                .setSpaceId(meterBo.getSpaceId())
                .setIsCalculate(meterBo.getIsCalculate())
                .setCalculateType(meterBo.getCalculateType())
                .setIsPrepay(meterBo.getIsPrepay())
                .setPricePlanId(meterBo.getPricePlanId())
                .setCt(meterBo.getCt())
                .setIsOnline(meterBo.getIsOnline())
                .setIsCutOff(meterBo.getIsCutOff());
    }

    private String buildOriginalReportId(StandardEnergyReportSaveVo saveVo) {
        String source = StringUtils.defaultIfBlank(saveVo.getSource(), DEFAULT_REPORT_SOURCE)
                .toUpperCase(Locale.ROOT);
        return source + ":" + saveVo.getSourceReportId();
    }
}
