package info.zhihui.ems.web.finance;

import com.fasterxml.jackson.databind.ObjectMapper;
import info.zhihui.ems.business.billing.enums.CorrectionTypeEnum;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.config.satoken.SaWebConfig;
import info.zhihui.ems.web.finance.biz.FinanceBiz;
import info.zhihui.ems.web.finance.controller.FinanceConsumeController;
import info.zhihui.ems.web.finance.vo.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FinanceConsumeController.class)
class FinanceConsumeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FinanceBiz financeBiz;

    @MockitoBean
    private SaWebConfig saWebConfig;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("分页查询账户包月消费记录")
    void testFindAccountConsumePage() throws Exception {
        PageResult<AccountConsumeRecordVo> page = new PageResult<AccountConsumeRecordVo>()
                .setPageNum(1).setPageSize(10).setTotal(1L)
                .setList(List.of(new AccountConsumeRecordVo()
                        .setAccountId(1)
                        .setConsumeNo("AC202401")
                        .setPayAmount(new BigDecimal("50"))));
        when(financeBiz.findAccountConsumePage(any(AccountConsumeQueryVo.class), eq(1), eq(10))).thenReturn(page);

        mockMvc.perform(get("/v1/finance/account-consumes")
                        .param("accountNameLike", "张三")
                        .param("consumeTimeStart", LocalDateTime.now().minusDays(1).toString())
                        .param("consumeTimeEnd", LocalDateTime.now().toString())
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.list[0].consumeNo").value("AC202401"));
    }

    @Test
    @DisplayName("分页查询电表计费记录")
    void testFindMeterBillingPage() throws Exception {
        PageResult<MeterBillingRecordVo> page = new PageResult<MeterBillingRecordVo>()
                .setPageNum(1).setPageSize(5).setTotal(1L)
                .setList(List.of(new MeterBillingRecordVo()
                        .setMeterName("1号楼")
                        .setConsumeAmount(new BigDecimal("80"))));
        when(financeBiz.findMeterBillingPage(any(MeterBillingQueryVo.class), eq(1), eq(5))).thenReturn(page);

        mockMvc.perform(get("/v1/finance/meter-billings")
                        .param("searchKey", "1号")
                        .param("spaceNameLike", "1号楼")
                        .param("beginTime", LocalDateTime.now().minusDays(2).toString())
                        .param("endTime", LocalDateTime.now().toString())
                        .param("pageNum", "1")
                        .param("pageSize", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.list[0].meterName").value("1号楼"));
    }

    @Test
    @DisplayName("分页查询电表计费记录-不传时间条件")
    void testFindMeterBillingPage_WithoutTimeRange() throws Exception {
        PageResult<MeterBillingRecordVo> page = new PageResult<MeterBillingRecordVo>()
                .setPageNum(1).setPageSize(5).setTotal(1L)
                .setList(List.of(new MeterBillingRecordVo()
                        .setMeterName("2号楼")
                        .setConsumeAmount(new BigDecimal("60"))));
        when(financeBiz.findMeterBillingPage(any(MeterBillingQueryVo.class), eq(1), eq(5))).thenReturn(page);

        mockMvc.perform(get("/v1/finance/meter-billings")
                        .param("searchKey", "2号")
                        .param("pageNum", "1")
                        .param("pageSize", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.list[0].meterName").value("2号楼"));
    }

    @Test
    @DisplayName("查询电表计费记录明细")
    void testGetMeterBillingDetail() throws Exception {
        MeterBillingDetailVo detailVo = new MeterBillingDetailVo()
                .setId(1001)
                .setMeterConsumeRecordId(2001)
                .setConsumeNo("CONSUME_001")
                .setMeterName("1号楼电表")
                .setConsumeAmount(new BigDecimal("50.50"))
                .setConsumePower(new BigDecimal("100.00"))
                .setCreateTime(LocalDateTime.now());
        when(financeBiz.getMeterBillingDetail(1001)).thenReturn(detailVo);

        mockMvc.perform(get("/v1/finance/meter-billings/{id}", 1001))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1001))
                .andExpect(jsonPath("$.data.consumeNo").value("CONSUME_001"))
                .andExpect(jsonPath("$.data.meterName").value("1号楼电表"))
                .andExpect(jsonPath("$.data.createTime").exists());
    }

    @Test
    @DisplayName("分页查询补正记录")
    void testFindCorrectionRecordPage() throws Exception {
        PageResult<CorrectionRecordVo> page = new PageResult<CorrectionRecordVo>()
                .setPageNum(1).setPageSize(5).setTotal(1L)
                .setList(List.of(new CorrectionRecordVo()
                        .setConsumeNo("CORR-001")
                        .setConsumeAmount(new BigDecimal("10"))));
        when(financeBiz.findCorrectionRecordPage(any(CorrectionRecordQueryVo.class), eq(1), eq(5))).thenReturn(page);

        mockMvc.perform(get("/v1/finance/meter-corrections")
                        .param("accountId", "1")
                        .param("beginTime", LocalDateTime.now().minusDays(3).toString())
                        .param("endTime", LocalDateTime.now().toString())
                        .param("pageNum", "1")
                        .param("pageSize", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.list[0].consumeNo").value("CORR-001"));
    }

    @Test
    @DisplayName("新增补正记录")
    void testCorrectByAmount() throws Exception {
        CorrectionMeterAmountVo vo = new CorrectionMeterAmountVo()
                .setAccountId(1)
                .setMeterId(2)
                .setCorrectionType(2)
                .setAmount(new BigDecimal("10.00"))
                .setReason("补扣测试");
        doNothing().when(financeBiz).correctByAmount(any(CorrectionMeterAmountVo.class));

        mockMvc.perform(post("/v1/finance/meter-corrections")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(vo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        var captor = forClass(CorrectionMeterAmountVo.class);
        verify(financeBiz).correctByAmount(captor.capture());
        CorrectionMeterAmountVo actualVo = captor.getValue();
        assertEquals(vo.getAccountId(), actualVo.getAccountId());
        assertEquals(vo.getMeterId(), actualVo.getMeterId());
        assertEquals(vo.getCorrectionType(), actualVo.getCorrectionType());
        assertEquals(vo.getAmount(), actualVo.getAmount());
    }
}
