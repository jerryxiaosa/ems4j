package info.zhihui.ems.business.account;

import info.zhihui.ems.business.account.bo.AccountBo;
import info.zhihui.ems.business.account.dto.AccountCancelDetailDto;
import info.zhihui.ems.business.account.dto.AccountCancelQueryDto;
import info.zhihui.ems.business.account.dto.AccountCancelRecordDto;
import info.zhihui.ems.business.account.dto.AccountQueryDto;
import info.zhihui.ems.business.account.enums.CleanBalanceTypeEnum;
import info.zhihui.ems.business.account.service.AccountInfoService;
import info.zhihui.ems.business.device.dto.CanceledMeterDto;
import info.zhihui.ems.common.enums.OwnerTypeEnum;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AccountInfoService集成测试
 *
 * @author jerryxiaosa
 */
@SpringBootTest
@ActiveProfiles("integrationtest")
@Transactional
@Rollback
class AccountInfoServiceImplIntegrationTest {

    @Autowired
    private AccountInfoService accountInfoService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("参数校验测试 - 覆盖所有方法的null参数和有效参数场景")
    void testAccountInfoService_ValidationTests_ShouldThrowException() {
        // 测试1: findList方法 - null参数
        assertThrows(ConstraintViolationException.class, () -> {
            accountInfoService.findList(null);
        }, "findList方法null参数应抛出ConstraintViolationException");

        // 测试2: findPage方法 - null查询参数
        assertThrows(ConstraintViolationException.class, () -> {
            accountInfoService.findPage(null, new PageParam());
        }, "findPage方法null查询参数应抛出ConstraintViolationException");

        // 测试3: findPage方法 - null分页参数
        assertThrows(ConstraintViolationException.class, () -> {
            accountInfoService.findPage(new AccountQueryDto(), null);
        }, "findPage方法null分页参数应抛出ConstraintViolationException");

        // 测试4: getById方法 - null参数
        assertThrows(ConstraintViolationException.class, () -> {
            accountInfoService.getById(null);
        }, "getById方法null参数应抛出ConstraintViolationException");

        // 测试5: findCancelRecordPage方法 - null参数
        assertThrows(ConstraintViolationException.class, () -> {
            accountInfoService.findCancelRecordPage(null, new PageParam());
        }, "findCancelRecordPage方法null查询参数应抛出ConstraintViolationException");

        assertThrows(ConstraintViolationException.class, () -> {
            accountInfoService.findCancelRecordPage(new AccountCancelQueryDto(), null);
        }, "findCancelRecordPage方法null分页参数应抛出ConstraintViolationException");

        // 测试6: getCancelRecordDetail方法 - null参数
        assertThrows(ConstraintViolationException.class, () -> {
            accountInfoService.getCancelRecordDetail(null);
        }, "getCancelRecordDetail方法null参数应抛出ConstraintViolationException");

        // 测试7: findList方法 - 有效参数（不应抛出参数校验异常）
        AccountQueryDto query = new AccountQueryDto();
        try {
            accountInfoService.findList(query);
        } catch (ConstraintViolationException e) {
            throw new AssertionError("findList方法有效参数不应抛出ConstraintViolationException", e);
        } catch (Exception e) {
            // 其他异常（如业务异常）是可以接受的，我们只关心参数校验
        }

        // 测试8: findPage方法 - 有效参数（不应抛出参数校验异常）
        try {
            accountInfoService.findPage(query, new PageParam().setPageNum(1).setPageSize(10));
        } catch (ConstraintViolationException e) {
            throw new AssertionError("findPage方法有效参数不应抛出ConstraintViolationException", e);
        } catch (Exception e) {
            // 其他异常（如业务异常）是可以接受的，我们只关心参数校验
        }

    }

