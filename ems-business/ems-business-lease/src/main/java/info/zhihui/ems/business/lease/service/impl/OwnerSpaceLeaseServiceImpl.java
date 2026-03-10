package info.zhihui.ems.business.lease.service.impl;

import info.zhihui.ems.business.device.bo.ElectricMeterBo;
import info.zhihui.ems.business.device.dto.ElectricMeterQueryDto;
import info.zhihui.ems.business.device.service.ElectricMeterInfoService;
import info.zhihui.ems.business.lease.dto.OwnerSpaceRentDto;
import info.zhihui.ems.business.lease.dto.OwnerSpaceUnrentDto;
import info.zhihui.ems.business.lease.entity.OwnerSpaceRelationEntity;
import info.zhihui.ems.business.lease.qo.OwnerSpaceRelationQueryQo;
import info.zhihui.ems.business.lease.repository.OwnerSpaceRelationRepository;
import info.zhihui.ems.business.lease.service.OwnerSpaceLeaseService;
import info.zhihui.ems.common.enums.OwnerTypeEnum;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.components.context.RequestContext;
import info.zhihui.ems.components.lock.core.LockTemplate;
import info.zhihui.ems.foundation.organization.service.OrganizationService;
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
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

/**
 * 主体空间租赁服务
 */
@Service
@Slf4j
@Validated
@RequiredArgsConstructor
public class OwnerSpaceLeaseServiceImpl implements OwnerSpaceLeaseService {

    private static final String LOCK_OWNER = "LOCK:OWNER-SPACE-LEASE:%s:%d";

    private final OwnerSpaceRelationRepository ownerSpaceRelationRepository;
    private final OrganizationService organizationService;
    private final SpaceService spaceService;
    private final ElectricMeterInfoService electricMeterInfoService;
    private final LockTemplate lockTemplate;
    private final RequestContext requestContext;

