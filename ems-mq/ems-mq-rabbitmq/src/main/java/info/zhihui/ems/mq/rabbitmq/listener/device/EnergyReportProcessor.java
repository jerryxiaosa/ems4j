package info.zhihui.ems.mq.rabbitmq.listener.device;

import info.zhihui.ems.business.account.bo.AccountBo;
import info.zhihui.ems.business.account.service.AccountInfoService;
import info.zhihui.ems.business.device.bo.ElectricMeterBo;
import info.zhihui.ems.business.device.service.ElectricMeterInfoService;
import info.zhihui.ems.business.billing.dto.ElectricMeterDetailDto;
import info.zhihui.ems.business.billing.dto.ElectricMeterPowerRecordDto;
import info.zhihui.ems.business.billing.service.consume.MeterConsumeService;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.mq.api.message.device.StandardEnergyReportMessage;
import info.zhihui.ems.mq.rabbitmq.exception.NonRetryableException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;

/**
 * 标准电量上报消息处理器
 *
 * @author jerryxiaosa
 */
@Component
@Slf4j
@Validated
@RequiredArgsConstructor
public class EnergyReportProcessor {

    private static final String DEFAULT_REPORT_SOURCE = "STANDARD";

    private final ElectricMeterInfoService electricMeterInfoService;
    private final AccountInfoService accountInfoService;
    private final MeterConsumeService meterConsumeService;

    public void process(@Valid @NotNull StandardEnergyReportMessage message) {
        String originalReportId = buildOriginalReportId(message);
        ElectricMeterBo meterBo = resolveMeter(message.getDeviceNo());
        AccountBo accountBo = resolveAccount(meterBo.getAccountId());
        meterConsumeService.savePowerRecord(buildPowerRecordDto(message, meterBo, accountBo, originalReportId));
    }

    private ElectricMeterBo resolveMeter(String deviceNo) {
        try {
            return electricMeterInfoService.getByDeviceNo(deviceNo);
        } catch (NotFoundException | BusinessRuntimeException exception) {
            throw new NonRetryableException(
                    String.format("标准电量上报无法匹配电表，deviceNo=%s，error=%s", deviceNo, exception.getMessage()),
                    exception);
        }
    }

    private AccountBo resolveAccount(Integer accountId) {
        if (accountId == null) {
            return null;
        }
        try {
            return accountInfoService.getById(accountId);
        } catch (NotFoundException exception) {
            throw new NonRetryableException(
                    String.format("标准电量上报关联账户不存在，accountId=%d，error=%s", accountId, exception.getMessage()),
                    exception);
        }
    }

    private ElectricMeterPowerRecordDto buildPowerRecordDto(StandardEnergyReportMessage message,
                                                            ElectricMeterBo meterBo,
                                                            AccountBo accountBo,
                                                            String originalReportId) {
        return new ElectricMeterPowerRecordDto()
                .setOriginalReportId(originalReportId)
                .setElectricMeterDetailDto(buildMeterDetailDto(meterBo))
                .setAccountId(meterBo.getAccountId())
                .setOwnerId(accountBo == null ? null : accountBo.getOwnerId())
                .setOwnerType(accountBo == null ? null : accountBo.getOwnerType())
                .setOwnerName(accountBo == null ? null : accountBo.getOwnerName())
                .setElectricAccountType(accountBo == null ? null : accountBo.getElectricAccountType())
                .setPower(message.getTotalEnergy())
                .setPowerHigher(message.getHigherEnergy())
                .setPowerHigh(message.getHighEnergy())
                .setPowerLow(message.getLowEnergy())
                .setPowerLower(message.getLowerEnergy())
                .setPowerDeepLow(message.getDeepLowEnergy())
                .setRecordTime(message.getRecordTime())
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

    /**
     * 标准电量上报的幂等键。
     * 使用 source + deviceNo + sourceReportId 组合保证同一来源、同一设备内幂等，
     * 再做 SHA-256 固定长度摘要，避免直接明文拼接超过 original_report_id 列长度。
     */
    private String buildOriginalReportId(StandardEnergyReportMessage message) {
        String source = StringUtils.hasText(message.getSource()) ? message.getSource() : DEFAULT_REPORT_SOURCE;
        source = source.toUpperCase(Locale.ROOT);
        String rawReportId = source + ":" + message.getDeviceNo() + ":" + message.getSourceReportId();
        return sha256Hex(rawReportId);
    }

    private String sha256Hex(String rawValue) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] digestBytes = messageDigest.digest(rawValue.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digestBytes);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm not available", exception);
        }
    }

}
