package info.zhihui.ems.business.report.service.build.impl;

import info.zhihui.ems.business.report.entity.DailyMeterReportEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import info.zhihui.ems.common.enums.ElectricPricePeriodEnum;

@DisplayName("日报构建通用工具测试")
class ReportBuildSupportTest {

    @Test
    @DisplayName("空值兜底方法应返回默认结果")
    void testDefaultHelper_NullInput_ShouldReturnDefaultValue() {
        assertEquals(0, BigDecimal.ZERO.compareTo(ReportBuildSupport.defaultDecimal(null)));
        assertTrue(ReportBuildSupport.defaultList(null).isEmpty());
        assertEquals("fallback", ReportBuildSupport.defaultValue(null, "fallback"));
    }

    @Test
    @DisplayName("首尾读取方法应返回列表两端元素")
    void testFirstOrLastOrNull_NonEmptyList_ShouldReturnBoundaryItem() {
        List<String> itemList = List.of("first", "middle", "last");

        assertEquals("first", ReportBuildSupport.firstOrNull(itemList));
        assertEquals("last", ReportBuildSupport.lastOrNull(itemList));
    }

    @Test
    @DisplayName("按键索引应以后值覆盖前值")
    void testIndexByKey_DuplicateKey_ShouldKeepLastItem() {
        TestItem firstItem = new TestItem(10, "first");
        TestItem lastItem = new TestItem(10, "last");

        Map<Integer, TestItem> itemMap = ReportBuildSupport.indexByKey(List.of(firstItem, lastItem), TestItem::id);

        assertEquals(1, itemMap.size());
        assertSame(lastItem, itemMap.get(10));
    }

    @Test
    @DisplayName("按键分组应保留同键全部元素")
    void testGroupByKey_DuplicateKey_ShouldKeepAllItems() {
        TestItem firstItem = new TestItem(10, "first");
        TestItem secondItem = new TestItem(10, "second");

        Map<Integer, List<TestItem>> itemMap = ReportBuildSupport.groupByKey(List.of(firstItem, secondItem), TestItem::id);

        assertEquals(1, itemMap.size());
        assertEquals(List.of(firstItem, secondItem), itemMap.get(10));
    }

    @Test
    @DisplayName("带过滤条件的索引和分组应跳过不合法元素")
    void testIndexOrGroupByKey_WithFilter_ShouldSkipInvalidItem() {
        TestItem validItem = new TestItem(10, "valid");
        TestItem invalidItem = new TestItem(null, "invalid");

        Map<Integer, TestItem> indexMap = ReportBuildSupport.indexByKey(
                List.of(validItem, invalidItem),
                TestItem::id,
                Objects::nonNull
        );
        Map<Integer, List<TestItem>> groupMap = ReportBuildSupport.groupByKey(
                List.of(validItem, invalidItem),
                TestItem::id,
                Objects::nonNull
        );

        assertEquals(1, indexMap.size());
        assertSame(validItem, indexMap.get(10));
        assertEquals(1, groupMap.size());
        assertEquals(List.of(validItem), groupMap.get(10));
    }

    @Test
    @DisplayName("排序拷贝应返回新列表且按主次键升序排列")
    void testSortedCopy_WithTwoSortKeys_ShouldReturnSortedNewList() {
        SortItem lateItem = new SortItem(LocalDateTime.of(2026, 4, 8, 10, 0), 2, "late");
        SortItem earlySecondItem = new SortItem(LocalDateTime.of(2026, 4, 8, 9, 0), 2, "early-second");
        SortItem earlyFirstItem = new SortItem(LocalDateTime.of(2026, 4, 8, 9, 0), 1, "early-first");
        List<SortItem> originalList = List.of(lateItem, earlySecondItem, earlyFirstItem);

        List<SortItem> sortedItemList = ReportBuildSupport.sortedCopy(
                originalList,
                SortItem::eventTime,
                SortItem::id
        );

        assertEquals(List.of(earlyFirstItem, earlySecondItem, lateItem), sortedItemList);
        assertEquals(List.of(lateItem, earlySecondItem, earlyFirstItem), originalList);
    }

