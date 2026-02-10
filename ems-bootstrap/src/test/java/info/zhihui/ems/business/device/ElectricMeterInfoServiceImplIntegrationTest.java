package info.zhihui.ems.business.device;

import info.zhihui.ems.common.enums.CalculateTypeEnum;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.business.device.bo.ElectricMeterBo;
import info.zhihui.ems.business.device.dto.ElectricMeterQueryDto;
import info.zhihui.ems.business.device.dto.CanceledMeterDto;
import info.zhihui.ems.business.device.service.ElectricMeterInfoService;
import info.zhihui.ems.common.enums.MeterTypeEnum;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ElectricMeterInfoService集成测试
 *
 * @author jerryxiaosa
 */
@SpringBootTest
@ActiveProfiles("integrationtest")
@Transactional
@Rollback
class ElectricMeterInfoServiceImplIntegrationTest {

    @Autowired
    private ElectricMeterInfoService electricMeterInfoService;

    @Test
    @DisplayName("参数校验测试 - 覆盖所有方法的null参数和有效参数场景")
    void testElectricMeterInfoService_ValidationTests_ShouldThrowException() {
        // 测试1: findPage方法 - null query参数
        assertThrows(ConstraintViolationException.class, () -> {
            electricMeterInfoService.findPage(null, new PageParam());
        }, "findPage方法null query参数应抛出ConstraintViolationException");

        // 测试2: findPage方法 - null pageParam参数
        assertThrows(ConstraintViolationException.class, () -> {
            electricMeterInfoService.findPage(new ElectricMeterQueryDto(), null);
        }, "findPage方法null pageParam参数应抛出ConstraintViolationException");

        // 测试3: findList方法 - null参数
        assertThrows(ConstraintViolationException.class, () -> {
            electricMeterInfoService.findList(null);
        }, "findList方法null参数应抛出ConstraintViolationException");

        // 测试4: getDetail方法 - null参数
        assertThrows(ConstraintViolationException.class, () -> {
            electricMeterInfoService.getDetail(null);
        }, "getDetail方法null参数应抛出ConstraintViolationException");

        // 测试5: getByIotId方法 - null参数
        assertThrows(ConstraintViolationException.class, () -> {
            electricMeterInfoService.getByIotId(null);
        }, "getByIotId方法null参数应抛出ConstraintViolationException");

        // 测试6: findMetersByCancelNo方法 - null参数
        assertThrows(ConstraintViolationException.class, () -> {
            electricMeterInfoService.findMetersByCancelNo(null);
        }, "findMetersByCancelNo方法null参数应抛出ConstraintViolationException");

        // 测试7: findMetersByCancelNo方法 - 空字符串参数
        assertThrows(ConstraintViolationException.class, () -> {
            electricMeterInfoService.findMetersByCancelNo("");
        }, "findMetersByCancelNo方法空字符串参数应抛出ConstraintViolationException");

        // 测试8: findPage方法 - 有效参数（不应抛出参数校验异常）
        ElectricMeterQueryDto query = new ElectricMeterQueryDto();
        PageParam pageParam = new PageParam();
        pageParam.setPageNum(1);
        pageParam.setPageSize(10);
        try {
            electricMeterInfoService.findPage(query, pageParam);
        } catch (ConstraintViolationException e) {
            throw new AssertionError("findPage方法有效参数不应抛出ConstraintViolationException", e);
        } catch (Exception e) {
            // 其他异常（如业务异常）是可以接受的，我们只关心参数校验
        }
    }

