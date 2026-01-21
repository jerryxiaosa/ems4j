package info.zhihui.ems.foundation.organization.service.impl;

import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.foundation.organization.bo.OrganizationBo;
import info.zhihui.ems.foundation.organization.dto.OrganizationCreateDto;
import info.zhihui.ems.foundation.organization.dto.OrganizationQueryDto;
import info.zhihui.ems.foundation.organization.dto.OrganizationUpdateDto;
import info.zhihui.ems.foundation.organization.entity.OrganizationEntity;
import info.zhihui.ems.foundation.organization.mapper.OrganizationMapper;
import info.zhihui.ems.foundation.organization.qo.OrganizationQueryQo;
import info.zhihui.ems.foundation.organization.repository.OrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * OrganizationServiceImpl 单元测试类
 *
 * @author jerryxiaosa
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("组织机构服务实现类测试")
class OrganizationServiceImplTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private OrganizationMapper organizationMapper;

    @InjectMocks
    private OrganizationServiceImpl organizationService;

    // 测试数据
    private OrganizationEntity mockEntity;
    private OrganizationBo mockBo;
    private OrganizationCreateDto mockCreateDto;
    private OrganizationUpdateDto mockUpdateDto;
    private OrganizationQueryDto mockQueryDto;
    private OrganizationQueryQo mockQueryQo;
    private PageParam mockPageParam;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        mockEntity = new OrganizationEntity();
        mockEntity.setId(1);
        mockEntity.setOrganizationName("测试组织");
        mockEntity.setCreditCode("TEST_ORG");
        mockEntity.setOrganizationAddress("测试地址");
        mockEntity.setManagerName("测试负责人");
        mockEntity.setManagerPhone("13800138000");
        mockEntity.setEntryDate(LocalDate.now());
        mockEntity.setRemark("测试备注");

        mockBo = new OrganizationBo();
        mockBo.setId(1);
        mockBo.setName("测试组织");
        mockBo.setCreditCode("TEST_ORG");
        mockBo.setOrganizationAddress("测试地址");
        mockBo.setManagerName("测试负责人");
        mockBo.setManagerPhone("13800138000");
        mockBo.setEntryDate(LocalDate.now());
        mockBo.setRemark("测试备注");

        mockCreateDto = new OrganizationCreateDto();
        mockCreateDto.setOrganizationName("新组织");
        mockCreateDto.setCreditCode("NEW_ORG");
        mockCreateDto.setOrganizationAddress("新地址");
        mockCreateDto.setManagerName("新负责人");
        mockCreateDto.setManagerPhone("13900139000");
        mockCreateDto.setEntryDate(LocalDate.now());
        mockCreateDto.setRemark("新备注");

        mockUpdateDto = new OrganizationUpdateDto();
        mockUpdateDto.setId(1);
        mockUpdateDto.setOrganizationName("更新组织");
        mockUpdateDto.setCreditCode("UPDATE_ORG");
        mockUpdateDto.setOrganizationAddress("更新地址");
        mockUpdateDto.setManagerName("更新负责人");
        mockUpdateDto.setManagerPhone("13700137000");
        mockUpdateDto.setEntryDate(LocalDate.now());
        mockUpdateDto.setRemark("更新备注");

        mockQueryDto = new OrganizationQueryDto();
        mockQueryDto.setOrganizationNameLike("测试");
        mockQueryDto.setCreditCode("TEST");
        mockQueryDto.setManagerNameLike("负责人");

        mockQueryQo = new OrganizationQueryQo();
        mockQueryQo.setOrganizationNameLike("测试");
        mockQueryQo.setCreditCode("TEST");
        mockQueryQo.setManagerNameLike("负责人");

        mockPageParam = new PageParam();
        mockPageParam.setPageNum(1);
        mockPageParam.setPageSize(10);
    }

    // ==================== getDetail方法测试 ====================

    @Test
    @DisplayName("getDetail - 成功获取组织详情")
    void testGetDetail_Success() {
        // Given
        when(organizationRepository.selectById(1)).thenReturn(mockEntity);
        when(organizationMapper.entityToBo(mockEntity)).thenReturn(mockBo);

        // When
        OrganizationBo result = organizationService.getDetail(1);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getName()).isEqualTo("测试组织");

        verify(organizationRepository).selectById(1);
        verify(organizationMapper).entityToBo(mockEntity);
    }

    @Test
    @DisplayName("getDetail - 组织不存在")
    void testGetDetail_NotFound() {
        // Given
        when(organizationRepository.selectById(1)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> organizationService.getDetail(1))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("机构不存在");

        verify(organizationRepository).selectById(1);
        verify(organizationMapper, never()).entityToBo(any());
    }

    // ==================== findOrganizationList方法测试 ====================

    @Test
    @DisplayName("findOrganizationList - 成功查询组织列表")
    void testFindOrganizationList_Success() {
        // Given
        List<OrganizationEntity> entityList = List.of(mockEntity);
        List<OrganizationBo> boList = List.of(mockBo);

        when(organizationMapper.queryDtoToQo(mockQueryDto)).thenReturn(mockQueryQo);
        when(organizationRepository.selectByQo(mockQueryQo)).thenReturn(entityList);
        when(organizationMapper.listEntityToBo(entityList)).thenReturn(boList);

        // When
        List<OrganizationBo> result = organizationService.findOrganizationList(mockQueryDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1);
        assertThat(result.get(0).getName()).isEqualTo("测试组织");

        verify(organizationMapper).queryDtoToQo(mockQueryDto);
        verify(organizationRepository).selectByQo(mockQueryQo);
        verify(organizationMapper).listEntityToBo(entityList);
    }

    @Test
    @DisplayName("findOrganizationList - 查询结果为空")
    void testFindOrganizationList_Empty() {
        // Given
        when(organizationMapper.queryDtoToQo(mockQueryDto)).thenReturn(mockQueryQo);
        when(organizationRepository.selectByQo(mockQueryQo)).thenReturn(Collections.emptyList());
        when(organizationMapper.listEntityToBo(Collections.emptyList())).thenReturn(Collections.emptyList());

        // When
        List<OrganizationBo> result = organizationService.findOrganizationList(mockQueryDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(organizationMapper).queryDtoToQo(mockQueryDto);
        verify(organizationRepository).selectByQo(mockQueryQo);
        verify(organizationMapper).listEntityToBo(Collections.emptyList());
    }

    // ==================== create方法测试 ====================

    @Test
    @DisplayName("create - 成功创建组织")
    void testAdd_Success() {
        // Given
        OrganizationEntity newEntity = new OrganizationEntity();
        newEntity.setOrganizationName("新组织");
        newEntity.setCreditCode("NEW_ORG");

        when(organizationMapper.createDtoToEntity(mockCreateDto)).thenReturn(newEntity);
        when(organizationRepository.insert(newEntity)).thenReturn(1);
        newEntity.setId(100);

        // When
        Integer result = organizationService.add(mockCreateDto);

        // Then
        assertThat(result).isEqualTo(100);

        verify(organizationMapper).createDtoToEntity(mockCreateDto);
        verify(organizationRepository).insert(newEntity);
        verify(organizationMapper, never()).entityToBo(any());
    }

    @Test
    @DisplayName("create - 机构名称已存在")
    void testAdd_CodeExists() {
        // Given
        OrganizationEntity newEntity = new OrganizationEntity();
        when(organizationMapper.createDtoToEntity(mockCreateDto)).thenReturn(newEntity);
        when(organizationRepository.insert(newEntity)).thenThrow(new DuplicateKeyException("Duplicate key"));

        // When & Then
        assertThatThrownBy(() -> organizationService.add(mockCreateDto))
                .isInstanceOf(BusinessRuntimeException.class)
                .hasMessage("机构名称已存在");

        verify(organizationMapper).createDtoToEntity(mockCreateDto);
        verify(organizationRepository).insert(newEntity);
        verify(organizationMapper, never()).entityToBo(any());
    }

    // ==================== update方法测试 ====================

    @Test
    @DisplayName("update - 成功更新组织")
    void testUpdate_Success() {
        // Given
        OrganizationEntity updateEntity = new OrganizationEntity();
        updateEntity.setId(1);
        updateEntity.setOrganizationName("更新组织");

        when(organizationRepository.selectById(1)).thenReturn(mockEntity);
        when(organizationMapper.updateDtoToEntity(mockUpdateDto)).thenReturn(updateEntity);
        when(organizationRepository.updateById(any(OrganizationEntity.class))).thenReturn(1);

        // When
        organizationService.update(mockUpdateDto);

        // Then
        verify(organizationRepository).selectById(1);
        verify(organizationMapper).updateDtoToEntity(mockUpdateDto);
        verify(organizationRepository).updateById(any(OrganizationEntity.class));
    }

    @Test
    @DisplayName("update - 组织不存在")
    void testUpdate_NotFound() {
        // Given
        when(organizationRepository.selectById(1)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> organizationService.update(mockUpdateDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("机构不存在");

        verify(organizationRepository).selectById(1);
        verify(organizationMapper, never()).updateDtoToEntity(any());
        verify(organizationRepository, never()).updateById(any(OrganizationEntity.class));
    }

    @Test
    @DisplayName("update - 机构名称已存在")
    void testUpdate_CodeExists() {
        // Given
        OrganizationEntity updateEntity = new OrganizationEntity();
        updateEntity.setId(1);

        when(organizationRepository.selectById(1)).thenReturn(mockEntity);
        when(organizationMapper.updateDtoToEntity(mockUpdateDto)).thenReturn(updateEntity);
        when(organizationRepository.updateById(any(OrganizationEntity.class))).thenThrow(new DuplicateKeyException("Duplicate key"));

        // When & Then
        assertThatThrownBy(() -> organizationService.update(mockUpdateDto))
                .isInstanceOf(BusinessRuntimeException.class)
                .hasMessage("机构名称已存在");

        verify(organizationRepository).selectById(1);
        verify(organizationMapper).updateDtoToEntity(mockUpdateDto);
        verify(organizationRepository).updateById(any(OrganizationEntity.class));
    }

    // ==================== delete方法测试 ====================

    @Test
    @DisplayName("delete - 成功删除组织")
    void testDelete_Success() {
        // Given
        when(organizationRepository.selectById(1)).thenReturn(mockEntity);
        when(organizationRepository.deleteById(1)).thenReturn(1);

        // When
        organizationService.delete(1);

        // Then
        verify(organizationRepository).selectById(1);
        verify(organizationRepository).deleteById(1);
    }

    @Test
    @DisplayName("delete - 组织不存在")
    void testDelete_NotFound() {
        // Given
        when(organizationRepository.selectById(1)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> organizationService.delete(1))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("机构不存在");

        verify(organizationRepository).selectById(1);
        verify(organizationRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("delete - 删除失败")
    void testDelete_Failed() {
        // Given
        when(organizationRepository.selectById(1)).thenReturn(mockEntity);
        when(organizationRepository.deleteById(1)).thenReturn(0);

        // When & Then
        assertThatThrownBy(() -> organizationService.delete(1))
                .isInstanceOf(BusinessRuntimeException.class)
                .hasMessage("删除组织失败");

        verify(organizationRepository).selectById(1);
        verify(organizationRepository).deleteById(1);
    }

    // ==================== findOrganizationPage方法测试 ====================

    @Test
    @DisplayName("findOrganizationPage - 成功分页查询组织")
    void testFindOrganizationPage_Success() {
        // Given
        when(organizationMapper.queryDtoToQo(mockQueryDto)).thenReturn(mockQueryQo);

        PageResult<OrganizationBo> expectedResult = new PageResult<>();
        expectedResult.setList(List.of(mockBo));
        expectedResult.setTotal(1L);
        when(organizationMapper.pageEntityToPageBo(any())).thenReturn(expectedResult);

        // When
        PageResult<OrganizationBo> result = organizationService.findOrganizationPage(mockQueryDto, mockPageParam);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getList()).hasSize(1);
        assertThat(result.getList().get(0).getId()).isEqualTo(1);

        verify(organizationMapper).queryDtoToQo(mockQueryDto);
        verify(organizationMapper).pageEntityToPageBo(any());
    }

    @Test
    @DisplayName("findOrganizationPage - 分页查询结果为空")
    void testFindOrganizationPage_Empty() {
        // Given
        when(organizationMapper.queryDtoToQo(mockQueryDto)).thenReturn(mockQueryQo);
        PageResult<OrganizationBo> expectedResult = new PageResult<>();
        expectedResult.setList(List.of());
        when(organizationMapper.pageEntityToPageBo(any())).thenReturn(expectedResult);

        // When
        PageResult<OrganizationBo> result = organizationService.findOrganizationPage(mockQueryDto, mockPageParam);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getList()).isEmpty();

        verify(organizationMapper).queryDtoToQo(mockQueryDto);
        verify(organizationMapper).pageEntityToPageBo(any());
    }
}
