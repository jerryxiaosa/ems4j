package info.zhihui.ems.business.device;

import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.business.device.bo.GatewayBo;
import info.zhihui.ems.business.device.dto.GatewayOnlineStatusDto;
import info.zhihui.ems.business.device.dto.GatewayQueryDto;
import info.zhihui.ems.business.device.dto.GatewayCreateDto;
import info.zhihui.ems.business.device.dto.GatewayUpdateDto;
import info.zhihui.ems.business.device.service.GatewayService;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GatewayService集成测试
 *
 * @author jerryxiaosa
 */
@SpringBootTest
@ActiveProfiles("integrationtest")
@Transactional
class GatewayServiceImplIntegrationTest {

    @Autowired
    private GatewayService gatewayService;

    @Test
    @DisplayName("参数校验测试 - 覆盖所有方法的null参数和有效参数场景")
    void testGatewayService_ValidationTests_ShouldThrowException() {
        // 测试1: getDetail方法 - null参数
        assertThrows(ConstraintViolationException.class, () -> {
            gatewayService.getDetail(null);
        }, "getDetail方法null参数应抛出ConstraintViolationException");

        // 测试2: findPage方法 - null query参数
        PageParam pageParam = new PageParam();
        pageParam.setPageNum(1);
        pageParam.setPageSize(10);
        assertThrows(ConstraintViolationException.class, () -> {
            gatewayService.findPage(null, pageParam);
        }, "findPage方法null query参数应抛出ConstraintViolationException");

        // 测试3: findPage方法 - null pageParam参数
        GatewayQueryDto query = new GatewayQueryDto();
        assertThrows(ConstraintViolationException.class, () -> {
            gatewayService.findPage(query, null);
        }, "findPage方法null pageParam参数应抛出ConstraintViolationException");

        // 测试4: findList方法 - null参数
        assertThrows(ConstraintViolationException.class, () -> {
            gatewayService.findList(null);
        }, "findList方法null参数应抛出ConstraintViolationException");

        // 测试5: delete方法 - null参数
        assertThrows(ConstraintViolationException.class, () -> {
            gatewayService.delete(null);
        }, "delete方法null参数应抛出ConstraintViolationException");

        // 测试6: add方法 - null参数
        assertThrows(ConstraintViolationException.class, () -> {
            gatewayService.add(null);
        }, "add方法null参数应抛出ConstraintViolationException");

        // 测试7: update方法 - null参数
        assertThrows(ConstraintViolationException.class, () -> {
            gatewayService.update(null);
        }, "update方法null参数应抛出ConstraintViolationException");

        // 测试8: add方法 - 必填字段为null
        GatewayCreateDto addDto = new GatewayCreateDto();
        addDto.setSpaceId(null);
        addDto.setGatewayName("测试网关");
        addDto.setModelId(1);
        addDto.setSn("TEST_SN");
        addDto.setDeviceNo(addDto.getSn());
        assertThrows(ConstraintViolationException.class, () -> {
            gatewayService.add(addDto);
        }, "add方法spaceId为null应抛出ConstraintViolationException");

        // 测试9: 有效参数测试（不应抛出参数校验异常）
        try {
            gatewayService.findList(query);
        } catch (ConstraintViolationException e) {
            throw new AssertionError("findList方法有效参数不应抛出ConstraintViolationException", e);
        } catch (Exception e) {
            // 其他异常（如业务异常）是可以接受的，我们只关心参数校验
        }

        // 测试10: getCommunicationOption方法 - 无参数方法不应抛出参数校验异常
        try {
            gatewayService.getCommunicationOption();
        } catch (ConstraintViolationException e) {
            throw new AssertionError("getCommunicationOption方法不应抛出ConstraintViolationException", e);
        } catch (Exception e) {
            // 其他异常（如业务异常）是可以接受的，我们只关心参数校验
        }

        GatewayCreateDto addDtoConfig = new GatewayCreateDto();
        addDtoConfig.setSpaceId(1);
        addDtoConfig.setGatewayName("测试网关");
        addDtoConfig.setModelId(1);
        addDtoConfig.setSn("TEST_SN");
        addDtoConfig.setDeviceNo(addDtoConfig.getSn());
        assertThrows(ConstraintViolationException.class, () -> {
            gatewayService.add(addDtoConfig);
        }, "add方法configInfo为null应抛出ConstraintViolationException");
    }

