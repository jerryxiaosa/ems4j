package info.zhihui.ems.foundation.user;

import cn.dev33.satoken.context.mock.SaTokenContextMockUtil;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.StpUtil;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.foundation.user.bo.UserBo;
import info.zhihui.ems.foundation.user.constants.LoginConstant;
import info.zhihui.ems.components.redis.utils.RedisUtil;
import info.zhihui.ems.foundation.user.entity.UserEntity;
import info.zhihui.ems.foundation.user.entity.UserRoleEntity;
import info.zhihui.ems.foundation.user.dto.UserCreateDto;
import info.zhihui.ems.foundation.user.dto.UserQueryDto;
import info.zhihui.ems.foundation.user.dto.UserUpdateDto;
import info.zhihui.ems.foundation.user.dto.UserUpdatePasswordDto;
import info.zhihui.ems.foundation.user.dto.UserResetPasswordDto;
import info.zhihui.ems.foundation.user.enums.CertificatesTypeEnum;
import info.zhihui.ems.foundation.user.enums.UserGenderEnum;
import info.zhihui.ems.foundation.user.repository.UserRepository;
import info.zhihui.ems.foundation.user.repository.UserRoleRepository;
import info.zhihui.ems.foundation.user.service.PasswordService;
import info.zhihui.ems.foundation.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * UserService 集成测试
 *
 * @author jerryxiaosa
 */
