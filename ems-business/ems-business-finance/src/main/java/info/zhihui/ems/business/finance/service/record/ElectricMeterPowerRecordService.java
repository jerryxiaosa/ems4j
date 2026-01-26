package info.zhihui.ems.business.finance.service.record;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * 电表电量记录查询服务
 *
 * @author jerryxiaosa
 */
public interface ElectricMeterPowerRecordService {

    /**
     * 查询电表最新电量（无记录返回 null）
     *
     * @param meterId 电表ID
     * @return 最新电量
     */
    BigDecimal findLatestPower(@NotNull Integer meterId);
}
