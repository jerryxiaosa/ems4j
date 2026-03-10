package info.zhihui.ems.business.billing.service.record;

import info.zhihui.ems.business.billing.dto.ElectricMeterLatestPowerRecordDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 电表电量记录查询服务
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
     * 是否存在指定原始上报记录
     *
     * @param originalReportId 原始上报ID
     * @return true-存在；false-不存在
     */
    boolean existsByOriginalReportId(@NotBlank String originalReportId);
}
