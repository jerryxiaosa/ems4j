package info.zhihui.ems.business.finance.service.consume;

import info.zhihui.ems.business.finance.bo.BalanceBo;
import info.zhihui.ems.business.finance.dto.*;
import info.zhihui.ems.business.finance.entity.*;
import info.zhihui.ems.business.finance.enums.CorrectionTypeEnum;
import info.zhihui.ems.business.finance.service.consume.impl.MeterConsumeServiceImpl;
import info.zhihui.ems.common.enums.BalanceTypeEnum;
import info.zhihui.ems.business.finance.qo.ElectricMeterPowerRecordQo;
import info.zhihui.ems.business.finance.repository.ElectricMeterBalanceConsumeRecordRepository;
import info.zhihui.ems.business.finance.repository.ElectricMeterPowerConsumeRecordRepository;
import info.zhihui.ems.business.finance.repository.ElectricMeterPowerRecordRepository;
import info.zhihui.ems.business.finance.repository.ElectricMeterPowerRelationRepository;
import info.zhihui.ems.business.finance.service.balance.BalanceService;
import info.zhihui.ems.common.enums.CalculateTypeEnum;
import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import info.zhihui.ems.common.enums.OwnerTypeEnum;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.exception.ParamException;
import info.zhihui.ems.components.context.RequestContext;
import info.zhihui.ems.components.lock.core.LockTemplate;
import info.zhihui.ems.business.plan.bo.ElectricPricePlanDetailBo;
import info.zhihui.ems.business.plan.bo.StepPriceBo;
import info.zhihui.ems.business.plan.service.ElectricPricePlanService;
import info.zhihui.ems.foundation.organization.bo.OrganizationBo;
import info.zhihui.ems.foundation.organization.service.OrganizationService;
import info.zhihui.ems.foundation.space.bo.SpaceBo;
import info.zhihui.ems.foundation.space.service.SpaceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * MeterConsumeServiceImpl 单元测试类
 *
 * @author jerryxiaosa
 */
@DisplayName("电表电量消费服务测试")
@ExtendWith(MockitoExtension.class)
class MeterConsumeServiceImplTest {

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
    private ElectricPricePlanService electricPricePlanService;

    @Mock
    private SpaceService spaceService;

    @Mock
    private OrganizationService organizationService;

    @Mock
    private LockTemplate lockTemplate;

    @Mock
    private RLock lock;

    @Mock
    private RequestContext requestContext;

    @InjectMocks
    private MeterConsumeServiceImpl electricMeterConsumeService;

    private ElectricMeterPowerRecordDto testDto;
    private ElectricMeterDetailDto meterDetailDto;
    private ElectricPricePlanDetailBo pricePlanDetailBo;
    private BalanceBo balanceBo;
    private SpaceBo spaceBo;
    private OrganizationBo organizationBo;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        meterDetailDto = new ElectricMeterDetailDto()
                .setMeterId(1)
                .setMeterName("测试电表")
                .setMeterNo("METER001")
                .setIsPrepay(true)
                .setCt(BigDecimal.valueOf(100))
                .setIsCalculate(true)
                .setCalculateType(CalculateTypeEnum.ELEVATOR)
                .setSpaceId(1)
                .setPricePlanId(1)
                .setStepStartValue(BigDecimal.ZERO);

