package info.zhihui.ems.foundation.user.service.impl;

import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.foundation.user.bo.MenuBo;
import info.zhihui.ems.foundation.user.bo.MenuDetailBo;
import info.zhihui.ems.foundation.user.dto.MenuCreateDto;
import info.zhihui.ems.foundation.user.dto.MenuQueryDto;
import info.zhihui.ems.foundation.user.dto.MenuUpdateDto;
import info.zhihui.ems.foundation.user.entity.MenuAuthEntity;
import info.zhihui.ems.foundation.user.entity.MenuEntity;
import info.zhihui.ems.foundation.user.entity.MenuPathEntity;
import info.zhihui.ems.foundation.user.enums.MenuSourceEnum;
import info.zhihui.ems.foundation.user.enums.MenuTypeEnum;
import info.zhihui.ems.foundation.user.mapper.MenuMapper;
import info.zhihui.ems.foundation.user.qo.MenuQueryQo;
import info.zhihui.ems.foundation.user.repository.MenuAuthRepository;
import info.zhihui.ems.foundation.user.repository.MenuPathRepository;
import info.zhihui.ems.foundation.user.repository.MenuRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("菜单服务实现类测试")
class MenuServiceImplTest {

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private MenuAuthRepository menuAuthRepository;

    @Mock
    private MenuPathRepository menuPathRepository;

    @Mock
    private MenuMapper menuMapper;

    @InjectMocks
    private MenuServiceImpl menuService;

    private MenuEntity mockMenuEntity;
    private MenuBo mockMenuBo;
    private MenuDetailBo mockMenuDetailBo;
    private MenuCreateDto mockCreateDto;
    private MenuUpdateDto mockUpdateDto;
    private MenuQueryDto mockFindListDto;
    private MenuQueryQo mockQueryQo;

    @BeforeEach
    void setUp() {
        // 初始化 MenuEntity
        mockMenuEntity = new MenuEntity();
        mockMenuEntity.setId(1);
        mockMenuEntity.setMenuName("测试菜单");
        mockMenuEntity.setMenuKey("test_menu");
        mockMenuEntity.setPid(0);
        mockMenuEntity.setSortNum(1);
        mockMenuEntity.setPath("/test");
        mockMenuEntity.setMenuSource(MenuSourceEnum.WEB.getCode());
        mockMenuEntity.setMenuType(MenuTypeEnum.MENU.getCode());
        mockMenuEntity.setIcon("test-icon");
        mockMenuEntity.setRemark("测试备注");
        mockMenuEntity.setIsHidden(false);

        // 初始化 MenuBo
        mockMenuBo = new MenuBo();
        mockMenuBo.setId(1);
        mockMenuBo.setMenuName("测试菜单");
        mockMenuBo.setMenuKey("test_menu");
        mockMenuBo.setPid(0);
        mockMenuBo.setSortNum(1);
        mockMenuBo.setPath("/test");
        mockMenuBo.setMenuSource(MenuSourceEnum.WEB);
        mockMenuBo.setMenuType(MenuTypeEnum.MENU);
        mockMenuBo.setIcon("test-icon");
        mockMenuBo.setRemark("测试备注");
        mockMenuBo.setIsHidden(false);

        // 初始化 MenuDetailBo
        mockMenuDetailBo = new MenuDetailBo();
        mockMenuDetailBo.setId(1);
        mockMenuDetailBo.setMenuName("测试菜单");
        mockMenuDetailBo.setMenuKey("test_menu");
        mockMenuDetailBo.setPid(0);
        mockMenuDetailBo.setSortNum(1);
        mockMenuDetailBo.setPath("/test");
        mockMenuDetailBo.setMenuSource(MenuSourceEnum.WEB);
        mockMenuDetailBo.setMenuType(MenuTypeEnum.MENU);
        mockMenuDetailBo.setIcon("test-icon");
        mockMenuDetailBo.setRemark("测试备注");
        mockMenuDetailBo.setIsHidden(false);
        mockMenuDetailBo.setPermissionCodes(Arrays.asList("test:read", "test:write"));

        // 初始化 MenuCreateDto
        mockCreateDto = new MenuCreateDto();
        mockCreateDto.setMenuName("新菜单");
        mockCreateDto.setMenuKey("new_menu");
        mockCreateDto.setPid(0);
        mockCreateDto.setSortNum(1);
        mockCreateDto.setPath("/new");
        mockCreateDto.setMenuSource(MenuSourceEnum.WEB);
        mockCreateDto.setMenuType(MenuTypeEnum.MENU);
        mockCreateDto.setIcon("new-icon");
        mockCreateDto.setRemark("新菜单备注");
        mockCreateDto.setIsHidden(false);
        mockCreateDto.setPermissionCodes(Arrays.asList("new:read", "new:write"));

        // 初始化 MenuUpdateDto
        mockUpdateDto = new MenuUpdateDto();
        mockUpdateDto.setId(1);
        mockUpdateDto.setMenuName("更新菜单");
        mockUpdateDto.setMenuKey("updated_menu");
        mockUpdateDto.setSortNum(2);
        mockUpdateDto.setPath("/updated");
        mockUpdateDto.setMenuSource(MenuSourceEnum.MOBILE);
        mockUpdateDto.setMenuType(MenuTypeEnum.BUTTON);
        mockUpdateDto.setIcon("updated-icon");
        mockUpdateDto.setRemark("更新备注");
        mockUpdateDto.setIsHidden(true);
        mockUpdateDto.setPermissionCodes(List.of("updated:read"));

        // 初始化 MenuFindListDto
        mockFindListDto = new MenuQueryDto();
        mockFindListDto.setMenuNameLike("测试");
        mockFindListDto.setIds(Arrays.asList(1, 2, 3));
        mockFindListDto.setMenuSource(MenuSourceEnum.WEB);

        // 初始化 MenuQueryQo
        mockQueryQo = new MenuQueryQo();
        mockQueryQo.setMenuNameLike("测试");
        mockQueryQo.setIds(Arrays.asList(1, 2, 3));
        mockQueryQo.setMenuSource(MenuSourceEnum.WEB.getCode());
    }

