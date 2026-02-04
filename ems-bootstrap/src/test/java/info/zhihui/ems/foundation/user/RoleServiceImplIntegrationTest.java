package info.zhihui.ems.foundation.user;

import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.foundation.user.bo.RoleBo;
import info.zhihui.ems.foundation.user.bo.RoleDetailBo;
import info.zhihui.ems.foundation.user.dto.RoleCreateDto;
import info.zhihui.ems.foundation.user.dto.RoleQueryDto;
import info.zhihui.ems.foundation.user.dto.RoleMenuSaveDto;
import info.zhihui.ems.foundation.user.dto.RoleUpdateDto;
import info.zhihui.ems.foundation.user.entity.MenuAuthEntity;
import info.zhihui.ems.foundation.user.entity.RoleMenuEntity;
import info.zhihui.ems.foundation.user.repository.MenuAuthRepository;
import info.zhihui.ems.foundation.user.repository.RoleMenuRepository;
import info.zhihui.ems.foundation.user.service.RoleService;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 角色服务集成测试
 */
@SpringBootTest
@ActiveProfiles("integrationtest")
@Transactional
@Rollback
@DisplayName("角色服务集成测试")
class RoleServiceImplIntegrationTest {

    @Autowired
    private RoleService roleService;

    @Autowired
    private RoleMenuRepository roleMenuRepository;

    @Autowired
    private MenuAuthRepository menuAuthRepository;

    @Test
    @DisplayName("分页查询角色列表 - 成功")
    void testFindPage_Success() {
        // Given
        RoleQueryDto dto = new RoleQueryDto();
        PageParam pageParam = new PageParam();

        // When
        PageResult<RoleBo> result = roleService.findPage(dto, pageParam);

        // Then
        assertNotNull(result);
        assertEquals(3, result.getTotal());
        assertNotNull(result.getList());
        assertEquals(result.getList().size(), result.getTotal());
    }

    @Test
    @DisplayName("分页查询角色列表 - 按角色名称查询")
    void testFindPage_ByRoleName() {
        // Given
        RoleQueryDto dto = new RoleQueryDto();
        dto.setRoleNameLike("管理员");
        PageParam pageParam = new PageParam();

        // When
        PageResult<RoleBo> result = roleService.findPage(dto, pageParam);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertTrue(result.getList().stream()
                .anyMatch(role -> role.getRoleName().contains("管理员")));
    }

    @Test
    @DisplayName("分页查询角色列表 - 按角色标识查询")
    void testFindPage_ByRoleKey() {
        // Given
        RoleQueryDto dto = new RoleQueryDto();
        dto.setRoleKey("admin");
        PageParam pageParam = new PageParam();

        // When
        PageResult<RoleBo> result = roleService.findPage(dto, pageParam);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertTrue(result.getList().stream()
                .anyMatch(role -> "admin".equals(role.getRoleKey())));
    }

    @Test
    @DisplayName("分页查询角色列表 - 按系统角色查询")
    void testFindPage_BySystemRole() {
        // Given
        RoleQueryDto dto = new RoleQueryDto();
        dto.setIsSystem(true);
        PageParam pageParam = new PageParam();

        // When
        PageResult<RoleBo> result = roleService.findPage(dto, pageParam);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertTrue(result.getList().stream()
                .allMatch(RoleBo::getIsSystem));
    }

    @Test
    @DisplayName("分页查询角色列表 - 按禁用状态查询")
    void testFindPage_ByDisabled() {
        // Given
        RoleQueryDto dto = new RoleQueryDto();
        dto.setIsDisabled(true);
        PageParam pageParam = new PageParam();

        // When
        PageResult<RoleBo> result = roleService.findPage(dto, pageParam);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertFalse(result.getList().stream()
                .noneMatch(RoleBo::getIsDisabled));
    }

