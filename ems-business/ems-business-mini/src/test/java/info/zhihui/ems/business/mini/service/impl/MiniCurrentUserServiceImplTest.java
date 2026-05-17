package info.zhihui.ems.business.mini.service.impl;

import info.zhihui.ems.business.account.bo.AccountBo;
import info.zhihui.ems.business.account.dto.AccountElectricBalanceAggregateItemDto;
import info.zhihui.ems.business.account.service.AccountAdditionalInfoService;
import info.zhihui.ems.business.account.service.AccountInfoService;
import info.zhihui.ems.business.device.bo.ElectricMeterBo;
import info.zhihui.ems.business.device.dto.ElectricMeterQueryDto;
import info.zhihui.ems.business.device.service.ElectricMeterInfoService;
import info.zhihui.ems.business.mini.bo.MiniCurrentUserBo;
import info.zhihui.ems.common.constant.ResultCode;
import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.components.context.RequestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MiniCurrentUserServiceImplTest {

    @Mock
    private AccountInfoService accountInfoService;
    @Mock
    private AccountAdditionalInfoService accountAdditionalInfoService;
    @Mock
    private ElectricMeterInfoService electricMeterInfoService;
    @Mock
    private RequestContext requestContext;
    @InjectMocks
    private MiniCurrentUserServiceImpl service;

    @Test
    @SuppressWarnings("unchecked")
    void testGetCurrentUser_FromRequestContext_ShouldNotQueryUserInfo() {
        when(requestContext.getUserPhone()).thenReturn("13800138000");
        when(requestContext.getAccountId()).thenReturn(20);
        AccountBo account = new AccountBo()
                .setId(20)
                .setOwnerName("星河家园 2 栋住户账户")
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY);
        when(accountInfoService.getById(20)).thenReturn(account);
        when(accountAdditionalInfoService.findElectricBalanceAmountMap(any())).thenReturn(Map.of(20, new BigDecimal("12.34")));
        when(electricMeterInfoService.findList(any())).thenReturn(List.of(new ElectricMeterBo(), new ElectricMeterBo()));

        MiniCurrentUserBo result = service.getCurrentUser();

        assertThat(result.getUserPhone()).isEqualTo("13800138000");
        assertThat(result.getElectricAccountId()).isEqualTo(20);
        assertThat(result.getElectricAccountName()).isEqualTo("星河家园 2 栋住户账户");
        assertThat(result.getElectricAccountType()).isEqualTo(ElectricAccountTypeEnum.QUANTITY);
        assertThat(result.getBalance()).isEqualByComparingTo("12.34");
        assertThat(result.getMeterCount()).isEqualTo(2);

        verify(accountInfoService).getById(20);

        ArgumentCaptor<List<AccountElectricBalanceAggregateItemDto>> balanceItemCaptor = ArgumentCaptor.forClass(List.class);
        verify(accountAdditionalInfoService).findElectricBalanceAmountMap(balanceItemCaptor.capture());
        assertThat(balanceItemCaptor.getValue()).hasSize(1);
        assertThat(balanceItemCaptor.getValue().get(0).getAccountId()).isEqualTo(20);
        assertThat(balanceItemCaptor.getValue().get(0).getElectricAccountType()).isEqualTo(ElectricAccountTypeEnum.QUANTITY);

        ArgumentCaptor<ElectricMeterQueryDto> meterQueryCaptor = ArgumentCaptor.forClass(ElectricMeterQueryDto.class);
        verify(electricMeterInfoService).findList(meterQueryCaptor.capture());
        assertThat(meterQueryCaptor.getValue().getAccountIds()).containsExactly(20);
    }

    @Test
    void testGetCurrentUser_WhenBalanceMissing_ShouldUseZeroBalance() {
        when(requestContext.getUserPhone()).thenReturn("13800138000");
        when(requestContext.getAccountId()).thenReturn(20);
        AccountBo account = new AccountBo()
                .setId(20)
                .setOwnerName("星河家园 2 栋住户账户")
                .setElectricAccountType(ElectricAccountTypeEnum.MONTHLY);
        when(accountInfoService.getById(20)).thenReturn(account);
        when(accountAdditionalInfoService.findElectricBalanceAmountMap(any())).thenReturn(Map.of());
        when(electricMeterInfoService.findList(any())).thenReturn(List.of());

        MiniCurrentUserBo result = service.getCurrentUser();

        assertThat(result.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getMeterCount()).isZero();
    }

    @Test
    void testGetCurrentUser_WhenAccountTypeMissing_ShouldThrowAccountAbnormal() {
        when(requestContext.getAccountId()).thenReturn(20);
        when(accountInfoService.getById(20)).thenReturn(new AccountBo().setId(20));

        assertThatThrownBy(() -> service.getCurrentUser())
                .isInstanceOfSatisfying(BusinessRuntimeException.class, exception -> {
                    assertThat(exception.getCode()).isEqualTo(ResultCode.MINI_ACCOUNT_ABNORMAL.getCode());
                    assertThat(exception.getMessage()).isEqualTo(ResultCode.MINI_ACCOUNT_ABNORMAL.getMessage());
                });

        verify(accountAdditionalInfoService, never()).findElectricBalanceAmountMap(any());
        verify(electricMeterInfoService, never()).findList(any());
    }
}
