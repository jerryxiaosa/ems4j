package info.zhihui.ems.business.plan.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import info.zhihui.ems.common.enums.ElectricPricePeriodEnum;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.common.utils.JacksonUtil;
import info.zhihui.ems.business.plan.bo.ElectricPricePlanBo;
import info.zhihui.ems.business.plan.bo.ElectricPricePlanDetailBo;
import info.zhihui.ems.business.plan.bo.StepPriceBo;
import info.zhihui.ems.business.plan.dto.ElectricPricePlanQueryDto;
import info.zhihui.ems.business.plan.dto.ElectricPricePlanSaveDto;
import info.zhihui.ems.business.plan.dto.ElectricPriceTimeDto;
import info.zhihui.ems.business.plan.dto.ElectricPriceTypeDto;
import info.zhihui.ems.business.plan.entity.ElectricPricePlanEntity;
import info.zhihui.ems.business.plan.mapper.ElectricPlanMapper;
import info.zhihui.ems.business.plan.qo.ElectricPricePlanQo;
import info.zhihui.ems.business.plan.repository.ElectricPricePlanRepository;
import info.zhihui.ems.foundation.system.bo.ConfigBo;
import info.zhihui.ems.foundation.system.constant.SystemConfigConstant;
import info.zhihui.ems.foundation.system.dto.ConfigUpdateDto;
import info.zhihui.ems.foundation.system.service.ConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * ElectricPlanServiceImpl单元测试
 *
 * @author jerryxiaosa
 */
@ExtendWith(MockitoExtension.class)
class ElectricPlanServiceImplTest {

    @Mock
    private ElectricPricePlanRepository repository;

    @Mock
    private ElectricPlanMapper mapper;

    @Mock
    private ConfigService configService;

    @InjectMocks
    private ElectricPricePlanServiceImpl service;

    @BeforeEach
    void setUpDefaultBaseConfig() {
        ConfigBo baseConfig = new ConfigBo();
        baseConfig.setConfigValue(JacksonUtil.toJson(createValidElectricTypes()));
        lenient().when(configService.getByKey(SystemConfigConstant.ELECTRIC_PRICE_TYPE_KEY)).thenReturn(baseConfig);
    }

    @Test
    void testFindList() {
        // 准备测试数据
        ElectricPricePlanQueryDto query = new ElectricPricePlanQueryDto();
        ElectricPricePlanQo qo = new ElectricPricePlanQo();
        List<ElectricPricePlanEntity> entityList = List.of(new ElectricPricePlanEntity());
        List<ElectricPricePlanBo> boList = List.of(new ElectricPricePlanBo());

        // Mock行为
        when(mapper.queryDtoToQo(query)).thenReturn(qo);
        when(repository.findList(qo)).thenReturn(entityList);
        when(mapper.listEntityToBo(entityList)).thenReturn(boList);

        // 执行测试
        List<ElectricPricePlanBo> result = service.findList(query);

        // 验证结果
        assertEquals(boList, result);
        verify(mapper).queryDtoToQo(query);
        verify(repository).findList(qo);
        verify(mapper).listEntityToBo(entityList);
    }

    @Test
    void testGetDetail_Success() {
        // 准备测试数据
        Integer id = 1;
        ElectricPricePlanEntity entity = new ElectricPricePlanEntity();
        entity.setId(id);
        entity.setIsCustomPrice(false);
        entity.setIsStep(false);

        ElectricPricePlanDetailBo detailBo = new ElectricPricePlanDetailBo();
        detailBo.setIsCustomPrice(false);
        detailBo.setIsStep(false);

        // Mock行为
        when(repository.selectById(id)).thenReturn(entity);
        when(mapper.detailEntityToBo(entity)).thenReturn(detailBo);

        // 执行测试
        ElectricPricePlanDetailBo result = service.getDetail(id);

        // 验证结果
        assertNotNull(result);
        assertEquals(detailBo, result);
        assertNotNull(result.getStepPrices());
        assertTrue(result.getStepPrices().isEmpty());
    }

    @Test
    void testGetDetail_WithCustomPrice() {
        // 准备测试数据
        Integer id = 1;
        ElectricPricePlanEntity entity = new ElectricPricePlanEntity();
        entity.setId(id);
        entity.setIsCustomPrice(true);

        ElectricPricePlanDetailBo detailBo = new ElectricPricePlanDetailBo();
        detailBo.setIsCustomPrice(true);
        detailBo.setPriceHigherMultiply(BigDecimal.ONE);
        detailBo.setPriceHighMultiply(BigDecimal.ONE);
        detailBo.setPriceLowMultiply(BigDecimal.ONE);
        detailBo.setPriceLowerMultiply(BigDecimal.ONE);
        detailBo.setPriceDeepLowMultiply(BigDecimal.ONE);

        // Mock行为
        when(repository.selectById(id)).thenReturn(entity);
        when(mapper.detailEntityToBo(entity)).thenReturn(detailBo);

        // 执行测试
        ElectricPricePlanDetailBo result = service.getDetail(id);

        // 验证结果
        assertNotNull(result);
        assertNull(result.getPriceHigherMultiply());
        assertNull(result.getPriceHighMultiply());
        assertNull(result.getPriceLowMultiply());
        assertNull(result.getPriceLowerMultiply());
        assertNull(result.getPriceDeepLowMultiply());
    }

    @Test
    void testGetDetail_WithStepPrice() {
        try (MockedStatic<JacksonUtil> jacksonUtilMock = mockStatic(JacksonUtil.class)) {
            // 准备测试数据
            Integer id = 1;
            ElectricPricePlanEntity entity = new ElectricPricePlanEntity();
            entity.setId(id);
            entity.setIsStep(true);

            ElectricPricePlanDetailBo detailBo = new ElectricPricePlanDetailBo();
            detailBo.setIsStep(true);
            detailBo.setStepPrice("{\"stepPrices\":[{\"start\":0,\"end\":100,\"value\":1.0}]}");

            List<StepPriceBo> stepPrices = List.of(new StepPriceBo());

            // Mock行为
            when(repository.selectById(id)).thenReturn(entity);
            when(mapper.detailEntityToBo(entity)).thenReturn(detailBo);
            jacksonUtilMock.when(() -> JacksonUtil.fromJson(anyString(), any(TypeReference.class)))
                    .thenReturn(stepPrices);

            // 执行测试
            ElectricPricePlanDetailBo result = service.getDetail(id);

            // 验证结果
            assertNotNull(result);
            assertEquals(stepPrices, result.getStepPrices());
        }
    }

