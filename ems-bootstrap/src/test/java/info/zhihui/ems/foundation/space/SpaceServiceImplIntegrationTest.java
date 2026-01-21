package info.zhihui.ems.foundation.space;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import info.zhihui.ems.foundation.space.bo.SpaceBo;
import info.zhihui.ems.foundation.space.dto.SpaceCreateDto;
import info.zhihui.ems.foundation.space.dto.SpaceQueryDto;
import info.zhihui.ems.foundation.space.dto.SpaceUpdateDto;
import info.zhihui.ems.foundation.space.entity.SpaceEntity;
import info.zhihui.ems.foundation.space.enums.SpaceTypeEnum;
import info.zhihui.ems.foundation.space.repository.SpaceRepository;
import info.zhihui.ems.foundation.space.service.SpaceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SpaceServiceImpl集成测试类
 * 测试空间服务的所有CRUD操作，包括数据库交互
 */
@SpringBootTest
@ActiveProfiles("integrationtest")
@Transactional
@Rollback
public class SpaceServiceImplIntegrationTest {

    @Autowired
    private SpaceService spaceService;

    @Autowired
    private SpaceRepository spaceRepository;

    private SpaceEntity parentSpace;
    private SpaceEntity childSpace;

    /**
     * 测试前准备数据
     */
    @BeforeEach
    void setUp() {
        // 清理测试数据
        spaceRepository.delete(new QueryWrapper<>());

        // 创建父空间测试数据
        parentSpace = new SpaceEntity();
        parentSpace.setId(1000);
        parentSpace.setName("测试主空间");
        parentSpace.setPid(0);
        parentSpace.setType(SpaceTypeEnum.MAIN.getCode());
        parentSpace.setArea(new BigDecimal("1000.00"));
        parentSpace.setSortIndex(1);
        parentSpace.setCreateTime(LocalDateTime.now());
        parentSpace.setUpdateTime(LocalDateTime.now());
        parentSpace.setFullPath("1000");
        spaceRepository.insert(parentSpace);

        // 创建子空间测试数据
        childSpace = new SpaceEntity();
        childSpace.setId(1010);
        childSpace.setName("测试子空间");
        childSpace.setPid(parentSpace.getId());
        childSpace.setType(SpaceTypeEnum.INNER_SPACE.getCode());
        childSpace.setArea(new BigDecimal("500.00"));
        childSpace.setSortIndex(1);
        childSpace.setCreateTime(LocalDateTime.now());
        childSpace.setUpdateTime(LocalDateTime.now());
        childSpace.setFullPath("1000,1010");
        spaceRepository.insert(childSpace);
    }

    /**
     * 测试getDetail方法 - 成功获取空间详情
     */
    @Test
    void testGetDetail_Success() {
        // 执行测试
        SpaceBo result = spaceService.getDetail(parentSpace.getId());

        // 验证结果
        assertNotNull(result);
        assertEquals(parentSpace.getId(), result.getId());
        assertEquals("测试主空间", result.getName());
        assertEquals(SpaceTypeEnum.MAIN, result.getType());
        assertEquals(new BigDecimal("1000.00"), result.getArea());
        assertEquals(1, result.getSortIndex());
        assertEquals("1000", result.getFullPath());
    }

