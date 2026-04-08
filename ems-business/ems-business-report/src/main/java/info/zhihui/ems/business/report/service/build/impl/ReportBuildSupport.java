package info.zhihui.ems.business.report.service.build.impl;

import info.zhihui.ems.business.report.entity.DailyMeterReportEntity;
import info.zhihui.ems.common.enums.ElectricPricePeriodEnum;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 日报构建过程中复用的通用辅助方法集合。
 * <p>
 * 该类只服务于 report 模块内部的 builder 实现，统一承接空值兜底、
 * 列表排序、按 key 建索引/分组、按电价时段取值汇总，以及日电表读数写入等重复逻辑。
 */
final class ReportBuildSupport {

    private ReportBuildSupport() {
    }

    /**
     * 将空金额统一折算为零值，避免累加时出现空指针。
     */
    static BigDecimal defaultDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    /**
     * 当前值为空时回退到默认值。
     */
    static <T> T defaultValue(T currentValue, T fallbackValue) {
        return currentValue != null ? currentValue : fallbackValue;
    }

    /**
     * 将空列表统一视为空集合，减少调用侧空判断。
     */
    static <T> List<T> defaultList(List<T> valueList) {
        return valueList == null ? Collections.emptyList() : valueList;
    }

    /**
     * 拷贝列表后按主次键排序，避免直接修改原始输入集合。
     */
    static <T, U extends Comparable<? super U>, V extends Comparable<? super V>> List<T> sortedCopy(List<T> valueList,
                                                                                                     Function<T, U> primaryGetter,
                                                                                                     Function<T, V> secondaryGetter) {
        List<T> sortedValueList = new ArrayList<>(defaultList(valueList));
        sortedValueList.sort(Comparator
                .comparing(primaryGetter, Comparator.nullsLast(U::compareTo))
                .thenComparing(secondaryGetter, Comparator.nullsLast(V::compareTo)));
        return sortedValueList;
    }

    /**
     * 读取列表首个元素，空列表时返回 null。
     */
    static <T> T firstOrNull(List<T> valueList) {
        List<T> safeValueList = defaultList(valueList);
        return safeValueList.isEmpty() ? null : safeValueList.get(0);
    }

    /**
     * 读取列表最后一个元素，空列表时返回 null。
     */
    static <T> T lastOrNull(List<T> valueList) {
        List<T> safeValueList = defaultList(valueList);
        return safeValueList.isEmpty() ? null : safeValueList.get(safeValueList.size() - 1);
    }

    /**
     * 按电价时段读取对象中的金额或电量字段，并对空值做零值兜底。
     */
    static <T> BigDecimal getDecimalByPricePeriod(T source,
                                                  ElectricPricePeriodEnum pricePeriod,
                                                  Function<T, BigDecimal> totalGetter,
                                                  Function<T, BigDecimal> higherGetter,
                                                  Function<T, BigDecimal> highGetter,
                                                  Function<T, BigDecimal> lowGetter,
                                                  Function<T, BigDecimal> lowerGetter,
                                                  Function<T, BigDecimal> deepLowGetter) {
        BigDecimal value = switch (pricePeriod) {
            case TOTAL -> totalGetter.apply(source);
            case HIGHER -> higherGetter.apply(source);
            case HIGH -> highGetter.apply(source);
            case LOW -> lowGetter.apply(source);
            case LOWER -> lowerGetter.apply(source);
            case DEEP_LOW -> deepLowGetter.apply(source);
        };
        return defaultDecimal(value);
    }

    /**
     * 按电价时段汇总列表中的金额或电量字段。
     */
    static <T> BigDecimal sumByPricePeriod(List<T> itemList,
                                           ElectricPricePeriodEnum pricePeriod,
                                           Function<T, BigDecimal> totalGetter,
                                           Function<T, BigDecimal> higherGetter,
                                           Function<T, BigDecimal> highGetter,
                                           Function<T, BigDecimal> lowGetter,
                                           Function<T, BigDecimal> lowerGetter,
                                           Function<T, BigDecimal> deepLowGetter) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (T item : defaultList(itemList)) {
            totalAmount = totalAmount.add(getDecimalByPricePeriod(item, pricePeriod,
                    totalGetter, higherGetter, highGetter, lowGetter, lowerGetter, deepLowGetter));
        }
        return totalAmount;
    }

    /**
     * 将六个分时段期初读数写入日电表日报实体。
     */
    static void applyBeginPowerValues(DailyMeterReportEntity reportEntity,
                                      BigDecimal total,
                                      BigDecimal higher,
                                      BigDecimal high,
                                      BigDecimal low,
                                      BigDecimal lower,
                                      BigDecimal deepLow) {
        reportEntity.setBeginPower(total);
        reportEntity.setBeginPowerHigher(higher);
        reportEntity.setBeginPowerHigh(high);
        reportEntity.setBeginPowerLow(low);
        reportEntity.setBeginPowerLower(lower);
        reportEntity.setBeginPowerDeepLow(deepLow);
    }

    /**
     * 将六个分时段期末读数写入日电表日报实体。
     */
    static void applyEndPowerValues(DailyMeterReportEntity reportEntity,
                                    BigDecimal total,
                                    BigDecimal higher,
                                    BigDecimal high,
                                    BigDecimal low,
                                    BigDecimal lower,
                                    BigDecimal deepLow) {
        reportEntity.setEndPower(total);
        reportEntity.setEndPowerHigher(higher);
        reportEntity.setEndPowerHigh(high);
        reportEntity.setEndPowerLow(low);
        reportEntity.setEndPowerLower(lower);
        reportEntity.setEndPowerDeepLow(deepLow);
    }

    /**
     * 按 key 建立单值索引，后出现的元素覆盖先出现的元素。
     */
    static <T, K> Map<K, T> indexByKey(List<T> itemList, Function<T, K> keyGetter) {
        return indexByKey(itemList, keyGetter, key -> true);
    }

    /**
     * 按 key 建立单值索引，并通过过滤条件跳过无效 key。
     */
    static <T, K> Map<K, T> indexByKey(List<T> itemList,
                                       Function<T, K> keyGetter,
                                       Predicate<K> keyFilter) {
        Map<K, T> itemMap = new HashMap<>();
        for (T item : defaultList(itemList)) {
            if (item == null) {
                continue;
            }
            K key = keyGetter.apply(item);
            if (!keyFilter.test(key)) {
                continue;
            }
            itemMap.put(key, item);
        }
        return itemMap;
    }

    /**
     * 按 key 对列表元素分组。
     */
    static <T, K> Map<K, List<T>> groupByKey(List<T> itemList, Function<T, K> keyGetter) {
        return groupByKey(itemList, keyGetter, key -> true);
    }

    /**
     * 按 key 对列表元素分组，并通过过滤条件跳过无效 key。
     */
    static <T, K> Map<K, List<T>> groupByKey(List<T> itemList,
                                             Function<T, K> keyGetter,
                                             Predicate<K> keyFilter) {
        Map<K, List<T>> itemMap = new HashMap<>();
        for (T item : defaultList(itemList)) {
            if (item == null) {
                continue;
            }
            K key = keyGetter.apply(item);
            if (!keyFilter.test(key)) {
                continue;
            }
            itemMap.computeIfAbsent(key, currentKey -> new ArrayList<>()).add(item);
        }
        return itemMap;
    }

}