    @Test
    void testGetDetail_NotFound() {
        // 准备测试数据
        Integer id = 1;

        // Mock行为
        when(repository.selectById(id)).thenReturn(null);

        // 执行测试并验证异常
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> service.getDetail(id));
        assertEquals("电价方案数据不存在", exception.getMessage());
    }

    @Test
    void testAdd_Success() {
        try (MockedStatic<JacksonUtil> jacksonUtilMock = mockStatic(JacksonUtil.class)) {
            // 准备测试数据
            ElectricPricePlanSaveDto dto = new ElectricPricePlanSaveDto();
            dto.setId(1); // 会被设置为null
            dto.setName("测试方案");
            dto.setIsStep(true);
            dto.setStepPrices(createValidStepPrices());
            dto.setIsCustomPrice(true);

            ElectricPricePlanEntity entity = new ElectricPricePlanEntity();
            entity.setName("测试方案");
            entity.setIsCustomPrice(true);

            // Mock行为
            when(mapper.saveDtoToEntity(any(ElectricPricePlanSaveDto.class))).thenReturn(entity);
            jacksonUtilMock.when(() -> JacksonUtil.toJson(any())).thenReturn("{}");
            when(repository.findList(any(ElectricPricePlanQo.class))).thenReturn(new ArrayList<>());
            doAnswer(invocation -> {
                ((ElectricPricePlanEntity) invocation.getArgument(0)).setId(123);
                return 1;
            }).when(repository).insert(any(ElectricPricePlanEntity.class));

            // 执行测试
            Integer result = service.add(dto);

            // 验证结果
            assertEquals(123, result);
            assertNull(dto.getId()); // 确认ID被设置为null
            verify(repository).insert(any(ElectricPricePlanEntity.class));
        }
    }

    @Test
    void testEdit_Success() {
        try (MockedStatic<JacksonUtil> jacksonUtilMock = mockStatic(JacksonUtil.class)) {
            // 准备测试数据
            ElectricPricePlanSaveDto dto = new ElectricPricePlanSaveDto();
            dto.setId(1);
            dto.setName("测试方案");
            dto.setIsStep(true);
            dto.setStepPrices(createValidStepPrices());
            dto.setIsCustomPrice(true);

            ElectricPricePlanEntity oldEntity = new ElectricPricePlanEntity();
            oldEntity.setId(1);

            ElectricPricePlanEntity entity = new ElectricPricePlanEntity();
            entity.setId(1);
            entity.setName("测试方案");
            entity.setIsCustomPrice(true);

            // Mock行为
            when(repository.selectById(1)).thenReturn(oldEntity);
            when(mapper.saveDtoToEntity(dto)).thenReturn(entity);
            jacksonUtilMock.when(() -> JacksonUtil.toJson(any())).thenReturn("{}");
            when(repository.findList(any(ElectricPricePlanQo.class))).thenReturn(new ArrayList<>());
            when(repository.updateById(entity)).thenReturn(1);

            // 执行测试
            assertDoesNotThrow(() -> service.edit(dto));

            // 验证
            verify(repository).updateById(entity);
        }
    }

    @Test
    void testEdit_NotFound() {
        // 准备测试数据
        ElectricPricePlanSaveDto dto = new ElectricPricePlanSaveDto();
        dto.setId(1);

        // Mock行为
        when(repository.selectById(1)).thenReturn(null);

        // 执行测试并验证异常
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> service.edit(dto));
        assertEquals("数据不存在，请刷新后重试", exception.getMessage());
    }

    @Test
    void testDel() {
        // 准备测试数据
        Integer id = 1;

        // Mock行为
        when(repository.deleteById(id)).thenReturn(1);

        // 执行测试
        assertDoesNotThrow(() -> service.del(id));

        // 验证
        verify(repository).deleteById(id);
    }

    @Test
    void testGetDefaultStepPrice_Success() {
        try (MockedStatic<JacksonUtil> jacksonUtilMock = mockStatic(JacksonUtil.class)) {
            // 准备测试数据
            ConfigBo config = new ConfigBo();
            config.setConfigValue("{\"stepPrices\":[{\"start\":0,\"end\":100,\"value\":1.0}]}");

            List<StepPriceBo> stepPrices = createValidStepPrices();

            // Mock行为
            when(configService.getByKey(SystemConfigConstant.ELECTRIC_STEP_PRICE_KEY)).thenReturn(config);
            jacksonUtilMock.when(() -> JacksonUtil.fromJson(anyString(), any(TypeReference.class)))
                    .thenReturn(stepPrices);

            // 执行测试
            List<StepPriceBo> result = service.getDefaultStepPrice();

            // 验证结果
            assertEquals(stepPrices, result);
        }
    }

    @Test
    void testGetDefaultStepPrice_ConfigNotFound() {
        // Mock行为
        when(configService.getByKey(SystemConfigConstant.ELECTRIC_STEP_PRICE_KEY)).thenReturn(null);

        // 执行测试并验证异常
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> service.getDefaultStepPrice());
        assertEquals("请先配置默认阶梯电价", exception.getMessage());
    }

    @Test
    void testGetDefaultStepPrice_ConfigValueBlank() {
        // 准备测试数据
        ConfigBo config = new ConfigBo();
        config.setConfigValue("");

        // Mock行为
        when(configService.getByKey(SystemConfigConstant.ELECTRIC_STEP_PRICE_KEY)).thenReturn(config);

        // 执行测试并验证异常
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> service.getDefaultStepPrice());
        assertEquals("请先配置默认阶梯电价", exception.getMessage());
    }

    @Test
    void testEditDefaultStepPrice_Success() {
        try (MockedStatic<JacksonUtil> jacksonUtilMock = mockStatic(JacksonUtil.class)) {
            // 准备测试数据
            List<StepPriceBo> boList = createValidStepPrices();
            ConfigBo existingConfig = new ConfigBo();

            // Mock行为
            when(configService.getByKey(SystemConfigConstant.ELECTRIC_STEP_PRICE_KEY)).thenReturn(existingConfig);
            jacksonUtilMock.when(() -> JacksonUtil.toJson(boList)).thenReturn("{}");

            // 执行测试
            assertDoesNotThrow(() -> service.editDefaultStepPrice(boList));

            // 验证
            verify(configService).update(any(ConfigUpdateDto.class));
        }
    }

    @Test
    void testGetDefaultElectricTime_Success() {
        try (MockedStatic<JacksonUtil> jacksonUtilMock = mockStatic(JacksonUtil.class)) {
            // 准备测试数据
            ConfigBo config = new ConfigBo();
            config.setConfigValue("{\"times\":[{\"start\":\"08:00\",\"type\":1}]}");

            List<ElectricPriceTimeDto> timeBos = createValidElectricTimes();

            // Mock行为
            when(configService.getByKey(SystemConfigConstant.ELECTRIC_PRICE_TIME_KEY)).thenReturn(config);
            jacksonUtilMock.when(() -> JacksonUtil.fromJson(anyString(), any(TypeReference.class)))
                    .thenReturn(timeBos);

            // 执行测试
            List<ElectricPriceTimeDto> result = service.getElectricTime();

            // 验证结果
            assertEquals(timeBos, result);
        }
    }

    @Test
    void testGetDefaultElectricTime_ConfigNotFound() {
        // Mock行为
        when(configService.getByKey(SystemConfigConstant.ELECTRIC_PRICE_TIME_KEY)).thenReturn(null);

        // 执行测试并验证异常
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> service.getElectricTime());
        assertEquals("请先配置默认尖峰平谷深谷时间段", exception.getMessage());
    }

    @Test
    void testEditDefaultElectricTime_Success() {
        try (MockedStatic<JacksonUtil> jacksonUtilMock = mockStatic(JacksonUtil.class)) {
            // 准备测试数据
            List<ElectricPriceTimeDto> boList = createValidElectricTimes();
            ConfigBo existingConfig = new ConfigBo();

            // Mock行为
            when(configService.getByKey(SystemConfigConstant.ELECTRIC_PRICE_TIME_KEY)).thenReturn(existingConfig);
            jacksonUtilMock.when(() -> JacksonUtil.toJson(any())).thenReturn("{}");

            // 执行测试
            assertDoesNotThrow(() -> service.editElectricTime(boList));

            // 验证
            verify(configService).update(any(ConfigUpdateDto.class));
        }
    }

    @Test
    void testEditDefaultElectricTime_NormalizesTimeList() {
        try (MockedStatic<JacksonUtil> jacksonUtilMock = mockStatic(JacksonUtil.class)) {
            // 准备测试数据：乱序且缺少00:00
            List<ElectricPriceTimeDto> boList = new ArrayList<>();
            boList.add(new ElectricPriceTimeDto()
                    .setStart(LocalTime.of(18, 0))
                    .setType(ElectricPricePeriodEnum.LOW));
            boList.add(new ElectricPriceTimeDto()
                    .setStart(LocalTime.of(8, 0))
                    .setType(ElectricPricePeriodEnum.HIGH));
            ConfigBo existingConfig = new ConfigBo();

            // Mock行为
            AtomicReference<List<ElectricPriceTimeDto>> captured = new AtomicReference<>();
            when(configService.getByKey(SystemConfigConstant.ELECTRIC_PRICE_TIME_KEY)).thenReturn(existingConfig);
            jacksonUtilMock.when(() -> JacksonUtil.toJson(any())).thenAnswer(invocation -> {
                captured.set((List<ElectricPriceTimeDto>) invocation.getArgument(0));
                return "{}";
            });

            // 执行测试
            service.editElectricTime(boList);

            // 验证保存时使用的是规范化后的列表
            List<ElectricPriceTimeDto> normalizedList = captured.get();

            assertNotNull(normalizedList);
            assertEquals(3, normalizedList.size());
            assertEquals(LocalTime.of(0, 0), normalizedList.get(0).getStart());
            assertEquals(ElectricPricePeriodEnum.LOW, normalizedList.get(0).getType());
            assertEquals(LocalTime.of(8, 0), normalizedList.get(1).getStart());
            assertEquals(ElectricPricePeriodEnum.HIGH, normalizedList.get(1).getType());
            assertEquals(LocalTime.of(18, 0), normalizedList.get(2).getStart());
            assertEquals(ElectricPricePeriodEnum.LOW, normalizedList.get(2).getType());
        }
    }

    @Test
    void testGetDefaultElectricPrice_Success() {
        try (MockedStatic<JacksonUtil> jacksonUtilMock = mockStatic(JacksonUtil.class)) {
            // 准备测试数据
            ConfigBo config = new ConfigBo();
            config.setConfigValue("{\"prices\":[{\"type\":1,\"price\":1.0}]}");

            List<ElectricPriceTypeDto> typeBos = createValidElectricTypes();

            // Mock行为
            when(configService.getByKey(SystemConfigConstant.ELECTRIC_PRICE_TYPE_KEY)).thenReturn(config);
            jacksonUtilMock.when(() -> JacksonUtil.fromJson(anyString(), any(TypeReference.class)))
                    .thenReturn(typeBos);

            // 执行测试
            List<ElectricPriceTypeDto> result = service.getElectricPrice();

            // 验证结果
            assertEquals(typeBos, result);
        }
    }

    @Test
    void testGetDefaultElectricPrice_ConfigNotFound() {
        // Mock行为
        when(configService.getByKey(SystemConfigConstant.ELECTRIC_PRICE_TYPE_KEY)).thenReturn(null);

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> service.getElectricPrice());
        assertEquals("请先配置默认尖峰平谷电价", exception.getMessage());
    }

    @Test
    void testGetDefaultElectricPrice_TypeOrPriceNull() {
        try (MockedStatic<JacksonUtil> jacksonUtilMock = mockStatic(JacksonUtil.class)) {
            // 准备测试数据
            ConfigBo config = new ConfigBo();
            config.setConfigValue("{\"prices\":[{\"type\":1,\"price\":1.0}]}");

            ElectricPriceTypeDto typeDto = new ElectricPriceTypeDto();
            typeDto.setType(ElectricPricePeriodEnum.HIGHER);
            typeDto.setPrice(null);
            List<ElectricPriceTypeDto> typeBos = List.of(typeDto);

            // Mock行为
            when(configService.getByKey(SystemConfigConstant.ELECTRIC_PRICE_TYPE_KEY)).thenReturn(config);
            jacksonUtilMock.when(() -> JacksonUtil.fromJson(anyString(), any(TypeReference.class)))
                    .thenReturn(typeBos);

            // 执行测试并验证异常
            BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                    () -> service.getElectricPrice());
            assertEquals("默认尖峰平谷电价配置不完整，类型或价格不能为空", exception.getMessage());
        }
    }

    @Test
    void testEditDefaultElectricPrice_Success() {
        try (MockedStatic<JacksonUtil> jacksonUtilMock = mockStatic(JacksonUtil.class)) {
            // 准备测试数据
            List<ElectricPriceTypeDto> boList = createValidElectricTypes();
            ConfigBo existingConfig = new ConfigBo();

            ElectricPricePlanQo query = new ElectricPricePlanQo();
            query.setIsCustomPrice(false);
            List<ElectricPricePlanEntity> planEntities = createNonCustomPricePlans();

            // Mock行为
            when(configService.getByKey(SystemConfigConstant.ELECTRIC_PRICE_TYPE_KEY)).thenReturn(existingConfig);
            jacksonUtilMock.when(() -> JacksonUtil.toJson(boList)).thenReturn("{}");
            when(repository.findList(any(ElectricPricePlanQo.class))).thenReturn(planEntities);
            when(repository.updateById(any(ElectricPricePlanEntity.class))).thenReturn(1);

            // 执行测试
            assertDoesNotThrow(() -> service.editElectricPrice(boList));

            // 验证
            verify(configService).update(any(ConfigUpdateDto.class));
            verify(repository, times(planEntities.size())).updateById(any(ElectricPricePlanEntity.class));
        }
    }

    // 辅助方法
    private List<StepPriceBo> createValidStepPrices() {
        List<StepPriceBo> stepPrices = new ArrayList<>();

        StepPriceBo step1 = new StepPriceBo();
        step1.setStart(BigDecimal.ZERO);
        step1.setEnd(BigDecimal.valueOf(100));
        step1.setValue(BigDecimal.valueOf(1.0));
        stepPrices.add(step1);

        StepPriceBo step2 = new StepPriceBo();
        step2.setStart(BigDecimal.valueOf(100));
        step2.setEnd(null); // 最后一个阶段没有结束值
        step2.setValue(BigDecimal.valueOf(1.2));
        stepPrices.add(step2);

        return stepPrices;
    }

    private List<ElectricPriceTimeDto> createValidElectricTimes() {
        List<ElectricPriceTimeDto> times = new ArrayList<>();

        ElectricPriceTimeDto time1 = new ElectricPriceTimeDto();
        time1.setStart(LocalTime.of(8, 0));
        time1.setType(ElectricPricePeriodEnum.HIGH);
        times.add(time1);

        ElectricPriceTimeDto time2 = new ElectricPriceTimeDto();
        time2.setStart(LocalTime.of(12, 0));
        time2.setType(ElectricPricePeriodEnum.LOW);
        times.add(time2);

        return times;
    }

    private List<ElectricPriceTypeDto> createValidElectricTypes() {
        List<ElectricPriceTypeDto> types = new ArrayList<>();

        types.add(buildPriceType(ElectricPricePeriodEnum.HIGHER, new BigDecimal("1.00")));
        types.add(buildPriceType(ElectricPricePeriodEnum.HIGH, new BigDecimal("0.80")));
        types.add(buildPriceType(ElectricPricePeriodEnum.LOW, new BigDecimal("0.60")));
        types.add(buildPriceType(ElectricPricePeriodEnum.LOWER, new BigDecimal("0.40")));
        types.add(buildPriceType(ElectricPricePeriodEnum.DEEP_LOW, new BigDecimal("0.20")));

        return types;
    }

    private ElectricPriceTypeDto buildPriceType(ElectricPricePeriodEnum type, BigDecimal price) {
        ElectricPriceTypeDto dto = new ElectricPriceTypeDto();
        dto.setType(type);
        dto.setPrice(price);
        return dto;
    }

    private List<ElectricPricePlanEntity> createNonCustomPricePlans() {
        List<ElectricPricePlanEntity> plans = new ArrayList<>();

        ElectricPricePlanEntity plan = new ElectricPricePlanEntity();
        plan.setId(1);
        plan.setName("测试方案");
        plan.setIsCustomPrice(false);
        plan.setPriceHigherBase(BigDecimal.valueOf(1.0));
        plan.setPriceHighBase(BigDecimal.valueOf(0.8));
        plan.setPriceLowBase(BigDecimal.valueOf(0.6));
        plan.setPriceLowerBase(BigDecimal.valueOf(0.4));
        plan.setPriceDeepLowBase(BigDecimal.valueOf(0.2));
        plan.setPriceHigherMultiply(BigDecimal.valueOf(1.2));
        plan.setPriceHighMultiply(BigDecimal.valueOf(1.1));
        plan.setPriceLowMultiply(BigDecimal.valueOf(1.0));
        plan.setPriceLowerMultiply(BigDecimal.valueOf(0.9));
        plan.setPriceDeepLowMultiply(BigDecimal.valueOf(0.8));
        plans.add(plan);

        return plans;
    }

    // 测试私有方法 checkElectricPlanTime 的异常情况
    @Test
    void testCheckElectricPlanTime_AllTypeNull() {
        // 准备测试数据 - 所有时间点类型均未配置（null）
        List<ElectricPriceTimeDto> boList = new ArrayList<>();
        ElectricPriceTimeDto time1 = new ElectricPriceTimeDto();
        time1.setStart(LocalTime.of(8, 0));
        time1.setType(null);
        boList.add(time1);

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> service.editElectricTime(boList));
        assertEquals("至少配置一个时间点", exception.getMessage());
    }

    @Test
    void testCheckElectricPlanTime_AllUnconfigured() {
        // 准备测试数据 - 全部未配置（null），导致有效配置列表为空
        List<ElectricPriceTimeDto> boList = new ArrayList<>();
        ElectricPriceTimeDto time1 = new ElectricPriceTimeDto();
        time1.setStart(LocalTime.of(8, 0));
        time1.setType(null);
        boList.add(time1);
        ElectricPriceTimeDto time2 = new ElectricPriceTimeDto();
        time2.setStart(LocalTime.of(12, 0));
        time2.setType(null);
        boList.add(time2);

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> service.editElectricTime(boList));
        assertEquals("至少配置一个时间点", exception.getMessage());
    }

    @Test
    void testCheckElectricPlanTime_EmptyList() {
        // 准备测试数据 - 空列表
        List<ElectricPriceTimeDto> boList = new ArrayList<>();

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> service.editElectricTime(boList));
        assertEquals("电价方案不能为空", exception.getMessage());
    }

    @Test
    void testCheckElectricPlanTime_SkipNode() {
        // 准备测试数据 - 时间点配置跳节点
        List<ElectricPriceTimeDto> boList = new ArrayList<>();
        ElectricPriceTimeDto time1 = new ElectricPriceTimeDto();
        time1.setStart(LocalTime.of(8, 0));
        time1.setType(ElectricPricePeriodEnum.HIGH);
        boList.add(time1);
        ElectricPriceTimeDto time2 = new ElectricPriceTimeDto();
        time2.setStart(LocalTime.of(12, 0));
        time2.setType(null);
        boList.add(time2);
        ElectricPriceTimeDto time3 = new ElectricPriceTimeDto();
        time3.setStart(LocalTime.of(18, 0));
        time3.setType(ElectricPricePeriodEnum.LOW);
        boList.add(time3);

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> service.editElectricTime(boList));
        assertEquals("时间点配置不允许跳节点", exception.getMessage());
    }

    @Test
    void testCheckElectricPlanTime_ConsecutiveSameType() {
        // 准备测试数据 - 连续配置相同费率
        List<ElectricPriceTimeDto> boList = new ArrayList<>();
        ElectricPriceTimeDto time1 = new ElectricPriceTimeDto();
        time1.setStart(LocalTime.of(8, 0));
        time1.setType(ElectricPricePeriodEnum.HIGH);
        boList.add(time1);
        ElectricPriceTimeDto time2 = new ElectricPriceTimeDto();
        time2.setStart(LocalTime.of(12, 0));
        time2.setType(ElectricPricePeriodEnum.HIGH);
        boList.add(time2);

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> service.editElectricTime(boList));
        assertEquals("请勿连续配置相同费率", exception.getMessage());
    }

    @Test
    void testCheckElectricPlanTime_SameTimePoint() {
        // 准备测试数据 - 配置相同时间点
        List<ElectricPriceTimeDto> boList = new ArrayList<>();
        ElectricPriceTimeDto time1 = new ElectricPriceTimeDto();
        time1.setStart(LocalTime.of(8, 0));
        time1.setType(ElectricPricePeriodEnum.HIGH);
        boList.add(time1);
        ElectricPriceTimeDto time2 = new ElectricPriceTimeDto();
        time2.setStart(LocalTime.of(8, 0));
        time2.setType(ElectricPricePeriodEnum.LOW);
        boList.add(time2);

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> service.editElectricTime(boList));
        assertEquals("请勿配置相同时间点", exception.getMessage());
    }

    @Test
    void testCheckElectricPlanTime_TooManyTimeSegments() {
        // 准备测试数据 - 超过14个时间段
        List<ElectricPriceTimeDto> boList = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            ElectricPriceTimeDto time = new ElectricPriceTimeDto();
            time.setStart(LocalTime.of(i, 0));
            time.setType(i % 2 == 0 ? ElectricPricePeriodEnum.HIGH : ElectricPricePeriodEnum.LOW);
            boList.add(time);
        }

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> service.editElectricTime(boList));
        assertEquals("最多支持14个时间段", exception.getMessage());
    }

    // 测试私有方法 checkPriceType 的异常情况
    @Test
    void testCheckPriceType_WrongSize() {
        // 准备测试数据 - 数量不等于5
        List<ElectricPriceTypeDto> boList = new ArrayList<>();
        ElectricPricePeriodEnum[] order = new ElectricPricePeriodEnum[]{
                ElectricPricePeriodEnum.HIGHER,
                ElectricPricePeriodEnum.HIGH,
                ElectricPricePeriodEnum.LOW,
                ElectricPricePeriodEnum.LOWER,
                ElectricPricePeriodEnum.DEEP_LOW
        };
        for (int i = 0; i < 3; i++) {
            ElectricPriceTypeDto typeDto = new ElectricPriceTypeDto();
            typeDto.setType(order[i]);
            typeDto.setPrice(BigDecimal.valueOf((i + 1) * 0.5));
            boList.add(typeDto);
        }

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> service.editElectricPrice(boList));
        assertEquals("请配置尖峰平谷深谷电价", exception.getMessage());
    }

    @Test
    void testCheckPriceType_NullType() {
        // 准备测试数据 - 包含null的类型
        List<ElectricPriceTypeDto> boList = new ArrayList<>();
        ElectricPricePeriodEnum[] order = new ElectricPricePeriodEnum[]{
                ElectricPricePeriodEnum.HIGHER,
                ElectricPricePeriodEnum.HIGH,
                ElectricPricePeriodEnum.LOW,
                ElectricPricePeriodEnum.LOWER,
                ElectricPricePeriodEnum.DEEP_LOW
        };
        for (int idx = 0; idx < 5; idx++) {
            ElectricPriceTypeDto typeDto;
            if (idx == 2) {
                typeDto = null; // 第三个设为null
            } else {
                typeDto = new ElectricPriceTypeDto();
                typeDto.setType(order[idx]);
                typeDto.setPrice(BigDecimal.valueOf((idx + 1) * 0.5));
            }
            boList.add(typeDto);
        }

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> service.editElectricPrice(boList));
        assertEquals("尖峰平谷深谷电价配置不正确", exception.getMessage());
    }

    @Test
    void testCheckPriceType_NullPrice() {
        // 准备测试数据 - 价格为null
        List<ElectricPriceTypeDto> boList = new ArrayList<>();
        ElectricPricePeriodEnum[] order = new ElectricPricePeriodEnum[]{
                ElectricPricePeriodEnum.HIGHER,
                ElectricPricePeriodEnum.HIGH,
                ElectricPricePeriodEnum.LOW,
                ElectricPricePeriodEnum.LOWER,
                ElectricPricePeriodEnum.DEEP_LOW
        };
        for (int idx = 0; idx < 5; idx++) {
            ElectricPriceTypeDto typeDto = new ElectricPriceTypeDto();
            typeDto.setType(order[idx]);
            if (idx == 1) {
                typeDto.setPrice(null); // 第二个价格设为null
            } else {
                typeDto.setPrice(BigDecimal.valueOf((idx + 1) * 0.5));
            }
            boList.add(typeDto);
        }

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> service.editElectricPrice(boList));
        assertEquals("尖峰平谷深谷电价配置不正确", exception.getMessage());
    }

    @Test
    void testCheckPriceType_WrongTypeValue() {
        // 准备测试数据 - 类型值不正确
        List<ElectricPriceTypeDto> boList = new ArrayList<>();
        ElectricPricePeriodEnum[] order = new ElectricPricePeriodEnum[]{
                ElectricPricePeriodEnum.HIGHER,
                ElectricPricePeriodEnum.HIGH,
                ElectricPricePeriodEnum.LOW,
                ElectricPricePeriodEnum.LOWER,
                ElectricPricePeriodEnum.DEEP_LOW
        };
        for (int idx = 0; idx < 5; idx++) {
            ElectricPriceTypeDto typeDto = new ElectricPriceTypeDto();
            if (idx == 3) {
                // 第四个类型设为错误值（与预期LOWER不一致）
                typeDto.setType(ElectricPricePeriodEnum.TOTAL);
            } else {
                typeDto.setType(order[idx]);
            }
            typeDto.setPrice(BigDecimal.valueOf((idx + 1) * 0.5));
            boList.add(typeDto);
        }

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> service.editElectricPrice(boList));
        assertEquals("尖峰平谷深谷电价配置不正确", exception.getMessage());
    }

    // 测试私有方法 checkEntity 的异常情况
    @Test
    void testCheckEntity_MultiplyNull() {
        // 准备测试数据 - 非自定义价格但倍率为null
        ElectricPricePlanSaveDto dto = new ElectricPricePlanSaveDto();
        dto.setName("测试方案");
        dto.setIsCustomPrice(false);
        dto.setPriceHigherMultiply(null); // 倍率为null
        dto.setPriceHighMultiply(BigDecimal.valueOf(1.1));
        dto.setPriceLowMultiply(BigDecimal.valueOf(1.0));
        dto.setPriceLowerMultiply(BigDecimal.valueOf(0.9));
        dto.setPriceDeepLowMultiply(BigDecimal.valueOf(0.8));

        ElectricPricePlanEntity entity = new ElectricPricePlanEntity();
        entity.setName("测试方案");
        entity.setIsCustomPrice(false);
        entity.setPriceHigherMultiply(null);
        entity.setPriceHighMultiply(BigDecimal.valueOf(1.1));
        entity.setPriceLowMultiply(BigDecimal.valueOf(1.0));
        entity.setPriceLowerMultiply(BigDecimal.valueOf(0.9));
        entity.setPriceDeepLowMultiply(BigDecimal.valueOf(0.8));

        lenient().when(mapper.saveDtoToEntity(any(ElectricPricePlanSaveDto.class))).thenReturn(entity);
        lenient().when(repository.findList(any(ElectricPricePlanQo.class))).thenReturn(new ArrayList<>());

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> service.add(dto));
        assertEquals("倍率不能为空", exception.getMessage());
    }

    @Test
    void testCheckEntity_BasePriceNull() {
        ElectricPricePlanEntity entity = new ElectricPricePlanEntity();
        entity.setName("测试方案");
        entity.setIsCustomPrice(false);
        entity.setPriceHigher(new BigDecimal("1.20"));
        entity.setPriceHigherBase(null);
        entity.setPriceHigherMultiply(BigDecimal.ONE);
        entity.setPriceHigh(new BigDecimal("1.10"));
        entity.setPriceHighBase(new BigDecimal("1.00"));
        entity.setPriceHighMultiply(BigDecimal.ONE);
        entity.setPriceLow(new BigDecimal("1.00"));
        entity.setPriceLowBase(new BigDecimal("1.00"));
        entity.setPriceLowMultiply(BigDecimal.ONE);
        entity.setPriceLower(new BigDecimal("0.90"));
        entity.setPriceLowerBase(new BigDecimal("1.00"));
        entity.setPriceLowerMultiply(BigDecimal.ONE);
        entity.setPriceDeepLow(new BigDecimal("0.80"));
        entity.setPriceDeepLowBase(new BigDecimal("1.00"));
        entity.setPriceDeepLowMultiply(BigDecimal.ONE);

        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> service.checkEntity(entity));
        assertEquals("尖电价配置不完整", exception.getMessage());
    }

    @Test
    void testAdd_RecalculatePriceBeforeValidation() {
        // 准备测试数据 - 提供错误的价格，验证服务会重新计算
        ElectricPricePlanSaveDto dto = new ElectricPricePlanSaveDto();
        dto.setName("测试方案");
        dto.setIsCustomPrice(false);
        dto.setPriceHigherMultiply(BigDecimal.valueOf(1.2));
        dto.setPriceHighMultiply(BigDecimal.valueOf(1.1));
        dto.setPriceLowMultiply(BigDecimal.valueOf(1.0));
        dto.setPriceLowerMultiply(BigDecimal.valueOf(0.9));
        dto.setPriceDeepLowMultiply(BigDecimal.valueOf(0.8));

        ElectricPricePlanEntity entity = new ElectricPricePlanEntity();
        entity.setName("测试方案");
        entity.setIsCustomPrice(false);
        entity.setPriceHigherMultiply(BigDecimal.valueOf(1.2));
        entity.setPriceHighMultiply(BigDecimal.valueOf(1.1));
        entity.setPriceLowMultiply(BigDecimal.valueOf(1.0));
        entity.setPriceLowerMultiply(BigDecimal.valueOf(0.9));
        entity.setPriceDeepLowMultiply(BigDecimal.valueOf(0.8));
        entity.setPriceHigher(new BigDecimal("9.99")); // 故意赋予错误值

        lenient().when(mapper.saveDtoToEntity(any(ElectricPricePlanSaveDto.class))).thenReturn(entity);
        lenient().when(repository.findList(any(ElectricPricePlanQo.class))).thenReturn(new ArrayList<>());
        doAnswer(invocation -> {
            ElectricPricePlanEntity inserted = invocation.getArgument(0);
            inserted.setId(555);
            return 1;
        }).when(repository).insert(any(ElectricPricePlanEntity.class));

        Integer newId = service.add(dto);

        assertEquals(555, newId);
        assertEquals(new BigDecimal("1.200"), entity.getPriceHigher());
    }

    @Test
    void testCheckEntity_DuplicateName() {
        // 准备测试数据 - 方案名称重复
        ElectricPricePlanSaveDto dto = new ElectricPricePlanSaveDto();
        dto.setName("重复方案");
        dto.setIsCustomPrice(true);

        ElectricPricePlanEntity entity = new ElectricPricePlanEntity();
        entity.setName("重复方案");
        entity.setIsCustomPrice(true);

        List<ElectricPricePlanEntity> existingPlans = new ArrayList<>();
        ElectricPricePlanEntity existingPlan = new ElectricPricePlanEntity();
        existingPlan.setName("重复方案");
        existingPlans.add(existingPlan);

        lenient().when(mapper.saveDtoToEntity(any(ElectricPricePlanSaveDto.class))).thenReturn(entity);
        when(repository.findList(any(ElectricPricePlanQo.class))).thenReturn(existingPlans);

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> service.add(dto));
        assertEquals("方案名称已存在", exception.getMessage());
    }

    // 测试私有方法 checkStepPrice 的异常情况
    @Test
    void testCheckStepPrice_EmptyList() {
        // 准备测试数据 - 空的阶梯价格列表
        ElectricPricePlanSaveDto dto = new ElectricPricePlanSaveDto();
        dto.setName("测试方案");
        dto.setIsStep(true);
        dto.setStepPrices(new ArrayList<>());
        dto.setIsCustomPrice(true);

        ElectricPricePlanEntity entity = new ElectricPricePlanEntity();
        entity.setName("测试方案");
        entity.setIsCustomPrice(true);

        lenient().when(mapper.saveDtoToEntity(any(ElectricPricePlanSaveDto.class))).thenReturn(entity);
        lenient().when(repository.findList(any(ElectricPricePlanQo.class))).thenReturn(new ArrayList<>());

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> service.add(dto));
        assertEquals("至少配置一个阶梯", exception.getMessage());
    }

    @Test
    void testCheckStepPrice_TooManySteps() {
        // 准备测试数据 - 超过3个阶梯
        List<StepPriceBo> stepPrices = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            StepPriceBo step = new StepPriceBo();
            step.setStart(BigDecimal.valueOf(i * 100));
            if (i < 3) {
                step.setEnd(BigDecimal.valueOf((i + 1) * 100));
            }
            step.setValue(BigDecimal.valueOf(1.0 + i * 0.1));
            stepPrices.add(step);
        }

        ElectricPricePlanSaveDto dto = new ElectricPricePlanSaveDto();
        dto.setName("测试方案");
        dto.setIsStep(true);
        dto.setStepPrices(stepPrices);
        dto.setIsCustomPrice(true);

        ElectricPricePlanEntity entity = new ElectricPricePlanEntity();
        entity.setName("测试方案");
        entity.setIsCustomPrice(true);

        lenient().when(mapper.saveDtoToEntity(any(ElectricPricePlanSaveDto.class))).thenReturn(entity);
        lenient().when(repository.findList(any(ElectricPricePlanQo.class))).thenReturn(new ArrayList<>());

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> service.add(dto));
        assertEquals("最多支持3个等级", exception.getMessage());
    }

    // 测试私有方法 checkStepOrder 的异常情况
    @Test
    void testCheckStepOrder_StartValueNull() {
        // 准备测试数据 - 开始值为null
        List<StepPriceBo> stepPrices = new ArrayList<>();
        StepPriceBo step1 = new StepPriceBo();
        step1.setStart(null); // 开始值为null
        step1.setEnd(BigDecimal.valueOf(100));
        step1.setValue(BigDecimal.valueOf(1.0));
        stepPrices.add(step1);

        ElectricPricePlanSaveDto dto = new ElectricPricePlanSaveDto();
        dto.setName("测试方案");
        dto.setIsStep(true);
        dto.setStepPrices(stepPrices);
        dto.setIsCustomPrice(true);

        ElectricPricePlanEntity entity = new ElectricPricePlanEntity();
        entity.setName("测试方案");
        entity.setIsCustomPrice(true);

        lenient().when(mapper.saveDtoToEntity(any(ElectricPricePlanSaveDto.class))).thenReturn(entity);
        lenient().when(repository.findList(any(ElectricPricePlanQo.class))).thenReturn(new ArrayList<>());

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> service.add(dto));
        assertEquals("第1阶段开始值不能为空", exception.getMessage());
    }

    @Test
    void testCheckStepOrder_FirstStepNotStartFromZero() {
        // 准备测试数据 - 第一阶段不从0开始
        List<StepPriceBo> stepPrices = new ArrayList<>();
        StepPriceBo step1 = new StepPriceBo();
        step1.setStart(BigDecimal.valueOf(10)); // 不从0开始
        step1.setEnd(BigDecimal.valueOf(100));
        step1.setValue(BigDecimal.valueOf(1.0));
        stepPrices.add(step1);

        ElectricPricePlanSaveDto dto = new ElectricPricePlanSaveDto();
        dto.setName("测试方案");
        dto.setIsStep(true);
        dto.setStepPrices(stepPrices);
        dto.setIsCustomPrice(true);

        ElectricPricePlanEntity entity = new ElectricPricePlanEntity();
        entity.setName("测试方案");
        entity.setIsCustomPrice(true);

        lenient().when(mapper.saveDtoToEntity(any(ElectricPricePlanSaveDto.class))).thenReturn(entity);
        lenient().when(repository.findList(any(ElectricPricePlanQo.class))).thenReturn(new ArrayList<>());

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> service.add(dto));
        assertEquals("第一阶段必须从0开始", exception.getMessage());
    }

    @Test
    void testCheckStepOrder_EndValueNull() {
        // 准备测试数据 - 非最后阶段结束值为null
        List<StepPriceBo> stepPrices = new ArrayList<>();
        StepPriceBo step1 = new StepPriceBo();
        step1.setStart(BigDecimal.ZERO);
        step1.setEnd(null); // 结束值为null
        step1.setValue(BigDecimal.valueOf(1.0));
        stepPrices.add(step1);
        StepPriceBo step2 = new StepPriceBo();
        step2.setStart(BigDecimal.valueOf(100));
        step2.setEnd(null);
        step2.setValue(BigDecimal.valueOf(1.2));
        stepPrices.add(step2);

        ElectricPricePlanSaveDto dto = new ElectricPricePlanSaveDto();
        dto.setName("测试方案");
        dto.setIsStep(true);
        dto.setStepPrices(stepPrices);
        dto.setIsCustomPrice(true);

        ElectricPricePlanEntity entity = new ElectricPricePlanEntity();
        entity.setName("测试方案");
        entity.setIsCustomPrice(true);

        lenient().when(mapper.saveDtoToEntity(any(ElectricPricePlanSaveDto.class))).thenReturn(entity);
        lenient().when(repository.findList(any(ElectricPricePlanQo.class))).thenReturn(new ArrayList<>());

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> service.add(dto));
        assertEquals("上一阶段结束值不能为空", exception.getMessage());
    }

    @Test
    void testCheckStepOrder_EndLessOrEqualStart() {
        // 准备测试数据 - 结束值小于等于开始值
        List<StepPriceBo> stepPrices = new ArrayList<>();
        StepPriceBo step1 = new StepPriceBo();
        step1.setStart(BigDecimal.ZERO);
        step1.setEnd(BigDecimal.ZERO); // 结束值等于开始值
        step1.setValue(BigDecimal.valueOf(1.0));
        stepPrices.add(step1);
        StepPriceBo step2 = new StepPriceBo();
        step2.setStart(BigDecimal.valueOf(100));
        step2.setEnd(null);
        step2.setValue(BigDecimal.valueOf(1.2));
        stepPrices.add(step2);

        ElectricPricePlanSaveDto dto = new ElectricPricePlanSaveDto();
        dto.setName("测试方案");
        dto.setIsStep(true);
        dto.setStepPrices(stepPrices);
        dto.setIsCustomPrice(true);

        ElectricPricePlanEntity entity = new ElectricPricePlanEntity();
        entity.setName("测试方案");
        entity.setIsCustomPrice(true);

        lenient().when(mapper.saveDtoToEntity(any(ElectricPricePlanSaveDto.class))).thenReturn(entity);
        lenient().when(repository.findList(any(ElectricPricePlanQo.class))).thenReturn(new ArrayList<>());

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> service.add(dto));
        assertEquals("阶段结束值必须大于阶段开始值", exception.getMessage());
    }

    @Test
    void testCheckStepOrder_NextStartNotEqualPrevEnd() {
        // 准备测试数据 - 下一阶段开始值不等于上一阶段结束值
        List<StepPriceBo> stepPrices = new ArrayList<>();
        StepPriceBo step1 = new StepPriceBo();
        step1.setStart(BigDecimal.ZERO);
        step1.setEnd(BigDecimal.valueOf(100));
        step1.setValue(BigDecimal.valueOf(1.0));
        stepPrices.add(step1);
        StepPriceBo step2 = new StepPriceBo();
        step2.setStart(BigDecimal.valueOf(150)); // 不等于上一阶段结束值
        step2.setEnd(null);
        step2.setValue(BigDecimal.valueOf(1.2));
        stepPrices.add(step2);

        ElectricPricePlanSaveDto dto = new ElectricPricePlanSaveDto();
        dto.setName("测试方案");
        dto.setIsStep(true);
        dto.setStepPrices(stepPrices);
        dto.setIsCustomPrice(true);

        ElectricPricePlanEntity entity = new ElectricPricePlanEntity();
        entity.setName("测试方案");
        entity.setIsCustomPrice(true);

        lenient().when(mapper.saveDtoToEntity(any(ElectricPricePlanSaveDto.class))).thenReturn(entity);
        lenient().when(repository.findList(any(ElectricPricePlanQo.class))).thenReturn(new ArrayList<>());

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> service.add(dto));
        assertEquals("阶段开始值必须和上一阶段结束值相同", exception.getMessage());
    }

    @Test
    void testCheckStepOrder_LastStepHasEndValue() {
        // 准备测试数据 - 最后一个阶段有上限值
        List<StepPriceBo> stepPrices = new ArrayList<>();
        StepPriceBo step1 = new StepPriceBo();
        step1.setStart(BigDecimal.ZERO);
        step1.setEnd(BigDecimal.valueOf(100));
        step1.setValue(BigDecimal.valueOf(1.0));
        stepPrices.add(step1);
        StepPriceBo step2 = new StepPriceBo();
        step2.setStart(BigDecimal.valueOf(100));
        step2.setEnd(BigDecimal.valueOf(200)); // 最后一个阶段不应该有上限值
        step2.setValue(BigDecimal.valueOf(1.2));
        stepPrices.add(step2);

        ElectricPricePlanSaveDto dto = new ElectricPricePlanSaveDto();
        dto.setName("测试方案");
        dto.setIsStep(true);
        dto.setStepPrices(stepPrices);
        dto.setIsCustomPrice(true);

        ElectricPricePlanEntity entity = new ElectricPricePlanEntity();
        entity.setName("测试方案");
        entity.setIsCustomPrice(true);

        lenient().when(mapper.saveDtoToEntity(any(ElectricPricePlanSaveDto.class))).thenReturn(entity);
        lenient().when(repository.findList(any(ElectricPricePlanQo.class))).thenReturn(new ArrayList<>());

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> service.add(dto));
        assertEquals("最后一个阶段不能有上限值", exception.getMessage());
    }

    @Test
    void testUpdateSystemConfig_ConfigNotFound() {
        // 准备测试数据
        List<StepPriceBo> boList = createValidStepPrices();

        // Mock行为 - 配置不存在
        when(configService.getByKey(SystemConfigConstant.ELECTRIC_STEP_PRICE_KEY)).thenReturn(null);

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> service.editDefaultStepPrice(boList));
        assertEquals("请先设置默认配置" + SystemConfigConstant.ELECTRIC_STEP_PRICE_KEY, exception.getMessage());
    }

    @Test
    void testAdd_NonCustomPricePassValidation() {
        ElectricPricePlanSaveDto dto = new ElectricPricePlanSaveDto();
        dto.setId(5);
        dto.setIsCustomPrice(false);
        dto.setIsStep(false);
        dto.setName("non-custom-plan");
        dto.setPriceHigherMultiply(new BigDecimal("1.20"));
        dto.setPriceHighMultiply(new BigDecimal("1.10"));
        dto.setPriceLowMultiply(new BigDecimal("1.00"));
        dto.setPriceLowerMultiply(new BigDecimal("0.90"));
        dto.setPriceDeepLowMultiply(new BigDecimal("0.80"));

        ElectricPricePlanEntity entity = new ElectricPricePlanEntity();
        entity.setName("non-custom-plan");
        entity.setIsCustomPrice(false);
        entity.setPriceHigherMultiply(new BigDecimal("1.20"));
        entity.setPriceHighMultiply(new BigDecimal("1.10"));
        entity.setPriceLowMultiply(new BigDecimal("1.00"));
        entity.setPriceLowerMultiply(new BigDecimal("0.90"));
        entity.setPriceDeepLowMultiply(new BigDecimal("0.80"));

        when(mapper.saveDtoToEntity(dto)).thenReturn(entity);
        when(repository.findList(any(ElectricPricePlanQo.class))).thenReturn(Collections.emptyList());
        doAnswer(invocation -> {
            ElectricPricePlanEntity inserted = invocation.getArgument(0);
            inserted.setId(77);
            return 1;
        }).when(repository).insert(any(ElectricPricePlanEntity.class));

        Integer id = service.add(dto);

        assertEquals(77, id);
        assertNull(dto.getId());
        verify(repository).insert(entity);
        verify(repository).findList(any(ElectricPricePlanQo.class));
    }

    @Test
    void testEdit_IncompleteBaseConfigThrowsException() {
        ElectricPricePlanSaveDto dto = new ElectricPricePlanSaveDto();
        dto.setId(10);
        dto.setIsCustomPrice(false);
        dto.setIsStep(false);
        dto.setName("invalid-price-plan");
        dto.setPriceHigherMultiply(new BigDecimal("1.20"));
        dto.setPriceHighMultiply(new BigDecimal("1.10"));
        dto.setPriceLowMultiply(new BigDecimal("1.00"));
        dto.setPriceLowerMultiply(new BigDecimal("0.90"));
        dto.setPriceDeepLowMultiply(new BigDecimal("0.80"));

        ElectricPricePlanEntity oldEntity = new ElectricPricePlanEntity();
        oldEntity.setId(10);

        ElectricPricePlanEntity entity = new ElectricPricePlanEntity();
        entity.setId(10);
        entity.setName("invalid-price-plan");
        entity.setIsCustomPrice(false);
        entity.setPriceHigherMultiply(new BigDecimal("1.20"));
        entity.setPriceHighMultiply(new BigDecimal("1.10"));
        entity.setPriceLowMultiply(new BigDecimal("1.00"));
        entity.setPriceLowerMultiply(new BigDecimal("0.90"));
        entity.setPriceDeepLowMultiply(new BigDecimal("0.80"));

        when(repository.selectById(anyInt())).thenReturn(oldEntity);
        when(mapper.saveDtoToEntity(dto)).thenReturn(entity);

        ConfigBo incompleteConfig = new ConfigBo();
        incompleteConfig.setConfigValue(JacksonUtil.toJson(List.of(
                buildBaseType(ElectricPricePeriodEnum.HIGHER, new BigDecimal("1.00")),
                buildBaseType(ElectricPricePeriodEnum.HIGH, new BigDecimal("0.80"))
        )));
        when(configService.getByKey(SystemConfigConstant.ELECTRIC_PRICE_TYPE_KEY)).thenReturn(incompleteConfig);

        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class, () -> service.edit(dto));
        assertTrue(exception.getMessage().contains("默认尖峰平谷电价配置不完整"));
        verify(repository, never()).updateById(any(ElectricPricePlanEntity.class));
    }

    @Test
    void testEdit_BaseConfigPriceNullThrowsException() {
        ElectricPricePlanSaveDto dto = new ElectricPricePlanSaveDto();
        dto.setId(10);
        dto.setIsCustomPrice(false);
        dto.setIsStep(false);
        dto.setName("invalid-price-plan");
        dto.setPriceHigherMultiply(new BigDecimal("1.20"));
        dto.setPriceHighMultiply(new BigDecimal("1.10"));
        dto.setPriceLowMultiply(new BigDecimal("1.00"));
        dto.setPriceLowerMultiply(new BigDecimal("0.90"));
        dto.setPriceDeepLowMultiply(new BigDecimal("0.80"));

        ElectricPricePlanEntity oldEntity = new ElectricPricePlanEntity();
        oldEntity.setId(10);

        ElectricPricePlanEntity entity = new ElectricPricePlanEntity();
        entity.setId(10);
        entity.setName("invalid-price-plan");
        entity.setIsCustomPrice(false);
        entity.setPriceHigherMultiply(new BigDecimal("1.20"));
        entity.setPriceHighMultiply(new BigDecimal("1.10"));
        entity.setPriceLowMultiply(new BigDecimal("1.00"));
        entity.setPriceLowerMultiply(new BigDecimal("0.90"));
        entity.setPriceDeepLowMultiply(new BigDecimal("0.80"));

        when(repository.selectById(anyInt())).thenReturn(oldEntity);
        when(mapper.saveDtoToEntity(dto)).thenReturn(entity);

        ConfigBo invalidConfig = new ConfigBo();
        invalidConfig.setConfigValue(JacksonUtil.toJson(List.of(
                buildBaseType(ElectricPricePeriodEnum.HIGHER, null),
                buildBaseType(ElectricPricePeriodEnum.HIGH, new BigDecimal("0.80")),
                buildBaseType(ElectricPricePeriodEnum.LOW, new BigDecimal("0.60")),
                buildBaseType(ElectricPricePeriodEnum.LOWER, new BigDecimal("0.40")),
                buildBaseType(ElectricPricePeriodEnum.DEEP_LOW, new BigDecimal("0.20"))
        )));
        when(configService.getByKey(SystemConfigConstant.ELECTRIC_PRICE_TYPE_KEY)).thenReturn(invalidConfig);

        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class, () -> service.edit(dto));
        assertEquals("默认尖峰平谷电价配置不完整，类型或价格不能为空", exception.getMessage());
        verify(repository, never()).updateById(any(ElectricPricePlanEntity.class));
    }

    @Test
    void testEdit_BaseConfigPriceNegativeThrowsException() {
        ElectricPricePlanSaveDto dto = new ElectricPricePlanSaveDto();
        dto.setId(10);
        dto.setIsCustomPrice(false);
        dto.setIsStep(false);
        dto.setName("invalid-price-plan");
        dto.setPriceHigherMultiply(new BigDecimal("1.20"));
        dto.setPriceHighMultiply(new BigDecimal("1.10"));
        dto.setPriceLowMultiply(new BigDecimal("1.00"));
        dto.setPriceLowerMultiply(new BigDecimal("0.90"));
        dto.setPriceDeepLowMultiply(new BigDecimal("0.80"));

        ElectricPricePlanEntity oldEntity = new ElectricPricePlanEntity();
        oldEntity.setId(10);

        ElectricPricePlanEntity entity = new ElectricPricePlanEntity();
        entity.setId(10);
        entity.setName("invalid-price-plan");
        entity.setIsCustomPrice(false);
        entity.setPriceHigherMultiply(new BigDecimal("1.20"));
        entity.setPriceHighMultiply(new BigDecimal("1.10"));
        entity.setPriceLowMultiply(new BigDecimal("1.00"));
        entity.setPriceLowerMultiply(new BigDecimal("0.90"));
        entity.setPriceDeepLowMultiply(new BigDecimal("0.80"));

        when(repository.selectById(anyInt())).thenReturn(oldEntity);
        when(mapper.saveDtoToEntity(dto)).thenReturn(entity);

        ConfigBo invalidConfig = new ConfigBo();
        invalidConfig.setConfigValue(JacksonUtil.toJson(List.of(
                buildBaseType(ElectricPricePeriodEnum.HIGHER, new BigDecimal("-1.00")),
                buildBaseType(ElectricPricePeriodEnum.HIGH, new BigDecimal("0.80")),
                buildBaseType(ElectricPricePeriodEnum.LOW, new BigDecimal("0.60")),
                buildBaseType(ElectricPricePeriodEnum.LOWER, new BigDecimal("0.40")),
                buildBaseType(ElectricPricePeriodEnum.DEEP_LOW, new BigDecimal("0.20"))
        )));
        when(configService.getByKey(SystemConfigConstant.ELECTRIC_PRICE_TYPE_KEY)).thenReturn(invalidConfig);

        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class, () -> service.edit(dto));
        assertEquals("默认尖峰平谷电价配置不正确，HIGHER价格不能为负数", exception.getMessage());
        verify(repository, never()).updateById(any(ElectricPricePlanEntity.class));
    }


    private ElectricPriceTypeDto buildBaseType(ElectricPricePeriodEnum type, BigDecimal price) {
        ElectricPriceTypeDto dto = new ElectricPriceTypeDto();
        dto.setType(type);
        dto.setPrice(price);
        return dto;
    }
}
