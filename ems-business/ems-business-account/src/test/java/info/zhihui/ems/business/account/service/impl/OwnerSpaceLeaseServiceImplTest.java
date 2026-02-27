package info.zhihui.ems.business.account.service.impl;

import info.zhihui.ems.business.account.dto.OwnerSpaceRentDto;
import info.zhihui.ems.business.account.dto.OwnerSpaceUnrentDto;
import info.zhihui.ems.business.account.entity.OwnerSpaceRelEntity;
import info.zhihui.ems.business.account.qo.OwnerSpaceRelQueryQo;
import info.zhihui.ems.business.account.repository.OwnerSpaceRelRepository;
import info.zhihui.ems.business.device.bo.ElectricMeterBo;
import info.zhihui.ems.business.device.service.ElectricMeterInfoService;
import info.zhihui.ems.common.enums.OwnerTypeEnum;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.components.context.RequestContext;
import info.zhihui.ems.components.lock.core.LockTemplate;
import info.zhihui.ems.foundation.organization.bo.OrganizationBo;
import info.zhihui.ems.foundation.organization.service.OrganizationService;
import info.zhihui.ems.foundation.space.bo.SpaceBo;
import info.zhihui.ems.foundation.space.service.SpaceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OwnerSpaceLeaseServiceImplTest {

    @Mock
    private OwnerSpaceRelRepository ownerSpaceRelRepository;

    @Mock
    private SpaceService spaceService;

    @Mock
    private OrganizationService organizationService;

    @Mock
    private ElectricMeterInfoService electricMeterInfoService;

    @Mock
    private LockTemplate lockTemplate;

    @Mock
    private RequestContext requestContext;

    @Mock
    private Lock lock;

    @InjectMocks
    private OwnerSpaceLeaseServiceImpl ownerSpaceLeaseService;

    @Test
    void testRentSpaces_LockFailed() {
        when(lockTemplate.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock()).thenReturn(false);

        OwnerSpaceRentDto dto = new OwnerSpaceRentDto()
                .setOwnerType(OwnerTypeEnum.ENTERPRISE)
                .setOwnerId(1001)
                .setSpaceIds(List.of(101));

        assertThrows(BusinessRuntimeException.class, () -> ownerSpaceLeaseService.rentSpaces(dto));
        verify(ownerSpaceRelRepository, never()).insert(any(Collection.class));
    }

    @Test
    void testRentSpaces_Success() {
        when(organizationService.getDetail(anyInt())).thenReturn(new OrganizationBo());
        when(lockTemplate.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
        doNothing().when(lock).unlock();

        when(spaceService.findSpaceList(any())).thenReturn(List.of(
                new SpaceBo().setId(101),
                new SpaceBo().setId(102)
        ));
        when(ownerSpaceRelRepository.findListByOwnerAndSpaceIds(argThat(queryQo ->
                queryQo != null
                        && queryQo.getOwnerType() == null
                        && queryQo.getOwnerId() == null
                        && queryQo.getSpaceIds() != null
                        && queryQo.getSpaceIds().size() == 2
                        && queryQo.getSpaceIds().containsAll(List.of(101, 102))
        ))).thenReturn(List.of(
                new OwnerSpaceRelEntity().setOwnerType(OwnerTypeEnum.ENTERPRISE.getCode()).setOwnerId(1001).setSpaceId(101)
        ));
        when(requestContext.getUserId()).thenReturn(1);
        when(requestContext.getUserRealName()).thenReturn("admin");

        OwnerSpaceRentDto dto = new OwnerSpaceRentDto()
                .setOwnerType(OwnerTypeEnum.ENTERPRISE)
                .setOwnerId(1001)
                .setSpaceIds(List.of(101, 102, 102));
        ownerSpaceLeaseService.rentSpaces(dto);

        ArgumentCaptor<Collection<OwnerSpaceRelEntity>> captor = ArgumentCaptor.forClass(Collection.class);
        verify(ownerSpaceRelRepository).insert(captor.capture());
        List<OwnerSpaceRelEntity> insertList = captor.getValue().stream().toList();
        assertThat(insertList).hasSize(1);
        assertThat(insertList.get(0).getOwnerType()).isEqualTo(OwnerTypeEnum.ENTERPRISE.getCode());
        assertThat(insertList.get(0).getOwnerId()).isEqualTo(1001);
        assertThat(insertList.get(0).getSpaceId()).isEqualTo(102);
        verify(lock).unlock();
    }

    @Test
    void testRentSpaces_Conflict() {
        when(organizationService.getDetail(anyInt())).thenReturn(new OrganizationBo());
        when(lockTemplate.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
        doNothing().when(lock).unlock();

        when(spaceService.findSpaceList(any())).thenReturn(List.of(new SpaceBo().setId(101)));
        when(ownerSpaceRelRepository.findListByOwnerAndSpaceIds(argThat(queryQo ->
                queryQo != null
                        && queryQo.getOwnerType() == null
                        && queryQo.getOwnerId() == null
                        && queryQo.getSpaceIds() != null
                        && queryQo.getSpaceIds().size() == 1
                        && queryQo.getSpaceIds().contains(101)
        ))).thenReturn(List.of(
                new OwnerSpaceRelEntity().setOwnerType(OwnerTypeEnum.PERSONAL.getCode()).setOwnerId(2).setSpaceId(101)
        ));

        OwnerSpaceRentDto dto = new OwnerSpaceRentDto()
                .setOwnerType(OwnerTypeEnum.ENTERPRISE)
                .setOwnerId(1001)
                .setSpaceIds(List.of(101));

        assertThrows(BusinessRuntimeException.class, () -> ownerSpaceLeaseService.rentSpaces(dto));
        verify(ownerSpaceRelRepository, never()).insert(any(Collection.class));
        verify(lock).unlock();
    }

    @Test
    void testUnrentSpaces_Success() {
        when(organizationService.getDetail(anyInt())).thenReturn(new OrganizationBo());
        when(lockTemplate.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
        doNothing().when(lock).unlock();

        when(ownerSpaceRelRepository.findListByOwnerAndSpaceIds(argThat(queryQo ->
                queryQo != null
                        && OwnerTypeEnum.ENTERPRISE.getCode().equals(queryQo.getOwnerType())
                        && Integer.valueOf(1001).equals(queryQo.getOwnerId())
                        && queryQo.getSpaceIds() != null
                        && queryQo.getSpaceIds().size() == 2
                        && queryQo.getSpaceIds().containsAll(List.of(101, 102))
        ))).thenReturn(List.of(
                new OwnerSpaceRelEntity().setOwnerType(OwnerTypeEnum.ENTERPRISE.getCode()).setOwnerId(1001).setSpaceId(101),
                new OwnerSpaceRelEntity().setOwnerType(OwnerTypeEnum.ENTERPRISE.getCode()).setOwnerId(1001).setSpaceId(102)
        ));
        when(electricMeterInfoService.findList(any())).thenReturn(Collections.emptyList());

        OwnerSpaceUnrentDto dto = new OwnerSpaceUnrentDto()
                .setOwnerType(OwnerTypeEnum.ENTERPRISE)
                .setOwnerId(1001)
                .setSpaceIds(List.of(101, 102));

        ownerSpaceLeaseService.unrentSpaces(dto);
        verify(ownerSpaceRelRepository).deleteByOwnerAndSpaceIds(eq(OwnerTypeEnum.ENTERPRISE.getCode()), eq(1001), any());
        verify(lock).unlock();
    }

    @Test
    void testUnrentSpaces_BlockedByMeter() {
        when(organizationService.getDetail(anyInt())).thenReturn(new OrganizationBo());
        when(lockTemplate.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
        doNothing().when(lock).unlock();

        when(ownerSpaceRelRepository.findListByOwnerAndSpaceIds(argThat(queryQo ->
                queryQo != null
                        && OwnerTypeEnum.ENTERPRISE.getCode().equals(queryQo.getOwnerType())
                        && Integer.valueOf(1001).equals(queryQo.getOwnerId())
                        && queryQo.getSpaceIds() != null
                        && queryQo.getSpaceIds().size() == 1
                        && queryQo.getSpaceIds().contains(101)
        ))).thenReturn(List.of(
                new OwnerSpaceRelEntity().setOwnerType(OwnerTypeEnum.ENTERPRISE.getCode()).setOwnerId(1001).setSpaceId(101)
        ));
        when(electricMeterInfoService.findList(any())).thenReturn(List.of(
                new ElectricMeterBo().setId(10).setAccountId(1).setSpaceId(101)
        ));

        OwnerSpaceUnrentDto dto = new OwnerSpaceUnrentDto()
                .setOwnerType(OwnerTypeEnum.ENTERPRISE)
                .setOwnerId(1001)
                .setSpaceIds(List.of(101));

        assertThrows(BusinessRuntimeException.class, () -> ownerSpaceLeaseService.unrentSpaces(dto));
        verify(ownerSpaceRelRepository, never()).deleteByOwnerAndSpaceIds(any(), any(), any());
        verify(lock).unlock();
    }
}
