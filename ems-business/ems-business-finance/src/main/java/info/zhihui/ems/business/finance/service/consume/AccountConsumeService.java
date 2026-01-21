package info.zhihui.ems.business.finance.service.consume;

import info.zhihui.ems.business.finance.dto.MonthlyConsumeDto;
import info.zhihui.ems.business.finance.dto.AccountConsumeQueryDto;
import info.zhihui.ems.business.finance.dto.AccountConsumeRecordDto;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * 账户费用相关消费服务
 */
public interface AccountConsumeService {

    /**
     * 包月消费，账户每月只能扣除一次
     * @param monthlyConsumeDto 包月消费参数
     */
    void monthlyConsume(@Valid @NotNull MonthlyConsumeDto monthlyConsumeDto);

    /**
     * 分页查询账户消费记录（包月等）
     * @param queryDto 查询参数
     * @param pageParam 分页参数
     * @return 账户消费记录
     */
    PageResult<AccountConsumeRecordDto> findAccountConsumePage(@NotNull AccountConsumeQueryDto queryDto,
                                                               @NotNull PageParam pageParam);
}
