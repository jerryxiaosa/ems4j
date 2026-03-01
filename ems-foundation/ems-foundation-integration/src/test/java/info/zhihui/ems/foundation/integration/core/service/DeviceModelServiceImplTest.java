package info.zhihui.ems.foundation.integration.core.service;

import com.github.pagehelper.PageInfo;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.foundation.integration.core.bo.DeviceModelBo;
import info.zhihui.ems.foundation.integration.core.dto.DeviceModelQueryDto;
import info.zhihui.ems.foundation.integration.core.entity.DeviceModelEntity;
import info.zhihui.ems.foundation.integration.core.mapper.DeviceModelMapper;
import info.zhihui.ems.foundation.integration.core.qo.DeviceModelQueryQo;
import info.zhihui.ems.foundation.integration.core.repository.DeviceModelRepository;
import info.zhihui.ems.foundation.integration.core.service.impl.DeviceModelServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * DeviceModelServiceImpl单元测试
 *
 * @author jerryxiaosa
 */
@ExtendWith(MockitoExtension.class)
class DeviceModelServiceImplTest {

    @InjectMocks
    private DeviceModelServiceImpl deviceModelService;

    @Mock
    private DeviceModelMapper mapper;

    @Mock
    private DeviceModelRepository repository;

    private DeviceModelQueryDto queryDto;
    private DeviceModelQueryQo queryQo;
    private DeviceModelEntity entity;
    private DeviceModelBo bo;
    private List<DeviceModelEntity> entityList;
    private List<DeviceModelBo> boList;
    private PageParam pageParam;
    private PageResult<DeviceModelBo> pageResult;

    @BeforeEach
    void setUp() {
        // 初始化查询DTO
        queryDto = new DeviceModelQueryDto()
                .setTypeKey("electricMeter")
                .setManufacturerName("华为")
                .setModelName("HW-001")
                .setProductCode("HW-001-KEY");

        // 初始化查询QO
        queryQo = new DeviceModelQueryQo()
                .setTypeKey("electricMeter")
                .setManufacturerName("华为")
                .setModelName("HW-001")
                .setProductCode("HW-001-KEY");

        // 初始化实体
        entity = new DeviceModelEntity();
        entity.setId(1);
        entity.setTypeId(1);
        entity.setTypeKey("electricMeter");
        entity.setManufacturerName("华为");
        entity.setModelName("HW-001");
        entity.setProductCode("HW-001-KEY");
        entity.setModelProperty("{\"voltage\":\"220V\",\"power\":\"100W\"}");

        // 初始化BO
        Map<String, Object> modelProperty = new HashMap<>();
        modelProperty.put("voltage", "220V");
        modelProperty.put("power", "100W");

        bo = new DeviceModelBo()
                .setId(1)
                .setTypeId(1)
                .setTypeKey("electricMeter")
                .setManufacturerName("华为")
                .setModelName("HW-001")
                .setProductCode("HW-001-KEY")
                .setModelProperty(modelProperty);

        // 初始化列表 - 使用List.of替换Arrays.asList
        entityList = List.of(entity);
        boList = List.of(bo);

        // 初始化分页参数
        pageParam = new PageParam();
        pageParam.setPageNum(1);
        pageParam.setPageSize(10);

        // 初始化分页信息
        PageInfo<DeviceModelEntity> pageInfo = new PageInfo<>(entityList);
        pageInfo.setTotal(1);
        pageInfo.setPages(1);
        pageInfo.setPageNum(1);
        pageInfo.setPageSize(10);

        // 初始化分页结果
        pageResult = new PageResult<>();
        pageResult.setList(boList);
        pageResult.setTotal(1L);
        pageResult.setPageNum(1);
        pageResult.setPageSize(10);
    }

    @Test
    void findPage_shouldReturnPageResult() {
        // Given
        when(mapper.queryDtoToQo(queryDto)).thenReturn(queryQo);
        when(repository.findList(queryQo)).thenReturn(entityList);
        when(mapper.pageEntityToBo(org.mockito.ArgumentMatchers.any())).thenReturn(pageResult);

        // When
        PageResult<DeviceModelBo> result = deviceModelService.findPage(queryDto, pageParam);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getTotal());
        assertEquals(1, result.getList().size());
        assertEquals("华为", result.getList().get(0).getManufacturerName());
        assertEquals("HW-001", result.getList().get(0).getModelName());

