package info.zhihui.ems.business.aggregation.service.account;

import info.zhihui.ems.business.aggregation.dto.AccountElectricBalanceAggregateItemDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 账户电费余额聚合服务接口
 *
 * <p>用于跨账户/财务模块计算账户列表展示用电费余额。</p>
 */
public interface AccountElectricBalanceAggregateService {

    /**
     * 批量计算账户列表展示电费余额。
     *
     * <p>实现步骤：</p>
     * <p>1. 对输入按 accountId 去重，并保留账户计费类型</p>
     * <p>2. 批量查询财务余额明细（仅一次调用 finance 服务）</p>
     * <p>3. 分别聚合账户余额与电表余额合计</p>
     * <p>4. 按账户计费类型选择最终展示余额并返回</p>
     *
     * <p>返回结果会覆盖所有有效输入账户ID，缺失余额记录统一返回0。</p>
     *
     * @param itemDtoList 聚合输入项列表
     * @return key=账户ID，value=展示电费余额
     */
    Map<Integer, BigDecimal> findElectricBalanceAmountMap(
            @NotEmpty List<@Valid @NotNull AccountElectricBalanceAggregateItemDto> itemDtoList);
}
