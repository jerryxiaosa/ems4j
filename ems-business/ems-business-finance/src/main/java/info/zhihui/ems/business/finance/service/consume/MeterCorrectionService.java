package info.zhihui.ems.business.finance.service.consume;

import info.zhihui.ems.business.finance.dto.CorrectMeterAmountDto;
import info.zhihui.ems.business.finance.dto.MeterCorrectionRecordDto;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.business.finance.dto.MeterCorrectionRecordQueryDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * 电表补正相关服务
 */
public interface MeterCorrectionService {

    /**
     * 指定金额补正
     *
     * @param correctMeterAmountDto 补正金额
     */
    void correctByAmount(@NotNull @Valid CorrectMeterAmountDto correctMeterAmountDto);


    /**
     * 补正记录分页查询
     *
     * @param queryDto 查询参数
     * @param pageParam 分页参数
     * @return 补正记录
     */
    PageResult<MeterCorrectionRecordDto> findCorrectionRecordPage(@NotNull @Valid MeterCorrectionRecordQueryDto queryDto, @NotNull PageParam pageParam);

}