    @Test
    @DisplayName("findPage方法集成测试 - 测试分页查询功能")
    void testFindPage_Success() {
        // 准备测试数据
        GatewayQueryDto query = new GatewayQueryDto();
        PageParam pageParam = new PageParam();
        pageParam.setPageNum(1);
        pageParam.setPageSize(10);

        // 执行测试
        PageResult<GatewayBo> result = gatewayService.findPage(query, pageParam);

        // 验证结果
        assertNotNull(result, "分页查询结果不应为null");
        assertNotNull(result.getList(), "分页数据列表不应为null");
        assertTrue(result.getPageNum() > 0, "页码应大于0");
        assertTrue(result.getPageSize() > 0, "每页大小应大于0");
        assertTrue(result.getTotal() >= 0, "总数应大于等于0");

        // 如果有数据，验证第一个网关的基本信息
        GatewayBo firstGateway = result.getList().get(0);
        assertNotNull(firstGateway.getId(), "网关ID不应为null");
        assertNotNull(firstGateway.getGatewayName(), "网关名称不应为null");
        assertNotNull(firstGateway.getSpaceId(), "空间ID不应为null");
    }

    @Test
    @DisplayName("findList方法集成测试 - 覆盖所有查询字段条件")
    void testFindList_AllFilterFields_Coverage() {
        GatewayQueryDto q = new GatewayQueryDto();
        q.setSearchKey("GW");
        q.setSn("SN");
        q.setIsOnline(true);
        q.setIotId("1");
        q.setSpaceIds(List.of(100, 1));

        List<GatewayBo> list = gatewayService.findList(q);
        assertNotNull(list, "findList 应返回非null列表（即使无匹配数据）");
    }

    @Test
    @DisplayName("findList方法集成测试 - 测试列表查询功能")
    void testFindList_Success() {
        // 准备测试数据
        GatewayQueryDto query = new GatewayQueryDto();

        // 执行测试
        List<GatewayBo> result = gatewayService.findList(query);

        // 验证结果
        assertNotNull(result, "查询结果不应为null");

        // 如果有数据，验证第一个网关的基本信息
        GatewayBo firstGateway = result.get(0);
        assertNotNull(firstGateway.getId(), "网关ID不应为null");
        assertNotNull(firstGateway.getGatewayName(), "网关名称不应为null");
        assertNotNull(firstGateway.getSpaceId(), "空间ID不应为null");
        assertNotNull(firstGateway.getModelId(), "型号ID不应为null");

        // 测试按条件查询
        GatewayQueryDto specificQuery = new GatewayQueryDto();
        specificQuery.setSpaceIds(List.of(1));
        List<GatewayBo> specificResult = gatewayService.findList(specificQuery);
        assertNotNull(specificResult, "特定条件查询结果不应为null");
    }

    @Test
    @DisplayName("getDetail方法集成测试 - 测试根据ID查询网关详情功能")
    void testGetDetail_Success() {
        // 准备测试数据 - 使用测试数据中存在的网关ID
        Integer testGatewayId = 1;

        // 执行测试
        GatewayBo result = gatewayService.getDetail(testGatewayId);

        // 验证结果
        assertNotNull(result, "查询结果不应为null");
        assertEquals(testGatewayId, result.getId(), "返回的网关ID应与查询ID一致");
        assertNotNull(result.getGatewayName(), "网关名称不应为null");
        assertNotNull(result.getSpaceId(), "空间ID不应为null");
        assertNotNull(result.getModelId(), "型号ID不应为null");

        // 验证网关的基本属性
        assertTrue(result.getSpaceId() > 0, "空间ID应大于0");
        assertTrue(result.getModelId() > 0, "型号ID应大于0");

        // 测试查询不存在的网关ID
        Integer nonExistentId = 99999;
        assertThrows(Exception.class, () -> {
            gatewayService.getDetail(nonExistentId);
        }, "查询不存在的网关ID应抛出异常");
    }

