package info.zhihui.ems.business.account.service.impl;

import info.zhihui.ems.business.account.bo.AccountBo;
import info.zhihui.ems.business.account.dto.AccountConfigUpdateDto;
import info.zhihui.ems.business.account.dto.AccountMetersOpenDto;
import info.zhihui.ems.business.account.dto.CancelAccountDto;
import info.zhihui.ems.business.account.dto.CancelAccountResponseDto;
import info.zhihui.ems.business.account.dto.OpenAccountDto;
import info.zhihui.ems.business.account.entity.AccountCancelRecordEntity;
import info.zhihui.ems.business.account.entity.AccountEntity;
import info.zhihui.ems.business.account.enums.CleanBalanceTypeEnum;
import info.zhihui.ems.business.account.mapper.AccountInfoMapper;
import info.zhihui.ems.business.account.mapper.AccountManagerMapper;
import info.zhihui.ems.business.account.repository.AccountCancelRecordRepository;
import info.zhihui.ems.business.account.repository.AccountRepository;
import info.zhihui.ems.business.account.service.AccountInfoService;
import info.zhihui.ems.business.device.bo.ElectricMeterBo;
import info.zhihui.ems.business.device.dto.*;
import info.zhihui.ems.business.device.service.ElectricMeterInfoService;
import info.zhihui.ems.business.device.service.ElectricMeterManagerService;
import info.zhihui.ems.business.finance.bo.BalanceBo;
import info.zhihui.ems.business.finance.dto.BalanceQueryDto;
import info.zhihui.ems.business.finance.dto.order.creation.TerminationOrderCreationInfoDto;
import info.zhihui.ems.business.finance.enums.PaymentChannelEnum;
import info.zhihui.ems.business.finance.service.balance.BalanceService;
import info.zhihui.ems.business.finance.service.consume.AccountConsumeService;
import info.zhihui.ems.business.finance.service.order.core.OrderService;
import info.zhihui.ems.business.plan.bo.ElectricPricePlanDetailBo;
import info.zhihui.ems.business.plan.bo.WarnPlanBo;
import info.zhihui.ems.business.plan.service.ElectricPricePlanService;
import info.zhihui.ems.business.plan.service.WarnPlanService;
import info.zhihui.ems.common.enums.BalanceTypeEnum;
import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import info.zhihui.ems.common.enums.OwnerTypeEnum;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.components.context.RequestContext;
import info.zhihui.ems.components.lock.core.LockTemplate;
import info.zhihui.ems.foundation.organization.bo.OrganizationBo;
import info.zhihui.ems.foundation.organization.service.OrganizationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AccountManagerService单元测试
 *
 * @author jerryxiaosa
 */
@ExtendWith(MockitoExtension.class)
class AccountManagerServiceImplTest {

    @Mock
    private AccountInfoService accountInfoService;

    @Mock
    private AccountConsumeService accountConsumeService;

    @Mock
    private AccountManagerMapper mapper;

    @Mock
    private AccountInfoMapper infoMapper;

    @Mock
    private AccountRepository repository;

    @Mock
    private ElectricMeterManagerService electricMeterManagerService;

    @Mock
    private BalanceService balanceService;

    @Mock
    private OrderService orderService;

    @Mock
    private ElectricPricePlanService electricPricePlanService;

    @Mock
    private WarnPlanService warnPlanService;

    @Mock
    private OrganizationService organizationService;

    @Mock
    private AccountCancelRecordRepository cancelRecordRepository;

    @Mock
    private ElectricMeterInfoService electricMeterInfoService;

    @Mock
    private LockTemplate lockTemplate;

    @Mock
    private RequestContext requestContext;

    @Mock
    private Lock lock;

    @InjectMocks
    private AccountManagerServiceImpl accountManagerService;

    @Test
    void testOpenAccount_LockFailed() {
        // Given
        OpenAccountDto openAccountDto = new OpenAccountDto()
                .setElectricAccountType(ElectricAccountTypeEnum.MONTHLY)
                .setOwnerId(1)
                .setOwnerType(OwnerTypeEnum.ENTERPRISE)
                .setMonthlyPayAmount(BigDecimal.valueOf(100))
                .setElectricMeterList(List.of(new MeterOpenDetailDto()
                        .setMeterId(1)
                        ));

        when(lockTemplate.getLock(any(String.class))).thenReturn(lock);
        when(lock.tryLock()).thenReturn(false);

        // When & Then
        assertThrows(BusinessRuntimeException.class, () -> accountManagerService.openAccount(openAccountDto));
    }

    @Test
    void testOpenAccount_Success_FirstTimeMonthly() {
        // Given
        OpenAccountDto openAccountDto = new OpenAccountDto()
                .setElectricAccountType(ElectricAccountTypeEnum.MONTHLY)
                .setOwnerId(1)
                .setOwnerType(OwnerTypeEnum.ENTERPRISE)
                .setMonthlyPayAmount(BigDecimal.valueOf(100))
                .setElectricMeterList(List.of(new MeterOpenDetailDto()
                        .setMeterId(1)
                        ));

        // Mock lock operations
        when(lockTemplate.getLock(any(String.class))).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
        doNothing().when(lock).unlock();

        // Mock first time account
        when(accountInfoService.findList(any())).thenReturn(Collections.emptyList());
        when(organizationService.getDetail(anyInt())).thenReturn(new OrganizationBo());

        // Mock meter operations
        doNothing().when(electricMeterManagerService).openMeterAccount(any());

        AccountEntity savedEntity = new AccountEntity().setId(1);
        AccountBo savedAccountBo = new AccountBo()
                .setId(1)
                .setElectricAccountType(ElectricAccountTypeEnum.MONTHLY)
                .setMonthlyPayAmount(BigDecimal.valueOf(100));

        when(mapper.openAccountDtoToEntity(any())).thenReturn(savedEntity);
        when(infoMapper.entityToBo(any())).thenReturn(savedAccountBo);
        when(repository.insert(any(AccountEntity.class))).thenReturn(1);
        doNothing().when(accountConsumeService).monthlyConsume(any());
        doNothing().when(balanceService).initAccountBalance(1);

        // When
        Integer accountId = accountManagerService.openAccount(openAccountDto);

        // Then
        verify(lock).tryLock();
        verify(lock).unlock();
        verify(accountInfoService).findList(any());
        verify(repository).insert(any(AccountEntity.class));
        verify(accountConsumeService).monthlyConsume(any());
        verify(electricMeterManagerService).openMeterAccount(any());
        assertThat(accountId).isEqualTo(1);
        verify(organizationService).getDetail(1);
        verifyNoInteractions(electricPricePlanService, warnPlanService);
    }

