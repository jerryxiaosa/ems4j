package info.zhihui.ems.foundation.user.service;

import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.foundation.user.bo.UserBo;
import info.zhihui.ems.foundation.user.dto.UserCreateDto;
import info.zhihui.ems.foundation.user.dto.UserQueryDto;
import info.zhihui.ems.foundation.user.dto.UserUpdateDto;
import info.zhihui.ems.foundation.user.dto.UserUpdatePasswordDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * @author jerryxiaosa
 */
public interface UserService {
    /**
     * 分页查询用户列表
     * - 仅返回未删除的数据
     *
     * @param queryDto  查询条件（支持用户名模糊、真实姓名模糊、手机号、机构ID、ID集合）
     * @param pageParam 分页参数（页码与每页大小）
     * @return 分页结果（包含总数与用户列表）
     */
    PageResult<UserBo> findUserPage(@NotNull @Valid UserQueryDto queryDto, @NotNull PageParam pageParam);

    /**
     * 查询用户列表
     * - 仅返回未删除的数据
     *
     * @param queryDto 查询条件（支持用户名模糊、真实姓名模糊、手机号、机构ID、ID集合）
     * @return 用户业务对象列表
     */
    List<UserBo> findUserList(@NotNull @Valid UserQueryDto queryDto);

    /**
     * 获取用户详情
     *
     * @param id 用户ID
     * @return 用户业务对象
     * @throws NotFoundException 当用户不存在时抛出
     */
    UserBo getUserInfo(@NotNull Integer id);

    /**
     * 新增用户
     * - 对用户名唯一性进行校验
     *
     * @param dto 新增DTO
     * @return 新增的用户ID
     * @throws BusinessRuntimeException 当用户名已存在时抛出
     */
    Integer add(@NotNull @Valid UserCreateDto dto);

    /**
     * 更新用户信息
     *
     * @param dto 更新DTO（支持更新真实姓名、手机号、性别、证件信息、备注、机构ID等）
     */
    void update(@NotNull @Valid UserUpdateDto dto);

    /**
     * 逻辑删除用户
     *
     * @param id 用户ID
     */
    void delete(@NotNull Integer id);

    /**
     * 更新用户密码
     *
     * @param dto 更新密码DTO（包含用户ID、旧密码、新密码）
     * @throws NotFoundException        当用户不存在时抛出
     * @throws BusinessRuntimeException 当旧密码不正确时抛出
     */
    void updatePassword(@NotNull @Valid UserUpdatePasswordDto dto);

    /**
     * 判断当前用户是否拥有指定权限
     *
     * @param userId     用户ID
     * @param permission 权限标识
     * @return 是否拥有权限
     */
    boolean hasPermission(@NotNull Integer userId, @NotEmpty String permission);
}
