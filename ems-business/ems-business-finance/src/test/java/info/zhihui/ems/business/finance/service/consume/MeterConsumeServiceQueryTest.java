package info.zhihui.ems.business.finance.service.consume;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.page.PageMethod;
import info.zhihui.ems.business.finance.dto.PowerConsumeQueryDto;
import info.zhihui.ems.business.finance.dto.PowerConsumeRecordDto;
import info.zhihui.ems.business.finance.entity.ElectricMeterBalanceConsumeRecordEntity;
import info.zhihui.ems.business.finance.repository.ElectricMeterBalanceConsumeRecordRepository;
import info.zhihui.ems.business.finance.service.consume.impl.MeterConsumeServiceImpl;
import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 电量消费记录查询功能单元测试
 *
 * @author jerryxiaosa
 */
@ExtendWith(MockitoExtension.class)
class MeterConsumeServiceQueryTest {

    @Mock
    private ElectricMeterBalanceConsumeRecordRepository electricMeterBalanceConsumeRecordRepository;

    @InjectMocks
    private MeterConsumeServiceImpl meterConsumeService;

    private List<ElectricMeterBalanceConsumeRecordEntity> mockEntities;

    @BeforeEach
    void setUp() {
        mockEntities = List.of(
                createMockEntity(1, "电表001", "M001", "房间001", new BigDecimal("100.00"),
                        new BigDecimal("10.50"), new BigDecimal("89.50"), ElectricAccountTypeEnum.QUANTITY.getCode(),
                        LocalDateTime.of(2024, 1, 15, 10, 30)),
                createMockEntity(2, "电表002", "M002", "房间002", new BigDecimal("200.00"),
                        new BigDecimal("15.75"), new BigDecimal("184.25"), ElectricAccountTypeEnum.MERGED.getCode(),
                        LocalDateTime.of(2024, 1, 14, 14, 20))
        );
    }

    @Test
    void testQueryPowerConsumes_WithAllConditions() {
        // Given
        PowerConsumeQueryDto queryDto = new PowerConsumeQueryDto()
                .setMeterName("电表")
                .setSpaceName("房间")
                .setBeginTime(LocalDateTime.of(2024, 1, 1, 0, 0))
                .setEndTime(LocalDateTime.of(2024, 1, 31, 23, 59));

        PageParam pageParam = new PageParam()
                .setPageNum(1)
                .setPageSize(10);

        // Mock PageHelper behavior
        try (MockedStatic<PageMethod> pageHelper = Mockito.mockStatic(PageMethod.class);
             Page<ElectricMeterBalanceConsumeRecordEntity> mockPage = mockPage()) {
            PageInfo<ElectricMeterBalanceConsumeRecordEntity> pageInfo = new PageInfo<>();
            pageInfo.setList(mockEntities);
            pageInfo.setTotal(2);

            pageHelper.when(() -> PageMethod.startPage(pageParam.getPageNum(), pageParam.getPageSize())).thenReturn(mockPage);
            when(mockPage.<ElectricMeterBalanceConsumeRecordEntity>doSelectPageInfo(any())).thenReturn(pageInfo);

            // When
            PageResult<PowerConsumeRecordDto> result = meterConsumeService.findPowerConsumePage(queryDto, pageParam);

            // Then
            assertNotNull(result);
            assertEquals(1, result.getPageNum());
            assertEquals(10, result.getPageSize());
            assertEquals(2L, result.getTotal());
            assertEquals(2, result.getList().size());

            // 验证第一条记录
            PowerConsumeRecordDto firstRecord = result.getList().get(0);
            assertEquals("电表001", firstRecord.getMeterName());
            assertEquals("M001", firstRecord.getMeterNo());
            assertEquals("房间001", firstRecord.getSpaceName());
            assertEquals(new BigDecimal("100.00"), firstRecord.getBeginBalance());
            assertEquals(new BigDecimal("10.50"), firstRecord.getConsumeAmount());
            assertEquals(new BigDecimal("89.50"), firstRecord.getEndBalance());
            assertFalse(firstRecord.getMergedMeasure()); // QUANTITY类型，非合并计量
            assertEquals(LocalDateTime.of(2024, 1, 15, 10, 30), firstRecord.getConsumeTime());

            // 验证第二条记录
            PowerConsumeRecordDto secondRecord = result.getList().get(1);
            assertEquals("电表002", secondRecord.getMeterName());
            assertEquals("M002", secondRecord.getMeterNo());
            assertEquals("房间002", secondRecord.getSpaceName());
            assertEquals(new BigDecimal("200.00"), secondRecord.getBeginBalance());
            assertEquals(new BigDecimal("15.75"), secondRecord.getConsumeAmount());
            assertEquals(new BigDecimal("184.25"), secondRecord.getEndBalance());
            assertTrue(secondRecord.getMergedMeasure()); // MERGED类型，合并计量
            assertEquals(LocalDateTime.of(2024, 1, 14, 14, 20), secondRecord.getConsumeTime());
        }
    }

