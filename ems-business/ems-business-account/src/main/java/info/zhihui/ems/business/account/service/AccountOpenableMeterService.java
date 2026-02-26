package info.zhihui.ems.business.account.service;

import info.zhihui.ems.business.account.dto.AccountCandidateMeterDto;
import info.zhihui.ems.business.account.dto.AccountOwnerInfoDto;
import info.zhihui.ems.business.account.dto.OwnerCandidateMeterQueryDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

/**
 * 账户开户电表查询服务。
 *
 * <p>说明：本服务返回的是开户候选电表列表（租赁空间内、预付费、未开户），
 * 不代表最终一定可开户。最终可开户校验仍以开户接口校验逻辑为准（例如在线状态、并发占用等）。</p>
 */
public interface AccountOpenableMeterService {

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
}
