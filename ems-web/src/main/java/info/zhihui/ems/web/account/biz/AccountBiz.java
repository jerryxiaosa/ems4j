package info.zhihui.ems.web.account.biz;

import info.zhihui.ems.business.account.bo.AccountBo;
import info.zhihui.ems.business.account.dto.*;
import info.zhihui.ems.business.account.service.AccountInfoService;
import info.zhihui.ems.business.account.service.AccountManagerService;
import info.zhihui.ems.business.account.service.AccountSpaceLeaseService;
import info.zhihui.ems.business.aggregation.dto.AccountElectricBalanceAggregateItemDto;
import info.zhihui.ems.business.aggregation.service.account.AccountElectricBalanceAggregateService;
import info.zhihui.ems.business.device.bo.ElectricMeterBo;
import info.zhihui.ems.business.device.dto.ElectricMeterQueryDto;
import info.zhihui.ems.business.device.service.ElectricMeterInfoService;
import info.zhihui.ems.business.finance.bo.BalanceBo;
import info.zhihui.ems.business.finance.service.balance.BalanceService;
import info.zhihui.ems.common.enums.BalanceTypeEnum;
import info.zhihui.ems.common.enums.CodeEnum;
import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.foundation.space.bo.SpaceBo;
import info.zhihui.ems.foundation.space.dto.SpaceQueryDto;
import info.zhihui.ems.foundation.space.service.SpaceService;
import info.zhihui.ems.web.account.mapstruct.AccountWebMapper;
import info.zhihui.ems.web.account.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 账户业务编排层
 */
@Service
@RequiredArgsConstructor
public class AccountBiz {

    private final AccountInfoService accountInfoService;
    private final AccountManagerService accountManagerService;
    private final AccountSpaceLeaseService accountSpaceLeaseService;
    private final AccountWebMapper accountWebMapper;
    private final ElectricMeterInfoService electricMeterInfoService;
    private final AccountElectricBalanceAggregateService accountElectricBalanceAggregateService;
    private final SpaceService spaceService;
    private final BalanceService balanceService;

    /**
     * 分页查询账户列表
     */
    public PageResult<AccountVo> findAccountPage(AccountQueryVo queryVo, Integer pageNum, Integer pageSize) {
        PageParam pageParam = new PageParam()
                .setPageNum(Objects.requireNonNullElse(pageNum, 1))
                .setPageSize(Objects.requireNonNullElse(pageSize, 10));

        AccountQueryDto queryDto = accountWebMapper.toAccountQueryDto(queryVo);

        PageResult<AccountVo> pageResult = accountWebMapper.toAccountVoPage(accountInfoService.findPage(queryDto, pageParam));
        List<AccountVo> accountVoList = pageResult.getList();
        if (accountVoList == null || accountVoList.isEmpty()) {
            return pageResult;
        }
        List<Integer> accountIdList = extractAccountIdList(accountVoList);
        if (accountIdList.isEmpty()) {
            return pageResult;
        }

        fillOpenedMeterCount(accountVoList, accountIdList);
        fillTotalOpenableMeterCount(accountVoList, accountIdList);
        fillElectricBalanceAmount(accountVoList);
        return pageResult;
    }

    /**
     * 根据ID获取账户详情
     */
    public AccountDetailVo getAccount(Integer id) {
        AccountBo accountBo = accountInfoService.getById(id);
        AccountDetailVo accountVo = accountWebMapper.toAccountDetailVo(accountBo);
        List<BalanceBo> quantityBalanceBoList = prefetchQuantityBalanceList(accountBo);
        fillAccountElectricBalanceAmount(accountBo, accountVo, quantityBalanceBoList);

        List<ElectricMeterBo> meterBos = electricMeterInfoService.findList(new ElectricMeterQueryDto().setAccountIds(List.of(id)));
        List<AccountMeterVo> meterVoList = accountWebMapper.toAccountMeterVoList(meterBos);
        fillAccountMeterSpaceInfo(meterVoList);
        fillAccountMeterBalanceAmount(accountBo, meterVoList, quantityBalanceBoList);
        accountVo.setMeterList(meterVoList);
        accountVo.setOpenedMeterCount(meterBos == null ? 0 : meterBos.size());

        Map<Integer, Integer> totalOpenableMeterCountMap = accountInfoService.countTotalOpenableMeterByAccountIds(List.of(id));
        accountVo.setTotalOpenableMeterCount(totalOpenableMeterCountMap.getOrDefault(id, 0));
        return accountVo;
    }