    @Test
    void testQueryPowerConsumes_WithPagination() {
        // Given
        PowerConsumeQueryDto queryDto = new PowerConsumeQueryDto();
        PageParam pageParam = new PageParam()
                .setPageNum(1)
                .setPageSize(1);

        // Mock PageHelper behavior
        try (MockedStatic<PageMethod> pageHelper = Mockito.mockStatic(PageMethod.class);
             Page<ElectricMeterBalanceConsumeRecordEntity> mockPage = mockPage()) {
            PageInfo<ElectricMeterBalanceConsumeRecordEntity> pageInfo = new PageInfo<>();
            pageInfo.setList(List.of(mockEntities.get(0))); // 只返回第一条记录
            pageInfo.setTotal(2);

            pageHelper.when(() -> PageMethod.startPage(pageParam.getPageNum(), pageParam.getPageSize())).thenReturn(mockPage);
            when(mockPage.<ElectricMeterBalanceConsumeRecordEntity>doSelectPageInfo(any())).thenReturn(pageInfo);

            // When
            PageResult<PowerConsumeRecordDto> result = meterConsumeService.findPowerConsumePage(queryDto, pageParam);

            // Then
            assertNotNull(result);
            assertEquals(1, result.getPageNum());
            assertEquals(1, result.getPageSize());
            assertEquals(2L, result.getTotal());
            assertEquals(1, result.getList().size()); // 只返回第一页的一条记录
        }
    }

    @Test
    void testQueryPowerConsumes_EmptyResult() {
        // Given
        PowerConsumeQueryDto queryDto = new PowerConsumeQueryDto()
                .setMeterName("不存在的电表");
        PageParam pageParam = new PageParam()
                .setPageNum(1)
                .setPageSize(10);

        // Mock PageHelper behavior
        try (MockedStatic<PageMethod> pageHelper = Mockito.mockStatic(PageMethod.class);
             Page<ElectricMeterBalanceConsumeRecordEntity> mockPage = mockPage()) {
            PageInfo<ElectricMeterBalanceConsumeRecordEntity> pageInfo = new PageInfo<>();
            pageInfo.setList(List.of());
            pageInfo.setTotal(0);

            pageHelper.when(() -> PageMethod.startPage(pageParam.getPageNum(), pageParam.getPageSize())).thenReturn(mockPage);
            when(mockPage.<ElectricMeterBalanceConsumeRecordEntity>doSelectPageInfo(any())).thenReturn(pageInfo);

            // When
            PageResult<PowerConsumeRecordDto> result = meterConsumeService.findPowerConsumePage(queryDto, pageParam);

            // Then
            assertNotNull(result);
            assertEquals(1, result.getPageNum());
            assertEquals(10, result.getPageSize());
            assertEquals(0L, result.getTotal());
            assertTrue(result.getList().isEmpty());
        }
    }

    @Test
    void testQueryPowerConsumes_OnlyMeterNameCondition() {
        // Given
        PowerConsumeQueryDto queryDto = new PowerConsumeQueryDto()
                .setMeterName("电表001");
        PageParam pageParam = new PageParam()
                .setPageNum(1)
                .setPageSize(10);

        // Mock PageHelper behavior
        try (MockedStatic<PageMethod> pageHelper = Mockito.mockStatic(PageMethod.class);
             Page<ElectricMeterBalanceConsumeRecordEntity> mockPage = mockPage()) {
            PageInfo<ElectricMeterBalanceConsumeRecordEntity> pageInfo = new PageInfo<>();
            pageInfo.setList(List.of(mockEntities.get(0)));
            pageInfo.setTotal(1);

            pageHelper.when(() -> PageMethod.startPage(pageParam.getPageNum(), pageParam.getPageSize())).thenReturn(mockPage);
            when(mockPage.<ElectricMeterBalanceConsumeRecordEntity>doSelectPageInfo(any())).thenReturn(pageInfo);

            // When
            PageResult<PowerConsumeRecordDto> result = meterConsumeService.findPowerConsumePage(queryDto, pageParam);

            // Then
            assertNotNull(result);
            assertEquals(1L, result.getTotal());
            assertEquals(1, result.getList().size());
            assertEquals("电表001", result.getList().get(0).getMeterName());
        }
    }

    /**
     * 创建模拟实体对象
     */
    private ElectricMeterBalanceConsumeRecordEntity createMockEntity(Integer id, String meterName, String meterNo,
                                                                     String spaceName, BigDecimal beginBalance,
                                                                     BigDecimal consumeAmount, BigDecimal endBalance,
                                                                     Integer electricAccountType, LocalDateTime consumeTime) {
        return new ElectricMeterBalanceConsumeRecordEntity()
                .setId(id)
                .setMeterName(meterName)
                .setMeterNo(meterNo)
                .setSpaceName(spaceName)
                .setBeginBalance(beginBalance)
                .setConsumeAmount(consumeAmount)
                .setEndBalance(endBalance)
                .setElectricAccountType(electricAccountType)
                .setMeterConsumeTime(consumeTime);
    }

    @SuppressWarnings("unchecked")
    private <T> Page<T> mockPage() {
        return Mockito.mock(Page.class);
    }
}
