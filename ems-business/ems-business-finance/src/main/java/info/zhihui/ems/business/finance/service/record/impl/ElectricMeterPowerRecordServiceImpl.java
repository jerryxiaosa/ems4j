package info.zhihui.ems.business.finance.service.record.impl;

import info.zhihui.ems.business.finance.dto.ElectricMeterLatestPowerRecordDto;
import info.zhihui.ems.business.finance.entity.ElectricMeterPowerRecordEntity;
import info.zhihui.ems.business.finance.qo.ElectricMeterPowerRecordQo;
import info.zhihui.ems.business.finance.repository.ElectricMeterPowerRecordRepository;
import info.zhihui.ems.business.finance.service.record.ElectricMeterPowerRecordService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import java.util.List;

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
                new ElectricMeterPowerRecordQo().setMeterId(meterId).setLimit(1));
        if (CollectionUtils.isEmpty(recordList)) {
            return null;
        }
        return toLatestPowerRecordDto(recordList.get(0));
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
}
