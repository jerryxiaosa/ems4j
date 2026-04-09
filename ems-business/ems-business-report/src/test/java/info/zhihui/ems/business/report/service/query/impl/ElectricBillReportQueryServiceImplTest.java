package info.zhihui.ems.business.report.service.query.impl;

import info.zhihui.ems.business.account.bo.AccountBo;
import info.zhihui.ems.business.account.service.AccountInfoService;
import info.zhihui.ems.business.report.bo.ElectricBillReportDetailBo;
import info.zhihui.ems.business.report.dto.ElectricBillReportQueryDto;
import info.zhihui.ems.business.report.entity.DailyAccountReportEntity;
import info.zhihui.ems.business.report.entity.DailyMeterReportEntity;
import info.zhihui.ems.business.report.qo.ElectricBillAccountSummaryQo;
import info.zhihui.ems.business.report.qo.ElectricBillMeterCountQo;
import info.zhihui.ems.business.report.qo.ElectricBillReportQueryQo;
import info.zhihui.ems.business.report.repository.report.DailyAccountReportRepository;
import info.zhihui.ems.business.report.repository.report.DailyMeterReportRepository;
import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import info.zhihui.ems.common.exception.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ElectricBillReportQueryServiceImplTest {

    @Test
    @DisplayName("合并计费详情应保留电表单价和电费，仅隐藏充值补正字段")
    void testGetDetail_MergedAccount_ShouldKeepChargeFieldsButHideRechargeAndCorrection() {
        FakeDailyAccountReportRepository accountReportRepository = new FakeDailyAccountReportRepository();
        FakeDailyMeterReportRepository meterReportRepository = new FakeDailyMeterReportRepository();
        FakeAccountInfoService accountInfoService = new FakeAccountInfoService();
        ElectricBillReportQueryServiceImpl service = new ElectricBillReportQueryServiceImpl(
                accountReportRepository, meterReportRepository, accountInfoService
        );
        ElectricBillReportQueryDto queryBo = new ElectricBillReportQueryDto()
                .setStartDate(LocalDate.of(2026, 4, 1))
                .setEndDate(LocalDate.of(2026, 4, 8));
        accountReportRepository.accountSummaryToReturn = new ElectricBillAccountSummaryQo()
                .setAccountId(10)
                .setAccountName("企业A")
                .setElectricAccountType(ElectricAccountTypeEnum.MERGED.getCode())
                .setPeriodConsumePower(new BigDecimal("123.45"))
                .setPeriodElectricChargeAmount(new BigDecimal("88.00"))
                .setPeriodRechargeAmount(new BigDecimal("10.00"))
                .setPeriodCorrectionAmount(new BigDecimal("-3.00"));
        accountReportRepository.latestReportToReturn = new DailyAccountReportEntity().setEndBalance(new BigDecimal("66.66"));
        meterReportRepository.reportListByDateRangeToReturn = List.of(
                new DailyMeterReportEntity()
                        .setMeterId(100)
                        .setDeviceNo("M-100")
                        .setMeterName("一号表")
                        .setDisplayPriceHigh(new BigDecimal("0.6680"))
                        .setConsumePowerHigh(new BigDecimal("12.30"))
                        .setConsumePower(new BigDecimal("12.30"))
                        .setElectricChargeAmountHigh(new BigDecimal("8.22"))
                        .setElectricChargeAmount(new BigDecimal("15.00"))
                        .setRechargeAmount(new BigDecimal("20.00"))
                        .setCorrectionNetAmount(new BigDecimal("3.00"))
        );
        accountInfoService.accountBoToReturn = new AccountBo()
                .setContactName("张三")
                .setContactPhone("13800000000")
                .setMonthlyPayAmount(new BigDecimal("99.00"));

        ElectricBillReportDetailBo detailBo = service.getDetail(10, queryBo);

        assertThat(detailBo.getAccountInfo().getAccountBalance()).isEqualByComparingTo("66.66");
        assertThat(detailBo.getMeterList()).hasSize(1);
        assertThat(detailBo.getMeterList().get(0).getConsumePowerHigh()).isEqualByComparingTo("12.30");
        assertThat(detailBo.getMeterList().get(0).getDisplayPriceHigh()).isEqualByComparingTo("0.6680");
        assertThat(detailBo.getMeterList().get(0).getElectricChargeAmountHigh()).isEqualByComparingTo("8.22");
        assertThat(detailBo.getMeterList().get(0).getTotalElectricChargeAmount()).isEqualByComparingTo("15.00");
        assertThat(detailBo.getMeterList().get(0).getTotalRechargeAmount()).isNull();
        assertThat(detailBo.getMeterList().get(0).getTotalCorrectionAmount()).isNull();
    }

    @Test
    @DisplayName("按需计费详情应保留电表金额字段并在当前账户缺失时返回空联系人")
    void testGetDetail_QuantityAccount_ShouldKeepChargeFields() {
        FakeDailyAccountReportRepository accountReportRepository = new FakeDailyAccountReportRepository();
        FakeDailyMeterReportRepository meterReportRepository = new FakeDailyMeterReportRepository();
        FakeAccountInfoService accountInfoService = new FakeAccountInfoService();
        ElectricBillReportQueryServiceImpl service = new ElectricBillReportQueryServiceImpl(
                accountReportRepository, meterReportRepository, accountInfoService
        );
        ElectricBillReportQueryDto queryBo = new ElectricBillReportQueryDto()
                .setStartDate(LocalDate.of(2026, 4, 1))
                .setEndDate(LocalDate.of(2026, 4, 8));
        accountReportRepository.accountSummaryToReturn = new ElectricBillAccountSummaryQo()
                .setAccountId(11)
                .setAccountName("企业B")
                .setElectricAccountType(ElectricAccountTypeEnum.QUANTITY.getCode())
                .setPeriodConsumePower(new BigDecimal("100.00"))
                .setPeriodElectricChargeAmount(new BigDecimal("50.12"))
                .setPeriodRechargeAmount(new BigDecimal("30.00"))
                .setPeriodCorrectionAmount(new BigDecimal("5.00"));
        accountReportRepository.latestReportToReturn = new DailyAccountReportEntity().setEndBalance(new BigDecimal("80.50"));
        meterReportRepository.reportListByDateRangeToReturn = List.of(
                new DailyMeterReportEntity()
                        .setMeterId(101)
                        .setDeviceNo("M-101")
                        .setMeterName("二号表")
                        .setDisplayPriceHigh(new BigDecimal("0.80"))
                        .setConsumePowerHigh(new BigDecimal("30.00"))
                        .setConsumePower(new BigDecimal("30.00"))
                        .setElectricChargeAmountHigh(new BigDecimal("24.00"))
                        .setElectricChargeAmount(new BigDecimal("24.00"))
                        .setRechargeAmount(new BigDecimal("5.00"))
                        .setCorrectionNetAmount(new BigDecimal("1.00")),
                new DailyMeterReportEntity()
                        .setMeterId(101)
                        .setDeviceNo("M-101-N")
                        .setMeterName("二号表新")
                        .setDisplayPriceHigh(new BigDecimal("0.90"))
                        .setConsumePowerHigh(new BigDecimal("20.00"))
                        .setConsumePower(new BigDecimal("20.00"))
                        .setElectricChargeAmountHigh(new BigDecimal("18.00"))
                        .setElectricChargeAmount(new BigDecimal("18.00"))
                        .setRechargeAmount(new BigDecimal("2.00"))
                        .setCorrectionNetAmount(new BigDecimal("-0.50"))
        );
        accountInfoService.notFound = true;

        ElectricBillReportDetailBo detailBo = service.getDetail(11, queryBo);

        assertThat(detailBo.getAccountInfo().getContactName()).isNull();
        assertThat(detailBo.getMeterList()).hasSize(1);
        assertThat(detailBo.getMeterList().get(0).getMeterName()).isEqualTo("二号表新");
        assertThat(detailBo.getMeterList().get(0).getDisplayPriceHigh()).isEqualByComparingTo("0.90");
        assertThat(detailBo.getMeterList().get(0).getElectricChargeAmountHigh()).isEqualByComparingTo("42.00");
        assertThat(detailBo.getMeterList().get(0).getTotalElectricChargeAmount()).isEqualByComparingTo("42.00");
        assertThat(detailBo.getMeterList().get(0).getTotalRechargeAmount()).isEqualByComparingTo("7.00");
        assertThat(detailBo.getMeterList().get(0).getTotalCorrectionAmount()).isEqualByComparingTo("0.50");
        assertThat(detailBo.getMeterList().get(0).getTotalConsumePower()).isEqualByComparingTo("50.00");
    }

    private abstract static class BaseMapperStub<T> implements com.baomidou.mybatisplus.core.mapper.BaseMapper<T> {

        @Override
        public int insert(T entity) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int deleteById(T entity) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int delete(com.baomidou.mybatisplus.core.conditions.Wrapper<T> queryWrapper) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int updateById(T entity) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int update(T entity, com.baomidou.mybatisplus.core.conditions.Wrapper<T> updateWrapper) {
            throw new UnsupportedOperationException();
        }

        @Override
        public T selectById(java.io.Serializable id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<T> selectByIds(Collection<? extends java.io.Serializable> idList) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void selectByIds(Collection<? extends java.io.Serializable> idList,
                                org.apache.ibatis.session.ResultHandler<T> resultHandler) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Long selectCount(com.baomidou.mybatisplus.core.conditions.Wrapper<T> queryWrapper) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<T> selectList(com.baomidou.mybatisplus.core.conditions.Wrapper<T> queryWrapper) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void selectList(com.baomidou.mybatisplus.core.conditions.Wrapper<T> queryWrapper,
                               org.apache.ibatis.session.ResultHandler<T> resultHandler) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<T> selectList(com.baomidou.mybatisplus.core.metadata.IPage<T> page,
                                  com.baomidou.mybatisplus.core.conditions.Wrapper<T> queryWrapper) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void selectList(com.baomidou.mybatisplus.core.metadata.IPage<T> page,
                               com.baomidou.mybatisplus.core.conditions.Wrapper<T> queryWrapper,
                               org.apache.ibatis.session.ResultHandler<T> resultHandler) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Map<String, Object>> selectMaps(com.baomidou.mybatisplus.core.conditions.Wrapper<T> queryWrapper) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void selectMaps(com.baomidou.mybatisplus.core.conditions.Wrapper<T> queryWrapper,
                               org.apache.ibatis.session.ResultHandler<Map<String, Object>> resultHandler) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Map<String, Object>> selectMaps(com.baomidou.mybatisplus.core.metadata.IPage<? extends Map<String, Object>> page,
                                                    com.baomidou.mybatisplus.core.conditions.Wrapper<T> queryWrapper) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void selectMaps(com.baomidou.mybatisplus.core.metadata.IPage<? extends Map<String, Object>> page,
                               com.baomidou.mybatisplus.core.conditions.Wrapper<T> queryWrapper,
                               org.apache.ibatis.session.ResultHandler<Map<String, Object>> resultHandler) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <E> List<E> selectObjs(com.baomidou.mybatisplus.core.conditions.Wrapper<T> queryWrapper) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <E> void selectObjs(com.baomidou.mybatisplus.core.conditions.Wrapper<T> queryWrapper,
                                   org.apache.ibatis.session.ResultHandler<E> resultHandler) {
            throw new UnsupportedOperationException();
        }
    }

    private static class FakeDailyAccountReportRepository extends BaseMapperStub<DailyAccountReportEntity>
            implements DailyAccountReportRepository {

        private ElectricBillAccountSummaryQo accountSummaryToReturn;

        private DailyAccountReportEntity latestReportToReturn;

        @Override
        public int deleteByReportDate(LocalDate reportDate) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<DailyAccountReportEntity> findListByReportDate(LocalDate reportDate) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Integer> findAccountIdListByReportDate(LocalDate reportDate) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<DailyAccountReportEntity> findListByReportDateAndAccountIdList(LocalDate reportDate,
                                                                                    List<Integer> accountIdList) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<ElectricBillAccountSummaryQo> findElectricBillAccountPageList(ElectricBillReportQueryQo queryQo) {
            return Collections.emptyList();
        }

        @Override
        public ElectricBillAccountSummaryQo getElectricBillAccountSummary(Integer accountId,
                                                                          LocalDate startDate,
                                                                          LocalDate endDate) {
            return accountSummaryToReturn;
        }

        @Override
        public DailyAccountReportEntity getLatestByAccountIdAndDateRange(Integer accountId,
                                                                         LocalDate startDate,
                                                                         LocalDate endDate) {
            return latestReportToReturn;
        }
    }

    private static class FakeDailyMeterReportRepository extends BaseMapperStub<DailyMeterReportEntity>
            implements DailyMeterReportRepository {

        private List<DailyMeterReportEntity> reportListByDateRangeToReturn = new ArrayList<>();

        @Override
        public int deleteByReportDate(LocalDate reportDate) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<DailyMeterReportEntity> findListByReportDate(LocalDate reportDate) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<DailyMeterReportEntity> findActiveListByReportDateAndAccountIdList(LocalDate reportDate,
                                                                                        List<Integer> accountIdList) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<ElectricBillMeterCountQo> findMeterCountListByAccountIdList(LocalDate startDate,
                                                                                LocalDate endDate,
                                                                                List<Integer> accountIdList) {
            return Collections.emptyList();
        }

        @Override
        public List<DailyMeterReportEntity> findListByAccountIdAndDateRange(Integer accountId,
                                                                            LocalDate startDate,
                                                                            LocalDate endDate) {
            return reportListByDateRangeToReturn;
        }
    }

    private static class FakeAccountInfoService implements AccountInfoService {

        private AccountBo accountBoToReturn;

        private boolean notFound;

        @Override
        public List<AccountBo> findList(info.zhihui.ems.business.account.dto.AccountQueryDto query) {
            throw new UnsupportedOperationException();
        }

        @Override
        public info.zhihui.ems.common.paging.PageResult<AccountBo> findPage(info.zhihui.ems.business.account.dto.AccountQueryDto query,
                                                                            info.zhihui.ems.common.paging.PageParam pageParam) {
            throw new UnsupportedOperationException();
        }

        @Override
        public AccountBo getById(Integer id) {
            if (notFound) {
                throw new NotFoundException("账户不存在");
            }
            return accountBoToReturn;
        }

        @Override
        public info.zhihui.ems.common.paging.PageResult<info.zhihui.ems.business.account.dto.AccountCancelRecordDto> findCancelRecordPage(
                info.zhihui.ems.business.account.dto.AccountCancelQueryDto query,
                info.zhihui.ems.common.paging.PageParam pageParam) {
            throw new UnsupportedOperationException();
        }

        @Override
        public info.zhihui.ems.business.account.dto.AccountCancelDetailDto getCancelRecordDetail(String cancelNo) {
            throw new UnsupportedOperationException();
        }
    }
}
