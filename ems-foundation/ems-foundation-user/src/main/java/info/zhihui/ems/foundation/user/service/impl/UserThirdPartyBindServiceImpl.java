package info.zhihui.ems.foundation.user.service.impl;

import info.zhihui.ems.foundation.user.bo.UserThirdPartyBindBo;
import info.zhihui.ems.foundation.user.dto.UserThirdPartyBindDto;
import info.zhihui.ems.foundation.user.entity.UserThirdPartyBindEntity;
import info.zhihui.ems.foundation.user.repository.UserThirdPartyBindRepository;
import info.zhihui.ems.foundation.user.service.UserThirdPartyBindService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;

/**
 * 用户第三方身份绑定服务实现。
 */
@Service
@Validated
@RequiredArgsConstructor
public class UserThirdPartyBindServiceImpl implements UserThirdPartyBindService {

    private final UserThirdPartyBindRepository repository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserThirdPartyBindBo bindOrUpdate(@NotNull @Valid UserThirdPartyBindDto dto) {
        UserThirdPartyBindEntity entity = toEntity(dto);
        int updateRows = repository.updateByIdentity(entity);
        if (updateRows <= 0) {
            updateRows = repository.updateByUserPlatform(entity);
        }
        if (updateRows <= 0) {
            try {
                repository.insert(entity);
            } catch (DuplicateKeyException e) {
                repository.updateByIdentity(entity);
            }
        }

        UserThirdPartyBindEntity latest = repository.selectByIdentity(
                entity.getPlatform(),
                entity.getAppId(),
                entity.getThirdPartyUserId()
        );
        return toBo(latest);
    }

    private UserThirdPartyBindEntity toEntity(UserThirdPartyBindDto dto) {
        return new UserThirdPartyBindEntity()
                .setUserId(dto.getUserId())
                .setPlatform(dto.getPlatform().getCode())
                .setAppId(dto.getAppId())
                .setThirdPartyUserId(dto.getThirdPartyUserId())
                .setThirdPartyUnionId(dto.getThirdPartyUnionId())
                .setPhone(dto.getPhone())
                .setLastLoginTime(LocalDateTime.now());
    }

    private UserThirdPartyBindBo toBo(UserThirdPartyBindEntity entity) {
        if (entity == null) {
            return null;
        }
        return new UserThirdPartyBindBo()
                .setId(entity.getId())
                .setUserId(entity.getUserId())
                .setPlatform(entity.getPlatform())
                .setAppId(entity.getAppId())
                .setThirdPartyUserId(entity.getThirdPartyUserId())
                .setThirdPartyUnionId(entity.getThirdPartyUnionId())
                .setPhone(entity.getPhone())
                .setLastLoginTime(entity.getLastLoginTime());
    }
}
