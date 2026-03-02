package info.zhihui.ems.business.finance.service.consume;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.page.PageMethod;
import info.zhihui.ems.business.finance.bo.BalanceBo;
import info.zhihui.ems.business.finance.dto.*;
import info.zhihui.ems.business.finance.dto.MeterCorrectionRecordDto;
import info.zhihui.ems.business.finance.entity.ElectricMeterBalanceConsumeRecordEntity;
import info.zhihui.ems.business.finance.enums.CorrectionTypeEnum;
import info.zhihui.ems.business.finance.repository.ElectricMeterBalanceConsumeRecordRepository;
import info.zhihui.ems.business.finance.repository.ElectricMeterPowerConsumeRecordRepository;
import info.zhihui.ems.business.finance.repository.ElectricMeterPowerRecordRepository;
import info.zhihui.ems.business.finance.repository.ElectricMeterPowerRelationRepository;
import info.zhihui.ems.business.finance.service.balance.BalanceService;
import info.zhihui.ems.business.finance.service.consume.impl.MeterConsumeServiceImpl;
import info.zhihui.ems.common.enums.BalanceTypeEnum;
import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import info.zhihui.ems.common.enums.OwnerTypeEnum;
import info.zhihui.ems.common.exception.ParamException;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.components.context.RequestContext;
import info.zhihui.ems.components.lock.core.LockTemplate;
import info.zhihui.ems.business.plan.service.ElectricPricePlanService;
import info.zhihui.ems.foundation.organization.service.OrganizationService;
import info.zhihui.ems.foundation.space.service.SpaceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MeterCorrectionServiceImplTest {

    @Mock
    private BalanceService balanceService;
    @Mock
    private ElectricMeterPowerRecordRepository electricMeterPowerRecordRepository;
    @Mock
    private ElectricMeterPowerRelationRepository electricMeterPowerRelationRepository;
    @Mock
    private ElectricMeterPowerConsumeRecordRepository electricMeterPowerConsumeRecordRepository;
    @Mock
    private ElectricMeterBalanceConsumeRecordRepository electricMeterBalanceConsumeRecordRepository;
    @Mock
    private LockTemplate lockTemplate;
    @Mock
    private RLock lock;

    @Mock
    private RequestContext requestContext;

    @InjectMocks
    private MeterConsumeServiceImpl meterCorrectionService;

    @Test
    void testCorrectByAmount_QuantityAccount_ShouldDeductAndRecord() {
        CorrectMeterAmountDto dto = new CorrectMeterAmountDto()
                .setAccountId(10)
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY)
                .setOwnerId(200)
                .setOwnerType(OwnerTypeEnum.ENTERPRISE)
                .setOwnerName("企业客户")
                .setMeterId(300)
                .setMeterName("测试电表A")
                .setDeviceNo("MTR-A")
                .setCorrectionType(CorrectionTypeEnum.PAY)
                .setAmount(new BigDecimal("12.349"))
                .setReason("补扣差额");

        when(balanceService.getByQuery(any(BalanceQueryDto.class)))
                .thenReturn(new BalanceBo().setBalance(new BigDecimal("80.00")));
        when(electricMeterBalanceConsumeRecordRepository.insert(any(ElectricMeterBalanceConsumeRecordEntity.class)))
                .thenReturn(1);

        assertDoesNotThrow(() -> meterCorrectionService.correctByAmount(dto));

        ArgumentCaptor<BalanceDto> balanceCaptor = ArgumentCaptor.forClass(BalanceDto.class);
        verify(balanceService).deduct(balanceCaptor.capture());
        verify(balanceService, never()).topUp(any());

        BalanceDto balanceDto = balanceCaptor.getValue();
        assertEquals(BalanceTypeEnum.ELECTRIC_METER, balanceDto.getBalanceType());
        assertEquals(dto.getMeterId(), balanceDto.getBalanceRelationId());
        assertEquals(new BigDecimal("12.34"), balanceDto.getAmount());

        ArgumentCaptor<ElectricMeterBalanceConsumeRecordEntity> recordCaptor = ArgumentCaptor.forClass(ElectricMeterBalanceConsumeRecordEntity.class);
        verify(electricMeterBalanceConsumeRecordRepository).insert(recordCaptor.capture());
        ElectricMeterBalanceConsumeRecordEntity record = recordCaptor.getValue();
        assertEquals(dto.getMeterId(), record.getMeterId());
        assertEquals(dto.getOwnerId(), record.getOwnerId());
        assertEquals(dto.getOwnerName(), record.getOwnerName());
        assertEquals(dto.getElectricAccountType().getCode(), record.getElectricAccountType());
        assertEquals(new BigDecimal("12.34"), record.getConsumeAmount());
    }

    @Test
    void testFindCorrectionRecordPage_ShouldApplyFilters() {
        MeterCorrectionRecordQueryDto queryDto = new MeterCorrectionRecordQueryDto()
                .setAccountId(1)
                .setMeterId(2)
                .setMeterName("电表")
                .setBeginTime(LocalDateTime.of(2024, 1, 1, 0, 0))
                .setEndTime(LocalDateTime.of(2024, 1, 31, 23, 59));
        PageParam pageParam = new PageParam().setPageNum(1).setPageSize(10);

        ElectricMeterBalanceConsumeRecordEntity entity = new ElectricMeterBalanceConsumeRecordEntity()
                .setConsumeNo("NO1")
                .setAccountId(1)
                .setOwnerId(10)
                .setOwnerType(OwnerTypeEnum.PERSONAL.getCode())
                .setOwnerName("用户A")
                .setMeterId(2)
                .setMeterName("电表A")
                .setDeviceNo("M001")
                .setSpaceId(100)
                .setSpaceName("房间1")
                .setConsumeAmount(new BigDecimal("5.00"))
                .setBeginBalance(new BigDecimal("100.00"))
                .setEndBalance(new BigDecimal("95.00"))
                .setRemark("补正原因:测试")
                .setMeterConsumeTime(LocalDateTime.now())
                .setCreateTime(LocalDateTime.now());
        try (MockedStatic<PageMethod> pageHelper = Mockito.mockStatic(PageMethod.class)) {
            PageInfo<ElectricMeterBalanceConsumeRecordEntity> pageInfo = new PageInfo<>();
            pageInfo.setList(List.of(entity));
            pageInfo.setTotal(1L);

            pageHelper.when(() -> PageHelper.startPage(1, 10)).thenAnswer(invocation -> {
                Page<ElectricMeterBalanceConsumeRecordEntity> mockPage = Mockito.mock(Page.class);
                when(mockPage.<ElectricMeterBalanceConsumeRecordEntity>doSelectPageInfo(any()))
                        .thenReturn(pageInfo);
                return mockPage;
            });

            PageResult<MeterCorrectionRecordDto> result = meterCorrectionService.findCorrectionRecordPage(queryDto, pageParam);

            assertEquals(1L, result.getTotal());
            assertEquals(1, result.getList().size());
            assertEquals("NO1", result.getList().get(0).getConsumeNo());
            assertEquals(new BigDecimal("100.00"), result.getList().get(0).getBeginBalance());
            assertEquals(new BigDecimal("95.00"), result.getList().get(0).getEndBalance());
        }
    }

    @Test
    void testCorrectByAmount_MergedAccount_ShouldRefundAndRecord() {
        CorrectMeterAmountDto dto = new CorrectMeterAmountDto()
                .setAccountId(11)
                .setElectricAccountType(ElectricAccountTypeEnum.MERGED)
                .setOwnerId(201)
                .setOwnerType(OwnerTypeEnum.PERSONAL)
                .setOwnerName("个人用户")
                .setCorrectionType(CorrectionTypeEnum.REFUND)
                .setAmount(new BigDecimal("5.678"))
                .setReason("返还差额");

        when(balanceService.getByQuery(any(BalanceQueryDto.class)))
                .thenReturn(new BalanceBo().setBalance(new BigDecimal("120.00")));
        when(electricMeterBalanceConsumeRecordRepository.insert(any(ElectricMeterBalanceConsumeRecordEntity.class)))
                .thenReturn(1);

        assertDoesNotThrow(() -> meterCorrectionService.correctByAmount(dto));

        ArgumentCaptor<BalanceDto> balanceCaptor = ArgumentCaptor.forClass(BalanceDto.class);
        verify(balanceService).topUp(balanceCaptor.capture());
        verify(balanceService, never()).deduct(any());

        BalanceDto balanceDto = balanceCaptor.getValue();
        assertEquals(BalanceTypeEnum.ACCOUNT, balanceDto.getBalanceType());
        assertEquals(dto.getAccountId(), balanceDto.getBalanceRelationId());
        assertEquals(new BigDecimal("5.67"), balanceDto.getAmount());

        ArgumentCaptor<ElectricMeterBalanceConsumeRecordEntity> recordCaptor = ArgumentCaptor.forClass(ElectricMeterBalanceConsumeRecordEntity.class);
        verify(electricMeterBalanceConsumeRecordRepository).insert(recordCaptor.capture());
        ElectricMeterBalanceConsumeRecordEntity record = recordCaptor.getValue();
        assertNull(record.getMeterId(), "合并账户补正不应关联具体电表");
        assertEquals(dto.getOwnerId(), record.getOwnerId());
        assertEquals(dto.getOwnerName(), record.getOwnerName());
        assertEquals(dto.getElectricAccountType().getCode(), record.getElectricAccountType());
        assertEquals(new BigDecimal("-5.67"), record.getConsumeAmount());
    }

}
