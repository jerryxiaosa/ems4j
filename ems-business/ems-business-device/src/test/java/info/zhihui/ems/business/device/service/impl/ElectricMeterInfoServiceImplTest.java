package info.zhihui.ems.business.device.service.impl;

import com.github.pagehelper.PageInfo;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.business.device.bo.ElectricMeterBo;
import info.zhihui.ems.business.device.dto.CanceledMeterDto;
import info.zhihui.ems.business.device.dto.ElectricMeterQueryDto;
import info.zhihui.ems.business.device.entity.ElectricMeterEntity;
import info.zhihui.ems.business.device.entity.MeterCancelRecordEntity;
import info.zhihui.ems.business.device.mapper.ElectricMeterMapper;
import info.zhihui.ems.business.device.qo.ElectricMeterQo;
import info.zhihui.ems.business.device.repository.ElectricMeterRepository;
import info.zhihui.ems.business.device.repository.MeterCancelRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ElectricMeterInfoServiceImpl单元测试类
 *
 * @author jerryxiaosa
 */
@ExtendWith(MockitoExtension.class)
class ElectricMeterInfoServiceImplTest {

    @Mock
    private ElectricMeterRepository repository;

    @Mock
    private ElectricMeterMapper mapper;

    @Mock
    private MeterCancelRecordRepository meterCancelRecordRepository;

    @InjectMocks
    private ElectricMeterInfoServiceImpl electricMeterInfoService;

    private ElectricMeterQueryDto queryDto;
    private ElectricMeterEntity entity;
    private ElectricMeterBo bo;
    private ElectricMeterQo qo;
    private PageParam pageParam;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        queryDto = new ElectricMeterQueryDto()
                .setMeterName("测试电表")
                .setIsOnline(true);

        // 设置基础实体数据
        entity = new ElectricMeterEntity();
        entity.setId(1)
                .setMeterName("测试电表")
                .setSpaceId(100)
                .setIotId("12345")
                .setModelId(1)
                .setIsOnline(true)
                .setIsPrepay(false)
                .setAccountId(null)
                .setOwnAreaId(1000)
                .setCreateTime(LocalDateTime.now())
                .setUpdateTime(LocalDateTime.now())
        ;

        bo = new ElectricMeterBo()
                .setId(1)
                .setSpaceId(100)
                .setMeterName("测试电表")
                .setMeterNo("EM202401010001")
                .setModelId(200)
                .setGatewayId(300)
                .setPortNo(1)
                .setMeterAddress(1)
                .setIsCalculate(true)
                .setIsPrepay(false)
                .setCt(1)
                .setOwnAreaId(1000)
                .setIotId("12345");

        qo = new ElectricMeterQo()
                .setMeterName("测试电表")
                .setIsOnline(true);

