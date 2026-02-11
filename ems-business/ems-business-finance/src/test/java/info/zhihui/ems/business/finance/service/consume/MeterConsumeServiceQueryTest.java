package info.zhihui.ems.business.finance.service.consume;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.page.PageMethod;
import info.zhihui.ems.business.finance.dto.PowerConsumeQueryDto;
import info.zhihui.ems.business.finance.dto.PowerConsumeDetailDto;
import info.zhihui.ems.business.finance.dto.PowerConsumeRecordDto;
import info.zhihui.ems.business.finance.entity.ElectricMeterBalanceConsumeRecordEntity;
import info.zhihui.ems.business.finance.entity.ElectricMeterPowerConsumeRecordEntity;
import info.zhihui.ems.business.finance.enums.ConsumeTypeEnum;
import info.zhihui.ems.business.finance.repository.ElectricMeterBalanceConsumeRecordRepository;
import info.zhihui.ems.business.finance.repository.ElectricMeterPowerConsumeRecordRepository;
import info.zhihui.ems.business.finance.service.consume.impl.MeterConsumeServiceImpl;
import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.exception.NotFoundException;
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

    @Mock
    private ElectricMeterPowerConsumeRecordRepository electricMeterPowerConsumeRecordRepository;

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

    @Test
    void testGetPowerConsumeDetail_Success() {
        Integer consumeRecordId = 1;
        Integer meterConsumeRecordId = 101;

        ElectricMeterBalanceConsumeRecordEntity balanceRecord = new ElectricMeterBalanceConsumeRecordEntity()
                .setId(consumeRecordId)
                .setConsumeType(ConsumeTypeEnum.ELECTRIC.getCode())
                .setMeterConsumeRecordId(meterConsumeRecordId)
                .setConsumeNo("CP202601010001")
                .setAccountId(10)
                .setMeterId(20)
                .setMeterName("测试电表")
                .setMeterNo("M001")
                .setSpaceName("A101")
                .setConsumeAmount(new BigDecimal("8.66"))
                .setConsumeAmountHigh(new BigDecimal("2.00"))
                .setConsumeAmountLow(new BigDecimal("6.66"))
                .setBeginBalance(new BigDecimal("100.00"))
                .setEndBalance(new BigDecimal("91.34"))
                .setMeterConsumeTime(LocalDateTime.of(2026, 2, 10, 12, 30))
                .setIsDeleted(false);
        when(electricMeterBalanceConsumeRecordRepository.selectById(consumeRecordId)).thenReturn(balanceRecord);

        ElectricMeterPowerConsumeRecordEntity powerRecord = new ElectricMeterPowerConsumeRecordEntity()
                .setId(meterConsumeRecordId)
                .setBeginPower(new BigDecimal("1000.00"))
                .setEndPower(new BigDecimal("1010.50"))
                .setConsumePower(new BigDecimal("10.50"))
                .setConsumePowerHigh(new BigDecimal("3.20"))
                .setConsumePowerLow(new BigDecimal("7.30"))
                .setBeginRecordTime(LocalDateTime.of(2026, 2, 10, 12, 0))
                .setEndRecordTime(LocalDateTime.of(2026, 2, 10, 12, 30))
                .setIsDeleted(false);
        when(electricMeterPowerConsumeRecordRepository.selectById(meterConsumeRecordId)).thenReturn(powerRecord);

        PowerConsumeDetailDto detailDto = meterConsumeService.getPowerConsumeDetail(consumeRecordId);

        assertNotNull(detailDto);
        assertEquals(consumeRecordId, detailDto.getId());
        assertEquals(meterConsumeRecordId, detailDto.getMeterConsumeRecordId());
        assertEquals("CP202601010001", detailDto.getConsumeNo());
        assertEquals(new BigDecimal("10.50"), detailDto.getConsumePower());
        assertEquals(new BigDecimal("8.66"), detailDto.getConsumeAmount());
        assertEquals(LocalDateTime.of(2026, 2, 10, 12, 30), detailDto.getConsumeTime());
    }

    @Test
    void testGetPowerConsumeDetail_WhenBalanceRecordNotFound_ShouldThrow() {
        when(electricMeterBalanceConsumeRecordRepository.selectById(1)).thenReturn(null);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> meterConsumeService.getPowerConsumeDetail(1));
        assertEquals("电量消费记录不存在", exception.getMessage());
    }

    @Test
    void testGetPowerConsumeDetail_WhenNotElectricConsume_ShouldThrow() {
        ElectricMeterBalanceConsumeRecordEntity balanceRecord = new ElectricMeterBalanceConsumeRecordEntity()
                .setId(1)
                .setConsumeType(0)
                .setIsDeleted(false);
        when(electricMeterBalanceConsumeRecordRepository.selectById(1)).thenReturn(balanceRecord);

        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> meterConsumeService.getPowerConsumeDetail(1));
        assertEquals("当前记录不是电量消费记录", exception.getMessage());
    }

    @Test
    void testGetPowerConsumeDetail_WhenMeterConsumeRecordIdNull_ShouldThrow() {
        ElectricMeterBalanceConsumeRecordEntity balanceRecord = new ElectricMeterBalanceConsumeRecordEntity()
                .setId(1)
                .setConsumeType(ConsumeTypeEnum.ELECTRIC.getCode())
                .setMeterConsumeRecordId(null)
                .setIsDeleted(false);
        when(electricMeterBalanceConsumeRecordRepository.selectById(1)).thenReturn(balanceRecord);

        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> meterConsumeService.getPowerConsumeDetail(1));
        assertEquals("电量消费记录数据异常：缺少电量明细记录", exception.getMessage());
    }

    @Test
    void testGetPowerConsumeDetail_WhenPowerRecordNotFound_ShouldThrow() {
        ElectricMeterBalanceConsumeRecordEntity balanceRecord = new ElectricMeterBalanceConsumeRecordEntity()
                .setId(1)
                .setConsumeType(ConsumeTypeEnum.ELECTRIC.getCode())
                .setMeterConsumeRecordId(101)
                .setIsDeleted(false);
        when(electricMeterBalanceConsumeRecordRepository.selectById(1)).thenReturn(balanceRecord);
        when(electricMeterPowerConsumeRecordRepository.selectById(101)).thenReturn(null);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> meterConsumeService.getPowerConsumeDetail(1));
        assertEquals("电量明细记录不存在", exception.getMessage());
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