    @Test
    @DisplayName("findPage方法集成测试 - 测试分页查询功能")
    void testFindPage_Success() {
        // 准备测试数据 - 查询所有电表
        ElectricMeterQueryDto query = new ElectricMeterQueryDto();
        PageParam pageParam = new PageParam();
        pageParam.setPageNum(1);
        pageParam.setPageSize(10);

        // 执行测试
        PageResult<ElectricMeterBo> result = electricMeterInfoService.findPage(query, pageParam);

        // 验证结果
        assertNotNull(result, "分页查询结果不应为null");
        assertNotNull(result.getList(), "分页记录不应为null");
        assertTrue(result.getTotal() > 0, "总记录数应大于等于0");
        assertTrue(result.getPageNum() > 0, "页码应大于0");
        assertTrue(result.getPageSize() > 0, "每页大小应大于0");

        ElectricMeterBo firstMeter = result.getList().get(0);
        assertNotNull(firstMeter.getId(), "电表ID不应为null");
        assertNotNull(firstMeter.getIotId(), "物联网设备ID不应为null");
        assertNotNull(firstMeter.getMeterNo(), "电表编号不应为null");
    }

    @Test
    @DisplayName("findList方法集成测试 - 测试列表查询功能")
    void testFindList_Success() {
        // 准备测试数据 - 查询所有电表
        ElectricMeterQueryDto query = new ElectricMeterQueryDto();

        // 执行测试
        List<ElectricMeterBo> result = electricMeterInfoService.findList(query);

        // 验证结果
        assertNotNull(result, "查询结果不应为null");

        ElectricMeterBo firstMeter = result.get(0);
        assertNotNull(firstMeter.getId(), "电表ID不应为null");
        assertNotNull(firstMeter.getIotId(), "物联网设备ID不应为null");
        assertNotNull(firstMeter.getMeterNo(), "电表编号不应为null");
        assertNotNull(firstMeter.getAccountId(), "账户ID不应为null");
    }

    @Test
    @DisplayName("getDetail方法集成测试 - 测试根据ID查询电表详情功能")
    void testGetDetail_Success() {
        // 准备测试数据 - 使用测试数据中存在的电表ID
        Integer testMeterId = 1;

        // 执行测试
        ElectricMeterBo result = electricMeterInfoService.getDetail(testMeterId);

        // 验证结果
        assertNotNull(result, "查询结果不应为null");
        assertEquals(testMeterId, result.getId(), "返回的电表ID应与查询ID一致");
        assertNotNull(result.getIotId(), "物联网设备ID不应为null");
        assertNotNull(result.getMeterNo(), "电表编号不应为null");
        assertNotNull(result.getAccountId(), "账户ID不应为null");

        // 验证电表的基本属性
        assertFalse(result.getIotId().isBlank(), "物联网设备ID不应为空");
        assertNotNull(result.getMeterName(), "电表名称不应为null");

        // 测试查询不存在的电表ID
        Integer nonExistentId = 99999;
        assertThrows(Exception.class, () -> {
            electricMeterInfoService.getDetail(nonExistentId);
        }, "查询不存在的电表ID应抛出异常");
    }

    @Test
    @DisplayName("getByIotId方法集成测试 - 测试根据物联网设备ID查询电表功能")
    void testGetByIotId_Success() {
        // 准备测试数据 - 使用测试数据中存在的物联网设备ID
        String testIotId = "1001";

        // 执行测试
        ElectricMeterBo result = electricMeterInfoService.getByIotId(testIotId);

        // 验证结果
        assertNotNull(result, "查询结果不应为null");
        assertEquals(testIotId, result.getIotId(), "返回的物联网设备ID应与查询ID一致");
        assertNotNull(result.getId(), "电表ID不应为null");
        assertNotNull(result.getMeterNo(), "电表编号不应为null");
        assertNotNull(result.getAccountId(), "账户ID不应为null");

        // 验证电表的基本属性
        assertTrue(result.getId() > 0, "电表ID应大于0");
        assertNotNull(result.getMeterName(), "电表名称不应为null");

        // 测试查询不存在的物联网设备ID
        String nonExistentIotId = "99999";
        assertThrows(Exception.class, () -> {
            electricMeterInfoService.getByIotId(nonExistentIotId);
        }, "查询不存在的物联网设备ID应抛出异常");
    }