    @Test
    @DisplayName("findList方法集成测试 - 测试正常查询功能")
    void testFindList_Success() {
        // 准备测试数据 - 查询所有账户
        AccountQueryDto query = new AccountQueryDto();

        // 执行测试
        List<AccountBo> result = accountInfoService.findList(query);

        // 验证结果
        assertNotNull(result, "查询结果不应为null");
        assertFalse(result.isEmpty(), "应该查询到测试数据");

        // 验证第一个账户的基本信息
        AccountBo firstAccount = result.get(0);
        assertNotNull(firstAccount.getId(), "账户ID不应为null");
        assertNotNull(firstAccount.getOwnerType(), "账户类型不应为null");
        assertNotNull(firstAccount.getOwnerId(), "归属者ID不应为null");

        // 测试按条件查询 - 查询特定归属者类型的账户
        AccountQueryDto specificQuery = new AccountQueryDto()
                .setOwnerType(OwnerTypeEnum.ENTERPRISE)
                .setOwnerIds(List.of(1001));

        List<AccountBo> specificResult = accountInfoService.findList(specificQuery);
        assertNotNull(specificResult, "特定条件查询结果不应为null");

        AccountBo account = specificResult.get(0);
        assertEquals(OwnerTypeEnum.ENTERPRISE, account.getOwnerType(), "查询结果应匹配指定的归属者类型");
        assertEquals(1001, account.getOwnerId(), "查询结果应匹配指定的归属者ID");
    }

    @Test
    @DisplayName("findList方法集成测试 - 覆盖所有查询字段条件")
    void testFindList_AllFilterFields_Coverage() {
        // 覆盖 includeDeleted / ownerType / ownerIds / ownerNameLike / electricAccountType / warnPlanId
        AccountQueryDto q = new AccountQueryDto()
                .setIncludeDeleted(Boolean.TRUE)
                .setOwnerType(OwnerTypeEnum.ENTERPRISE)
                .setOwnerIds(List.of(1001, 1002))
                .setOwnerNameLike("账户")
                .setElectricAccountType(info.zhihui.ems.common.enums.ElectricAccountTypeEnum.MONTHLY)
                .setWarnPlanId(1);

        List<AccountBo> list = accountInfoService.findList(q);
        assertNotNull(list, "findList 应返回非null列表");

        // 再次调用以覆盖 includeDeleted=false 的分支
        q.setIncludeDeleted(Boolean.FALSE);
        List<AccountBo> list2 = accountInfoService.findList(q);
        assertNotNull(list2);
        assertFalse(list2.isEmpty());
    }

    @Test
    @DisplayName("findPage方法集成测试 - 测试正常分页查询功能")
    void testFindPage_Success() {
        AccountQueryDto query = new AccountQueryDto();
        PageParam pageParam = new PageParam().setPageNum(1).setPageSize(2);

        PageResult<AccountBo> result = accountInfoService.findPage(query, pageParam);

        assertNotNull(result, "分页查询结果不应为null");
        assertNotNull(result.getList(), "分页数据列表不应为null");
        assertTrue(result.getTotal() >= 0, "总数应大于等于0");
        assertTrue(result.getPageNum() > 0, "页码应大于0");
        assertTrue(result.getPageSize() > 0, "每页数量应大于0");
        assertFalse(result.getList().isEmpty(), "分页结果应包含数据");

        AccountBo firstAccount = result.getList().get(0);
        assertNotNull(firstAccount.getId(), "账户ID不应为null");
        assertNotNull(firstAccount.getOwnerType(), "账户类型不应为null");
        assertNotNull(firstAccount.getOwnerId(), "归属者ID不应为null");

        AccountQueryDto specificQuery = new AccountQueryDto()
                .setOwnerType(OwnerTypeEnum.ENTERPRISE)
                .setOwnerIds(List.of(1001));
        PageResult<AccountBo> specificResult = accountInfoService.findPage(
                specificQuery,
                new PageParam().setPageNum(1).setPageSize(10)
        );
        assertNotNull(specificResult, "特定条件分页结果不应为null");
        assertNotNull(specificResult.getList(), "特定条件分页列表不应为null");
        assertFalse(specificResult.getList().isEmpty(), "特定条件分页结果应有数据");
        assertEquals(1L, specificResult.getTotal(), "特定条件总数应为1");
        assertEquals(OwnerTypeEnum.ENTERPRISE, specificResult.getList().get(0).getOwnerType(), "归属者类型应匹配");
        assertEquals(1001, specificResult.getList().get(0).getOwnerId(), "归属者ID应匹配");
    }

