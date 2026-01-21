package info.zhihui.ems.business.plan.utils;

import info.zhihui.ems.common.enums.ElectricPricePeriodEnum;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.business.plan.bo.StepPriceBo;
import info.zhihui.ems.business.plan.dto.ElectricPriceTimeDto;
import info.zhihui.ems.business.plan.dto.ElectricPriceTypeDto;
import info.zhihui.ems.business.plan.dto.StepDto;
import info.zhihui.ems.business.plan.repository.ElectricPricePlanRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ElectricPlanValidationUtil 单元测试
 *
 * @author jerryxiaosa
 */
@ExtendWith(MockitoExtension.class)
class ElectricPlanValidationUtilTest {

    @Mock
    private ElectricPricePlanRepository repository;

    @Test
    void testGetValidElectricPlanTime_Success() {
        // 准备测试数据
        ElectricPriceTimeDto time1 = new ElectricPriceTimeDto();
        time1.setStart(LocalTime.of(0, 0));
        time1.setType(ElectricPricePeriodEnum.LOW);

        ElectricPriceTimeDto time2 = new ElectricPriceTimeDto();
        time2.setStart(LocalTime.of(8, 0));
        time2.setType(ElectricPricePeriodEnum.HIGH);

        ElectricPriceTimeDto time3 = new ElectricPriceTimeDto();
        time3.setStart(LocalTime.of(18, 0));
        time3.setType(ElectricPricePeriodEnum.LOW);

        List<ElectricPriceTimeDto> boList = List.of(time1, time2, time3);

        // 执行测试 - 应该不抛出异常
        assertDoesNotThrow(() -> ElectricPlanValidationUtil.getValidElectricPlanTime(boList));
    }