    @Test
    @DisplayName("查询菜单列表 - 成功")
    void findList_Success() {
        // Given
        List<MenuEntity> entities = Collections.singletonList(mockMenuEntity);
        List<MenuBo> menuBos = Collections.singletonList(mockMenuBo);

        when(menuMapper.dtoToQo(mockFindListDto)).thenReturn(mockQueryQo);
        when(menuRepository.selectByQo(mockQueryQo)).thenReturn(entities);
        when(menuMapper.listEntityToBo(entities)).thenReturn(menuBos);

        // When
        List<MenuBo> result = menuService.findList(mockFindListDto);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("测试菜单", result.get(0).getMenuName());

        verify(menuMapper).dtoToQo(mockFindListDto);
        verify(menuRepository).selectByQo(mockQueryQo);
        verify(menuMapper).listEntityToBo(entities);
    }

    @Test
    @DisplayName("查询菜单列表 - 空结果")
    void findList_EmptyResult() {
        // Given
        when(menuMapper.dtoToQo(mockFindListDto)).thenReturn(mockQueryQo);
        when(menuRepository.selectByQo(mockQueryQo)).thenReturn(Collections.emptyList());
        when(menuMapper.listEntityToBo(Collections.emptyList())).thenReturn(Collections.emptyList());

        // When
        List<MenuBo> result = menuService.findList(mockFindListDto);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(menuMapper).dtoToQo(mockFindListDto);
        verify(menuRepository).selectByQo(mockQueryQo);
        verify(menuMapper).listEntityToBo(Collections.emptyList());
        verifyNoInteractions(menuAuthRepository);
    }

    @Test
    @DisplayName("获取菜单详情 - 成功")
    void getDetail_Success() {
        // Given
        Integer menuId = 1;

        when(menuRepository.selectById(menuId)).thenReturn(mockMenuEntity);
        when(menuMapper.entityToDetailBo(mockMenuEntity)).thenReturn(mockMenuDetailBo);
        when( menuAuthRepository.selectPermissionCodesByMenuId(menuId)).thenReturn(List.of("test:read", "test:write"));

        // When
        MenuDetailBo result = menuService.getDetail(menuId);

        // Then
        assertNotNull(result);
        assertEquals("测试菜单", result.getMenuName());
        assertEquals(Arrays.asList("test:read", "test:write"), result.getPermissionCodes());

        verify(menuRepository).selectById(menuId);
        verify(menuMapper).entityToDetailBo(mockMenuEntity);
    }

    @Test
    @DisplayName("获取菜单详情 - 菜单不存在")
    void getDetail_MenuNotFound() {
        // Given
        Integer menuId = 999;
        when(menuRepository.selectById(menuId)).thenReturn(null);

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> menuService.getDetail(menuId));
        assertEquals("菜单不存在", exception.getMessage());