    @Test
    @DisplayName("分页查询角色列表 - 按ID集合查询")
    void testFindPage_ByIds() {
        // Given
        RoleQueryDto dto = new RoleQueryDto();
        dto.setIds(Arrays.asList(1, 2));
        PageParam pageParam = new PageParam();

        // When
        PageResult<RoleBo> result = roleService.findPage(dto, pageParam);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotal());
        assertTrue(result.getList().stream()
                .allMatch(role -> Arrays.asList(1, 2).contains(role.getId())));
    }

    @Test
    @DisplayName("分页查询角色列表 - 排除ID集合查询")
    void testFindPage_ByExcludeIds() {
        // Given
        RoleQueryDto dto = new RoleQueryDto();
        dto.setExcludeIds(List.of(1));
        PageParam pageParam = new PageParam();

        // When
        PageResult<RoleBo> result = roleService.findPage(dto, pageParam);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotal());
        assertTrue(result.getList().stream()
                .noneMatch(role -> role.getId().equals(1)));
    }

    @Test
    @DisplayName("分页查询角色列表 - 空结果")
    void testFindPage_EmptyResult() {
        // Given
        RoleQueryDto dto = new RoleQueryDto();
        dto.setRoleNameLike("不存在的角色名称");
        PageParam pageParam = new PageParam();

        // When
        PageResult<RoleBo> result = roleService.findPage(dto, pageParam);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotal());
        assertTrue(result.getList().isEmpty());
    }

    @Test
    @DisplayName("分页查询角色列表 - 多条件组合查询")
    void testFindPage_MultipleConditions() {
        // Given
        RoleQueryDto dto = new RoleQueryDto();
        dto.setRoleNameLike("管理");
        dto.setIsSystem(true);
        dto.setIsDisabled(false);
        PageParam pageParam = new PageParam();

        // When
        PageResult<RoleBo> result = roleService.findPage(dto, pageParam);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertTrue(result.getList().stream()
                .allMatch(role -> role.getRoleName().contains("管理")
                        && role.getIsSystem()
                        && !role.getIsDisabled()));
    }

    @Test
    @DisplayName("查询角色列表 - 成功")
    void testFindList_Success() {
        // Given
        RoleQueryDto dto = new RoleQueryDto();

        // When
        List<RoleBo> result = roleService.findList(dto);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
    }

    @Test
    @DisplayName("查询角色列表 - 按ID集合查询")
    void testFindList_ByIds() {
        // Given
        RoleQueryDto dto = new RoleQueryDto();
        dto.setIds(Arrays.asList(1, 2));

        // When
        List<RoleBo> result = roleService.findList(dto);

        // Then
        assertNotNull(result);
        assertTrue(result.stream()
                .allMatch(role -> Arrays.asList(1, 2).contains(role.getId())));
    }

    @Test
    @DisplayName("获取角色详情 - 成功")
    void testGetDetail_Success() {
        // Given
        Integer roleId = 1;

        // When
        RoleDetailBo result = roleService.getDetail(roleId);

        // Then
        assertNotNull(result);
        assertEquals(roleId, result.getId());
        assertEquals("超级管理员", result.getRoleName());
        assertEquals("admin", result.getRoleKey());
        assertEquals(1, result.getSortNum());
        assertEquals("系统超级管理员角色", result.getRemark());
        assertTrue(result.getIsSystem());
        assertFalse(result.getIsDisabled());

        // 验证菜单ID列表
        assertNotNull(result.getMenuIds());
        assertEquals(5, result.getMenuIds().size());
        assertTrue(result.getMenuIds().containsAll(List.of(1, 2, 3, 4, 5)));

        // 验证权限列表
        assertNotNull(result.getPermissions());
    }

    @Test
    @DisplayName("获取角色详情 - 角色不存在")
    void testGetDetail_NotFound() {
        // Given
        Integer roleId = 99999;

        // When & Then
        assertThrows(NotFoundException.class, () -> roleService.getDetail(roleId));
    }

    @Test
    @DisplayName("新增角色 - 成功")
    void testAdd_Success() {
        // Given
        RoleCreateDto dto = new RoleCreateDto();
        dto.setRoleName("测试角色");
        dto.setRoleKey("test_role");
        dto.setSortNum(100);
        dto.setIsSystem(false);
        dto.setIsDisabled(false);
        dto.setRemark("测试角色备注");

        // When
        Integer result = roleService.add(dto);

        // Then
        assertNotNull(result);
        assertTrue(result > 0);

        // 验证角色是否创建成功
        RoleDetailBo createdRole = roleService.getDetail(result);
        assertEquals(dto.getRoleName(), createdRole.getRoleName());
        assertEquals(dto.getRoleKey(), createdRole.getRoleKey());
        assertEquals(dto.getSortNum(), createdRole.getSortNum());
        assertEquals(dto.getRemark(), createdRole.getRemark());
        assertEquals(dto.getIsSystem(), createdRole.getIsSystem());
        assertEquals(dto.getIsDisabled(), createdRole.getIsDisabled());

        // 验证菜单ID列表为空（新创建的角色没有关联菜单）
        assertNotNull(createdRole.getMenuIds());
        assertTrue(createdRole.getMenuIds().isEmpty());

        // 验证权限列表为空（新创建的角色没有权限）
        assertNotNull(createdRole.getPermissions());
        assertTrue(createdRole.getPermissions().isEmpty());
    }

    @Test
    @DisplayName("新增角色 - 角色标识已存在")
    void testAdd_RoleKeyExists() {
        // Given
        RoleCreateDto dto = new RoleCreateDto();
        dto.setRoleName("测试角色");
        dto.setRoleKey("admin"); // 使用已存在的角色标识
        dto.setSortNum(100);
        dto.setIsSystem(false);
        dto.setIsDisabled(false);

        // When & Then
        assertThrows(BusinessRuntimeException.class, () -> roleService.add(dto));
    }

    @Test
    @DisplayName("更新角色 - 成功")
    void testUpdate_Success() {
        // Given
        Integer roleId = 2;
        RoleUpdateDto dto = new RoleUpdateDto();
        dto.setId(roleId);
        dto.setRoleName("更新后的角色名");
        dto.setRoleKey("updated_role");
        dto.setSortNum(200);
        dto.setIsDisabled(true);
        dto.setRemark("更新后的备注");

        // When
        roleService.update(dto);

        // Then
        RoleDetailBo updatedRole = roleService.getDetail(roleId);
        assertEquals(dto.getRoleName(), updatedRole.getRoleName());
        assertEquals(dto.getRoleKey(), updatedRole.getRoleKey());
        assertEquals(dto.getSortNum(), updatedRole.getSortNum());
        assertEquals(dto.getRemark(), updatedRole.getRemark());
        assertEquals(dto.getIsDisabled(), updatedRole.getIsDisabled());

        // 验证系统角色标识不会被更新（业务逻辑保护）
        assertNotNull(updatedRole.getIsSystem());

        // 验证菜单ID列表和权限列表仍然存在
        assertNotNull(updatedRole.getMenuIds());
        assertNotNull(updatedRole.getPermissions());
    }

    @Test
    @DisplayName("更新角色 - 角色不存在")
    void testUpdate_NotFound() {
        // Given
        RoleUpdateDto dto = new RoleUpdateDto();
        dto.setId(99999);
        dto.setRoleName("不存在的角色");
        dto.setRoleKey("not_exist");

        // When & Then
        assertThrows(NotFoundException.class, () -> roleService.update(dto));
    }

    @Test
    @DisplayName("更新角色 - 角色标识已存在")
    void testUpdate_RoleKeyExists() {
        // Given
        RoleUpdateDto dto = new RoleUpdateDto();
        dto.setId(2);
        dto.setRoleName("测试角色");
        dto.setRoleKey("admin"); // 使用已存在的角色标识

        // When & Then
        assertThrows(BusinessRuntimeException.class, () -> roleService.update(dto));
    }

    @Test
    @DisplayName("删除角色 - 成功")
    void testDelete_Success() {
        // Given - 先创建一个测试角色
        RoleCreateDto createDto = new RoleCreateDto();
        createDto.setRoleName("待删除角色");
        createDto.setRoleKey("to_delete");
        createDto.setSortNum(100);
        createDto.setIsSystem(false);
        createDto.setIsDisabled(false);
        Integer roleId = roleService.add(createDto);

        // When
        assertDoesNotThrow(() -> roleService.delete(roleId));

        // Then - 验证角色已被逻辑删除
        assertThrows(NotFoundException.class, () -> roleService.getDetail(roleId));
    }

    @Test
    @DisplayName("删除角色 - 角色不存在")
    void testDelete_NotFound() {
        // Given
        Integer roleId = 99999;

        // When & Then
        assertThrows(NotFoundException.class, () -> roleService.delete(roleId));
    }

    @Test
    @DisplayName("删除角色 - 系统内置角色不允许删除")
    void testDelete_SystemRole() {
        // Given - 假设ID为1的是系统内置角色
        Integer roleId = 1;

        // When & Then
        assertThrows(BusinessRuntimeException.class, () -> roleService.delete(roleId));
    }

    @Test
    @DisplayName("删除角色 - 角色已绑定用户不允许删除")
    void testDelete_RoleHasUsers() {
        // Given - 角色ID为2已被用户绑定
        Integer roleId = 2;

        // When & Then
        assertThrows(BusinessRuntimeException.class, () -> roleService.delete(roleId));
    }

    @Test
    @DisplayName("保存角色菜单关联 - 成功")
    void testSaveRoleMenu_Success() {
        // Given
        RoleMenuSaveDto dto = new RoleMenuSaveDto();
        dto.setRoleId(2);
        dto.setMenuIds(Arrays.asList(1, 2, 3));

        // When
        assertDoesNotThrow(() -> roleService.saveRoleMenu(dto));

        // Then - 验证角色菜单关联是否保存成功
        RoleDetailBo roleAfterSave = roleService.getDetail(dto.getRoleId());
        assertNotNull(roleAfterSave.getMenuIds());
        assertEquals(3, roleAfterSave.getMenuIds().size());
        assertTrue(roleAfterSave.getMenuIds().containsAll(dto.getMenuIds()));

        // 验证通过 getRolePermissions 能查出对应的权限
        List<String> permissions = roleService.getRolePermissions(dto.getRoleId());
        assertNotNull(permissions);
        assertFalse(permissions.isEmpty());

        // 验证权限列表与角色详情中的权限一致
        assertNotNull(roleAfterSave.getPermissions());
        assertEquals(permissions.size(), roleAfterSave.getPermissions().size());
        assertTrue(roleAfterSave.getPermissions().containsAll(permissions));
    }

    @Test
    @DisplayName("保存角色菜单关联 - 角色不存在")
    void testSaveRoleMenu_RoleNotFound() {
        // Given
        RoleMenuSaveDto dto = new RoleMenuSaveDto();
        dto.setRoleId(99999);
        dto.setMenuIds(Arrays.asList(1, 2));

        // When & Then
        assertThrows(NotFoundException.class, () -> roleService.saveRoleMenu(dto));
    }

    @Test
    @DisplayName("保存角色菜单关联 - 空菜单列表")
    void testSaveRoleMenu_EmptyMenuIds() {
        // Given
        RoleMenuSaveDto dto = new RoleMenuSaveDto();
        dto.setRoleId(1);
        dto.setMenuIds(List.of());

        // When
        assertDoesNotThrow(() -> roleService.saveRoleMenu(dto));
    }

    @Test
    @DisplayName("获取角色权限 - 成功")
    void testGetRolePermission_Success() {
        // Given
        Integer roleId = 1;

        // When
        List<String> result = roleService.getRolePermissions(roleId);

        // Then
        assertNotNull(result);
        // 权限列表可能为空，这是正常的
    }

    @Test
    @DisplayName("获取角色权限 - 排除已删除菜单权限")
    void testGetRolePermission_ShouldIgnoreDeletedMenu() {
        RoleCreateDto roleCreateDto = new RoleCreateDto();
        roleCreateDto.setRoleName("删除菜单权限验证角色");
        roleCreateDto.setRoleKey("deleted-menu-role");
        roleCreateDto.setSortNum(99);
        roleCreateDto.setIsSystem(false);
        roleCreateDto.setIsDisabled(false);
        Integer roleId = roleService.add(roleCreateDto);

        Integer deletedMenuId = 6;
        List<String> existingPermissions = menuAuthRepository.selectPermissionCodesByMenuId(deletedMenuId);
        if (existingPermissions == null || !existingPermissions.contains("deleted:permission")) {
            MenuAuthEntity menuAuthEntity = new MenuAuthEntity();
            menuAuthEntity.setMenuId(deletedMenuId);
            menuAuthEntity.setPermissionCode("deleted:permission");
            menuAuthEntity.setCreateTime(LocalDateTime.now());
            menuAuthRepository.insert(menuAuthEntity);
        }

        RoleMenuEntity roleMenuEntity = new RoleMenuEntity();
        roleMenuEntity.setRoleId(roleId);
        roleMenuEntity.setMenuId(deletedMenuId);
        roleMenuEntity.setCreateTime(LocalDateTime.now());
        roleMenuRepository.insert(roleMenuEntity);

        List<String> result = roleService.getRolePermissions(roleId);

        assertNotNull(result);
        assertThat(result).doesNotContain("deleted:permission");
    }

    @Test
    @DisplayName("获取角色权限 - 不存在的角色")
    void testGetRolePermission_NonExistentRole() {
        // Given
        Integer roleId = 99999;

        // When
        List<String> result = roleService.getRolePermissions(roleId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("判断角色是否拥有权限 - 已删除菜单权限不应生效")
    void testExistsPermission_ShouldIgnoreDeletedMenu() {
        RoleCreateDto roleCreateDto = new RoleCreateDto();
        roleCreateDto.setRoleName("权限判断删除菜单角色");
        roleCreateDto.setRoleKey("deleted-menu-permission-role");
        roleCreateDto.setSortNum(98);
        roleCreateDto.setIsSystem(false);
        roleCreateDto.setIsDisabled(false);
        Integer roleId = roleService.add(roleCreateDto);

        Integer deletedMenuId = 6;
        List<String> existingPermissions = menuAuthRepository.selectPermissionCodesByMenuId(deletedMenuId);
        if (existingPermissions == null || !existingPermissions.contains("deleted:permission")) {
            MenuAuthEntity menuAuthEntity = new MenuAuthEntity();
            menuAuthEntity.setMenuId(deletedMenuId);
            menuAuthEntity.setPermissionCode("deleted:permission");
            menuAuthEntity.setCreateTime(LocalDateTime.now());
            menuAuthRepository.insert(menuAuthEntity);
        }

        RoleMenuEntity roleMenuEntity = new RoleMenuEntity();
        roleMenuEntity.setRoleId(roleId);
        roleMenuEntity.setMenuId(deletedMenuId);
        roleMenuEntity.setCreateTime(LocalDateTime.now());
        roleMenuRepository.insert(roleMenuEntity);

        boolean hasPermission = roleService.existsPermission(java.util.Set.of(roleId), "deleted:permission");

        assertFalse(hasPermission);
    }

    // 验证测试 - 测试参数校验
    @Test
    @DisplayName("参数校验 - findPage 参数为null")
    void testValidation_FindPage_NullParams() {
        // When & Then
        assertThrows(ConstraintViolationException.class, () -> roleService.findPage(null, new PageParam()));
        assertThrows(ConstraintViolationException.class, () -> roleService.findPage(new RoleQueryDto(), null));
    }

    @Test
    @DisplayName("参数校验 - findList 参数为null")
    void testValidation_FindList_NullParams() {
        // When & Then
        assertThrows(ConstraintViolationException.class, () -> roleService.findList(null));
    }

    @Test
    @DisplayName("参数校验 - getDetail 参数为null")
    void testValidation_GetDetail_NullParams() {
        // When & Then
        assertThrows(ConstraintViolationException.class, () -> roleService.getDetail(null));
    }

    @Test
    @DisplayName("参数校验 - add 参数为null")
    void testValidation_Add_NullParams() {
        // When & Then
        assertThrows(ConstraintViolationException.class, () -> roleService.add(null));
    }

    @Test
    @DisplayName("参数校验 - update 参数为null")
    void testValidation_Update_NullParams() {
        // When & Then
        assertThrows(ConstraintViolationException.class, () -> roleService.update(null));
    }

    @Test
    @DisplayName("参数校验 - delete 参数为null")
    void testValidation_Delete_NullParams() {
        // When & Then
        assertThrows(ConstraintViolationException.class, () -> roleService.delete(null));
    }

    @Test
    @DisplayName("参数校验 - saveRoleMenu 参数为null")
    void testValidation_SaveRoleMenu_NullParams() {
        // When & Then
        assertThrows(ConstraintViolationException.class, () -> roleService.saveRoleMenu(null));
    }

    @Test
    @DisplayName("参数校验 - getRolePermissions 参数为null")
    void testValidation_GetRolePermissions_NullParams() {
        // 因为加了缓存
        assertThrows(IllegalArgumentException.class, () -> roleService.getRolePermissions(null));
    }

    // 分页测试
    @Test
    @DisplayName("分页测试 - 第一页")
    void testPagination_FirstPage() {
        // Given
        RoleQueryDto dto = new RoleQueryDto();
        PageParam pageParam = new PageParam().setPageSize(2).setPageNum(1);

        // When
        PageResult<RoleBo> result = roleService.findPage(dto, pageParam);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getList().size());
    }

    @Test
    @DisplayName("分页测试 - 第二页")
    void testPagination_SecondPage() {
        // Given
        RoleQueryDto dto = new RoleQueryDto();
        PageParam pageParam = new PageParam().setPageSize(2).setPageNum(2);

        // When
        PageResult<RoleBo> result = roleService.findPage(dto, pageParam);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getList().size());
    }
}
