package info.zhihui.ems.business.plan;

import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.business.plan.bo.WarnPlanBo;
import info.zhihui.ems.business.plan.dto.WarnPlanQueryDto;
import info.zhihui.ems.business.plan.dto.WarnPlanSaveDto;
import info.zhihui.ems.business.plan.service.WarnPlanService;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * WarnPlanService 集成测试
 */
@SpringBootTest
@ActiveProfiles("integrationtest")
@Transactional
class WarnPlanServiceImplIntegrationTest {

    @Autowired
    private WarnPlanService warnPlanService;

    @Test
    @DisplayName("方法参数校验 - null 值触发 ConstraintViolationException")
    void testValidationConstraints() {
        assertThrows(ConstraintViolationException.class, () -> warnPlanService.findList(null));
        assertThrows(ConstraintViolationException.class, () -> warnPlanService.getDetail(null));
        assertThrows(ConstraintViolationException.class, () -> warnPlanService.add(null));
        assertThrows(ConstraintViolationException.class, () -> warnPlanService.edit(null));
        assertThrows(ConstraintViolationException.class, () -> warnPlanService.delete(null));
    }

    @Test
    @DisplayName("新增并查询详情成功")
    void testAddAndGetDetail_Success() {
        String name = "warn-plan-" + UUID.randomUUID();
        WarnPlanSaveDto dto = buildSaveDto(name, new BigDecimal("200.00"), new BigDecimal("120.00"));

        Integer id = warnPlanService.add(dto);
        assertNotNull(id);

        WarnPlanBo detail = warnPlanService.getDetail(id);
        assertEquals(name, detail.getName());
        assertEquals(0, detail.getFirstLevel().compareTo(new BigDecimal("200.00")));
        assertEquals(0, detail.getSecondLevel().compareTo(new BigDecimal("120.00")));
        assertTrue(detail.getAutoClose());
    }

    @Test
    @DisplayName("新增失败 - 第一预警金额不大于第二预警金额")
    void testAdd_InvalidLevels() {
        WarnPlanSaveDto dto = buildSaveDto("invalid-plan", new BigDecimal("100"), new BigDecimal("150"));
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class, () -> warnPlanService.add(dto));
        assertEquals("第一预警金额必须大于第二预警金额", exception.getMessage());
    }

    @Test
    @DisplayName("编辑成功并验证更新")
    void testEdit_Success() {
        Integer id = warnPlanService.add(buildSaveDto("edit-plan", new BigDecimal("220"), new BigDecimal("180")));

        WarnPlanSaveDto updateDto = buildSaveDto("edit-plan-updated", new BigDecimal("300"), new BigDecimal("200"));
        updateDto.setId(id);
        assertDoesNotThrow(() -> warnPlanService.edit(updateDto));

        WarnPlanBo detail = warnPlanService.getDetail(id);
        assertEquals("edit-plan-updated", detail.getName());
        assertEquals(0, detail.getFirstLevel().compareTo(new BigDecimal("300")));
        assertEquals(0, detail.getSecondLevel().compareTo(new BigDecimal("200")));
    }

    @Test
    @DisplayName("编辑失败 - 新配置不合法")
    void testEdit_InvalidLevels() {
        Integer id = warnPlanService.add(buildSaveDto("edit-invalid-plan", new BigDecimal("250"), new BigDecimal("150")));

        WarnPlanSaveDto updateDto = buildSaveDto("edit-invalid-plan", new BigDecimal("150"), new BigDecimal("200"));
        updateDto.setId(id);
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class, () -> warnPlanService.edit(updateDto));
        assertEquals("第一预警金额必须大于第二预警金额", exception.getMessage());
    }

    @Test
    @DisplayName("编辑失败 - 方案不存在")
    void testEdit_NotFound() {
        WarnPlanSaveDto dto = buildSaveDto("not-exist-plan", new BigDecimal("200"), new BigDecimal("150"));
        dto.setId(999999);
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class, () -> warnPlanService.edit(dto));
        assertEquals("数据不存在，请刷新后重试", exception.getMessage());
    }

    @Test
    @DisplayName("删除成功后无法再查询")
    void testDelete_Success() {
        Integer id = warnPlanService.add(buildSaveDto("delete-plan", new BigDecimal("210"), new BigDecimal("160")));
        assertDoesNotThrow(() -> warnPlanService.delete(id));
        assertThrows(NotFoundException.class, () -> warnPlanService.getDetail(id));
    }

    @Test
    @DisplayName("按名称查询列表")
    void testFindList_FilterByName() {
        String name = "query-plan-" + UUID.randomUUID();
        Integer id = warnPlanService.add(buildSaveDto(name, new BigDecimal("230"), new BigDecimal("170")));

        WarnPlanQueryDto query = new WarnPlanQueryDto();
        query.setName(name);
        List<WarnPlanBo> result = warnPlanService.findList(query);
        assertTrue(result.stream().anyMatch(item -> item.getId().equals(id)));
    }

    @Test
    @DisplayName("查询不存在的方案抛出异常")
    void testGetDetail_NotFound() {
        assertThrows(NotFoundException.class, () -> warnPlanService.getDetail(987654));
    }

    private WarnPlanSaveDto buildSaveDto(String name, BigDecimal first, BigDecimal second) {
        return new WarnPlanSaveDto()
                .setName(name)
                .setFirstLevel(first)
                .setSecondLevel(second)
                .setAutoClose(Boolean.TRUE)
                .setRemark("integration-test");
    }
}
