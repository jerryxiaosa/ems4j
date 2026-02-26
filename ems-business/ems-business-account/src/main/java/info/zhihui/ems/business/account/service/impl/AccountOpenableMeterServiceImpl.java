package info.zhihui.ems.business.account.service.impl;

import info.zhihui.ems.business.account.dto.AccountCandidateMeterDto;
import info.zhihui.ems.business.account.dto.AccountOwnerInfoDto;
import info.zhihui.ems.business.account.dto.OwnerCandidateMeterQueryDto;
import info.zhihui.ems.business.account.entity.OwnerSpaceRelEntity;
import info.zhihui.ems.business.account.repository.OwnerSpaceRelRepository;
import info.zhihui.ems.business.account.service.AccountOpenableMeterService;
import info.zhihui.ems.business.device.bo.ElectricMeterBo;
import info.zhihui.ems.business.device.dto.ElectricMeterQueryDto;
import info.zhihui.ems.business.device.service.ElectricMeterInfoService;
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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 账户开户电表查询服务实现
 */
@Service
@Validated
@RequiredArgsConstructor
public class AccountOpenableMeterServiceImpl implements AccountOpenableMeterService {

    private final OwnerSpaceRelRepository ownerSpaceRelRepository;
    private final OrganizationService organizationService;
    private final SpaceService spaceService;
    private final ElectricMeterInfoService electricMeterInfoService;

    /**
     * {@inheritDoc}
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

        Map<Integer, SpaceBo> spaceBoMap = rentedSpaceList.stream()
                .filter(Objects::nonNull)
                .filter(spaceBo -> spaceBo.getId() != null)
                .collect(Collectors.toMap(SpaceBo::getId, spaceBo -> spaceBo, (left, right) -> left));

        // 通过空间查找出对应的预付费表
        List<ElectricMeterBo> meterBoList = electricMeterInfoService.findList(new ElectricMeterQueryDto()
                .setSpaceIds(matchedSpaceIdList)
                .setIsPrepay(true));
        if (CollectionUtils.isEmpty(meterBoList)) {
            return Collections.emptyList();
        }

        return meterBoList.stream()
                // 候选列表展示离线表，但只展示未开户表。
                .filter(Objects::nonNull)
                .filter(meterBo -> meterBo.getAccountId() == null)
                .map(meterBo -> toCandidateMeterDto(meterBo, spaceBoMap.get(meterBo.getSpaceId())))
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Integer, Integer> countTotalOpenableMeterByAccountOwnerInfoList(@NotEmpty List<@NotNull AccountOwnerInfoDto> accountOwnerInfoDtoList) {
        List<Integer> validAccountIdList = extractAccountIdList(accountOwnerInfoDtoList);
        if (validAccountIdList.isEmpty()) {
            return Map.of();
        }

        Map<Integer, OwnerKey> accountOwnerMap = buildAccountOwnerMap(accountOwnerInfoDtoList);
        if (accountOwnerMap.isEmpty()) {
            return validAccountIdList.stream().collect(Collectors.toMap(accountId -> accountId, accountId -> 0));
        }

        Map<OwnerKey, Set<Integer>> ownerSpaceIdMap = buildOwnerSpaceIdMapByAccount(accountOwnerMap);
        if (ownerSpaceIdMap.isEmpty()) {
            return validAccountIdList.stream().collect(Collectors.toMap(accountId -> accountId, accountId -> 0));
        }

        List<Integer> allSpaceIdList = ownerSpaceIdMap.values().stream()
                .flatMap(Set::stream)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (allSpaceIdList.isEmpty()) {
            return validAccountIdList.stream().collect(Collectors.toMap(accountId -> accountId, accountId -> 0));
        }

        Map<Integer, Integer> spaceMeterCountMap = electricMeterInfoService.findList(
                        new ElectricMeterQueryDto().setSpaceIds(allSpaceIdList)
                ).stream()
                .map(ElectricMeterBo::getSpaceId)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(spaceId -> spaceId, Collectors.summingInt(ignore -> 1)));

        return validAccountIdList.stream().collect(Collectors.toMap(
                accountId -> accountId,
                accountId -> ownerSpaceIdMap.getOrDefault(accountOwnerMap.get(accountId), Set.of()).stream()
                        .mapToInt(spaceId -> spaceMeterCountMap.getOrDefault(spaceId, 0))
                        .sum()
        ));
    }

    /**
     * 规范化账户ID列表：过滤null并去重。
     */
    private List<Integer> extractAccountIdList(List<AccountOwnerInfoDto> accountOwnerInfoDtoList) {
        if (CollectionUtils.isEmpty(accountOwnerInfoDtoList)) {
            return List.of();
        }
        return accountOwnerInfoDtoList.stream()
                .filter(Objects::nonNull)
                .map(AccountOwnerInfoDto::getAccountId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
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
        List<OwnerSpaceRelEntity> entityList = ownerSpaceRelRepository.findListByOwnerTypeAndOwnerIds(ownerType.getCode(), validOwnerIdList);

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
        if (accountOwnerMap.isEmpty()) {
            return Map.of();
        }
        Map<OwnerKey, Set<Integer>> ownerSpaceIdMap = new HashMap<>();
        Map<OwnerTypeEnum, List<Integer>> ownerIdsByType = accountOwnerMap.values().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(
                        OwnerKey::ownerType,
                        Collectors.mapping(OwnerKey::ownerId, Collectors.collectingAndThen(Collectors.toList(), this::normalizeOwnerIds))
                ));
        for (Map.Entry<OwnerTypeEnum, List<Integer>> entry : ownerIdsByType.entrySet()) {
            if (entry.getKey() == null || CollectionUtils.isEmpty(entry.getValue())) {
                continue;
            }
            Map<Integer, Set<Integer>> ownerIdSpaceMap = buildOwnerSpaceIdMap(entry.getKey(), entry.getValue());
            ownerIdSpaceMap.forEach((ownerId, spaceIdSet) ->
                    ownerSpaceIdMap.put(new OwnerKey(entry.getKey(), ownerId), spaceIdSet)
            );
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

    private record OwnerKey(OwnerTypeEnum ownerType, Integer ownerId) {
    }
}
