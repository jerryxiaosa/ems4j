package info.zhihui.ems.mq.rabbitmq.listener.finance;

import info.zhihui.ems.business.account.service.AccountBalanceChangeService;
import info.zhihui.ems.business.account.service.MeterBalanceChangeService;
import info.zhihui.ems.common.enums.BalanceTypeEnum;
import info.zhihui.ems.mq.api.message.finance.BalanceChangedMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class BalanceChangedListenerTest {

    @Mock
    private AccountBalanceChangeService accountBalanceChangeService;
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
        verifyNoInteractions(meterBalanceChangeService);
    }

    @Test
    void testHandle_WhenMeterMessageReceived_ShouldDispatchToMeterService() {
        BalanceChangedMessage meterMessage = new BalanceChangedMessage()
                .setBalanceType(BalanceTypeEnum.ELECTRIC_METER)
                .setBalanceRelationId(20)
                .setAccountId(200)
                .setNewBalance(new BigDecimal("66"));

        balanceChangedListener.handle(meterMessage);

        verify(meterBalanceChangeService, times(1)).handleBalanceChange(meterMessage);
        verifyNoInteractions(accountBalanceChangeService);
    }
}