    @Test
    @DisplayName("findPage方法集成测试 - 测试分页功能")
    void testFindPage_Pagination() {
        AccountQueryDto query = new AccountQueryDto();
        PageParam firstPageParam = new PageParam().setPageNum(1).setPageSize(2);
        PageResult<AccountBo> firstPageResult = accountInfoService.findPage(query, firstPageParam);

        assertNotNull(firstPageResult, "第一页结果不应为null");
        assertNotNull(firstPageResult.getList(), "第一页数据列表不应为null");

        if (firstPageResult.getTotal() > 2) {
            PageParam secondPageParam = new PageParam().setPageNum(2).setPageSize(2);
            PageResult<AccountBo> secondPageResult = accountInfoService.findPage(query, secondPageParam);

            assertNotNull(secondPageResult, "第二页结果不应为null");
            assertNotNull(secondPageResult.getList(), "第二页数据列表不应为null");
            assertEquals(firstPageResult.getTotal(), secondPageResult.getTotal(), "两页总数应一致");
            assertFalse(secondPageResult.getList().isEmpty(), "第二页应包含数据");

            Integer firstPageFirstId = firstPageResult.getList().get(0).getId();
            Integer secondPageFirstId = secondPageResult.getList().get(0).getId();
            assertTrue(firstPageFirstId > secondPageFirstId, "按ID倒序分页时，第一页首条ID应大于第二页首条ID");
        }
    }

    @Test
    @DisplayName("getById方法集成测试 - 测试根据ID查询账户功能")
    void testGetById_Success() {
        // 准备测试数据 - 使用测试数据中存在的账户ID
        Integer testAccountId = 1;

        // 执行测试
        AccountBo result = accountInfoService.getById(testAccountId);

        // 验证结果
        assertNotNull(result, "查询结果不应为null");
        assertEquals(testAccountId, result.getId(), "返回的账户ID应与查询ID一致");
        assertNotNull(result.getOwnerType(), "账户类型不应为null");
        assertNotNull(result.getOwnerId(), "归属者ID不应为null");
        assertNotNull(result.getElectricAccountType(), "电费计费类型不应为null");

        // 验证账户的基本属性
        assertTrue(result.getOwnerId() > 0, "归属者ID应大于0");

        // 测试查询不存在的账户ID
        Integer nonExistentId = 99999;
        assertThrows(Exception.class, () -> {
            accountInfoService.getById(nonExistentId);
        }, "查询不存在的账户ID应抛出异常");
    }

    @Test
    @DisplayName("findCancelRecordPage方法集成测试 - 测试销户记录分页查询功能和MapStruct转换")
    void testFindCancelRecordPage_Success() {
        // 准备测试数据
        AccountCancelQueryDto queryDto = new AccountCancelQueryDto()
                .setOwnerName("测试企业")
                .setCleanBalanceType(CleanBalanceTypeEnum.REFUND);
        PageParam pageParam = new PageParam().setPageNum(1).setPageSize(10);

        // 执行测试
        PageResult<AccountCancelRecordDto> result = accountInfoService.findCancelRecordPage(queryDto, pageParam);

        // 验证分页结果
        assertNotNull(result, "分页查询结果不应为null");
        assertFalse(result.getList().isEmpty(), "分页数据列表不应为空");
        assertNotNull(result.getList(), "分页数据列表不应为null");
        assertNotNull(result.getTotal(), "分页总数不应为null");
        assertTrue(result.getTotal() >= 0, "分页总数应大于等于0");

        // 验证MapStruct转换的正确性
        AccountCancelRecordDto firstRecord = result.getList().get(0);

        // 验证所有字段的MapStruct转换
        assertNotNull(firstRecord.getCancelNo(), "销户编号不应为null");
        assertNotNull(firstRecord.getOwnerName(), "归属者名称不应为null");
        assertNotNull(firstRecord.getCleanBalanceType(), "余额处理方式不应为null");
        assertNotNull(firstRecord.getCancelTime(), "销户时间不应为null");
        assertNotNull(firstRecord.getCleanBalanceReal(), "退费金额不应为null");
        assertNotNull(firstRecord.getOperatorName(), "操作员姓名不应为null");

        // 验证字符串字段不为空
        assertFalse(firstRecord.getCancelNo().trim().isEmpty(), "销户编号不应为空字符串");
        assertFalse(firstRecord.getOwnerName().trim().isEmpty(), "归属者名称不应为空字符串");
        assertFalse(firstRecord.getOperatorName().trim().isEmpty(), "操作员姓名不应为空字符串");
    }

