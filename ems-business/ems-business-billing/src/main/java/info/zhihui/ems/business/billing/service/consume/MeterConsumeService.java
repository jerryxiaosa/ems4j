package info.zhihui.ems.business.billing.service.consume;

import info.zhihui.ems.business.billing.dto.ElectricMeterPowerRecordDto;
import info.zhihui.ems.business.billing.dto.MeterBillingDetailDto;
import info.zhihui.ems.business.billing.dto.MeterBillingQueryDto;
import info.zhihui.ems.business.billing.dto.MeterBillingRecordDto;
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
    PageResult<MeterBillingRecordDto> findMeterBillingPage(@Valid @NotNull MeterBillingQueryDto queryDto,
                                                           @NotNull PageParam pageParam);

    /**
     * 查询电表计费明细
     */
    MeterBillingDetailDto getMeterBillingDetail(@NotNull Integer id);
}
