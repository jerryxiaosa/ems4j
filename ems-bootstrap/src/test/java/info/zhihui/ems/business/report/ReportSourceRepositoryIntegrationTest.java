package info.zhihui.ems.business.report;

import info.zhihui.ems.business.account.entity.AccountOpenRecordEntity;
import info.zhihui.ems.business.billing.entity.AccountBalanceConsumeRecordEntity;
import info.zhihui.ems.business.billing.entity.ElectricMeterBalanceConsumeRecordEntity;
import info.zhihui.ems.business.billing.entity.OrderFlowEntity;
import info.zhihui.ems.business.report.qo.MeterSnapshotSourceQo;
import info.zhihui.ems.business.report.qo.PowerRecordSnapshotSourceQo;
import info.zhihui.ems.business.report.qo.RechargeSourceItemQo;
import info.zhihui.ems.business.report.qo.ReportDateRangeQo;
import info.zhihui.ems.business.report.repository.source.ReportAccountSourceRepository;
import info.zhihui.ems.business.report.repository.source.ReportMeterSourceRepository;
import info.zhihui.ems.business.report.repository.source.ReportRechargeSourceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@ActiveProfiles("integrationtest")
@Transactional
@DisplayName("日报源仓储集成测试")
class ReportSourceRepositoryIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ReportMeterSourceRepository reportMeterSourceRepository;

    @Autowired
    private ReportAccountSourceRepository reportAccountSourceRepository;

    @Autowired
    private ReportRechargeSourceRepository reportRechargeSourceRepository;

    @Test
    @DisplayName("充值相关查询应以到账时间归属且返回基础到账事实")
    void testReportRechargeSourceRepository_ShouldUseOrderFlowCreateTime() {
        insertRechargeSource("ORDER-METER-IN", 100, 901, 1,
                LocalDateTime.of(2026, 4, 6, 10, 0),
                LocalDateTime.of(2026, 4, 6, 18, 0),
                new BigDecimal("88.00"),
                new BigDecimal("12.00"));
        insertRechargeSource("ORDER-METER-OUT", 100, 902, 1,
                LocalDateTime.of(2026, 4, 5, 23, 59),
                LocalDateTime.of(2026, 4, 6, 9, 0),
                new BigDecimal("66.00"),
                new BigDecimal("6.00"));
        insertRechargeSource("ORDER-ACCOUNT-IN", 101, 101, 0,
                LocalDateTime.of(2026, 4, 6, 11, 0),
                LocalDateTime.of(2026, 4, 6, 19, 0),
                new BigDecimal("100.00"),
                new BigDecimal("8.00"));

        ReportDateRangeQo query = buildReportDateRangeQo();

        List<RechargeSourceItemQo> meterRechargeList = reportRechargeSourceRepository
                .findDailyMeterRechargeListByAccountIdList(query, 1, "SUCCESS", 1, List.of(100, 101));
        List<RechargeSourceItemQo> accountRechargeList = reportRechargeSourceRepository
                .findDailyAccountRechargeListByAccountIdList(query, 1, "SUCCESS", List.of(100, 101));

        assertEquals(1, meterRechargeList.size());
        assertEquals("ORDER-METER-IN", meterRechargeList.get(0).getOrderSn());
        assertEquals(2, accountRechargeList.size());
        assertEquals(901, accountRechargeList.get(0).getMeterId());
        assertEquals(101, accountRechargeList.get(1).getMeterId());
        assertEquals(0, new BigDecimal("188.00").compareTo(accountRechargeList.stream()
                .map(RechargeSourceItemQo::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)));
    }

    @Test
    @DisplayName("电表上报查询应以recordTime归属")
    void testReportMeterSourceRepository_ShouldUseRecordTimeForPowerRecord() {
        jdbcTemplate.update(
                "insert into energy_electric_meter_power_record(meter_id, account_id, power, record_time, create_time, is_deleted) values (?, ?, ?, ?, ?, ?)",
                100, 10, new BigDecimal("123.45"),
                Timestamp.valueOf(LocalDateTime.of(2026, 4, 6, 8, 0)),
                Timestamp.valueOf(LocalDateTime.of(2026, 4, 6, 8, 1)), false
        );
        jdbcTemplate.update(
                "insert into energy_electric_meter_power_record(meter_id, account_id, power, record_time, create_time, is_deleted) values (?, ?, ?, ?, ?, ?)",
                101, 11, new BigDecimal("200.00"),
                Timestamp.valueOf(LocalDateTime.of(2026, 4, 5, 23, 59)),
                Timestamp.valueOf(LocalDateTime.of(2026, 4, 6, 9, 0)), false
        );

        List<PowerRecordSnapshotSourceQo> powerRecordSnapshotList =
                reportMeterSourceRepository.findDailyPowerRecordSnapshotListByAccountIdList(buildReportDateRangeQo(), List.of(10, 11));

        assertEquals(1, powerRecordSnapshotList.size());
        assertEquals(10, powerRecordSnapshotList.get(0).getAccountId());
        assertEquals(100, powerRecordSnapshotList.get(0).getMeterId());
    }

    @Test
    @DisplayName("电表上报快照查询应联表补齐关系字段")
    void testReportMeterSourceRepository_ShouldJoinPowerRecordSnapshot() {
        jdbcTemplate.update(
                "insert into energy_electric_meter_power_record(id, meter_id, meter_name, device_no, account_id, power, record_time, create_time, is_deleted) values (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                1001, 100, "电表A", "METER-A", 10, new BigDecimal("123.45"),
                Timestamp.valueOf(LocalDateTime.of(2026, 4, 6, 8, 0)),
                Timestamp.valueOf(LocalDateTime.of(2026, 4, 6, 8, 1)), false
        );
        jdbcTemplate.update(
                "insert into energy_electric_meter_power_relation(record_id, meter_id, account_id, owner_id, owner_type, owner_name, space_id, space_name, electric_account_type, record_time, create_time, is_deleted) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                1001, 100, 10, 1000, 1, "企业A", 2000, "一层", 0,
                Timestamp.valueOf(LocalDateTime.of(2026, 4, 6, 8, 0)),
                Timestamp.valueOf(LocalDateTime.of(2026, 4, 6, 8, 1)), false
        );
        jdbcTemplate.update(
                "insert into energy_electric_meter_power_record(id, meter_id, meter_name, device_no, account_id, power, record_time, create_time, is_deleted) values (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                1002, 101, "电表B", "METER-B", 11, new BigDecimal("200.00"),
                Timestamp.valueOf(LocalDateTime.of(2026, 4, 6, 9, 0)),
                Timestamp.valueOf(LocalDateTime.of(2026, 4, 6, 9, 1)), false
        );

        List<PowerRecordSnapshotSourceQo> powerRecordSnapshotList =
                reportMeterSourceRepository.findDailyPowerRecordSnapshotListByAccountIdList(buildReportDateRangeQo(), List.of(10, 11));

        assertEquals(2, powerRecordSnapshotList.size());
        assertEquals(1001, powerRecordSnapshotList.get(0).getRecordId());
        assertEquals("企业A", powerRecordSnapshotList.get(0).getOwnerName());
        assertEquals("一层", powerRecordSnapshotList.get(0).getSpaceName());
        assertEquals("电表B", powerRecordSnapshotList.get(1).getMeterName());
        assertNull(powerRecordSnapshotList.get(1).getOwnerName());
    }

    @Test
    @DisplayName("当前电表快照查询应返回电表和空间名称")
    void testReportMeterSourceRepository_ShouldFindCurrentMeterSnapshot() {
        jdbcTemplate.update(
                "insert into sys_space(id, name, pid, full_path, type, area, sort_index, is_deleted) values (?, ?, ?, ?, ?, ?, ?, ?)",
                801, "一层东", 0, "1,801", 1, new BigDecimal("10.00"), 1, false
        );
        jdbcTemplate.update(
                "insert into energy_electric_meter(id, space_id, meter_name, device_no, model_id, product_code, communicate_model, account_id, is_deleted) values (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                701, 801, "当前表A", "DEVICE-A", 1, "P-1", "485", 70, false
        );
        jdbcTemplate.update(
                "insert into energy_electric_meter(id, space_id, meter_name, device_no, model_id, product_code, communicate_model, account_id, is_deleted) values (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                702, 801, "当前表B", "DEVICE-B", 1, "P-2", "485", 71, false
        );

        List<MeterSnapshotSourceQo> meterSnapshotList =
                reportMeterSourceRepository.findCurrentMeterSnapshotListByAccountIdList(List.of(70, 71));

        assertEquals(2, meterSnapshotList.size());
        assertEquals(70, meterSnapshotList.get(0).getAccountId());
        assertEquals("当前表A", meterSnapshotList.get(0).getMeterName());
        assertEquals("一层东", meterSnapshotList.get(0).getSpaceName());
    }

    @Test
    @DisplayName("补正查询应仅返回补正类型记录")
    void testReportMeterSourceRepository_ShouldOnlyReturnCorrectionRecord() {
        jdbcTemplate.update(
                "insert into energy_electric_meter_balance_consume_record(consume_no, consume_type, meter_type, account_id, meter_id, consume_amount, meter_consume_time, create_time, is_deleted) values (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                "CHARGE-1", 1, 1, 10, 100, new BigDecimal("6.00"),
                Timestamp.valueOf(LocalDateTime.of(2026, 4, 6, 8, 0)),
                Timestamp.valueOf(LocalDateTime.of(2026, 4, 6, 8, 1)), false
        );
        jdbcTemplate.update(
                "insert into energy_electric_meter_balance_consume_record(consume_no, consume_type, meter_type, account_id, meter_id, consume_amount, meter_consume_time, create_time, is_deleted) values (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                "CORRECTION-1", 2, 1, 10, 100, new BigDecimal("-3.00"),
                Timestamp.valueOf(LocalDateTime.of(2026, 4, 6, 9, 0)),
                Timestamp.valueOf(LocalDateTime.of(2026, 4, 6, 9, 1)), false
        );

        List<ElectricMeterBalanceConsumeRecordEntity> correctionList = reportMeterSourceRepository
                .findDailyBalanceConsumeRecordListByAccountIdList(buildReportDateRangeQo(), 2, List.of(10));

        assertEquals(1, correctionList.size());
        assertEquals(0, new BigDecimal("-3.00").compareTo(correctionList.get(0).getConsumeAmount()));
    }

    @Test
    @DisplayName("包月查询应以consumeTime归属而不是createTime")
    void testReportAccountSourceRepository_ShouldUseConsumeTimeForMonthlyRecord() {
        jdbcTemplate.update(
                "insert into energy_account_balance_consume_record(consume_no, account_id, consume_type, pay_amount, consume_time, create_time, is_deleted) values (?, ?, ?, ?, ?, ?, ?)",
                "MONTHLY-IN", 200, 0, new BigDecimal("30.00"),
                Timestamp.valueOf(LocalDateTime.of(2026, 4, 6, 8, 0)),
                Timestamp.valueOf(LocalDateTime.of(2026, 4, 6, 8, 0)), false
        );
        jdbcTemplate.update(
                "insert into energy_account_balance_consume_record(consume_no, account_id, consume_type, pay_amount, consume_time, create_time, is_deleted) values (?, ?, ?, ?, ?, ?, ?)",
                "MONTHLY-OUT", 201, 0, new BigDecimal("31.00"),
                Timestamp.valueOf(LocalDateTime.of(2026, 4, 5, 23, 0)),
                Timestamp.valueOf(LocalDateTime.of(2026, 4, 6, 9, 0)), false
        );

        List<AccountBalanceConsumeRecordEntity> monthlyRecordList = reportAccountSourceRepository
                .findDailyMonthlyConsumeRecordListByAccountIdList(buildReportDateRangeQo(), 0, List.of(200, 201));

        assertEquals(1, monthlyRecordList.size());
        assertEquals(200, monthlyRecordList.get(0).getAccountId());
    }

    @Test
    @DisplayName("账户流水查询应按createTime与balanceType筛选")
    void testReportAccountSourceRepository_ShouldFindAccountOrderFlowByCreateTime() {
        jdbcTemplate.update(
                "insert into energy_account_order_flow(consume_id, account_id, balance_relation_id, balance_type, amount, begin_balance, end_balance, create_time) values (?, ?, ?, ?, ?, ?, ?, ?)",
                "FLOW-IN", 300, 300, 0, new BigDecimal("20.00"), new BigDecimal("80.00"), new BigDecimal("100.00"),
                Timestamp.valueOf(LocalDateTime.of(2026, 4, 6, 8, 0))
        );
        jdbcTemplate.update(
                "insert into energy_account_order_flow(consume_id, account_id, balance_relation_id, balance_type, amount, begin_balance, end_balance, create_time) values (?, ?, ?, ?, ?, ?, ?, ?)",
                "FLOW-METER", 300, 301, 1, new BigDecimal("10.00"), new BigDecimal("40.00"), new BigDecimal("50.00"),
                Timestamp.valueOf(LocalDateTime.of(2026, 4, 6, 9, 0))
        );
        jdbcTemplate.update(
                "insert into energy_account_order_flow(consume_id, account_id, balance_relation_id, balance_type, amount, begin_balance, end_balance, create_time) values (?, ?, ?, ?, ?, ?, ?, ?)",
                "FLOW-OUT", 301, 301, 0, new BigDecimal("15.00"), new BigDecimal("20.00"), new BigDecimal("35.00"),
                Timestamp.valueOf(LocalDateTime.of(2026, 4, 5, 23, 0))
        );

        List<OrderFlowEntity> orderFlowList = reportAccountSourceRepository
                .findDailyAccountOrderFlowListByAccountIdList(buildReportDateRangeQo(), 0, List.of(300, 301));

        assertEquals(1, orderFlowList.size());
        assertEquals("FLOW-IN", orderFlowList.get(0).getConsumeId());
        assertEquals(0, new BigDecimal("80.00").compareTo(orderFlowList.get(0).getBeginBalance()));
    }

    @Test
    @DisplayName("账户流水候选查询应按到账时间返回去重账户")
    void testReportAccountSourceRepository_ShouldFindCandidateAccountIdByCreateTime() {
        jdbcTemplate.update(
                "insert into energy_account_order_flow(consume_id, account_id, balance_relation_id, balance_type, amount, begin_balance, end_balance, create_time) values (?, ?, ?, ?, ?, ?, ?, ?)",
                "FLOW-IN-1", 300, 300, 0, new BigDecimal("20.00"), new BigDecimal("80.00"), new BigDecimal("100.00"),
                Timestamp.valueOf(LocalDateTime.of(2026, 4, 6, 8, 0))
        );
        jdbcTemplate.update(
                "insert into energy_account_order_flow(consume_id, account_id, balance_relation_id, balance_type, amount, begin_balance, end_balance, create_time) values (?, ?, ?, ?, ?, ?, ?, ?)",
                "FLOW-IN-2", 300, 301, 1, new BigDecimal("10.00"), new BigDecimal("40.00"), new BigDecimal("50.00"),
                Timestamp.valueOf(LocalDateTime.of(2026, 4, 6, 9, 0))
        );
        jdbcTemplate.update(
                "insert into energy_account_order_flow(consume_id, account_id, balance_relation_id, balance_type, amount, begin_balance, end_balance, create_time) values (?, ?, ?, ?, ?, ?, ?, ?)",
                "FLOW-IN-3", 301, 301, 0, new BigDecimal("15.00"), new BigDecimal("20.00"), new BigDecimal("35.00"),
                Timestamp.valueOf(LocalDateTime.of(2026, 4, 6, 10, 0))
        );
        jdbcTemplate.update(
                "insert into energy_account_order_flow(consume_id, account_id, balance_relation_id, balance_type, amount, begin_balance, end_balance, create_time) values (?, ?, ?, ?, ?, ?, ?, ?)",
                "FLOW-OUT", 302, 302, 0, new BigDecimal("15.00"), new BigDecimal("20.00"), new BigDecimal("35.00"),
                Timestamp.valueOf(LocalDateTime.of(2026, 4, 5, 23, 0))
        );

        List<Integer> accountIdList = reportAccountSourceRepository.findDailyAccountOrderFlowAccountIdList(buildReportDateRangeQo());

        List<Integer> sortedAccountIdList = new ArrayList<>(accountIdList);
        sortedAccountIdList.sort(Integer::compareTo);
        assertEquals(List.of(300, 301), sortedAccountIdList);
    }

    @Test
    @DisplayName("账户开户快照查询应返回稳定账户类型和主体信息")
    void testReportAccountSourceRepository_ShouldFindAccountOpenRecordByAccountIdList() {
        jdbcTemplate.update(
                "insert into energy_account_open_record(account_id, owner_id, owner_type, owner_name, electric_account_type, open_time, create_time, is_deleted) values (?, ?, ?, ?, ?, ?, ?, ?)",
                401, 9001, 1, "稳定业主A", 0,
                Timestamp.valueOf(LocalDateTime.of(2026, 4, 1, 10, 0)),
                Timestamp.valueOf(LocalDateTime.of(2026, 4, 1, 10, 0)), false
        );
        jdbcTemplate.update(
                "insert into energy_account_open_record(account_id, owner_id, owner_type, owner_name, electric_account_type, open_time, create_time, is_deleted) values (?, ?, ?, ?, ?, ?, ?, ?)",
                402, 9002, 0, "稳定业主B", 1,
                Timestamp.valueOf(LocalDateTime.of(2026, 4, 2, 11, 0)),
                Timestamp.valueOf(LocalDateTime.of(2026, 4, 2, 11, 0)), false
        );

        List<AccountOpenRecordEntity> accountOpenRecordList =
                reportAccountSourceRepository.findAccountOpenRecordListByAccountIdList(List.of(401, 402));

        assertEquals(2, accountOpenRecordList.size());
        assertEquals(401, accountOpenRecordList.get(0).getAccountId());
        assertEquals("稳定业主A", accountOpenRecordList.get(0).getOwnerName());
        assertEquals(1, accountOpenRecordList.get(1).getElectricAccountType());
    }

    @Test
    @DisplayName("全量销户查询应仅返回统计日开始前已全量销户的账户")
    void testReportAccountSourceRepository_ShouldFindFullCancelledAccountBeforeBeginTime() {
        jdbcTemplate.update(
                "insert into energy_account_cancel_record(account_id, full_cancel, create_time, is_deleted) values (?, ?, ?, ?)",
                501, true, Timestamp.valueOf(LocalDateTime.of(2026, 4, 5, 23, 59)), false
        );
        jdbcTemplate.update(
                "insert into energy_account_cancel_record(account_id, full_cancel, create_time, is_deleted) values (?, ?, ?, ?)",
                502, true, Timestamp.valueOf(LocalDateTime.of(2026, 4, 6, 0, 0)), false
        );
        jdbcTemplate.update(
                "insert into energy_account_cancel_record(account_id, full_cancel, create_time, is_deleted) values (?, ?, ?, ?)",
                503, false, Timestamp.valueOf(LocalDateTime.of(2026, 4, 5, 23, 0)), false
        );

        List<Integer> accountIdList = reportAccountSourceRepository.findFullCancelledAccountIdListBeforeTime(
                LocalDateTime.of(2026, 4, 6, 0, 0),
                List.of(501, 502, 503, 504));

        assertEquals(List.of(501), accountIdList);
    }

    private ReportDateRangeQo buildReportDateRangeQo() {
        return new ReportDateRangeQo()
                .setReportDate(LocalDate.of(2026, 4, 6))
                .setPreviousReportDate(LocalDate.of(2026, 4, 5))
                .setBeginTime(LocalDateTime.of(2026, 4, 6, 0, 0))
                .setEndTime(LocalDateTime.of(2026, 4, 7, 0, 0));
    }

    private void insertRechargeSource(String orderSn,
                                      Integer accountId,
                                      Integer balanceRelationId,
                                      Integer balanceType,
                                      LocalDateTime flowCreateTime,
                                      LocalDateTime orderSuccessTime,
                                      BigDecimal amount,
                                      BigDecimal serviceAmount) {
        jdbcTemplate.update(
                "insert into energy_account_order_flow(consume_id, account_id, balance_relation_id, balance_type, amount, begin_balance, end_balance, create_time) values (?, ?, ?, ?, ?, ?, ?, ?)",
                orderSn, accountId, balanceRelationId, balanceType, amount, new BigDecimal("10.00"),
                new BigDecimal("10.00").add(amount), Timestamp.valueOf(flowCreateTime)
        );
        jdbcTemplate.update(
                "insert into purchase_order(order_sn, user_id, user_real_name, user_phone, third_party_user_id, account_id, order_type, order_amount, order_status, order_create_time, order_success_time, service_amount, is_deleted) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                orderSn, 1, "测试用户", "13800138000", "third-user", accountId, 1, amount.add(serviceAmount),
                "SUCCESS", Timestamp.valueOf(orderSuccessTime.minusMinutes(5)), Timestamp.valueOf(orderSuccessTime), serviceAmount, false
        );
        jdbcTemplate.update(
                "insert into order_detail_energy_top_up(order_sn, account_id, meter_type, meter_id, balance_type, top_up_amount, create_time) values (?, ?, ?, ?, ?, ?, ?)",
                orderSn, accountId, 1, balanceRelationId, balanceType, amount, Timestamp.valueOf(flowCreateTime)
        );
    }
}
