package info.zhihui.ems.business.plan.utils;

import info.zhihui.ems.common.enums.ElectricPricePeriodEnum;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.business.plan.dto.ElectricPriceTimeDto;
import info.zhihui.ems.business.plan.dto.ElectricPriceTypeDto;
import info.zhihui.ems.business.plan.bo.StepPriceBo;
import info.zhihui.ems.business.plan.dto.StepDto;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 电价方案校验工具类
 *
 * @author jerryxiaosa
 */
public class ElectricPlanValidationUtil {

    /**
     * 最大时间段数量
     */
    private static final int MAX_TIME_SEGMENTS = 14;

    /**
     * 电价类型数量
     */
    private static final int ELECTRIC_TYPE_COUNT = 5;

    /**
     * 最大阶梯等级数量
     */
    private static final int MAX_STEP_LEVELS = 3;

    /**
     * 一天开始时间
     */
    private static final LocalTime DAY_START_TIME = LocalTime.of(0, 0);

    /**
     * 校验时间段配置
     *
     * @param dtoList 时间段配置
     *                type为null的数据表示没有配置
     *                TOTAL类型不能配置
     *                允许末尾不配置，中间需要配置
     */
    public static List<ElectricPriceTimeDto> getValidElectricPlanTime(List<ElectricPriceTimeDto> dtoList) {
        validateTimeTypeConstraints(dtoList);

        List<ElectricPriceTimeDto> configTimeList = extractConfiguredTimeList(dtoList);
        validateNoGapsInConfiguration(configTimeList);

        configTimeList = sortAndNormalizeTimeList(configTimeList);
        validateTimeSequence(configTimeList);
        validateMaxTimeSegments(configTimeList);

        return configTimeList;
    }

    /**
     * 校验时间段类型约束：
     * - 非空校验：时间段列表不能为空；
     * - 禁止 TOTAL：不允许配置总电价类型；
     * - 至少一个有效时间点：不能全部为未配置（type 为 null）。
     */
    private static void validateTimeTypeConstraints(List<ElectricPriceTimeDto> dtoList) {
        if (CollectionUtils.isEmpty(dtoList)) {
            throw new BusinessRuntimeException("电价方案不能为空");
        }

        validateTimeStartNotNull(dtoList);

        if (dtoList.stream().anyMatch(b -> ElectricPricePeriodEnum.TOTAL.equals(b.getType()))) {
            throw new BusinessRuntimeException("电价方案不能配置总电价");
        }

        if (dtoList.stream().allMatch(b -> b.getType() == null)) {
            throw new BusinessRuntimeException("至少配置一个时间点");
        }
    }

    /**
     * 校验时间点不能为空
     */
    private static void validateTimeStartNotNull(List<ElectricPriceTimeDto> dtoList) {
        for (int i = 0; i < dtoList.size(); i++) {
            ElectricPriceTimeDto dto = dtoList.get(i);
            if (dto == null || dto.getStart() == null) {
                throw new BusinessRuntimeException("第" + (i + 1) + "个时间点不能为空");
            }
        }
    }

    /**
     * 提取已配置的时间列表，去掉中间无效的配置
     */
    private static List<ElectricPriceTimeDto> extractConfiguredTimeList(List<ElectricPriceTimeDto> dtoList) {
        List<ElectricPriceTimeDto> configTimeList;
        if (dtoList.stream().anyMatch(b -> b.getType() == null)) {
            configTimeList = findLastConfiguredIndex(dtoList);
        } else {
            configTimeList = new ArrayList<>(dtoList);
        }

        if (configTimeList.isEmpty()) {
            throw new BusinessRuntimeException("至少配置一个时间点");
        }
        return configTimeList;
    }

    /**
     * 查找最后一个配置的索引并返回子列表
     */
    private static List<ElectricPriceTimeDto> findLastConfiguredIndex(List<ElectricPriceTimeDto> dtoList) {
        for (int i = dtoList.size() - 1; i >= 0; i--) {
            ElectricPriceTimeDto dto = dtoList.get(i);
            if (dto.getType() != null) {
                return new ArrayList<>(dtoList.subList(0, i + 1));
            }
        }
        return new ArrayList<>();
    }

