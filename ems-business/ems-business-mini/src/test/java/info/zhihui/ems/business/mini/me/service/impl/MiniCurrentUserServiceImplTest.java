package info.zhihui.ems.business.mini.me.service.impl;

import info.zhihui.ems.business.account.bo.AccountBo;
import info.zhihui.ems.business.account.dto.AccountQueryDto;
import info.zhihui.ems.business.account.service.AccountAdditionalInfoService;
import info.zhihui.ems.business.account.service.AccountInfoService;
import info.zhihui.ems.business.device.bo.ElectricMeterBo;
import info.zhihui.ems.business.device.service.ElectricMeterInfoService;
import info.zhihui.ems.business.mini.me.bo.MiniCurrentUserBo;
import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import info.zhihui.ems.components.context.RequestContext;
import info.zhihui.ems.foundation.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MiniCurrentUserServiceImplTest {

    @Mock
    private UserService userService;
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
    void testGetCurrentUser_FromRequestContext_ShouldNotQueryUserInfo() {
        when(requestContext.getUserPhone()).thenReturn("13800138000");
        when(requestContext.getOrganizationId()).thenReturn(10);
        AccountBo account = new AccountBo()
                .setId(20)
                .setOwnerName("星河家园 2 栋住户账户")
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY);
        when(accountInfoService.findList(any(AccountQueryDto.class))).thenReturn(List.of(account));
        when(accountAdditionalInfoService.findElectricBalanceAmountMap(any())).thenReturn(Map.of(20, new BigDecimal("12.34")));
        when(electricMeterInfoService.findList(any())).thenReturn(List.of(new ElectricMeterBo(), new ElectricMeterBo()));

        MiniCurrentUserBo result = service.getCurrentUser();

        assertThat(result.getUserPhone()).isEqualTo("13800138000");
        assertThat(result.getElectricAccountId()).isEqualTo(20);
        assertThat(result.getBalance()).isEqualByComparingTo("12.34");
        assertThat(result.getMeterCount()).isEqualTo(2);
        verify(userService, never()).getUserInfo(any());
    }
}
