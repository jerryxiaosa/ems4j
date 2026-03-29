package info.zhihui.ems.business.billing.service.record;

import info.zhihui.ems.business.billing.dto.ElectricMeterLatestPowerRecordDto;
import info.zhihui.ems.business.billing.dto.ElectricMeterPowerTrendPointDto;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 电表原始电量记录查询服务。
 * <p>
 * 该服务只负责读取原始抄表记录，不负责消费计算。
 *
 * @author jerryxiaosa
 */
public interface ElectricMeterPowerRecordService {

    /**
     * 查询电表最近一次上报电量记录（无记录返回 null）
     *
     * @param meterId 电表ID
     * @return 最近一次上报电量记录
     */
    ElectricMeterLatestPowerRecordDto findLatestRecord(@NotNull Integer meterId);

    /**
     * 查询电表趋势记录列表
     *
     * @param meterId   电表ID
     * @param beginTime 开始时间
     * @param endTime   结束时间
     * @return 趋势记录列表
     */
    List<ElectricMeterPowerTrendPointDto> findTrendRecordList(@NotNull Integer meterId,
                                                              @NotNull LocalDateTime beginTime,
                                                              @NotNull LocalDateTime endTime);

}
