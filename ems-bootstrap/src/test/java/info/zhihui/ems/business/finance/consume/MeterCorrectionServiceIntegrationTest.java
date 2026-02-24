package info.zhihui.ems.business.finance.consume;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import info.zhihui.ems.business.finance.bo.BalanceBo;
import info.zhihui.ems.business.finance.dto.*;
import info.zhihui.ems.business.finance.dto.CorrectMeterAmountDto;
import info.zhihui.ems.business.finance.entity.ElectricMeterBalanceConsumeRecordEntity;
import info.zhihui.ems.business.finance.enums.ConsumeTypeEnum;
import info.zhihui.ems.business.finance.enums.CorrectionTypeEnum;
import info.zhihui.ems.business.finance.repository.ElectricMeterBalanceConsumeRecordRepository;
import info.zhihui.ems.business.finance.service.balance.BalanceService;
import info.zhihui.ems.business.finance.service.consume.MeterCorrectionService;
import info.zhihui.ems.common.enums.BalanceTypeEnum;
import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import info.zhihui.ems.common.enums.OwnerTypeEnum;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MeterCorrectionService 集成测试
 */
@SpringBootTest
@ActiveProfiles("integrationtest")
@Transactional
class MeterCorrectionServiceIntegrationTest {

    @Autowired
    private MeterCorrectionService meterCorrectionService;

    @Autowired
    private BalanceService balanceService;

    @Autowired
    private ElectricMeterBalanceConsumeRecordRepository electricMeterBalanceConsumeRecordRepository;

    @Test
    @DisplayName("correctByAmount - 按需账户补扣成功")
    void testCorrectByAmount_DeductQuantityAccount_Success() {
        balanceService.initElectricMeterBalance(1, 1);
        balanceService.topUp(new BalanceDto()
                .setBalanceRelationId(1)
                .setBalanceType(BalanceTypeEnum.ELECTRIC_METER)
                .setAccountId(1)
                .setOrderNo("TOPUP-CORR-" + System.currentTimeMillis())
                .setAmount(new BigDecimal("50.00")));

        BalanceBo before = balanceService.getByQuery(new BalanceQueryDto()
                .setBalanceRelationId(1)
                .setBalanceType(BalanceTypeEnum.ELECTRIC_METER));

        CorrectMeterAmountDto dto = new CorrectMeterAmountDto()
                .setAccountId(1)
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY)
                .setOwnerId(1001)
                .setOwnerType(OwnerTypeEnum.PERSONAL)
                .setOwnerName("测试用户1")
                .setMeterId(1)
                .setMeterName("1号楼电表")
                .setMeterNo("EM001")
                .setReason("测试补扣")
                .setCorrectionType(CorrectionTypeEnum.PAY)
                .setAmount(new BigDecimal("10.00"));

        meterCorrectionService.correctByAmount(dto);

        BalanceBo after = balanceService.getByQuery(new BalanceQueryDto()
                .setBalanceRelationId(1)
                .setBalanceType(BalanceTypeEnum.ELECTRIC_METER));

        assertEquals(0, before.getBalance().subtract(new BigDecimal("10.00")).compareTo(after.getBalance()));