    /**
     * 分页查询销户记录
     */
    public PageResult<AccountCancelRecordVo> findCancelRecordPage(AccountCancelQueryVo queryVo, Integer pageNum, Integer pageSize) {
        AccountCancelQueryDto queryDto = accountWebMapper.toAccountCancelQueryDto(queryVo);
        if (queryDto == null) {
            queryDto = new AccountCancelQueryDto();
        }
        PageParam pageParam = new PageParam()
                .setPageNum(Objects.requireNonNullElse(pageNum, 1))
                .setPageSize(Objects.requireNonNullElse(pageSize, 10));
        return accountWebMapper.toAccountCancelRecordVoPage(accountInfoService.findCancelRecordPage(queryDto, pageParam));
    }

    /**
     * 获取销户详情
     */
    public AccountCancelDetailVo getCancelRecordDetail(String cancelNo) {
        return accountWebMapper.toAccountCancelDetailVo(accountInfoService.getCancelRecordDetail(cancelNo));
    }

    /**
     * 开户
     */
    public Integer openAccount(OpenAccountVo openAccountVo) {
        OpenAccountDto dto = accountWebMapper.toOpenAccountDto(openAccountVo);
        return accountManagerService.openAccount(dto);
    }

    /**
     * 追加绑定电表
     */
    public void appendMeters(Integer accountId, AccountMetersOpenVo accountMetersOpenVo) {
        AccountMetersOpenDto dto = accountWebMapper.toAccountMetersOpenDto(accountMetersOpenVo);
        dto.setAccountId(accountId);
        accountManagerService.appendMeters(dto);
    }

    /**
     * 租赁空间
     */
    public void rentSpaces(Integer accountId, AccountSpaceRentVo accountSpaceRentVo) {
        AccountSpaceRentDto dto = accountWebMapper.toAccountSpaceRentDto(accountSpaceRentVo);
        dto.setAccountId(accountId);
        accountSpaceLeaseService.rentSpaces(dto);
    }

    /**
     * 退租空间
     */
    public void unrentSpaces(Integer accountId, AccountSpaceUnrentVo accountSpaceUnrentVo) {
        AccountSpaceUnrentDto dto = accountWebMapper.toAccountSpaceUnrentDto(accountSpaceUnrentVo);
        dto.setAccountId(accountId);
        accountSpaceLeaseService.unrentSpaces(dto);
    }

    /**
     * 更新账户配置
     */
    public void updateAccountConfig(Integer accountId, AccountConfigUpdateVo accountConfigUpdateVo) {
        AccountConfigUpdateDto dto = accountWebMapper.toAccountConfigUpdateDto(accountConfigUpdateVo);
        dto.setAccountId(accountId);
        accountManagerService.updateAccount(dto);
    }

    /**
     * 销户
     */
    public CancelAccountResponseVo cancelAccount(CancelAccountVo cancelAccountVo) {
        CancelAccountDto dto = accountWebMapper.toCancelAccountDto(cancelAccountVo);
        CancelAccountResponseDto responseDto = accountManagerService.cancelAccount(dto);
        return accountWebMapper.toCancelAccountResponseVo(responseDto);
    }