    @Test
    @DisplayName("按时段读取金额应支持空值兜底")
    void testGetDecimalByPricePeriod_NullValue_ShouldReturnZero() {
        PeriodItem periodItem = new PeriodItem(
                new BigDecimal("10.00"),
                null,
                new BigDecimal("8.00"),
                new BigDecimal("6.00"),
                new BigDecimal("4.00"),
                new BigDecimal("2.00")
        );

        BigDecimal amount = ReportBuildSupport.getDecimalByPricePeriod(
                periodItem,
                ElectricPricePeriodEnum.HIGHER,
                PeriodItem::total,
                PeriodItem::higher,
                PeriodItem::high,
                PeriodItem::low,
                PeriodItem::lower,
                PeriodItem::deepLow
        );

        assertEquals(0, BigDecimal.ZERO.compareTo(amount));
    }

    @Test
    @DisplayName("按时段汇总金额应累加对应字段")
    void testSumByPricePeriod_MultipleItems_ShouldSumMatchedField() {
        List<PeriodItem> itemList = List.of(
                new PeriodItem(new BigDecimal("10.00"), new BigDecimal("5.00"), new BigDecimal("4.00"),
                        new BigDecimal("3.00"), new BigDecimal("2.00"), new BigDecimal("1.00")),
                new PeriodItem(new BigDecimal("20.00"), new BigDecimal("7.00"), new BigDecimal("6.00"),
                        new BigDecimal("5.00"), new BigDecimal("4.00"), new BigDecimal("3.00"))
        );

        BigDecimal amount = ReportBuildSupport.sumByPricePeriod(
                itemList,
                ElectricPricePeriodEnum.HIGHER,
                PeriodItem::total,
                PeriodItem::higher,
                PeriodItem::high,
                PeriodItem::low,
                PeriodItem::lower,
                PeriodItem::deepLow
        );

        assertEquals(0, new BigDecimal("12.00").compareTo(amount));
    }

    @Test
    @DisplayName("电量写入方法应分别写入日报的期初期末读数")
    void testApplyPowerValues_ShouldWriteBeginAndEndPowerFields() {
        DailyMeterReportEntity reportEntity = new DailyMeterReportEntity();

        ReportBuildSupport.applyBeginPowerValues(reportEntity,
                new BigDecimal("10.00"), new BigDecimal("9.00"), new BigDecimal("8.00"),
                new BigDecimal("7.00"), new BigDecimal("6.00"), new BigDecimal("5.00"));
        ReportBuildSupport.applyEndPowerValues(reportEntity,
                new BigDecimal("20.00"), new BigDecimal("19.00"), new BigDecimal("18.00"),
                new BigDecimal("17.00"), new BigDecimal("16.00"), new BigDecimal("15.00"));

        assertEquals(0, new BigDecimal("10.00").compareTo(reportEntity.getBeginPower()));
        assertEquals(0, new BigDecimal("9.00").compareTo(reportEntity.getBeginPowerHigher()));
        assertEquals(0, new BigDecimal("8.00").compareTo(reportEntity.getBeginPowerHigh()));
        assertEquals(0, new BigDecimal("7.00").compareTo(reportEntity.getBeginPowerLow()));
        assertEquals(0, new BigDecimal("6.00").compareTo(reportEntity.getBeginPowerLower()));
        assertEquals(0, new BigDecimal("5.00").compareTo(reportEntity.getBeginPowerDeepLow()));
        assertEquals(0, new BigDecimal("20.00").compareTo(reportEntity.getEndPower()));
        assertEquals(0, new BigDecimal("19.00").compareTo(reportEntity.getEndPowerHigher()));
        assertEquals(0, new BigDecimal("18.00").compareTo(reportEntity.getEndPowerHigh()));
        assertEquals(0, new BigDecimal("17.00").compareTo(reportEntity.getEndPowerLow()));
        assertEquals(0, new BigDecimal("16.00").compareTo(reportEntity.getEndPowerLower()));
        assertEquals(0, new BigDecimal("15.00").compareTo(reportEntity.getEndPowerDeepLow()));
    }

    private record TestItem(Integer id, String name) {
    }

    private record SortItem(LocalDateTime eventTime, Integer id, String name) {
    }

    private record PeriodItem(BigDecimal total,
                              BigDecimal higher,
                              BigDecimal high,
                              BigDecimal low,
                              BigDecimal lower,
                              BigDecimal deepLow) {
    }
}