        ElectricMeterBalanceConsumeRecordEntity latestRecord = fetchLatestCorrectionRecord(1);
        assertNotNull(latestRecord);
        assertEquals(ConsumeTypeEnum.CORRECTION.getCode(), latestRecord.getConsumeType());
        assertEquals(dto.getMeterId(), latestRecord.getMeterId());
        assertEquals(new BigDecimal("10.00"), latestRecord.getConsumeAmount().setScale(2, RoundingMode.FLOOR));
    }

    @Test
    @DisplayName("correctByAmount - 合并账户退款成功")
    void testCorrectByAmount_RefundMergedAccount_Success() {
        BalanceBo before = balanceService.getByQuery(new BalanceQueryDto()
                .setBalanceRelationId(1)
                .setBalanceType(BalanceTypeEnum.ACCOUNT));

        CorrectMeterAmountDto dto = new CorrectMeterAmountDto()
                .setAccountId(1)
                .setMeterId(1)
                .setElectricAccountType(ElectricAccountTypeEnum.MERGED)
                .setOwnerId(1001)
                .setOwnerType(OwnerTypeEnum.PERSONAL)
                .setOwnerName("测试用户1")
                .setReason("测试退款")
                .setCorrectionType(CorrectionTypeEnum.REFUND)
                .setAmount(new BigDecimal("20.50"));

        meterCorrectionService.correctByAmount(dto);

        BalanceBo after = balanceService.getByQuery(new BalanceQueryDto()
                .setBalanceRelationId(1)
                .setBalanceType(BalanceTypeEnum.ACCOUNT));

        assertEquals(0, before.getBalance().add(new BigDecimal("20.50")).compareTo(after.getBalance()));

        ElectricMeterBalanceConsumeRecordEntity latestRecord = fetchLatestCorrectionRecord(1);
        assertNotNull(latestRecord);
        assertEquals(ConsumeTypeEnum.CORRECTION.getCode(), latestRecord.getConsumeType());
        assertEquals(new BigDecimal("-20.50"), latestRecord.getConsumeAmount().setScale(2, RoundingMode.FLOOR));
    }

    @Test
    @DisplayName("correctByAmount - 按需账户缺少电表抛异常")
    void testCorrectByAmount_QuantityAccountMissingMeter_ShouldThrowException() {
        CorrectMeterAmountDto dto = new CorrectMeterAmountDto()
                .setAccountId(3)
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY)
                .setOwnerId(1003)
                .setOwnerType(OwnerTypeEnum.PERSONAL)
                .setOwnerName("测试用户3")
                .setReason("缺少电表")
                .setCorrectionType(CorrectionTypeEnum.PAY)
                .setAmount(new BigDecimal("8.00"));

        assertThrows(ConstraintViolationException.class, () -> meterCorrectionService.correctByAmount(dto));
    }

    @Test
    @DisplayName("correctByAmount - 包月账户不允许补正")
    void testCorrectByAmount_MonthlyAccount_ShouldThrowException() {
        CorrectMeterAmountDto dto = new CorrectMeterAmountDto()
                .setAccountId(4)
                .setMeterId(4)
                .setElectricAccountType(ElectricAccountTypeEnum.MONTHLY)
                .setOwnerId(1004)
                .setOwnerType(OwnerTypeEnum.ENTERPRISE)
                .setOwnerName("测试企业4")
                .setReason("包月不支持")
                .setCorrectionType(CorrectionTypeEnum.PAY)
                .setAmount(new BigDecimal("15.00"));

        assertThrows(BusinessRuntimeException.class, () -> meterCorrectionService.correctByAmount(dto));
    }

    @Test
    @DisplayName("findCorrectionRecordPage - 按账户电表过滤成功")
    void testFindCorrectionRecordPage_Success() {
        CorrectMeterAmountDto dto = new CorrectMeterAmountDto()
                .setAccountId(3)
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY)
                .setOwnerId(1001)
                .setOwnerType(OwnerTypeEnum.PERSONAL)
                .setOwnerName("测试用户1")
                .setMeterId(3)
                .setMeterName("1号楼电表")
                .setMeterNo("EM001")
                .setCorrectionType(CorrectionTypeEnum.PAY)
                .setAmount(new BigDecimal("6.66"))
                .setReason("测试查询")
                .setCorrectionTime(LocalDateTime.now());

        meterCorrectionService.correctByAmount(dto);

        MeterCorrectionRecordQueryDto queryDto = new MeterCorrectionRecordQueryDto()
                .setAccountId(dto.getAccountId())
                .setMeterId(dto.getMeterId())
                .setMeterName("1号楼")
                .setBeginTime(LocalDateTime.now().minusYears(1))
                .setEndTime(LocalDateTime.now().plusYears(1));

        PageResult<MeterCorrectionRecordDto> result = meterCorrectionService.findCorrectionRecordPage(queryDto,
                new PageParam().setPageNum(1).setPageSize(5));

        assertNotNull(result);
        assertFalse(result.getList().isEmpty());
        MeterCorrectionRecordDto record = result.getList().get(0);
        assertEquals(dto.getAccountId(), record.getAccountId());
        assertEquals(dto.getMeterId(), record.getMeterId());
        assertEquals(dto.getMeterName(), record.getMeterName());
        assertEquals(dto.getAmount().setScale(2, RoundingMode.FLOOR), record.getConsumeAmount().setScale(2, RoundingMode.FLOOR));
        assertEquals(record.getBeginBalance().subtract(record.getConsumeAmount()), record.getEndBalance());
    }

    private ElectricMeterBalanceConsumeRecordEntity fetchLatestCorrectionRecord(Integer accountId) {
        return electricMeterBalanceConsumeRecordRepository.selectOne(new QueryWrapper<ElectricMeterBalanceConsumeRecordEntity>()
                .eq("account_id", accountId)
                .eq("consume_type", ConsumeTypeEnum.CORRECTION.getCode())
                .orderByDesc("id")
                .last("limit 1"));
    }
}
