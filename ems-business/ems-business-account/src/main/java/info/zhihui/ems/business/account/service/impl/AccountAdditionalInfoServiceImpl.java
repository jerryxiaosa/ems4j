package info.zhihui.ems.business.account.service.impl;

import info.zhihui.ems.business.account.dto.AccountCandidateMeterDto;
import info.zhihui.ems.business.account.dto.AccountElectricBalanceAggregateItemDto;
import info.zhihui.ems.business.account.dto.AccountOwnerInfoDto;
import info.zhihui.ems.business.account.dto.OwnerCandidateMeterQueryDto;
import info.zhihui.ems.business.account.entity.OwnerSpaceRelEntity;
import info.zhihui.ems.business.account.repository.OwnerSpaceRelRepository;
import info.zhihui.ems.business.account.service.AccountAdditionalInfoService;
import info.zhihui.ems.business.device.bo.ElectricMeterBo;
import info.zhihui.ems.business.device.dto.ElectricMeterQueryDto;
import info.zhihui.ems.business.device.service.ElectricMeterInfoService;
import info.zhihui.ems.business.finance.bo.BalanceBo;
import info.zhihui.ems.business.finance.service.balance.BalanceService;
import info.zhihui.ems.common.enums.BalanceTypeEnum;
import info.zhihui.ems.common.enums.CodeEnum;
import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import info.zhihui.ems.common.enums.OwnerTypeEnum;
import info.zhihui.ems.foundation.organization.service.OrganizationService;
import info.zhihui.ems.foundation.space.bo.SpaceBo;
import info.zhihui.ems.foundation.space.dto.SpaceQueryDto;
import info.zhihui.ems.foundation.space.service.SpaceService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 账户附加信息服务实现。
 */
@Service
@Validated
@RequiredArgsConstructor
public class AccountAdditionalInfoServiceImpl implements AccountAdditionalInfoService {

    private final OwnerSpaceRelRepository ownerSpaceRelRepository;
    private final OrganizationService organizationService;
    private final SpaceService spaceService;
    private final ElectricMeterInfoService electricMeterInfoService;
    private final BalanceService balanceService;