        verify(mapper).queryDtoToQo(queryDto);
        verify(repository).findList(queryQo);
        verify(mapper).pageEntityToBo(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void findPage_withNullQuery_shouldHandleGracefully() {
        // Given
        when(mapper.queryDtoToQo(null)).thenReturn(new DeviceModelQueryQo());
        when(repository.findList(any(DeviceModelQueryQo.class))).thenReturn(entityList);
        when(mapper.pageEntityToBo(org.mockito.ArgumentMatchers.any())).thenReturn(pageResult);

        // When
        PageResult<DeviceModelBo> result = deviceModelService.findPage(null, pageParam);

        // Then
        assertNotNull(result);
        verify(mapper).queryDtoToQo(null);
    }

    @Test
    void findList_shouldReturnBoList() {
        // Given
        when(mapper.queryDtoToQo(queryDto)).thenReturn(queryQo);
        when(repository.findList(queryQo)).thenReturn(entityList);
        when(mapper.listEntityToBo(entityList)).thenReturn(boList);

        // When
        List<DeviceModelBo> result = deviceModelService.findList(queryDto);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("华为", result.get(0).getManufacturerName());
        assertEquals("HW-001", result.get(0).getModelName());
        assertEquals("electricMeter", result.get(0).getTypeKey());
        assertEquals("HW-001-KEY", result.get(0).getProductCode());

        verify(mapper).queryDtoToQo(queryDto);
        verify(repository).findList(queryQo);
        verify(mapper).listEntityToBo(entityList);
    }

    @Test
    void findList_withIds_shouldReturnBoList() {
        // Given
        DeviceModelQueryDto queryWithIdsDto = new DeviceModelQueryDto().setIds(List.of(1, 2));
        DeviceModelQueryQo queryWithIdsQo = new DeviceModelQueryQo().setIds(List.of(1, 2));
        when(mapper.queryDtoToQo(queryWithIdsDto)).thenReturn(queryWithIdsQo);
        when(repository.findList(queryWithIdsQo)).thenReturn(entityList);
        when(mapper.listEntityToBo(entityList)).thenReturn(boList);

        // When
        List<DeviceModelBo> result = deviceModelService.findList(queryWithIdsDto);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getId());

        verify(mapper).queryDtoToQo(queryWithIdsDto);
        verify(repository).findList(queryWithIdsQo);
        verify(mapper).listEntityToBo(entityList);
    }

    @Test
    void findList_withEmptyResult_shouldReturnEmptyList() {
        // Given
        when(mapper.queryDtoToQo(queryDto)).thenReturn(queryQo);
        when(repository.findList(queryQo)).thenReturn(List.of());
        when(mapper.listEntityToBo(List.of())).thenReturn(List.of());

        // When
        List<DeviceModelBo> result = deviceModelService.findList(queryDto);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(mapper).queryDtoToQo(queryDto);
        verify(repository).findList(queryQo);
        verify(mapper).listEntityToBo(List.of());
    }

    @Test
    void getDetail_shouldReturnBo() {
        // Given
        when(repository.selectById(1)).thenReturn(entity);
        when(mapper.entityToBo(entity)).thenReturn(bo);

        // When
        DeviceModelBo result = deviceModelService.getDetail(1);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("华为", result.getManufacturerName());
        assertEquals("HW-001", result.getModelName());
        assertEquals("electricMeter", result.getTypeKey());
        assertEquals("HW-001-KEY", result.getProductCode());
        assertNotNull(result.getModelProperty());
        assertEquals("220V", result.getModelProperty().get("voltage"));
        assertEquals("100W", result.getModelProperty().get("power"));

        verify(repository).selectById(1);
        verify(mapper).entityToBo(entity);
    }

    @Test
    void getDetail_whenEntityNotFound_shouldReturnNull() {
        // Given
        when(repository.selectById(999)).thenReturn(null);

        // When
        assertThrows(NotFoundException.class,  () -> deviceModelService.getDetail(999));

        verify(repository).selectById(999);
        verify(mapper, never()).entityToBo(any());
    }
}