    /**
     * 填充账户电表数量
     */
    private void fillOpenedMeterCount(List<AccountVo> accountVoList, List<Integer> accountIdList) {
        List<ElectricMeterBo> meterBoList = electricMeterInfoService.findList(
                new ElectricMeterQueryDto().setAccountIds(accountIdList)
        );
        Map<Integer, Integer> meterCountMap = meterBoList.stream()
                .map(ElectricMeterBo::getAccountId)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(accountId -> accountId, Collectors.summingInt(ignore -> 1)));

        for (AccountVo accountVo : accountVoList) {
            if (accountVo != null && accountVo.getId() != null) {
                accountVo.setOpenedMeterCount(meterCountMap.getOrDefault(accountVo.getId(), 0));
            }
        }
    }

    /**
     * 填充账户可开户电表总数（租赁空间内电表数）
     */
    private void fillTotalOpenableMeterCount(List<AccountVo> accountVoList, List<Integer> accountIdList) {
        Map<Integer, Integer> totalOpenableMeterCountMap = accountInfoService.countTotalOpenableMeterByAccountIds(accountIdList);
        for (AccountVo accountVo : accountVoList) {
            if (accountVo != null && accountVo.getId() != null) {
                accountVo.setTotalOpenableMeterCount(totalOpenableMeterCountMap.getOrDefault(accountVo.getId(), 0));
            }
        }
    }

    /**
     * 填充账户电费余额（按需=电表余额合计；包月/合并=账户余额）
     */
    private void fillElectricBalanceAmount(List<AccountVo> accountVoList) {
        List<AccountElectricBalanceAggregateItemDto> itemDtoList = accountVoList.stream()
                .filter(Objects::nonNull)
                .filter(accountVo -> accountVo.getId() != null)
                .map(this::toAccountElectricBalanceAggregateItemDto)
                .toList();
        Map<Integer, BigDecimal> electricBalanceAmountMap = itemDtoList.isEmpty()
                ? Collections.emptyMap()
                : accountElectricBalanceAggregateService.findElectricBalanceAmountMap(itemDtoList);

        for (AccountVo accountVo : accountVoList) {
            if (accountVo != null && accountVo.getId() != null) {
                accountVo.setElectricBalanceAmount(electricBalanceAmountMap.getOrDefault(accountVo.getId(), BigDecimal.ZERO));
            }
        }
    }

    private AccountElectricBalanceAggregateItemDto toAccountElectricBalanceAggregateItemDto(AccountVo accountVo) {
        ElectricAccountTypeEnum electricAccountType = CodeEnum.fromCode(
                accountVo.getElectricAccountType(), ElectricAccountTypeEnum.class
        );
        return new AccountElectricBalanceAggregateItemDto()
                .setAccountId(accountVo.getId())
                .setElectricAccountType(electricAccountType);
    }

    /**
     * 提取有效账户ID列表
     */
    private List<Integer> extractAccountIdList(List<AccountVo> accountVoList) {
        return accountVoList.stream()
                .filter(Objects::nonNull)
                .map(AccountVo::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    /**
     * 填充账户详情中的电费余额（口径与分页一致）。
     */
    private void fillAccountElectricBalanceAmount(AccountBo accountBo,
                                                  AccountDetailVo accountVo,
                                                  List<BalanceBo> quantityBalanceBoList) {
        if (accountBo == null || accountVo == null || accountBo.getId() == null) {
            return;
        }
        if (accountBo.getElectricAccountType() == ElectricAccountTypeEnum.QUANTITY) {
            BigDecimal electricBalanceAmount = (quantityBalanceBoList == null ? Collections.<BalanceBo>emptyList() : quantityBalanceBoList)
                    .stream()
                    .filter(Objects::nonNull)
                    .filter(balanceBo -> balanceBo.getBalanceType() == BalanceTypeEnum.ELECTRIC_METER)
                    .map(balanceBo -> Objects.requireNonNullElse(balanceBo.getBalance(), BigDecimal.ZERO))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            accountVo.setElectricBalanceAmount(electricBalanceAmount);
            return;
        }

        AccountElectricBalanceAggregateItemDto itemDto = new AccountElectricBalanceAggregateItemDto()
                .setAccountId(accountBo.getId())
                .setElectricAccountType(accountBo.getElectricAccountType());
        Map<Integer, BigDecimal> electricBalanceAmountMap = accountElectricBalanceAggregateService
                .findElectricBalanceAmountMap(List.of(itemDto));
        accountVo.setElectricBalanceAmount(electricBalanceAmountMap.getOrDefault(accountBo.getId(), BigDecimal.ZERO));
    }

    /**
     * 填充账户详情中的电表空间信息（所在位置、所属区域）。
     */
    private void fillAccountMeterSpaceInfo(List<AccountMeterVo> meterVoList) {
        if (meterVoList == null || meterVoList.isEmpty()) {
            return;
        }
        Set<Integer> spaceIdSet = meterVoList.stream()
                .filter(Objects::nonNull)
                .map(AccountMeterVo::getSpaceId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (spaceIdSet.isEmpty()) {
            return;
        }

        List<SpaceBo> spaceBoList = spaceService.findSpaceList(new SpaceQueryDto().setIds(spaceIdSet));
        Map<Integer, SpaceBo> spaceBoMap = spaceBoList.stream()
                .filter(Objects::nonNull)
                .filter(spaceBo -> spaceBo.getId() != null)
                .collect(Collectors.toMap(SpaceBo::getId, spaceBo -> spaceBo, (left, right) -> left));

        for (AccountMeterVo meterVo : meterVoList) {
            if (meterVo == null || meterVo.getSpaceId() == null) {
                continue;
            }
            SpaceBo spaceBo = spaceBoMap.get(meterVo.getSpaceId());
            if (spaceBo == null) {
                continue;
            }
            meterVo.setSpaceName(spaceBo.getName());
            meterVo.setSpaceParentNames(spaceBo.getParentsNames());
        }
    }

    /**
     * 填充账户详情中的电表余额（仅按需计费账户展示电表余额，包月/合并返回null）。
     */
    private void fillAccountMeterBalanceAmount(AccountBo accountBo,
                                               List<AccountMeterVo> meterVoList,
                                               List<BalanceBo> quantityBalanceBoList) {
        if (accountBo == null || accountBo.getId() == null || meterVoList == null || meterVoList.isEmpty()) {
            return;
        }
        if (accountBo.getElectricAccountType() != ElectricAccountTypeEnum.QUANTITY) {
            return;
        }

        List<BalanceBo> balanceBoList = quantityBalanceBoList == null ? Collections.emptyList() : quantityBalanceBoList;
        Map<Integer, BigDecimal> meterBalanceAmountMap = balanceBoList.stream()
                .filter(Objects::nonNull)
                .filter(balanceBo -> balanceBo.getBalanceType() == BalanceTypeEnum.ELECTRIC_METER)
                .filter(balanceBo -> balanceBo.getBalanceRelationId() != null)
                .collect(Collectors.toMap(BalanceBo::getBalanceRelationId, BalanceBo::getBalance, (left, right) -> left));

        for (AccountMeterVo meterVo : meterVoList) {
            if (meterVo == null || meterVo.getId() == null) {
                continue;
            }
            meterVo.setMeterBalanceAmount(meterBalanceAmountMap.get(meterVo.getId()));
        }
    }

    /**
     * 详情仅在按需账户场景预取一次余额明细，供顶层余额和电表余额共用，避免重复查询。
     */
    private List<BalanceBo> prefetchQuantityBalanceList(AccountBo accountBo) {
        if (accountBo == null || accountBo.getId() == null) {
            return Collections.emptyList();
        }
        if (accountBo.getElectricAccountType() != ElectricAccountTypeEnum.QUANTITY) {
            return Collections.emptyList();
        }
        return balanceService.findListByAccountIds(List.of(accountBo.getId()));
    }
}
