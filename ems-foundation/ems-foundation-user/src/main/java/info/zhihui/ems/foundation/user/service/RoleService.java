package info.zhihui.ems.foundation.user.service;

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
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Set;

/**
 * 角色服务接口
 */
public interface RoleService {

    /**
     * 分页查询角色列表
     * - 仅返回未删除的数据
     * - 支持按角色名称模糊查询、角色标识、是否系统角色、是否禁用等条件过滤
     * - 支持ID集合查询和排除ID集合查询
     *
     * @param dto 查询条件DTO
     * @param pageParam 分页参数
     * @return 分页结果
     */
    PageResult<RoleBo> findPage(@NotNull @Valid RoleQueryDto dto, @NotNull PageParam pageParam);

    /**
     * 查询角色列表
     * - 仅返回未删除的数据
     * - 支持按角色名称模糊查询、角色标识、是否系统角色、是否禁用等条件过滤
     * - 支持ID集合查询和排除ID集合查询
     *
     * @param dto 查询条件DTO
     * @return 角色业务对象列表
     */
    List<RoleBo> findList(@NotNull @Valid RoleQueryDto dto);

    /**
     * 获取角色详情
     *
     * @param id 角色ID
     * @return 角色业务对象
     * @throws NotFoundException 当角色不存在时抛出
     */
    RoleDetailBo getDetail(@NotNull Integer id);

    /**
     * 新增角色
     * - 对角色标识唯一性进行校验
     *
     * @param dto 新增DTO
     * @return 新增的角色ID
     * @throws BusinessRuntimeException 当角色标识已存在时抛出
     */
    Integer add(@NotNull @Valid RoleCreateDto dto);

    /**
     * 更新角色信息
     * - 对角色标识唯一性进行校验（排除自身）
     *
     * @param dto 更新DTO
     * @throws NotFoundException 当角色不存在时抛出
     * @throws BusinessRuntimeException 当角色标识已存在时抛出
     */
    void update(@NotNull @Valid RoleUpdateDto dto);

    /**
     * 逻辑删除角色
     * - 检查是否为系统内置角色，如果是则不允许删除
     * - 检查是否有用户关联该角色，如有则不允许删除
     *
     * @param id 角色ID
     * @throws NotFoundException 当角色不存在时抛出
     * @throws BusinessRuntimeException 当角色为系统内置角色或有用户关联该角色时抛出
     */
    void delete(@NotNull Integer id);

    /**
     * 保存角色菜单关联
     * - 先删除原有关联，再批量插入新关联
     *
     * @param dto 角色菜单保存DTO
     * @throws NotFoundException 当角色不存在时抛出
     */
    void saveRoleMenu(@NotNull @Valid RoleMenuSaveDto dto);

    /**
     * 获取多个角色的权限
     * - 返回去重后的菜单权限代码
     *
     * @param roleId 角色ID集合
     * @return 权限代码列表
     */
    List<String> getRolePermissions(@NotNull Integer roleId);

    /**
     * 判断角色集合是否拥有指定权限
     *
     * @param roleIds 角色ID集合
     * @param permission 权限标识
     * @return 是否拥有权限
     */
    boolean existsPermission(@NotNull Set<Integer> roleIds, @NotNull String permission);
}
