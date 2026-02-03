package info.zhihui.ems.business.plan;

import info.zhihui.ems.business.plan.bo.ElectricPricePlanBo;
import info.zhihui.ems.business.plan.bo.ElectricPricePlanDetailBo;
import info.zhihui.ems.business.plan.bo.StepPriceBo;
import info.zhihui.ems.business.plan.dto.ElectricPriceTimeDto;
import info.zhihui.ems.business.plan.dto.ElectricPriceTypeDto;
import info.zhihui.ems.business.plan.dto.ElectricPricePlanQueryDto;
import info.zhihui.ems.business.plan.dto.ElectricPricePlanSaveDto;
import info.zhihui.ems.business.plan.service.ElectricPricePlanService;
import info.zhihui.ems.common.enums.ElectricPricePeriodEnum;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.exception.NotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ElectricPricePlanService 集成测试
 *
 * @author jerryxiaosa
 */
@SpringBootTest
@ActiveProfiles("integrationtest")
@Transactional
public class ElectricPricePlanServiceImplIntegrationTest {

    @Autowired
    private ElectricPricePlanService electricPricePlanService;

    @Test
    @DisplayName("参数校验测试")
    void testElectricPricePlanService_ValidationTests_ShouldThrowException() {
        // 测试1: findList方法 - null参数
        assertThrows(ConstraintViolationException.class, () -> {
            electricPricePlanService.findList(null);
        }, "findList方法null参数应抛出ConstraintViolationException");

        // 测试2: getDetail方法 - null参数
        assertThrows(ConstraintViolationException.class, () -> {
            electricPricePlanService.getDetail(null);
        }, "getDetail方法null参数应抛出ConstraintViolationException");

        // 测试3: add方法 - null参数
        assertThrows(ConstraintViolationException.class, () -> {
            electricPricePlanService.add(null);
        }, "add方法null参数应抛出ConstraintViolationException");

        // 测试4: edit方法 - null参数
        assertThrows(ConstraintViolationException.class, () -> {
            electricPricePlanService.edit(null);
        }, "edit方法null参数应抛出ConstraintViolationException");

        // 测试5: del方法 - null参数
        assertThrows(ConstraintViolationException.class, () -> {
            electricPricePlanService.del(null);
        }, "del方法null参数应抛出ConstraintViolationException");

        // 测试6: editDefaultStepPrice方法 - null参数
        assertThrows(ConstraintViolationException.class, () -> {
            electricPricePlanService.editDefaultStepPrice(null);
        }, "editDefaultStepPrice方法null参数应抛出ConstraintViolationException");

        // 测试7: editDefaultElectricTime方法 - null参数
        assertThrows(ConstraintViolationException.class, () -> {
            electricPricePlanService.editElectricTime(null);
        }, "editDefaultElectricTime方法null参数应抛出ConstraintViolationException");

        // 测试8: editDefaultElectricPrice方法 - null参数
        assertThrows(ConstraintViolationException.class, () -> {
            electricPricePlanService.editElectricPrice(null);
        }, "editDefaultElectricPrice方法null参数应抛出ConstraintViolationException");

    }

    @Test
    public void testFindList_Success() {
        ElectricPricePlanQueryDto query = new ElectricPricePlanQueryDto();
        query.setName("方案");
        List<ElectricPricePlanBo> result = electricPricePlanService.findList(query);
        assertNotNull(result);
    }

    @Test
    @DisplayName("findList方法集成测试 - 覆盖所有查询字段条件")
    public void testFindList_AllFilterFields_Coverage() {
        // 覆盖 name / isCustomPrice / neId / eqName
        ElectricPricePlanQueryDto q1 = new ElectricPricePlanQueryDto()
                .setName("默认")
                .setIsCustomPrice(false)
                .setNeId(1)
                .setEqName("默认电价方案");
        List<ElectricPricePlanBo> r1 = electricPricePlanService.findList(q1);
        assertNotNull(r1);

        ElectricPricePlanQueryDto q2 = new ElectricPricePlanQueryDto()
                .setIsCustomPrice(true);
        List<ElectricPricePlanBo> r2 = electricPricePlanService.findList(q2);
        assertNotNull(r2);
    }

    @Test
    public void testGetDetail_NotFound() {
        assertThrows(NotFoundException.class, () -> electricPricePlanService.getDetail(99999));
    }