    @Test
    void testOpenAccount_Success_FirstTimeQuantity() {
        // Given
        OpenAccountDto openAccountDto = new OpenAccountDto()
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY)
                .setOwnerId(1)
                .setOwnerType(OwnerTypeEnum.ENTERPRISE)
                .setWarnPlanId(1)
                .setElectricPricePlanId(1)
                .setElectricMeterList(List.of(new MeterOpenDetailDto()
                        .setMeterId(1)
                        ));

        // Mock lock operations
        when(lockTemplate.getLock(any(String.class))).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
        doNothing().when(lock).unlock();

        // Mock first time account
        when(accountInfoService.findList(any())).thenReturn(Collections.emptyList());
        when(organizationService.getDetail(anyInt())).thenReturn(new OrganizationBo());

        // Mock meter operations
        doNothing().when(electricMeterManagerService).openMeterAccount(any());
        when(electricPricePlanService.getDetail(1)).thenReturn(new ElectricPricePlanDetailBo());
        when(warnPlanService.getDetail(1)).thenReturn(new WarnPlanBo());

        AccountEntity savedEntity = new AccountEntity().setId(1);
        AccountBo savedAccountBo = new AccountBo()
                .setId(1)
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY)
                .setElectricPricePlanId(1)
                .setWarnPlanId(1);

        when(mapper.openAccountDtoToEntity(any())).thenReturn(savedEntity);
        when(infoMapper.entityToBo(any())).thenReturn(savedAccountBo);
        when(repository.insert(any(AccountEntity.class))).thenReturn(1);
        doNothing().when(balanceService).initAccountBalance(1);

        // When
        Integer accountId = accountManagerService.openAccount(openAccountDto);

        // Then
        verify(lock).tryLock();
        verify(lock).unlock();
        verify(accountInfoService).findList(any());
        verify(repository).insert(any(AccountEntity.class));
        verify(accountConsumeService, never()).monthlyConsume(any());
        verify(electricMeterManagerService).openMeterAccount(any());
        assertThat(accountId).isEqualTo(1);
        verify(organizationService).getDetail(1);
        verify(electricPricePlanService).getDetail(1);
        verify(warnPlanService).getDetail(1);
    }

    @Test
    void testOpenAccount_Success_FirstTimeMerged() {
        // Given
        OpenAccountDto openAccountDto = new OpenAccountDto()
                .setElectricAccountType(ElectricAccountTypeEnum.MERGED)
                .setOwnerId(1)
                .setOwnerType(OwnerTypeEnum.ENTERPRISE)
                .setElectricPricePlanId(3)
                .setWarnPlanId(1)
                .setElectricMeterList(List.of(new MeterOpenDetailDto()
                        .setMeterId(1)
                        ));

        when(lockTemplate.getLock(any(String.class))).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
        doNothing().when(lock).unlock();
        when(accountInfoService.findList(any())).thenReturn(Collections.emptyList());
        when(organizationService.getDetail(anyInt())).thenReturn(new OrganizationBo());
        doNothing().when(electricMeterManagerService).openMeterAccount(any());
        when(electricPricePlanService.getDetail(3)).thenReturn(new ElectricPricePlanDetailBo());
        when(warnPlanService.getDetail(1)).thenReturn(new WarnPlanBo());

        AccountEntity savedEntity = new AccountEntity().setId(1);
        AccountBo savedAccountBo = new AccountBo()
                .setId(1)
                .setElectricAccountType(ElectricAccountTypeEnum.MERGED)
                .setWarnPlanId(1)
                .setElectricPricePlanId(3);

        when(mapper.openAccountDtoToEntity(any())).thenReturn(savedEntity);
        when(infoMapper.entityToBo(any())).thenReturn(savedAccountBo);
        when(repository.insert(any(AccountEntity.class))).thenReturn(1);
        doNothing().when(balanceService).initAccountBalance(1);

        // When
        Integer accountId = accountManagerService.openAccount(openAccountDto);

        // Then
        verify(lock).tryLock();
        verify(lock).unlock();
        verify(accountInfoService).findList(any());
        verify(repository).insert(any(AccountEntity.class));
        verify(accountConsumeService, never()).monthlyConsume(any());
        verify(electricMeterManagerService).openMeterAccount(any());
        assertThat(accountId).isEqualTo(1);
        verify(organizationService).getDetail(1);
        verify(electricPricePlanService).getDetail(3);
    }

    @Test
    void testAppendMeters_Success() {
        // Given
        AccountMetersOpenDto accountMetersOpenDto = new AccountMetersOpenDto()
                .setAccountId(1)
                .setElectricMeterList(List.of(new MeterOpenDetailDto()
                        .setMeterId(10)
                        ));

        AccountBo accountBo = new AccountBo()
                .setId(1)
                .setOwnerId(100)
                .setOwnerType(OwnerTypeEnum.ENTERPRISE)
                .setOwnerName("测试企业")
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY)
                .setElectricPricePlanId(5)
                .setWarnPlanId(9);

        when(lockTemplate.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
        doNothing().when(lock).unlock();
        when(accountInfoService.getById(1)).thenReturn(accountBo);
        doNothing().when(electricMeterManagerService).openMeterAccount(any());

        // When
        accountManagerService.appendMeters(accountMetersOpenDto);

        // Then
        verify(lock).tryLock();
        verify(lock).unlock();
        ArgumentCaptor<MeterOpenDto> captor = ArgumentCaptor.forClass(MeterOpenDto.class);
        verify(electricMeterManagerService).openMeterAccount(captor.capture());
        MeterOpenDto meterOpenDto = captor.getValue();
        assertThat(meterOpenDto.getAccountId()).isEqualTo(1);
        assertThat(meterOpenDto.getOwnerId()).isEqualTo(100);
        assertThat(meterOpenDto.getOwnerType()).isEqualTo(OwnerTypeEnum.ENTERPRISE);
        assertThat(meterOpenDto.getOwnerName()).isEqualTo("测试企业");
        assertThat(meterOpenDto.getElectricAccountType()).isEqualTo(ElectricAccountTypeEnum.QUANTITY);
        assertThat(meterOpenDto.getElectricPricePlanId()).isEqualTo(5);
        assertThat(meterOpenDto.getWarnPlanId()).isEqualTo(9);
        assertThat(meterOpenDto.getMeterOpenDetail()).hasSize(1);
        assertThat(meterOpenDto.getMeterOpenDetail().get(0).getMeterId()).isEqualTo(10);
    }

    @Test
    void testAppendMeters_Exception_AccountNotFound() {
        AccountMetersOpenDto accountMetersOpenDto = new AccountMetersOpenDto()
                .setAccountId(1)
                .setElectricMeterList(List.of(new MeterOpenDetailDto().setMeterId(10)));

        when(lockTemplate.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
        doNothing().when(lock).unlock();
        when(accountInfoService.getById(1)).thenReturn(null);

        assertThrows(BusinessRuntimeException.class, () -> accountManagerService.appendMeters(accountMetersOpenDto));

        verify(lock).unlock();
        verify(electricMeterManagerService, never()).openMeterAccount(any());
    }

    @Test
    void testAppendMeters_Exception_MissingPricePlan() {
        AccountMetersOpenDto accountMetersOpenDto = new AccountMetersOpenDto()
                .setAccountId(1)
                .setElectricMeterList(List.of(new MeterOpenDetailDto().setMeterId(10)));

        AccountBo accountBo = new AccountBo()
                .setId(1)
                .setOwnerId(100)
                .setOwnerType(OwnerTypeEnum.ENTERPRISE)
                .setOwnerName("测试企业")
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY)
                .setWarnPlanId(9);

        when(lockTemplate.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
        doNothing().when(lock).unlock();
        when(accountInfoService.getById(1)).thenReturn(accountBo);

        assertThrows(BusinessRuntimeException.class, () -> accountManagerService.appendMeters(accountMetersOpenDto));

        verify(lock).unlock();
        verify(electricMeterManagerService, never()).openMeterAccount(any());
    }

    @Test
    void testAppendMeters_Exception_MissingWarnPlan() {
        AccountMetersOpenDto accountMetersOpenDto = new AccountMetersOpenDto()
                .setAccountId(1)
                .setElectricMeterList(List.of(new MeterOpenDetailDto().setMeterId(10)));

        AccountBo accountBo = new AccountBo()
                .setId(1)
                .setOwnerId(100)
                .setOwnerType(OwnerTypeEnum.ENTERPRISE)
                .setOwnerName("测试企业")
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY)
                .setElectricPricePlanId(5);

        when(lockTemplate.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
        doNothing().when(lock).unlock();
        when(accountInfoService.getById(1)).thenReturn(accountBo);

        assertThrows(BusinessRuntimeException.class, () -> accountManagerService.appendMeters(accountMetersOpenDto));

        verify(lock).unlock();
        verify(electricMeterManagerService, never()).openMeterAccount(any());
    }

    @Test
    void testUpdateAccountConfig_Success_Monthly() {
        when(lockTemplate.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
        doNothing().when(lock).unlock();

        AccountBo accountBo = new AccountBo()
                .setId(1)
                .setElectricAccountType(ElectricAccountTypeEnum.MONTHLY)
                .setMonthlyPayAmount(BigDecimal.valueOf(100));
        when(accountInfoService.getById(1)).thenReturn(accountBo);

        AccountConfigUpdateDto dto = new AccountConfigUpdateDto()
                .setAccountId(1)
                .setMonthlyPayAmount(new BigDecimal("180"));

        accountManagerService.updateAccountConfig(dto);

        ArgumentCaptor<AccountEntity> captor = ArgumentCaptor.forClass(AccountEntity.class);
        verify(repository).updateById(captor.capture());
        AccountEntity entity = captor.getValue();
        assertThat(entity.getId()).isEqualTo(1);
        assertThat(entity.getMonthlyPayAmount()).isEqualByComparingTo("180");
        verifyNoInteractions(electricPricePlanService, warnPlanService);
    }

    @Test
    void testUpdateAccountConfig_Success_Quantity() {
        when(lockTemplate.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
        doNothing().when(lock).unlock();

        AccountBo accountBo = new AccountBo()
                .setId(2)
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY)
                .setElectricPricePlanId(3)
                .setWarnPlanId(6);
        when(accountInfoService.getById(2)).thenReturn(accountBo);
        when(electricPricePlanService.getDetail(5)).thenReturn(new ElectricPricePlanDetailBo());
        when(warnPlanService.getDetail(9)).thenReturn(new WarnPlanBo());

        AccountConfigUpdateDto dto = new AccountConfigUpdateDto()
                .setAccountId(2)
                .setElectricPricePlanId(5)
                .setWarnPlanId(9);

        accountManagerService.updateAccountConfig( dto);

        ArgumentCaptor<AccountEntity> captor = ArgumentCaptor.forClass(AccountEntity.class);
        verify(repository).updateById(captor.capture());
        AccountEntity entity = captor.getValue();
        assertThat(entity.getElectricPricePlanId()).isEqualTo(5);
        assertThat(entity.getWarnPlanId()).isEqualTo(9);
    }

    @Test
    void testUpdateAccountConfig_Success_Merged() {
        when(lockTemplate.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
        doNothing().when(lock).unlock();

        AccountBo accountBo = new AccountBo()
                .setId(3)
                .setElectricAccountType(ElectricAccountTypeEnum.MERGED)
                .setElectricPricePlanId(4);
        when(accountInfoService.getById(3)).thenReturn(accountBo);
        when(electricPricePlanService.getDetail(8)).thenReturn(new ElectricPricePlanDetailBo());

        AccountConfigUpdateDto dto = new AccountConfigUpdateDto()
                .setAccountId(3)
                .setElectricPricePlanId(8);

        accountManagerService.updateAccountConfig(dto);

        ArgumentCaptor<AccountEntity> captor = ArgumentCaptor.forClass(AccountEntity.class);
        verify(repository).updateById(captor.capture());
        AccountEntity entity = captor.getValue();
        assertThat(entity.getElectricPricePlanId()).isEqualTo(8);
        verify(warnPlanService, never()).getDetail(anyInt());
    }

    @Test
    void testUpdateAccountConfig_NoChange_Monthly() {
        when(lockTemplate.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
        doNothing().when(lock).unlock();

        AccountBo accountBo = new AccountBo()
                .setId(7)
                .setElectricAccountType(ElectricAccountTypeEnum.MONTHLY)
                .setMonthlyPayAmount(new BigDecimal("100.00"));
        when(accountInfoService.getById(7)).thenReturn(accountBo);

        AccountConfigUpdateDto dto = new AccountConfigUpdateDto()
                .setAccountId(7)
                .setMonthlyPayAmount(new BigDecimal("100"));

        accountManagerService.updateAccountConfig(dto);

        verify(repository, never()).updateById(any(AccountEntity.class));
        verifyNoInteractions(electricPricePlanService, warnPlanService, electricMeterManagerService);
    }

    @Test
    void testUpdateAccountConfig_NoChange_Quantity() {
        when(lockTemplate.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
        doNothing().when(lock).unlock();

        AccountBo accountBo = new AccountBo()
                .setId(8)
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY)
                .setElectricPricePlanId(5)
                .setWarnPlanId(9);
        when(accountInfoService.getById(8)).thenReturn(accountBo);

        AccountConfigUpdateDto dto = new AccountConfigUpdateDto()
                .setAccountId(8)
                .setElectricPricePlanId(5)
                .setWarnPlanId(9);

        accountManagerService.updateAccountConfig(dto);

        verify(repository, never()).updateById(any(AccountEntity.class));
        verifyNoInteractions(electricPricePlanService, warnPlanService, electricMeterManagerService);
    }

    @Test
    void testUpdateAccountConfig_Error_InvalidForMonthly() {
        when(lockTemplate.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
        doNothing().when(lock).unlock();

        AccountBo accountBo = new AccountBo()
                .setId(4)
                .setElectricAccountType(ElectricAccountTypeEnum.MONTHLY)
                .setMonthlyPayAmount(BigDecimal.valueOf(80));
        when(accountInfoService.getById(4)).thenReturn(accountBo);

        AccountConfigUpdateDto dto = new AccountConfigUpdateDto()
                .setAccountId(4)
                .setElectricPricePlanId(1);

        assertThrows(BusinessRuntimeException.class,
                () -> accountManagerService.updateAccountConfig(dto));

        verify(repository, never()).updateById(any(AccountEntity.class));
    }

    @Test
    void testUpdateAccountConfig_Error_NoConfigProvided() {
        when(lockTemplate.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
        doNothing().when(lock).unlock();

        assertThrows(BusinessRuntimeException.class,
                () -> accountManagerService.updateAccountConfig(new AccountConfigUpdateDto().setAccountId(5)));
        verify(repository, never()).updateById(any(AccountEntity.class));
    }

    @Test
    void testUpdateAccountConfig_Error_AccountNotFound() {
        when(lockTemplate.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
        doNothing().when(lock).unlock();
        when(accountInfoService.getById(6)).thenReturn(null);

        AccountConfigUpdateDto dto = new AccountConfigUpdateDto().setAccountId(6).setElectricPricePlanId(1);

        assertThrows(BusinessRuntimeException.class,
                () -> accountManagerService.updateAccountConfig(dto));
        verify(repository, never()).updateById(any(AccountEntity.class));
    }

    @Test
    void testOpenAccount_Exception_ExistingAccount() {
        // Given
        OpenAccountDto openAccountDto = new OpenAccountDto()
                .setElectricAccountType(ElectricAccountTypeEnum.MONTHLY)
                .setOwnerId(1)
                .setOwnerType(OwnerTypeEnum.ENTERPRISE)
                .setMonthlyPayAmount(BigDecimal.valueOf(100))
                .setElectricMeterList(List.of(new MeterOpenDetailDto()
                        .setMeterId(1)
                        ));

        // Mock lock operations
        when(lockTemplate.getLock(any(String.class))).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
        doNothing().when(lock).unlock();

        // Mock existing account
        AccountBo existingAccount = new AccountBo()
                .setId(1)
                .setElectricAccountType(ElectricAccountTypeEnum.MONTHLY)
                .setOwnerType(OwnerTypeEnum.ENTERPRISE);
        when(accountInfoService.findList(any())).thenReturn(List.of(existingAccount));

        assertThrows(BusinessRuntimeException.class, () -> accountManagerService.openAccount(openAccountDto));

        // Then
        verify(lock).tryLock();
        verify(lock).unlock();
        verify(accountInfoService).findList(any());
        verify(repository, never()).insert(any(AccountEntity.class));
    }

    @Test
    void testOpenAccount_NonEnterpriseWithoutAreaId() {
        // Given
        OpenAccountDto openAccountDto = new OpenAccountDto()
                .setElectricAccountType(ElectricAccountTypeEnum.MONTHLY)
                .setOwnerId(1)
                .setOwnerType(OwnerTypeEnum.PERSONAL)
                .setMonthlyPayAmount(BigDecimal.valueOf(100))
                .setElectricMeterList(List.of(new MeterOpenDetailDto()
                        .setMeterId(1)
                        ));

        when(lockTemplate.getLock(any(String.class))).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
        doNothing().when(lock).unlock();
        when(accountInfoService.findList(any())).thenReturn(Collections.emptyList());

        // When & Then
        assertThrows(BusinessRuntimeException.class, () -> accountManagerService.openAccount(openAccountDto));

        verify(lock).unlock();
        verifyNoInteractions(electricPricePlanService, warnPlanService);
    }

    @Test
    void testOpenAccount_MonthlyWithoutMonthlyPayAmount() {
        // Given
        OpenAccountDto openAccountDto = new OpenAccountDto()
                .setElectricAccountType(ElectricAccountTypeEnum.MONTHLY)
                .setOwnerId(1)
                .setOwnerType(OwnerTypeEnum.ENTERPRISE)
                .setElectricMeterList(List.of(new MeterOpenDetailDto()
                        .setMeterId(1)
                        ));

        when(lockTemplate.getLock(any(String.class))).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
        doNothing().when(lock).unlock();
        when(accountInfoService.findList(any())).thenReturn(Collections.emptyList());

        // When & Then
        assertThrows(BusinessRuntimeException.class, () -> accountManagerService.openAccount(openAccountDto));

        verify(lock).unlock();
    }

    @Test
    void testOpenAccount_QuantityWithoutPricePlan() {
        // Given
        OpenAccountDto openAccountDto = new OpenAccountDto()
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY)
                .setOwnerId(1)
                .setOwnerType(OwnerTypeEnum.ENTERPRISE)
                .setElectricMeterList(List.of(new MeterOpenDetailDto()
                        .setMeterId(1)
                        ));

        when(lockTemplate.getLock(any(String.class))).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
        doNothing().when(lock).unlock();
        when(accountInfoService.findList(any())).thenReturn(Collections.emptyList());

        // When & Then
        assertThrows(BusinessRuntimeException.class, () -> accountManagerService.openAccount(openAccountDto));

        verify(lock).unlock();
        verify(organizationService).getDetail(1);
        verifyNoInteractions(electricPricePlanService, warnPlanService);
    }

    @Test
    void testOpenAccount_QuantityWithoutWarnPlan() {
        // Given
        OpenAccountDto openAccountDto = new OpenAccountDto()
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY)
                .setOwnerId(1)
                .setOwnerType(OwnerTypeEnum.ENTERPRISE)
                .setElectricPricePlanId(1)
                .setElectricMeterList(List.of(new MeterOpenDetailDto()
                        .setMeterId(1)
                        ));

        when(lockTemplate.getLock(any(String.class))).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
        doNothing().when(lock).unlock();
        when(accountInfoService.findList(any())).thenReturn(Collections.emptyList());

        // When & Then
        assertThrows(BusinessRuntimeException.class, () -> accountManagerService.openAccount(openAccountDto));

        verify(lock).unlock();
    }

    @Test
    void testOpenAccount_ExceptionHandling() {
        // Given
        OpenAccountDto openAccountDto = new OpenAccountDto()
                .setElectricAccountType(ElectricAccountTypeEnum.MONTHLY)
                .setOwnerId(1)
                .setOwnerType(OwnerTypeEnum.ENTERPRISE)
                .setMonthlyPayAmount(BigDecimal.valueOf(100))
                .setElectricMeterList(List.of(new MeterOpenDetailDto()
                        .setMeterId(1)
                        ));

        when(lockTemplate.getLock(any(String.class))).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
        doNothing().when(lock).unlock();
        when(accountInfoService.findList(any())).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(BusinessRuntimeException.class, () -> accountManagerService.openAccount(openAccountDto));

        verify(lock).unlock();
    }

    @Test
    void testCancelAccount_LockFailed() {
        // Given
        CancelAccountDto cancelAccountDto = new CancelAccountDto()
                .setAccountId(1)
                .setRemark("测试销户")
                .setMeterList(List.of(new MeterCancelDetailDto()
                        .setMeterId(1)));

        when(lockTemplate.getLock(any(String.class))).thenReturn(lock);
        when(lock.tryLock()).thenReturn(false);

        // When & Then
        assertThrows(BusinessRuntimeException.class, () -> accountManagerService.cancelAccount(cancelAccountDto));
    }

    @Test
    void testCancelAccount_Success_QuantityAccountRefund() {
        // Given
        CancelAccountDto cancelAccountDto = new CancelAccountDto()
                .setAccountId(1)
                .setRemark("按量计费销户退费")
                .setMeterList(List.of(new MeterCancelDetailDto()
                        .setMeterId(1)));

        AccountBo accountBo = new AccountBo()
                .setId(1)
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY)
                .setOwnerId(1)
                .setOwnerType(OwnerTypeEnum.ENTERPRISE)
                .setOwnerName("测试企业");

        ElectricMeterBo electricMeterBo = new ElectricMeterBo()
                .setId(1)
                .setAccountId(1);

        List<MeterCancelResultDto> meterCancelBalances = List.of(
                new MeterCancelResultDto().setMeterId(1).setBalance(BigDecimal.valueOf(100))
        );

        // Mock lock operations
        when(lockTemplate.getLock(any(String.class))).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
        doNothing().when(lock).unlock();

        // Mock account operations
        when(accountInfoService.getById(1)).thenReturn(accountBo);
        when(electricMeterInfoService.findList(new ElectricMeterQueryDto().setAccountId(1).setInIds(List.of(1))))
                .thenReturn(List.of(electricMeterBo));
        when(electricMeterManagerService.cancelMeterAccount(any())).thenReturn(meterCancelBalances);
        when(cancelRecordRepository.insert(any(AccountCancelRecordEntity.class))).thenReturn(1);
        when(repository.deleteById(1)).thenReturn(1);
        mockRequestContext();

        // When
        accountManagerService.cancelAccount(cancelAccountDto);

        // Then
        verify(lock).tryLock();
        verify(lock).unlock();
        verify(accountInfoService).getById(1);
        verify(electricMeterManagerService).cancelMeterAccount(argThat(dto ->
                dto.getAccountId().equals(1) &&
                        dto.getOwnerId().equals(1) &&
                        dto.getOwnerType().equals(OwnerTypeEnum.ENTERPRISE) &&
                        dto.getElectricAccountType().equals(ElectricAccountTypeEnum.QUANTITY) &&
                        dto.getMeterCloseDetail().size() == 1 &&
                        dto.getMeterCloseDetail().get(0).getMeterId().equals(1)
        ));
        verify(cancelRecordRepository).insert(ArgumentMatchers.<AccountCancelRecordEntity>argThat(entity ->
                entity.getAccountId().equals(1) &&
                        entity.getOwnerId().equals(1) &&
                        entity.getOwnerType().equals(OwnerTypeEnum.ENTERPRISE.getCode()) &&
                        entity.getElectricAccountType().equals(ElectricAccountTypeEnum.QUANTITY.getCode())
        ));
        verify(repository).deleteById(1);

        ArgumentCaptor<TerminationOrderCreationInfoDto> orderCaptor = ArgumentCaptor.forClass(TerminationOrderCreationInfoDto.class);
        verify(orderService, times(1)).createOrder(orderCaptor.capture());
        TerminationOrderCreationInfoDto orderDto = orderCaptor.getValue();

        assertThat(orderDto.getPaymentChannel()).isEqualTo(PaymentChannelEnum.OFFLINE);
        assertThat(orderDto.getOrderAmount()).isEqualByComparingTo("100");
        assertThat(orderDto.getTerminationInfo().getCancelNo()).isNotBlank();
        assertThat(orderDto.getTerminationInfo().getSettlementAmount()).isEqualByComparingTo("100");
        assertThat(orderDto.getTerminationInfo().getCloseReason()).isEqualTo(cancelAccountDto.getRemark());
        assertThat(orderDto.getTerminationInfo().getAccountId()).isEqualTo(1);
        assertThat(orderDto.getTerminationInfo().getOwnerId()).isEqualTo(1);
        assertThat(orderDto.getTerminationInfo().getOwnerType()).isEqualTo(OwnerTypeEnum.ENTERPRISE);
        assertThat(orderDto.getTerminationInfo().getOwnerName()).isEqualTo("测试企业");
        assertThat(orderDto.getTerminationInfo().getMeterIdList()).isEqualTo(List.of(1));
        assertThat(orderDto.getUserId()).isEqualTo(100);
        assertThat(orderDto.getUserPhone()).isEqualTo("13800000000");
        assertThat(orderDto.getUserRealName()).isEqualTo("李四");
        assertThat(orderDto.getThirdPartyUserId()).isEqualTo("100");
    }

    @Test
    void testCancelAccount_Success_QuantityAccountPay() {
        // Given
        CancelAccountDto cancelAccountDto = new CancelAccountDto()
                .setAccountId(1)
                .setRemark("按量计费销户补缴")
                .setMeterList(List.of(new MeterCancelDetailDto()
                        .setMeterId(1)));

        AccountBo accountBo = new AccountBo()
                .setId(1)
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY)
                .setOwnerId(1)
                .setOwnerType(OwnerTypeEnum.ENTERPRISE)
                .setOwnerName("测试企业");

        List<MeterCancelResultDto> meterCancelBalances = List.of(
                new MeterCancelResultDto().setMeterId(1).setBalance(BigDecimal.valueOf(-50))
        );

        // Mock lock operations
        when(lockTemplate.getLock(any(String.class))).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
        doNothing().when(lock).unlock();

        ElectricMeterBo electricMeterBo = new ElectricMeterBo()
                .setId(1)
                .setAccountId(1);
        ElectricMeterQueryDto query = new ElectricMeterQueryDto()
                .setAccountId(1)
                .setInIds(List.of(1));

        // Mock account operations
        when(accountInfoService.getById(1)).thenReturn(accountBo);
        when(electricMeterInfoService.findList(query)).thenReturn(List.of(electricMeterBo));
        when(electricMeterManagerService.cancelMeterAccount(any())).thenReturn(meterCancelBalances);
        when(cancelRecordRepository.insert(any(AccountCancelRecordEntity.class))).thenReturn(1);
        when(repository.deleteById(1)).thenReturn(1);
        mockRequestContext();

        // When
        accountManagerService.cancelAccount(cancelAccountDto);

        // Then
        verify(lock).tryLock();
        verify(lock).unlock();
        verify(accountInfoService).getById(1);
        verify(electricMeterManagerService).cancelMeterAccount(argThat(dto ->
                dto.getAccountId().equals(1) &&
                        dto.getOwnerId().equals(1) &&
                        dto.getOwnerType().equals(OwnerTypeEnum.ENTERPRISE) &&
                        dto.getElectricAccountType().equals(ElectricAccountTypeEnum.QUANTITY) &&
                        dto.getMeterCloseDetail().size() == 1 &&
                        dto.getMeterCloseDetail().get(0).getMeterId().equals(1)
        ));
        verify(cancelRecordRepository).insert(ArgumentMatchers.<AccountCancelRecordEntity>argThat(entity ->
                entity.getAccountId().equals(1) &&
                        entity.getOwnerId().equals(1) &&
                        entity.getOwnerType().equals(OwnerTypeEnum.ENTERPRISE.getCode()) &&
                        entity.getElectricAccountType().equals(ElectricAccountTypeEnum.QUANTITY.getCode())
        ));
        verify(repository).deleteById(1);

        ArgumentCaptor<TerminationOrderCreationInfoDto> orderCaptor = ArgumentCaptor.forClass(TerminationOrderCreationInfoDto.class);
        verify(orderService, times(1)).createOrder(orderCaptor.capture());
        TerminationOrderCreationInfoDto orderDto = orderCaptor.getValue();

        assertThat(orderDto.getPaymentChannel()).isEqualTo(PaymentChannelEnum.OFFLINE);
        assertThat(orderDto.getOrderAmount()).isEqualByComparingTo("-50");
        assertThat(orderDto.getTerminationInfo().getCancelNo()).isNotBlank();
        assertThat(orderDto.getTerminationInfo().getSettlementAmount()).isEqualByComparingTo("-50");
        assertThat(orderDto.getTerminationInfo().getCloseReason()).isEqualTo(cancelAccountDto.getRemark());
        assertThat(orderDto.getTerminationInfo().getAccountId()).isEqualTo(1);
        assertThat(orderDto.getTerminationInfo().getOwnerId()).isEqualTo(1);
        assertThat(orderDto.getTerminationInfo().getOwnerType()).isEqualTo(OwnerTypeEnum.ENTERPRISE);
        assertThat(orderDto.getTerminationInfo().getOwnerName()).isEqualTo("测试企业");
        assertThat(orderDto.getUserId()).isEqualTo(100);
        assertThat(orderDto.getUserPhone()).isEqualTo("13800000000");
        assertThat(orderDto.getUserRealName()).isEqualTo("李四");
        assertThat(orderDto.getThirdPartyUserId()).isEqualTo("100");
    }

    @Test
    void testCancelAccount_RoundingSmallPositiveBalance() {
        CancelAccountDto cancelAccountDto = new CancelAccountDto()
                .setAccountId(1)
                .setRemark("小额余额销户")
                .setMeterList(List.of(new MeterCancelDetailDto().setMeterId(1)));

        AccountBo accountBo = new AccountBo()
                .setId(1)
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY)
                .setOwnerId(1)
                .setOwnerType(OwnerTypeEnum.ENTERPRISE)
                .setOwnerName("测试企业");

        ElectricMeterBo electricMeterBo = new ElectricMeterBo()
                .setId(1)
                .setAccountId(1);

        List<MeterCancelResultDto> meterCancelBalances = List.of(
                new MeterCancelResultDto().setMeterId(1).setBalance(new BigDecimal("0.004"))
        );

        when(lockTemplate.getLock(any(String.class))).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
        doNothing().when(lock).unlock();
        when(accountInfoService.getById(1)).thenReturn(accountBo);
        when(electricMeterInfoService.findList(new ElectricMeterQueryDto().setAccountId(1).setInIds(List.of(1))))
                .thenReturn(List.of(electricMeterBo));
        when(electricMeterManagerService.cancelMeterAccount(any())).thenReturn(meterCancelBalances);
        when(cancelRecordRepository.insert(any(AccountCancelRecordEntity.class))).thenReturn(1);
        when(repository.deleteById(1)).thenReturn(1);
        mockRequestContext();

        CancelAccountResponseDto response = accountManagerService.cancelAccount(cancelAccountDto);

        assertThat(response.getCleanBalanceType()).isEqualTo(CleanBalanceTypeEnum.SKIP);
        assertThat(response.getAmount()).isEqualByComparingTo("0.00");

        ArgumentCaptor<AccountCancelRecordEntity> recordCaptor = ArgumentCaptor.forClass(AccountCancelRecordEntity.class);
        verify(cancelRecordRepository).insert(recordCaptor.capture());
        AccountCancelRecordEntity record = recordCaptor.getValue();
        assertThat(record.getCleanBalanceType()).isEqualTo(CleanBalanceTypeEnum.SKIP.getCode());
        assertThat(record.getCleanBalanceReal()).isEqualByComparingTo("0.00");
        assertThat(record.getCleanBalanceIgnore()).isEqualByComparingTo("0.004");

        ArgumentCaptor<TerminationOrderCreationInfoDto> orderCaptor = ArgumentCaptor.forClass(TerminationOrderCreationInfoDto.class);
        verify(orderService).createOrder(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getOrderAmount()).isEqualByComparingTo("0.00");
    }

    @Test
    void testCloseAccount_Success_MonthlyPartialCancel() {
        // Given
        CancelAccountDto cancelAccountDto = new CancelAccountDto()
                .setAccountId(1)
                .setRemark("包月计费部分销户")
                .setMeterList(List.of(new MeterCancelDetailDto()
                        .setMeterId(1)));

        AccountBo accountBo = new AccountBo()
                .setId(1)
                .setElectricAccountType(ElectricAccountTypeEnum.MONTHLY)
                .setOwnerId(1)
                .setOwnerType(OwnerTypeEnum.ENTERPRISE)
                .setOwnerName("测试企业");

        ElectricMeterBo electricMeterBo = new ElectricMeterBo()
                .setId(1)
                .setAccountId(1);

        List<MeterCancelResultDto> meterCancelBalances = List.of(
                new MeterCancelResultDto().setMeterId(1).setBalance(BigDecimal.ZERO)
        );

        ElectricMeterQueryDto query = new ElectricMeterQueryDto()
                .setAccountId(1).setInIds(List.of(1));

        // Mock lock operations
        when(lockTemplate.getLock(any(String.class))).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
        doNothing().when(lock).unlock();

        // Mock account operations - 部分销户，账户下还有其他电表
        when(electricMeterInfoService.findList(query)).thenReturn(List.of(electricMeterBo));
        when(electricMeterInfoService.findList(new ElectricMeterQueryDto().setAccountId(1))).thenReturn(List.of(new ElectricMeterBo().setAccountId(1)));
        when(accountInfoService.getById(1)).thenReturn(accountBo);
        when(electricMeterManagerService.cancelMeterAccount(any())).thenReturn(meterCancelBalances);
        when(cancelRecordRepository.insert(any(AccountCancelRecordEntity.class))).thenReturn(1);

        // When
        accountManagerService.cancelAccount(cancelAccountDto);

        // Then
        verify(lock).tryLock();
        verify(lock).unlock();
        verify(accountInfoService).getById(1);
        verify(electricMeterManagerService).cancelMeterAccount(argThat(dto ->
                dto.getAccountId().equals(1) &&
                        dto.getOwnerId().equals(1) &&
                        dto.getOwnerType().equals(OwnerTypeEnum.ENTERPRISE) &&
                        dto.getElectricAccountType().equals(ElectricAccountTypeEnum.MONTHLY) &&
                        dto.getMeterCloseDetail().size() == 1 &&
                        dto.getMeterCloseDetail().get(0).getMeterId().equals(1)
        ));
        verify(cancelRecordRepository).insert(ArgumentMatchers.<AccountCancelRecordEntity>argThat(entity ->
                entity.getAccountId().equals(1) &&
                        entity.getOwnerId().equals(1) &&
                        entity.getOwnerType().equals(OwnerTypeEnum.ENTERPRISE.getCode()) &&
                        entity.getElectricAccountType().equals(ElectricAccountTypeEnum.MONTHLY.getCode())
        ));
        verify(repository, never()).deleteById(anyInt()); // 部分销户不删除账户
    }

    @Test
    void testCloseAccount_Success_MonthlyFullCancelRefund() {
        // Given
        CancelAccountDto cancelAccountDto = new CancelAccountDto()
                .setAccountId(1)
                .setRemark("包月计费全部销户退费")
                .setMeterList(List.of(new MeterCancelDetailDto()
                        .setMeterId(1)));

        AccountBo accountBo = new AccountBo()
                .setId(1)
                .setElectricAccountType(ElectricAccountTypeEnum.MONTHLY)
                .setOwnerId(1)
                .setOwnerType(OwnerTypeEnum.ENTERPRISE)
                .setOwnerName("测试企业");

        ElectricMeterBo electricMeterBo = new ElectricMeterBo()
                .setId(1)
                .setAccountId(1);

        List<MeterCancelResultDto> meterCancelBalances = List.of(
                new MeterCancelResultDto().setMeterId(1).setBalance(BigDecimal.valueOf(200))
        );

        ElectricMeterQueryDto query = new ElectricMeterQueryDto()
                .setAccountId(1).setInIds(List.of(1));

        // Mock lock operations
        when(lockTemplate.getLock(any(String.class))).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
        doNothing().when(lock).unlock();

        // Mock account operations - 全部销户，账户下只有一个电表
        when(accountInfoService.getById(1)).thenReturn(accountBo);
        when(electricMeterInfoService.findList(query)).thenReturn(List.of(electricMeterBo));
        when(electricMeterManagerService.cancelMeterAccount(any())).thenReturn(meterCancelBalances);
        when(balanceService.query(new BalanceQueryDto().setBalanceRelationId(1).setBalanceType(BalanceTypeEnum.ACCOUNT)))
                .thenReturn(new BalanceBo().setBalance(new BigDecimal("100.00")));
        when(cancelRecordRepository.insert(any(AccountCancelRecordEntity.class))).thenReturn(1);
        when(repository.deleteById(1)).thenReturn(1);
        mockRequestContext();

        // When
        accountManagerService.cancelAccount(cancelAccountDto);

        // Then
        verify(lock).tryLock();
        verify(lock).unlock();
        verify(accountInfoService).getById(1);
        verify(electricMeterManagerService).cancelMeterAccount(argThat(dto ->
                dto.getAccountId().equals(1) &&
                        dto.getOwnerId().equals(1) &&
                        dto.getOwnerType().equals(OwnerTypeEnum.ENTERPRISE) &&
                        dto.getElectricAccountType().equals(ElectricAccountTypeEnum.MONTHLY) &&
                        dto.getMeterCloseDetail().size() == 1 &&
                        dto.getMeterCloseDetail().get(0).getMeterId().equals(1)
        ));
        verify(cancelRecordRepository).insert(ArgumentMatchers.<AccountCancelRecordEntity>argThat(entity ->
                entity.getAccountId().equals(1) &&
                        entity.getOwnerId().equals(1) &&
                        entity.getOwnerType().equals(OwnerTypeEnum.ENTERPRISE.getCode()) &&
                        entity.getElectricAccountType().equals(ElectricAccountTypeEnum.MONTHLY.getCode())
        ));
        verify(repository).deleteById(1); // 全部销户删除账户

        ArgumentCaptor<TerminationOrderCreationInfoDto> orderCaptor = ArgumentCaptor.forClass(TerminationOrderCreationInfoDto.class);
        verify(orderService, times(1)).createOrder(orderCaptor.capture());
        TerminationOrderCreationInfoDto orderDto = orderCaptor.getValue();

        assertThat(orderDto.getPaymentChannel()).isEqualTo(PaymentChannelEnum.OFFLINE);
        assertThat(orderDto.getOrderAmount()).isEqualByComparingTo("100");
        assertThat(orderDto.getTerminationInfo().getCancelNo()).isNotBlank();
        assertThat(orderDto.getTerminationInfo().getSettlementAmount()).isEqualByComparingTo("100");
        assertThat(orderDto.getTerminationInfo().getCloseReason()).isEqualTo(cancelAccountDto.getRemark());
        assertThat(orderDto.getTerminationInfo().getAccountId()).isEqualTo(1);
        assertThat(orderDto.getTerminationInfo().getOwnerId()).isEqualTo(1);
        assertThat(orderDto.getTerminationInfo().getOwnerType()).isEqualTo(OwnerTypeEnum.ENTERPRISE);
        assertThat(orderDto.getTerminationInfo().getOwnerName()).isEqualTo("测试企业");
        assertThat(orderDto.getUserId()).isEqualTo(100);
        assertThat(orderDto.getUserPhone()).isEqualTo("13800000000");
        assertThat(orderDto.getUserRealName()).isEqualTo("李四");
        assertThat(orderDto.getThirdPartyUserId()).isEqualTo("100");
    }

    @Test
    void testCancelAccount_MeterValidationFailed() {
        // Given
        CancelAccountDto cancelAccountDto = new CancelAccountDto()
                .setAccountId(1)
                .setRemark("电表校验失败")
                .setMeterList(List.of(new MeterCancelDetailDto()
                        .setMeterId(999))); // 不存在的电表ID

        AccountBo accountBo = new AccountBo()
                .setId(1)
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY)
                .setOwnerId(1)
                .setOwnerType(OwnerTypeEnum.ENTERPRISE);

        ElectricMeterBo electricMeterBo = new ElectricMeterBo()
                .setId(1)
                .setAccountId(1);

        // Mock lock operations
        when(lockTemplate.getLock(any(String.class))).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
        doNothing().when(lock).unlock();

        // Mock account operations
        when(accountInfoService.getById(1)).thenReturn(accountBo);
        when(electricMeterInfoService.findList(new ElectricMeterQueryDto().setAccountId(1).setInIds(List.of(999)))).thenReturn(List.of(electricMeterBo)); // 账户下的电表不包含999

        // When & Then
        assertThrows(BusinessRuntimeException.class, () -> accountManagerService.cancelAccount(cancelAccountDto));

        verify(lock).unlock();
    }

    @Test
    void testCancelAccount_ExceptionHandling() {
        // Given
        CancelAccountDto cancelAccountDto = new CancelAccountDto()
                .setAccountId(1)
                .setRemark("系统异常测试")
                .setMeterList(List.of(new MeterCancelDetailDto()
                        .setMeterId(1)));

        // Mock lock operations
        when(lockTemplate.getLock(any(String.class))).thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
        doNothing().when(lock).unlock();

        // Mock exception
        when(accountInfoService.getById(1)).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(BusinessRuntimeException.class, () -> accountManagerService.cancelAccount(cancelAccountDto));

        verify(lock).unlock();
    }

    private void mockRequestContext() {
        when(requestContext.getUserId()).thenReturn(100);
        when(requestContext.getUserPhone()).thenReturn("13800000000");
        when(requestContext.getUserRealName()).thenReturn("李四");
    }

}
