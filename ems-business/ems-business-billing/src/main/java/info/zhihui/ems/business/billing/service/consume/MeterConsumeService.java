package info.zhihui.ems.business.billing.service.consume;

import info.zhihui.ems.business.billing.dto.ElectricMeterPowerRecordDto;
import info.zhihui.ems.business.billing.dto.PowerConsumeDetailDto;
import info.zhihui.ems.business.billing.dto.PowerConsumeQueryDto;
import info.zhihui.ems.business.billing.dto.PowerConsumeRecordDto;
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
