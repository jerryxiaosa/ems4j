package info.zhihui.ems.foundation.user;

import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.foundation.user.bo.MenuBo;
import info.zhihui.ems.foundation.user.bo.MenuDetailBo;
import info.zhihui.ems.foundation.user.dto.MenuCreateDto;
import info.zhihui.ems.foundation.user.dto.MenuQueryDto;
import info.zhihui.ems.foundation.user.dto.MenuUpdateDto;
import info.zhihui.ems.foundation.user.enums.MenuSourceEnum;
import info.zhihui.ems.foundation.user.enums.MenuTypeEnum;
import info.zhihui.ems.foundation.user.service.MenuService;
import info.zhihui.ems.foundation.user.service.RoleService;
import info.zhihui.ems.foundation.user.service.impl.RoleServiceImpl;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * MenuService 集成测试
 *
 * @author jerryxiaosa
 */
@SpringBootTest
@ActiveProfiles("integrationtest")
@Transactional
@Rollback
@DisplayName("菜单服务集成测试")
class MenuServiceImplIntegrationTest {

    @Autowired
    private MenuService menuService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private CacheManager cacheManager;

    @Test
    @DisplayName("查询菜单列表 - 成功场景")
    void testFindList_Success() {
        // Given
        MenuQueryDto dto = new MenuQueryDto();

        // When
        List<MenuBo> result = menuService.findList(dto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();

        // 验证返回的菜单数据
        MenuBo firstMenu = result.get(0);
        assertThat(firstMenu.getId()).isNotNull();
        assertThat(firstMenu.getMenuName()).isNotBlank();
        assertThat(firstMenu.getMenuKey()).isNotBlank();
    }

    @Test
    @DisplayName("查询菜单列表 - 按菜单名称模糊查询")
    void testFindList_ByMenuName() {
        // Given
        MenuQueryDto dto = new MenuQueryDto();
        dto.setMenuNameLike("系统");

        // When
        List<MenuBo> result = menuService.findList(dto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);

        // 验证查询结果包含指定菜单名称
        result.forEach(menu ->
            assertThat(menu.getMenuName()).contains("系统")
        );
    }

    @Test
    @DisplayName("查询菜单列表 - 按菜单标识查询")
    void testFindList_ByMenuKey() {
        // Given
        MenuQueryDto dto = new MenuQueryDto();
        dto.setMenuKey("user-manage");

        // When
        List<MenuBo> result = menuService.findList(dto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMenuKey()).isEqualTo("user-manage");
        assertThat(result.get(0).getMenuName()).isEqualTo("用户管理");
    }

    @Test
    @DisplayName("查询菜单列表 - 按父菜单ID查询")
    void testFindList_ByPid() {
        // Given
        MenuQueryDto dto = new MenuQueryDto();
        dto.setPid(1); // 系统管理的子菜单

        // When
        List<MenuBo> result = menuService.findList(dto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);

        // 验证查询结果的父菜单ID
        result.forEach(menu ->
            assertThat(menu.getPid()).isEqualTo(1)
        );
    }

    @Test
    @DisplayName("查询菜单列表 - 按ID集合查询")
    void testFindList_ByIds() {
        // Given
        MenuQueryDto dto = new MenuQueryDto();
        dto.setIds(List.of(1, 2, 3));

        // When
        List<MenuBo> result = menuService.findList(dto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result).extracting(MenuBo::getId).containsExactlyInAnyOrder(1, 2, 3);
    }

    @Test
    @DisplayName("查询菜单列表 - 排除ID集合查询")
    void testFindList_ExcludeIds() {
        // Given
        MenuQueryDto dto = new MenuQueryDto();
        dto.setExcludeIds(List.of(6)); // 排除已删除的菜单

        // When
        List<MenuBo> result = menuService.findList(dto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(6);

        // 验证结果不包含排除的ID
        result.forEach(menu ->
            assertThat(menu.getId()).isNotEqualTo(6)
        );
    }

    @Test
    @DisplayName("查询菜单列表 - 按来源查询 WEB")
    void testFindList_ByMenuSource_Web() {
        MenuQueryDto dto = new MenuQueryDto();
        dto.setMenuSource(MenuSourceEnum.WEB);

        List<MenuBo> result = menuService.findList(dto);

        assertThat(result).isNotEmpty();
        assertThat(result)
                .extracting(MenuBo::getMenuSource)
                .containsOnly(MenuSourceEnum.WEB);
    }

    @Test
    @DisplayName("查询菜单列表 - 按来源查询 MOBILE")
    void testFindList_ByMenuSource_Mobile() {
        MenuQueryDto dto = new MenuQueryDto();
        dto.setMenuSource(MenuSourceEnum.MOBILE);

        List<MenuBo> result = menuService.findList(dto);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMenuSource()).isEqualTo(MenuSourceEnum.MOBILE);
        assertThat(result.get(0).getMenuKey()).isEqualTo("mobile-home");
    }

    @Test
    @DisplayName("查询菜单列表 - 空结果")
    void testFindList_EmptyResult() {
        // Given
        MenuQueryDto dto = new MenuQueryDto();
        dto.setMenuNameLike("不存在的菜单");

        // When
        List<MenuBo> result = menuService.findList(dto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("获取菜单详情 - 成功场景")
    void testGetDetail_Success() {
        // Given
        Integer menuId = 1;

        // When
        MenuDetailBo result = menuService.getDetail(menuId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(menuId);
        assertThat(result.getMenuName()).isEqualTo("系统管理");
        assertThat(result.getMenuKey()).isEqualTo("system");
        assertThat(result.getPid()).isEqualTo(0);
        assertThat(result.getSortNum()).isEqualTo(1);
        assertThat(result.getPath()).isEqualTo("/system");
        assertThat(result.getMenuSource()).isEqualTo(MenuSourceEnum.WEB);
        assertThat(result.getMenuType()).isEqualTo(MenuTypeEnum.MENU);
        assertThat(result.getIcon()).isEqualTo("setting");
        assertThat(result.getRemark()).isEqualTo("系统管理菜单");
        assertThat(result.getIsHidden()).isFalse();
        assertThat(result.getPermissionCodes()).isNotNull();
    }

    @Test
    @DisplayName("获取菜单详情 - 菜单不存在")
    void testGetDetail_NotFound() {
        // Given
        Integer nonExistentMenuId = 999999;

        // When & Then
        assertThatThrownBy(() -> menuService.getDetail(nonExistentMenuId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("获取菜单详情 - 已删除菜单")
    void testGetDetail_DeletedMenu() {
        // Given
        Integer deletedMenuId = 6; // 已删除的菜单

        // When & Then
        assertThatThrownBy(() -> menuService.getDetail(deletedMenuId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("新增菜单 - 成功场景")
    void testAdd_Success() {
        // Given
        MenuCreateDto dto = new MenuCreateDto();
        dto.setMenuName("新菜单");
        dto.setMenuKey("new-menu");
        dto.setPid(1);
        dto.setSortNum(10);
        dto.setPath("/system/new");
        dto.setMenuSource(MenuSourceEnum.WEB);
        dto.setMenuType(MenuTypeEnum.MENU);
        dto.setIcon("new");
        dto.setRemark("新增菜单测试");
        dto.setIsHidden(false);
        dto.setPermissionCodes(List.of("menu:add", "menu:edit", "menu:delete"));

        // When
        Integer menuId = menuService.add(dto);

        // Then
        assertThat(menuId).isNotNull();
        assertThat(menuId).isGreaterThan(0);

        // 验证菜单已创建
        MenuDetailBo createdMenu = menuService.getDetail(menuId);
        assertThat(createdMenu.getMenuName()).isEqualTo("新菜单");
        assertThat(createdMenu.getMenuKey()).isEqualTo("new-menu");
        assertThat(createdMenu.getPid()).isEqualTo(1);
        assertThat(createdMenu.getSortNum()).isEqualTo(10);
        assertThat(createdMenu.getPath()).isEqualTo("/system/new");
        assertThat(createdMenu.getMenuSource()).isEqualTo(MenuSourceEnum.WEB);
        assertThat(createdMenu.getMenuType()).isEqualTo(MenuTypeEnum.MENU);
        assertThat(createdMenu.getIcon()).isEqualTo("new");
        assertThat(createdMenu.getRemark()).isEqualTo("新增菜单测试");
        assertThat(createdMenu.getIsHidden()).isFalse();

        // 验证权限代码
        assertThat(createdMenu.getPermissionCodes()).isNotNull();
        assertThat(createdMenu.getPermissionCodes()).containsExactlyInAnyOrder("menu:add", "menu:edit", "menu:delete");
    }

    @Test
    @DisplayName("新增菜单 - 菜单标识已存在")
    void testAdd_MenuKeyExists() {
        // Given
        MenuCreateDto dto = new MenuCreateDto();
        dto.setMenuName("重复菜单");
        dto.setMenuKey("system"); // 已存在的菜单标识
        dto.setPid(0);
        dto.setSortNum(1);
        dto.setPath("/duplicate");
        dto.setMenuSource(MenuSourceEnum.WEB);
        dto.setMenuType(MenuTypeEnum.MENU);
        dto.setIcon("duplicate");
        dto.setRemark("重复菜单测试");
        dto.setIsHidden(false);
        dto.setPermissionCodes(List.of("duplicate:read", "duplicate:write"));

        // When & Then
        assertThatThrownBy(() -> menuService.add(dto))
                .isInstanceOf(BusinessRuntimeException.class);
    }

    @Test
    @DisplayName("更新菜单信息 - 成功场景")
    void testUpdate_Success() {
        // Given
        MenuUpdateDto dto = new MenuUpdateDto();
        dto.setId(2);
        dto.setMenuName("更新后的用户管理");
        dto.setMenuKey("user-manage-updated");
        dto.setSortNum(5);
        dto.setPath("/system/user-updated");
        dto.setMenuSource(MenuSourceEnum.WEB);
        dto.setMenuType(MenuTypeEnum.MENU);
        dto.setIcon("user-updated");
        dto.setRemark("更新菜单信息测试");
        dto.setIsHidden(true);
        dto.setPermissionCodes(List.of("menu:view", "menu:update"));

        // When
        menuService.update(dto);

        // Then
        MenuDetailBo updatedMenu = menuService.getDetail(2);
        assertThat(updatedMenu.getMenuName()).isEqualTo("更新后的用户管理");
        assertThat(updatedMenu.getMenuKey()).isEqualTo("user-manage-updated");
        assertThat(updatedMenu.getSortNum()).isEqualTo(5);
        assertThat(updatedMenu.getPath()).isEqualTo("/system/user-updated");
        assertThat(updatedMenu.getIcon()).isEqualTo("user-updated");
        assertThat(updatedMenu.getRemark()).isEqualTo("更新菜单信息测试");
        assertThat(updatedMenu.getIsHidden()).isTrue();

        // 验证权限代码
        assertThat(updatedMenu.getPermissionCodes()).isNotNull();
        assertThat(updatedMenu.getPermissionCodes()).containsExactlyInAnyOrder("menu:view", "menu:update");
    }

    @Test
    @DisplayName("更新菜单权限 - 角色权限缓存应失效")
    void testUpdate_ShouldEvictRolePermissionsCache() {
        Cache cache = cacheManager.getCache(RoleServiceImpl.ROLE_PERMISSIONS_CACHE_NAME);
        if (cache != null) {
            cache.clear();
        }

        Integer roleId = 2;
        Integer menuId = 2;
        List<String> before = roleService.getRolePermissions(roleId);
        assertThat(before).contains("user:list");

        MenuDetailBo menuDetail = menuService.getDetail(menuId);
        MenuUpdateDto dto = new MenuUpdateDto();
        dto.setId(menuDetail.getId());
        dto.setMenuName(menuDetail.getMenuName());
        dto.setMenuKey(menuDetail.getMenuKey());
        dto.setSortNum(menuDetail.getSortNum());
        dto.setPath(menuDetail.getPath());
        dto.setMenuSource(menuDetail.getMenuSource());
        dto.setMenuType(menuDetail.getMenuType());
        dto.setIcon(menuDetail.getIcon());
        dto.setRemark(menuDetail.getRemark());
        dto.setIsHidden(menuDetail.getIsHidden());
        dto.setPermissionCodes(List.of("user:add"));

        menuService.update(dto);

        List<String> after = roleService.getRolePermissions(roleId);
        assertThat(after).containsExactly("user:add");
    }

    @Test
    @DisplayName("更新菜单信息 - 菜单不存在")
    void testUpdate_NotFound() {
        // Given
        MenuUpdateDto dto = new MenuUpdateDto();
        dto.setId(999999);
        dto.setMenuName("不存在的菜单");
        dto.setMenuKey("nonexistent");
        dto.setSortNum(1);
        dto.setPath("/nonexistent");
        dto.setMenuSource(MenuSourceEnum.WEB);
        dto.setMenuType(MenuTypeEnum.MENU);
        dto.setIcon("nonexistent");
        dto.setRemark("不存在的菜单");
        dto.setIsHidden(false);

        // When & Then
        assertThatThrownBy(() -> menuService.update(dto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("更新菜单信息 - 菜单标识已存在")
    void testUpdate_MenuKeyExists() {
        // Given
        MenuUpdateDto dto = new MenuUpdateDto();
        dto.setId(2);
        dto.setMenuName("用户管理");
        dto.setMenuKey("role-manage"); // 使用其他菜单的标识
        dto.setSortNum(1);
        dto.setPath("/system/user");
        dto.setMenuSource(MenuSourceEnum.WEB);
        dto.setMenuType(MenuTypeEnum.MENU);
        dto.setIcon("user");
        dto.setRemark("用户管理菜单");
        dto.setIsHidden(false);
        dto.setPermissionCodes(List.of("user:read", "user:write"));

        // When & Then
        assertThatThrownBy(() -> menuService.update(dto))
                .isInstanceOf(BusinessRuntimeException.class);
    }

    @Test
    @DisplayName("删除菜单 - 成功场景")
    void testDelete_Success() {
        // Given
        Integer menuId = 5; // 编辑用户按钮，没有子菜单

        // 验证菜单存在
        MenuDetailBo menuBeforeDelete = menuService.getDetail(menuId);
        assertThat(menuBeforeDelete).isNotNull();

        // When
        menuService.delete(menuId);

        // Then
        assertThatThrownBy(() -> menuService.getDetail(menuId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("删除菜单 - 菜单不存在")
    void testDelete_NotFound() {
        // Given
        Integer nonExistentMenuId = 999999;

        // When & Then
        assertThatThrownBy(() -> menuService.delete(nonExistentMenuId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("删除菜单 - 存在子菜单")
    void testDelete_HasChildren() {
        // Given
        Integer parentMenuId = 1; // 系统管理，有子菜单

        // When & Then
        assertThatThrownBy(() -> menuService.delete(parentMenuId))
                .isInstanceOf(BusinessRuntimeException.class);
    }

    @Test
    @DisplayName("菜单服务验证测试 - 参数校验")
    void testMenuService_ValidationTests_ShouldThrowException() {
        // 测试空参数
        assertThatThrownBy(() -> menuService.findList(null))
                .isInstanceOf(ConstraintViolationException.class);

        assertThatThrownBy(() -> menuService.getDetail(null))
                .isInstanceOf(ConstraintViolationException.class);

        assertThatThrownBy(() -> menuService.add(null))
                .isInstanceOf(ConstraintViolationException.class);

        assertThatThrownBy(() -> menuService.update(null))
                .isInstanceOf(ConstraintViolationException.class);

        assertThatThrownBy(() -> menuService.delete(null))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    @DisplayName("查询菜单列表 - 多条件组合查询")
    void testFindList_MultipleConditions() {
        // Given
        MenuQueryDto dto = new MenuQueryDto();
        dto.setPid(1); // 系统管理的子菜单

        // When
        List<MenuBo> result = menuService.findList(dto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);

        // 验证查询结果满足条件
        result.forEach(menu -> {
            assertThat(menu.getPid()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("新增菜单 - 带权限测试")
    void testAdd_WithPermissions() {
        MenuCreateDto dto = new MenuCreateDto();
        dto.setMenuName("权限测试菜单");
        dto.setMenuKey("permission-test-menu");
        dto.setPid(1);
        dto.setSortNum(20);
        dto.setPath("/system/permission-test");
        dto.setMenuSource(MenuSourceEnum.WEB);
        dto.setMenuType(MenuTypeEnum.MENU);
        dto.setIcon("permission");
        dto.setRemark("权限测试菜单");
        dto.setIsHidden(false);
        dto.setPermissionCodes(List.of("permission:read", "permission:write", "permission:delete", "permission:admin"));

        // When
        Integer menuId = menuService.add(dto);

        // Then
        assertThat(menuId).isNotNull();
        assertThat(menuId).isGreaterThan(0);

        // 验证菜单已创建并包含权限
        MenuDetailBo createdMenu = menuService.getDetail(menuId);
        assertThat(createdMenu.getMenuName()).isEqualTo("权限测试菜单");
        assertThat(createdMenu.getMenuKey()).isEqualTo("permission-test-menu");
        assertThat(createdMenu.getPermissionCodes()).isNotNull();
        assertThat(createdMenu.getPermissionCodes()).hasSize(4);
        assertThat(createdMenu.getPermissionCodes()).containsExactlyInAnyOrder(
                "permission:read", "permission:write", "permission:delete", "permission:admin");
    }

    @Test
    @DisplayName("更新菜单 - 带权限测试")
    void testUpdate_WithPermissions() {
        MenuCreateDto createDto = new MenuCreateDto();
        createDto.setMenuName("待更新菜单");
        createDto.setMenuKey("to-update-menu");
        createDto.setPid(1);
        createDto.setSortNum(30);
        createDto.setPath("/system/to-update");
        createDto.setMenuSource(MenuSourceEnum.WEB);
        createDto.setMenuType(MenuTypeEnum.MENU);
        createDto.setIcon("update");
        createDto.setRemark("待更新菜单");
        createDto.setIsHidden(false);
        createDto.setPermissionCodes(List.of("old:read", "old:write"));

        Integer menuId = menuService.add(createDto);

        // When - 更新菜单权限
        MenuUpdateDto updateDto = new MenuUpdateDto();
        updateDto.setId(menuId);
        updateDto.setMenuName("已更新菜单");
        updateDto.setMenuKey("updated-menu");
        updateDto.setSortNum(35);
        updateDto.setPath("/system/updated");
        updateDto.setMenuSource(MenuSourceEnum.WEB);
        updateDto.setMenuType(MenuTypeEnum.MENU);
        updateDto.setIcon("updated");
        updateDto.setRemark("已更新菜单");
        updateDto.setIsHidden(false);
        updateDto.setPermissionCodes(List.of("new:read", "new:write", "new:admin"));

        menuService.update(updateDto);

        // Then
        MenuDetailBo updatedMenu = menuService.getDetail(menuId);
        assertThat(updatedMenu.getMenuName()).isEqualTo("已更新菜单");
        assertThat(updatedMenu.getMenuKey()).isEqualTo("updated-menu");
        assertThat(updatedMenu.getPermissionCodes()).isNotNull();
        assertThat(updatedMenu.getPermissionCodes()).hasSize(3);
        assertThat(updatedMenu.getPermissionCodes()).containsExactlyInAnyOrder(
                "new:read", "new:write", "new:admin");
    }

    @Test
    @DisplayName("新增菜单 - 空权限列表测试")
    void testAdd_WithEmptyPermissions() {
        // Given
        MenuCreateDto dto = new MenuCreateDto();
        dto.setMenuName("无权限菜单");
        dto.setMenuKey("no-permission-menu");
        dto.setPid(1);
        dto.setSortNum(40);
        dto.setPath("/system/no-permission");
        dto.setMenuSource(MenuSourceEnum.WEB);
        dto.setMenuType(MenuTypeEnum.MENU);
        dto.setIcon("no-permission");
        dto.setRemark("无权限菜单");
        dto.setIsHidden(false);
        dto.setPermissionCodes(List.of()); // 空权限列表

        // When
        Integer menuId = menuService.add(dto);

        // Then
        assertThat(menuId).isNotNull();
        assertThat(menuId).isGreaterThan(0);

        // 验证菜单已创建且权限列表为空
        MenuDetailBo createdMenu = menuService.getDetail(menuId);
        assertThat(createdMenu.getMenuName()).isEqualTo("无权限菜单");
        assertThat(createdMenu.getMenuKey()).isEqualTo("no-permission-menu");
        assertThat(createdMenu.getPermissionCodes()).isEmpty();
    }

    @Test
    @DisplayName("更新菜单 - 清空权限测试")
    void testUpdate_ClearPermissions() {
        MenuCreateDto createDto = new MenuCreateDto();
        createDto.setMenuName("带权限菜单");
        createDto.setMenuKey("with-permission-menu");
        createDto.setPid(1);
        createDto.setSortNum(50);
        createDto.setPath("/system/with-permission");
        createDto.setMenuSource(MenuSourceEnum.WEB);
        createDto.setMenuType(MenuTypeEnum.MENU);
        createDto.setIcon("with-permission");
        createDto.setRemark("带权限菜单");
        createDto.setIsHidden(false);
        createDto.setPermissionCodes(List.of("clear:read", "clear:write"));

        Integer menuId = menuService.add(createDto);

        // When - 清空权限
        MenuUpdateDto updateDto = new MenuUpdateDto();
        updateDto.setId(menuId);
        updateDto.setMenuName("已清空权限菜单");
        updateDto.setMenuKey("cleared-permission-menu");
        updateDto.setSortNum(55);
        updateDto.setPath("/system/cleared-permission");
        updateDto.setMenuSource(MenuSourceEnum.WEB);
        updateDto.setMenuType(MenuTypeEnum.MENU);
        updateDto.setIcon("cleared");
        updateDto.setRemark("已清空权限菜单");
        updateDto.setIsHidden(false);
        updateDto.setPermissionCodes(List.of()); // 清空权限

        menuService.update(updateDto);

        // Then
        MenuDetailBo updatedMenu = menuService.getDetail(menuId);
        assertThat(updatedMenu.getMenuName()).isEqualTo("已清空权限菜单");
        assertThat(updatedMenu.getMenuKey()).isEqualTo("cleared-permission-menu");
        assertThat(updatedMenu.getPermissionCodes()).isEmpty();
    }
}
