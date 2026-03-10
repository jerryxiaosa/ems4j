package info.zhihui.ems.business.lease.service.impl;

import info.zhihui.ems.business.lease.dto.OwnerSpaceRelationDto;
import info.zhihui.ems.business.lease.entity.OwnerSpaceRelationEntity;
import info.zhihui.ems.business.lease.repository.OwnerSpaceRelationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OwnerSpaceRelationQueryServiceImplTest {

    @Mock
    private OwnerSpaceRelationRepository ownerSpaceRelationRepository;

    @InjectMocks
    private OwnerSpaceRelationQueryServiceImpl ownerSpaceRelationQueryService;

    @Test
    void testFindRelationListByOwnerTypesAndOwnerIds_WhenParamsValid_ShouldDelegateRepository() {
        List<OwnerSpaceRelationEntity> relationEntityList = List.of(
                new OwnerSpaceRelationEntity().setOwnerType(1).setOwnerId(1001).setSpaceId(101)
        );
        when(ownerSpaceRelationRepository.findListByOwnerTypesAndOwnerIds(List.of(1), List.of(1001))).thenReturn(relationEntityList);

        List<OwnerSpaceRelationDto> result = ownerSpaceRelationQueryService.findRelationListByOwnerTypesAndOwnerIds(List.of(1), List.of(1001));

        assertThat(result).containsExactly(
                new OwnerSpaceRelationDto().setOwnerType(1).setOwnerId(1001).setSpaceId(101)
        );
        verify(ownerSpaceRelationRepository).findListByOwnerTypesAndOwnerIds(List.of(1), List.of(1001));
    }
}
