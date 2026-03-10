package info.zhihui.ems.mq.rabbitmq.listener.finance;

import info.zhihui.ems.business.account.bo.AccountBo;
import info.zhihui.ems.business.account.service.AccountBalanceChangeService;
import info.zhihui.ems.business.account.service.AccountInfoService;
import info.zhihui.ems.business.device.dto.MeterBalanceChangeDto;
import info.zhihui.ems.business.device.service.MeterBalanceChangeService;
import info.zhihui.ems.common.enums.BalanceTypeEnum;
import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import info.zhihui.ems.mq.api.message.finance.BalanceChangedMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BalanceChangedListenerTest {

    @Mock
    private AccountBalanceChangeService accountBalanceChangeService;
    @Mock
    private AccountInfoService accountInfoService;
    @Mock
    private MeterBalanceChangeService meterBalanceChangeService;

    @InjectMocks
    private BalanceChangedListener balanceChangedListener;

    @Test
    void testHandle_WhenAccountMessageReceived_ShouldDispatchToAccountService() {
        BalanceChangedMessage accountMessage = new BalanceChangedMessage()
                .setBalanceType(BalanceTypeEnum.ACCOUNT)
                .setBalanceRelationId(10)
                .setAccountId(100)
                .setNewBalance(new BigDecimal("88"));

        balanceChangedListener.handle(accountMessage);

        verify(accountBalanceChangeService, times(1)).handleBalanceChange(accountMessage);
        verifyNoInteractions(accountInfoService, meterBalanceChangeService);
    }

    @Test
    void testHandle_WhenMeterMessageReceivedAndAccountTypeIsQuantity_ShouldHandleSwitchStatus() {
        BalanceChangedMessage meterMessage = new BalanceChangedMessage()
                .setBalanceType(BalanceTypeEnum.ELECTRIC_METER)
                .setBalanceRelationId(20)
                .setAccountId(200)
                .setNewBalance(new BigDecimal("66"));
        when(accountInfoService.getById(200)).thenReturn(new AccountBo()
                .setId(200)
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY));

        balanceChangedListener.handle(meterMessage);

        ArgumentCaptor<MeterBalanceChangeDto> dtoCaptor = ArgumentCaptor.forClass(MeterBalanceChangeDto.class);
        verify(meterBalanceChangeService, times(1)).handleBalanceChange(dtoCaptor.capture());
        MeterBalanceChangeDto meterBalanceChangeDto = dtoCaptor.getValue();
        Assertions.assertEquals(20, meterBalanceChangeDto.getMeterId());
        Assertions.assertEquals(new BigDecimal("66"), meterBalanceChangeDto.getNewBalance());
        Assertions.assertTrue(meterBalanceChangeDto.getNeedHandleSwitchStatus());
        verifyNoInteractions(accountBalanceChangeService);
    }

    @Test
    void testHandle_WhenMeterMessageReceivedAndAccountTypeIsNotQuantity_ShouldSkipSwitchStatus() {
        BalanceChangedMessage meterMessage = new BalanceChangedMessage()
                .setBalanceType(BalanceTypeEnum.ELECTRIC_METER)
                .setBalanceRelationId(21)
                .setAccountId(201)
                .setNewBalance(new BigDecimal("77"));
        when(accountInfoService.getById(201)).thenReturn(new AccountBo()
                .setId(201)
                .setElectricAccountType(ElectricAccountTypeEnum.MONTHLY));

        balanceChangedListener.handle(meterMessage);

        ArgumentCaptor<MeterBalanceChangeDto> dtoCaptor = ArgumentCaptor.forClass(MeterBalanceChangeDto.class);
        verify(meterBalanceChangeService, times(1)).handleBalanceChange(dtoCaptor.capture());
        Assertions.assertFalse(dtoCaptor.getValue().getNeedHandleSwitchStatus());
        verifyNoInteractions(accountBalanceChangeService);
    }

    @Test
    void testHandle_WhenMeterBranchQueryAccountThrowsException_ShouldNotThrow() {
        BalanceChangedMessage meterMessage = new BalanceChangedMessage()
                .setBalanceType(BalanceTypeEnum.ELECTRIC_METER)
                .setBalanceRelationId(23)
                .setAccountId(203)
                .setNewBalance(new BigDecimal("44"));
        when(accountInfoService.getById(203)).thenThrow(new IllegalStateException("account query failed"));

        Assertions.assertDoesNotThrow(() -> balanceChangedListener.handle(meterMessage));

        verify(meterBalanceChangeService, never()).handleBalanceChange(org.mockito.ArgumentMatchers.any());
        verifyNoInteractions(accountBalanceChangeService);
    }
}
