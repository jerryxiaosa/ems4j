package info.zhihui.ems.web.owner;

import info.zhihui.ems.config.satoken.SaWebConfig;
import info.zhihui.ems.web.owner.biz.OwnerAccountBiz;
import info.zhihui.ems.web.owner.controller.OwnerAccountController;
import info.zhihui.ems.web.owner.vo.OwnerAccountStatusQueryVo;
import info.zhihui.ems.web.owner.vo.OwnerAccountStatusVo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OwnerAccountController.class)
class OwnerAccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OwnerAccountBiz ownerAccountBiz;

    @MockitoBean
    private SaWebConfig saWebConfig;

    @Test
    @DisplayName("查询主体账户状态应绑定参数并返回结果")
    void testGetAccountStatus_Success() throws Exception {
        when(ownerAccountBiz.getAccountStatus(any())).thenReturn(new OwnerAccountStatusVo()
                .setOwnerType(0)
                .setOwnerId(1001)
                .setHasAccount(true)
                .setAccountId(3)
                .setElectricAccountType(1)
                .setElectricPricePlanId(2001)
                .setWarnPlanId(3001)
                .setMonthlyPayAmount(new BigDecimal("66.60"))
                .setMonthlyPayAmountText("66.60"));

        mockMvc.perform(get("/owner-accounts/status")
                        .param("ownerType", "0")
                        .param("ownerId", "1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.hasAccount").value(true))
                .andExpect(jsonPath("$.data.accountId").value(3))
                .andExpect(jsonPath("$.data.electricAccountType").value(1))
                .andExpect(jsonPath("$.data.electricPricePlanId").value(2001))
                .andExpect(jsonPath("$.data.warnPlanId").value(3001))
                .andExpect(jsonPath("$.data.monthlyPayAmount").value(66.60))
                .andExpect(jsonPath("$.data.monthlyPayAmountText").value("66.60"));

        ArgumentCaptor<OwnerAccountStatusQueryVo> captor = ArgumentCaptor.forClass(OwnerAccountStatusQueryVo.class);
        verify(ownerAccountBiz).getAccountStatus(captor.capture());
        assertEquals(0, captor.getValue().getOwnerType());
        assertEquals(1001, captor.getValue().getOwnerId());
    }
}