        testDto = new ElectricMeterPowerRecordDto()
                .setElectricMeterDetailDto(meterDetailDto)
                .setAccountId(1)
                .setOwnerId(1)
                .setOwnerType(OwnerTypeEnum.ENTERPRISE)
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY)
                .setPower(BigDecimal.valueOf(1000))
                .setPowerHigher(BigDecimal.valueOf(200))
                .setPowerHigh(BigDecimal.valueOf(300))
                .setPowerLow(BigDecimal.valueOf(400))
                .setPowerLower(BigDecimal.valueOf(100))
                .setPowerDeepLow(BigDecimal.ZERO)
                .setRecordTime(LocalDateTime.now())
                .setNeedConsume(true);

        // 初始化价格计划
        pricePlanDetailBo = new ElectricPricePlanDetailBo();
        pricePlanDetailBo.setName("测试价格计划")
                .setPriceHigher(BigDecimal.valueOf(1.2))
                .setPriceHigh(BigDecimal.valueOf(1.0))
                .setPriceLow(BigDecimal.valueOf(0.8))
                .setPriceLower(BigDecimal.valueOf(0.6))
                .setPriceDeepLow(BigDecimal.valueOf(0.4))
                .setIsStep(false);

        // 初始化余额信息
        balanceBo = new BalanceBo()
                .setBalance(BigDecimal.valueOf(1000));

        // 初始化空间信息
        spaceBo = new SpaceBo()
                .setName("测试空间")
                .setParentsIds(List.of(1, 2))
                .setParentsNames(List.of("父空间1", "父空间2"));

        // 初始化组织信息
        organizationBo = new OrganizationBo()
                .setManagerName("测试管理员")
                .setManagerPhone("13800138000");
    }

    /**
     * 测试扣费失败场景
     */
    @Test
    @DisplayName("保存电表记录-扣费失败应抛异常且不写余额消费记录")
    void testSavePowerRecord_DeductFailed_ShouldThrow() {
        when(lockTemplate.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
        when(electricMeterPowerRecordRepository.insert(any(ElectricMeterPowerRecordEntity.class))).thenReturn(1);
        when(electricMeterPowerRelationRepository.insert(any(ElectricMeterPowerRelationEntity.class))).thenReturn(1);

        ElectricMeterPowerConsumeRecordEntity lastConsumeRecord = new ElectricMeterPowerConsumeRecordEntity()
                .setAccountId(1)
                .setEndRecordId(100)
                .setMeterConsumeTime(LocalDateTime.now().minusHours(1));
        when(electricMeterPowerConsumeRecordRepository.getMeterLastConsumeRecord(1)).thenReturn(lastConsumeRecord);

        ElectricMeterPowerRecordEntity lastPowerRecord = new ElectricMeterPowerRecordEntity()
                .setPower(BigDecimal.valueOf(800))
                .setPowerHigher(BigDecimal.valueOf(150))
                .setPowerHigh(BigDecimal.valueOf(250))
                .setPowerLow(BigDecimal.valueOf(350))
                .setPowerLower(BigDecimal.valueOf(50))
                .setPowerDeepLow(BigDecimal.ZERO)
                .setRecordTime(LocalDateTime.now().minusHours(1));
        when(electricMeterPowerRecordRepository.selectById(100)).thenReturn(lastPowerRecord);

        when(electricMeterPowerConsumeRecordRepository.insert(any(ElectricMeterPowerConsumeRecordEntity.class))).thenReturn(1);
        when(electricPricePlanService.getDetail(1)).thenReturn(pricePlanDetailBo);
        doThrow(new BusinessRuntimeException("余额不足"))
                .when(balanceService).deduct(any(BalanceDto.class));
        when(spaceService.getDetail(1)).thenReturn(spaceBo);
        when(organizationService.getDetail(1)).thenReturn(organizationBo);

        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> electricMeterConsumeService.savePowerRecord(testDto));
        assertTrue(exception.getMessage().contains("余额扣费失败"));

        verify(electricMeterBalanceConsumeRecordRepository, never()).insert(any(ElectricMeterBalanceConsumeRecordEntity.class));
        verify(lock).unlock();
    }

    @Test
    @DisplayName("保存电表记录-消费记录保存失败应抛异常并跳过扣费")
    void testSavePowerRecord_ConsumeRecordInsertFailed_ShouldThrow() {
        when(lockTemplate.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
        when(electricMeterPowerRecordRepository.insert(any(ElectricMeterPowerRecordEntity.class))).thenReturn(1);
        when(electricMeterPowerRelationRepository.insert(any(ElectricMeterPowerRelationEntity.class))).thenReturn(1);

        ElectricMeterPowerConsumeRecordEntity lastConsumeRecord = new ElectricMeterPowerConsumeRecordEntity()
                .setAccountId(1)
                .setEndRecordId(100)
                .setMeterConsumeTime(LocalDateTime.now().minusHours(1));
        when(electricMeterPowerConsumeRecordRepository.getMeterLastConsumeRecord(1)).thenReturn(lastConsumeRecord);

        ElectricMeterPowerRecordEntity lastPowerRecord = new ElectricMeterPowerRecordEntity()
                .setPower(BigDecimal.valueOf(800))
                .setRecordTime(LocalDateTime.now().minusHours(1));
        when(electricMeterPowerRecordRepository.selectById(100)).thenReturn(lastPowerRecord);

        when(electricMeterPowerConsumeRecordRepository.insert(any(ElectricMeterPowerConsumeRecordEntity.class)))
                .thenThrow(new RuntimeException("插入失败"));
        when(spaceService.getDetail(1)).thenReturn(spaceBo);
        when(organizationService.getDetail(1)).thenReturn(organizationBo);

        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> electricMeterConsumeService.savePowerRecord(testDto));
        assertTrue(exception.getMessage().contains("保存消费记录失败"));

        verify(balanceService, never()).deduct(any(BalanceDto.class));
        verify(lock).unlock();
    }

    @Test
    @DisplayName("保存电表记录-accountId为空时仅记录电量并跳过扣费")
    void testSavePowerRecord_AccountIdNull_ShouldSkipBalanceDeduction() {
        testDto.setAccountId(null);
        when(lockTemplate.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
        when(electricMeterPowerRecordRepository.insert(any(ElectricMeterPowerRecordEntity.class))).thenReturn(1);
        when(electricMeterPowerRelationRepository.insert(any(ElectricMeterPowerRelationEntity.class))).thenReturn(1);

        ElectricMeterPowerRecordEntity historyRecord1 = new ElectricMeterPowerRecordEntity()
                .setId(10)
                .setPower(BigDecimal.valueOf(900))
                .setPowerHigher(BigDecimal.valueOf(150))
                .setPowerHigh(BigDecimal.valueOf(250))
                .setPowerLow(BigDecimal.valueOf(300))
                .setPowerLower(BigDecimal.valueOf(80))
                .setPowerDeepLow(BigDecimal.ZERO)
                .setRecordTime(LocalDateTime.now().minusHours(2));
        ElectricMeterPowerRecordEntity historyRecord2 = new ElectricMeterPowerRecordEntity()
                .setId(11)
                .setPower(BigDecimal.valueOf(950))
                .setPowerHigher(BigDecimal.valueOf(180))
                .setPowerHigh(BigDecimal.valueOf(260))
                .setPowerLow(BigDecimal.valueOf(320))
                .setPowerLower(BigDecimal.valueOf(90))
                .setPowerDeepLow(BigDecimal.ZERO)
                .setRecordTime(LocalDateTime.now().minusHours(1));
        when(electricMeterPowerRecordRepository.findRecordList(any(ElectricMeterPowerRecordQo.class)))
                .thenReturn(List.of(historyRecord1, historyRecord2));
        when(electricMeterPowerConsumeRecordRepository.insert(any(ElectricMeterPowerConsumeRecordEntity.class))).thenReturn(1);

        assertDoesNotThrow(() -> electricMeterConsumeService.savePowerRecord(testDto));

        verify(electricMeterPowerConsumeRecordRepository).insert(any(ElectricMeterPowerConsumeRecordEntity.class));
        verify(balanceService, never()).deduct(any(BalanceDto.class));
        verify(electricMeterBalanceConsumeRecordRepository, never()).insert(any(ElectricMeterBalanceConsumeRecordEntity.class));
        verify(lock).unlock();
    }

    @Test
    @DisplayName("保存电表记录-ownerType为空时余额消费记录ownerType为null")
    void testSavePowerRecord_OwnerTypeNull_ShouldSetNullInBalanceRecord() {
        testDto.setOwnerType(null);
        when(lockTemplate.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
        when(electricMeterPowerRecordRepository.insert(any(ElectricMeterPowerRecordEntity.class))).thenReturn(1);
        when(electricMeterPowerRelationRepository.insert(any(ElectricMeterPowerRelationEntity.class))).thenReturn(1);

        ElectricMeterPowerConsumeRecordEntity lastConsumeRecord = new ElectricMeterPowerConsumeRecordEntity()
                .setAccountId(1)
                .setEndRecordId(100)
                .setMeterConsumeTime(LocalDateTime.now().minusHours(1));
        when(electricMeterPowerConsumeRecordRepository.getMeterLastConsumeRecord(1)).thenReturn(lastConsumeRecord);

        ElectricMeterPowerRecordEntity lastPowerRecord = new ElectricMeterPowerRecordEntity()
                .setPower(BigDecimal.valueOf(800))
                .setPowerHigher(BigDecimal.valueOf(150))
                .setPowerHigh(BigDecimal.valueOf(250))
                .setPowerLow(BigDecimal.valueOf(350))
                .setPowerLower(BigDecimal.valueOf(50))
                .setPowerDeepLow(BigDecimal.ZERO)
                .setRecordTime(LocalDateTime.now().minusHours(1));
        when(electricMeterPowerRecordRepository.selectById(100)).thenReturn(lastPowerRecord);

        when(electricMeterPowerConsumeRecordRepository.insert(any(ElectricMeterPowerConsumeRecordEntity.class))).thenReturn(1);
        when(electricPricePlanService.getDetail(1)).thenReturn(pricePlanDetailBo);
        when(balanceService.query(any(BalanceQueryDto.class))).thenReturn(balanceBo);
        when(electricMeterBalanceConsumeRecordRepository.insert(any(ElectricMeterBalanceConsumeRecordEntity.class))).thenReturn(1);
        when(spaceService.getDetail(1)).thenReturn(spaceBo);

        assertDoesNotThrow(() -> electricMeterConsumeService.savePowerRecord(testDto));

        ArgumentCaptor<ElectricMeterBalanceConsumeRecordEntity> captor = ArgumentCaptor.forClass(ElectricMeterBalanceConsumeRecordEntity.class);
        verify(electricMeterBalanceConsumeRecordRepository).insert(captor.capture());
        assertNull(captor.getValue().getOwnerType());
        verify(lock).unlock();
    }

    @Test
    @DisplayName("保存电表记录-读数回退导致负用电量应记录负值")
    void testSavePowerRecord_NegativeConsumePower_ShouldRecordNegative() {
        meterDetailDto.setIsPrepay(false);
        when(lockTemplate.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
        when(electricMeterPowerRecordRepository.insert(any(ElectricMeterPowerRecordEntity.class))).thenReturn(1);
        when(electricMeterPowerRelationRepository.insert(any(ElectricMeterPowerRelationEntity.class))).thenReturn(1);

        ElectricMeterPowerConsumeRecordEntity lastConsumeRecord = new ElectricMeterPowerConsumeRecordEntity()
                .setAccountId(1)
                .setEndRecordId(100)
                .setMeterConsumeTime(LocalDateTime.now().minusHours(1));
        when(electricMeterPowerConsumeRecordRepository.getMeterLastConsumeRecord(1)).thenReturn(lastConsumeRecord);

        ElectricMeterPowerRecordEntity lastPowerRecord = new ElectricMeterPowerRecordEntity()
                .setPower(BigDecimal.valueOf(1200))
                .setPowerHigher(BigDecimal.valueOf(300))
                .setPowerHigh(BigDecimal.valueOf(400))
                .setPowerLow(BigDecimal.valueOf(500))
                .setPowerLower(BigDecimal.valueOf(200))
                .setPowerDeepLow(BigDecimal.ZERO)
                .setRecordTime(LocalDateTime.now().minusHours(1));
        when(electricMeterPowerRecordRepository.selectById(100)).thenReturn(lastPowerRecord);
        when(electricMeterPowerConsumeRecordRepository.insert(any(ElectricMeterPowerConsumeRecordEntity.class))).thenReturn(1);
        when(spaceService.getDetail(1)).thenReturn(spaceBo);
        when(organizationService.getDetail(1)).thenReturn(organizationBo);

        electricMeterConsumeService.savePowerRecord(testDto);

        ArgumentCaptor<ElectricMeterPowerConsumeRecordEntity> captor = ArgumentCaptor.forClass(ElectricMeterPowerConsumeRecordEntity.class);
        verify(electricMeterPowerConsumeRecordRepository).insert(captor.capture());
        assertTrue(captor.getValue().getConsumePower().compareTo(BigDecimal.ZERO) < 0);
        verify(lock).unlock();
    }

    @Test
    @DisplayName("保存电表记录-余额不足应抛异常")
    void testSavePowerRecord_BalanceInsufficient_ShouldThrow() {
        when(lockTemplate.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
        when(electricMeterPowerRecordRepository.insert(any(ElectricMeterPowerRecordEntity.class))).thenReturn(1);
        when(electricMeterPowerRelationRepository.insert(any(ElectricMeterPowerRelationEntity.class))).thenReturn(1);

        ElectricMeterPowerConsumeRecordEntity lastConsumeRecord = new ElectricMeterPowerConsumeRecordEntity()
                .setAccountId(1)
                .setEndRecordId(100)
                .setMeterConsumeTime(LocalDateTime.now().minusHours(1));
        when(electricMeterPowerConsumeRecordRepository.getMeterLastConsumeRecord(1)).thenReturn(lastConsumeRecord);

        ElectricMeterPowerRecordEntity lastPowerRecord = new ElectricMeterPowerRecordEntity()
                .setPower(BigDecimal.valueOf(800))
                .setPowerHigher(BigDecimal.valueOf(150))
                .setPowerHigh(BigDecimal.valueOf(250))
                .setPowerLow(BigDecimal.valueOf(350))
                .setPowerLower(BigDecimal.valueOf(50))
                .setPowerDeepLow(BigDecimal.ZERO)
                .setRecordTime(LocalDateTime.now().minusHours(1));
        when(electricMeterPowerRecordRepository.selectById(100)).thenReturn(lastPowerRecord);

        when(electricMeterPowerConsumeRecordRepository.insert(any(ElectricMeterPowerConsumeRecordEntity.class))).thenReturn(1);
        when(electricPricePlanService.getDetail(1)).thenReturn(pricePlanDetailBo);
        doThrow(new BusinessRuntimeException("余额不足"))
                .when(balanceService).deduct(any(BalanceDto.class));
        when(spaceService.getDetail(1)).thenReturn(spaceBo);
        when(organizationService.getDetail(1)).thenReturn(organizationBo);

        assertThrows(BusinessRuntimeException.class, () -> electricMeterConsumeService.savePowerRecord(testDto));
        verify(lock).unlock();
    }
    @Test
    void testSavePowerRecord_Normal() {
        // Given
        when(lockTemplate.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
        when(electricMeterPowerRecordRepository.insert(any(ElectricMeterPowerRecordEntity.class))).thenReturn(1);
        when(electricMeterPowerRelationRepository.insert(any(ElectricMeterPowerRelationEntity.class))).thenReturn(1);

        // 模拟存在上一条消费记录
        ElectricMeterPowerConsumeRecordEntity lastConsumeRecord = new ElectricMeterPowerConsumeRecordEntity()
                .setAccountId(1)
                .setEndRecordId(100)
                .setMeterConsumeTime(LocalDateTime.now().minusHours(1));
        when(electricMeterPowerConsumeRecordRepository.getMeterLastConsumeRecord(1)).thenReturn(lastConsumeRecord);

        // 模拟上一条电表记录
        ElectricMeterPowerRecordEntity lastPowerRecord = new ElectricMeterPowerRecordEntity()
                .setPower(BigDecimal.valueOf(800))
                .setPowerHigher(BigDecimal.valueOf(150))
                .setPowerHigh(BigDecimal.valueOf(250))
                .setPowerLow(BigDecimal.valueOf(350))
                .setPowerLower(BigDecimal.valueOf(50))
                .setPowerDeepLow(BigDecimal.ZERO)
                .setRecordTime(LocalDateTime.now().minusHours(1));
        when(electricMeterPowerRecordRepository.selectById(100)).thenReturn(lastPowerRecord);

        when(electricMeterPowerConsumeRecordRepository.insert(any(ElectricMeterPowerConsumeRecordEntity.class))).thenReturn(1);
        when(electricPricePlanService.getDetail(1)).thenReturn(pricePlanDetailBo);
        when(balanceService.query(any(BalanceQueryDto.class))).thenReturn(balanceBo);
        when(electricMeterBalanceConsumeRecordRepository.insert(any(ElectricMeterBalanceConsumeRecordEntity.class))).thenReturn(1);
        when(spaceService.getDetail(1)).thenReturn(spaceBo);
        when(organizationService.getDetail(1)).thenReturn(organizationBo);

        // When
        assertDoesNotThrow(() -> electricMeterConsumeService.savePowerRecord(testDto));

        // Then
        verify(electricMeterPowerRecordRepository).insert(any(ElectricMeterPowerRecordEntity.class));
        verify(electricMeterPowerRelationRepository).insert(any(ElectricMeterPowerRelationEntity.class));
        verify(electricMeterPowerConsumeRecordRepository).insert(any(ElectricMeterPowerConsumeRecordEntity.class));
        verify(balanceService).deduct(any(BalanceDto.class));
        verify(electricMeterBalanceConsumeRecordRepository).insert(any(ElectricMeterBalanceConsumeRecordEntity.class));
        verify(lock).unlock();
    }

    /**
     * 测试锁获取失败的情况
     */
    @Test
    void testSavePowerRecord_LockFailed() {
        // Given
        when(lockTemplate.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock()).thenReturn(false);

        // When & Then
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> electricMeterConsumeService.savePowerRecord(testDto));
        assertEquals("正在保存电表记录，请稍后重试", exception.getMessage());

        verify(electricMeterPowerRecordRepository, never()).insert(any(ElectricMeterPowerRecordEntity.class));
    }

    /**
     * 测试 needConsume 为 false 的情况
     */
    @Test
    void testSavePowerRecord_NoConsume() {
        // Given
        testDto.setNeedConsume(false);
        when(lockTemplate.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
        when(electricMeterPowerRecordRepository.insert(any(ElectricMeterPowerRecordEntity.class))).thenReturn(1);
        when(electricMeterPowerRelationRepository.insert(any(ElectricMeterPowerRelationEntity.class))).thenReturn(1);
        when(spaceService.getDetail(1)).thenReturn(spaceBo);
        when(organizationService.getDetail(1)).thenReturn(organizationBo);

        // When
        assertDoesNotThrow(() -> electricMeterConsumeService.savePowerRecord(testDto));

        // Then
        verify(electricMeterPowerRecordRepository).insert(any(ElectricMeterPowerRecordEntity.class));
        verify(electricMeterPowerRelationRepository).insert(any(ElectricMeterPowerRelationEntity.class));
        verify(electricMeterPowerConsumeRecordRepository, never()).getMeterLastConsumeRecord(anyInt());
        verify(lock).unlock();
    }

    /**
     * 测试首次上报数据的情况
     */
    @Test
    void testSavePowerRecord_FirstReport() {
        // Given
        when(lockTemplate.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
        when(electricMeterPowerRecordRepository.insert(any(ElectricMeterPowerRecordEntity.class))).thenReturn(1);
        when(electricMeterPowerRelationRepository.insert(any(ElectricMeterPowerRelationEntity.class))).thenReturn(1);
        when(electricMeterPowerConsumeRecordRepository.getMeterLastConsumeRecord(1)).thenReturn(null);

        // 模拟只有一条记录（当前插入的记录）
        when(electricMeterPowerRecordRepository.findRecordList(any(ElectricMeterPowerRecordQo.class)))
                .thenReturn(Collections.singletonList(new ElectricMeterPowerRecordEntity()));
        when(spaceService.getDetail(1)).thenReturn(spaceBo);
        when(organizationService.getDetail(1)).thenReturn(organizationBo);

        // When
        assertDoesNotThrow(() -> electricMeterConsumeService.savePowerRecord(testDto));

        // Then
        verify(electricMeterPowerRecordRepository).insert(any(ElectricMeterPowerRecordEntity.class));
        verify(electricMeterPowerRelationRepository).insert(any(ElectricMeterPowerRelationEntity.class));
        verify(electricMeterPowerConsumeRecordRepository, never()).insert(any(ElectricMeterPowerConsumeRecordEntity.class));
        verify(lock).unlock();
    }

    /**
     * 测试非预付费电表的情况
     */
    @Test
    void testSavePowerRecord_NonPrepay() {
        // Given
        meterDetailDto.setIsPrepay(false);
        when(lockTemplate.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
        when(electricMeterPowerRecordRepository.insert(any(ElectricMeterPowerRecordEntity.class))).thenReturn(1);
        when(electricMeterPowerRelationRepository.insert(any(ElectricMeterPowerRelationEntity.class))).thenReturn(1);

        ElectricMeterPowerConsumeRecordEntity lastConsumeRecord = new ElectricMeterPowerConsumeRecordEntity()
                .setAccountId(1)
                .setEndRecordId(100)
                .setMeterConsumeTime(LocalDateTime.now().minusHours(1));
        when(electricMeterPowerConsumeRecordRepository.getMeterLastConsumeRecord(1)).thenReturn(lastConsumeRecord);

        ElectricMeterPowerRecordEntity lastPowerRecord = new ElectricMeterPowerRecordEntity()
                .setPower(BigDecimal.valueOf(800))
                .setRecordTime(LocalDateTime.now().minusHours(1));
        when(electricMeterPowerRecordRepository.selectById(100)).thenReturn(lastPowerRecord);
        when(electricMeterPowerConsumeRecordRepository.insert(any(ElectricMeterPowerConsumeRecordEntity.class))).thenReturn(1);
        when(spaceService.getDetail(1)).thenReturn(spaceBo);
        when(organizationService.getDetail(1)).thenReturn(organizationBo);

        // When
        assertDoesNotThrow(() -> electricMeterConsumeService.savePowerRecord(testDto));

        // Then
        verify(electricMeterPowerConsumeRecordRepository).insert(any(ElectricMeterPowerConsumeRecordEntity.class));
        verify(balanceService, never()).topUp(any());
        verify(electricMeterBalanceConsumeRecordRepository, never()).insert(any(ElectricMeterBalanceConsumeRecordEntity.class));
        verify(lock).unlock();
    }

    /**
     * 测试包月账户类型的情况
     */
    @Test
    void testSavePowerRecord_MonthlyAccount() {
        // Given
        testDto.setElectricAccountType(ElectricAccountTypeEnum.MONTHLY);
        when(lockTemplate.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
        when(electricMeterPowerRecordRepository.insert(any(ElectricMeterPowerRecordEntity.class))).thenReturn(1);
        when(electricMeterPowerRelationRepository.insert(any(ElectricMeterPowerRelationEntity.class))).thenReturn(1);

        ElectricMeterPowerConsumeRecordEntity lastConsumeRecord = new ElectricMeterPowerConsumeRecordEntity()
                .setAccountId(1)
                .setEndRecordId(100)
                .setMeterConsumeTime(LocalDateTime.now().minusHours(1));
        when(electricMeterPowerConsumeRecordRepository.getMeterLastConsumeRecord(1)).thenReturn(lastConsumeRecord);

        ElectricMeterPowerRecordEntity lastPowerRecord = new ElectricMeterPowerRecordEntity()
                .setPower(BigDecimal.valueOf(800))
                .setRecordTime(LocalDateTime.now().minusHours(1));
        when(electricMeterPowerRecordRepository.selectById(100)).thenReturn(lastPowerRecord);
        when(electricMeterPowerConsumeRecordRepository.insert(any(ElectricMeterPowerConsumeRecordEntity.class))).thenReturn(1);
        when(spaceService.getDetail(1)).thenReturn(spaceBo);
        when(organizationService.getDetail(1)).thenReturn(organizationBo);

        // When
        assertDoesNotThrow(() -> electricMeterConsumeService.savePowerRecord(testDto));

        // Then
        verify(electricMeterPowerConsumeRecordRepository).insert(any(ElectricMeterPowerConsumeRecordEntity.class));
        verify(balanceService, never()).topUp(any());
        verify(electricMeterBalanceConsumeRecordRepository, never()).insert(any(ElectricMeterBalanceConsumeRecordEntity.class));
        verify(lock).unlock();
    }

    /**
     * 测试阶梯电价计算的情况
     */
    @Test
    void testSavePowerRecord_StepPrice() {
        // Given
        pricePlanDetailBo.setIsStep(true);
        StepPriceBo stepPrice1 = new StepPriceBo().setStart(BigDecimal.ZERO).setEnd(BigDecimal.valueOf(500)).setValue(BigDecimal.valueOf(0.8));
        StepPriceBo stepPrice2 = new StepPriceBo().setStart(BigDecimal.valueOf(500)).setEnd(null).setValue(BigDecimal.valueOf(1.2));
        pricePlanDetailBo.setStepPrices(List.of(stepPrice1, stepPrice2));

        when(lockTemplate.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
        when(electricMeterPowerRecordRepository.insert(any(ElectricMeterPowerRecordEntity.class))).thenReturn(1);
        when(electricMeterPowerRelationRepository.insert(any(ElectricMeterPowerRelationEntity.class))).thenReturn(1);

        ElectricMeterPowerConsumeRecordEntity lastConsumeRecord = new ElectricMeterPowerConsumeRecordEntity()
                .setAccountId(1)
                .setEndRecordId(100)
                .setMeterConsumeTime(LocalDateTime.now().minusHours(1));
        when(electricMeterPowerConsumeRecordRepository.getMeterLastConsumeRecord(1)).thenReturn(lastConsumeRecord);

        ElectricMeterPowerRecordEntity lastPowerRecord = new ElectricMeterPowerRecordEntity()
                .setPower(BigDecimal.valueOf(800))
                .setRecordTime(LocalDateTime.now().minusHours(1));
        when(electricMeterPowerRecordRepository.selectById(100)).thenReturn(lastPowerRecord);
        when(electricMeterPowerConsumeRecordRepository.insert(any(ElectricMeterPowerConsumeRecordEntity.class))).thenReturn(1);
        when(electricPricePlanService.getDetail(1)).thenReturn(pricePlanDetailBo);
        when(balanceService.query(any(BalanceQueryDto.class))).thenReturn(balanceBo);
        when(electricMeterBalanceConsumeRecordRepository.insert(any(ElectricMeterBalanceConsumeRecordEntity.class))).thenReturn(1);
        when(spaceService.getDetail(1)).thenReturn(spaceBo);
        when(organizationService.getDetail(1)).thenReturn(organizationBo);

        // When
        assertDoesNotThrow(() -> electricMeterConsumeService.savePowerRecord(testDto));

        // Then
        verify(electricPricePlanService).getDetail(1);
        verify(balanceService).deduct(any(BalanceDto.class));
        verify(electricMeterBalanceConsumeRecordRepository).insert(any(ElectricMeterBalanceConsumeRecordEntity.class));
        verify(lock).unlock();
    }

    /**
     * 测试记录时间早于上次消费时间的情况
     */
    @Test
    void testSavePowerRecord_RecordTimeBeforeLastConsume() {
        // Given
        when(lockTemplate.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
        when(electricMeterPowerRecordRepository.insert(any(ElectricMeterPowerRecordEntity.class))).thenReturn(1);
        when(electricMeterPowerRelationRepository.insert(any(ElectricMeterPowerRelationEntity.class))).thenReturn(1);

        // 模拟上次消费时间晚于当前记录时间
        ElectricMeterPowerConsumeRecordEntity lastConsumeRecord = new ElectricMeterPowerConsumeRecordEntity()
                .setAccountId(1)
                .setEndRecordId(100)
                .setMeterConsumeTime(LocalDateTime.now().plusHours(1)); // 未来时间
        when(electricMeterPowerConsumeRecordRepository.getMeterLastConsumeRecord(1)).thenReturn(lastConsumeRecord);

        when(spaceService.getDetail(1)).thenReturn(spaceBo);
        when(organizationService.getDetail(1)).thenReturn(organizationBo);

        // When
        assertDoesNotThrow(() -> electricMeterConsumeService.savePowerRecord(testDto));

        // Then
        verify(electricMeterPowerRecordRepository).insert(any(ElectricMeterPowerRecordEntity.class));
        verify(electricMeterPowerRelationRepository).insert(any(ElectricMeterPowerRelationEntity.class));
        verify(electricMeterPowerConsumeRecordRepository, never()).insert(any(ElectricMeterPowerConsumeRecordEntity.class));
        verify(lock).unlock();
    }

    /**
     * 测试阶梯电价计算异常的情况
     */
    @Test
    void testSavePowerRecord_StepPriceException() {
        // Given
        pricePlanDetailBo.setIsStep(true);
        // 设置一个无法匹配当前用电量的阶梯价格
        StepPriceBo stepPrice = new StepPriceBo().setStart(BigDecimal.valueOf(2000)).setEnd(BigDecimal.valueOf(3000)).setValue(BigDecimal.valueOf(1.5));
        pricePlanDetailBo.setStepPrices(Collections.singletonList(stepPrice));

        when(lockTemplate.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
        when(electricMeterPowerRecordRepository.insert(any(ElectricMeterPowerRecordEntity.class))).thenReturn(1);
        when(electricMeterPowerRelationRepository.insert(any(ElectricMeterPowerRelationEntity.class))).thenReturn(1);

        ElectricMeterPowerConsumeRecordEntity lastConsumeRecord = new ElectricMeterPowerConsumeRecordEntity()
                .setAccountId(1)
                .setEndRecordId(100)
                .setMeterConsumeTime(LocalDateTime.now().minusHours(1));
        when(electricMeterPowerConsumeRecordRepository.getMeterLastConsumeRecord(1)).thenReturn(lastConsumeRecord);

        ElectricMeterPowerRecordEntity lastPowerRecord = new ElectricMeterPowerRecordEntity()
                .setPower(BigDecimal.valueOf(800))
                .setRecordTime(LocalDateTime.now().minusHours(1));
        when(electricMeterPowerRecordRepository.selectById(100)).thenReturn(lastPowerRecord);
        when(electricMeterPowerConsumeRecordRepository.insert(any(ElectricMeterPowerConsumeRecordEntity.class))).thenReturn(1);
        when(electricPricePlanService.getDetail(1)).thenReturn(pricePlanDetailBo);
        when(balanceService.query(any(BalanceQueryDto.class))).thenReturn(balanceBo);
        when(electricMeterBalanceConsumeRecordRepository.insert(any(ElectricMeterBalanceConsumeRecordEntity.class))).thenReturn(1);
        when(spaceService.getDetail(1)).thenReturn(spaceBo);
        when(organizationService.getDetail(1)).thenReturn(organizationBo);

        // When
        assertDoesNotThrow(() -> electricMeterConsumeService.savePowerRecord(testDto));

        // Then - 验证使用默认倍率
        ArgumentCaptor<ElectricMeterBalanceConsumeRecordEntity> captor = ArgumentCaptor.forClass(ElectricMeterBalanceConsumeRecordEntity.class);
        verify(electricMeterBalanceConsumeRecordRepository).insert(captor.capture());
        assertEquals(BigDecimal.valueOf(1.0).setScale(8, RoundingMode.DOWN), captor.getValue().getPriceHigh());
        verify(balanceService).deduct(any(BalanceDto.class));
        verify(lock).unlock();
    }

    /**
     * 测试阶梯电价计算时 historyPowerOffset 为 0 的行为保持一致
     */
    @Test
    void testCalculatePriceRate_HistoryOffsetZero() {
        pricePlanDetailBo.setIsStep(true);
        StepPriceBo normalStep = new StepPriceBo()
                .setStart(BigDecimal.ZERO)
                .setEnd(BigDecimal.valueOf(500))
                .setValue(BigDecimal.valueOf(0.9));
        StepPriceBo highStep = new StepPriceBo()
                .setStart(BigDecimal.valueOf(500))
                .setEnd(null)
                .setValue(BigDecimal.valueOf(1.1));
        pricePlanDetailBo.setStepPrices(List.of(normalStep, highStep));

        ElectricMeterPowerConsumeRecordEntity consumeRecord = new ElectricMeterPowerConsumeRecordEntity()
                .setEndPower(BigDecimal.valueOf(400));
        meterDetailDto.setStepStartValue(BigDecimal.valueOf(100));
        meterDetailDto.setHistoryPowerOffset(BigDecimal.ZERO);

        BigDecimal priceRate = ReflectionTestUtils.invokeMethod(
                electricMeterConsumeService,
                "calculatePriceRate",
                testDto,
                consumeRecord,
                pricePlanDetailBo);

        assertEquals(BigDecimal.valueOf(0.9), priceRate);
    }

    /**
     * 测试 historyPowerOffset 正常推进阶梯
     */
    @Test
    void testCalculatePriceRate_HistoryOffsetApplied() {
        pricePlanDetailBo.setIsStep(true);
        StepPriceBo normalStep = new StepPriceBo()
                .setStart(BigDecimal.ZERO)
                .setEnd(BigDecimal.valueOf(500))
                .setValue(BigDecimal.valueOf(0.9));
        StepPriceBo highStep = new StepPriceBo()
                .setStart(BigDecimal.valueOf(500))
                .setEnd(null)
                .setValue(BigDecimal.valueOf(1.1));
        pricePlanDetailBo.setStepPrices(List.of(normalStep, highStep));

        ElectricMeterPowerConsumeRecordEntity consumeRecord = new ElectricMeterPowerConsumeRecordEntity()
                .setEndPower(BigDecimal.valueOf(400));
        meterDetailDto.setStepStartValue(BigDecimal.valueOf(100));
        meterDetailDto.setHistoryPowerOffset(BigDecimal.valueOf(250));

        BigDecimal priceRate = ReflectionTestUtils.invokeMethod(
                electricMeterConsumeService,
                "calculatePriceRate",
                testDto,
                consumeRecord,
                pricePlanDetailBo);

        assertEquals(BigDecimal.valueOf(1.1), priceRate);
    }

    /**
     * 测试不同账户ID的上次消费记录情况
     */
    @Test
    void testSavePowerRecord_DifferentAccountId() {
        // Given
        when(lockTemplate.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
        when(electricMeterPowerRecordRepository.insert(any(ElectricMeterPowerRecordEntity.class))).thenReturn(1);
        when(electricMeterPowerRelationRepository.insert(any(ElectricMeterPowerRelationEntity.class))).thenReturn(1);

        // 模拟上次消费记录的账户ID与当前不同
        ElectricMeterPowerConsumeRecordEntity lastConsumeRecord = new ElectricMeterPowerConsumeRecordEntity()
                .setAccountId(999) // 不同的账户ID
                .setEndRecordId(100)
                .setMeterConsumeTime(LocalDateTime.now().minusHours(1));
        when(electricMeterPowerConsumeRecordRepository.getMeterLastConsumeRecord(1)).thenReturn(lastConsumeRecord);

        // 模拟查询历史记录，返回两条记录
        List<ElectricMeterPowerRecordEntity> recordList = List.of(
                new ElectricMeterPowerRecordEntity().setPower(BigDecimal.valueOf(1000)),
                new ElectricMeterPowerRecordEntity().setPower(BigDecimal.valueOf(800)).setRecordTime(LocalDateTime.now().minusHours(1))
        );
        when(electricMeterPowerRecordRepository.findRecordList(any(ElectricMeterPowerRecordQo.class))).thenReturn(recordList);
        when(electricMeterPowerConsumeRecordRepository.insert(any(ElectricMeterPowerConsumeRecordEntity.class))).thenReturn(1);
        when(electricPricePlanService.getDetail(1)).thenReturn(pricePlanDetailBo);
        when(balanceService.query(any(BalanceQueryDto.class))).thenReturn(balanceBo);
        when(electricMeterBalanceConsumeRecordRepository.insert(any(ElectricMeterBalanceConsumeRecordEntity.class))).thenReturn(1);
        when(spaceService.getDetail(1)).thenReturn(spaceBo);
        when(organizationService.getDetail(1)).thenReturn(organizationBo);

        // When
        assertDoesNotThrow(() -> electricMeterConsumeService.savePowerRecord(testDto));

        // Then
        verify(electricMeterPowerRecordRepository).findRecordList(any(ElectricMeterPowerRecordQo.class));
        verify(electricMeterPowerConsumeRecordRepository).insert(any(ElectricMeterPowerConsumeRecordEntity.class));
        verify(balanceService).deduct(any(BalanceDto.class));
        verify(lock).unlock();
    }

    /**
     * 测试账户类型为ACCOUNT的情况
     */
    @Test
    void testSavePowerRecord_AccountType() {
        // Given
        testDto.setElectricAccountType(ElectricAccountTypeEnum.MERGED);
        when(lockTemplate.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
        when(electricMeterPowerRecordRepository.insert(any(ElectricMeterPowerRecordEntity.class))).thenReturn(1);
        when(electricMeterPowerRelationRepository.insert(any(ElectricMeterPowerRelationEntity.class))).thenReturn(1);

        ElectricMeterPowerConsumeRecordEntity lastConsumeRecord = new ElectricMeterPowerConsumeRecordEntity()
                .setAccountId(1)
                .setEndRecordId(100)
                .setMeterConsumeTime(LocalDateTime.now().minusHours(1));
        when(electricMeterPowerConsumeRecordRepository.getMeterLastConsumeRecord(1)).thenReturn(lastConsumeRecord);

        ElectricMeterPowerRecordEntity lastPowerRecord = new ElectricMeterPowerRecordEntity()
                .setPower(BigDecimal.valueOf(800))
                .setRecordTime(LocalDateTime.now().minusHours(1));
        when(electricMeterPowerRecordRepository.selectById(100)).thenReturn(lastPowerRecord);
        when(electricMeterPowerConsumeRecordRepository.insert(any(ElectricMeterPowerConsumeRecordEntity.class))).thenReturn(1);
        when(electricPricePlanService.getDetail(1)).thenReturn(pricePlanDetailBo);
        when(balanceService.query(any(BalanceQueryDto.class))).thenReturn(balanceBo);
        when(electricMeterBalanceConsumeRecordRepository.insert(any(ElectricMeterBalanceConsumeRecordEntity.class))).thenReturn(1);
        when(spaceService.getDetail(1)).thenReturn(spaceBo);
        when(organizationService.getDetail(1)).thenReturn(organizationBo);

        // When
        assertDoesNotThrow(() -> electricMeterConsumeService.savePowerRecord(testDto));

        // Then
        verify(balanceService).deduct(argThat(dto ->
                BalanceTypeEnum.ACCOUNT.equals(dto.getBalanceType()) &&
                        dto.getBalanceRelationId().equals(1)
        ));
        verify(lock).unlock();
    }

    /**
     * 测试空间信息为null的情况
     */
    @Test
    void testSavePowerRecord_NullSpaceInfo() {
        // Given
        when(lockTemplate.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
        when(electricMeterPowerRecordRepository.insert(any(ElectricMeterPowerRecordEntity.class))).thenReturn(1);
        when(electricMeterPowerRelationRepository.insert(any(ElectricMeterPowerRelationEntity.class))).thenReturn(1);
        when(spaceService.getDetail(1)).thenReturn(null); // 返回null
        when(organizationService.getDetail(1)).thenReturn(organizationBo);

        // When
        assertDoesNotThrow(() -> electricMeterConsumeService.savePowerRecord(testDto));

        // Then
        verify(electricMeterPowerRecordRepository).insert(any(ElectricMeterPowerRecordEntity.class));
        verify(electricMeterPowerRelationRepository).insert(any(ElectricMeterPowerRelationEntity.class));
        verify(lock).unlock();
    }

    /**
     * 测试组织信息为null的情况
     */
    @Test
    void testSavePowerRecord_NullOrganizationInfo() {
        // Given
        when(lockTemplate.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
        when(electricMeterPowerRecordRepository.insert(any(ElectricMeterPowerRecordEntity.class))).thenReturn(1);
        when(electricMeterPowerRelationRepository.insert(any(ElectricMeterPowerRelationEntity.class))).thenReturn(1);
        when(spaceService.getDetail(1)).thenReturn(spaceBo);
        when(organizationService.getDetail(1)).thenReturn(null); // 返回null

        // When
        assertDoesNotThrow(() -> electricMeterConsumeService.savePowerRecord(testDto));

        // Then
        verify(electricMeterPowerRecordRepository).insert(any(ElectricMeterPowerRecordEntity.class));
        verify(electricMeterPowerRelationRepository).insert(any(ElectricMeterPowerRelationEntity.class));
        verify(lock).unlock();
    }

    /**
     * 测试价格计划为null的情况
     */
    @Test
    void testSavePowerRecord_NullPricePlan() {
        // Given
        meterDetailDto.setPricePlanId(null);
        when(lockTemplate.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
        when(electricMeterPowerRecordRepository.insert(any(ElectricMeterPowerRecordEntity.class))).thenReturn(1);
        when(electricMeterPowerRelationRepository.insert(any(ElectricMeterPowerRelationEntity.class))).thenReturn(1);
        when(spaceService.getDetail(1)).thenReturn(spaceBo);
        when(organizationService.getDetail(1)).thenReturn(organizationBo);

        // When
        assertDoesNotThrow(() -> electricMeterConsumeService.savePowerRecord(testDto));

        // Then
        verify(electricMeterPowerRecordRepository).insert(any(ElectricMeterPowerRecordEntity.class));
        verify(electricMeterPowerRelationRepository).insert(any(ElectricMeterPowerRelationEntity.class));
        verify(electricPricePlanService, never()).getDetail(anyInt());
        verify(lock).unlock();
    }
}