    @Test
    @DisplayName("findCancelRecordPage方法集成测试 - 测试空结果场景")
    void testFindCancelRecordPage_EmptyResult() {
        // 准备测试数据 - 使用不存在的查询条件
        AccountCancelQueryDto queryDto = new AccountCancelQueryDto()
                .setOwnerName("不存在的企业名称")
                .setCleanBalanceType(CleanBalanceTypeEnum.PAY);
        PageParam pageParam = new PageParam().setPageNum(1).setPageSize(10);

        // 执行测试
        PageResult<AccountCancelRecordDto> result = accountInfoService.findCancelRecordPage(queryDto, pageParam);

        // 验证空结果
        assertNotNull(result, "分页查询结果不应为null");
        assertNotNull(result.getList(), "分页数据列表不应为null");
        assertTrue(result.getList().isEmpty(), "应该返回空列表");
        assertEquals(0L, result.getTotal(), "总数应为0");
    }

    @Test
    @DisplayName("findCancelRecordPage方法集成测试 - 测试分页功能")
    void testFindCancelRecordPage_Pagination() {
        // 测试第一页
        AccountCancelQueryDto queryDto = new AccountCancelQueryDto();
        PageParam firstPageParam = new PageParam().setPageNum(1).setPageSize(2);

        PageResult<AccountCancelRecordDto> firstPageResult = accountInfoService.findCancelRecordPage(queryDto, firstPageParam);

        // 验证分页结果
        assertNotNull(firstPageResult, "第一页结果不应为null");
        assertNotNull(firstPageResult.getList(), "第一页数据列表不应为null");

        if (firstPageResult.getTotal() > 2) {
            // 如果总数大于2，测试第二页
            PageParam secondPageParam = new PageParam().setPageNum(2).setPageSize(2);
            PageResult<AccountCancelRecordDto> secondPageResult = accountInfoService.findCancelRecordPage(queryDto, secondPageParam);

            assertNotNull(secondPageResult, "第二页结果不应为null");
            assertNotNull(secondPageResult.getList(), "第二页数据列表不应为null");
            assertEquals(firstPageResult.getTotal(), secondPageResult.getTotal(), "两页的总数应该相同");
        }
    }

