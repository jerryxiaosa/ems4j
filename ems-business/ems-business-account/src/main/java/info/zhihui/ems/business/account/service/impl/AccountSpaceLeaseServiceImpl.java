package info.zhihui.ems.business.account.service.impl;

import info.zhihui.ems.business.account.dto.AccountSpaceRentDto;
import info.zhihui.ems.business.account.dto.AccountSpaceUnrentDto;
import info.zhihui.ems.business.account.entity.AccountSpaceRelEntity;
import info.zhihui.ems.business.account.repository.AccountSpaceRelRepository;
import info.zhihui.ems.business.account.service.AccountInfoService;
import info.zhihui.ems.business.account.service.AccountSpaceLeaseService;
import info.zhihui.ems.business.device.bo.ElectricMeterBo;
import info.zhihui.ems.business.device.dto.ElectricMeterQueryDto;
import info.zhihui.ems.business.device.service.ElectricMeterInfoService;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.components.context.RequestContext;
import info.zhihui.ems.components.lock.core.LockTemplate;
import info.zhihui.ems.foundation.space.bo.SpaceBo;
import info.zhihui.ems.foundation.space.dto.SpaceQueryDto;
import info.zhihui.ems.foundation.space.service.SpaceService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

/**
 * 账户空间租赁服务
 */
@Service
@Slf4j
@Validated
@RequiredArgsConstructor
public class AccountSpaceLeaseServiceImpl implements AccountSpaceLeaseService {

    private static final String LOCK_ACCOUNT = "LOCK:ACCOUNT-RENT:%d";

    private final AccountInfoService accountInfoService;
    private final AccountSpaceRelRepository accountSpaceRelRepository;
    private final SpaceService spaceService;
    private final ElectricMeterInfoService electricMeterInfoService;
    private final LockTemplate lockTemplate;
    private final RequestContext requestContext;

    @Override
    public void rentSpaces(@NotNull @Valid AccountSpaceRentDto rentDto) {
        Set<Integer> requestedSpaceIdSet = new LinkedHashSet<>(rentDto.getSpaceIds());

        Integer accountId = rentDto.getAccountId();
        Lock lock = getAccountLock(accountId);
        if (!lock.tryLock()) {
            throw new BusinessRuntimeException("账户正在操作，请稍后重试");
        }

        try {
            accountInfoService.getById(accountId);
            validateSpacesExist(requestedSpaceIdSet);

            List<AccountSpaceRelEntity> existingRelationList = accountSpaceRelRepository.findListBySpaceIds(requestedSpaceIdSet);
            if (CollectionUtils.isEmpty(existingRelationList)) {
                existingRelationList = List.of();
            }
            List<Integer> occupiedSpaceIdList = existingRelationList.stream()
                    .filter(relation -> !Objects.equals(relation.getAccountId(), accountId))
                    .map(AccountSpaceRelEntity::getSpaceId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .sorted()
                    .toList();
            if (!occupiedSpaceIdList.isEmpty()) {
                throw new BusinessRuntimeException("空间已被其他账户租赁: " + joinIds(occupiedSpaceIdList));
            }

            Set<Integer> alreadyRentedSpaceIdSet = existingRelationList.stream()
                    .map(AccountSpaceRelEntity::getSpaceId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            LocalDateTime now = LocalDateTime.now();
            List<AccountSpaceRelEntity> insertEntityList = new ArrayList<>();
            for (Integer spaceId : requestedSpaceIdSet) {
                if (alreadyRentedSpaceIdSet.contains(spaceId)) {
                    continue;
                }
                insertEntityList.add(buildRelationEntity(accountId, spaceId, now));
            }

            if (!insertEntityList.isEmpty()) {
                accountSpaceRelRepository.insert(insertEntityList);
            }
        } finally {
            lock.unlock();
            log.debug("空间租赁结束释放账户锁，accountId={}", accountId);
        }
    }

    @Override
    public void unrentSpaces(@NotNull @Valid AccountSpaceUnrentDto unrentDto) {
        Set<Integer> requestedSpaceIdSet = new LinkedHashSet<>(unrentDto.getSpaceIds());

        Integer accountId = unrentDto.getAccountId();
        Lock lock = getAccountLock(accountId);
        if (!lock.tryLock()) {
            throw new BusinessRuntimeException("账户正在操作，请稍后重试");
        }

        try {
            accountInfoService.getById(accountId);

            List<AccountSpaceRelEntity> relationList = accountSpaceRelRepository.findListByAccountIdAndSpaceIds(accountId, requestedSpaceIdSet);
            if (CollectionUtils.isEmpty(relationList)) {
                return;
            }

            List<Integer> rentableSpaceIdList = relationList.stream()
                    .map(AccountSpaceRelEntity::getSpaceId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
            if (rentableSpaceIdList.isEmpty()) {
                return;
            }

            List<ElectricMeterBo> meterList = electricMeterInfoService.findList(
                    new ElectricMeterQueryDto()
                            .setAccountIds(List.of(accountId))
                            .setSpaceIds(rentableSpaceIdList)
            );

            if (!CollectionUtils.isEmpty(meterList)) {
                List<Integer> occupiedSpaceIdList = meterList.stream()
                        .map(ElectricMeterBo::getSpaceId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .sorted()
                        .toList();
                throw new BusinessRuntimeException("空间下存在已开户电表，禁止退租: " + joinIds(occupiedSpaceIdList));
            }

            accountSpaceRelRepository.deleteByAccountIdAndSpaceIds(accountId, rentableSpaceIdList);
        } finally {
            lock.unlock();
            log.debug("空间退租结束释放账户锁，accountId={}", accountId);
        }
    }

    private Lock getAccountLock(Integer accountId) {
        return lockTemplate.getLock(String.format(LOCK_ACCOUNT, accountId));
    }

    private void validateSpacesExist(Set<Integer> spaceIdSet) {
        List<SpaceBo> spaceList = spaceService.findSpaceList(new SpaceQueryDto().setIds(spaceIdSet));
        if (CollectionUtils.isEmpty(spaceList)) {
            throw new BusinessRuntimeException("空间不存在或已删除: " + joinIds(spaceIdSet.stream().sorted().toList()));
        }
        Set<Integer> existingSpaceIdSet = spaceList.stream()
                .map(SpaceBo::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<Integer> missingSpaceIdList = spaceIdSet.stream()
                .filter(spaceId -> !existingSpaceIdSet.contains(spaceId))
                .sorted()
                .toList();
        if (!missingSpaceIdList.isEmpty()) {
            throw new BusinessRuntimeException("空间不存在或已删除: " + joinIds(missingSpaceIdList));
        }
    }

    private AccountSpaceRelEntity buildRelationEntity(Integer accountId, Integer spaceId, LocalDateTime now) {
        Integer userId = requestContext.getUserId();
        String userRealName = requestContext.getUserRealName();
        return new AccountSpaceRelEntity()
                .setAccountId(accountId)
                .setSpaceId(spaceId)
                .setCreateUser(userId)
                .setCreateUserName(userRealName)
                .setCreateTime(now)
                .setUpdateUser(userId)
                .setUpdateUserName(userRealName)
                .setUpdateTime(now);
    }

    private String joinIds(List<Integer> idList) {
        return idList.stream().map(String::valueOf).collect(Collectors.joining(","));
    }
}
