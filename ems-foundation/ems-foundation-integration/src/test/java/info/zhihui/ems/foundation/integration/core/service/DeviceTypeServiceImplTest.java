package info.zhihui.ems.foundation.integration.core.service;

import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.foundation.integration.core.bo.DeviceTypeBo;
import info.zhihui.ems.foundation.integration.core.dto.DeviceTypeQueryDto;
import info.zhihui.ems.foundation.integration.core.dto.DeviceTypeSaveDto;
import info.zhihui.ems.foundation.integration.core.entity.DeviceTypeEntity;
import info.zhihui.ems.foundation.integration.core.mapper.DeviceTypeMapper;
import info.zhihui.ems.foundation.integration.core.qo.DeviceTypeQueryQo;
import info.zhihui.ems.foundation.integration.core.repository.DeviceTypeRepository;
import info.zhihui.ems.foundation.integration.core.service.impl.DeviceTypeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * DeviceTypeServiceImpl单元测试
 *
 * @author jerryxiaosa
 */
@ExtendWith(MockitoExtension.class)
class DeviceTypeServiceImplTest {

    @InjectMocks
    private DeviceTypeServiceImpl deviceTypeService;

    @Mock
    private DeviceTypeMapper mapper;

    @Mock
    private DeviceTypeRepository repository;

    private DeviceTypeQueryDto queryDto;
    private DeviceTypeQueryQo queryQo;
    private DeviceTypeSaveDto saveDto;
    private DeviceTypeEntity entity;
    private DeviceTypeBo bo;
    private List<DeviceTypeEntity> entityList;
    private List<DeviceTypeBo> boList;

    @BeforeEach
    void setUp() {
        // 初始化查询DTO
        queryDto = new DeviceTypeQueryDto()
                .setPid(1)
                .setTypeKey("electricMeter");

        // 初始化查询QO
        queryQo = new DeviceTypeQueryQo()
                .setPid(1)
                .setTypeKey("electricMeter");

        // 初始化保存DTO
        saveDto = new DeviceTypeSaveDto();
        saveDto.setId(1);
        saveDto.setPid(1);
        saveDto.setTypeName("电表");
        saveDto.setTypeKey("electricMeter");

        // 初始化实体
        entity = new DeviceTypeEntity()
                .setId(1)
                .setPid(1)
                .setAncestorId("0")
                .setTypeName("电表")
                .setTypeKey("electricMeter")
                .setLevel(2);

        // 初始化BO
        bo = new DeviceTypeBo()
                .setId(1)
                .setPid(1)
                .setAncestorId("0")
                .setTypeName("电表")
                .setTypeKey("electricMeter")
                .setLevel(2);

        // 初始化列表
        entityList = Arrays.asList(entity);
        boList = Arrays.asList(bo);
    }

    @Test
    void findList_shouldReturnBoList() {
        // Given
        when(mapper.queryDtoToQo(queryDto)).thenReturn(queryQo);
        when(repository.findList(queryQo)).thenReturn(entityList);
        when(mapper.listEntityToBo(entityList)).thenReturn(boList);

        // When
        List<DeviceTypeBo> result = deviceTypeService.findList(queryDto);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("电表", result.get(0).getTypeName());
        assertEquals("electricMeter", result.get(0).getTypeKey());

        verify(mapper).queryDtoToQo(queryDto);
        verify(repository).findList(queryQo);
        verify(mapper).listEntityToBo(entityList);
    }

    @Test
    void getDetail_shouldReturnBo() {
        // Given
        when(repository.selectById(1)).thenReturn(entity);
        when(mapper.entityToBo(entity)).thenReturn(bo);

        // When
        DeviceTypeBo result = deviceTypeService.getDetail(1);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("电表", result.getTypeName());
        assertEquals("electricMeter", result.getTypeKey());

        verify(repository).selectById(1);
        verify(mapper).entityToBo(entity);
    }

    @Test
    void getByKey_whenFound_shouldReturnBo() {
        // Given
        DeviceTypeQueryDto expectedQuery = new DeviceTypeQueryDto().setTypeKey("electricMeter");
        when(mapper.queryDtoToQo(any(DeviceTypeQueryDto.class))).thenReturn(queryQo);
        when(repository.findList(queryQo)).thenReturn(entityList);
        when(mapper.listEntityToBo(entityList)).thenReturn(boList);

        // When
        DeviceTypeBo result = deviceTypeService.getByKey("electricMeter");

        // Then
        assertNotNull(result);
        assertEquals("electricMeter", result.getTypeKey());

        verify(mapper).queryDtoToQo(any(DeviceTypeQueryDto.class));
        verify(repository).findList(queryQo);
        verify(mapper).listEntityToBo(entityList);
    }

    @Test
    void getByKey_whenNotFound_shouldThrowException() {
        // Given
        when(mapper.queryDtoToQo(any(DeviceTypeQueryDto.class))).thenReturn(queryQo);
        when(repository.findList(queryQo)).thenReturn(Collections.emptyList());
        when(mapper.listEntityToBo(Collections.emptyList())).thenReturn(Collections.emptyList());

        // When & Then
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class, () -> {
            deviceTypeService.getByKey("nonExistentKey");
        });

