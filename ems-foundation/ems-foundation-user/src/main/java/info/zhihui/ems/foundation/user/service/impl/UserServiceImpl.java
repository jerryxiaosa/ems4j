package info.zhihui.ems.foundation.user.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.StpUtil;
import info.zhihui.ems.foundation.user.bo.RoleBo;
import info.zhihui.ems.foundation.user.bo.RoleSimpleBo;
import info.zhihui.ems.foundation.user.bo.UserBo;
import info.zhihui.ems.foundation.user.dto.*;
import info.zhihui.ems.foundation.user.entity.UserEntity;
import info.zhihui.ems.foundation.user.entity.UserRoleEntity;
import info.zhihui.ems.foundation.user.enums.RoleEnum;
import info.zhihui.ems.foundation.user.mapper.UserMapper;
import info.zhihui.ems.foundation.user.qo.UserQueryQo;
import info.zhihui.ems.foundation.user.repository.UserRepository;
import info.zhihui.ems.foundation.user.repository.UserRoleRepository;
import info.zhihui.ems.foundation.user.service.PasswordService;
import info.zhihui.ems.foundation.user.service.RoleService;
import info.zhihui.ems.foundation.user.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author jerryxiaosa
 */
@Service
@Validated
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final UserMapper mapper;
    private final UserRoleRepository userRoleRepository;
    private final RoleService roleService;
    private final PasswordService passwordService;

    private static final Pattern PWD_PATTERN = Pattern.compile(".*[^a-zA-Z0-9].*");

    /**
     * 分页查询用户列表
     * - 仅返回未删除的数据
     *
     * @param queryDto  查询条件（支持用户名模糊、真实姓名模糊、手机号、机构ID、ID集合）
     * @param pageParam 分页参数（页码与每页大小）
     * @return 分页结果（包含总数与用户列表）
     */
    @Override
    public PageResult<UserBo> findUserPage(@NotNull @Valid UserQueryDto queryDto, @NotNull PageParam pageParam) {
        UserQueryQo qo = mapper.queryDtoToQo(queryDto);
        try (Page<UserEntity> page = PageHelper.startPage(pageParam.getPageNum(), pageParam.getPageSize())) {
            PageInfo<UserEntity> pageInfo = page.doSelectPageInfo(() -> repository.selectByQo(qo));
            PageResult<UserBo> result = mapper.pageEntityToPageBo(pageInfo);

            fillUserRoles(result.getList());

            return result;
        }
    }

    /**
     * 查询用户列表
     * - 仅返回未删除的数据
     *
     * @param queryDto 查询条件（支持用户名模糊、真实姓名模糊、手机号、机构ID、ID集合）
     * @return 用户业务对象列表
     */
    @Override
    public List<UserBo> findUserList(@NotNull @Valid UserQueryDto queryDto) {
        UserQueryQo qo = mapper.queryDtoToQo(queryDto);
        List<UserEntity> list = repository.selectByQo(qo);
        List<UserBo> userBos = mapper.listEntityToBo(list);

        fillUserRoles(userBos);

        return userBos;
    }

    /**
     * 获取用户详情
     *
     * @param id 用户ID
     * @return 用户业务对象
     * @throws NotFoundException 当用户不存在时抛出
     */
    @Override
    public @NotNull UserBo getUserInfo(@NotNull Integer id) throws NotFoundException {
        UserEntity entity = repository.selectById(id);
        if (entity == null) {
            throw new NotFoundException("用户不存在");
        }
        UserBo userBo = mapper.entityToBo(entity);

        fillUserRoles(List.of(userBo));

        return userBo;
    }

    /**
     * 新增用户
     * - 对用户名唯一性进行校验
     *
     * @param dto 新增DTO
     * @return 新增的用户ID
     * @throws BusinessRuntimeException 当用户名已存在时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public @NotNull Integer add(@NotNull @Valid UserCreateDto dto) {
        // 校验角色ID
        validateRoleIds(dto.getRoleIds());
        validateUserNamePrefix(dto.getUserName());

        UserEntity entity = mapper.createDtoToEntity(dto);
        entity.setPassword(encodePasswordAfterSecurityCheck(dto.getPassword()));

        try {
            repository.insert(entity);
        } catch (DuplicateKeyException e) {
            throw new BusinessRuntimeException("用户名或手机号已存在");
        }

        // 保存用户角色关联
        saveUserRoles(entity.getId(), dto.getRoleIds());

        return entity.getId();
    }

    /**
     * 更新用户信息
     *
     * @param dto 更新DTO（支持更新真实姓名、手机号、性别、证件信息、备注、机构ID等）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(@NotNull @Valid UserUpdateDto dto) {
        // 校验用户是否存在
        UserEntity existEntity = repository.selectById(dto.getId());
        if (existEntity == null) {
            throw new NotFoundException("用户不存在");
        }

        // 校验角色ID
        validateRoleIds(dto.getRoleIds());

        UserEntity entity = mapper.updateDtoToEntity(dto);
        try {
            repository.updateById(entity);
        } catch (DuplicateKeyException e) {
            throw new BusinessRuntimeException("用户名或手机号已存在");
        }

        // 更新用户角色关联
        saveUserRoles(dto.getId(), dto.getRoleIds());
    }

    /**
     * 逻辑删除用户
     *
     * @param id 用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(@NotNull Integer id) {
        userRoleRepository.deleteByUserId(id);
        repository.deleteById(id);

        try {
            StpUtil.logout(id);
        } catch (NotLoginException ignore) {
            // ignore when user is not logged in
        }
    }

    /**
     * 更新用户密码
     *
     * @param dto 更新密码DTO（包含用户ID、旧密码、新密码）
     * @throws NotFoundException        当用户不存在时抛出
     * @throws BusinessRuntimeException 当旧密码不正确时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(@NotNull @Valid UserUpdatePasswordDto dto) {
        UserEntity entity = repository.selectById(dto.getId());
        if (entity == null) {
            throw new NotFoundException("用户不存在");
        }

        if (!passwordService.matchesPassword(dto.getOldPassword(), entity.getPassword())) {
            throw new BusinessRuntimeException("原密码不正确");
        }
        entity.setPassword(encodePasswordAfterSecurityCheck(dto.getNewPassword()));
        repository.updateById(entity);
    }

    /**
     * 判断当前用户是否拥有指定权限
     *
     * @param userId     用户ID
     * @param permission 权限标识
     * @return 是否拥有权限
     */
    @Override
    public boolean hasPermission(@NotNull Integer userId, @NotEmpty String permission) {
        UserBo userBo = getUserInfo(userId);

        if (CollectionUtils.isEmpty(userBo.getRoles())) {
            return false;
        }

        if (userBo.getRoles().stream().anyMatch(role -> RoleEnum.SUPER_ADMIN.getCode().equals(role.getRoleKey()))) {
            return true;
        }

        Set<Integer> roleIds = userBo.getRoles().stream()
                .map(RoleSimpleBo::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(roleIds)) {
            return false;
        }

        return roleService.existsPermission(roleIds, permission);
    }

    /**
     * 对密码进行编码并返回
     * - 密码安全级别校验
     *
     * @param rawPassword 原始密码
     * @return 编码后的密码
     * @throws BusinessRuntimeException 当密码安全级别不足时抛出
     */
    private String encodePasswordAfterSecurityCheck(String rawPassword) {
        // 校验密码安全级别
        int securityLevel = 0;
        if (rawPassword.chars().anyMatch(Character::isDigit)) {
            securityLevel++;
        }
        if (rawPassword.chars().anyMatch(Character::isLowerCase)) {
            securityLevel++;
        }
        if (rawPassword.chars().anyMatch(Character::isUpperCase)) {
            securityLevel++;
        }
        // 判断特殊字符
        if (securityLevel == 2) {
            Matcher matcher = PWD_PATTERN.matcher(rawPassword);
            if (matcher.find()) {
                securityLevel++;
            }
        }
        if (securityLevel < 3) {
            throw new BusinessRuntimeException("密码需要包含大小写字母、数字、字符，至少包含三种");
        }

        return passwordService.encode(rawPassword);
    }

    /**
     * 填充用户角色信息
     */
    private void fillUserRoles(List<UserBo> userBos) {
        if (CollectionUtils.isEmpty(userBos)) {
            return;
        }

        List<Integer> userIds = userBos.stream()
                .map(UserBo::getId)
                .collect(Collectors.toList());

        // 查询用户角色关联
        List<UserRoleEntity> userRoles = userRoleRepository.selectByUserIds(userIds);
        if (CollectionUtils.isEmpty(userRoles)) {
            return;
        }

        Map<Integer, List<RoleSimpleBo>> userRoleMap = buildGroupedRoleMap(userRoles);

        for (UserBo userBo : userBos) {
            List<RoleSimpleBo> userRoleList = userRoleMap.getOrDefault(userBo.getId(), Collections.emptyList());
            userBo.setRoles(userRoleList);
        }
    }

    /*
     * 构建按照用户分组的RoleSimpleBo集合
     */
    private Map<Integer, List<RoleSimpleBo>> buildGroupedRoleMap(List<UserRoleEntity> userRoles) {
        Map<Integer, RoleSimpleBo> roleMap = buildRoleMapByUserRoleEntity(userRoles);

        Map<Integer, List<RoleSimpleBo>> userRoleMap = new HashMap<>();
        for (UserRoleEntity userRole : userRoles) {
            RoleSimpleBo simpleBo = roleMap.get(userRole.getRoleId());
            // 排除禁用的角色
            if (simpleBo == null || Boolean.TRUE.equals(simpleBo.getIsDisabled())) {
                continue;
            }
            userRoleMap.computeIfAbsent(userRole.getUserId(), ignored -> new ArrayList<>())
                    .add(simpleBo);
        }

        return userRoleMap;
    }

    /*
     * 按角色ID分组角色
     */
    private Map<Integer, RoleSimpleBo> buildRoleMapByUserRoleEntity(List<UserRoleEntity> userRoles) {
        // 获取角色ID列表
        List<Integer> roleIds = userRoles.stream()
                .map(UserRoleEntity::getRoleId)
                .distinct()
                .collect(Collectors.toList());

        // 查询角色信息
        RoleQueryDto queryDto = new RoleQueryDto().setIds(roleIds);
        List<RoleBo> roles = roleService.findList(queryDto);
        List<RoleSimpleBo> roleSimpleBos = mapper.listRoleBoToSimpleBo(roles);

        return roleSimpleBos.stream()
                .collect(Collectors.toMap(RoleSimpleBo::getId, role -> role, (existing, replacement) -> existing));
    }

    /**
     * 校验角色ID
     */
    private void validateRoleIds(List<Integer> roleIds) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return;
        }

        RoleQueryDto queryDto = new RoleQueryDto().setIds(roleIds);
        List<RoleBo> roles = roleService.findList(queryDto);
        if (roles.size() != roleIds.size()) {
            throw new BusinessRuntimeException("存在无效的角色ID");
        }
    }

    /**
     * 保存用户角色关联
     */
    private void saveUserRoles(Integer userId, List<Integer> roleIds) {
        // 未传角色列表，保持现有角色不变
        if (roleIds == null) {
            return;
        }

        // 删除原有关联
        userRoleRepository.deleteByUserId(userId);

        // 角色列表为空，表示清空角色
        if (CollectionUtils.isEmpty(roleIds)) {
            return;
        }

        // 批量插入新关联
        List<UserRoleEntity> userRoles = roleIds.stream()
                .distinct()
                .map(roleId -> new UserRoleEntity()
                        .setUserId(userId)
                        .setRoleId(roleId))
                .toList();

        // 批量插入用户角色关联
        userRoleRepository.insert(userRoles);
    }

    private void validateUserNamePrefix(String userName) {
        if (!Character.isLetter(userName.charAt(0))) {
            throw new BusinessRuntimeException("用户名必须以字母开头");
        }
    }

}