    @Test
    public void testGetDetail_Success() {
        // 先添加一个电价方案
        ElectricPricePlanSaveDto saveDto = createTestElectricPricePlanSaveDto();
        Integer id = electricPricePlanService.add(saveDto);

        // 获取详情
        ElectricPricePlanDetailBo detail = electricPricePlanService.getDetail(id);
        assertNotNull(detail);
        assertEquals(saveDto.getName(), detail.getName());
    }

    @Test
    public void testAdd_Success() {
        ElectricPricePlanSaveDto saveDto = createTestElectricPricePlanSaveDto();
        Integer id = electricPricePlanService.add(saveDto);
        assertNotNull(id);
        assertTrue(id > 0);
    }

    @Test
    public void testEdit_NotFound() {
        ElectricPricePlanSaveDto saveDto = createTestElectricPricePlanSaveDto();
        saveDto.setId(99999);
        assertThrows(NotFoundException.class, () -> electricPricePlanService.edit(saveDto));
    }

    @Test
    public void testEdit_Success() {
        // 先添加一个电价方案
        ElectricPricePlanSaveDto saveDto = createTestElectricPricePlanSaveDto();
        Integer id = electricPricePlanService.add(saveDto);

        // 修改方案
        saveDto.setId(id);
        saveDto.setName("修改后的方案名称");
        assertDoesNotThrow(() -> electricPricePlanService.edit(saveDto));

        // 验证修改结果
        ElectricPricePlanDetailBo detail = electricPricePlanService.getDetail(id);
        assertEquals("修改后的方案名称", detail.getName());
    }

    @Test
    public void testDel_Success() {
        // 先添加一个电价方案
        ElectricPricePlanSaveDto saveDto = createTestElectricPricePlanSaveDto();
        Integer id = electricPricePlanService.add(saveDto);

        // 删除方案
        assertDoesNotThrow(() -> electricPricePlanService.del(id));

        // 验证删除结果
        assertThrows(NotFoundException.class, () -> electricPricePlanService.getDetail(id));
    }

    @Test
    public void testGetDefaultStepPrice_Success() {
        try {
            List<StepPriceBo> result = electricPricePlanService.getDefaultStepPrice();
            assertNotNull(result);
        } catch (NotFoundException e) {
            // 如果没有配置默认阶梯电价，抛出NotFoundException是正常的
            assertTrue(e.getMessage().contains("请先配置默认阶梯电价"));
        }
    }

    @Test
    public void testEditDefaultStepPrice_Success() {
        List<StepPriceBo> stepPrices = createTestStepPrices();
        assertDoesNotThrow(() -> electricPricePlanService.editDefaultStepPrice(stepPrices));
    }

    @Test
    public void testGetElectricTime_Success() {
        try {
            List<ElectricPriceTimeDto> result = electricPricePlanService.getElectricTime();
            assertNotNull(result);
        } catch (NotFoundException e) {
            // 如果没有配置默认时间段，抛出NotFoundException是正常的
            assertTrue(e.getMessage().contains("请先配置默认尖峰平谷深谷时间段"));
        }
    }

    @Test
    public void testEditElectricTime_Success() {
        List<ElectricPriceTimeDto> timeDtos = createTestElectricPriceTimes();
        assertDoesNotThrow(() -> electricPricePlanService.editElectricTime(timeDtos));
    }

    @Test
    public void testGetElectricPrice_Success() {
        try {
            List<ElectricPriceTypeDto> result = electricPricePlanService.getElectricPrice();
            assertNotNull(result);
        } catch (BusinessRuntimeException e) {
            // 如果没有配置默认电价，抛出BusinessRuntimeException是正常的
            assertTrue(e.getMessage().contains("请先配置默认尖峰平谷电价"));
        }
    }

    @Test
    public void testEditElectricPrice_Success() {
        Map<Integer, BigDecimal> originalBaseMap = getCurrentBasePriceMap();
        Integer planId1 = electricPricePlanService.add(createNonCustomElectricPricePlanSaveDto("非自定义方案A" + System.currentTimeMillis(), originalBaseMap));
        Integer planId2 = electricPricePlanService.add(createNonCustomElectricPricePlanSaveDto("非自定义方案B" + (System.currentTimeMillis() + 1), originalBaseMap));

        Map<Integer, BigDecimal> updatedBaseMap = buildUpdatedBasePriceMap(originalBaseMap);
        List<ElectricPriceTypeDto> priceDtos = createTestElectricPriceTypes(updatedBaseMap);
        assertDoesNotThrow(() -> electricPricePlanService.editElectricPrice(priceDtos));

        ElectricPricePlanDetailBo detail1 = electricPricePlanService.getDetail(planId1);
        ElectricPricePlanDetailBo detail2 = electricPricePlanService.getDetail(planId2);

        assertNonCustomPlanSynced(detail1, updatedBaseMap);
        assertNonCustomPlanSynced(detail2, updatedBaseMap);
    }