    @Test
    void testGetValidElectricPlanTime_ContainsTotalType() {
        // 准备测试数据 - 包含 TOTAL 类型（总电价），应当禁止
        ElectricPriceTimeDto time1 = new ElectricPriceTimeDto();
        time1.setStart(LocalTime.of(0, 0));
        time1.setType(ElectricPricePeriodEnum.TOTAL);

        ElectricPriceTimeDto time2 = new ElectricPriceTimeDto();
        time2.setStart(LocalTime.of(8, 0));
        time2.setType(ElectricPricePeriodEnum.TOTAL);

        List<ElectricPriceTimeDto> boList = List.of(time1, time2);

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> ElectricPlanValidationUtil.getValidElectricPlanTime(boList));
        assertEquals("电价方案不能配置总电价", exception.getMessage());
    }

    @Test
    void testGetValidElectricPlanTime_TotalTypeForbidden() {
        // 准备测试数据 - TOTAL 类型被禁止
        ElectricPriceTimeDto time1 = new ElectricPriceTimeDto();
        time1.setStart(LocalTime.of(0, 0));
        time1.setType(ElectricPricePeriodEnum.TOTAL);

        ElectricPriceTimeDto time2 = new ElectricPriceTimeDto();
        time2.setStart(LocalTime.of(8, 0));
        time2.setType(ElectricPricePeriodEnum.TOTAL);

        List<ElectricPriceTimeDto> boList = List.of(time1, time2);

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> ElectricPlanValidationUtil.getValidElectricPlanTime(boList));
        assertEquals("电价方案不能配置总电价", exception.getMessage());
    }

    @Test
    void testGetValidElectricPlanTime_EmptyList() {
        // 准备测试数据 - 空列表
        List<ElectricPriceTimeDto> boList = Collections.emptyList();

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> ElectricPlanValidationUtil.getValidElectricPlanTime(boList));
        assertEquals("电价方案不能为空", exception.getMessage());
    }

    @Test
    void testGetValidElectricPlanTime_MixedContainsTotalType() {
        // 准备测试数据 - 同时存在合法类型与 TOTAL 类型
        ElectricPriceTimeDto time1 = new ElectricPriceTimeDto();
        time1.setStart(LocalTime.of(0, 0));
        time1.setType(ElectricPricePeriodEnum.LOW);

        ElectricPriceTimeDto time2 = new ElectricPriceTimeDto();
        time2.setStart(LocalTime.of(8, 0));
        time2.setType(ElectricPricePeriodEnum.TOTAL);

        List<ElectricPriceTimeDto> boList = List.of(time1, time2);

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> ElectricPlanValidationUtil.getValidElectricPlanTime(boList));
        assertEquals("电价方案不能配置总电价", exception.getMessage());
    }

    @Test
    void testGetValidElectricPlanTime_SkipNode() {
        // 准备测试数据 - 中间跳节点
        ElectricPriceTimeDto time1 = new ElectricPriceTimeDto();
        time1.setStart(LocalTime.of(0, 0));
        time1.setType(ElectricPricePeriodEnum.LOW);

        ElectricPriceTimeDto time2 = new ElectricPriceTimeDto();
        time2.setStart(LocalTime.of(8, 0));
        time2.setType(null);

        ElectricPriceTimeDto time3 = new ElectricPriceTimeDto();
        time3.setStart(LocalTime.of(18, 0));
        time3.setType(ElectricPricePeriodEnum.HIGH);

        List<ElectricPriceTimeDto> boList = List.of(time1, time2, time3);

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> ElectricPlanValidationUtil.getValidElectricPlanTime(boList));
        assertEquals("时间点配置不允许跳节点", exception.getMessage());
    }

    @Test
    void testGetValidElectricPlanTime_SameType() {
        // 准备测试数据 - 连续相同费率
        ElectricPriceTimeDto time1 = new ElectricPriceTimeDto();
        time1.setStart(LocalTime.of(0, 0));
        time1.setType(ElectricPricePeriodEnum.LOW);

        ElectricPriceTimeDto time2 = new ElectricPriceTimeDto();
        time2.setStart(LocalTime.of(8, 0));
        time2.setType(ElectricPricePeriodEnum.LOW);

        List<ElectricPriceTimeDto> boList = List.of(time1, time2);

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> ElectricPlanValidationUtil.getValidElectricPlanTime(boList));
        assertEquals("请勿连续配置相同费率", exception.getMessage());
    }

    @Test
    void testGetValidElectricPlanTime_SameTime() {
        // 准备测试数据 - 相同时间点
        ElectricPriceTimeDto time1 = new ElectricPriceTimeDto();
        time1.setStart(LocalTime.of(0, 0));
        time1.setType(ElectricPricePeriodEnum.LOW);

        ElectricPriceTimeDto time2 = new ElectricPriceTimeDto();
        time2.setStart(LocalTime.of(8, 0));
        time2.setType(ElectricPricePeriodEnum.HIGH);

        ElectricPriceTimeDto time3 = new ElectricPriceTimeDto();
        time3.setStart(LocalTime.of(8, 0));
        time3.setType(ElectricPricePeriodEnum.LOW);

        List<ElectricPriceTimeDto> boList = List.of(time1, time2, time3);

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> ElectricPlanValidationUtil.getValidElectricPlanTime(boList));
        assertEquals("请勿配置相同时间点", exception.getMessage());
    }

    @Test
    void testGetValidElectricPlanTime_TooManyTimeSegments() {
        // 准备测试数据 - 超过14个时间段
        List<ElectricPriceTimeDto> boList = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            ElectricPriceTimeDto time = new ElectricPriceTimeDto();
            time.setStart(LocalTime.of(i, 0));
            time.setType(i % 2 == 0 ? ElectricPricePeriodEnum.LOW : ElectricPricePeriodEnum.HIGH);
            boList.add(time);
        }

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> ElectricPlanValidationUtil.getValidElectricPlanTime(boList));
        assertEquals("最多支持14个时间段", exception.getMessage());
    }

    @Test
    void testCheckPriceType_Success() {
        // 准备测试数据 - 正确的5个电价类型
        ElectricPriceTypeDto type1 = new ElectricPriceTypeDto();
        type1.setType(ElectricPricePeriodEnum.HIGHER);
        type1.setPrice(new BigDecimal("1.0"));

        ElectricPriceTypeDto type2 = new ElectricPriceTypeDto();
        type2.setType(ElectricPricePeriodEnum.HIGH);
        type2.setPrice(new BigDecimal("0.8"));

        ElectricPriceTypeDto type3 = new ElectricPriceTypeDto();
        type3.setType(ElectricPricePeriodEnum.LOW);
        type3.setPrice(new BigDecimal("0.6"));

        ElectricPriceTypeDto type4 = new ElectricPriceTypeDto();
        type4.setType(ElectricPricePeriodEnum.LOWER);
        type4.setPrice(new BigDecimal("0.4"));

        ElectricPriceTypeDto type5 = new ElectricPriceTypeDto();
        type5.setType(ElectricPricePeriodEnum.DEEP_LOW);
        type5.setPrice(new BigDecimal("0.2"));

        List<ElectricPriceTypeDto> boList = List.of(type1, type2, type3, type4, type5);

        // 执行测试 - 应该不抛出异常
        assertDoesNotThrow(() -> ElectricPlanValidationUtil.checkPriceType(boList));
    }

    @Test
    void testCheckPriceType_WrongSize() {
        // 准备测试数据 - 不是5个电价类型
        ElectricPriceTypeDto type1 = new ElectricPriceTypeDto();
        type1.setType(ElectricPricePeriodEnum.HIGHER);
        type1.setPrice(new BigDecimal("1.0"));

        ElectricPriceTypeDto type2 = new ElectricPriceTypeDto();
        type2.setType(ElectricPricePeriodEnum.HIGH);
        type2.setPrice(new BigDecimal("0.8"));

        List<ElectricPriceTypeDto> boList = List.of(type1, type2);

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> ElectricPlanValidationUtil.checkPriceType(boList));
        assertEquals("请配置尖峰平谷深谷电价", exception.getMessage());
    }

    @Test
    void testCheckPriceType_NullType() {
        // 准备测试数据 - type为null
        ElectricPriceTypeDto type1 = new ElectricPriceTypeDto();
        type1.setType(null);
        type1.setPrice(new BigDecimal("1.0"));

        ElectricPriceTypeDto type2 = new ElectricPriceTypeDto();
        type2.setType(ElectricPricePeriodEnum.HIGH);
        type2.setPrice(new BigDecimal("0.8"));

        ElectricPriceTypeDto type3 = new ElectricPriceTypeDto();
        type3.setType(ElectricPricePeriodEnum.LOW);
        type3.setPrice(new BigDecimal("0.6"));

        ElectricPriceTypeDto type4 = new ElectricPriceTypeDto();
        type4.setType(ElectricPricePeriodEnum.LOWER);
        type4.setPrice(new BigDecimal("0.4"));

        ElectricPriceTypeDto type5 = new ElectricPriceTypeDto();
        type5.setType(ElectricPricePeriodEnum.DEEP_LOW);
        type5.setPrice(new BigDecimal("0.2"));

        List<ElectricPriceTypeDto> boList = List.of(type1, type2, type3, type4, type5);

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> ElectricPlanValidationUtil.checkPriceType(boList));
        assertEquals("尖峰平谷深谷电价配置不正确", exception.getMessage());
    }

    @Test
    void testCheckPriceType_WrongTypeValue() {
        // 准备测试数据 - type值不正确
        ElectricPriceTypeDto type1 = new ElectricPriceTypeDto();
        type1.setType(ElectricPricePeriodEnum.HIGHER);
        type1.setPrice(new BigDecimal("1.0"));

        ElectricPriceTypeDto type2 = new ElectricPriceTypeDto();
        type2.setType(ElectricPricePeriodEnum.LOW); // 应该是HIGH
        type2.setPrice(new BigDecimal("0.8"));

        ElectricPriceTypeDto type3 = new ElectricPriceTypeDto();
        type3.setType(ElectricPricePeriodEnum.LOW);
        type3.setPrice(new BigDecimal("0.6"));

        ElectricPriceTypeDto type4 = new ElectricPriceTypeDto();
        type4.setType(ElectricPricePeriodEnum.LOWER);
        type4.setPrice(new BigDecimal("0.4"));

        ElectricPriceTypeDto type5 = new ElectricPriceTypeDto();
        type5.setType(ElectricPricePeriodEnum.DEEP_LOW);
        type5.setPrice(new BigDecimal("0.2"));

        List<ElectricPriceTypeDto> boList = List.of(type1, type2, type3, type4, type5);

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> ElectricPlanValidationUtil.checkPriceType(boList));
        assertEquals("尖峰平谷深谷电价配置不正确", exception.getMessage());
    }

    @Test
    void testCheckStepPrice_Success() {
        // 准备测试数据
        StepPriceBo stepPrice1 = new StepPriceBo();
         stepPrice1.setStart(new BigDecimal("0"));
         stepPrice1.setEnd(new BigDecimal("100"));
         stepPrice1.setValue(new BigDecimal("0.5"));

         StepPriceBo stepPrice2 = new StepPriceBo();
         stepPrice2.setStart(new BigDecimal("100"));
         stepPrice2.setEnd(new BigDecimal("200"));
         stepPrice2.setValue(new BigDecimal("0.8"));

         StepPriceBo stepPrice3 = new StepPriceBo();
         stepPrice3.setStart(new BigDecimal("200"));
         stepPrice3.setEnd(null);
         stepPrice3.setValue(new BigDecimal("1.0"));

        List<StepPriceBo> boList = List.of(stepPrice1, stepPrice2, stepPrice3);

        // 执行测试 - 应该不抛出异常
        assertDoesNotThrow(() -> ElectricPlanValidationUtil.checkStepPrice(boList));
    }

    @Test
    void testCheckStepPrice_Empty() {
        // 准备测试数据 - 空列表
        List<StepPriceBo> boList = Collections.emptyList();

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> ElectricPlanValidationUtil.checkStepPrice(boList));
        assertEquals("至少配置一个阶梯", exception.getMessage());
    }

    @Test
    void testCheckStepPrice_TooMany() {
        // 准备测试数据 - 超过3个等级
        StepPriceBo stepPrice1 = new StepPriceBo();
        stepPrice1.setStart(new BigDecimal("0"));
        stepPrice1.setEnd(new BigDecimal("100"));

        StepPriceBo stepPrice2 = new StepPriceBo();
        stepPrice2.setStart(new BigDecimal("100"));
        stepPrice2.setEnd(new BigDecimal("200"));

        StepPriceBo stepPrice3 = new StepPriceBo();
        stepPrice3.setStart(new BigDecimal("200"));
        stepPrice3.setEnd(new BigDecimal("300"));

        StepPriceBo stepPrice4 = new StepPriceBo();
        stepPrice4.setStart(new BigDecimal("300"));
        stepPrice4.setEnd(null);

        List<StepPriceBo> boList = List.of(stepPrice1, stepPrice2, stepPrice3, stepPrice4);

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> ElectricPlanValidationUtil.checkStepPrice(boList));
        assertEquals("最多支持3个等级", exception.getMessage());
    }

    @Test
    void testCheckStepOrder_Success() {
        // 准备测试数据
        List<StepDto> stepDtoList = List.of(
                new StepDto().setStart(new BigDecimal("0")).setEnd(new BigDecimal("100")),
                new StepDto().setStart(new BigDecimal("100")).setEnd(new BigDecimal("200")),
                new StepDto().setStart(new BigDecimal("200")).setEnd(null)
        );

        // 执行测试 - 应该不抛出异常
        assertDoesNotThrow(() -> ElectricPlanValidationUtil.checkStepOrder(stepDtoList));
    }

    @Test
    void testCheckStepOrder_FirstStartNull() {
        // 准备测试数据 - 第一阶段开始值为空
        List<StepDto> stepDtoList = List.of(
                new StepDto().setStart(null).setEnd(new BigDecimal("100"))
        );

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> ElectricPlanValidationUtil.checkStepOrder(stepDtoList));
        assertEquals("第1阶段开始值不能为空", exception.getMessage());
    }

    @Test
    void testCheckStepOrder_FirstNotStartFromZero() {
        // 准备测试数据 - 第一阶段不从0开始
        List<StepDto> stepDtoList = List.of(
                new StepDto().setStart(new BigDecimal("10")).setEnd(new BigDecimal("100"))
        );

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> ElectricPlanValidationUtil.checkStepOrder(stepDtoList));
        assertEquals("第一阶段必须从0开始", exception.getMessage());
    }

    @Test
    void testCheckStepOrder_EndNull() {
        // 准备测试数据 - 上一阶段结束值为空
        List<StepDto> stepDtoList = List.of(
                new StepDto().setStart(new BigDecimal("0")).setEnd(null),
                new StepDto().setStart(new BigDecimal("100")).setEnd(null)
        );

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> ElectricPlanValidationUtil.checkStepOrder(stepDtoList));
        assertEquals("上一阶段结束值不能为空", exception.getMessage());
    }

    @Test
    void testCheckStepOrder_EndLessOrEqualStart() {
        // 准备测试数据 - 结束值小于等于开始值
        List<StepDto> stepDtoList = List.of(
                new StepDto().setStart(new BigDecimal("0")).setEnd(new BigDecimal("0")),
                new StepDto().setStart(new BigDecimal("0")).setEnd(null)
        );

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> ElectricPlanValidationUtil.checkStepOrder(stepDtoList));
        assertEquals("阶段结束值必须大于阶段开始值", exception.getMessage());
    }

    @Test
    void testCheckStepOrder_NextStartNotEqualPrevEnd() {
        // 准备测试数据 - 下一阶段开始值不等于上一阶段结束值
        List<StepDto> stepDtoList = List.of(
                new StepDto().setStart(new BigDecimal("0")).setEnd(new BigDecimal("100")),
                new StepDto().setStart(new BigDecimal("150")).setEnd(null)
        );

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> ElectricPlanValidationUtil.checkStepOrder(stepDtoList));
        assertEquals("阶段开始值必须和上一阶段结束值相同", exception.getMessage());
    }

    @Test
    void testCheckStepOrder_LastStepHasEnd() {
        // 准备测试数据 - 最后一个阶段有上限值
        List<StepDto> stepDtoList = List.of(
                new StepDto().setStart(new BigDecimal("0")).setEnd(new BigDecimal("100")),
                new StepDto().setStart(new BigDecimal("100")).setEnd(new BigDecimal("200"))
        );

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> ElectricPlanValidationUtil.checkStepOrder(stepDtoList));
        assertEquals("最后一个阶段不能有上限值", exception.getMessage());
    }
}