        assertEquals("该设备类型key不存在：nonExistentKey", exception.getMessage());
    }

    @Test
    void getByKey_whenMultipleFound_shouldThrowException() {
        // Given
        List<DeviceTypeBo> multipleBos = Arrays.asList(bo, bo);
        when(mapper.queryDtoToQo(any(DeviceTypeQueryDto.class))).thenReturn(queryQo);
        when(repository.findList(queryQo)).thenReturn(entityList);
        when(mapper.listEntityToBo(entityList)).thenReturn(multipleBos);

        // When & Then
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class, () -> {
            deviceTypeService.getByKey("duplicateKey");
        });

        assertEquals("该设备类型key不存在：duplicateKey", exception.getMessage());
    }

    @Test
    void add_withTopLevel_shouldSetCorrectHierarchy() {
        // Given
        DeviceTypeSaveDto topLevelDto = new DeviceTypeSaveDto();
        topLevelDto.setPid(null);
        topLevelDto.setTypeName("顶级类型");
        topLevelDto.setTypeKey("topLevel");

        DeviceTypeEntity expectedEntity = new DeviceTypeEntity()
                .setPid(0)
                .setAncestorId("0")
                .setLevel(1);

        when(mapper.saveDtoEntity(topLevelDto)).thenReturn(expectedEntity);

        // When
        deviceTypeService.add(topLevelDto);

        // Then
        verify(mapper).saveDtoEntity(topLevelDto);
        verify(repository).insert(expectedEntity);
        assertEquals(Integer.valueOf(0), expectedEntity.getPid());
        assertEquals("0", expectedEntity.getAncestorId());
        assertEquals(Integer.valueOf(1), expectedEntity.getLevel());
    }

    @Test
    void add_withParent_shouldSetCorrectHierarchy() {
        // Given
        DeviceTypeEntity parentEntity = new DeviceTypeEntity()
                .setId(1)
                .setAncestorId("0")
                .setLevel(1);

        DeviceTypeSaveDto childDto = new DeviceTypeSaveDto();
        childDto.setPid(1);
        childDto.setTypeName("子类型");
        childDto.setTypeKey("childType");

        DeviceTypeEntity childEntity = new DeviceTypeEntity();

        when(mapper.saveDtoEntity(childDto)).thenReturn(childEntity);
        when(repository.selectById(1)).thenReturn(parentEntity);

        // When
        deviceTypeService.add(childDto);

        // Then
        verify(repository).selectById(1);
        verify(mapper).saveDtoEntity(childDto);
        verify(repository).insert(childEntity);
        assertEquals("0,1", childEntity.getAncestorId());
        assertEquals(Integer.valueOf(2), childEntity.getLevel());
    }

    @Test
    void add_withNonExistentParent_shouldThrowException() {
        // Given
        DeviceTypeSaveDto childDto = new DeviceTypeSaveDto();
        childDto.setPid(999);
        childDto.setTypeName("子类型");
        childDto.setTypeKey("childType");

        DeviceTypeEntity childEntity = new DeviceTypeEntity();

        when(mapper.saveDtoEntity(childDto)).thenReturn(childEntity);
        when(repository.selectById(999)).thenReturn(null);

        // When & Then
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class, () -> {
            deviceTypeService.add(childDto);
        });

        assertEquals("pid不存在，请确认", exception.getMessage());
        verify(repository, never()).insert(any(DeviceTypeEntity.class));
    }

    @Test
    void update_whenEntityExists_shouldUpdateSuccessfully() {
        // Given
        DeviceTypeEntity existingEntity = new DeviceTypeEntity().setId(1);
        DeviceTypeEntity updatedEntity = new DeviceTypeEntity();

        when(repository.selectById(1)).thenReturn(existingEntity);
        when(mapper.saveDtoEntity(saveDto)).thenReturn(updatedEntity);

        // When
        deviceTypeService.update(saveDto);

        // Then
        verify(repository).selectById(1);
        verify(mapper).saveDtoEntity(saveDto);
        verify(repository).updateById(updatedEntity);
    }

    @Test
    void update_whenEntityNotExists_shouldThrowException() {
        // Given
        when(repository.selectById(1)).thenReturn(null);

        // When & Then
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class, () -> {
            deviceTypeService.update(saveDto);
        });

        assertEquals("数据不存在，请确认", exception.getMessage());
        verify(repository, never()).updateById(any(DeviceTypeEntity.class));
    }

    @Test
    void delete_withNoChildren_shouldDeleteSuccessfully() {
        // Given
        when(mapper.queryDtoToQo(any(DeviceTypeQueryDto.class))).thenReturn(queryQo);
        when(repository.findList(queryQo)).thenReturn(Collections.emptyList());
        when(mapper.listEntityToBo(Collections.emptyList())).thenReturn(Collections.emptyList());

        // When
        deviceTypeService.delete(1);

        // Then
        verify(repository).deleteById(1);
    }

    @Test
    void delete_withChildren_shouldThrowException() {
        // Given
        when(mapper.queryDtoToQo(any(DeviceTypeQueryDto.class))).thenReturn(queryQo);
        when(repository.findList(queryQo)).thenReturn(entityList);
        when(mapper.listEntityToBo(entityList)).thenReturn(boList);

        // When & Then
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class, () -> {
            deviceTypeService.delete(1);
        });

        assertEquals("该品类下有子级，无法删除", exception.getMessage());
        verify(repository, never()).deleteById(anyInt());
    }
}