    /**
     * 校验配置中没有跳跃节点
     */
    private static void validateNoGapsInConfiguration(List<ElectricPriceTimeDto> configTimeList) {
        if (configTimeList.stream().anyMatch(b -> b.getType() == null)) {
            throw new BusinessRuntimeException("时间点配置不允许跳节点");
        }
    }

    /**
     * 排序并标准化时间列表
     */
    private static List<ElectricPriceTimeDto> sortAndNormalizeTimeList(List<ElectricPriceTimeDto> configTimeList) {
        List<ElectricPriceTimeDto> sortedList = new ArrayList<>(configTimeList);
        sortedList.sort(Comparator.comparing(ElectricPriceTimeDto::getStart));

        // 如果第一个时间点不是0点，则添加0点作为起始点
        if (!sortedList.get(0).getStart().equals(DAY_START_TIME)) {
            ElectricPriceTimeDto dayStartTime = new ElectricPriceTimeDto()
                    .setStart(DAY_START_TIME)
                    .setType(ElectricPricePeriodEnum.LOW);
            sortedList.add(0, dayStartTime);

            // 如果添加的0点和第二个时间点类型相同，则移除第二个
            if (sortedList.size() > 1 && ElectricPricePeriodEnum.LOW.equals(sortedList.get(1).getType())) {
                sortedList.remove(1);
            }
        }
        return sortedList;
    }

    /**
     * 校验时间序列的有效性
     */
    private static void validateTimeSequence(List<ElectricPriceTimeDto> configTimeList) {
        for (int i = 0; i < configTimeList.size() - 1; i++) {
            ElectricPriceTimeDto current = configTimeList.get(i);
            ElectricPriceTimeDto next = configTimeList.get(i + 1);

            if (Objects.equals(current.getType(), next.getType())) {
                throw new BusinessRuntimeException("请勿连续配置相同费率");
            }
            if (current.getStart().equals(next.getStart())) {
                throw new BusinessRuntimeException("请勿配置相同时间点");
            }
        }
    }

    /**
     * 校验最大时间段数量
     */
    private static void validateMaxTimeSegments(List<ElectricPriceTimeDto> configTimeList) {
        if (configTimeList.size() > MAX_TIME_SEGMENTS) {
            throw new BusinessRuntimeException("最多支持" + MAX_TIME_SEGMENTS + "个时间段");
        }
    }

    /**
     * 校验电价类型配置
     *
     * @param dtoList 电价类型配置列表
     */
    public static void checkPriceType(List<ElectricPriceTypeDto> dtoList) {
        validateElectricTypeCount(dtoList);
        List<ElectricPriceTypeDto> sortedList = new ArrayList<>(dtoList);
        sortedList.sort(Comparator.comparing(
                type -> type == null || type.getType() == null ? null : type.getType().getCode(),
                Comparator.nullsFirst(Integer::compareTo)));

        validateElectricTypeConfiguration(sortedList);
    }

    /**
     * 校验电价类型数量
     */
    private static void validateElectricTypeCount(List<ElectricPriceTypeDto> dtoList) {
        if (dtoList.size() != ELECTRIC_TYPE_COUNT) {
            throw new BusinessRuntimeException("请配置尖峰平谷深谷电价");
        }
    }

    /**
     * 校验电价类型配置的正确性
     */
    private static void validateElectricTypeConfiguration(List<ElectricPriceTypeDto> dtoList) {
        for (int i = 0; i < dtoList.size(); i++) {
            ElectricPriceTypeDto type = dtoList.get(i);
            int expectedType = i + 1; // 尖峰平谷深谷是1-5

            if (isInvalidElectricType(type, expectedType)) {
                throw new BusinessRuntimeException("尖峰平谷深谷电价配置不正确");
            }
        }
    }

