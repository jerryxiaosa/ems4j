package info.zhihui.ems.foundation.user.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.foundation.user.bo.RoleBo;
import info.zhihui.ems.foundation.user.bo.RoleSimpleBo;
import info.zhihui.ems.foundation.user.bo.UserBo;
import info.zhihui.ems.foundation.user.dto.RoleQueryDto;
import info.zhihui.ems.foundation.user.dto.UserCreateDto;
import info.zhihui.ems.foundation.user.dto.UserQueryDto;
import info.zhihui.ems.foundation.user.dto.UserUpdateDto;
import info.zhihui.ems.foundation.user.dto.UserUpdatePasswordDto;
import info.zhihui.ems.foundation.user.entity.UserEntity;
import info.zhihui.ems.foundation.user.entity.UserRoleEntity;
import info.zhihui.ems.foundation.user.event.UserProfileUpdatedEvent;
import info.zhihui.ems.foundation.user.enums.CertificatesTypeEnum;
import info.zhihui.ems.foundation.user.enums.UserGenderEnum;
import info.zhihui.ems.foundation.user.mapper.UserMapper;
import info.zhihui.ems.foundation.user.qo.UserQueryQo;
import info.zhihui.ems.foundation.user.repository.UserRepository;
import info.zhihui.ems.foundation.user.repository.UserRoleRepository;
import info.zhihui.ems.foundation.user.service.PasswordService;
import info.zhihui.ems.foundation.user.service.RoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DuplicateKeyException;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * UserServiceImpl 单元测试类
 *
 * @author jerryxiaosa
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("用户服务实现类测试")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private RoleService roleService;

    @Mock
    private PasswordService passwordService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private UserServiceImpl userService;

    // 测试数据
    private UserEntity mockEntity;
    private UserBo mockBo;
    private UserCreateDto mockCreateDto;
    private UserUpdateDto mockUpdateDto;
    private UserUpdatePasswordDto mockUpdatePasswordDto;
    private UserQueryDto mockQueryDto;
    private UserQueryQo mockQueryQo;
    private PageParam mockPageParam;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        mockEntity = new UserEntity()
                .setId(1)
                .setUserName("testuser")
                .setPassword("hashedpassword")
                .setRealName("测试用户")
                .setUserPhone("13800138000")
                .setUserGender(1)
                .setCertificatesType(1)
                .setCertificatesNo("110101199001011234")
                .setRemark("测试备注")
                .setOrganizationId(1001);

        mockBo = new UserBo()
                .setId(1)
                .setUserName("testuser")
                .setRealName("测试用户")
                .setUserPhone("13800138000")
                .setUserGender(UserGenderEnum.MALE)
                .setCertificatesType(CertificatesTypeEnum.ID_CARD)
                .setCertificatesNo("110101199001011234")
                .setRemark("测试备注")
                .setOrganizationId(1001)
                .setRoles(List.of(
                        new RoleSimpleBo().setId(1).setRoleName("管理员"),
                        new RoleSimpleBo().setId(2).setRoleName("普通用户")
                ));

        mockCreateDto = new UserCreateDto()
                .setUserName("newuser")
                .setPassword("password@123")
                .setRealName("新用户")
                .setUserPhone("13900139000")
                .setUserGender(UserGenderEnum.FEMALE)
                .setCertificatesType(CertificatesTypeEnum.ID_CARD)
                .setCertificatesNo("110101199001011235")
                .setRemark("新用户备注")
                .setOrganizationId(1002)
                .setRoleIds(List.of(2, 3));

        mockUpdateDto = new UserUpdateDto()
                .setId(1)
                .setRealName("更新用户")
                .setUserPhone("13700137000")
                .setUserGender(UserGenderEnum.MALE)
                .setCertificatesType(CertificatesTypeEnum.PASSPORT)
                .setCertificatesNo("E12345678")
                .setRemark("更新备注")
                .setOrganizationId(1003)
                .setRoleIds(List.of(1, 3));

        mockUpdatePasswordDto = new UserUpdatePasswordDto()
                .setId(1)
                .setOldPassword("hashedpassword")
                .setNewPassword("newhashedpassworD123");

        mockQueryDto = new UserQueryDto()
                .setUserNameLike("test")
                .setRealNameLike("测试")
                .setUserPhone("13800138000")
                .setOrganizationId(1001)
                .setIds(List.of(1, 2, 3));

        mockQueryQo = new UserQueryQo()
                .setUserNameLike("test")
                .setRealNameLike("测试")
                .setUserPhone("13800138000")
                .setOrganizationId(1001)
                .setIds(List.of(1, 2, 3));

        mockPageParam = new PageParam()
                .setPageNum(1)
                .setPageSize(10);
    }

    // ==================== findUserPage方法测试 ====================

    @Test
    @DisplayName("findUserPage - 成功分页查询用户")
    void testFindUserPage_Success() {
        // Given
        when(userMapper.queryDtoToQo(mockQueryDto)).thenReturn(mockQueryQo);

        PageResult<UserBo> expectedResult = new PageResult<>();
        expectedResult.setList(List.of(mockBo));
        expectedResult.setTotal(1L);
        when(userMapper.pageEntityToPageBo(any())).thenReturn(expectedResult);

        // Mock user role relationships
        List<UserRoleEntity> mockUserRoles = List.of(
                new UserRoleEntity().setUserId(1).setRoleId(1),
                new UserRoleEntity().setUserId(1).setRoleId(2)
        );
        when(userRoleRepository.selectByUserIds(any())).thenReturn(mockUserRoles);

        // Mock user role relationships
        List<UserRoleEntity> mockUserRoles2 = List.of(
                new UserRoleEntity().setUserId(1).setRoleId(1),
                new UserRoleEntity().setUserId(1).setRoleId(2)
        );
        when(userRoleRepository.selectByUserIds(any())).thenReturn(mockUserRoles2);

        // Mock role service for fillUserRoles
        List<RoleBo> mockRoles = List.of(
                new RoleBo().setId(1).setRoleName("管理员"),
                new RoleBo().setId(2).setRoleName("普通用户")
        );
        when(roleService.findList(any(RoleQueryDto.class))).thenReturn(mockRoles);
        when(userMapper.listRoleBoToSimpleBo(mockRoles)).thenReturn(List.of(
                new RoleSimpleBo().setId(1).setRoleName("管理员"),
                new RoleSimpleBo().setId(2).setRoleName("普通用户")
        ));

        // When
        PageResult<UserBo> result = userService.findUserPage(mockQueryDto, mockPageParam);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getList()).hasSize(1);
        assertThat(result.getList().get(0).getId()).isEqualTo(1);

        verify(userMapper).queryDtoToQo(mockQueryDto);
        verify(userMapper).pageEntityToPageBo(any());
    }

    @Test
    @DisplayName("findUserPage - 分页查询结果为空")
    void testFindUserPage_Empty() {
        // Given
        when(userMapper.queryDtoToQo(mockQueryDto)).thenReturn(mockQueryQo);
        PageResult<UserBo> expectedResult = new PageResult<>();
        expectedResult.setList(List.of());
        when(userMapper.pageEntityToPageBo(any())).thenReturn(expectedResult);

        // When
        PageResult<UserBo> result = userService.findUserPage(mockQueryDto, mockPageParam);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getList()).isEmpty();

        verify(userMapper).queryDtoToQo(mockQueryDto);
        verify(userMapper).pageEntityToPageBo(any());
    }

    // ==================== findUserList方法测试 ====================

    @Test
    @DisplayName("findUserList - 成功查询用户列表")
    void testFindUserList_Success() {
        // Given
        List<UserEntity> entityList = List.of(mockEntity);
        List<UserBo> boList = List.of(mockBo);

        when(userMapper.queryDtoToQo(mockQueryDto)).thenReturn(mockQueryQo);
        when(userRepository.selectByQo(mockQueryQo)).thenReturn(entityList);
        when(userMapper.listEntityToBo(entityList)).thenReturn(boList);

        // When
        List<UserBo> result = userService.findUserList(mockQueryDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1);
        assertThat(result.get(0).getUserName()).isEqualTo("testuser");

        verify(userMapper).queryDtoToQo(mockQueryDto);
        verify(userRepository).selectByQo(mockQueryQo);
        verify(userMapper).listEntityToBo(entityList);
    }

    @Test
    @DisplayName("findUserList - 查询结果为空")
    void testFindUserList_Empty() {
        // Given
        when(userMapper.queryDtoToQo(mockQueryDto)).thenReturn(mockQueryQo);
        when(userRepository.selectByQo(mockQueryQo)).thenReturn(Collections.emptyList());
        when(userMapper.listEntityToBo(Collections.emptyList())).thenReturn(Collections.emptyList());

        // When
        List<UserBo> result = userService.findUserList(mockQueryDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(userMapper).queryDtoToQo(mockQueryDto);
        verify(userRepository).selectByQo(mockQueryQo);
        verify(userMapper).listEntityToBo(Collections.emptyList());
    }

    // ==================== getUserInfo 测试 ====================

    @Test
    @DisplayName("getUserInfo - 成功获取用户详情")
    void testGetUserInfo_Success() {
        // Given
        when(userRepository.selectById(1)).thenReturn(mockEntity);
        when(userMapper.entityToBo(mockEntity)).thenReturn(mockBo);

        // When
        UserBo result = userService.getUserInfo(1);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getUserName()).isEqualTo("testuser");
        assertThat(result.getRealName()).isEqualTo("测试用户");

        verify(userRepository).selectById(1);
        verify(userMapper).entityToBo(mockEntity);
    }

    @Test
    @DisplayName("getUserInfo - 用户不存在")
    void testGetUserInfo_NotFound() {
        // Given
        when(userRepository.selectById(999)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> userService.getUserInfo(999))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("用户不存在");

        verify(userRepository).selectById(999);
        verify(userMapper, never()).entityToBo(any());
    }

    // ==================== add 测试 ====================

    @Test
    @DisplayName("add - 成功创建用户")
    void testAdd_Success() {
        // Given
        UserEntity newEntity = new UserEntity()
                .setId(2)
                .setUserName("newuser")
                .setRealName("新用户")
                .setUserPhone("13900139000")
                .setUserGender(2)
                .setCertificatesType(1)
                .setCertificatesNo("110101199001011235")
                .setRemark("新用户备注")
                .setOrganizationId(1002);

        when(userMapper.createDtoToEntity(mockCreateDto)).thenReturn(newEntity);
        when(userRepository.insert(newEntity)).thenReturn(1);

        // Mock role validation
        List<RoleBo> mockRoles = List.of(
                new RoleBo().setId(2).setRoleName("普通用户"),
                new RoleBo().setId(3).setRoleName("访客")
        );
        when(roleService.findList(any(RoleQueryDto.class))).thenReturn(mockRoles);

        // When
        Integer result = userService.add(mockCreateDto);

        // Then
        assertThat(result).isEqualTo(2);

        verify(userMapper).createDtoToEntity(mockCreateDto);
        verify(userRepository).insert(newEntity);
        verify(roleService).findList(any(RoleQueryDto.class));
        verify(userRoleRepository).insert(ArgumentMatchers.<List<UserRoleEntity>>any());
    }

    @Test
    @DisplayName("add - 用户名已存在")
    void testAdd_UsernameExists() {
        // Given
        UserEntity newEntity = new UserEntity()
                .setUserName("existinguser")
                .setPassword("password@123");

        when(roleService.findList(any(RoleQueryDto.class))).thenReturn(List.of(new RoleBo().setRoleKey("aaa"), new RoleBo().setRoleKey("bbb")));
        when(userMapper.createDtoToEntity(mockCreateDto)).thenReturn(newEntity);
        when(userRepository.insert(newEntity)).thenThrow(new DuplicateKeyException("Duplicate entry user name"));

        // When & Then
        assertThatThrownBy(() -> userService.add(mockCreateDto))
                .isInstanceOf(BusinessRuntimeException.class)
                .hasMessage("用户名或手机号已存在");

        verify(userMapper).createDtoToEntity(mockCreateDto);
        verify(userRepository).insert(newEntity);
    }

    // ==================== update 测试 ====================

    @Test
    @DisplayName("update - 成功更新用户")
    void testUpdate_Success() {
        // Given
        UserEntity updateEntity = new UserEntity()
                .setId(1)
                .setRealName("更新用户")
                .setUserPhone("13700137000")
                .setUserGender(1)
                .setCertificatesType(2)
                .setCertificatesNo("E12345678")
                .setRemark("更新备注")
                .setOrganizationId(1003);

        when(userMapper.updateDtoToEntity(mockUpdateDto)).thenReturn(updateEntity);
        when(userRepository.updateById(updateEntity)).thenReturn(1);
        when(userRepository.selectById(1)).thenReturn(mockEntity); // 添加这行来模拟用户存在

        // Mock role validation
        List<RoleBo> mockRoles = List.of(
                new RoleBo().setId(1).setRoleName("管理员"),
                new RoleBo().setId(3).setRoleName("访客")
        );
        when(roleService.findList(any(RoleQueryDto.class))).thenReturn(mockRoles);

        // When
        userService.update(mockUpdateDto);

        // Then
        verify(userMapper).updateDtoToEntity(mockUpdateDto);
        verify(userRepository).updateById(updateEntity);
        verify(roleService).findList(any(RoleQueryDto.class));
        verify(userRoleRepository).deleteByUserId(1);
        verify(userRoleRepository).insert(ArgumentMatchers.<List<UserRoleEntity>>any());
        verify(applicationEventPublisher).publishEvent(any(UserProfileUpdatedEvent.class));
    }

    @Test
    @DisplayName("update - roleIds为null不变更角色")
    void testUpdate_RoleIdsNull_ShouldSkipRoleChanges() {
        UserUpdateDto dto = new UserUpdateDto();
        dto.setId(1);
        dto.setRoleIds(null);

        UserEntity updateEntity = new UserEntity().setId(1);

        when(userRepository.selectById(1)).thenReturn(mockEntity);
        when(userMapper.updateDtoToEntity(dto)).thenReturn(updateEntity);
        when(userRepository.updateById(updateEntity)).thenReturn(1);

        userService.update(dto);

        verify(userRepository, times(2)).selectById(1);
        verify(userRepository).updateById(updateEntity);
        verify(roleService, never()).findList(any(RoleQueryDto.class));
        verify(userRoleRepository, never()).deleteByUserId(anyInt());
        verify(userRoleRepository, never()).insert(anyList());
        verify(applicationEventPublisher).publishEvent(any(UserProfileUpdatedEvent.class));
    }

    @Test
    @DisplayName("update - roleIds为空清空角色")
    void testUpdate_EmptyRoleIds_ShouldClearRoles() {
        UserUpdateDto dto = new UserUpdateDto();
        dto.setId(1);
        dto.setRoleIds(Collections.emptyList());

        UserEntity updateEntity = new UserEntity().setId(1);

        when(userRepository.selectById(1)).thenReturn(mockEntity);
        when(userMapper.updateDtoToEntity(dto)).thenReturn(updateEntity);
        when(userRepository.updateById(updateEntity)).thenReturn(1);

        userService.update(dto);

        verify(userRepository,times(2)).selectById(1);
        verify(userRepository).updateById(updateEntity);
        verify(userRoleRepository).deleteByUserId(1);
        verify(userRoleRepository, never()).insert(anyList());
        verify(applicationEventPublisher).publishEvent(any(UserProfileUpdatedEvent.class));
    }

    // ==================== delete 测试 ====================

    @Test
    @DisplayName("delete - 成功删除用户")
    void testDelete_Success() {
        // Given
        when(userRepository.deleteById(1)).thenReturn(1);

        try (MockedStatic<StpUtil> stpMock = mockStatic(StpUtil.class)) {
            // When
            userService.delete(1);

            // Then
            stpMock.verify(() -> StpUtil.logout(1));
            verify(userRoleRepository).deleteByUserId(1);
            verify(userRepository).deleteById(1);
        }
    }

    // ==================== updatePassword 测试 ====================

    @Test
    @DisplayName("updatePassword - 成功更新密码")
    void testUpdatePassword_Success() {
        // Given
        when(userRepository.selectById(1)).thenReturn(mockEntity);
        when(userRepository.updateById(any(UserEntity.class))).thenReturn(1);
        when(passwordService.matchesPassword(mockUpdatePasswordDto.getOldPassword(), mockEntity.getPassword())).thenReturn(true);
        when(passwordService.encode(mockUpdatePasswordDto.getNewPassword())).thenReturn("123abc");

        // When
        userService.updatePassword(mockUpdatePasswordDto);

        // Then
        verify(userRepository).selectById(1);
        verify(userRepository).updateById(argThat((UserEntity entity) ->
                entity.getId().equals(1) && entity.getPassword().equals("123abc")));
    }

    @Test
    @DisplayName("updatePassword - 用户不存在")
    void testUpdatePassword_UserNotFound() {
        // Given
        when(userRepository.selectById(999)).thenReturn(null);

        UserUpdatePasswordDto dto = new UserUpdatePasswordDto()
                .setId(999)
                .setOldPassword("oldpassword")
                .setNewPassword("newpassword");

        // When & Then
        assertThatThrownBy(() -> userService.updatePassword(dto))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("用户不存在");

        verify(userRepository).selectById(999);
        verify(userRepository, never()).updateById(any(UserEntity.class));
    }

    @Test
    @DisplayName("updatePassword - 原密码不正确")
    void testUpdatePassword_WrongOldPassword() {
        // Given
        when(userRepository.selectById(1)).thenReturn(mockEntity);
        when(passwordService.matchesPassword("wrongpassword", "hashedpassword")).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> userService.updatePassword(mockUpdatePasswordDto.setOldPassword("wrongpassword")))
                .isInstanceOf(BusinessRuntimeException.class)
                .hasMessage("原密码不正确");

        verify(userRepository).selectById(1);
        verify(passwordService).matchesPassword("wrongpassword", "hashedpassword");
        verify(passwordService, never()).encode(anyString());
        verify(userRepository, never()).updateById(any(UserEntity.class));
    }

    // ==================== fillUserRoles 测试 ====================

    @Test
    @DisplayName("fillUserRoles - 排除禁用角色")
    void testFillUserRoles_ExcludeDisabledRoles() {
        // Given
        List<UserBo> userBos = List.of(
                new UserBo().setId(1).setUserName("user1"),
                new UserBo().setId(2).setUserName("user2")
        );

        // Mock user role relationships - user1 has 3 roles, user2 has 2 roles
        List<UserRoleEntity> mockUserRoles = List.of(
                new UserRoleEntity().setUserId(1).setRoleId(1),
                new UserRoleEntity().setUserId(1).setRoleId(2),
                new UserRoleEntity().setUserId(1).setRoleId(3),
                new UserRoleEntity().setUserId(2).setRoleId(2),
                new UserRoleEntity().setUserId(2).setRoleId(4)
        );
        when(userRoleRepository.selectByUserIds(List.of(1, 2))).thenReturn(mockUserRoles);

        // Mock role service - role2 is disabled
        List<RoleBo> mockRoles = List.of(
                new RoleBo().setId(1).setRoleName("管理员").setIsDisabled(false),
                new RoleBo().setId(2).setRoleName("禁用角色").setIsDisabled(true),  // 禁用角色
                new RoleBo().setId(3).setRoleName("普通用户").setIsDisabled(false),
                new RoleBo().setId(4).setRoleName("访客").setIsDisabled(false)
        );
        when(roleService.findList(any(RoleQueryDto.class))).thenReturn(mockRoles);

        // Mock mapper conversion
        when(userMapper.listRoleBoToSimpleBo(mockRoles)).thenReturn(List.of(
                new RoleSimpleBo().setId(1).setRoleName("管理员").setIsDisabled(false),
                new RoleSimpleBo().setId(2).setRoleName("禁用角色").setIsDisabled(true),
                new RoleSimpleBo().setId(3).setRoleName("普通用户").setIsDisabled(false),
                new RoleSimpleBo().setId(4).setRoleName("访客").setIsDisabled(false)
        ));

        // When - call private method through reflection or use a public method that calls it
        // Since fillUserRoles is private, we need to test it through a public method
        // Let's use findUserList which calls fillUserRoles internally
        when(userRepository.selectByQo(any())).thenReturn(List.of(
                mockEntity.setId(1),
                new UserEntity().setId(2).setUserName("user2")
        ));
        when(userMapper.listEntityToBo(any())).thenReturn(userBos);
        when(userMapper.queryDtoToQo(any())).thenReturn(mockQueryQo);

        List<UserBo> result = userService.findUserList(mockQueryDto);

        // Then
        assertThat(result).hasSize(2);

        // User1 should have 2 roles (role1 and role3, excluding disabled role2)
        UserBo user1 = result.stream().filter(u -> u.getId().equals(1)).findFirst().orElse(null);
        assertThat(user1).isNotNull();
        assertThat(user1.getRoles()).hasSize(2);
        assertThat(user1.getRoles()).extracting(RoleSimpleBo::getId).containsExactlyInAnyOrder(1, 3);
        assertThat(user1.getRoles()).extracting(RoleSimpleBo::getRoleName).containsExactlyInAnyOrder("管理员", "普通用户");

        // User2 should have 1 role (role4, excluding disabled role2)
        UserBo user2 = result.stream().filter(u -> u.getId().equals(2)).findFirst().orElse(null);
        assertThat(user2).isNotNull();
        assertThat(user2.getRoles()).hasSize(1);
        assertThat(user2.getRoles()).extracting(RoleSimpleBo::getId).containsExactly(4);
        assertThat(user2.getRoles()).extracting(RoleSimpleBo::getRoleName).containsExactly("访客");

        verify(userRoleRepository).selectByUserIds(List.of(1, 2));
        verify(roleService).findList(any(RoleQueryDto.class));
        verify(userMapper).listRoleBoToSimpleBo(mockRoles);
    }

    @Test
    @DisplayName("fillUserRoles - 包含启用角色")
    void testFillUserRoles_IncludeEnabledRoles() {
        // Given
        List<UserBo> userBos = List.of(
                new UserBo().setId(1).setUserName("user1")
        );

        // Mock user role relationships
        List<UserRoleEntity> mockUserRoles = List.of(
                new UserRoleEntity().setUserId(1).setRoleId(1),
                new UserRoleEntity().setUserId(1).setRoleId(2),
                new UserRoleEntity().setUserId(1).setRoleId(3)
        );
        when(userRoleRepository.selectByUserIds(List.of(1))).thenReturn(mockUserRoles);

        // Mock role service - all roles are enabled
        List<RoleBo> mockRoles = List.of(
                new RoleBo().setId(1).setRoleName("管理员").setIsDisabled(false),
                new RoleBo().setId(2).setRoleName("普通用户").setIsDisabled(false),
                new RoleBo().setId(3).setRoleName("访客").setIsDisabled(null)  // null means enabled
        );
        when(roleService.findList(any(RoleQueryDto.class))).thenReturn(mockRoles);

        // Mock mapper conversion
        when(userMapper.listRoleBoToSimpleBo(mockRoles)).thenReturn(List.of(
                new RoleSimpleBo().setId(1).setRoleName("管理员").setIsDisabled(false),
                new RoleSimpleBo().setId(2).setRoleName("普通用户").setIsDisabled(false),
                new RoleSimpleBo().setId(3).setRoleName("访客").setIsDisabled(null)
        ));

        // When - test through findUserList
        when(userRepository.selectByQo(any())).thenReturn(List.of(mockEntity.setId(1)));
        when(userMapper.listEntityToBo(any())).thenReturn(userBos);
        when(userMapper.queryDtoToQo(any())).thenReturn(mockQueryQo);

        List<UserBo> result = userService.findUserList(mockQueryDto);

        // Then
        assertThat(result).hasSize(1);

        UserBo user1 = result.get(0);
        assertThat(user1.getRoles()).hasSize(3);
        assertThat(user1.getRoles()).extracting(RoleSimpleBo::getId).containsExactlyInAnyOrder(1, 2, 3);
        assertThat(user1.getRoles()).extracting(RoleSimpleBo::getRoleName).containsExactlyInAnyOrder("管理员", "普通用户", "访客");

        verify(userRoleRepository).selectByUserIds(List.of(1));
        verify(roleService).findList(any(RoleQueryDto.class));
        verify(userMapper).listRoleBoToSimpleBo(mockRoles);
    }

    @Test
    @DisplayName("hasPermission - 角色key包含超管但不应视为超管")
    void testHasPermission_SuperAdminContains_ShouldNotGrant() {
        UserBo userBo = new UserBo().setId(1).setRoles(List.of(
                new RoleSimpleBo().setId(1).setRoleKey("super_admin_ext")
        ));

        when(userRepository.selectById(1)).thenReturn(mockEntity);
        when(userMapper.entityToBo(mockEntity)).thenReturn(userBo);
        when(userRoleRepository.selectByUserIds(List.of(1))).thenReturn(List.of(
                new UserRoleEntity().setUserId(1).setRoleId(1)
        ));

        RoleBo roleBo = new RoleBo().setId(1).setRoleKey("super_admin_ext");
        when(roleService.findList(any(RoleQueryDto.class))).thenReturn(List.of(roleBo));
        when(userMapper.listRoleBoToSimpleBo(List.of(roleBo))).thenReturn(List.of(
                new RoleSimpleBo().setId(1).setRoleKey("super_admin_ext")
        ));

        when(roleService.existsPermission(anySet(), anyString())).thenReturn(false);

        boolean result = userService.hasPermission(1, "user:read");

        assertThat(result).isFalse();
    }

}