        pageParam = new PageParam();
    }

    @Test
    void testFindPage() {
        // 准备数据
        PageResult<ElectricMeterBo> expectedResult = new PageResult<>();
        expectedResult.setList(Collections.singletonList(bo));
        expectedResult.setTotal(1L);

        // Mock行为
        when(mapper.queryDtoToQo(queryDto)).thenReturn(qo);
        when(repository.findList(qo)).thenReturn(Collections.singletonList(entity));
        when(mapper.pageEntityToBo(any(PageInfo.class))).thenReturn(expectedResult);

        // 执行测试
        PageResult<ElectricMeterBo> result = electricMeterInfoService.findPage(queryDto, pageParam);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getList().size());
        assertEquals(1L, result.getTotal());
        verify(mapper).queryDtoToQo(queryDto);
        verify(mapper).pageEntityToBo(any(PageInfo.class));
    }

    @Test
    void testFindList() {
        // 准备数据
        List<ElectricMeterEntity> entityList = Collections.singletonList(entity);
        List<ElectricMeterBo> boList = Collections.singletonList(bo);

        // Mock行为
        when(mapper.queryDtoToQo(queryDto)).thenReturn(qo);
        when(repository.findList(qo)).thenReturn(entityList);
        when(mapper.listEntityToBo(entityList)).thenReturn(boList);

        // 执行测试
        List<ElectricMeterBo> result = electricMeterInfoService.findList(queryDto);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(bo.getId(), result.get(0).getId());
        verify(mapper).queryDtoToQo(queryDto);
        verify(repository).findList(qo);
        verify(mapper).listEntityToBo(entityList);
    }

    @Test
    void testGetDetail_Success() {
        // Mock行为
        when(repository.selectById(1)).thenReturn(entity);
        when(mapper.entityToBo(entity)).thenReturn(bo);

        // 执行测试
        ElectricMeterBo result = electricMeterInfoService.getDetail(1);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getId());
        verify(repository).selectById(1);
        verify(mapper).entityToBo(entity);
    }

    @Test
    void testGetDetail_NotFound() {
        // Mock行为
        when(repository.selectById(1)).thenReturn(null);

        // 执行测试并验证异常
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> electricMeterInfoService.getDetail(1));

        assertEquals("电表数据不存在或已被删除", exception.getMessage());
        verify(repository).selectById(1);
    }

    @Test
    void testGetByDeviceNo_Success() {
        // 准备数据
        String deviceNo = "DEV-001";
        entity.setDeviceNo(deviceNo);
        bo.setDeviceNo(deviceNo);
        List<ElectricMeterEntity> entityList = Collections.singletonList(entity);

        // Mock行为
        ElectricMeterQo queryQo = new ElectricMeterQo().setDeviceNo(deviceNo);
        when(repository.findList(queryQo)).thenReturn(entityList);
        when(mapper.entityToBo(entity)).thenReturn(bo);

        // 执行测试
        ElectricMeterBo result = electricMeterInfoService.getByDeviceNo(deviceNo);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(deviceNo, result.getDeviceNo());
        verify(repository).findList(queryQo);
        verify(mapper).entityToBo(entity);
    }

    @Test
    void testGetByDeviceNo_NotFound() {
        // 准备数据
        String deviceNo = "DEV-404";
        List<ElectricMeterEntity> emptyList = Collections.emptyList();

        // Mock行为
        ElectricMeterQo queryQo = new ElectricMeterQo().setDeviceNo(deviceNo);
        when(repository.findList(queryQo)).thenReturn(emptyList);

        // 执行测试并验证异常
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> electricMeterInfoService.getByDeviceNo(deviceNo));

        assertEquals("能耗系统查询到deviceNo=DEV-404没有匹配的电表", exception.getMessage());
        verify(repository).findList(queryQo);
    }

    @Test
    void testGetByDeviceNo_MultipleFound() {
        // 准备数据
        String deviceNo = "DEV-001";
        ElectricMeterEntity entity2 = new ElectricMeterEntity();
        entity2.setId(2)
                .setMeterName("测试电表2")
                .setDeviceNo(deviceNo);
        List<ElectricMeterEntity> multipleEntityList = List.of(entity, entity2);

        // Mock行为
        ElectricMeterQo queryQo = new ElectricMeterQo().setDeviceNo(deviceNo);
        when(repository.findList(queryQo)).thenReturn(multipleEntityList);

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> electricMeterInfoService.getByDeviceNo(deviceNo));

        assertEquals("能耗系统查询到deviceNo=DEV-001的电表数量=2无法匹配电表", exception.getMessage());
        verify(repository).findList(queryQo);
    }

    @Test
    void testFindMetersByCancelNo_Success() {
        // 准备测试数据
        String cancelNo = "CANCEL001";
        
        MeterCancelRecordEntity entity1 = new MeterCancelRecordEntity()
                .setCancelNo(cancelNo)
                .setMeterNo("METER001")
                .setMeterName("测试电表1");
        
        MeterCancelRecordEntity entity2 = new MeterCancelRecordEntity()
                .setCancelNo(cancelNo)
                .setMeterNo("METER002")
                .setMeterName("测试电表2");
        
        List<MeterCancelRecordEntity> entities = List.of(entity1, entity2);
        
        CanceledMeterDto dto1 = new CanceledMeterDto()
                .setMeterNo("METER001")
                .setMeterName("测试电表1");
        
        CanceledMeterDto dto2 = new CanceledMeterDto()
                .setMeterNo("METER002")
                .setMeterName("测试电表2");
        
        List<CanceledMeterDto> expectedDtos = List.of(dto1, dto2);

        // Mock 行为
        when(meterCancelRecordRepository.selectByCancelNo(cancelNo)).thenReturn(entities);
        when(mapper.listMeterEntityToDto(entities)).thenReturn(expectedDtos);

        // 执行测试
        List<CanceledMeterDto> result = electricMeterInfoService.findMetersByCancelNo(cancelNo);

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("METER001", result.get(0).getMeterNo());
        assertEquals("测试电表1", result.get(0).getMeterName());
        assertEquals("METER002", result.get(1).getMeterNo());
        assertEquals("测试电表2", result.get(1).getMeterName());

        // 验证方法调用
        verify(meterCancelRecordRepository).selectByCancelNo(cancelNo);
        verify(mapper).listMeterEntityToDto(entities);
    }

    @Test
    void testFindMetersByCancelNo_EmptyResult() {
        // 准备测试数据
        String cancelNo = "NONEXISTENT";
        List<MeterCancelRecordEntity> emptyEntities = List.of();
        List<CanceledMeterDto> emptyDtos = List.of();

        // Mock 行为
        when(meterCancelRecordRepository.selectByCancelNo(cancelNo)).thenReturn(emptyEntities);
        when(mapper.listMeterEntityToDto(emptyEntities)).thenReturn(emptyDtos);

        // 执行测试
        List<CanceledMeterDto> result = electricMeterInfoService.findMetersByCancelNo(cancelNo);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // 验证方法调用
        verify(meterCancelRecordRepository).selectByCancelNo(cancelNo);
        verify(mapper).listMeterEntityToDto(emptyEntities);
    }

}