    /**
     * 创建测试用的电价方案保存DTO
     */
    private ElectricPricePlanSaveDto createTestElectricPricePlanSaveDto() {
        ElectricPricePlanSaveDto dto = new ElectricPricePlanSaveDto();
        dto.setName("测试电价方案" + System.currentTimeMillis());
        dto.setIsCustomPrice(true);
        dto.setPriceHigher(new BigDecimal("1.2"));
        dto.setPriceHigh(new BigDecimal("1.0"));
        dto.setPriceLow(new BigDecimal("0.8"));
        dto.setPriceLower(new BigDecimal("0.6"));
        dto.setPriceDeepLow(new BigDecimal("0.4"));
        dto.setIsStep(false);
        return dto;
    }

    /**
     * 创建测试用的阶梯价格列表
     */
    private List<StepPriceBo> createTestStepPrices() {
        List<StepPriceBo> stepPrices = new ArrayList<>();

        StepPriceBo step1 = new StepPriceBo();
        step1.setStart(new BigDecimal("0"));
        step1.setEnd(new BigDecimal("100"));
        step1.setValue(new BigDecimal("1.0"));
        stepPrices.add(step1);

        StepPriceBo step2 = new StepPriceBo();
        step2.setStart(new BigDecimal("100"));
        step2.setValue(new BigDecimal("1.2"));
        stepPrices.add(step2);

        return stepPrices;
    }

    /**
     * 创建测试用的电价时间段列表
     */
    private List<ElectricPriceTimeDto> createTestElectricPriceTimes() {
        List<ElectricPriceTimeDto> timeDtos = new ArrayList<>();

        ElectricPriceTimeDto time1 = new ElectricPriceTimeDto();
        time1.setType(ElectricPricePeriodEnum.HIGH);
        time1.setStart(java.time.LocalTime.of(8, 0));
        timeDtos.add(time1);

        ElectricPriceTimeDto time2 = new ElectricPriceTimeDto();
        time2.setType(ElectricPricePeriodEnum.LOW);
        time2.setStart(java.time.LocalTime.of(12, 0));
        timeDtos.add(time2);

        return timeDtos;
    }

    /**
     * 创建测试用的电价类型列表
     */
    private List<ElectricPriceTypeDto> createTestElectricPriceTypes(Map<Integer, BigDecimal> basePriceMap) {
        List<ElectricPriceTypeDto> priceDtos = new ArrayList<>();

        ElectricPriceTypeDto price1 = new ElectricPriceTypeDto();
        price1.setType(ElectricPricePeriodEnum.HIGHER);
        price1.setPrice(basePriceMap.get(ElectricPricePeriodEnum.HIGHER.getCode()));
        priceDtos.add(price1);

        ElectricPriceTypeDto price2 = new ElectricPriceTypeDto();
        price2.setType(ElectricPricePeriodEnum.HIGH);
        price2.setPrice(basePriceMap.get(ElectricPricePeriodEnum.HIGH.getCode()));
        priceDtos.add(price2);

        ElectricPriceTypeDto price3 = new ElectricPriceTypeDto();
        price3.setType(ElectricPricePeriodEnum.LOW);
        price3.setPrice(basePriceMap.get(ElectricPricePeriodEnum.LOW.getCode()));
        priceDtos.add(price3);

        ElectricPriceTypeDto price4 = new ElectricPriceTypeDto();
        price4.setType(ElectricPricePeriodEnum.LOWER);
        price4.setPrice(basePriceMap.get(ElectricPricePeriodEnum.LOWER.getCode()));
        priceDtos.add(price4);

        ElectricPriceTypeDto price5 = new ElectricPriceTypeDto();
        price5.setType(ElectricPricePeriodEnum.DEEP_LOW);
        price5.setPrice(basePriceMap.get(ElectricPricePeriodEnum.DEEP_LOW.getCode()));
        priceDtos.add(price5);

        return priceDtos;
    }

