package info.zhihui.ems.foundation.organization;

import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.foundation.organization.bo.OrganizationBo;
import info.zhihui.ems.foundation.organization.dto.OrganizationCreateDto;
import info.zhihui.ems.foundation.organization.dto.OrganizationQueryDto;
import info.zhihui.ems.foundation.organization.dto.OrganizationUpdateDto;
import info.zhihui.ems.foundation.organization.entity.OrganizationEntity;
import info.zhihui.ems.foundation.organization.enums.OrganizationTypeEnum;
import info.zhihui.ems.foundation.organization.repository.OrganizationRepository;
import info.zhihui.ems.foundation.organization.service.OrganizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * OrganizationServiceImpl集成测试
 */
@SpringBootTest
@ActiveProfiles("integrationtest")
@Transactional
@Rollback
class OrganizationServiceImplIntegrationTest {

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private OrganizationRepository organizationRepository;

    private OrganizationEntity testOrganization1;
    private OrganizationEntity deletedOrganization;

    @BeforeEach
    void setUp() {
        // 清理测试数据
        organizationRepository.delete(null);

        // 准备测试数据
        testOrganization1 = new OrganizationEntity()
                .setId(null)
                .setOrganizationName("测试企业1")
                .setOrganizationType(OrganizationTypeEnum.ENTERPRISE.getCode())
                .setOrganizationAddress("北京市朝阳区测试地址1")
                .setManagerName("张三")
                .setManagerPhone("13800138001")
                .setCreditCode("91110000000000001X")
                .setEntryDate(LocalDate.of(2023, 1, 1))
                .setRemark("测试企业1备注");
        testOrganization1.setOwnAreaId(1)
                .setCreateTime(LocalDateTime.now())
                .setUpdateTime(LocalDateTime.now())
                .setIsDeleted(false)
        ;
        organizationRepository.insert(testOrganization1);

        OrganizationEntity testOrganization2 = new OrganizationEntity()
                .setId(null)
                .setOrganizationName("测试企业2")
                .setOrganizationType(OrganizationTypeEnum.ENTERPRISE.getCode())
                .setOrganizationAddress("上海市浦东新区测试地址2")
                .setManagerName("李四")
                .setManagerPhone("13800138002")
                .setCreditCode("91310000000000002X")
                .setEntryDate(LocalDate.of(2023, 2, 1))
                .setRemark("测试企业2备注");
        testOrganization2.setOwnAreaId(2)
                .setCreateTime(LocalDateTime.now())
                .setUpdateTime(LocalDateTime.now())
                .setIsDeleted(false);
        organizationRepository.insert(testOrganization2);

        // 准备已删除的测试数据
        deletedOrganization = new OrganizationEntity()
                .setId(null)
                .setOrganizationName("已删除企业")
                .setOrganizationType(OrganizationTypeEnum.ENTERPRISE.getCode())
                .setOrganizationAddress("广州市天河区测试地址3")
                .setManagerName("王五")
                .setManagerPhone("13800138003")
                .setCreditCode("91440000000000003X")
                .setEntryDate(LocalDate.of(2023, 3, 1))
                .setRemark("已删除企业备注");
        deletedOrganization.setOwnAreaId(3)
                .setCreateTime(LocalDateTime.now())
                .setUpdateTime(LocalDateTime.now())
                .setIsDeleted(true);
        organizationRepository.insert(deletedOrganization);
    }

    // ==================== getDetail方法测试 ====================