    /**
     * 测试getDetail方法 - 空间不存在
     */
    @Test
    void testGetDetail_NotFound() {
        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            spaceService.getDetail(999);
        });
    }

    /**
     * 测试getDetail方法 - 获取已删除的空间
     */
    @Test
    void testGetDetail_DeletedSpace() {
        // 准备数据：标记空间为已删除
        spaceRepository.deleteById(parentSpace.getId());

        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            spaceService.getDetail(parentSpace.getId());
        });
    }

    /**
     * 测试findSpaceList方法 - 正常查询
     */
    @Test
    void testFindSpaceList_Success() {
        // 准备查询条件
        SpaceQueryDto queryDto = new SpaceQueryDto();
        queryDto.setPid(parentSpace.getId());

        // 执行测试
        List<SpaceBo> result = spaceService.findSpaceList(queryDto);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(childSpace.getId(), result.get(0).getId());
        assertEquals("测试子空间", result.get(0).getName());
    }

    /**
     * 测试findSpaceList方法 - 按名称查询
     */
    @Test
    void testFindSpaceList_ByName() {
        // 准备查询条件
        SpaceQueryDto queryDto = new SpaceQueryDto();
        queryDto.setName("测试主空间");

        // 执行测试
        List<SpaceBo> result = spaceService.findSpaceList(queryDto);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(parentSpace.getId(), result.get(0).getId());
        assertEquals("测试主空间", result.get(0).getName());
    }

    /**
     * 测试findSpaceList方法 - 空结果
     */
    @Test
    void testFindSpaceList_EmptyResult() {
        // 准备查询条件
        SpaceQueryDto queryDto = new SpaceQueryDto();
        queryDto.setPid(999);

        // 执行测试
        List<SpaceBo> result = spaceService.findSpaceList(queryDto);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * 测试addSpace方法 - 成功创建顶级空间
     */
    @Test
    void testAddSpace_CreateTopLevelSpace_Success() {
        // 准备创建数据
        SpaceCreateDto createDto = new SpaceCreateDto();
        createDto.setName("新顶级空间");
        createDto.setPid(0);
        createDto.setType(SpaceTypeEnum.MAIN);
        createDto.setArea(new BigDecimal("2000.00"));
        createDto.setSortIndex(2);

        // 执行测试
        Integer spaceId = spaceService.addSpace(createDto);

        // 验证结果
        assertNotNull(spaceId);

        // 验证数据库中的数据
        SpaceEntity savedEntity = spaceRepository.selectById(spaceId);
        assertNotNull(savedEntity);
        assertEquals("新顶级空间", savedEntity.getName());
        assertEquals(0, savedEntity.getPid());
        assertEquals(SpaceTypeEnum.MAIN.getCode(), savedEntity.getType());
        assertEquals(new BigDecimal("2000.00"), savedEntity.getArea());
        assertEquals(savedEntity.getId(), savedEntity.getOwnAreaId());
        assertEquals(2, savedEntity.getSortIndex());
        assertFalse(savedEntity.getIsDeleted());
    }

    /**
     * 测试addSpace方法 - 成功创建子空间
     */
    @Test
    void testAddSpace_CreateChildSpace_Success() {
        // 准备创建数据
        SpaceCreateDto createDto = new SpaceCreateDto();
        createDto.setName("新子空间");
        createDto.setPid(parentSpace.getId());
        createDto.setType(SpaceTypeEnum.ROOM);
        createDto.setArea(new BigDecimal("300.00"));
        createDto.setSortIndex(2);

        // 执行测试
        Integer spaceId = spaceService.addSpace(createDto);

        // 验证结果
        assertNotNull(spaceId);

        // 验证数据库中的数据
        SpaceEntity savedEntity = spaceRepository.selectById(spaceId);
        assertNotNull(savedEntity);
        assertEquals("新子空间", savedEntity.getName());
        assertEquals(parentSpace.getId(), savedEntity.getPid());
        assertEquals(SpaceTypeEnum.ROOM.getCode(), savedEntity.getType());
        assertEquals(new BigDecimal("300.00"), savedEntity.getArea());
        assertEquals(1000, savedEntity.getOwnAreaId());
        assertEquals(2, savedEntity.getSortIndex());
        assertEquals("1000,1011", savedEntity.getFullPath());
        assertFalse(savedEntity.getIsDeleted());
    }

    /**
     * 测试addSpace方法 - 父空间不存在
     */
    @Test
    void testAddSpace_ParentNotFound() {
        // 准备创建数据
        SpaceCreateDto createDto = new SpaceCreateDto();
        createDto.setName("新子空间");
        createDto.setPid(999);
        createDto.setType(SpaceTypeEnum.ROOM);
        createDto.setArea(new BigDecimal("300.00"));
        createDto.setSortIndex(1);

        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            spaceService.addSpace(createDto);
        });
    }

    /**
     * 测试addSpace方法 - 同级空间名称重复
     */
    @Test
    void testAddSpace_DuplicateName() {
        // 准备创建数据（与已存在的子空间同名）
        SpaceCreateDto createDto = new SpaceCreateDto();
        createDto.setName("测试子空间");
        createDto.setPid(parentSpace.getId());
        createDto.setType(SpaceTypeEnum.ROOM);
        createDto.setArea(new BigDecimal("300.00"));
        createDto.setSortIndex(2);

        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            spaceService.addSpace(createDto);
        });
    }

    /**
     * 测试updateSpace方法 - 成功更新空间
     */
    @Test
    void testUpdateSpace_Success() {
        // 准备更新数据
        SpaceUpdateDto updateDto = new SpaceUpdateDto();
        updateDto.setId(parentSpace.getId());
        updateDto.setName("更新后的主空间");
        updateDto.setType(SpaceTypeEnum.MAIN);
        updateDto.setPid(0);
        updateDto.setArea(new BigDecimal("1500.00"));
        updateDto.setSortIndex(3);

        // 执行测试
        spaceService.updateSpace(updateDto);

        // 验证数据库中的数据
        SpaceEntity updatedEntity = spaceRepository.selectById(parentSpace.getId());
        assertNotNull(updatedEntity);
        assertEquals("更新后的主空间", updatedEntity.getName());
        assertEquals(new BigDecimal("1500.00"), updatedEntity.getArea());
        assertEquals(1000, updatedEntity.getOwnAreaId());
        assertEquals(3, updatedEntity.getSortIndex());
        assertEquals("1000", updatedEntity.getFullPath());
    }

    /**
     * 测试updateSpace方法 - 空间不存在
     */
    @Test
    void testUpdateSpace_NotFound() {
        // 准备更新数据
        SpaceUpdateDto updateDto = new SpaceUpdateDto();
        updateDto.setId(999);
        updateDto.setName("不存在的空间");
        updateDto.setType(SpaceTypeEnum.MAIN);
        updateDto.setArea(new BigDecimal("1000.00"));
        updateDto.setSortIndex(1);

        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            spaceService.updateSpace(updateDto);
        });
    }

    /**
     * 测试updateSpace方法 - 更新父空间导致循环引用
     */
    @Test
    void testUpdateSpace_CircularReference() {
        // 准备更新数据：将父空间的父ID设置为子空间ID
        SpaceUpdateDto updateDto = new SpaceUpdateDto();
        updateDto.setId(parentSpace.getId());
        updateDto.setName("测试主空间");
        updateDto.setPid(childSpace.getId());
        updateDto.setType(SpaceTypeEnum.MAIN);
        updateDto.setArea(new BigDecimal("1000.00"));
        updateDto.setSortIndex(1);

        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            spaceService.updateSpace(updateDto);
        });
    }

    /**
     * 测试updateSpace方法 - 同级空间名称重复
     */
    @Test
    void testUpdateSpace_DuplicateName() {
        // 创建另一个同级空间
        SpaceEntity anotherChild = new SpaceEntity();
        anotherChild.setName("另一个子空间");
        anotherChild.setPid(parentSpace.getId());
        anotherChild.setType(SpaceTypeEnum.ROOM.getCode());
        anotherChild.setArea(new BigDecimal("400.00"));
        anotherChild.setOwnAreaId(1);
        anotherChild.setSortIndex(2);
        anotherChild.setFullPath("/测试主空间/另一个子空间");
        anotherChild.setIsDeleted(false);
        anotherChild.setCreateTime(LocalDateTime.now());
        anotherChild.setUpdateTime(LocalDateTime.now());
        spaceRepository.insert(anotherChild);

        // 准备更新数据：将另一个子空间的名称改为与已存在的子空间同名
        SpaceUpdateDto updateDto = new SpaceUpdateDto();
        updateDto.setId(anotherChild.getId());
        updateDto.setName("测试子空间");
        updateDto.setPid(parentSpace.getId());
        updateDto.setType(SpaceTypeEnum.ROOM);
        updateDto.setArea(new BigDecimal("400.00"));
        updateDto.setSortIndex(2);

        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            spaceService.updateSpace(updateDto);
        });
    }

    /**
     * 测试deleteSpace方法 - 成功删除空间
     */
    @Test
    void testDeleteSpace_Success() {
        // 执行测试
        spaceService.deleteSpace(childSpace.getId());

        // 验证结果：空间应该被标记为已删除
        SpaceEntity deletedEntity = spaceRepository.selectById(childSpace.getId());
        assertNull(deletedEntity);
    }

    /**
     * 测试deleteSpace方法 - 空间不存在
     */
    @Test
    void testDeleteSpace_NotFound() {
        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            spaceService.deleteSpace(999);
        });
    }

    /**
     * 测试deleteSpace方法 - 删除已删除的空间
     */
    @Test
    void testDeleteSpace_AlreadyDeleted() {
        // 准备数据：先删除空间
        spaceRepository.deleteById(childSpace.getId());

        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            spaceService.deleteSpace(childSpace.getId());
        });
    }

    /**
     * 测试deleteSpace方法 - 删除存在子空间的空间
     */
    @Test
    void testDeleteSpace_HasChildren() {
        // 执行测试并验证异常：尝试删除有子空间的父空间
        assertThrows(RuntimeException.class, () -> {
            spaceService.deleteSpace(parentSpace.getId());
        });
    }
}