    private ElectricPricePlanSaveDto createNonCustomElectricPricePlanSaveDto(String name, Map<Integer, BigDecimal> basePriceMap) {
        ElectricPricePlanSaveDto dto = new ElectricPricePlanSaveDto();
        dto.setName(name);
        dto.setIsCustomPrice(false);
        dto.setIsStep(false);

        BigDecimal higherMultiply = new BigDecimal("1.10");
        BigDecimal highMultiply = new BigDecimal("1.08");
        BigDecimal lowMultiply = new BigDecimal("1.05");
        BigDecimal lowerMultiply = new BigDecimal("1.03");
        BigDecimal deepLowMultiply = new BigDecimal("1.02");

        dto.setPriceHigherMultiply(higherMultiply);
        dto.setPriceHighMultiply(highMultiply);
        dto.setPriceLowMultiply(lowMultiply);
        dto.setPriceLowerMultiply(lowerMultiply);
        dto.setPriceDeepLowMultiply(deepLowMultiply);

        BigDecimal higherBase = basePriceMap.get(ElectricPricePeriodEnum.HIGHER.getCode());
        BigDecimal highBase = basePriceMap.get(ElectricPricePeriodEnum.HIGH.getCode());
        BigDecimal lowBase = basePriceMap.get(ElectricPricePeriodEnum.LOW.getCode());
        BigDecimal lowerBase = basePriceMap.get(ElectricPricePeriodEnum.LOWER.getCode());
        BigDecimal deepLowBase = basePriceMap.get(ElectricPricePeriodEnum.DEEP_LOW.getCode());

        dto.setPriceHigher(higherBase.multiply(higherMultiply));
        dto.setPriceHigh(highBase.multiply(highMultiply));
        dto.setPriceLow(lowBase.multiply(lowMultiply));
        dto.setPriceLower(lowerBase.multiply(lowerMultiply));
        dto.setPriceDeepLow(deepLowBase.multiply(deepLowMultiply));

        return dto;
    }

    private void assertNonCustomPlanSynced(ElectricPricePlanBo plan, Map<Integer, BigDecimal> basePriceMap) {
        assertNotNull(plan);
        assertNotEquals(Boolean.TRUE, plan.getIsCustomPrice());

        BigDecimal expectedHigherBase = basePriceMap.get(ElectricPricePeriodEnum.HIGHER.getCode());
        BigDecimal expectedHighBase = basePriceMap.get(ElectricPricePeriodEnum.HIGH.getCode());
        BigDecimal expectedLowBase = basePriceMap.get(ElectricPricePeriodEnum.LOW.getCode());
        BigDecimal expectedLowerBase = basePriceMap.get(ElectricPricePeriodEnum.LOWER.getCode());
        BigDecimal expectedDeepLowBase = basePriceMap.get(ElectricPricePeriodEnum.DEEP_LOW.getCode());

        assertEquals(0, plan.getPriceHigherBase().compareTo(expectedHigherBase));
        assertEquals(0, plan.getPriceHighBase().compareTo(expectedHighBase));
        assertEquals(0, plan.getPriceLowBase().compareTo(expectedLowBase));
        assertEquals(0, plan.getPriceLowerBase().compareTo(expectedLowerBase));
        assertEquals(0, plan.getPriceDeepLowBase().compareTo(expectedDeepLowBase));

        assertEquals(0, plan.getPriceHigherMultiply().multiply(expectedHigherBase).compareTo(plan.getPriceHigher()));
        assertEquals(0, plan.getPriceHighMultiply().multiply(expectedHighBase).compareTo(plan.getPriceHigh()));
        assertEquals(0, plan.getPriceLowMultiply().multiply(expectedLowBase).compareTo(plan.getPriceLow()));
        assertEquals(0, plan.getPriceLowerMultiply().multiply(expectedLowerBase).compareTo(plan.getPriceLower()));
        assertEquals(0, plan.getPriceDeepLowMultiply().multiply(expectedDeepLowBase).compareTo(plan.getPriceDeepLow()));
    }

    private Map<Integer, BigDecimal> getCurrentBasePriceMap() {
        Map<Integer, BigDecimal> map = new HashMap<>();
        for (ElectricPriceTypeDto electricPriceTypeDto : electricPricePlanService.getElectricPrice()) {
            if (map.put(electricPriceTypeDto.getType().getCode(), electricPriceTypeDto.getPrice()) != null) {
                throw new IllegalStateException("Duplicate key");
            }
        }
        return map;
    }

    private Map<Integer, BigDecimal> buildUpdatedBasePriceMap(Map<Integer, BigDecimal> originalBaseMap) {
        return originalBaseMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().add(new BigDecimal("0.11"))
                ));
    }
}