    @Test
    @DisplayName("getDetail - 成功获取机构详情")
    void testGetDetail_Success() {
        // When
        OrganizationBo result = organizationService.getDetail(testOrganization1.getId());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testOrganization1.getId());
        assertThat(result.getName()).isEqualTo("测试企业1");
        assertThat(result.getOrganizationType()).isEqualTo(OrganizationTypeEnum.ENTERPRISE);
        assertThat(result.getOrganizationAddress()).isEqualTo("北京市朝阳区测试地址1");
        assertThat(result.getManagerName()).isEqualTo("张三");
        assertThat(result.getManagerPhone()).isEqualTo("13800138001");
        assertThat(result.getCreditCode()).isEqualTo("91110000000000001X");
        assertThat(result.getEntryDate()).isEqualTo(LocalDate.of(2023, 1, 1));
        assertThat(result.getRemark()).isEqualTo("测试企业1备注");
        assertThat(result.getOwnAreaId()).isEqualTo(1);
    }

    @Test
    @DisplayName("getDetail - 机构不存在")
    void testGetDetail_NotFound() {
        // When & Then
        assertThatThrownBy(() -> organizationService.getDetail(99999))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("机构不存在");
    }

    @Test
    @DisplayName("getDetail - 已删除的机构")
    void testGetDetail_DeletedOrganization() {
        // When & Then
        assertThatThrownBy(() -> organizationService.getDetail(deletedOrganization.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("机构不存在");
    }

    // ==================== findOrganizationList方法测试 ====================

    @Test
    @DisplayName("findOrganizationList - 成功查询所有机构")
    void testFindOrganizationList_Success() {
        // Given
        OrganizationQueryDto queryDto = new OrganizationQueryDto();

        // When
        List<OrganizationBo> result = organizationService.findOrganizationList(queryDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2); // 只返回未删除的数据
        assertThat(result).extracting(OrganizationBo::getName)
                .containsExactlyInAnyOrder("测试企业1", "测试企业2");
    }

    @Test
    @DisplayName("findOrganizationList - 按机构名称模糊查询")
    void testFindOrganizationList_ByName() {
        // Given
        OrganizationQueryDto queryDto = new OrganizationQueryDto()
                .setOrganizationNameLike("企业1");

        // When
        List<OrganizationBo> result = organizationService.findOrganizationList(queryDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("测试企业1");
    }

    @Test
    @DisplayName("findOrganizationList - 按统一社会信用代码查询")
    void testFindOrganizationList_ByCreditCode() {
        // Given
        OrganizationQueryDto queryDto = new OrganizationQueryDto()
                .setCreditCode("91110000000000001X");

        // When
        List<OrganizationBo> result = organizationService.findOrganizationList(queryDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCreditCode()).isEqualTo("91110000000000001X");
    }

    @Test
    @DisplayName("findOrganizationList - 按负责人名称模糊查询")
    void testFindOrganizationList_ByManagerName() {
        // Given
        OrganizationQueryDto queryDto = new OrganizationQueryDto()
                .setManagerNameLike("张");

        // When
        List<OrganizationBo> result = organizationService.findOrganizationList(queryDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getManagerName()).isEqualTo("张三");
    }

    @Test
    @DisplayName("findOrganizationList - 按ID集合查询")
    void testFindOrganizationList_ByIds() {
        // Given
        OrganizationQueryDto queryDto = new OrganizationQueryDto()
                .setIds(Set.of(testOrganization1.getId()));

        // When
        List<OrganizationBo> result = organizationService.findOrganizationList(queryDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(testOrganization1.getId());
    }

    @Test
    @DisplayName("findOrganizationList - 空结果")
    void testFindOrganizationList_EmptyResult() {
        // Given
        OrganizationQueryDto queryDto = new OrganizationQueryDto()
                .setOrganizationNameLike("不存在的机构");

        // When
        List<OrganizationBo> result = organizationService.findOrganizationList(queryDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    // ==================== findOrganizationPage方法测试 ====================

    @Test
    @DisplayName("findOrganizationPage - 成功分页查询")
    void testFindOrganizationPage_Success() {
        // Given
        OrganizationQueryDto queryDto = new OrganizationQueryDto();
        PageParam pageParam = new PageParam();

        // When
        PageResult<OrganizationBo> result = organizationService.findOrganizationPage(queryDto, pageParam);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotal()).isEqualTo(2);
        assertThat(result.getList()).hasSize(2);
        assertThat(result.getPageNum()).isEqualTo(1);
        assertThat(result.getPageSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("findOrganizationPage - 分页查询第二页")
    void testFindOrganizationPage_SecondPage() {
        // Given
        OrganizationQueryDto queryDto = new OrganizationQueryDto();
        PageParam pageParam = new PageParam().setPageNum(2).setPageSize(1);

        // When
        PageResult<OrganizationBo> result = organizationService.findOrganizationPage(queryDto, pageParam);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotal()).isEqualTo(2);
        assertThat(result.getList()).hasSize(1);
        assertThat(result.getPageNum()).isEqualTo(2);
        assertThat(result.getPageSize()).isEqualTo(1);
    }

    // ==================== create方法测试 ====================

    @Test
    @DisplayName("create - 成功创建机构")
    void testAdd_Success() {
        // Given
        OrganizationCreateDto createDto = new OrganizationCreateDto()
                .setOrganizationName("新建企业")
                .setCreditCode("91110000000000004X")
                .setOrganizationType(OrganizationTypeEnum.ENTERPRISE)
                .setOrganizationAddress("深圳市南山区测试地址")
                .setManagerName("赵六")
                .setManagerPhone("13800138004")
                .setEntryDate(LocalDate.of(2024, 5, 20))
                .setRemark("新建企业备注")
                .setOwnAreaId(4);

        // When
        Integer newId = organizationService.add(createDto);

        // Then
        assertThat(newId).isNotNull();

        // 验证数据库中确实创建了记录
        OrganizationEntity entity = organizationRepository.selectById(newId);
        assertThat(entity).isNotNull();
        assertThat(entity.getOrganizationName()).isEqualTo("新建企业");
        assertThat(entity.getCreditCode()).isEqualTo("91110000000000004X");
        assertThat(entity.getOrganizationType()).isEqualTo(OrganizationTypeEnum.ENTERPRISE.getCode());
        assertThat(entity.getOrganizationAddress()).isEqualTo("深圳市南山区测试地址");
        assertThat(entity.getManagerName()).isEqualTo("赵六");
        assertThat(entity.getManagerPhone()).isEqualTo("13800138004");
        assertThat(entity.getRemark()).isEqualTo("新建企业备注");
        assertThat(entity.getOwnAreaId()).isEqualTo(4);
    }

    @Test
    @DisplayName("create - 机构名称已存在")
    void testAdd_DuplicateName() {
        // Given
        OrganizationCreateDto createDto = new OrganizationCreateDto()
                .setOrganizationName("测试企业1") // 使用已存在的名称
                .setCreditCode("91110000000000005X")
                .setOrganizationType(OrganizationTypeEnum.ENTERPRISE)
                .setOrganizationAddress("杭州市西湖区测试地址")
                .setManagerName("孙七")
                .setManagerPhone("13800138005")
                .setEntryDate(LocalDate.of(2024, 6, 15))
                .setOwnAreaId(5);

        // When & Then
        assertThatThrownBy(() -> organizationService.add(createDto))
                .isInstanceOf(BusinessRuntimeException.class)
                .hasMessage("机构名称已存在");
    }

    // ==================== update方法测试 ====================

    @Test
    @DisplayName("update - 成功更新机构")
    void testUpdate_Success() {
        // Given
        OrganizationUpdateDto updateDto = new OrganizationUpdateDto()
                .setId(testOrganization1.getId())
                .setOrganizationName("更新后的企业名称")
                .setCreditCode("91110000000000006X")
                .setOrganizationType(OrganizationTypeEnum.ENTERPRISE)
                .setOrganizationAddress("更新后的地址")
                .setManagerName("更新后的负责人")
                .setManagerPhone("13800138006")
                .setEntryDate(LocalDate.of(2024, 7, 1))
                .setRemark("更新后的备注")
                .setOwnAreaId(6);

        // When
        organizationService.update(updateDto);

        // Then
        OrganizationBo result = organizationService.getDetail(testOrganization1.getId());
        assertThat(result.getName()).isEqualTo("更新后的企业名称");
        assertThat(result.getCreditCode()).isEqualTo("91110000000000006X");
        assertThat(result.getOrganizationAddress()).isEqualTo("更新后的地址");
        assertThat(result.getManagerName()).isEqualTo("更新后的负责人");
        assertThat(result.getManagerPhone()).isEqualTo("13800138006");
        assertThat(result.getRemark()).isEqualTo("更新后的备注");
        assertThat(result.getOwnAreaId()).isEqualTo(6);
    }

    @Test
    @DisplayName("update - 部分字段更新")
    void testUpdate_PartialUpdate() {
        // Given
        OrganizationUpdateDto updateDto = new OrganizationUpdateDto()
                .setId(testOrganization1.getId())
                .setOrganizationName("部分更新的企业名称");

        // When
        organizationService.update(updateDto);

        // Then
        OrganizationBo result = organizationService.getDetail(testOrganization1.getId());
        assertThat(result.getName()).isEqualTo("部分更新的企业名称");
        // 其他字段应该保持不变
        assertThat(result.getCreditCode()).isEqualTo("91110000000000001X");
        assertThat(result.getManagerName()).isEqualTo("张三");
    }

    @Test
    @DisplayName("update - 机构不存在")
    void testUpdate_NotFound() {
        // Given
        OrganizationUpdateDto updateDto = new OrganizationUpdateDto()
                .setId(99999)
                .setOrganizationName("不存在的机构");

        // When & Then
        assertThatThrownBy(() -> organizationService.update(updateDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("机构不存在");
    }

    @Test
    @DisplayName("update - 机构名称已存在")
    void testUpdate_DuplicateName() {
        // Given
        OrganizationUpdateDto updateDto = new OrganizationUpdateDto()
                .setId(testOrganization1.getId())
                .setOrganizationName("测试企业2"); // 使用已存在的名称

        // When & Then
        assertThatThrownBy(() -> organizationService.update(updateDto))
                .isInstanceOf(BusinessRuntimeException.class)
                .hasMessage("机构名称已存在");
    }

    // ==================== delete方法测试 ====================

    @Test
    @DisplayName("delete - 成功删除机构")
    void testDelete_Success() {
        // When
        organizationService.delete(testOrganization1.getId());

        // Then
        assertThatThrownBy(() -> organizationService.getDetail(testOrganization1.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("机构不存在");

        // 验证数据库中记录被标记为删除
        OrganizationEntity entity = organizationRepository.selectById(testOrganization1.getId());
        assertNull(entity);
    }

    @Test
    @DisplayName("delete - 机构不存在")
    void testDelete_NotFound() {
        // When & Then
        assertThatThrownBy(() -> organizationService.delete(99999))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("机构不存在");
    }

    @Test
    @DisplayName("delete - 已删除的机构")
    void testDelete_AlreadyDeleted() {
        // When & Then
        assertThatThrownBy(() -> organizationService.delete(deletedOrganization.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("机构不存在");
    }
}
