package info.zhihui.ems.business.device.service;

import info.zhihui.ems.business.device.dto.MeterBalanceChangeDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * 电表余额变化服务
 *
 * @author jerryxiaosa
 */
public interface MeterBalanceChangeService {

    /**
     * 处理电表余额变化
     *
     * @param meterBalanceChangeDto 电表余额变化参数
     */
    void handleBalanceChange(@NotNull @Valid MeterBalanceChangeDto meterBalanceChangeDto);
}
