package info.zhihui.ems.web.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import info.zhihui.ems.config.satoken.SaWebConfig;
import info.zhihui.ems.components.translate.engine.TranslateEngine;
import info.zhihui.ems.components.translate.engine.TranslateMetadataCache;
import info.zhihui.ems.components.translate.formatter.AbsoluteMoneyScale2TextFormatter;
import info.zhihui.ems.components.translate.formatter.MoneyScale2TextFormatter;
import info.zhihui.ems.components.translate.resolver.EnumLabelResolver;
import info.zhihui.ems.components.translate.web.advice.ResponseTranslateAdvice;
import info.zhihui.ems.web.account.biz.AccountBiz;
import info.zhihui.ems.web.account.controller.AccountController;
import info.zhihui.ems.web.account.vo.*;
import info.zhihui.ems.common.paging.PageResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
@Import({
        ResponseTranslateAdvice.class,
        TranslateEngine.class,
        TranslateMetadataCache.class,
        EnumLabelResolver.class,
        AbsoluteMoneyScale2TextFormatter.class,
        MoneyScale2TextFormatter.class
})
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AccountBiz accountBiz;

    @MockitoBean
    private SaWebConfig saWebConfig;

    @Test
    @DisplayName("分页查询账户列表")
    void testFindAccountPage() throws Exception {
        AccountVo vo = new AccountVo()
                .setId(1)
                .setOwnerName("企业A")
                .setElectricBalanceAmount(new BigDecimal("12.34"))
                .setElectricBalanceAmountText("12.34");
        PageResult<AccountVo> pageResult = new PageResult<AccountVo>()
                .setList(List.of(vo))
                .setPageNum(1)
                .setPageSize(10)
                .setTotal(1L);
        when(accountBiz.findAccountPage(any(), eq(1), eq(10))).thenReturn(pageResult);

        mockMvc.perform(get("/accounts/page").param("pageNum", "1").param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.list[0].ownerName").value("企业A"))
                .andExpect(jsonPath("$.data.list[0].electricBalanceAmount").value(12.34))
                .andExpect(jsonPath("$.data.list[0].electricBalanceAmountText").value("12.34"));
    }

    @Test
    @DisplayName("分页查询账户列表应返回降级后的0余额")
    void testFindAccountPage_WithZeroElectricBalanceAmount() throws Exception {
        AccountVo vo = new AccountVo()
                .setId(1)
                .setOwnerName("企业A")
                .setElectricBalanceAmount(new BigDecimal("0.00"))
                .setElectricBalanceAmountText("0.00");
        PageResult<AccountVo> pageResult = new PageResult<AccountVo>()
                .setList(List.of(vo))
                .setPageNum(1)
                .setPageSize(10)
                .setTotal(1L);
        when(accountBiz.findAccountPage(any(), eq(1), eq(10))).thenReturn(pageResult);

        mockMvc.perform(get("/accounts/page").param("pageNum", "1").param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.list[0].electricBalanceAmount").value(0.0))
                .andExpect(jsonPath("$.data.list[0].electricBalanceAmountText").value("0.00"));
    }

    @Test
    @DisplayName("根据ID获取账户详情")
    void testGetAccount() throws Exception {
        AccountMeterVo meterVo = new AccountMeterVo()
                .setId(1001)
                .setMeterName("1号楼电表")
                .setWarnType("NONE")
                .setMeterBalanceAmount(new BigDecimal("345.60"));
        AccountDetailVo vo = new AccountDetailVo()
                .setId(10)
                .setOwnerName("账号详情")
                .setElectricBalanceAmount(new BigDecimal("1200.50"))
                .setOpenedMeterCount(2)
                .setTotalOpenableMeterCount(6)
                .setMeterList(List.of(meterVo));
        when(accountBiz.getAccount(10)).thenReturn(vo);

        mockMvc.perform(get("/accounts/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.electricBalanceAmount").value(1200.50))
                .andExpect(jsonPath("$.data.electricBalanceAmountText").value("1200.50"))
                .andExpect(jsonPath("$.data.openedMeterCount").value(2))
                .andExpect(jsonPath("$.data.totalOpenableMeterCount").value(6))
                .andExpect(jsonPath("$.data.meterList[0].id").value(1001))
                .andExpect(jsonPath("$.data.meterList[0].warnTypeName").value("无预警"))
                .andExpect(jsonPath("$.data.meterList[0].meterBalanceAmount").value(345.60))
                .andExpect(jsonPath("$.data.meterList[0].meterBalanceAmountText").value("345.60"));
    }

    @Test
    @DisplayName("分页查询销户记录")
    void testFindCancelRecordPage() throws Exception {
        AccountCancelRecordVo recordVo = new AccountCancelRecordVo()
                .setCancelNo("C001")
                .setCleanBalanceReal(new BigDecimal("-80"))
                .setCancelTime(LocalDateTime.of(2026, 2, 28, 10, 58, 46));
        PageResult<AccountCancelRecordVo> pageResult = new PageResult<AccountCancelRecordVo>()
                .setList(List.of(recordVo))
                .setPageNum(1)
                .setPageSize(10)
                .setTotal(1L);
        when(accountBiz.findCancelRecordPage(any(), eq(1), eq(10))).thenReturn(pageResult);

        mockMvc.perform(get("/accounts/cancel/page").param("pageNum", "1").param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].cancelNo").value("C001"))
                .andExpect(jsonPath("$.data.list[0].cleanBalanceReal").value(-80))
                .andExpect(jsonPath("$.data.list[0].cleanBalanceAmountText").value("80.00"))
                .andExpect(jsonPath("$.data.list[0].cancelTime").value("2026-02-28 10:58:46"));
    }

    @Test
    @DisplayName("获取销户详情")
    void testGetCancelRecordDetail() throws Exception {
        AccountCancelDetailVo detailVo = new AccountCancelDetailVo()
                .setCancelNo("C002")
                .setCleanBalanceReal(new BigDecimal("-50"))
                .setCancelTime(LocalDateTime.of(2026, 2, 28, 10, 58, 46));
        when(accountBiz.getCancelRecordDetail("C002")).thenReturn(detailVo);

        mockMvc.perform(get("/accounts/cancel/C002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.cancelNo").value("C002"))
                .andExpect(jsonPath("$.data.cleanBalanceReal").value(-50))
                .andExpect(jsonPath("$.data.cleanBalanceAmountText").value("50.00"))
                .andExpect(jsonPath("$.data.cancelTime").value("2026-02-28 10:58:46"));
    }

    @Test
    @DisplayName("开户")
    void testOpenAccount() throws Exception {
        when(accountBiz.openAccount(any(OpenAccountVo.class))).thenReturn(88);

        OpenAccountVo vo = new OpenAccountVo()
                .setOwnerId(1)
                .setOwnerType(0)
                .setOwnerName("企业A")
                .setElectricAccountType(1)
                .setMonthlyPayAmount(new BigDecimal("100"))
                .setElectricMeterList(List.of(new MeterOpenDetailVo().setMeterId(10)));

        mockMvc.perform(post("/accounts/open")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(88));
    }

    @Test
    @DisplayName("销户")
    void testCancelAccount() throws Exception {
        CancelAccountResponseVo responseVo = new CancelAccountResponseVo()
                .setCancelNo("C003")
                .setAmount(new BigDecimal("-80"));
        when(accountBiz.cancelAccount(any(CancelAccountVo.class))).thenReturn(responseVo);

        CancelAccountVo cancelVo = new CancelAccountVo()
                .setAccountId(5)
                .setMeterList(List.of(new MeterCancelDetailVo().setMeterId(10)));

        mockMvc.perform(post("/accounts/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelVo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.cancelNo").value("C003"))
                .andExpect(jsonPath("$.data.amount").value(-80))
                .andExpect(jsonPath("$.data.cleanBalanceAmountText").value("80.00"));
    }

    @Test
    @DisplayName("追加绑定电表成功")
    void testAppendMeters_Success() throws Exception {
        AccountMetersOpenVo vo = new AccountMetersOpenVo()
                .setElectricMeterList(List.of(new MeterOpenDetailVo().setMeterId(10)));

        mockMvc.perform(post("/accounts/{id}/meters/open", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("修改账户信息成功")
    void testUpdateAccountConfig_Success() throws Exception {
        AccountConfigUpdateVo vo = new AccountConfigUpdateVo()
                .setMonthlyPayAmount(new BigDecimal("100"))
                .setContactName("张三")
                .setContactPhone("13800138000");

        mockMvc.perform(put("/accounts/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

}
