package info.zhihui.ems.foundation.user.service;

import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.foundation.user.bo.MenuBo;
import info.zhihui.ems.foundation.user.bo.MenuDetailBo;
import info.zhihui.ems.foundation.user.dto.MenuCreateDto;
import info.zhihui.ems.foundation.user.dto.MenuQueryDto;
import info.zhihui.ems.foundation.user.dto.MenuUpdateDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 菜单服务接口
 */
public interface MenuService {

    /**
     * 查询菜单列表
     * - 仅返回未删除的数据
     * - 支持按菜单名称模糊查询、菜单路径、权限标识、是否禁用等条件过滤
     * - 支持ID集合查询和排除ID集合查询
     *
     * @param dto 查询条件DTO
     * @return 菜单业务对象列表
     */
    List<MenuBo> findList(@NotNull @Valid MenuQueryDto dto);

    /**
     * 获取菜单详情
     *
     * @param id 菜单ID
     * @return 菜单业务对象
     * @throws NotFoundException 当菜单不存在时抛出
     */
    MenuDetailBo getDetail(@NotNull Integer id);

    /**
     * 新增菜单
     * - 对菜单标识唯一性进行校验
     * - 自动维护菜单路径闭包表
     *
     * @param dto 新增DTO
     * @return 新增的菜单ID
     * @throws BusinessRuntimeException 当菜单标识已存在时抛出
     */
    Integer add(@NotNull @Valid MenuCreateDto dto);

    /**
     * 更新菜单信息
     * - 对菜单标识唯一性进行校验（排除自身）
     *
     * @param dto 更新DTO
     * @throws NotFoundException 当菜单不存在时抛出
     * @throws BusinessRuntimeException 当菜单标识已存在时抛出
     */
    void update(@NotNull @Valid MenuUpdateDto dto);

    /**
     * 逻辑删除菜单
     * - 检查是否存在子菜单，如有则不允许删除
     * - 自动维护菜单路径闭包表
     *
     * @param id 菜单ID
     * @throws NotFoundException 当菜单不存在时抛出
     * @throws BusinessRuntimeException 当存在子菜单时抛出
     */
    void delete(@NotNull Integer id);

}