    /**
     * 判断电价类型是否无效
     */
    private static boolean isInvalidElectricType(ElectricPriceTypeDto type, int expectedType) {
        return type == null
                || type.getPrice() == null
                || type.getType() == null
                || !Objects.equals(type.getType().getCode(), expectedType);
    }


    /**
     * 校验阶梯价格配置
     *
     * @param boList 阶梯价格配置列表
     */
    public static void checkStepPrice(List<StepPriceBo> boList) {
        validateStepPriceCount(boList);
        validateStepPriceValues(boList);

        List<StepDto> stepDtoList = convertToStepDtoList(boList);
        checkStepOrder(stepDtoList);
    }

    /**
     * 校验阶梯价格数量
     */
    private static void validateStepPriceCount(List<StepPriceBo> boList) {
        if (CollectionUtils.isEmpty(boList)) {
            throw new BusinessRuntimeException("至少配置一个阶梯");
        }
        if (boList.size() > MAX_STEP_LEVELS) {
            throw new BusinessRuntimeException("最多支持" + MAX_STEP_LEVELS + "个等级");
        }
    }

    /**
     * 校验阶梯价格值
     */
    private static void validateStepPriceValues(List<StepPriceBo> boList) {
        for (int i = 0; i < boList.size(); i++) {
            StepPriceBo step = boList.get(i);
            if (step.getStart() == null) {
                throw new BusinessRuntimeException("第" + (i + 1) + "阶段开始值不能为空");
            }
            if (step.getValue() == null) {
                throw new BusinessRuntimeException("第" + (i + 1) + "阶段价格值不能为空");
            }
            if (step.getValue().compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessRuntimeException("第" + (i + 1) + "阶段价格值不能为负数");
            }
        }
    }

    /**
     * 转换为StepDto列表
     */
    private static List<StepDto> convertToStepDtoList(List<StepPriceBo> boList) {
        return boList.stream()
                .map(b -> new StepDto().setStart(b.getStart()).setEnd(b.getEnd()))
                .collect(Collectors.toList());
    }

    /**
     * 校验阶梯区间顺序
     *
     * @param stepDtoList 阶梯配置列表
     */
    public static void checkStepOrder(List<StepDto> stepDtoList) {
        for (int i = 0; i < stepDtoList.size(); i++) {
            StepDto currentStep = stepDtoList.get(i);
            validateStepStartValue(currentStep, i);

            if (isNotLastStep(i, stepDtoList.size())) {
                validateIntermediateStep(currentStep, stepDtoList.get(i + 1));
            } else {
                validateLastStep(currentStep);
            }
        }
    }

    /**
     * 校验阶梯开始值
     */
    private static void validateStepStartValue(StepDto step, int index) {
        if (step.getStart() == null) {
            throw new BusinessRuntimeException("第" + (index + 1) + "阶段开始值不能为空");
        }
        if (index == 0 && step.getStart().compareTo(BigDecimal.ZERO) != 0) {
            throw new BusinessRuntimeException("第一阶段必须从0开始");
        }
    }

    /**
     * 判断是否不是最后一个阶梯
     */
    private static boolean isNotLastStep(int index, int totalSize) {
        return index < totalSize - 1;
    }

    /**
     * 校验中间阶梯
     */
    private static void validateIntermediateStep(StepDto currentStep, StepDto nextStep) {
        if (currentStep.getEnd() == null) {
            throw new BusinessRuntimeException("上一阶段结束值不能为空");
        }
        if (currentStep.getEnd().compareTo(currentStep.getStart()) <= 0) {
            throw new BusinessRuntimeException("阶段结束值必须大于阶段开始值");
        }
        if (nextStep.getStart() == null || nextStep.getStart().compareTo(currentStep.getEnd()) != 0) {
            throw new BusinessRuntimeException("阶段开始值必须和上一阶段结束值相同");
        }
    }

    /**
     * 校验最后一个阶梯
     */
    private static void validateLastStep(StepDto step) {
        if (step.getEnd() != null) {
            throw new BusinessRuntimeException("最后一个阶段不能有上限值");
        }
    }
}
