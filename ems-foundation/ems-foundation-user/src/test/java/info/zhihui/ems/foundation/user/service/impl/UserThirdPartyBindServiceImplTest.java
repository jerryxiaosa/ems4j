package info.zhihui.ems.foundation.user.service.impl;

import info.zhihui.ems.common.constant.ResultCode;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.foundation.user.bo.UserThirdPartyBindBo;
import info.zhihui.ems.foundation.user.dto.UserThirdPartyBindDto;
import info.zhihui.ems.foundation.user.entity.UserThirdPartyBindEntity;
import info.zhihui.ems.foundation.user.enums.UserThirdPartyPlatformEnum;
import info.zhihui.ems.foundation.user.repository.UserThirdPartyBindRepository;
import info.zhihui.ems.foundation.user.service.UserThirdPartyBindService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("用户第三方身份绑定服务测试")
class UserThirdPartyBindServiceImplTest {

    @Test
    void testBindOrUpdate_ShouldUseVoidReturnType() throws NoSuchMethodException {
        Method method = UserThirdPartyBindService.class.getMethod("bindOrUpdate", UserThirdPartyBindDto.class);

        assertThat(method.getReturnType()).isEqualTo(Void.TYPE);
    }

    @Test
    void testGetByUserPlatformAndAppId_WhenBindExists_ShouldReturnBind() {
        LocalDateTime lastLoginTime = LocalDateTime.of(2026, 5, 17, 10, 0);
        UserThirdPartyBindEntity entity = new UserThirdPartyBindEntity()
                .setId(1)
                .setUserId(7)
                .setPlatform(UserThirdPartyPlatformEnum.WECHAT_MINI.getCode())
                .setAppId("mini-app-id")
                .setThirdPartyUserId("openid-001")
                .setThirdPartyUnionId("unionid-001")
                .setPhone("13800138000")
                .setLastLoginTime(lastLoginTime);
        UserThirdPartyBindServiceImpl service = new UserThirdPartyBindServiceImpl(repositoryReturning(entity));

        UserThirdPartyBindBo result = service.getByUserPlatformAndAppId(7, UserThirdPartyPlatformEnum.WECHAT_MINI, "mini-app-id");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getUserId()).isEqualTo(7);
        assertThat(result.getPlatform()).isEqualTo(UserThirdPartyPlatformEnum.WECHAT_MINI.getCode());
        assertThat(result.getAppId()).isEqualTo("mini-app-id");
        assertThat(result.getThirdPartyUserId()).isEqualTo("openid-001");
        assertThat(result.getThirdPartyUnionId()).isEqualTo("unionid-001");
        assertThat(result.getPhone()).isEqualTo("13800138000");
        assertThat(result.getLastLoginTime()).isEqualTo(lastLoginTime);
    }

    @Test
    void testGetByUserPlatformAndAppId_WhenBindMissing_ShouldReturnNull() {
        UserThirdPartyBindServiceImpl service = new UserThirdPartyBindServiceImpl(repositoryReturning(null));

        UserThirdPartyBindBo result = service.getByUserPlatformAndAppId(7, UserThirdPartyPlatformEnum.WECHAT_MINI, "mini-app-id");

        assertThat(result).isNull();
    }

    @Test
    void testBindOrUpdate_WhenUserPlatformAppBindExists_ShouldUpdateBind() {
        UserThirdPartyBindDto dto = new UserThirdPartyBindDto()
                .setUserId(7)
                .setPlatform(UserThirdPartyPlatformEnum.WECHAT_MINI)
                .setAppId("mini-app-id")
                .setThirdPartyUserId("openid-001")
                .setThirdPartyUnionId("unionid-001")
                .setPhone("13800138000");
        AtomicReference<UserThirdPartyBindEntity> updatedEntity = new AtomicReference<>();
        UserThirdPartyBindRepository repository = repositoryForBindOrUpdate(updatedEntity);
        UserThirdPartyBindServiceImpl service = new UserThirdPartyBindServiceImpl(repository);

        service.bindOrUpdate(dto);

        assertThat(updatedEntity.get()).isNotNull();
        assertThat(updatedEntity.get().getPlatform()).isEqualTo(UserThirdPartyPlatformEnum.WECHAT_MINI.getCode());
        assertThat(updatedEntity.get().getUserId()).isEqualTo(7);
        assertThat(updatedEntity.get().getAppId()).isEqualTo("mini-app-id");
        assertThat(updatedEntity.get().getThirdPartyUserId()).isEqualTo("openid-001");
    }

    @Test
    void testBindOrUpdate_WhenUserPlatformAppBindMissing_ShouldInsertBind() {
        UserThirdPartyBindDto dto = new UserThirdPartyBindDto()
                .setUserId(7)
                .setPlatform(UserThirdPartyPlatformEnum.WECHAT_MINI)
                .setAppId("mini-app-id")
                .setThirdPartyUserId("openid-001")
                .setThirdPartyUnionId("unionid-001")
                .setPhone("13800138000");
        AtomicReference<UserThirdPartyBindEntity> insertedEntity = new AtomicReference<>();
        UserThirdPartyBindServiceImpl service = new UserThirdPartyBindServiceImpl(repositoryForInsert(insertedEntity, false));

        service.bindOrUpdate(dto);

        assertThat(insertedEntity.get()).isNotNull();
        assertThat(insertedEntity.get().getUserId()).isEqualTo(7);
        assertThat(insertedEntity.get().getPlatform()).isEqualTo(UserThirdPartyPlatformEnum.WECHAT_MINI.getCode());
        assertThat(insertedEntity.get().getAppId()).isEqualTo("mini-app-id");
        assertThat(insertedEntity.get().getThirdPartyUserId()).isEqualTo("openid-001");
    }

    @Test
    void testBindOrUpdate_WhenUnionIdMissing_ShouldInsertBindWithNullUnionId() {
        UserThirdPartyBindDto dto = new UserThirdPartyBindDto()
                .setUserId(7)
                .setPlatform(UserThirdPartyPlatformEnum.WECHAT_MINI)
                .setAppId("mini-app-id")
                .setThirdPartyUserId("openid-001")
                .setThirdPartyUnionId(null)
                .setPhone("13800138000");
        AtomicReference<UserThirdPartyBindEntity> insertedEntity = new AtomicReference<>();
        UserThirdPartyBindServiceImpl service = new UserThirdPartyBindServiceImpl(repositoryForInsert(insertedEntity, false));

        service.bindOrUpdate(dto);

        assertThat(insertedEntity.get()).isNotNull();
        assertThat(insertedEntity.get().getThirdPartyUnionId()).isNull();
        assertThat(insertedEntity.get().getPhone()).isEqualTo("13800138000");
        assertThat(insertedEntity.get().getLastLoginTime()).isNotNull();
    }

    @Test
    void testBindOrUpdate_WhenThirdPartyIdentityAlreadyBound_ShouldThrowBindConflict() {
        UserThirdPartyBindDto dto = new UserThirdPartyBindDto()
                .setUserId(7)
                .setPlatform(UserThirdPartyPlatformEnum.WECHAT_MINI)
                .setAppId("mini-app-id")
                .setThirdPartyUserId("openid-001")
                .setThirdPartyUnionId("unionid-001")
                .setPhone("13800138000");
        UserThirdPartyBindServiceImpl service = new UserThirdPartyBindServiceImpl(repositoryForInsert(new AtomicReference<>(), true));

        assertThatThrownBy(() -> service.bindOrUpdate(dto))
                .isInstanceOfSatisfying(BusinessRuntimeException.class, exception -> {
                    assertThat(exception.getCode()).isEqualTo(ResultCode.MOBILE_THIRD_PARTY_BIND_CONFLICT.getCode());
                    assertThat(exception.getMessage()).isEqualTo(ResultCode.MOBILE_THIRD_PARTY_BIND_CONFLICT.getMessage());
                });
    }

    @Test
    void testBindOrUpdate_WhenConcurrentInsertSameUserPlatformApp_ShouldRetryUpdate() {
        UserThirdPartyBindDto dto = new UserThirdPartyBindDto()
                .setUserId(7)
                .setPlatform(UserThirdPartyPlatformEnum.WECHAT_MINI)
                .setAppId("mini-app-id")
                .setThirdPartyUserId("openid-001")
                .setThirdPartyUnionId("unionid-001")
                .setPhone("13800138000");
        AtomicInteger updateCount = new AtomicInteger();
        UserThirdPartyBindServiceImpl service = new UserThirdPartyBindServiceImpl(repositoryForConcurrentInsert(updateCount));

        service.bindOrUpdate(dto);

        assertThat(updateCount.get()).isEqualTo(2);
    }

    @Test
    void testBindOrUpdate_WhenUpdateHitsThirdPartyIdentityConflict_ShouldThrowBindConflict() {
        UserThirdPartyBindDto dto = new UserThirdPartyBindDto()
                .setUserId(7)
                .setPlatform(UserThirdPartyPlatformEnum.WECHAT_MINI)
                .setAppId("mini-app-id")
                .setThirdPartyUserId("openid-001")
                .setThirdPartyUnionId("unionid-001")
                .setPhone("13800138000");
        AtomicInteger updateCount = new AtomicInteger();
        UserThirdPartyBindServiceImpl service = new UserThirdPartyBindServiceImpl(repositoryForUpdateConflict(updateCount));

        assertThatThrownBy(() -> service.bindOrUpdate(dto))
                .isInstanceOfSatisfying(BusinessRuntimeException.class, exception -> {
                    assertThat(exception.getCode()).isEqualTo(ResultCode.MOBILE_THIRD_PARTY_BIND_CONFLICT.getCode());
                    assertThat(exception.getMessage()).isEqualTo(ResultCode.MOBILE_THIRD_PARTY_BIND_CONFLICT.getMessage());
                });
        assertThat(updateCount.get()).isEqualTo(1);
    }

    private UserThirdPartyBindRepository repositoryReturning(UserThirdPartyBindEntity entity) {
        return (UserThirdPartyBindRepository) Proxy.newProxyInstance(
                UserThirdPartyBindRepository.class.getClassLoader(),
                new Class<?>[]{UserThirdPartyBindRepository.class},
                (proxy, method, args) -> {
                    if ("selectByUserPlatformAndAppId".equals(method.getName())) {
                        assertThat(args[0]).isEqualTo(UserThirdPartyPlatformEnum.WECHAT_MINI.getCode());
                        assertThat(args[1]).isEqualTo(7);
                        assertThat(args[2]).isEqualTo("mini-app-id");
                        return entity;
                    }
                    throw new UnsupportedOperationException(method.getName());
                }
        );
    }

    private UserThirdPartyBindRepository repositoryForBindOrUpdate(AtomicReference<UserThirdPartyBindEntity> updatedEntity) {
        return (UserThirdPartyBindRepository) Proxy.newProxyInstance(
                UserThirdPartyBindRepository.class.getClassLoader(),
                new Class<?>[]{UserThirdPartyBindRepository.class},
                (proxy, method, args) -> {
                    if ("updateByUserPlatform".equals(method.getName())) {
                        updatedEntity.set((UserThirdPartyBindEntity) args[0]);
                        return 1;
                    }
                    throw new UnsupportedOperationException(method.getName());
                }
        );
    }

    private UserThirdPartyBindRepository repositoryForInsert(AtomicReference<UserThirdPartyBindEntity> insertedEntity, boolean duplicate) {
        return (UserThirdPartyBindRepository) Proxy.newProxyInstance(
                UserThirdPartyBindRepository.class.getClassLoader(),
                new Class<?>[]{UserThirdPartyBindRepository.class},
                (proxy, method, args) -> {
                    if ("updateByUserPlatform".equals(method.getName())) {
                        return 0;
                    }
                    if ("insert".equals(method.getName())) {
                        if (duplicate) {
                            throw new DuplicateKeyException("duplicate third party identity");
                        }
                        insertedEntity.set((UserThirdPartyBindEntity) args[0]);
                        return 1;
                    }
                    throw new UnsupportedOperationException(method.getName());
                }
        );
    }

    private UserThirdPartyBindRepository repositoryForConcurrentInsert(AtomicInteger updateCount) {
        return (UserThirdPartyBindRepository) Proxy.newProxyInstance(
                UserThirdPartyBindRepository.class.getClassLoader(),
                new Class<?>[]{UserThirdPartyBindRepository.class},
                (proxy, method, args) -> {
                    if ("updateByUserPlatform".equals(method.getName())) {
                        return updateCount.incrementAndGet() == 1 ? 0 : 1;
                    }
                    if ("insert".equals(method.getName())) {
                        throw new DuplicateKeyException("duplicate same user platform app");
                    }
                    throw new UnsupportedOperationException(method.getName());
                }
        );
    }

    private UserThirdPartyBindRepository repositoryForUpdateConflict(AtomicInteger updateCount) {
        return (UserThirdPartyBindRepository) Proxy.newProxyInstance(
                UserThirdPartyBindRepository.class.getClassLoader(),
                new Class<?>[]{UserThirdPartyBindRepository.class},
                (proxy, method, args) -> {
                    if ("updateByUserPlatform".equals(method.getName())) {
                        updateCount.incrementAndGet();
                        throw new DuplicateKeyException("duplicate third party identity");
                    }
                    throw new UnsupportedOperationException(method.getName());
                }
        );
    }
}