    @Test
    @DisplayName("getCancelRecordDetail方法集成测试 - 测试销户记录详情查询和MapStruct转换")
    void testGetCancelRecordDetail_Success() {
        // 使用测试数据中存在的销户编号
        String cancelNo = "CANCEL001";

        // 执行测试
        AccountCancelDetailDto result = accountInfoService.getCancelRecordDetail(cancelNo);

        // 验证基本结果
        assertNotNull(result, "销户详情结果不应为null");

        // 验证所有字段的MapStruct转换正确性
        assertNotNull(result.getCancelNo(), "销户编号不应为null");
        assertEquals(cancelNo, result.getCancelNo(), "销户编号应与查询参数一致");

        assertNotNull(result.getOwnerName(), "归属者名称不应为null");
        assertNotNull(result.getCleanBalanceType(), "余额处理方式不应为null");
        assertNotNull(result.getCancelTime(), "销户时间不应为null");
        assertNotNull(result.getCleanBalanceReal(), "退费金额不应为null");
        assertNotNull(result.getOperatorName(), "操作员姓名不应为null");
        assertNotNull(result.getRemark(), "销户原因不应为null");
        assertNotNull(result.getCleanBalanceType(), "账户余额不应为null");
        assertNotNull(result.getMeterList(), "设备明细列表不应为null");

        // 验证字符串字段不为空
        assertFalse(result.getCancelNo().trim().isEmpty(), "销户编号不应为空字符串");
        assertFalse(result.getOwnerName().trim().isEmpty(), "归属者名称不应为空字符串");
        assertFalse(result.getOperatorName().trim().isEmpty(), "操作员姓名不应为空字符串");

        // 验证设备明细列表的MapStruct转换
        if (!result.getMeterList().isEmpty()) {
            CanceledMeterDto firstMeter = result.getMeterList().get(0);

            // 验证销户设备明细的MapStruct转换
            if (firstMeter != null) {
                assertNotNull(firstMeter.getSpaceName(), "设备空间名称不应为null");
                assertNotNull(firstMeter.getMeterName(), "设备名称不应为null");
                assertNotNull(firstMeter.getMeterNo(), "设备编号不应为null");
                assertNotNull(firstMeter.getMeterType(), "设备类型不应为null");
                assertNotNull(firstMeter.getBalance(), "设备余额不应为null");

                // 验证枚举转换
                assertTrue(firstMeter.getBalance().compareTo(BigDecimal.ZERO) >= 0, "设备余额应大于等于0");

                // 验证字符串字段不为空
                assertFalse(firstMeter.getMeterNo().trim().isEmpty(), "设备编号不应为空字符串");
                assertFalse(firstMeter.getMeterName().trim().isEmpty(), "设备名称不应为空字符串");
                assertFalse(firstMeter.getSpaceName().trim().isEmpty(), "设备空间名称不应为空字符串");
            }
            // CanceledMeterDto 只包含 spaceName, spaceParentNames, meterName, meterNo, meterType, balance 字段
        }
    }

    @Test
    @DisplayName("getCancelRecordDetail方法集成测试 - 测试不存在的销户记录")
    void testGetCancelRecordDetail_NotFound() {
        // 使用不存在的销户编号
        String nonExistentCancelNo = "NONEXISTENT_CANCEL_NO";

        // 执行测试并验证异常
        Exception exception = assertThrows(Exception.class, () -> {
            accountInfoService.getCancelRecordDetail(nonExistentCancelNo);
        }, "查询不存在的销户记录应抛出异常");

        // 验证异常信息
        assertTrue(exception.getMessage().contains("销户记录不存在") ||
                        exception.getMessage().contains("not found"),
                "异常信息应包含销户记录不存在的提示");
    }

    @Test
    @DisplayName("销户查询接口集成测试 - 测试枚举类型转换的完整性")
    void testCancelRecordEnumConversion() {
        // 测试所有枚举类型的转换
        AccountCancelQueryDto queryDto = new AccountCancelQueryDto();

        // 测试REFUND枚举
        queryDto.setCleanBalanceType(CleanBalanceTypeEnum.REFUND);
        PageResult<AccountCancelRecordDto> refundResult = accountInfoService.findCancelRecordPage(queryDto, new PageParam());
        assertNotNull(refundResult, "REFUND枚举查询结果不应为null");

        // 测试PAY枚举
        queryDto.setCleanBalanceType(CleanBalanceTypeEnum.PAY);
        PageResult<AccountCancelRecordDto> payResult = accountInfoService.findCancelRecordPage(queryDto, new PageParam());
        assertNotNull(payResult, "PAY枚举查询结果不应为null");

        // 测试SKIP枚举
        queryDto.setCleanBalanceType(CleanBalanceTypeEnum.SKIP);
        PageResult<AccountCancelRecordDto> skipResult = accountInfoService.findCancelRecordPage(queryDto, new PageParam());
        assertNotNull(skipResult, "SKIP枚举查询结果不应为null");

        // 验证枚举转换在结果中的正确性
        if (!refundResult.getList().isEmpty()) {
            AccountCancelRecordDto record = refundResult.getList().get(0);
            assertNotNull(record.getCleanBalanceType(), "枚举字段不应为null");
        }
    }
}
