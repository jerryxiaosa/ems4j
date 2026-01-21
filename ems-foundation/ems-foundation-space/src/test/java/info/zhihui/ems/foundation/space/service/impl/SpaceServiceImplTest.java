package info.zhihui.ems.foundation.space.service.impl;

import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.foundation.space.bo.SpaceBo;
import info.zhihui.ems.foundation.space.dto.SpaceCreateDto;
import info.zhihui.ems.foundation.space.dto.SpaceQueryDto;
import info.zhihui.ems.foundation.space.dto.SpaceUpdateDto;
import info.zhihui.ems.foundation.space.entity.SpaceEntity;
import info.zhihui.ems.foundation.space.enums.SpaceTypeEnum;
import info.zhihui.ems.foundation.space.mapstruct.SpaceMapper;
import info.zhihui.ems.foundation.space.qo.SpaceQueryQo;
import info.zhihui.ems.foundation.space.repository.SpaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SpaceServiceImpl 单元测试类
 *
 * @author jerryxiaosa
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("空间服务实现类测试")
class SpaceServiceImplTest {

    @Mock
    private SpaceRepository spaceRepository;

    @Mock
    private SpaceMapper spaceMapper;

    @InjectMocks
    private SpaceServiceImpl spaceService;

    // 测试数据
    private SpaceEntity mockEntity;
    private SpaceBo mockBo;
    private SpaceCreateDto mockCreateDto;
    private SpaceUpdateDto mockUpdateDto;
    private SpaceQueryDto mockQueryDto;

    @BeforeEach
    void setUp() {
        // 初始化测试实体
        mockEntity = new SpaceEntity()
                .setId(1)
                .setName("测试空间")
                .setPid(0)
                .setFullPath("1")
                .setType(SpaceTypeEnum.MAIN.getCode())
                .setArea(new BigDecimal("100.00"))
                .setSortIndex(1);

        // 初始化测试BO
        mockBo = new SpaceBo()
                .setId(1)
                .setName("测试空间")
                .setPid(0)
                .setFullPath("1")
                .setType(SpaceTypeEnum.MAIN)
                .setArea(new BigDecimal("100.00"));

        // 初始化创建DTO
        mockCreateDto = new SpaceCreateDto()
                .setName("新空间")
                .setPid(0)
                .setType(SpaceTypeEnum.MAIN)
                .setArea(new BigDecimal("200.00"))
                .setSortIndex(1);

        // 初始化更新DTO
        mockUpdateDto = new SpaceUpdateDto()
                .setId(1)
                .setName("更新空间")
                .setPid(0)
                .setType(SpaceTypeEnum.MAIN)
                .setArea(new BigDecimal("150.00"))
                .setSortIndex(2);

        // 初始化查询DTO
        mockQueryDto = new SpaceQueryDto()
                .setIds(Set.of(1))
                .setPid(0)
                .setName("测试")
                .setType(List.of(SpaceTypeEnum.MAIN));
    }

    // ==================== getDetail 方法测试 ====================

    @Test
    @DisplayName("获取空间详情 - 成功")
    void testGetDetail_Success() {
        // 准备测试数据
        Integer spaceId = 1;

        // 设置 Mock 行为
        when(spaceRepository.selectById(spaceId)).thenReturn(mockEntity);
        when(spaceMapper.toBo(mockEntity)).thenReturn(mockBo);

        // 执行测试
        SpaceBo result = spaceService.getDetail(spaceId);

        // 验证结果
        assertNotNull(result);
        assertEquals(mockBo.getId(), result.getId());
        assertEquals(mockBo.getName(), result.getName());

        // 验证方法调用
        verify(spaceRepository).selectById(spaceId);
        verify(spaceMapper).toBo(mockEntity);
    }

    @Test
    @DisplayName("获取空间详情 - 空间不存在")
    void testGetDetail_SpaceNotFound() {
        // 准备测试数据
        Integer spaceId = 999;

        // 设置 Mock 行为
        when(spaceRepository.selectById(spaceId)).thenReturn(null);

        // 执行测试并验证异常
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> spaceService.getDetail(spaceId));
        assertEquals("空间不存在", exception.getMessage());