    @Test
    @DisplayName("findList方法集成测试 - 覆盖所有查询字段条件")
    void testFindList_AllFilterFields_Coverage() {
        ElectricMeterQueryDto q = new ElectricMeterQueryDto();
        q.setMeterId(1);
        q.setMeterName("EM");
        q.setMeterNo("EM");
        q.setAccountId(1);
        q.setIotId("1");
        q.setGatewayId(1);
        q.setIsOnline(true);
        q.setIsCutOff(false);
        q.setIsCalculate(true);
        q.setCalculateType(CalculateTypeEnum.AIR_CONDITIONING);
        q.setIsPrepay(true);
        q.setPortNo(1);
        q.setMeterAddress(1);
        q.setAreaId(1);
        q.setImei("IMEI");
        q.setSearchKey("EM");
        q.setNeId(99999);
        q.setInIds(List.of(1, 2));
        q.setSpaceIds(List.of(1, 100));

        List<ElectricMeterBo> list = electricMeterInfoService.findList(q);
        assertNotNull(list, "findList 应返回非null列表（即使无匹配数据）");
    }

    @Test
    @DisplayName("findMetersByCancelNo方法集成测试 - 测试根据销户编号查询设备明细和MapStruct转换")
    void testFindMetersByCancelNo_Success() {
        // 使用测试数据中存在的销户编号
        String cancelNo = "CANCEL001";

        // 执行测试
        List<CanceledMeterDto> result = electricMeterInfoService.findMetersByCancelNo(cancelNo);

        // 验证基本结果
        assertNotNull(result, "查询结果不应为null");

        // 如果有数据，验证MapStruct转换的正确性
        if (!result.isEmpty()) {
            CanceledMeterDto firstMeter = result.get(0);

            // 验证所有字段的MapStruct转换正确性
            assertNotNull(firstMeter.getSpaceName(), "空间名称不应为null");
            assertNotNull(firstMeter.getSpaceParentNames(), "空间父级名称不应为null");
            assertNotNull(firstMeter.getMeterName(), "表名称不应为null");
            assertNotNull(firstMeter.getMeterNo(), "表具号不应为null");
            assertNotNull(firstMeter.getMeterType(), "表类型不应为null");
            assertNotNull(firstMeter.getBalance(), "表余额不应为null");


            // 验证数值类型转换
            assertTrue(firstMeter.getBalance().compareTo(BigDecimal.ZERO) >= 0,
                "表余额应大于等于0");

            // 验证字符串字段不为空
            assertFalse(firstMeter.getSpaceName().trim().isEmpty(), "空间名称不应为空字符串");
            assertFalse(firstMeter.getMeterName().trim().isEmpty(), "表名称不应为空字符串");
            assertFalse(firstMeter.getMeterNo().trim().isEmpty(), "表具号不应为空字符串");

            // 验证枚举值的有效性
            MeterTypeEnum meterType = firstMeter.getMeterType();
            assertTrue(meterType == MeterTypeEnum.ELECTRIC || meterType == MeterTypeEnum.WATER,
                "表类型应为电表或水表");
        }
    }

    @Test
    @DisplayName("findMetersByCancelNo方法集成测试 - 测试空结果场景")
    void testFindMetersByCancelNo_EmptyResult() {
        // 使用不存在的销户编号
        String nonExistentCancelNo = "NONEXISTENT_CANCEL_NO";

        // 执行测试
        List<CanceledMeterDto> result = electricMeterInfoService.findMetersByCancelNo(nonExistentCancelNo);

        // 验证空结果
        assertNotNull(result, "查询结果不应为null");
        assertTrue(result.isEmpty(), "不存在的销户编号应返回空列表");
    }

