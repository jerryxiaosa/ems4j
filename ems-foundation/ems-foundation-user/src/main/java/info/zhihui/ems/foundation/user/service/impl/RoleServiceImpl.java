package info.zhihui.ems.foundation.user.service.impl;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
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
import info.zhihui.ems.foundation.user.entity.RoleMenuEntity;
import info.zhihui.ems.foundation.user.enums.RoleEnum;
import info.zhihui.ems.foundation.user.mapper.RoleMapper;
import info.zhihui.ems.foundation.user.qo.RoleQueryQo;
import info.zhihui.ems.foundation.user.repository.RoleMenuRepository;
import info.zhihui.ems.foundation.user.repository.RoleRepository;
import info.zhihui.ems.foundation.user.repository.UserRoleRepository;
import info.zhihui.ems.foundation.user.service.RoleService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 角色服务实现类
 */
@Service
@Validated
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    public static final String ROLE_PERMISSIONS_CACHE_NAME = "role-permissions";
    private static final String ADMIN_PERMISSION = "*:*:*";

    private final RoleRepository roleRepository;
    private final RoleMenuRepository roleMenuRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleMapper roleMapper;

    /**
     * 分页查询角色列表
     * - 仅返回未删除的数据
     * - 支持按角色名称模糊查询、角色标识、是否系统角色、是否禁用等条件过滤
     * - 支持ID集合查询和排除ID集合查询
     *
     * @param dto       查询条件DTO
     * @param pageParam 分页参数
     * @return 分页结果
     */
    @Override
    public PageResult<RoleBo> findPage(@NotNull @Valid RoleQueryDto dto, @NotNull PageParam pageParam) {
        RoleQueryQo qo = roleMapper.queryDtoToQo(dto);

        try (Page<RoleEntity> page = PageHelper.startPage(pageParam.getPageNum(), pageParam.getPageSize())) {
            PageInfo<RoleEntity> pageInfo = page.doSelectPageInfo(() -> roleRepository.selectByQo(qo));
            return roleMapper.pageEntityToPageBo(pageInfo);
        }
    }

    /**
     * 查询角色列表
     * - 仅返回未删除的数据
     * - 支持按角色名称模糊查询、角色标识、是否系统角色、是否禁用等条件过滤
     * - 支持ID集合查询和排除ID集合查询
     *
     * @param dto 查询条件DTO
     * @return 角色业务对象列表
     */
    @Override
    public List<RoleBo> findList(@NotNull @Valid RoleQueryDto dto) {
        RoleQueryQo qo = roleMapper.queryDtoToQo(dto);

        List<RoleEntity> entities = roleRepository.selectByQo(qo);
        return roleMapper.listEntityToBo(entities);
    }

    /**
     * 获取角色详情
     *
     * @param id 角色ID
     * @return 角色业务对象
     * @throws NotFoundException 当角色不存在时抛出
     */
    @Override
    public RoleDetailBo getDetail(@NotNull Integer id) {
        RoleEntity entity = roleRepository.selectById(id);
        if (entity == null) {
            throw new NotFoundException("角色不存在");
        }

        RoleDetailBo roleDetailBo = roleMapper.entityToDetailBo(entity);

        // 填充菜单权限信息
        fillRoleMenuInfo(roleDetailBo);

        return roleDetailBo;
    }

    /**
     * 新增角色
     * - 对角色标识唯一性进行校验
     *
     * @param dto 新增DTO
     * @return 新增的角色ID
     * @throws BusinessRuntimeException 当角色标识已存在时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = ROLE_PERMISSIONS_CACHE_NAME, key = "#result", condition = "#result != null")
    public Integer add(@NotNull @Valid RoleCreateDto dto) {
        RoleEntity entity = roleMapper.createDtoToEntity(dto);

        try {
            roleRepository.insert(entity);
        } catch (DuplicateKeyException e) {
            throw new BusinessRuntimeException("角色标识已存在");
        }

        return entity.getId();
    }

    /**
     * 更新角色信息
     * - 对角色标识唯一性进行校验（排除自身）
     *
     * @param dto 更新DTO
     * @throws NotFoundException        当角色不存在时抛出
     * @throws BusinessRuntimeException 当角色标识已存在时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = ROLE_PERMISSIONS_CACHE_NAME, key = "#dto.id")
    public void update(@NotNull @Valid RoleUpdateDto dto) {
        RoleEntity existEntity = roleRepository.selectById(dto.getId());
        if (existEntity == null) {
            throw new NotFoundException("角色不存在");
        }

        // 超管不能被禁用
        if (Boolean.TRUE.equals(dto.getIsDisabled()) && RoleEnum.SUPER_ADMIN.getCode().equals(existEntity.getRoleKey())) {
            throw new BusinessRuntimeException("超管角色不能被禁用");
        }

        // 检查角色标识唯一性（排除自身）
        if (!existEntity.getRoleKey().equals(dto.getRoleKey())) {
            RoleQueryQo queryQo = new RoleQueryQo();
            queryQo.setRoleKey(dto.getRoleKey());
            queryQo.setExcludeIds(List.of(dto.getId()));
            List<RoleEntity> existingRoles = roleRepository.selectByQo(queryQo);
            if (!CollectionUtils.isEmpty(existingRoles)) {
                throw new BusinessRuntimeException("角色标识已存在");
            }
        }

        RoleEntity entity = roleMapper.updateDtoToEntity(dto);
        roleRepository.updateById(entity);
    }

    /**
     * 逻辑删除角色
     * - 检查是否为系统内置角色，如果是则不允许删除
     * - 检查是否有用户关联该角色，如有则不允许删除
     *
     * @param id 角色ID
     * @throws NotFoundException        当角色不存在时抛出
     * @throws BusinessRuntimeException 当角色为系统内置角色或有用户关联该角色时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = ROLE_PERMISSIONS_CACHE_NAME, key = "#id")
    public void delete(@NotNull Integer id) {
        RoleEntity entity = roleRepository.selectById(id);
        if (entity == null) {
            throw new NotFoundException("角色不存在");
        }

        // 检查是否为系统内置角色，如果是则不允许删除
        if (Boolean.TRUE.equals(entity.getIsSystem())) {
            throw new BusinessRuntimeException("系统内置角色不允许删除");
        }

        // 存在用户关联不允许删除
        int userRoleCount = userRoleRepository.countByRoleId(id);
        if (userRoleCount > 0) {
            throw new BusinessRuntimeException("角色已被用户绑定，不允许删除");
        }

        // 删除角色
        roleRepository.deleteById(id);

        // 删除角色菜单关联
        roleMenuRepository.deleteByRoleId(id);
    }

    /**
     * 保存角色菜单关联
     * - 先删除原有关联，再批量插入新关联
     *
     * @param dto 角色菜单保存DTO
     * @throws NotFoundException 当角色不存在时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = ROLE_PERMISSIONS_CACHE_NAME, key = "#dto.roleId")
    public void saveRoleMenu(@NotNull @Valid RoleMenuSaveDto dto) {
        RoleEntity entity = roleRepository.selectById(dto.getRoleId());
        if (entity == null) {
            throw new NotFoundException("角色不存在");
        }

        // 先删除原有关联
        roleMenuRepository.deleteByRoleId(dto.getRoleId());

        // 批量插入新关联
        saveRoleMenuRelations(dto.getRoleId(), dto.getMenuIds());
    }

    /**
     * 获取角色权限
     * - 返回角色关联的所有菜单权限代码
     *
     * @param roleId 角色ID
     * @return 权限代码列表
     */
    @Override
    @Cacheable(value = ROLE_PERMISSIONS_CACHE_NAME, key = "#roleId")
    public List<String> getRolePermissions(@NotNull Integer roleId) {
        RoleEntity roleEntity = roleRepository.selectById(roleId);
        if (roleEntity == null) {
            return List.of();
        }
        if (RoleEnum.SUPER_ADMIN.getCode().equals(roleEntity.getRoleKey())) {
            return List.of(ADMIN_PERMISSION);
        }
        return roleRepository.selectPermissionsByRoleId(roleId);
    }

    /**
     * 判断角色集合是否拥有指定权限
     *
     * @param roleIds 角色ID集合
     * @param permission 权限标识
     * @return 是否拥有权限
     */
    @Override
    public boolean existsPermission(@NotNull Set<Integer> roleIds, @NotNull String permission) {
        if (CollectionUtils.isEmpty(roleIds) || !StringUtils.hasText(permission)) {
            return false;
        }
        return roleRepository.existsPermission(new ArrayList<>(roleIds), permission);
    }

    /**
     * 填充角色菜单信息
     */
    private void fillRoleMenuInfo(RoleDetailBo roleDetailBo) {
        // 填充菜单ID
        Integer roleId = roleDetailBo.getId();
        List<Integer> menuIds = roleMenuRepository.selectMenuIdsByRoleIds(List.of(roleId));
        roleDetailBo.setMenuIds(menuIds);

        // 填充权限信息
        List<String> permissions = roleRepository.selectPermissionsByRoleId(roleId);
        roleDetailBo.setPermissions(permissions);
    }

    /**
     * 保存角色菜单关联
     */
    private void saveRoleMenuRelations(Integer roleId, List<Integer> menuIds) {
        if (!CollectionUtils.isEmpty(menuIds)) {
            List<RoleMenuEntity> roleMenuEntities = new ArrayList<>();
            LocalDateTime now = LocalDateTime.now();

            for (Integer menuId : menuIds) {
                RoleMenuEntity roleMenuEntity = new RoleMenuEntity();
                roleMenuEntity.setRoleId(roleId);
                roleMenuEntity.setMenuId(menuId);
                roleMenuEntity.setCreateTime(now);
                roleMenuEntities.add(roleMenuEntity);
            }

            roleMenuRepository.insert(roleMenuEntities);
        }

    }
}
