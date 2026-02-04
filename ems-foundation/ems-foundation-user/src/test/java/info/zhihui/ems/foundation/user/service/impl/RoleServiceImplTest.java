package info.zhihui.ems.foundation.user.service.impl;

import com.github.pagehelper.PageInfo;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.foundation.user.bo.RoleBo;
import info.zhihui.ems.foundation.user.bo.RoleDetailBo;
import info.zhihui.ems.foundation.user.dto.RoleCreateDto;
import info.zhihui.ems.foundation.user.dto.RoleMenuSaveDto;
import info.zhihui.ems.foundation.user.dto.RoleQueryDto;
import info.zhihui.ems.foundation.user.dto.RoleUpdateDto;
import info.zhihui.ems.foundation.user.entity.RoleEntity;
import info.zhihui.ems.foundation.user.mapper.RoleMapper;
import info.zhihui.ems.foundation.user.enums.RoleEnum;
import info.zhihui.ems.foundation.user.qo.RoleQueryQo;
import info.zhihui.ems.foundation.user.repository.RoleMenuRepository;
import info.zhihui.ems.foundation.user.repository.RoleRepository;
import info.zhihui.ems.foundation.user.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 角色服务实现类测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("角色服务实现类测试")
class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RoleMenuRepository roleMenuRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private RoleMapper roleMapper;

    @InjectMocks
    private RoleServiceImpl roleService;

    // 测试数据
    private RoleEntity mockEntity;
    private RoleBo mockBo;
    private RoleDetailBo mockDetailBo;
    private RoleCreateDto mockCreateDto;
    private RoleUpdateDto mockUpdateDto;
    private RoleMenuSaveDto mockRoleMenuSaveDto;
    private RoleQueryDto mockQueryDto;
    private RoleQueryQo mockQueryQo;
    private PageParam mockPageParam;

    @BeforeEach
    void setUp() {
        mockEntity = new RoleEntity();
        mockEntity.setId(1);
        mockEntity.setRoleName("测试角色");
        mockEntity.setRoleKey("test_role");
        mockEntity.setSortNum(1);
        mockEntity.setRemark("测试角色描述");
        mockEntity.setIsSystem(false);
        mockEntity.setIsDisabled(false);

        mockBo = new RoleBo();
        mockBo.setId(1);
        mockBo.setRoleName("测试角色");
        mockBo.setRoleKey("test_role");
        mockBo.setSortNum(1);
        mockBo.setRemark("测试角色描述");
        mockBo.setIsSystem(false);
        mockBo.setIsDisabled(false);

        mockDetailBo = new RoleDetailBo();
        mockDetailBo.setId(1);
        mockDetailBo.setRoleName("测试角色");
        mockDetailBo.setRoleKey("test_role");
        mockDetailBo.setSortNum(1);
        mockDetailBo.setRemark("测试角色描述");
        mockDetailBo.setIsSystem(false);
        mockDetailBo.setIsDisabled(false);
        mockDetailBo.setMenuIds(Arrays.asList(1, 2, 3));
        mockDetailBo.setPermissions(Arrays.asList("user:read", "user:write"));

        mockCreateDto = new RoleCreateDto();
        mockCreateDto.setRoleName("新角色");
        mockCreateDto.setRoleKey("new_role");
        mockCreateDto.setSortNum(2);
        mockCreateDto.setRemark("新角色描述");
        mockCreateDto.setIsSystem(false);
        mockCreateDto.setIsDisabled(false);
        mockCreateDto.setMenuIds(Arrays.asList(1, 2));

        mockUpdateDto = new RoleUpdateDto();
        mockUpdateDto.setId(1);
        mockUpdateDto.setRoleName("更新角色");
        mockUpdateDto.setRoleKey("updated_role");
        mockUpdateDto.setSortNum(3);
        mockUpdateDto.setRemark("更新角色描述");
        mockUpdateDto.setIsDisabled(false);

        mockRoleMenuSaveDto = new RoleMenuSaveDto();
        mockRoleMenuSaveDto.setRoleId(1);
        mockRoleMenuSaveDto.setMenuIds(Arrays.asList(1, 2, 3));

        mockQueryDto = new RoleQueryDto();
        mockQueryDto.setRoleNameLike("测试");
        mockQueryDto.setIsSystem(false);

        mockQueryQo = new RoleQueryQo();
        mockQueryQo.setRoleNameLike("测试");
        mockQueryQo.setIsSystem(false);

        mockPageParam = new PageParam();
        mockPageParam.setPageNum(1);
        mockPageParam.setPageSize(10);
    }

    // ==================== findPage 测试 ====================

    @Test
    @DisplayName("findPage - 成功分页查询角色")
    void testFindPage_Success() {
        // Given
        List<RoleEntity> entities = Collections.singletonList(mockEntity);
        PageInfo<RoleEntity> pageInfo = new PageInfo<>(entities);
        pageInfo.setTotal(1L);

        List<RoleBo> bos = Collections.singletonList(mockBo);
        PageResult<RoleBo> pageResult = new PageResult<>();
        pageResult.setList(bos);
        pageResult.setTotal(1L);

        when(roleMapper.queryDtoToQo(mockQueryDto)).thenReturn(mockQueryQo);
        when(roleMapper.pageEntityToPageBo(any())).thenReturn(pageResult);

        // Mock the repository call directly
        when(roleRepository.selectByQo(mockQueryQo)).thenReturn(entities);

        PageResult<RoleBo> result = roleService.findPage(mockQueryDto, mockPageParam);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getList()).hasSize(1);
        assertThat(result.getTotal()).isEqualTo(1L);
        assertThat(result.getList().get(0).getId()).isEqualTo(1);
        assertThat(result.getList().get(0).getRoleName()).isEqualTo("测试角色");

        verify(roleMapper).queryDtoToQo(mockQueryDto);
    }

    // ==================== findList 测试 ====================

    @Test
    @DisplayName("findList - 成功查询角色列表")
    void testFindList_Success() {
        // Given
        List<RoleEntity> entityList = Collections.singletonList(mockEntity);
        List<RoleBo> boList = Collections.singletonList(mockBo);

        when(roleMapper.queryDtoToQo(mockQueryDto)).thenReturn(mockQueryQo);
        when(roleRepository.selectByQo(mockQueryQo)).thenReturn(entityList);
        when(roleMapper.listEntityToBo(entityList)).thenReturn(boList);

        // When
        List<RoleBo> result = roleService.findList(mockQueryDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1);
        assertThat(result.get(0).getRoleName()).isEqualTo("测试角色");

        verify(roleMapper).queryDtoToQo(mockQueryDto);
        verify(roleRepository).selectByQo(mockQueryQo);
        verify(roleMapper).listEntityToBo(entityList);
    }

    @Test
    @DisplayName("findList - 查询结果为空")
    void testFindList_Empty() {
        // Given
        when(roleMapper.queryDtoToQo(mockQueryDto)).thenReturn(mockQueryQo);
        when(roleRepository.selectByQo(mockQueryQo)).thenReturn(Collections.emptyList());
        when(roleMapper.listEntityToBo(Collections.emptyList())).thenReturn(Collections.emptyList());

        // When
        List<RoleBo> result = roleService.findList(mockQueryDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(roleMapper).queryDtoToQo(mockQueryDto);
        verify(roleRepository).selectByQo(mockQueryQo);
        verify(roleMapper).listEntityToBo(Collections.emptyList());
        verify(roleMenuRepository, never()).selectMenuIdsByRoleIds(ArgumentMatchers.anyList());
        verify(roleRepository, never()).selectPermissionsByRoleId(anyInt());
    }

    // ==================== getDetail 测试 ====================

    @Test
    @DisplayName("getDetail - 成功获取角色详情")
    void testGetDetail_Success() {
        // Given
        List<Integer> menuIds = Arrays.asList(1, 2, 3);
        List<String> permissions = Arrays.asList("user:read", "user:write");

        when(roleRepository.selectById(1)).thenReturn(mockEntity);
        when(roleMapper.entityToDetailBo(mockEntity)).thenReturn(mockDetailBo);
        when(roleMenuRepository.selectMenuIdsByRoleIds(ArgumentMatchers.anyList())).thenReturn(menuIds);
        when(roleRepository.selectPermissionsByRoleId(1)).thenReturn(permissions);

        // When
        RoleDetailBo result = roleService.getDetail(1);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getRoleName()).isEqualTo("测试角色");
        assertThat(result.getMenuIds()).containsExactly(1, 2, 3);
        assertThat(result.getPermissions()).containsExactly("user:read", "user:write");

        verify(roleRepository).selectById(1);
        verify(roleMapper).entityToDetailBo(mockEntity);
        verify(roleMenuRepository).selectMenuIdsByRoleIds(ArgumentMatchers.anyList());
        verify(roleRepository).selectPermissionsByRoleId(1);
    }

    @Test
    @DisplayName("getDetail - 角色不存在")
    void testGetDetail_NotFound() {
        // Given
        when(roleRepository.selectById(999)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> roleService.getDetail(999))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("角色不存在");

        verify(roleRepository).selectById(999);
        verify(roleMapper, never()).entityToDetailBo(any());
        verify(roleMenuRepository, never()).selectMenuIdsByRoleIds(ArgumentMatchers.anyList());
    }

    // ==================== add 测试 ====================

    @Test
    @DisplayName("add - 成功创建角色")
    void testAdd_Success() {
        // Given
        RoleEntity newEntity = new RoleEntity();
        newEntity.setId(2);
        newEntity.setRoleName("新角色");
        newEntity.setRoleKey("new_role");

        when(roleMapper.createDtoToEntity(mockCreateDto)).thenReturn(newEntity);
        when(roleRepository.insert(newEntity)).thenReturn(1);

        // When
        Integer result = roleService.add(mockCreateDto);

        // Then
        assertThat(result).isEqualTo(2);

        verify(roleMapper).createDtoToEntity(mockCreateDto);
        verify(roleRepository).insert(newEntity);
    }

    @Test
    @DisplayName("add - 角色标识已存在")
    void testAdd_RoleKeyExists() {
        // Given
        RoleEntity newEntity = new RoleEntity();
        newEntity.setRoleKey("existing_role");

        when(roleMapper.createDtoToEntity(mockCreateDto)).thenReturn(newEntity);
        when(roleRepository.insert(newEntity)).thenThrow(new DuplicateKeyException("Duplicate entry"));

        // When & Then
        assertThatThrownBy(() -> roleService.add(mockCreateDto))
                .isInstanceOf(BusinessRuntimeException.class)
                .hasMessage("角色标识已存在");

        verify(roleMapper).createDtoToEntity(mockCreateDto);
        verify(roleRepository).insert(newEntity);
    }

    // ==================== delete 测试 ====================

    @Test
    @DisplayName("delete - 成功删除角色")
    void testDelete_Success() {
        RoleEntity entity = new RoleEntity();
        entity.setId(1);
        entity.setIsSystem(false);

        when(roleRepository.selectById(1)).thenReturn(entity);
        when(userRoleRepository.countByRoleId(1)).thenReturn(0);

        roleService.delete(1);

        verify(roleRepository).selectById(1);
        verify(userRoleRepository).countByRoleId(1);
        verify(roleRepository).deleteById(1);
        verify(roleMenuRepository).deleteByRoleId(1);
    }

    @Test
    @DisplayName("delete - 角色已被用户绑定")
    void testDelete_RoleHasUsers() {
        RoleEntity entity = new RoleEntity();
        entity.setId(1);
        entity.setIsSystem(false);

        when(roleRepository.selectById(1)).thenReturn(entity);
        when(userRoleRepository.countByRoleId(1)).thenReturn(1);

        assertThatThrownBy(() -> roleService.delete(1))
                .isInstanceOf(BusinessRuntimeException.class)
                .hasMessage("角色已被用户绑定，不允许删除");

        verify(roleRepository).selectById(1);
        verify(userRoleRepository).countByRoleId(1);
        verify(roleRepository, never()).deleteById(anyInt());
        verify(roleMenuRepository, never()).deleteByRoleId(anyInt());
    }

    // ==================== update 测试 ====================

    @Test
    @DisplayName("update - 成功更新角色")
    void testUpdate_Success() {
        // Given
        RoleEntity updateEntity = new RoleEntity();
        updateEntity.setId(1);
        updateEntity.setRoleName("更新角色");
        updateEntity.setRoleKey("updated_role");

        when(roleRepository.selectById(1)).thenReturn(mockEntity);
        when(roleMapper.updateDtoToEntity(mockUpdateDto)).thenReturn(updateEntity);
        when(roleRepository.updateById(updateEntity)).thenReturn(1);

        // When
        roleService.update(mockUpdateDto);

        // Then
        verify(roleRepository).selectById(1);
        verify(roleMapper).updateDtoToEntity(mockUpdateDto);
        verify(roleRepository).updateById(updateEntity);
    }

    @Test
    @DisplayName("update - 角色不存在")
    void testUpdate_NotFound() {
        // Given
        when(roleRepository.selectById(1)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> roleService.update(mockUpdateDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("角色不存在");

        verify(roleRepository).selectById(1);
        verify(roleMapper, never()).updateDtoToEntity(any());
        verify(roleRepository, never()).updateById(any(RoleEntity.class));
    }

    @Test
    @DisplayName("update - 角色标识已存在")
    void testUpdate_RoleKeyExists() {
        // Given
        RoleEntity existEntity = new RoleEntity();
        existEntity.setId(1);
        existEntity.setRoleKey("old_role");

        RoleUpdateDto dto = new RoleUpdateDto();
        dto.setId(1);
        dto.setRoleKey("existing_role");

        RoleEntity updateEntity = new RoleEntity();
        updateEntity.setId(1);
        updateEntity.setRoleKey("existing_role");

        RoleEntity conflictEntity = new RoleEntity();
        conflictEntity.setId(2);
        conflictEntity.setRoleKey("existing_role");

        when(roleRepository.selectById(1)).thenReturn(existEntity);
        when(roleRepository.selectByQo(any(RoleQueryQo.class))).thenReturn(List.of(conflictEntity));

        // When & Then
        assertThatThrownBy(() -> roleService.update(dto))
                .isInstanceOf(BusinessRuntimeException.class)
                .hasMessage("角色标识已存在");

        verify(roleRepository).selectById(1);
        verify(roleRepository).selectByQo(any(RoleQueryQo.class));
        verify(roleMapper, never()).updateDtoToEntity(any());
        verify(roleRepository, never()).updateById(any(RoleEntity.class));
    }

    @Test
    @DisplayName("update - 超管角色不能被禁用")
    void testUpdate_CannotDisableSuperAdmin() {
        // Given
        RoleEntity existEntity = new RoleEntity();
        existEntity.setId(1);
        existEntity.setRoleKey(RoleEnum.SUPER_ADMIN.getCode());
        existEntity.setIsSystem(true);
        existEntity.setIsDisabled(false);

        RoleUpdateDto dto = new RoleUpdateDto();
        dto.setId(1);
        dto.setRoleKey(RoleEnum.SUPER_ADMIN.getCode());
        dto.setIsDisabled(true); // 尝试禁用超管角色

        when(roleRepository.selectById(1)).thenReturn(existEntity);

        // When & Then
        assertThatThrownBy(() -> roleService.update(dto))
                .isInstanceOf(BusinessRuntimeException.class)
                .hasMessage("超管角色不能被禁用");

        verify(roleRepository).selectById(1);
        verify(roleMapper, never()).updateDtoToEntity(any());
        verify(roleRepository, never()).updateById(any(RoleEntity.class));
    }

    // ==================== saveRoleMenu 测试 ====================

    @Test
    @DisplayName("saveRoleMenu - 成功保存角色菜单关联")
    void testSaveRoleMenu_Success() {
        // Given
        when(roleRepository.selectById(1)).thenReturn(mockEntity);

        // When
        roleService.saveRoleMenu(mockRoleMenuSaveDto);

        // Then
        verify(roleRepository).selectById(1);
        verify(roleMenuRepository).deleteByRoleId(1);
        verify(roleMenuRepository).insert(anyList());
    }

    @Test
    @DisplayName("saveRoleMenu - 角色不存在")
    void testSaveRoleMenu_RoleNotFound() {
        // Given
        when(roleRepository.selectById(999)).thenReturn(null);

        RoleMenuSaveDto dto = new RoleMenuSaveDto();
        dto.setRoleId(999);

        // When & Then
        assertThatThrownBy(() -> roleService.saveRoleMenu(dto))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("角色不存在");

        verify(roleRepository).selectById(999);
        verify(roleMenuRepository, never()).deleteByRoleId(anyInt());
        verify(roleMenuRepository, never()).insert(anyList());
    }

    @Test
    @DisplayName("saveRoleMenu - 菜单ID列表为空")
    void testSaveRoleMenu_EmptyMenuIds() {
        // Given
        RoleMenuSaveDto dto = new RoleMenuSaveDto();
        dto.setRoleId(1);
        dto.setMenuIds(Collections.emptyList());

        when(roleRepository.selectById(1)).thenReturn(mockEntity);

        // When
        roleService.saveRoleMenu(dto);

        // Then
        verify(roleRepository).selectById(1);
        verify(roleMenuRepository).deleteByRoleId(1);
        verify(roleMenuRepository, never()).insert(anyList());
    }

    // ==================== getRolePermissions 测试 ====================

    @Test
    @DisplayName("getRolePermissions - 普通角色返回菜单权限")
    void testGetRolePermissions_NormalRole() {
        List<String> permissions = Arrays.asList("user:read", "user:write");
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setId(1);
        roleEntity.setRoleKey("normal_role");

        when(roleRepository.selectById(1)).thenReturn(roleEntity);
        when(roleRepository.selectPermissionsByRoleId(1)).thenReturn(permissions);

        List<String> result = roleService.getRolePermissions(1);

        assertThat(result).containsExactlyInAnyOrder("user:read", "user:write");

        verify(roleRepository).selectById(1);
        verify(roleRepository).selectPermissionsByRoleId(1);
    }

    @Test
    @DisplayName("getRolePermissions - 超级管理员返回全部权限")
    void testGetRolePermissions_SuperAdmin() {
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setId(3);
        roleEntity.setRoleKey(RoleEnum.SUPER_ADMIN.getCode());

        when(roleRepository.selectById(3)).thenReturn(roleEntity);
        List<String> result = roleService.getRolePermissions(3);

        assertThat(result).containsExactly("*:*:*");

        verify(roleRepository).selectById(3);
        verify(roleRepository, never()).selectPermissionsByRoleId(anyInt());
    }

    @Test
    @DisplayName("existsPermission - 角色拥有指定权限")
    void testExistsPermission_True() {
        when(roleRepository.existsPermission(ArgumentMatchers.anyList(), eq("user:read"))).thenReturn(true);

        boolean result = roleService.existsPermission(Set.of(1, 2), "user:read");

        assertThat(result).isTrue();
        verify(roleRepository).existsPermission(ArgumentMatchers.anyList(), eq("user:read"));
    }

    @Test
    @DisplayName("existsPermission - 权限标识为空")
    void testExistsPermission_BlankPermission() {
        boolean result = roleService.existsPermission(Set.of(1, 2), " ");

        assertThat(result).isFalse();
        verify(roleRepository, never()).existsPermission(ArgumentMatchers.anyList(), anyString());
    }

}