@SpringBootTest
@ActiveProfiles("integrationtest")
@Transactional
@Rollback
@DisplayName("用户服务集成测试")
class UserServiceImplIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private PasswordService passwordService;

    @Test
    @DisplayName("分页查询用户列表 - 成功场景")
    void testFindUserPage_Success() {
        // Given
        UserQueryDto queryDto = new UserQueryDto();
        PageParam pageParam = new PageParam();

        // When
        PageResult<UserBo> result = userService.findUserPage(queryDto, pageParam);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotal()).isEqualTo(2);
        assertThat(result.getList()).isNotEmpty();
        assertThat(result.getList().size()).isLessThanOrEqualTo(10);

        // 验证返回的用户数据
        UserBo firstUser = result.getList().get(0);
        assertThat(firstUser.getId()).isNotNull();
        assertThat(firstUser.getUserName()).isNotBlank();
        assertThat(firstUser.getRealName()).isNotBlank();
    }

    @Test
    @DisplayName("分页查询用户列表 - 按用户名模糊查询")
    void testFindUserPage_ByUserName() {
        // Given
        UserQueryDto queryDto = new UserQueryDto();
        queryDto.setUserNameLike("testuser");
        PageParam pageParam = new PageParam();

        // When
        PageResult<UserBo> result = userService.findUserPage(queryDto, pageParam);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotal()).isEqualTo(2);
        assertThat(result.getList()).isNotEmpty();

        // 验证查询结果包含指定用户名
        result.getList().forEach(user ->
            assertThat(user.getUserName()).containsIgnoringCase("testuser")
        );
    }

    @Test
    @DisplayName("分页查询用户列表 - 按真实姓名模糊查询")
    void testFindUserPage_ByRealName() {
        // Given
        UserQueryDto queryDto = new UserQueryDto();
        queryDto.setRealNameLike("测试用户");
        PageParam pageParam = new PageParam();

        // When
        PageResult<UserBo> result = userService.findUserPage(queryDto, pageParam);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotal()).isEqualTo(2);
        assertThat(result.getList()).isNotEmpty();

        // 验证查询结果包含指定真实姓名
        result.getList().forEach(user ->
            assertThat(user.getRealName()).contains("测试用户")
        );
    }

    @Test
    @DisplayName("分页查询用户列表 - 按手机号查询")
    void testFindUserPage_ByPhone() {
        // Given
        UserQueryDto queryDto = new UserQueryDto();
        queryDto.setUserPhone("13800138001");
        PageParam pageParam = new PageParam();

        // When
        PageResult<UserBo> result = userService.findUserPage(queryDto, pageParam);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getList()).hasSize(1);

        // 验证返回的用户数据的所有属性
        UserBo user = result.getList().get(0);
        assertThat(user.getId()).isEqualTo(1);
        assertThat(user.getUserName()).isEqualTo("testuser1");
        assertThat(user.getRealName()).isEqualTo("测试用户1");
        assertThat(user.getUserPhone()).isEqualTo("13800138001");
        assertThat(user.getUserGender()).isEqualTo(UserGenderEnum.MALE);
        assertThat(user.getCertificatesType()).isEqualTo(CertificatesTypeEnum.ID_CARD);
        assertThat(user.getCertificatesNo()).isEqualTo("110101199001011234");
        assertThat(user.getRemark()).isEqualTo("测试用户1备注");
        assertThat(user.getOrganizationId()).isEqualTo(1001);
    }

    @Test
    @DisplayName("分页查询用户列表 - 按机构ID查询")
    void testFindUserPage_ByOrganizationId() {
        // Given
        UserQueryDto queryDto = new UserQueryDto();
        queryDto.setOrganizationId(1);
        PageParam pageParam = new PageParam();

        // When
        PageResult<UserBo> result = userService.findUserPage(queryDto, pageParam);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getList()).isNotEmpty();

        // 验证查询结果的机构ID
        result.getList().forEach(user ->
            assertThat(user.getOrganizationId()).isEqualTo(1)
        );
    }

    @Test
    @DisplayName("分页查询用户列表 - 空结果")
    void testFindUserPage_EmptyResult() {
        // Given
        UserQueryDto queryDto = new UserQueryDto();
        queryDto.setUserNameLike("nonexistentuser");
        PageParam pageParam = new PageParam();

        // When
        PageResult<UserBo> result = userService.findUserPage(queryDto, pageParam);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotal()).isEqualTo(0);
        assertThat(result.getList()).isEmpty();
    }

    @Test
    @DisplayName("查询用户列表 - 成功场景")
    void testFindUserList_Success() {
        // Given
        UserQueryDto queryDto = new UserQueryDto();

        // When
        List<UserBo> result = userService.findUserList(queryDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(2);

        // 验证第一个用户的所有属性
        UserBo firstUser = result.get(1);
        assertThat(firstUser.getId()).isEqualTo(1);
        assertThat(firstUser.getUserName()).isEqualTo("testuser1");
        assertThat(firstUser.getRealName()).isEqualTo("测试用户1");
        assertThat(firstUser.getUserPhone()).isEqualTo("13800138001");
        assertThat(firstUser.getUserGender()).isEqualTo(UserGenderEnum.MALE);
        assertThat(firstUser.getCertificatesType()).isEqualTo(CertificatesTypeEnum.ID_CARD);
        assertThat(firstUser.getCertificatesNo()).isEqualTo("110101199001011234");
        assertThat(firstUser.getRemark()).isEqualTo("测试用户1备注");
        assertThat(firstUser.getOrganizationId()).isEqualTo(1001);

        // 验证第二个用户的所有属性
        UserBo secondUser = result.get(0);
        assertThat(secondUser.getId()).isEqualTo(2);
        assertThat(secondUser.getUserName()).isEqualTo("testuser2");
        assertThat(secondUser.getRealName()).isEqualTo("测试用户2");
        assertThat(secondUser.getUserPhone()).isEqualTo("13800138002");
        assertThat(secondUser.getUserGender()).isEqualTo(UserGenderEnum.FEMALE);
        assertThat(secondUser.getCertificatesType()).isEqualTo(CertificatesTypeEnum.ID_CARD);
        assertThat(secondUser.getCertificatesNo()).isEqualTo("110101199001011235");
        assertThat(secondUser.getRemark()).isEqualTo("测试用户2备注");
        assertThat(secondUser.getOrganizationId()).isEqualTo(1);
    }

    @Test
    @DisplayName("查询用户列表 - 按ID集合查询")
    void testFindUserList_ByIds() {
        // Given
        UserQueryDto queryDto = new UserQueryDto();
        queryDto.setIds(List.of(1, 2));

        // When
        List<UserBo> result = userService.findUserList(queryDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).extracting(UserBo::getId).containsExactlyInAnyOrder(1, 2);
    }

    @Test
    @DisplayName("获取用户详情 - 成功场景")
    void testGetUserInfo_Success() {
        // Given
        Integer userId = 1;

        // When
        UserBo result = userService.getUserInfo(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getUserName()).isEqualTo("testuser1");
        assertThat(result.getRealName()).isEqualTo("测试用户1");
        assertThat(result.getUserPhone()).isEqualTo("13800138001");
        assertThat(result.getUserGender()).isEqualTo(UserGenderEnum.MALE);
        assertThat(result.getCertificatesType()).isEqualTo(CertificatesTypeEnum.ID_CARD);
        assertThat(result.getCertificatesNo()).isEqualTo("110101199001011234");
        assertThat(result.getOrganizationId()).isEqualTo(1001);
    }

    @Test
    @DisplayName("获取用户详情 - 用户不存在")
    void testGetUserInfo_NotFound() {
        // Given
        Integer nonExistentUserId = 999999;

        // When & Then
        assertThatThrownBy(() -> userService.getUserInfo(nonExistentUserId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("新增用户 - 成功场景")
    void testAdd_Success() {
        // Given
        UserCreateDto dto = new UserCreateDto();
        dto.setUserName("newuser");
        dto.setPassword("Password123");
        dto.setRealName("新用户");
        dto.setUserPhone("13900139000");
        dto.setUserGender(UserGenderEnum.MALE);
        dto.setCertificatesType(CertificatesTypeEnum.ID_CARD);
        dto.setCertificatesNo("110101199001011999");
        dto.setRemark("新增用户测试");
        dto.setOrganizationId(1);
        dto.setRoleIds(List.of(1));

        // When
        Integer userId = userService.add(dto);

        // Then
        assertThat(userId).isNotNull();
        assertThat(userId).isGreaterThan(0);

        // 验证用户已创建
        UserBo createdUser = userService.getUserInfo(userId);
        assertThat(createdUser.getUserName()).isEqualTo("newuser");
        assertThat(createdUser.getRealName()).isEqualTo("新用户");
        assertThat(createdUser.getUserPhone()).isEqualTo("13900139000");
        assertThat(createdUser.getUserGender()).isEqualTo(UserGenderEnum.MALE);
        assertThat(createdUser.getCertificatesType()).isEqualTo(CertificatesTypeEnum.ID_CARD);
        assertThat(createdUser.getCertificatesNo()).isEqualTo("110101199001011999");
        assertThat(createdUser.getRemark()).isEqualTo("新增用户测试");
        assertThat(createdUser.getOrganizationId()).isEqualTo(1);
        assertThat(createdUser.getRoles()).extracting("id").containsExactly(1);

        var createdUserRoles = userRoleRepository.selectByUserId(userId);
        assertThat(createdUserRoles).extracting(UserRoleEntity::getRoleId).containsExactly(1);
    }

    @Test
    @DisplayName("新增用户 - 用户名已存在")
    void testAdd_UserNameExists() {
        // Given
        UserCreateDto dto = new UserCreateDto();
        dto.setUserName("testuser1"); // 已存在的用户名
        dto.setPassword("password123");
        dto.setRealName("重复用户");
        dto.setUserPhone("13900139001");
        dto.setUserGender(UserGenderEnum.MALE);
        dto.setCertificatesType(CertificatesTypeEnum.ID_CARD);
        dto.setCertificatesNo("110101199001011998");
        dto.setOrganizationId(1);

        // When & Then
        assertThatThrownBy(() -> userService.add(dto))
                .isInstanceOf(BusinessRuntimeException.class);
    }

    @Test
    @DisplayName("新增用户 - 手机号已存在")
    void testAdd_UserPhoneExists() {
        UserCreateDto dto = new UserCreateDto();
        dto.setUserName("uniqueUser");
        dto.setPassword("Password123");
        dto.setRealName("手机号重复用户");
        dto.setUserPhone("13800138001"); // 已存在
        dto.setUserGender(UserGenderEnum.MALE);
        dto.setCertificatesType(CertificatesTypeEnum.ID_CARD);
        dto.setCertificatesNo("110101199001012222");
        dto.setOrganizationId(1);

        assertThatThrownBy(() -> userService.add(dto))
                .isInstanceOf(BusinessRuntimeException.class)
                .hasMessageContaining("用户名或手机号");
    }

    @Test
    @DisplayName("新增用户 - 用户名非字母开头")
    void testAdd_UserNameInvalidPrefix() {
        UserCreateDto dto = new UserCreateDto();
        dto.setUserName("1invalid");
        dto.setPassword("password123");
        dto.setRealName("非法用户名");
        dto.setUserPhone("13900139005");
        dto.setUserGender(UserGenderEnum.MALE);
        dto.setCertificatesType(CertificatesTypeEnum.ID_CARD);
        dto.setCertificatesNo("110101199001013333");
        dto.setOrganizationId(1);

        assertThatThrownBy(() -> userService.add(dto))
                .isInstanceOf(BusinessRuntimeException.class)
                .hasMessageContaining("字母开头");
    }

    @Test
    @DisplayName("新增用户 - 非法角色ID")
    void testAdd_InvalidRoleIds() {
        UserCreateDto dto = new UserCreateDto();
        dto.setUserName("roleUser");
        dto.setPassword("password123");
        dto.setRealName("角色异常用户");
        dto.setUserPhone("13900139002");
        dto.setUserGender(UserGenderEnum.MALE);
        dto.setCertificatesType(CertificatesTypeEnum.ID_CARD);
        dto.setCertificatesNo("110101199001012333");
        dto.setOrganizationId(1);
        dto.setRoleIds(List.of(999)); // 不存在的角色

        assertThatThrownBy(() -> userService.add(dto))
                .isInstanceOf(BusinessRuntimeException.class)
                .hasMessageContaining("角色");
    }

    @Test
    @DisplayName("更新用户信息 - 成功场景")
    void testUpdate_Success() {
        // Given
        UserUpdateDto dto = new UserUpdateDto();
        dto.setId(1);
        dto.setRealName("更新后的用户名");
        dto.setUserPhone("13800138999");
        dto.setUserGender(UserGenderEnum.FEMALE);
        dto.setCertificatesType(CertificatesTypeEnum.PASSPORT);
        dto.setCertificatesNo("110101199001011000");
        dto.setRemark("更新用户信息测试");
        dto.setOrganizationId(1);
        dto.setRoleIds(List.of(2));

        // When
        userService.update(dto);

        // Then
        UserBo updatedUser = userService.getUserInfo(1);
        assertThat(updatedUser.getRealName()).isEqualTo("更新后的用户名");
        assertThat(updatedUser.getUserPhone()).isEqualTo("13800138999");
        assertThat(updatedUser.getUserGender()).isEqualTo(UserGenderEnum.FEMALE);
        assertThat(updatedUser.getCertificatesType()).isEqualTo(CertificatesTypeEnum.PASSPORT);
        assertThat(updatedUser.getCertificatesNo()).isEqualTo("110101199001011000");
        assertThat(updatedUser.getRemark()).isEqualTo("更新用户信息测试");
        assertThat(updatedUser.getRoles()).extracting("id").containsExactly(2);

        var rolesAfterUpdate = userRoleRepository.selectByUserId(1);
        assertThat(rolesAfterUpdate).extracting(UserRoleEntity::getRoleId).containsExactly(2);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @DisplayName("更新用户信息 - 事务提交后同步刷新会话用户信息")
    void testUpdate_ShouldRefreshSessionUserData_AfterCommit() {
        String randomSuffix = String.valueOf(ThreadLocalRandom.current().nextInt(10000000, 99999999));
        String userName = "sessionuser" + randomSuffix;
        String originalPhone = "139" + randomSuffix;
        String updatedPhone = "137" + randomSuffix;
        String updatedRealName = "会话刷新用户" + randomSuffix.substring(0, 3);

        UserCreateDto createDto = new UserCreateDto()
                .setUserName(userName)
                .setPassword("Abc123!x")
                .setRealName("会话原始用户")
                .setUserPhone(originalPhone)
                .setUserGender(UserGenderEnum.MALE)
                .setCertificatesType(CertificatesTypeEnum.ID_CARD)
                .setCertificatesNo("11010119900101" + randomSuffix.substring(0, 4))
                .setOrganizationId(1)
                .setRoleIds(List.of(2));

        Integer userId = userService.add(createDto);

        SaTokenContextMockUtil.setMockContext(() -> {
            StpUtil.login(userId);
            try {
                StpUtil.getSession().set(LoginConstant.LOGIN_USER_REAL_NAME, createDto.getRealName());
                StpUtil.getSession().set(LoginConstant.LOGIN_USER_PHONE, createDto.getUserPhone());

                userService.update(new UserUpdateDto()
                        .setId(userId)
                        .setRealName(updatedRealName)
                        .setUserPhone(updatedPhone));

                assertThat(StpUtil.getSession().get(LoginConstant.LOGIN_USER_REAL_NAME)).isEqualTo(updatedRealName);
                assertThat(StpUtil.getSession().get(LoginConstant.LOGIN_USER_PHONE)).isEqualTo(updatedPhone);
            } finally {
                try {
                    StpUtil.logout();
                } catch (NotLoginException ignore) {
                    // ignore for cleanup
                }
                userService.delete(userId);
            }
        });
    }

    @Test
    @DisplayName("更新用户信息 - 用户不存在")
    void testUpdate_UserNotFound() {
        UserUpdateDto dto = new UserUpdateDto();
        dto.setId(999999);
        dto.setRealName("不存在的用户");

        assertThatThrownBy(() -> userService.update(dto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("更新用户信息 - 手机号已存在")
    void testUpdate_UserPhoneExists() {
        UserUpdateDto dto = new UserUpdateDto();
        dto.setId(1);
        dto.setUserPhone("13800138002"); // 用户2的手机号

        assertThatThrownBy(() -> userService.update(dto))
                .isInstanceOf(BusinessRuntimeException.class)
                .hasMessageContaining("用户名或手机号");
    }

    @Test
    @DisplayName("更新用户信息 - 非法角色ID")
    void testUpdate_InvalidRoleIds() {
        UserUpdateDto dto = new UserUpdateDto();
        dto.setId(1);
        dto.setRoleIds(List.of(999));

        assertThatThrownBy(() -> userService.update(dto))
                .isInstanceOf(BusinessRuntimeException.class)
                .hasMessageContaining("角色");
    }

    @Test
    @DisplayName("删除用户 - 成功场景")
    void testDelete_Success() {
        // Given
        Integer userId = 2;

        // 验证用户存在
        UserBo userBeforeDelete = userService.getUserInfo(userId);
        assertThat(userBeforeDelete).isNotNull();
        assertThat(userRoleRepository.selectByUserId(userId)).isNotEmpty();

        // When
        userService.delete(userId);

        // Then
        assertThatThrownBy(() -> userService.getUserInfo(userId))
                .isInstanceOf(NotFoundException.class);
        assertThat(userRoleRepository.selectByUserId(userId)).isEmpty();
    }

    @Test
    @DisplayName("更新用户密码 - 成功场景")
    void testUpdatePassword_Success() {
        // Given
        UserUpdatePasswordDto dto = new UserUpdatePasswordDto();
        dto.setId(1);
        dto.setOldPassword("newpassword@123");
        dto.setNewPassword("1234AAwwk@d");

        userService.updatePassword(dto);

        UserEntity entity = userRepository.selectById(1);
        assertThat(passwordService.matchesPassword(dto.getNewPassword(), entity.getPassword())).isEqualTo(true);
    }

    @Test
    @DisplayName("更新用户密码 - 用户不存在")
    void testUpdatePassword_UserNotFound() {
        // Given
        UserUpdatePasswordDto dto = new UserUpdatePasswordDto();
        dto.setId(999999);
        dto.setOldPassword("password123");
        dto.setNewPassword("newpassword123");

        // When & Then
        assertThatThrownBy(() -> userService.updatePassword(dto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("用户服务验证测试 - 参数校验")
    void testUserService_ValidationTests_ShouldThrowException() {
        // 测试空参数
        assertThatThrownBy(() -> userService.findUserPage(null, new PageParam()))
                .isInstanceOf(Exception.class);

        assertThatThrownBy(() -> userService.findUserPage(new UserQueryDto(), null))
                .isInstanceOf(Exception.class);

        assertThatThrownBy(() -> userService.findUserList(null))
                .isInstanceOf(Exception.class);

        assertThatThrownBy(() -> userService.getUserInfo(null))
                .isInstanceOf(Exception.class);

        assertThatThrownBy(() -> userService.add(null))
                .isInstanceOf(Exception.class);

        assertThatThrownBy(() -> userService.update(null))
                .isInstanceOf(Exception.class);

        assertThatThrownBy(() -> userService.delete(null))
                .isInstanceOf(Exception.class);

        assertThatThrownBy(() -> userService.updatePassword(null))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("分页查询用户列表 - 分页参数测试")
    void testFindUserPage_Pagination() {
        // Given
        UserQueryDto queryDto = new UserQueryDto();
        PageParam pageParam1 = new PageParam().setPageNum(1).setPageSize(1);
        PageParam pageParam2 = new PageParam().setPageNum(2).setPageSize(1);

        // When
        PageResult<UserBo> result1 = userService.findUserPage(queryDto, pageParam1);
        PageResult<UserBo> result2 = userService.findUserPage(queryDto, pageParam2);

        // Then
        assertThat(result1).isNotNull();
        assertThat(result1.getList()).hasSize(1);

        assertThat(result2).isNotNull();

        // 如果有多个用户，验证分页效果
        if (result1.getTotal() > 1) {
            assertThat(result2.getList()).hasSize(1);
            assertThat(result1.getList().get(0).getId())
                    .isNotEqualTo(result2.getList().get(0).getId());
        }
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @DisplayName("管理员重置密码 - 事务提交后应清理登录态")
    void testResetPassword_ShouldClearLoginState_AfterCommit() {
        String randomSuffix = String.valueOf(ThreadLocalRandom.current().nextInt(10000000, 99999999));
        UserCreateDto createDto = new UserCreateDto()
                .setUserName("resetuser" + randomSuffix)
                .setPassword("ResetPass1!")
                .setRealName("重置密码用户")
                .setUserPhone("139" + randomSuffix)
                .setUserGender(UserGenderEnum.MALE)
                .setCertificatesType(CertificatesTypeEnum.ID_CARD)
                .setCertificatesNo("11010119900101" + randomSuffix.substring(0, 4))
                .setOrganizationId(1)
                .setRoleIds(List.of(2));

        Integer userId = userService.add(createDto);
        String pwdErrKey = LoginConstant.PWD_ERR + userId;
        RedisUtil.setCacheObject(pwdErrKey, 3);

        SaTokenContextMockUtil.setMockContext(() -> {
            try {
                StpUtil.login(userId);
                assertThat(StpUtil.isLogin()).isTrue();

                UserResetPasswordDto resetPasswordDto = new UserResetPasswordDto()
                        .setId(userId)
                        .setNewPassword("Abc" + randomSuffix + "!x");

                userService.resetPassword(resetPasswordDto);

                assertThat(StpUtil.isLogin()).isFalse();
                assertThat(RedisUtil.hasKey(pwdErrKey)).isFalse();
            } finally {
                try {
                    StpUtil.logout();
                } catch (NotLoginException ignore) {
                    // ignore for cleanup
                }
                RedisUtil.deleteObject(pwdErrKey);
                try {
                    userService.delete(userId);
                } catch (NotFoundException ignore) {
                    // ignore for cleanup
                }
            }
        });
    }

    @Test
    @DisplayName("管理员重置密码 - 事务提交前不应清理登录态")
    void testResetPassword_ShouldNotClearLoginState_BeforeCommit() {
        String randomSuffix = String.valueOf(ThreadLocalRandom.current().nextInt(10000000, 99999999));
        UserCreateDto createDto = new UserCreateDto()
                .setUserName("pendinguser" + randomSuffix)
                .setPassword("PendingPass1!")
                .setRealName("事务中用户")
                .setUserPhone("137" + randomSuffix)
                .setUserGender(UserGenderEnum.MALE)
                .setCertificatesType(CertificatesTypeEnum.ID_CARD)
                .setCertificatesNo("11010119900102" + randomSuffix.substring(0, 4))
                .setOrganizationId(1)
                .setRoleIds(List.of(2));

        Integer userId = userService.add(createDto);
        String pwdErrKey = LoginConstant.PWD_ERR + userId;
        RedisUtil.setCacheObject(pwdErrKey, 5);

        SaTokenContextMockUtil.setMockContext(() -> {
            try {
                StpUtil.login(userId);
                assertThat(StpUtil.isLogin()).isTrue();

                UserResetPasswordDto resetPasswordDto = new UserResetPasswordDto()
                        .setId(userId)
                        .setNewPassword("Abc" + randomSuffix + "!y");

                userService.resetPassword(resetPasswordDto);

                // 当前测试事务尚未提交，AFTER_COMMIT 监听器不应触发
                assertThat(StpUtil.isLogin()).isTrue();
                assertThat(RedisUtil.hasKey(pwdErrKey)).isTrue();
            } finally {
                try {
                    StpUtil.logout();
                } catch (NotLoginException ignore) {
                    // ignore for cleanup
                }
                RedisUtil.deleteObject(pwdErrKey);
            }
        });
    }
}