    @Test
    @DisplayName("add方法集成测试 - 测试添加网关功能")
    void testAdd_Success() {
        // 准备测试数据
        GatewayCreateDto dto = new GatewayCreateDto();
        dto.setSpaceId(1);
        dto.setGatewayName("测试网关");
        dto.setModelId(4);
        dto.setSn("TEST_SN_" + System.currentTimeMillis());
        dto.setDeviceNo(dto.getSn());
        dto.setConfigInfo("{\"test\":\"data\"}");
        dto.setRemark("测试备注");

        // 执行测试
        Integer result = gatewayService.add(dto);

        // 验证结果
        assertNotNull(result, "添加网关应返回ID");
        assertTrue(result > 0, "返回的ID应大于0");

        // 验证添加的数据是否正确
        GatewayBo addedGateway = gatewayService.getDetail(result);
        assertNotNull(addedGateway, "添加的网关应能查询到");
        assertEquals(dto.getGatewayName(), addedGateway.getGatewayName(), "网关名称应一致");
        assertEquals(dto.getSpaceId(), addedGateway.getSpaceId(), "空间ID应一致");
        assertEquals(dto.getModelId(), addedGateway.getModelId(), "型号ID应一致");
        assertEquals(dto.getSn(), addedGateway.getSn(), "序列号应一致");
    }

    @Test
    @DisplayName("update方法集成测试 - 测试更新网关功能")
    void testUpdate_Success() {
        // 先添加一个网关用于测试更新
        GatewayCreateDto addDto = new GatewayCreateDto();
        addDto.setSpaceId(1);
        addDto.setGatewayName("原始网关");
        addDto.setModelId(4);
        addDto.setSn("ORIGINAL_SN_" + System.currentTimeMillis());
        addDto.setDeviceNo(addDto.getSn());
        addDto.setConfigInfo("{\"original\":\"data\"}");
        addDto.setRemark("原始备注");

        Integer gatewayId = gatewayService.add(addDto);
        assertNotNull(gatewayId, "添加网关应成功");

        GatewayBo beforeUpdate = gatewayService.getDetail(gatewayId);
        String originalDeviceNo = beforeUpdate.getDeviceNo();

        // 准备更新数据
        GatewayUpdateDto updateDto = new GatewayUpdateDto();
        updateDto.setId(gatewayId);
        updateDto.setSpaceId(1);
        updateDto.setGatewayName("更新后的网关");
        updateDto.setModelId(4);
        updateDto.setSn(addDto.getSn()); // 保持序列号不变
        updateDto.setDeviceNo(updateDto.getSn());
        updateDto.setConfigInfo("{\"updated\":\"data\"}");
        updateDto.setRemark("更新后的备注");

        // 执行更新测试
        assertDoesNotThrow(() -> {
            gatewayService.update(updateDto);
        }, "更新网关不应抛出异常");

        // 验证更新结果
        GatewayBo updatedGateway = gatewayService.getDetail(gatewayId);
        assertNotNull(updatedGateway, "更新后的网关应能查询到");
        assertEquals(updateDto.getGatewayName(), updatedGateway.getGatewayName(), "网关名称应已更新");
        assertEquals(updateDto.getConfigInfo(), updatedGateway.getConfigInfo(), "配置信息应已更新");
        assertEquals(updateDto.getRemark(), updatedGateway.getRemark(), "备注应已更新");
        assertEquals(originalDeviceNo, updatedGateway.getDeviceNo(), "网关编号不应改变");
    }