    @Override
    public void rentSpaces(@NotNull @Valid OwnerSpaceRentDto rentDto) {
        Lock lock = tryLockOwner(rentDto.getOwnerType(), rentDto.getOwnerId());

        try {
            validateOwnerExists(rentDto.getOwnerType(), rentDto.getOwnerId());
            Set<Integer> requestedSpaceIdSet = new HashSet<>(rentDto.getSpaceIds());
            validateSpacesExist(requestedSpaceIdSet);

            List<OwnerSpaceRelationEntity> existingRelationList = ownerSpaceRelationRepository.findListByOwnerAndSpaceIds(
                    new OwnerSpaceRelationQueryQo().setSpaceIds(requestedSpaceIdSet)
            );
            if (CollectionUtils.isEmpty(existingRelationList)) {
                existingRelationList = List.of();
            }
            List<Integer> occupiedSpaceIdList = existingRelationList.stream()
                    .filter(relation -> !isSameOwner(relation, rentDto.getOwnerType(), rentDto.getOwnerId()))
                    .map(OwnerSpaceRelationEntity::getSpaceId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .sorted()
                    .toList();
            if (!occupiedSpaceIdList.isEmpty()) {
                throw new BusinessRuntimeException("空间已被其他主体租赁: " + joinIds(occupiedSpaceIdList));
            }

            Set<Integer> alreadyRentedSpaceIdSet = existingRelationList.stream()
                    .map(OwnerSpaceRelationEntity::getSpaceId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            LocalDateTime now = LocalDateTime.now();
            List<OwnerSpaceRelationEntity> insertEntityList = new ArrayList<>();
            for (Integer spaceId : requestedSpaceIdSet) {
                if (alreadyRentedSpaceIdSet.contains(spaceId)) {
                    continue;
                }
                insertEntityList.add(buildRelationEntity(rentDto.getOwnerType(), rentDto.getOwnerId(), spaceId, now));
            }

            if (!insertEntityList.isEmpty()) {
                ownerSpaceRelationRepository.insert(insertEntityList);
            }
        } finally {
            lock.unlock();
            log.debug("空间租赁结束释放主体锁，ownerType={}, ownerId={}", rentDto.getOwnerType(), rentDto.getOwnerId());
        }
    }

    @Override
    public void unrentSpaces(@NotNull @Valid OwnerSpaceUnrentDto unrentDto) {
        Lock lock = tryLockOwner(unrentDto.getOwnerType(), unrentDto.getOwnerId());

        try {
            validateOwnerExists(unrentDto.getOwnerType(), unrentDto.getOwnerId());
            Set<Integer> requestedSpaceIdSet = new HashSet<>(unrentDto.getSpaceIds());
            List<OwnerSpaceRelationEntity> relationList = ownerSpaceRelationRepository.findListByOwnerAndSpaceIds(
                    new OwnerSpaceRelationQueryQo()
                            .setOwnerType(unrentDto.getOwnerType().getCode())
                            .setOwnerId(unrentDto.getOwnerId())
                            .setSpaceIds(requestedSpaceIdSet)
            );
            if (CollectionUtils.isEmpty(relationList)) {
                return;
            }

            List<Integer> rentableSpaceIdList = relationList.stream()
                    .map(OwnerSpaceRelationEntity::getSpaceId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
            if (rentableSpaceIdList.isEmpty()) {
                return;
            }

            List<ElectricMeterBo> meterList = electricMeterInfoService.findList(
                    new ElectricMeterQueryDto().setSpaceIds(rentableSpaceIdList)
            );
            if (CollectionUtils.isEmpty(meterList)) {
                ownerSpaceRelationRepository.deleteByOwnerAndSpaceIds(unrentDto.getOwnerType().getCode(), unrentDto.getOwnerId(), rentableSpaceIdList);
                return;
            }

            List<Integer> occupiedSpaceIdList = meterList.stream()
                    .filter(Objects::nonNull)
                    .filter(meter -> meter.getAccountId() != null)
                    .map(ElectricMeterBo::getSpaceId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .sorted()
                    .toList();
            if (!occupiedSpaceIdList.isEmpty()) {
                throw new BusinessRuntimeException("空间下存在已开户电表，禁止退租: " + joinIds(occupiedSpaceIdList));
            }
            ownerSpaceRelationRepository.deleteByOwnerAndSpaceIds(unrentDto.getOwnerType().getCode(), unrentDto.getOwnerId(), rentableSpaceIdList);
        } finally {
            lock.unlock();
            log.debug("空间退租结束释放主体锁，ownerType={}, ownerId={}", unrentDto.getOwnerType(), unrentDto.getOwnerId());
        }
    }

    private Lock tryLockOwner(OwnerTypeEnum ownerType, Integer ownerId) {
        Lock lock = lockTemplate.getLock(String.format(LOCK_OWNER, ownerType.name(), ownerId));
        if (!lock.tryLock()) {
            throw new BusinessRuntimeException("主体正在操作，请稍后重试");
        }
        return lock;
    }

    /**
     * 主体存在性校验（当前仅校验企业主体）。
     */
    private void validateOwnerExists(OwnerTypeEnum ownerType, Integer ownerId) {
        if (OwnerTypeEnum.ENTERPRISE.equals(ownerType)) {
            organizationService.getDetail(ownerId);
        }
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

    private boolean isSameOwner(OwnerSpaceRelationEntity relationEntity, OwnerTypeEnum ownerType, Integer ownerId) {
        return relationEntity != null
                && Objects.equals(relationEntity.getOwnerType(), ownerType.getCode())
                && Objects.equals(relationEntity.getOwnerId(), ownerId);
    }

    private OwnerSpaceRelationEntity buildRelationEntity(OwnerTypeEnum ownerType, Integer ownerId, Integer spaceId, LocalDateTime now) {
        Integer userId = requestContext.getUserId();
        String userRealName = requestContext.getUserRealName();
        return new OwnerSpaceRelationEntity()
                .setOwnerType(ownerType.getCode())
                .setOwnerId(ownerId)
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
