package info.zhihui.ems.foundation.space.service.impl;

import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.components.lock.core.LockTemplate;
import info.zhihui.ems.foundation.space.bo.SpaceBo;
import info.zhihui.ems.foundation.space.dto.SpaceCreateDto;
import info.zhihui.ems.foundation.space.dto.SpaceQueryDto;
import info.zhihui.ems.foundation.space.dto.SpaceUpdateDto;
import info.zhihui.ems.foundation.space.entity.SpaceEntity;
import info.zhihui.ems.foundation.space.enums.SpaceTypeEnum;
import info.zhihui.ems.foundation.space.mapstruct.SpaceMapper;
import info.zhihui.ems.foundation.space.repository.SpaceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.locks.Lock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("空间服务单元测试")
class SpaceServiceImplTest {

    @Mock
    private SpaceRepository spaceRepository;

    @Mock
    private SpaceMapper spaceMapper;

    @Mock
    private LockTemplate lockTemplate;

    @Mock
    private Lock lock;

    @InjectMocks
    private SpaceServiceImpl spaceService;

    @Test
    @DisplayName("新增空间-获取锁失败应抛异常")
    void testAddSpace_LockFail_ShouldThrow() {
        SpaceCreateDto createDto = new SpaceCreateDto()
                .setName("测试空间")
                .setPid(1)
                .setType(SpaceTypeEnum.ROOM);

        when(lockTemplate.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock()).thenReturn(false);

        assertThrows(BusinessRuntimeException.class, () -> spaceService.addSpace(createDto));

        verify(lock, never()).unlock();
        verifyNoInteractions(spaceRepository, spaceMapper);
    }

    @Test
    @DisplayName("更新空间-获取锁失败应抛异常")
    void testUpdateSpace_LockFail_ShouldThrow() {
        SpaceUpdateDto updateDto = new SpaceUpdateDto()
                .setId(1)
                .setName("测试空间")
                .setPid(1)
                .setType(SpaceTypeEnum.ROOM);

        when(lockTemplate.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock()).thenReturn(false);

        assertThrows(BusinessRuntimeException.class, () -> spaceService.updateSpace(updateDto));

        verify(lock, never()).unlock();
        verifyNoInteractions(spaceRepository, spaceMapper);
    }

    @Test
    @DisplayName("新增空间-异常时应释放锁")
    void testAddSpace_Exception_ShouldUnlock() {
        SpaceCreateDto createDto = new SpaceCreateDto()
                .setName("测试空间")
                .setPid(1)
                .setType(SpaceTypeEnum.ROOM);

        when(lockTemplate.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
        when(spaceRepository.selectById(1)).thenReturn(null);

        assertThrows(BusinessRuntimeException.class, () -> spaceService.addSpace(createDto));

        verify(lock).unlock();
    }

    @Test
    @DisplayName("更新空间-异常时应释放锁")
    void testUpdateSpace_Exception_ShouldUnlock() {
        SpaceUpdateDto updateDto = new SpaceUpdateDto()
                .setId(1)
                .setName("测试空间")
                .setPid(1)
                .setType(SpaceTypeEnum.ROOM);

        when(lockTemplate.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
        when(spaceRepository.selectById(1)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> spaceService.updateSpace(updateDto));

        verify(lock).unlock();
    }

    @Test
    @DisplayName("查询空间列表-批量填充祖先信息避免N+1")
    void testFindSpaceList_ShouldBatchFillAncestors() {
        SpaceEntity spaceEntity1 = new SpaceEntity()
                .setId(2)
                .setFullPath("1,2");
        SpaceEntity spaceEntity2 = new SpaceEntity()
                .setId(3)
                .setFullPath("1,3");

        SpaceBo spaceBo1 = new SpaceBo().setId(2).setFullPath("1,2");
        SpaceBo spaceBo2 = new SpaceBo().setId(3).setFullPath("1,3");

        SpaceEntity ancestor = new SpaceEntity().setId(1).setName("根空间");

        when(spaceRepository.selectByQo(any()))
                .thenReturn(List.of(spaceEntity1, spaceEntity2))
                .thenReturn(List.of(ancestor));
        when(spaceMapper.toBoList(List.of(spaceEntity1, spaceEntity2)))
                .thenReturn(List.of(spaceBo1, spaceBo2));

        List<SpaceBo> result = spaceService.findSpaceList(new SpaceQueryDto());

        assertEquals(2, result.size());
        assertEquals(List.of(1), result.get(0).getParentsIds());
        assertEquals(List.of("根空间"), result.get(0).getParentsNames());
        assertEquals(List.of(1), result.get(1).getParentsIds());
        assertEquals(List.of("根空间"), result.get(1).getParentsNames());
        verify(spaceRepository, times(2)).selectByQo(any());
    }
}