    /**
     * {@inheritDoc}
     * 查询主体候选电表列表（租赁空间内、预付费、未开户）。
     */
    @Override
    public List<AccountCandidateMeterDto> findCandidateMeterList(@Valid @NotNull OwnerCandidateMeterQueryDto queryDto) {
        validateOwnerExists(queryDto.getOwnerType(), queryDto.getOwnerId());

        Set<Integer> rentedSpaceIdSet = buildOwnerSpaceIdMap(queryDto.getOwnerType(), List.of(queryDto.getOwnerId()))
                .getOrDefault(queryDto.getOwnerId(), Set.of());
        if (rentedSpaceIdSet.isEmpty()) {
            return Collections.emptyList();
        }

        // 通过租赁的空间id获取空间列表
        List<SpaceBo> rentedSpaceList = spaceService.findSpaceList(new SpaceQueryDto()
                .setIds(rentedSpaceIdSet)
                .setName(queryDto.getSpaceNameLike()));
        if (CollectionUtils.isEmpty(rentedSpaceList)) {
            return Collections.emptyList();
        }

        List<Integer> matchedSpaceIdList = rentedSpaceList.stream()
                .filter(Objects::nonNull)
                .map(SpaceBo::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (matchedSpaceIdList.isEmpty()) {
            return Collections.emptyList();
        }

        // 通过空间查找出对应的预付费表
        List<ElectricMeterBo> meterBoList = electricMeterInfoService.findList(new ElectricMeterQueryDto()
                .setSpaceIds(matchedSpaceIdList)
                .setIsPrepay(true));
        if (CollectionUtils.isEmpty(meterBoList)) {
            return Collections.emptyList();
        }

        Map<Integer, SpaceBo> spaceBoMap = rentedSpaceList.stream()
                .filter(Objects::nonNull)
                .filter(spaceBo -> spaceBo.getId() != null)
                .collect(Collectors.toMap(SpaceBo::getId, spaceBo -> spaceBo, (left, right) -> left));

        // 候选列表展示离线表，但只展示未开户表。
        List<AccountCandidateMeterDto> list = new ArrayList<>();
        for (ElectricMeterBo meterBo : meterBoList) {
            if (meterBo != null) {
                if (meterBo.getAccountId() == null) {
                    AccountCandidateMeterDto candidateMeterDto = toCandidateMeterDto(meterBo, spaceBoMap.get(meterBo.getSpaceId()));
                    list.add(candidateMeterDto);
                }
            }
        }
        return list;
    }

    /**
     * {@inheritDoc}
     * 按账户ID批量统计可开户电表总数（租赁空间内电表数）。
     */
    @Override
    public Map<Integer, Integer> countTotalOpenableMeterByAccountOwnerInfoList(@NotEmpty List<@NotNull AccountOwnerInfoDto> accountOwnerInfoDtoList) {
        List<Integer> validAccountIdList = extractAccountIdList(accountOwnerInfoDtoList);
        if (validAccountIdList.isEmpty()) {
            return Map.of();
        }
        Map<Integer, Integer> resultMap = buildZeroCountMap(validAccountIdList);

        // 构建 accountId -> ownerKey 映射。
        // 因为查询租赁需要通过ownerId+ownerType的方式来查询，所以这里要转换
        Map<Integer, OwnerKey> accountOwnerMap = buildAccountOwnerMap(accountOwnerInfoDtoList);
        if (accountOwnerMap.isEmpty()) {
            return resultMap;
        }

        // 构建 ownerKey -> rentedSpaceIdSet 映射。
        Map<OwnerKey, Set<Integer>> ownerSpaceIdMap = buildOwnerSpaceIdMapByAccount(accountOwnerMap);
        if (ownerSpaceIdMap.isEmpty()) {
            return resultMap;
        }

        List<Integer> allSpaceIdList = ownerSpaceIdMap.values().stream()
                .flatMap(Set::stream)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (allSpaceIdList.isEmpty()) {
            return resultMap;
        }

        Map<Integer, Integer> spaceMeterCountMap = buildSpaceMeterCountMap(allSpaceIdList);
        Map<OwnerKey, Integer> ownerMeterCountMap = buildOwnerMeterCountMap(ownerSpaceIdMap, spaceMeterCountMap);
        for (Integer accountId : validAccountIdList) {
            OwnerKey ownerKey = accountOwnerMap.get(accountId);
            resultMap.put(accountId, ownerMeterCountMap.getOrDefault(ownerKey, 0));
        }
        return resultMap;
    }

    /**
     * {@inheritDoc}
     * 批量计算账户列表展示电费余额。
     */
    @Override
    public Map<Integer, BigDecimal> findElectricBalanceAmountMap(
            @NotEmpty List<@Valid @NotNull AccountElectricBalanceAggregateItemDto> itemDtoList) {
        Map<Integer, ElectricAccountTypeEnum> accountElectricAccountTypeMap = new HashMap<>();
        for (AccountElectricBalanceAggregateItemDto itemDto : itemDtoList) {
            Integer accountId = itemDto.getAccountId();
            ElectricAccountTypeEnum electricAccountType = itemDto.getElectricAccountType();
            if (!accountElectricAccountTypeMap.containsKey(accountId)) {
                accountElectricAccountTypeMap.put(accountId, electricAccountType);
            }
        }
        if (accountElectricAccountTypeMap.isEmpty()) {
            return Map.of();
        }

        List<Integer> accountIdList = new ArrayList<>(accountElectricAccountTypeMap.keySet());
        List<BalanceBo> balanceBoList = balanceService.findListByAccountIds(accountIdList);
        Map<Integer, BigDecimal> accountBalanceAmountMap = new HashMap<>();
        Map<Integer, BigDecimal> meterBalanceAmountSumMap = new HashMap<>();
        if (balanceBoList != null) {
            for (BalanceBo balanceBo : balanceBoList) {
                if (balanceBo == null || balanceBo.getAccountId() == null || balanceBo.getBalanceType() == null) {
                    continue;
                }
                BigDecimal balanceAmount = Objects.requireNonNullElse(balanceBo.getBalance(), BigDecimal.ZERO);
                if (BalanceTypeEnum.ACCOUNT.equals(balanceBo.getBalanceType())) {
                    accountBalanceAmountMap.put(balanceBo.getAccountId(), balanceAmount);
                    continue;
                }
                if (BalanceTypeEnum.ELECTRIC_METER.equals(balanceBo.getBalanceType())) {
                    meterBalanceAmountSumMap.merge(balanceBo.getAccountId(), balanceAmount, BigDecimal::add);
                }
            }
        }

        Map<Integer, BigDecimal> electricBalanceAmountMap = new HashMap<>();
        for (Map.Entry<Integer, ElectricAccountTypeEnum> entry : accountElectricAccountTypeMap.entrySet()) {
            Integer accountId = entry.getKey();
            BalanceTypeEnum balanceType = toBalanceType(entry.getValue());
            if (BalanceTypeEnum.ACCOUNT.equals(balanceType)) {
                electricBalanceAmountMap.put(accountId, accountBalanceAmountMap.getOrDefault(accountId, BigDecimal.ZERO));
                continue;
            }
            if (BalanceTypeEnum.ELECTRIC_METER.equals(balanceType)) {
                electricBalanceAmountMap.put(accountId, meterBalanceAmountSumMap.getOrDefault(accountId, BigDecimal.ZERO));
                continue;
            }
            electricBalanceAmountMap.put(accountId, BigDecimal.ZERO);
        }
        return electricBalanceAmountMap;
    }


    /**
     * 提取并去重有效账户ID。
     */
    private List<Integer> extractAccountIdList(List<AccountOwnerInfoDto> accountOwnerInfoDtoList) {
        return accountOwnerInfoDtoList.stream()
                .filter(Objects::nonNull)
                .map(AccountOwnerInfoDto::getAccountId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    /**
     * 初始化账户结果集（默认值为0）。
     */
    private Map<Integer, Integer> buildZeroCountMap(List<Integer> accountIdList) {
        Map<Integer, Integer> resultMap = new HashMap<>();
        for (Integer accountId : accountIdList) {
            resultMap.put(accountId, 0);
        }
        return resultMap;
    }

    /**
     * 统计空间维度电表数量：spaceId -> meterCount。
     */
    private Map<Integer, Integer> buildSpaceMeterCountMap(List<Integer> spaceIdList) {
        List<ElectricMeterBo> electricMeterBoList = electricMeterInfoService.findList(
                new ElectricMeterQueryDto().setSpaceIds(spaceIdList)
        );
        if (CollectionUtils.isEmpty(electricMeterBoList)) {
            return Map.of();
        }
        Map<Integer, Integer> spaceMeterCountMap = new HashMap<>();
        for (ElectricMeterBo electricMeterBo : electricMeterBoList) {
            if (electricMeterBo == null || electricMeterBo.getSpaceId() == null) {
                continue;
            }
            Integer spaceId = electricMeterBo.getSpaceId();
            spaceMeterCountMap.put(spaceId, spaceMeterCountMap.getOrDefault(spaceId, 0) + 1);
        }
        return spaceMeterCountMap;
    }

    /**
     * 汇总主体下所有空间的电表总数：ownerKey -> meterCount。
     */
    private Map<OwnerKey, Integer> buildOwnerMeterCountMap(Map<OwnerKey, Set<Integer>> ownerSpaceIdMap,
                                                           Map<Integer, Integer> spaceMeterCountMap) {
        Map<OwnerKey, Integer> ownerMeterCountMap = new HashMap<>();
        for (Map.Entry<OwnerKey, Set<Integer>> entry : ownerSpaceIdMap.entrySet()) {
            int meterCount = 0;
            for (Integer spaceId : entry.getValue()) {
                meterCount += spaceMeterCountMap.getOrDefault(spaceId, 0);
            }
            ownerMeterCountMap.put(entry.getKey(), meterCount);
        }
        return ownerMeterCountMap;
    }

    /**
     * 规范化主体ID列表：过滤null并去重。
     */
    private List<Integer> normalizeOwnerIds(List<Integer> ownerIds) {
        if (CollectionUtils.isEmpty(ownerIds)) {
            return List.of();
        }
        return ownerIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    /**
     * 构建 ownerId -> rentedSpaceIdSet 映射（同一 ownerType 下）。
     */
    private Map<Integer, Set<Integer>> buildOwnerSpaceIdMap(OwnerTypeEnum ownerType, List<Integer> ownerIds) {
        if (ownerType == null) {
            return Map.of();
        }
        List<Integer> validOwnerIdList = normalizeOwnerIds(ownerIds);
        if (validOwnerIdList.isEmpty()) {
            return Map.of();
        }
        List<OwnerSpaceRelEntity> entityList = ownerSpaceRelRepository.findListByOwnerTypesAndOwnerIds(
                Set.of(ownerType.getCode()), validOwnerIdList
        );

        return entityList.stream()
                .filter(item -> item.getOwnerId() != null && item.getSpaceId() != null)
                .collect(Collectors.groupingBy(
                        OwnerSpaceRelEntity::getOwnerId,
                        Collectors.mapping(OwnerSpaceRelEntity::getSpaceId, Collectors.toSet())
                ));
    }

    private Map<Integer, OwnerKey> buildAccountOwnerMap(List<AccountOwnerInfoDto> accountOwnerInfoDtoList) {
        Map<Integer, OwnerKey> accountOwnerMap = new HashMap<>();
        if (CollectionUtils.isEmpty(accountOwnerInfoDtoList)) {
            return accountOwnerMap;
        }
        for (AccountOwnerInfoDto accountOwnerInfoDto : accountOwnerInfoDtoList) {
            if (accountOwnerInfoDto == null || accountOwnerInfoDto.getAccountId() == null) {
                continue;
            }
            if (accountOwnerInfoDto.getOwnerType() == null || accountOwnerInfoDto.getOwnerId() == null) {
                continue;
            }
            accountOwnerMap.putIfAbsent(
                    accountOwnerInfoDto.getAccountId(),
                    new OwnerKey(accountOwnerInfoDto.getOwnerType(), accountOwnerInfoDto.getOwnerId())
            );
        }
        return accountOwnerMap;
    }

    /**
     * 主体存在性校验（当前仅校验企业主体）。
     */
    private void validateOwnerExists(OwnerTypeEnum ownerType, Integer ownerId) {
        if (OwnerTypeEnum.ENTERPRISE.equals(ownerType)) {
            organizationService.getDetail(ownerId);
        }
    }

    private Map<OwnerKey, Set<Integer>> buildOwnerSpaceIdMapByAccount(Map<Integer, OwnerKey> accountOwnerMap) {
        Map<OwnerKey, Set<Integer>> ownerSpaceIdMap = new HashMap<>();
        Set<Integer> ownerTypeCodeSet = new HashSet<>();
        Set<Integer> ownerIdSet = new HashSet<>();
        Set<OwnerKey> validOwnerKeySet = new HashSet<>();
        for (OwnerKey ownerKey : accountOwnerMap.values()) {
            if (ownerKey == null || ownerKey.ownerType() == null || ownerKey.ownerId() == null) {
                continue;
            }
            ownerTypeCodeSet.add(ownerKey.ownerType().getCode());
            ownerIdSet.add(ownerKey.ownerId());
            validOwnerKeySet.add(ownerKey);
        }
        if (ownerTypeCodeSet.isEmpty() || ownerIdSet.isEmpty()) {
            return Map.of();
        }

        List<OwnerSpaceRelEntity> relationList = ownerSpaceRelRepository.findListByOwnerTypesAndOwnerIds(ownerTypeCodeSet, ownerIdSet);
        if (CollectionUtils.isEmpty(relationList)) {
            return Map.of();
        }
        for (OwnerSpaceRelEntity relationEntity : relationList) {
            if (relationEntity == null
                    || relationEntity.getOwnerType() == null
                    || relationEntity.getOwnerId() == null
                    || relationEntity.getSpaceId() == null) {
                continue;
            }
            OwnerTypeEnum ownerType = CodeEnum.fromCode(relationEntity.getOwnerType(), OwnerTypeEnum.class);
            if (ownerType == null) {
                continue;
            }
            OwnerKey ownerKey = new OwnerKey(ownerType, relationEntity.getOwnerId());
            if (!validOwnerKeySet.contains(ownerKey)) {
                continue;
            }
            ownerSpaceIdMap.computeIfAbsent(ownerKey, ignore -> new HashSet<>()).add(relationEntity.getSpaceId());
        }
        return ownerSpaceIdMap;
    }

    private AccountCandidateMeterDto toCandidateMeterDto(ElectricMeterBo meterBo, SpaceBo spaceBo) {
        AccountCandidateMeterDto candidateMeterDto = new AccountCandidateMeterDto()
                .setId(meterBo.getId())
                .setMeterName(meterBo.getMeterName())
                .setMeterNo(meterBo.getMeterNo())
                .setSpaceId(meterBo.getSpaceId())
                .setIsOnline(meterBo.getIsOnline())
                .setIsPrepay(meterBo.getIsPrepay());
        if (spaceBo != null) {
            candidateMeterDto.setSpaceName(spaceBo.getName());
            candidateMeterDto.setSpaceParentNames(spaceBo.getParentsNames());
        }
        return candidateMeterDto;
    }

    private BalanceTypeEnum toBalanceType(ElectricAccountTypeEnum electricAccountType) {
        if (ElectricAccountTypeEnum.QUANTITY.equals(electricAccountType)) {
            return BalanceTypeEnum.ELECTRIC_METER;
        }
        if (ElectricAccountTypeEnum.MONTHLY.equals(electricAccountType)
                || ElectricAccountTypeEnum.MERGED.equals(electricAccountType)) {
            return BalanceTypeEnum.ACCOUNT;
        }
        return null;
    }

    private record OwnerKey(OwnerTypeEnum ownerType, Integer ownerId) {
    }
}