        verify(menuRepository).selectById(menuId);
        verifyNoInteractions(menuMapper);
        verifyNoInteractions(menuAuthRepository);
    }

    @Test
    @DisplayName("新增菜单 - 成功")
    void add_Success() {
        // Given
        MenuEntity entityToInsert = new MenuEntity();
        entityToInsert.setId(1);

        when(menuMapper.createDtoToEntity(mockCreateDto)).thenReturn(entityToInsert);
        when(menuRepository.insert(entityToInsert)).thenReturn(1);

        // When
        Integer result = menuService.add(mockCreateDto);

        // Then
        assertEquals(1, result);

        verify(menuMapper).createDtoToEntity(mockCreateDto);
        verify(menuRepository).insert(entityToInsert);
        verify(menuPathRepository).insert(new MenuPathEntity().setAncestorId(1).setDescendantId(1).setDepth(0));
        verify(menuAuthRepository).insert(anyList());
    }

    @Test
    @DisplayName("新增菜单 - 菜单标识已存在")
    void add_DuplicateKey() {
        // Given
        MenuEntity entityToInsert = new MenuEntity();
        when(menuMapper.createDtoToEntity(mockCreateDto)).thenReturn(entityToInsert);
        when(menuRepository.insert(entityToInsert)).thenThrow(new DuplicateKeyException("Duplicate key"));

        // When & Then
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
            () -> menuService.add(mockCreateDto));
        assertEquals("菜单标识已存在", exception.getMessage());

        verify(menuMapper).createDtoToEntity(mockCreateDto);
        verify(menuRepository).insert(entityToInsert);
        verifyNoInteractions(menuPathRepository);
        verifyNoInteractions(menuAuthRepository);
    }

    @Test
    @DisplayName("更新菜单 - 成功")
    void update_Success() {
        // Given
        MenuEntity entityToUpdate = new MenuEntity();
        when(menuRepository.selectById(mockUpdateDto.getId())).thenReturn(mockMenuEntity);
        when(menuMapper.updateDtoToEntity(mockUpdateDto)).thenReturn(entityToUpdate);
        when(menuRepository.updateById(entityToUpdate)).thenReturn(1);

        // When
        menuService.update(mockUpdateDto);

        // Then
        verify(menuRepository).selectById(mockUpdateDto.getId());
        verify(menuMapper).updateDtoToEntity(mockUpdateDto);
        verify(menuRepository).updateById(entityToUpdate);
        verify(menuAuthRepository).deleteByMenuId(mockUpdateDto.getId());
        verify(menuAuthRepository).insert(anyList());
    }

    @Test
    @DisplayName("更新菜单 - 菜单不存在")
    void update_MenuNotFound() {
        // Given
        when(menuRepository.selectById(mockUpdateDto.getId())).thenReturn(null);

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> menuService.update(mockUpdateDto));
        assertEquals("菜单不存在", exception.getMessage());

        verify(menuRepository).selectById(mockUpdateDto.getId());
        verifyNoMoreInteractions(menuRepository);
        verifyNoInteractions(menuMapper);
        verifyNoInteractions(menuAuthRepository);
    }

    @Test
    @DisplayName("更新菜单 - 菜单标识已存在")
    void update_DuplicateKey() {
        // Given
        MenuEntity entityToUpdate = new MenuEntity();
        when(menuRepository.selectById(mockUpdateDto.getId())).thenReturn(mockMenuEntity);
        when(menuMapper.updateDtoToEntity(mockUpdateDto)).thenReturn(entityToUpdate);
        when(menuRepository.updateById(entityToUpdate)).thenThrow(new DuplicateKeyException("Duplicate key"));

        // When & Then
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
            () -> menuService.update(mockUpdateDto));
        assertEquals("菜单标识已存在", exception.getMessage());

        verify(menuRepository).selectById(mockUpdateDto.getId());
        verify(menuMapper).updateDtoToEntity(mockUpdateDto);
        verify(menuRepository).updateById(entityToUpdate);
        verifyNoMoreInteractions(menuAuthRepository);
    }

    @Test
    @DisplayName("删除菜单 - 成功")
    void delete_Success() {
        // Given
        Integer menuId = 1;
        when(menuRepository.selectById(menuId)).thenReturn(mockMenuEntity);
        when(menuRepository.selectByQo(any(MenuQueryQo.class))).thenReturn(Collections.emptyList());

        // When
        menuService.delete(menuId);

        // Then
        verify(menuRepository).selectById(menuId);
        verify(menuRepository).selectByQo(any(MenuQueryQo.class));
        verify(menuRepository).deleteById(menuId);
        verify(menuPathRepository).deleteSubtreePaths(menuId);
        verify(menuAuthRepository).deleteByMenuId(menuId);
    }

    @Test
    @DisplayName("删除菜单 - 菜单不存在")
    void delete_MenuNotFound() {
        // Given
        Integer menuId = 999;
        when(menuRepository.selectById(menuId)).thenReturn(null);

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> menuService.delete(menuId));
        assertEquals("菜单不存在", exception.getMessage());

        verify(menuRepository).selectById(menuId);
        verifyNoMoreInteractions(menuRepository);
        verifyNoInteractions(menuPathRepository);
        verifyNoInteractions(menuAuthRepository);
    }

    @Test
    @DisplayName("删除菜单 - 存在子菜单")
    void delete_HasChildren() {
        // Given
        Integer menuId = 1;
        List<MenuEntity> children = Collections.singletonList(mockMenuEntity);
        when(menuRepository.selectById(menuId)).thenReturn(mockMenuEntity);
        when(menuRepository.selectByQo(any(MenuQueryQo.class))).thenReturn(children);

        // When & Then
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
            () -> menuService.delete(menuId));
        assertEquals("存在子菜单，无法删除", exception.getMessage());

        verify(menuRepository).selectById(menuId);
        verify(menuRepository).selectByQo(any(MenuQueryQo.class));
        verify(menuRepository, never()).deleteById(menuId);
        verifyNoInteractions(menuPathRepository);
        verifyNoInteractions(menuAuthRepository);
    }

}
