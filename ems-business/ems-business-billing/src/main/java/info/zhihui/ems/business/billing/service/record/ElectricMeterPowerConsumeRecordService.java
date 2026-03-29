package info.zhihui.ems.business.billing.service.record;

import info.zhihui.ems.business.billing.dto.ElectricMeterPowerConsumeTrendPointDto;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 电表区间耗电记录查询服务。
 * <p>
 * 该服务只负责读取已经生成的区间耗电记录，不负责消费计算或扣费。
 */
public interface ElectricMeterPowerConsumeRecordService {

    /**
     * 查询电表区间耗电趋势记录列表
     *
     * @param meterId   电表ID
     * @param beginTime 开始时间
     * @param endTime   结束时间
     * @return 趋势记录列表
     */
    List<ElectricMeterPowerConsumeTrendPointDto> findTrendRecordList(@NotNull Integer meterId,
                                                                     @NotNull LocalDateTime beginTime,
                                                                     @NotNull LocalDateTime endTime);
}