        // 验证方法调用
        verify(spaceRepository).selectById(spaceId);
        verify(spaceMapper, never()).toBo(any());
    }

    @Test
    @DisplayName("获取空间详情 - 填充祖先信息")
    void testGetDetail_FillAncestorInfo() {
        // 准备测试数据 - 有父空间的子空间
        SpaceEntity childEntity = new SpaceEntity()
                .setId(3)
                .setName("子空间")
                .setPid(1)
                .setFullPath("1,3")
                .setType(SpaceTypeEnum.ROOM.getCode());

        SpaceBo childBo = new SpaceBo()
                .setId(3)
                .setName("子空间")
                .setPid(1)
                .setFullPath("1,3")
                .setType(SpaceTypeEnum.ROOM);

        // 设置 Mock 行为
        when(spaceRepository.selectById(3)).thenReturn(childEntity);
        when(spaceMapper.toBo(childEntity)).thenReturn(childBo);
        when(spaceRepository.selectByQo(argThat(qo -> qo.getIds() != null && qo.getIds().contains(1))))
                .thenReturn(List.of(mockEntity));

        // 执行测试
        SpaceBo result = spaceService.getDetail(3);

        // 验证结果
        assertNotNull(result);
        assertEquals(3, result.getId());
        assertEquals("子空间", result.getName());
        assertEquals(List.of(1), result.getParentsIds());
        assertEquals(List.of("测试空间"), result.getParentsNames());

        // 验证祖先信息填充
        verify(spaceRepository).selectById(3);
        verify(spaceMapper).toBo(childEntity);
        verify(spaceRepository).selectByQo(argThat(qo -> qo.getIds() != null && qo.getIds().contains(1)));
    }

    // ==================== findSpaceList 方法测试 ====================

    @Test
    @DisplayName("查询空间列表 - 成功")
    void testFindSpaceList_Success() {
        // 准备测试数据
        List<SpaceEntity> mockEntities = List.of(mockEntity);
        List<SpaceBo> mockBoList = List.of(mockBo);

        // 设置 Mock 行为
        when(spaceRepository.selectByQo(any(SpaceQueryQo.class))).thenReturn(mockEntities);
        when(spaceMapper.toBoList(mockEntities)).thenReturn(mockBoList);

        // 执行测试
        List<SpaceBo> result = spaceService.findSpaceList(mockQueryDto);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(mockBo.getId(), result.get(0).getId());

        // 验证方法调用
        verify(spaceRepository, atLeastOnce()).selectByQo(any(SpaceQueryQo.class));
        verify(spaceMapper).toBoList(mockEntities);
    }

    @Test
    @DisplayName("查询空间列表 - 空结果")
    void testFindSpaceList_EmptyResult() {
        // 设置 Mock 行为
        when(spaceRepository.selectByQo(any(SpaceQueryQo.class))).thenReturn(Collections.emptyList());

        // 执行测试
        List<SpaceBo> result = spaceService.findSpaceList(mockQueryDto);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // 验证方法调用
        verify(spaceRepository).selectByQo(any(SpaceQueryQo.class));
        verify(spaceMapper, never()).toBoList(any());
    }

    // ==================== addSpace 方法测试 ====================

    @Test
    @DisplayName("创建空间 - 成功（顶级空间）")
    void testAddSpace_Success_TopLevel() {
        // 准备测试数据
        SpaceEntity newEntity = new SpaceEntity()
                .setId(2)
                .setName("新空间")
                .setPid(0)
                .setFullPath(null)
                .setType(SpaceTypeEnum.MAIN.getCode());

        // 设置 Mock 行为
        when(spaceRepository.countByParentAndName(0, "新空间")).thenReturn(0);
        when(spaceMapper.toEntity(mockCreateDto)).thenReturn(newEntity);
        when(spaceRepository.insert(any(SpaceEntity.class))).thenReturn(1);
        when(spaceRepository.updateById(any(SpaceEntity.class))).thenReturn(1);

        // 执行测试
        Integer result = spaceService.addSpace(mockCreateDto);

        // 验证结果
        assertEquals(2, result);

        // 验证方法调用
        verify(spaceRepository).countByParentAndName(0, "新空间");
        verify(spaceMapper).toEntity(mockCreateDto);
        verify(spaceRepository).insert(any(SpaceEntity.class));
        verify(spaceRepository).updateById(argThat((SpaceEntity entity) ->
                entity.getId().equals(2)
                        && "2".equals(entity.getFullPath())
                        && entity.getOwnAreaId().equals(2)));
    }

    @Test
    @DisplayName("创建空间 - 成功（子空间）")
    void testAddSpace_Success_SubSpace() {
        // 准备测试数据
        mockCreateDto.setPid(1); // 设置父空间ID
        SpaceEntity parentEntity = mockEntity;
        SpaceEntity newEntity = new SpaceEntity()
                .setId(3)
                .setName("新空间")
                .setPid(1)
                .setFullPath(null)
                .setType(SpaceTypeEnum.ROOM.getCode());

        // 设置 Mock 行为
        when(spaceRepository.selectById(1)).thenReturn(parentEntity);
        when(spaceRepository.countByParentAndName(1, "新空间")).thenReturn(0);
        when(spaceMapper.toEntity(mockCreateDto)).thenReturn(newEntity);
        when(spaceRepository.insert(any(SpaceEntity.class))).thenReturn(1);
        when(spaceRepository.updateById(any(SpaceEntity.class))).thenReturn(1);

        // 执行测试
        Integer result = spaceService.addSpace(mockCreateDto);

        // 验证结果
        assertEquals(3, result);

        // 验证方法调用
        verify(spaceRepository).selectById(1);
        verify(spaceRepository).countByParentAndName(1, "新空间");
        verify(spaceMapper).toEntity(mockCreateDto);
        verify(spaceRepository).insert(any(SpaceEntity.class));
        verify(spaceRepository).updateById(argThat((SpaceEntity entity) ->
                entity.getId().equals(3)
                        && "1,3".equals(entity.getFullPath())
                        && entity.getOwnAreaId().equals(3)));
    }

    @Test
    @DisplayName("创建空间 - 父fullPath缺失使用父ID拼接")
    void testAddSpace_ParentFullPathFallback() {
        mockCreateDto.setPid(1).setName("子空间");

        SpaceEntity parentEntity = new SpaceEntity()
                .setId(1)
                .setName("父空间")
                .setPid(0)
                .setFullPath(null)
                .setType(SpaceTypeEnum.MAIN.getCode());

        SpaceEntity newEntity = new SpaceEntity()
                .setId(5)
                .setName("子空间")
                .setPid(1)
                .setFullPath(null)
                .setType(SpaceTypeEnum.ROOM.getCode());

        when(spaceRepository.selectById(1)).thenReturn(parentEntity);
        when(spaceRepository.countByParentAndName(1, "子空间")).thenReturn(0);
        when(spaceMapper.toEntity(mockCreateDto)).thenReturn(newEntity);
        when(spaceRepository.insert(any(SpaceEntity.class))).thenReturn(1);

        assertThrows(BusinessRuntimeException.class, () -> spaceService.addSpace(mockCreateDto));

    }

    @Test
    @DisplayName("创建空间 - 父空间不存在")
    void testAddSpace_ParentNotFound() {
        // 准备测试数据
        mockCreateDto.setPid(999); // 不存在的父空间ID

        // 设置 Mock 行为
        when(spaceRepository.selectById(999)).thenReturn(null);

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> spaceService.addSpace(mockCreateDto));
        assertEquals("父空间不存在", exception.getMessage());

        // 验证方法调用
        verify(spaceRepository).selectById(999);
        verify(spaceRepository, never()).countByParentAndName(anyInt(), anyString());
        verify(spaceRepository, never()).insert(any(SpaceEntity.class));
    }

    @Test
    @DisplayName("创建空间 - 同级名称重复")
    void testAddSpace_DuplicateName() {
        // 设置 Mock 行为
        when(spaceRepository.countByParentAndName(0, "新空间")).thenReturn(1);

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> spaceService.addSpace(mockCreateDto));
        assertEquals("同级空间名称不能重复", exception.getMessage());

        // 验证方法调用
        verify(spaceRepository).countByParentAndName(0, "新空间");
        verify(spaceRepository, never()).insert(any(SpaceEntity.class));
    }

    @Test
    @DisplayName("创建空间 - 插入失败")
    void testAddSpace_InsertFailed() {
        // 准备测试数据
        SpaceEntity newEntity = new SpaceEntity()
                .setName("新空间")
                .setPid(0)
                .setType(SpaceTypeEnum.MAIN.getCode());

        // 设置 Mock 行为
        when(spaceRepository.countByParentAndName(0, "新空间")).thenReturn(0);
        when(spaceMapper.toEntity(mockCreateDto)).thenReturn(newEntity);
        when(spaceRepository.insert(any(SpaceEntity.class))).thenReturn(0); // 插入失败

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> spaceService.addSpace(mockCreateDto));
        assertEquals("创建空间失败", exception.getMessage());

        // 验证方法调用
        verify(spaceRepository).insert(any(SpaceEntity.class));
    }

    // ==================== updateSpace 方法测试 ====================

    @Test
    @DisplayName("更新空间 - 成功")
    void testUpdateSpace_Success() {
        // 准备测试数据
        SpaceEntity updateEntity = new SpaceEntity()
                .setId(1)
                .setName("更新空间")
                .setPid(0)
                .setType(SpaceTypeEnum.MAIN.getCode());

        // 设置 Mock 行为
        when(spaceRepository.selectById(1)).thenReturn(mockEntity);
        when(spaceRepository.countByParentAndName(0, "更新空间")).thenReturn(0);
        when(spaceMapper.toEntity(mockUpdateDto)).thenReturn(updateEntity);
        when(spaceRepository.updateById(any(SpaceEntity.class))).thenReturn(1);

        // 执行测试
        assertDoesNotThrow(() -> spaceService.updateSpace(mockUpdateDto));

        // 验证方法调用
        verify(spaceRepository).selectById(1);
        verify(spaceRepository).countByParentAndName(0, "更新空间");
        verify(spaceMapper).toEntity(mockUpdateDto);
        verify(spaceRepository).updateById(any(SpaceEntity.class));
    }

    @Test
    @DisplayName("更新空间 - 空间不存在")
    void testUpdateSpace_SpaceNotFound() {
        // 设置 Mock 行为
        when(spaceRepository.selectById(1)).thenReturn(null);

        // 执行测试并验证异常
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> spaceService.updateSpace(mockUpdateDto));
        assertEquals("空间不存在", exception.getMessage());

        // 验证方法调用
        verify(spaceRepository).selectById(1);
        verify(spaceRepository, never()).updateById(any(SpaceEntity.class));
    }

    @Test
    @DisplayName("更新空间 - 父空间不存在")
    void testUpdateSpace_ParentNotFound() {
        // 准备测试数据
        mockUpdateDto.setPid(999); // 不存在的父空间ID

        // 设置 Mock 行为
        when(spaceRepository.selectById(1)).thenReturn(mockEntity);
        when(spaceRepository.selectById(999)).thenReturn(null);

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> spaceService.updateSpace(mockUpdateDto));
        assertEquals("父空间不存在", exception.getMessage());

        // 验证方法调用
        verify(spaceRepository).selectById(1);
        verify(spaceRepository).selectById(999);
        verify(spaceRepository, never()).updateById(any(SpaceEntity.class));
    }

    @Test
    @DisplayName("更新空间 - 循环引用")
    void testUpdateSpace_CircularReference() {
        // 准备测试数据 - 尝试将空间1移动到空间2下，但空间2是空间1的子空间
        mockUpdateDto.setPid(2);

        SpaceEntity parentEntity = new SpaceEntity()
                .setId(2)
                .setName("子空间")
                .setFullPath("1,2")
                .setPid(1); // 空间2的父空间是空间1

        // 设置 Mock 行为
        when(spaceRepository.selectById(1)).thenReturn(mockEntity);
        when(spaceRepository.selectById(2)).thenReturn(parentEntity);

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> spaceService.updateSpace(mockUpdateDto));
        assertEquals("不能将空间移动到其子空间下", exception.getMessage());

        // 验证方法调用
        verify(spaceRepository).selectById(1);
        verify(spaceRepository).selectById(2);
        verify(spaceRepository, never()).updateById(any(SpaceEntity.class));
    }

    @Test
    @DisplayName("更新空间 - 同级名称重复")
    void testUpdateSpace_DuplicateName() {
        // 准备测试数据
        mockUpdateDto.setName("重复名称");

        // 设置 Mock 行为
        when(spaceRepository.selectById(1)).thenReturn(mockEntity);
        when(spaceRepository.countByParentAndName(0, "重复名称")).thenReturn(1);

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> spaceService.updateSpace(mockUpdateDto));
        assertEquals("同级空间名称不能重复", exception.getMessage());

        // 验证方法调用
        verify(spaceRepository).selectById(1);
        verify(spaceRepository).countByParentAndName(0, "重复名称");
        verify(spaceRepository, never()).updateById(any(SpaceEntity.class));
    }

    @Test
    @DisplayName("更新空间 - 更新失败")
    void testUpdateSpace_UpdateFailed() {
        // 准备测试数据
        SpaceEntity updateEntity = new SpaceEntity()
                .setId(1)
                .setName("更新空间")
                .setPid(0);

        // 设置 Mock 行为
        when(spaceRepository.selectById(1)).thenReturn(mockEntity);
        when(spaceRepository.countByParentAndName(0, "更新空间")).thenReturn(0);
        when(spaceMapper.toEntity(mockUpdateDto)).thenReturn(updateEntity);
        when(spaceRepository.updateById(any(SpaceEntity.class))).thenReturn(0); // 更新失败

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> spaceService.updateSpace(mockUpdateDto));
        assertEquals("更新空间失败", exception.getMessage());

        // 验证方法调用
        verify(spaceRepository).updateById(any(SpaceEntity.class));
    }

    @Test
    @DisplayName("更新空间 - 需要更新子空间路径")
    void testUpdateSpace_UpdateChildrenPath() {
        // 准备测试数据 - 修改名称，需要更新fullPath
        mockUpdateDto.setName("新名称").setPid(100);

        SpaceEntity updateEntity = new SpaceEntity()
                .setId(1)
                .setName("新名称")
                .setPid(100);

        List<SpaceEntity> children = List.of(
                new SpaceEntity().setId(3).setName("子空间1").setPid(1).setFullPath("1,3"),
                new SpaceEntity().setId(4).setName("子空间2").setPid(1).setFullPath("1,4")
        );

        // 设置 Mock 行为
        when(spaceRepository.selectById(1)).thenReturn(mockEntity);
        when(spaceRepository.selectById(100)).thenReturn(new SpaceEntity().setId(100).setPid(0).setName("new").setFullPath("100"));
        when(spaceRepository.countByParentAndName(100, "新名称")).thenReturn(0);
        when(spaceMapper.toEntity(mockUpdateDto)).thenReturn(updateEntity);
        when(spaceRepository.updateById(any(SpaceEntity.class))).thenReturn(1);
        when(spaceRepository.selectByQo(any(SpaceQueryQo.class))).thenReturn(children);
        doNothing().when(spaceRepository).updateFullPathBatch(anyList());

        // 执行测试
        assertDoesNotThrow(() -> spaceService.updateSpace(mockUpdateDto));

        // 验证方法调用
        verify(spaceRepository).updateById(any(SpaceEntity.class));
        verify(spaceRepository, atLeastOnce()).selectByQo(any(SpaceQueryQo.class));
        verify(spaceRepository).updateFullPathBatch(argThat((List<SpaceEntity> list) -> {
            List<String> paths = list.stream().map(SpaceEntity::getFullPath).toList();
            return paths.containsAll(List.of("100,1,3", "100,1,4")) && paths.size() == 2;
        }));
    }

    @Test
    @DisplayName("更新空间 - 移动到根节点")
    void testUpdateSpace_MoveToRoot() {
        SpaceEntity existing = new SpaceEntity()
                .setId(11)
                .setName("原空间")
                .setPid(22)
                .setFullPath("5,22,11")
                .setType(SpaceTypeEnum.MAIN.getCode());

        SpaceUpdateDto dto = new SpaceUpdateDto()
                .setId(11)
                .setName("原空间")
                .setPid(0)
                .setType(SpaceTypeEnum.MAIN)
                .setArea(new BigDecimal("90"));

        SpaceEntity updateEntity = new SpaceEntity()
                .setId(11)
                .setName("原空间")
                .setPid(0)
                .setType(SpaceTypeEnum.MAIN.getCode());

        when(spaceRepository.selectById(11)).thenReturn(existing);
        when(spaceRepository.countByParentAndName(0, "原空间")).thenReturn(0);
        when(spaceMapper.toEntity(dto)).thenReturn(updateEntity);
        when(spaceRepository.updateById(any(SpaceEntity.class))).thenReturn(1);
        when(spaceRepository.selectByQo(argThat(qo -> "5,22,11".equals(qo.getFullPathPrefix()))))
                .thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> spaceService.updateSpace(dto));

        verify(spaceRepository).updateById(argThat((SpaceEntity entity) ->
                entity.getPid().equals(0)
                        && "11".equals(entity.getFullPath())
                        && entity.getOwnAreaId().equals(11)));
        verify(spaceRepository).selectByQo(argThat(qo -> "5,22,11".equals(qo.getFullPathPrefix())));
        verify(spaceRepository, never()).updateFullPathBatch(anyList());
    }

    @Test
    @DisplayName("更新空间 - pid为空保持原父级")
    void testUpdateSpace_KeepParentWhenPidNull() {
        SpaceEntity existing = new SpaceEntity()
                .setId(10)
                .setName("原空间")
                .setPid(20)
                .setFullPath("5,20,10")
                .setType(SpaceTypeEnum.MAIN.getCode());

        SpaceUpdateDto dto = new SpaceUpdateDto()
                .setId(10)
                .setName("更新空间")
                .setPid(20)
                .setType(SpaceTypeEnum.MAIN)
                .setArea(new BigDecimal("120"))
                .setSortIndex(3);

        SpaceEntity updateEntity = new SpaceEntity()
                .setId(10)
                .setName("更新空间")
                .setPid(20)
                .setType(SpaceTypeEnum.MAIN.getCode());

        when(spaceRepository.selectById(10)).thenReturn(existing);
        when(spaceRepository.countByParentAndName(20, "更新空间")).thenReturn(0);
        when(spaceMapper.toEntity(dto)).thenReturn(updateEntity);
        when(spaceRepository.updateById(any(SpaceEntity.class))).thenReturn(1);

        assertDoesNotThrow(() -> spaceService.updateSpace(dto));

        verify(spaceRepository).countByParentAndName(20, "更新空间");
        verify(spaceRepository).updateById(argThat((SpaceEntity entity) ->
                entity.getPid().equals(20) && entity.getOwnAreaId().equals(10)));
    }

    // ==================== deleteSpace 方法测试 ====================

    @Test
    @DisplayName("删除空间 - 成功")
    void testDeleteSpace_Success() {
        // 设置 Mock 行为
        when(spaceRepository.selectById(1)).thenReturn(mockEntity);
        when(spaceRepository.countChildrenByPid(1)).thenReturn(0);
        when(spaceRepository.deleteById(1)).thenReturn(1);

        // 执行测试
        assertDoesNotThrow(() -> spaceService.deleteSpace(1));

        // 验证方法调用
        verify(spaceRepository).selectById(1);
        verify(spaceRepository).countChildrenByPid(1);
        verify(spaceRepository).deleteById(1);
    }

    @Test
    @DisplayName("删除空间 - 空间不存在")
    void testDeleteSpace_SpaceNotFound() {
        // 设置 Mock 行为
        when(spaceRepository.selectById(999)).thenReturn(null);

        // 执行测试并验证异常
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> spaceService.deleteSpace(999));
        assertEquals("空间不存在", exception.getMessage());

        // 验证方法调用
        verify(spaceRepository).selectById(999);
        verify(spaceRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("删除空间 - 存在子空间")
    void testDeleteSpace_HasChildren() {
        // 设置 Mock 行为
        when(spaceRepository.selectById(1)).thenReturn(mockEntity);
        when(spaceRepository.countChildrenByPid(1)).thenReturn(2); // 有2个子空间

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> spaceService.deleteSpace(1));
        assertEquals("存在子空间，无法删除", exception.getMessage());

        // 验证方法调用
        verify(spaceRepository).selectById(1);
        verify(spaceRepository).countChildrenByPid(1);
        verify(spaceRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("删除空间 - 删除失败")
    void testDeleteSpace_DeleteFailed() {
        // 设置 Mock 行为
        when(spaceRepository.selectById(1)).thenReturn(mockEntity);
        when(spaceRepository.countChildrenByPid(1)).thenReturn(0);
        when(spaceRepository.deleteById(1)).thenReturn(0); // 删除失败

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> spaceService.deleteSpace(1));
        assertEquals("删除空间失败", exception.getMessage());

        // 验证方法调用
        verify(spaceRepository).deleteById(1);
    }

    // ==================== 解析所属主区域（resolveOwnAreaId）与子路径重建覆盖 ====================

    @Test
    @DisplayName("创建空间 - 非主类型子空间 ownAreaId 解析成功")
    void testAddSpace_NonMainTypeSubSpace_OwnAreaResolved() {
        // 构造非主类型创建DTO，父为主区域
        SpaceCreateDto dto = new SpaceCreateDto()
                .setName("子空间N")
                .setPid(1)
                .setType(SpaceTypeEnum.ROOM)
                .setArea(new BigDecimal("10"))
                .setSortIndex(1);

        // 父空间为主区域，fullPath=1
        SpaceEntity parent = new SpaceEntity()
                .setId(1)
                .setName("父主区域")
                .setPid(0)
                .setFullPath("1")
                .setType(SpaceTypeEnum.MAIN.getCode());

        // 新建实体，id=33，类型为ROOM
        SpaceEntity newEntity = new SpaceEntity()
                .setId(33)
                .setName("子空间N")
                .setPid(1)
                .setType(SpaceTypeEnum.ROOM.getCode());

        when(spaceRepository.selectById(1)).thenReturn(parent);
        when(spaceRepository.countByParentAndName(1, "子空间N")).thenReturn(0);
        when(spaceMapper.toEntity(dto)).thenReturn(newEntity);
        when(spaceRepository.insert(any(SpaceEntity.class))).thenReturn(1);
        // resolveOwnAreaId 会按祖先ID查询（应包含父ID 1）
        when(spaceRepository.selectByQo(argThat(qo -> qo.getIds() != null && qo.getIds().size() == 1 && qo.getIds().contains(1))))
                .thenReturn(List.of(parent));
        when(spaceRepository.updateById(any(SpaceEntity.class))).thenReturn(1);

        Integer id = spaceService.addSpace(dto);
        assertEquals(33, id);

        // 验证 ownAreaId 解析为父主区域ID=1，fullPath 拼接为 1,33
        verify(spaceRepository).updateById(argThat((SpaceEntity entity) ->
                entity.getId().equals(33)
                        && "1,33".equals(entity.getFullPath())
                        && entity.getOwnAreaId().equals(1)));
    }

    @Test
    @DisplayName("创建空间 - 非主类型顶级空间 ownAreaId 解析失败")
    void testAddSpace_NonMainTypeTopLevel_OwnAreaResolveFail() {
        // 非主类型顶级空间，fullPath 只有自身，无法解析主区域
        SpaceCreateDto dto = new SpaceCreateDto()
                .setName("顶级房间")
                .setPid(0)
                .setType(SpaceTypeEnum.ROOM)
                .setArea(new BigDecimal("8"))
                .setSortIndex(1);

        SpaceEntity newEntity = new SpaceEntity()
                .setId(44)
                .setName("顶级房间")
                .setPid(0)
                .setType(SpaceTypeEnum.ROOM.getCode());

        when(spaceRepository.countByParentAndName(0, "顶级房间")).thenReturn(0);
        when(spaceMapper.toEntity(dto)).thenReturn(newEntity);
        when(spaceRepository.insert(any(SpaceEntity.class))).thenReturn(1);

        BusinessRuntimeException ex = assertThrows(BusinessRuntimeException.class, () -> spaceService.addSpace(dto));
        assertEquals("未找到关联的主区域，请检查空间层级配置", ex.getMessage());

        verify(spaceRepository, never()).updateById(any(SpaceEntity.class));
    }

    @Test
    @DisplayName("更新空间 - 非主类型解析祖先主区域成功")
    void testUpdateSpace_NonMainType_ResolveMainAncestorSuccess() {
        // 现有空间 fullPath=5,12,1，其中祖先 12 为主区域
        SpaceEntity existing = new SpaceEntity()
                .setId(1)
                .setName("房间A")
                .setPid(22)
                .setFullPath("5,12,1")
                .setType(SpaceTypeEnum.ROOM.getCode());

        SpaceUpdateDto dto = new SpaceUpdateDto()
                .setId(1)
                .setName("房间A")
                .setPid(22) // 不改变父级
                .setType(SpaceTypeEnum.ROOM)
                .setArea(new BigDecimal("9"));

        SpaceEntity updateEntity = new SpaceEntity()
                .setId(1)
                .setName("房间A")
                .setPid(22)
                .setType(SpaceTypeEnum.ROOM.getCode());

        when(spaceRepository.selectById(1)).thenReturn(existing);
        when(spaceRepository.countByParentAndName(22, "房间A")).thenReturn(0);
        when(spaceMapper.toEntity(dto)).thenReturn(updateEntity);
        // 祖先ID查询应包含 5 与 12
        when(spaceRepository.selectByQo(argThat(qo -> qo.getIds() != null && qo.getIds().containsAll(List.of(5, 12)))))
                .thenReturn(List.of(new SpaceEntity().setId(12).setType(SpaceTypeEnum.MAIN.getCode())));
        when(spaceRepository.updateById(any(SpaceEntity.class))).thenReturn(1);

        assertDoesNotThrow(() -> spaceService.updateSpace(dto));

        // 验证 ownAreaId 解析为 12
        verify(spaceRepository).updateById(argThat((SpaceEntity entity) ->
                entity.getId().equals(1)
                        && entity.getOwnAreaId().equals(12)
                        && "5,12,1".equals(entity.getFullPath())));
        // 父级未变化，不更新子路径
        verify(spaceRepository, never()).updateFullPathBatch(anyList());
    }

    @Test
    @DisplayName("更新空间 - 非主类型祖先无主区域抛异常")
    void testUpdateSpace_NonMainType_NoMainAncestorFail() {
        SpaceEntity existing = new SpaceEntity()
                .setId(2)
                .setName("房间B")
                .setPid(33)
                .setFullPath("7,9,2")
                .setType(SpaceTypeEnum.ROOM.getCode());

        SpaceUpdateDto dto = new SpaceUpdateDto()
                .setId(2)
                .setName("房间B")
                .setPid(33)
                .setType(SpaceTypeEnum.ROOM)
                .setArea(new BigDecimal("6"));

        SpaceEntity updateEntity = new SpaceEntity()
                .setId(2)
                .setName("房间B")
                .setPid(33)
                .setType(SpaceTypeEnum.ROOM.getCode());

        when(spaceRepository.selectById(2)).thenReturn(existing);
        when(spaceRepository.countByParentAndName(33, "房间B")).thenReturn(0);
        when(spaceMapper.toEntity(dto)).thenReturn(updateEntity);
        // 返回的祖先列表无主区域类型
        when(spaceRepository.selectByQo(argThat(qo -> qo.getIds() != null && qo.getIds().containsAll(List.of(7, 9)))))
                .thenReturn(List.of(new SpaceEntity().setId(7).setType(SpaceTypeEnum.ROOM.getCode())));

        BusinessRuntimeException ex = assertThrows(BusinessRuntimeException.class, () -> spaceService.updateSpace(dto));
        assertEquals("未找到关联的主区域，请检查空间层级配置", ex.getMessage());

        verify(spaceRepository, never()).updateById(any(SpaceEntity.class));
        verify(spaceRepository, never()).updateFullPathBatch(anyList());
    }

    @Test
    @DisplayName("更新空间 - 子路径重建跳过非法fullPath子项")
    void testUpdateSpace_RebuildChildrenPath_SkipInvalidFullPath() {
        // 修改父级到 100，需重建子路径
        mockUpdateDto.setName("新名称").setPid(100);

        SpaceEntity updateEntity = new SpaceEntity()
                .setId(1)
                .setName("新名称")
                .setPid(100);

        List<SpaceEntity> children = List.of(
                new SpaceEntity().setId(3).setName("子1").setPid(1).setFullPath(null),
                new SpaceEntity().setId(4).setName("子2").setPid(1).setFullPath(""),
                new SpaceEntity().setId(5).setName("子3").setPid(1).setFullPath("1,5")
        );

        when(spaceRepository.selectById(1)).thenReturn(mockEntity);
        when(spaceRepository.selectById(100)).thenReturn(new SpaceEntity().setId(100).setPid(0).setName("P100").setFullPath("100"));
        when(spaceRepository.countByParentAndName(100, "新名称")).thenReturn(0);
        when(spaceMapper.toEntity(mockUpdateDto)).thenReturn(updateEntity);
        when(spaceRepository.updateById(any(SpaceEntity.class))).thenReturn(1);
        when(spaceRepository.selectByQo(any(SpaceQueryQo.class))).thenReturn(children);
        doNothing().when(spaceRepository).updateFullPathBatch(anyList());

        assertDoesNotThrow(() -> spaceService.updateSpace(mockUpdateDto));

        // 仅有效子项被更新：100,1,5
        verify(spaceRepository).updateFullPathBatch(argThat((List<SpaceEntity> list) ->
                list.size() == 1 && "100,1,5".equals(list.get(0).getFullPath())));
    }

    @Test
    @DisplayName("更新空间 - 子路径重建包含父节点与后缀裁剪")
    void testUpdateSpace_RebuildChildrenPath_IncludeParentAndSuffixTrim() {
        // 现有父路径为 1，变更父到 200
        SpaceEntity existing = new SpaceEntity()
                .setId(1)
                .setName("原空间")
                .setPid(22)
                .setFullPath("1")
                .setType(SpaceTypeEnum.MAIN.getCode());

        SpaceUpdateDto dto = new SpaceUpdateDto()
                .setId(1)
                .setName("原空间")
                .setPid(200)
                .setType(SpaceTypeEnum.MAIN)
                .setArea(new BigDecimal("90"));

        SpaceEntity updateEntity = new SpaceEntity()
                .setId(1)
                .setName("原空间")
                .setPid(200)
                .setType(SpaceTypeEnum.MAIN.getCode());

        List<SpaceEntity> descendants = List.of(
                // 返回列表包含父自身 fullPath=1 以及其子 1,7
                new SpaceEntity().setId(1).setName("原空间").setPid(22).setFullPath("1").setType(SpaceTypeEnum.MAIN.getCode()),
                new SpaceEntity().setId(7).setName("子7").setPid(1).setFullPath("1,7")
        );

        when(spaceRepository.selectById(1)).thenReturn(existing);
        when(spaceRepository.selectById(200)).thenReturn(new SpaceEntity().setId(200).setPid(0).setName("P200").setFullPath("200"));
        when(spaceRepository.countByParentAndName(200, "原空间")).thenReturn(0);
        when(spaceMapper.toEntity(dto)).thenReturn(updateEntity);
        when(spaceRepository.updateById(any(SpaceEntity.class))).thenReturn(1);
        when(spaceRepository.selectByQo(argThat(qo -> "1".equals(qo.getFullPathPrefix())))).thenReturn(descendants);

        doNothing().when(spaceRepository).updateFullPathBatch(anyList());

        assertDoesNotThrow(() -> spaceService.updateSpace(dto));

        // 验证父与子均被重建：200,1 与 200,1,7
        verify(spaceRepository).updateFullPathBatch(argThat((List<SpaceEntity> list) -> {
            List<String> paths = list.stream().map(SpaceEntity::getFullPath).toList();
            return paths.containsAll(List.of("200,1", "200,1,7")) && paths.size() == 2;
        }));
    }

    @Test
    @DisplayName("获取空间详情 - fullPath 非法片段忽略")
    void testGetDetail_ParseAncestorIds_IllegalSegmentsIgnored() {
        SpaceEntity childEntity = new SpaceEntity()
                .setId(3)
                .setName("子空间")
                .setPid(1)
                .setFullPath("1,a,3")
                .setType(SpaceTypeEnum.ROOM.getCode());

        SpaceBo childBo = new SpaceBo()
                .setId(3)
                .setName("子空间")
                .setPid(1)
                .setFullPath("1,a,3")
                .setType(SpaceTypeEnum.ROOM);

        when(spaceRepository.selectById(3)).thenReturn(childEntity);
        when(spaceMapper.toBo(childEntity)).thenReturn(childBo);
        // 祖先ID应仅包含合法的 1
        when(spaceRepository.selectByQo(argThat(qo -> qo.getIds() != null && qo.getIds().size() == 1 && qo.getIds().contains(1))))
                .thenReturn(List.of(mockEntity));

        SpaceBo result = spaceService.getDetail(3);
        assertNotNull(result);
        assertEquals(List.of(1), result.getParentsIds());
        assertEquals(List.of("测试空间"), result.getParentsNames());
    }

    @Test
    @DisplayName("获取空间详情 - fullPath仅自身填充空祖先信息")
    void testGetDetail_ParseAncestorIds_EmptyAncestors() {
        SpaceEntity topEntity = new SpaceEntity()
                .setId(55)
                .setName("顶级")
                .setPid(0)
                .setFullPath("55")
                .setType(SpaceTypeEnum.MAIN.getCode());

        SpaceBo topBo = new SpaceBo()
                .setId(55)
                .setName("顶级")
                .setPid(0)
                .setFullPath("55")
                .setType(SpaceTypeEnum.MAIN);

        when(spaceRepository.selectById(55)).thenReturn(topEntity);
        when(spaceMapper.toBo(topEntity)).thenReturn(topBo);

        SpaceBo result = spaceService.getDetail(55);
        assertNotNull(result);
        assertEquals(Collections.emptyList(), result.getParentsIds());
        assertEquals(Collections.emptyList(), result.getParentsNames());
        // 不会查询祖先列表
        verify(spaceRepository, never()).selectByQo(any());
    }
}
