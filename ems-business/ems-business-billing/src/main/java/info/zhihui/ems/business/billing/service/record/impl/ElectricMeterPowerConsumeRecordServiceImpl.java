package info.zhihui.ems.business.billing.service.record.impl;

import info.zhihui.ems.business.billing.dto.ElectricMeterPowerConsumeTrendPointDto;
import info.zhihui.ems.business.billing.entity.ElectricMeterPowerConsumeRecordEntity;
import info.zhihui.ems.business.billing.qo.ElectricMeterPowerConsumeRecordQo;
import info.zhihui.ems.business.billing.repository.ElectricMeterPowerConsumeRecordRepository;
import info.zhihui.ems.business.billing.service.record.ElectricMeterPowerConsumeRecordService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 电表区间耗电记录查询服务实现。
 */
@Service
@RequiredArgsConstructor
@Validated
public class ElectricMeterPowerConsumeRecordServiceImpl implements ElectricMeterPowerConsumeRecordService {

    private final ElectricMeterPowerConsumeRecordRepository electricMeterPowerConsumeRecordRepository;

    @Override
    public List<ElectricMeterPowerConsumeTrendPointDto> findTrendRecordList(@NotNull Integer meterId,
                                                                            @NotNull LocalDateTime beginTime,
                                                                            @NotNull LocalDateTime endTime) {
        List<ElectricMeterPowerConsumeRecordEntity> recordList = electricMeterPowerConsumeRecordRepository.findTrendRecordList(
                new ElectricMeterPowerConsumeRecordQo()
                        .setMeterId(meterId)
                        .setBeginTime(beginTime)
                        .setEndTime(endTime)
                        .setLimit(1000));
        if (CollectionUtils.isEmpty(recordList)) {
            return Collections.emptyList();
        }
        return recordList.stream().map(this::toTrendPointDto).collect(Collectors.toList());
    }

    private ElectricMeterPowerConsumeTrendPointDto toTrendPointDto(ElectricMeterPowerConsumeRecordEntity recordEntity) {
        if (recordEntity == null) {
            return null;
        }
        return new ElectricMeterPowerConsumeTrendPointDto()
                .setBeginRecordTime(recordEntity.getBeginRecordTime())
                .setEndRecordTime(recordEntity.getEndRecordTime())
                .setMeterConsumeTime(recordEntity.getMeterConsumeTime())
                .setConsumePower(recordEntity.getConsumePower())
                .setConsumePowerHigher(recordEntity.getConsumePowerHigher())
                .setConsumePowerHigh(recordEntity.getConsumePowerHigh())
                .setConsumePowerLow(recordEntity.getConsumePowerLow())
                .setConsumePowerLower(recordEntity.getConsumePowerLower())
                .setConsumePowerDeepLow(recordEntity.getConsumePowerDeepLow());
    }
}
