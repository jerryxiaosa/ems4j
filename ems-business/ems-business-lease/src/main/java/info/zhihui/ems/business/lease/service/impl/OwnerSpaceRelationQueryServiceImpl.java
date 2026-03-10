package info.zhihui.ems.business.lease.service.impl;

import info.zhihui.ems.business.lease.dto.OwnerSpaceRelationDto;
import info.zhihui.ems.business.lease.entity.OwnerSpaceRelationEntity;
import info.zhihui.ems.business.lease.repository.OwnerSpaceRelationRepository;
import info.zhihui.ems.business.lease.service.OwnerSpaceRelationQueryService;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 主体空间租赁关系查询服务实现
 */
@Service
@Validated
@RequiredArgsConstructor
public class OwnerSpaceRelationQueryServiceImpl implements OwnerSpaceRelationQueryService {

    private final OwnerSpaceRelationRepository ownerSpaceRelationRepository;

    @Override
    public List<OwnerSpaceRelationDto> findRelationListByOwnerTypesAndOwnerIds(@NotEmpty Collection<Integer> ownerTypes,
                                                                               @NotEmpty Collection<Integer> ownerIds) {
        List<OwnerSpaceRelationEntity> relationEntityList = ownerSpaceRelationRepository.findListByOwnerTypesAndOwnerIds(ownerTypes, ownerIds);
        if (CollectionUtils.isEmpty(relationEntityList)) {
            return List.of();
        }

        List<OwnerSpaceRelationDto> relationDtoList = new ArrayList<>(relationEntityList.size());
        for (OwnerSpaceRelationEntity relationEntity : relationEntityList) {
            if (relationEntity == null) {
                continue;
            }
            relationDtoList.add(new OwnerSpaceRelationDto()
                    .setOwnerType(relationEntity.getOwnerType())
                    .setOwnerId(relationEntity.getOwnerId())
                    .setSpaceId(relationEntity.getSpaceId()));
        }
        return relationDtoList;
    }
}
