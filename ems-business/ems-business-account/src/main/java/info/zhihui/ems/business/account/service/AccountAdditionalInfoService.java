package info.zhihui.ems.business.account.service;

import info.zhihui.ems.business.account.dto.AccountCandidateMeterDto;
import info.zhihui.ems.business.account.dto.AccountElectricBalanceAggregateItemDto;
import info.zhihui.ems.business.account.dto.AccountOwnerInfoDto;
import info.zhihui.ems.business.account.dto.OwnerCandidateMeterQueryDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 账户附加信息服务。
 *
 * <p>聚合账户列表/详情需要的附加读信息，包含候选电表、可开户总数、电费余额。</p>
 */
public interface AccountAdditionalInfoService {

    /**
     * 查询主体候选电表列表（租赁空间内、预付费、未开户）。
     *
     * @param queryDto 查询条件
     * @return 候选电表列表（非最终可开户校验结果）
     */
    List<AccountCandidateMeterDto> findCandidateMeterList(@Valid @NotNull OwnerCandidateMeterQueryDto queryDto);

    /**
     * 按账户ID批量统计可开户电表总数（租赁空间内电表数）。
     *
     * @param accountOwnerInfoDtoList 账户归属信息列表（accountId + ownerType + ownerId）
     * @return key=账户ID，value=可开户电表总数
     */
    Map<Integer, Integer> countTotalOpenableMeterByAccountOwnerInfoList(@NotEmpty List<@NotNull AccountOwnerInfoDto> accountOwnerInfoDtoList);

    /**
     * 批量计算账户列表展示电费余额。
     *
     * @param itemDtoList 聚合输入项列表（accountId + electricAccountType）
     * @return key=账户ID，value=展示电费余额
     */
    Map<Integer, BigDecimal> findElectricBalanceAmountMap(
            @NotEmpty List<@Valid @NotNull AccountElectricBalanceAggregateItemDto> itemDtoList);
}