    @Test
    @DisplayName("syncGatewayOnlineStatus集成测试 - 强制设置在线状态")
    void testSyncGatewayOnlineStatus_Force() {
        GatewayCreateDto addDto = new GatewayCreateDto();
        addDto.setSpaceId(1);
        addDto.setGatewayName("同步状态网关");
        addDto.setModelId(4);
        addDto.setSn("SYNC_SN_" + System.currentTimeMillis());
        addDto.setDeviceNo(addDto.getSn());
        addDto.setConfigInfo("{\"sync\":true}");
        addDto.setRemark("初始状态");

        Integer gatewayId = gatewayService.add(addDto);
        assertNotNull(gatewayId);

        gatewayService.syncGatewayOnlineStatus(new GatewayOnlineStatusDto()
                .setGatewayId(gatewayId)
                .setForce(true)
                .setOnlineStatus(Boolean.FALSE));

        GatewayBo afterFirstSync = gatewayService.getDetail(gatewayId);
        assertNotNull(afterFirstSync);
        assertEquals(Boolean.FALSE, afterFirstSync.getIsOnline(), "第一次同步后应为离线");

        gatewayService.syncGatewayOnlineStatus(new GatewayOnlineStatusDto()
                .setGatewayId(gatewayId)
                .setForce(true)
                .setOnlineStatus(Boolean.TRUE));

        GatewayBo afterSecondSync = gatewayService.getDetail(gatewayId);
        assertNotNull(afterSecondSync);
        assertEquals(Boolean.TRUE, afterSecondSync.getIsOnline(), "第二次同步后应为在线");
    }

    @Test
    @DisplayName("delete方法集成测试 - 测试删除网关功能")
    void testDelete_Success() {
        // 先添加一个网关用于测试删除
        GatewayCreateDto addDto = new GatewayCreateDto();
        addDto.setSpaceId(1);
        addDto.setGatewayName("待删除网关");
        addDto.setModelId(4);
        addDto.setSn("DELETE_SN_" + System.currentTimeMillis());
        addDto.setDeviceNo(addDto.getSn());
        addDto.setConfigInfo("{\"delete\":\"test\"}");
        addDto.setRemark("待删除备注");

        Integer gatewayId = gatewayService.add(addDto);
        assertNotNull(gatewayId, "添加网关应成功");

        // 验证网关存在
        GatewayBo beforeDelete = gatewayService.getDetail(gatewayId);
        assertNotNull(beforeDelete, "删除前网关应存在");

        // 执行删除测试
        assertDoesNotThrow(() -> {
            gatewayService.delete(gatewayId);
        }, "删除网关不应抛出异常");

        // 验证删除结果 - 查询已删除的网关应抛出异常
        assertThrows(Exception.class, () -> {
            gatewayService.getDetail(gatewayId);
        }, "查询已删除的网关应抛出异常");

        // 测试删除不存在的网关ID
        Integer nonExistentId = 99999;
        assertThrows(Exception.class, () -> {
            gatewayService.delete(nonExistentId);
        }, "删除不存在的网关ID应抛出异常");
    }

    @Test
    @DisplayName("getCommunicationOption方法集成测试 - 测试获取通信方式选项列表功能")
    void testGetCommunicationOption_Success() {
        // 执行测试
        List<String> result = gatewayService.getCommunicationOption();

        // 验证结果
        assertNotNull(result, "通信方式选项列表不应为null");

        // 如果有数据，验证数据的基本特征
        if (!result.isEmpty()) {
            // 验证每个选项都不为空
            for (String option : result) {
                assertNotNull(option, "通信方式选项不应为null");
                assertFalse(option.trim().isEmpty(), "通信方式选项不应为空字符串");
            }

            // 验证选项的唯一性（不应有重复项）
            long distinctCount = result.stream().distinct().count();
            assertEquals(result.size(), distinctCount, "通信方式选项列表不应包含重复项");
        }

        // 验证返回的是不可变列表或至少是安全的列表
        assertDoesNotThrow(() -> {
            result.size(); // 基本操作应该正常
        }, "获取通信方式选项列表大小不应抛出异常");
    }
}