    @Test
    @DisplayName("findMetersByCancelNo方法集成测试 - 测试多条记录的MapStruct转换")
    void testFindMetersByCancelNo_MultipleRecords() {
        // 使用可能有多条记录的销户编号
        String cancelNo = "CANCEL002";

        // 执行测试
        List<CanceledMeterDto> result = electricMeterInfoService.findMetersByCancelNo(cancelNo);

        // 验证结果
        assertNotNull(result, "查询结果不应为null");

        // 如果有多条记录，验证每条记录的转换
        if (result.size() > 1) {
            for (int i = 0; i < result.size(); i++) {
                CanceledMeterDto meter = result.get(i);

                // 验证每条记录的基本字段
                assertNotNull(meter.getMeterName(), String.format("第%d条记录的表名称不应为null", i + 1));
                assertNotNull(meter.getMeterNo(), String.format("第%d条记录的表具号不应为null", i + 1));
                assertNotNull(meter.getMeterType(), String.format("第%d条记录的表类型不应为null", i + 1));
                assertNotNull(meter.getBalance(), String.format("第%d条记录的表余额不应为null", i + 1));

                // 验证数值转换
                assertTrue(meter.getBalance().compareTo(BigDecimal.ZERO) >= 0,
                    String.format("第%d条记录的表余额应大于等于0", i + 1));
            }

            // 验证不同记录的表具号应该不同（业务逻辑验证）
            for (int i = 0; i < result.size() - 1; i++) {
                for (int j = i + 1; j < result.size(); j++) {
                    assertNotEquals(result.get(i).getMeterNo(), result.get(j).getMeterNo(),
                        "同一销户编号下的不同设备表具号应该不同");
                }
            }
        }
    }

    @Test
    @DisplayName("findMetersByCancelNo方法集成测试 - 测试枚举类型转换的完整性")
    void testFindMetersByCancelNo_EnumConversion() {
        // 测试所有可能的销户编号，验证枚举转换
        List<String> testCancelNos = List.of("CANCEL001", "CANCEL002", "CANCEL003");

        for (String cancelNo : testCancelNos) {
            List<CanceledMeterDto> result = electricMeterInfoService.findMetersByCancelNo(cancelNo);

            // 验证每个结果中的枚举转换
            for (CanceledMeterDto meter : result) {
                if (meter.getMeterType() != null) {
                    // 验证枚举值的有效性
                    assertTrue(meter.getMeterType() == MeterTypeEnum.ELECTRIC ||
                              meter.getMeterType() == MeterTypeEnum.WATER,
                              String.format("销户编号%s的设备类型枚举值应为有效值", cancelNo));

                    // 验证枚举转换的一致性
                    assertNotNull(meter.getMeterType().name(),
                        String.format("销户编号%s的设备类型枚举名称不应为null", cancelNo));
                }
            }
        }
    }

    @Test
    @DisplayName("findMetersByCancelNo方法集成测试 - 测试数据完整性和字段映射")
    void testFindMetersByCancelNo_DataIntegrity() {
        // 使用测试数据中存在的销户编号
        String cancelNo = "CANCEL001";

        // 执行测试
        List<CanceledMeterDto> result = electricMeterInfoService.findMetersByCancelNo(cancelNo);

        // 验证数据完整性
        if (!result.isEmpty()) {
            CanceledMeterDto meter = result.get(0);

            // 验证所有必需字段都已正确映射
            assertNotNull(meter.getSpaceName(), "空间名称字段映射失败");
            assertNotNull(meter.getMeterName(), "表名称字段映射失败");
            assertNotNull(meter.getMeterNo(), "表具号字段映射失败");
            assertNotNull(meter.getMeterType(), "表类型字段映射失败");
            assertNotNull(meter.getBalance(), "表余额字段映射失败");

            // 验证可选字段的映射（可能为null但不应该抛出异常）
            // spaceParentNames 可能为null，这是正常的
            assertDoesNotThrow(meter::getSpaceParentNames,
                "获取空间父级名称不应抛出异常");

            // 验证字符串字段的有效性
            assertFalse(meter.getSpaceName().isEmpty(), "空间名称不应为空字符串");
            assertFalse(meter.getMeterName().isEmpty(), "表名称不应为空字符串");
            assertFalse(meter.getMeterNo().isEmpty(), "表具号不应为空字符串");
        }
    }
}
