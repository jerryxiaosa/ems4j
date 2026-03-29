package info.zhihui.ems.business.billing.service.record.impl;

import info.zhihui.ems.business.billing.dto.ElectricMeterLatestPowerRecordDto;
import info.zhihui.ems.business.billing.dto.ElectricMeterPowerTrendPointDto;
import info.zhihui.ems.business.billing.entity.ElectricMeterPowerRecordEntity;
import info.zhihui.ems.business.billing.qo.ElectricMeterPowerRecordQo;
import info.zhihui.ems.business.billing.repository.ElectricMeterPowerRecordRepository;
import info.zhihui.ems.business.billing.service.record.ElectricMeterPowerRecordService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * 电表电量记录查询服务实现
 *
 * @author jerryxiaosa
 */
@Service
@RequiredArgsConstructor
@Validated
public class ElectricMeterPowerRecordServiceImpl implements ElectricMeterPowerRecordService {

    private final ElectricMeterPowerRecordRepository electricMeterPowerRecordRepository;

    /**
     * {@inheritDoc}
     * 查询电表最近一次上报电量记录（无记录返回 null）
     */
    @Override
    public ElectricMeterLatestPowerRecordDto findLatestRecord(@NotNull Integer meterId) {
        List<ElectricMeterPowerRecordEntity> recordList = electricMeterPowerRecordRepository.findRecordList(
                new ElectricMeterPowerRecordQo().setMeterId(meterId).setLimit(1).setAsc(Boolean.FALSE));
        if (CollectionUtils.isEmpty(recordList)) {
            return null;
        }
        return toLatestPowerRecordDto(recordList.get(0));
    }

    /**
     * {@inheritDoc}
     * 查询电表趋势记录列表
     */
    @Override
    public List<ElectricMeterPowerTrendPointDto> findTrendRecordList(@NotNull Integer meterId,
                                                                     @NotNull LocalDateTime beginTime,
                                                                     @NotNull LocalDateTime endTime) {
        List<ElectricMeterPowerRecordEntity> recordList = electricMeterPowerRecordRepository.findRecordList(
                new ElectricMeterPowerRecordQo()
                        .setMeterId(meterId)
                        .setBeginTime(beginTime)
                        .setEndTime(endTime)
                        .setLimit(1000)
                        .setAsc(Boolean.TRUE));
        if (CollectionUtils.isEmpty(recordList)) {
            return Collections.emptyList();
        }
        return recordList.stream().map(this::toPowerTrendPointDto).collect(Collectors.toList());
    }

    private ElectricMeterLatestPowerRecordDto toLatestPowerRecordDto(ElectricMeterPowerRecordEntity recordEntity) {
        if (recordEntity == null) {
            return null;
        }
        return new ElectricMeterLatestPowerRecordDto()
                .setRecordTime(recordEntity.getRecordTime())
                .setPower(recordEntity.getPower())
                .setPowerHigher(recordEntity.getPowerHigher())
                .setPowerHigh(recordEntity.getPowerHigh())
                .setPowerLow(recordEntity.getPowerLow())
                .setPowerLower(recordEntity.getPowerLower())
                .setPowerDeepLow(recordEntity.getPowerDeepLow());
    }

    private ElectricMeterPowerTrendPointDto toPowerTrendPointDto(ElectricMeterPowerRecordEntity recordEntity) {
        if (recordEntity == null) {
            return null;
        }
        return new ElectricMeterPowerTrendPointDto()
                .setRecordTime(recordEntity.getRecordTime())
                .setPower(recordEntity.getPower())
                .setPowerHigher(recordEntity.getPowerHigher())
                .setPowerHigh(recordEntity.getPowerHigh())
                .setPowerLow(recordEntity.getPowerLow())
                .setPowerLower(recordEntity.getPowerLower())
                .setPowerDeepLow(recordEntity.getPowerDeepLow());
    }
}
