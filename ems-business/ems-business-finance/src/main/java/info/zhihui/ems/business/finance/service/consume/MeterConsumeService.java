package info.zhihui.ems.business.finance.service.consume;

import info.zhihui.ems.business.finance.dto.ElectricMeterPowerRecordDto;
import info.zhihui.ems.business.finance.dto.PowerConsumeDetailDto;
import info.zhihui.ems.business.finance.dto.PowerConsumeQueryDto;
import info.zhihui.ems.business.finance.dto.PowerConsumeRecordDto;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * 电表用电消费服务
 */
public interface MeterConsumeService {

    /**
     * 保存抄表数据并触发用电消费计算
     */
    void savePowerRecord(@Valid @NotNull ElectricMeterPowerRecordDto meterPowerRecordDto);

    /**
     * 查询电表消费记录
     */
    PageResult<PowerConsumeRecordDto> findPowerConsumePage(@Valid @NotNull PowerConsumeQueryDto queryDto,
                                                           @NotNull PageParam pageParam);

    /**
     * 查询电量消费明细
     */
    PowerConsumeDetailDto getPowerConsumeDetail(@NotNull Integer id);
}
