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
import info.zhihui.ems.foundation.user.mapper.MenuMapper;
import info.zhihui.ems.foundation.user.qo.MenuQueryQo;
import info.zhihui.ems.foundation.user.repository.MenuAuthRepository;
import info.zhihui.ems.foundation.user.repository.MenuPathRepository;
import info.zhihui.ems.foundation.user.repository.MenuRepository;
import info.zhihui.ems.foundation.user.service.MenuService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 菜单服务实现类
 */
@Service
@Validated
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final MenuRepository menuRepository;
    private final MenuAuthRepository menuAuthRepository;
    private final MenuPathRepository menuPathRepository;
    private final MenuMapper menuMapper;

    /**
     * 查询菜单列表
     * - 仅返回未删除的数据
     * - 支持按菜单名称模糊查询、菜单路径、权限标识、是否禁用等条件过滤
     * - 支持ID集合查询和排除ID集合查询
     *
     * @param dto 查询条件DTO
     * @return 菜单业务对象列表
     */
    @Override
    public List<MenuBo> findList(@NotNull @Valid MenuQueryDto dto) {
        MenuQueryQo qo = menuMapper.dtoToQo(dto);

        List<MenuEntity> entities = menuRepository.selectByQo(qo);
        return menuMapper.listEntityToBo(entities);
    }

    /**
     * 获取菜单详情
     *
     * @param id 菜单ID
     * @return 菜单业务对象
     * @throws NotFoundException 当菜单不存在时抛出
     */
    @Override
    public MenuDetailBo getDetail(@NotNull Integer id) {
        MenuEntity entity = menuRepository.selectById(id);
        if (entity == null) {
            throw new NotFoundException("菜单不存在");
        }

        MenuDetailBo menuDetailBo = menuMapper.entityToDetailBo(entity);

        // 填充权限代码
        List<String> permissionCodes = menuAuthRepository.selectPermissionCodesByMenuId(id);
        menuDetailBo.setPermissionCodes(permissionCodes);

        return menuDetailBo;
    }

    /**
     * 新增菜单
     * - 对菜单标识唯一性进行校验
     * - 自动维护菜单路径闭包表
     *
     * @param dto 新增DTO
     * @return 新增的菜单ID
     * @throws BusinessRuntimeException 当菜单标识已存在时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer add(@NotNull @Valid MenuCreateDto dto) {
        MenuEntity entity = menuMapper.createDtoToEntity(dto);

        try {
            menuRepository.insert(entity);
        } catch (DuplicateKeyException e) {
            throw new BusinessRuntimeException("菜单标识已存在");
        }

        Integer menuId = entity.getId();

        // 维护菜单路径闭包表
        menuPathRepository.insert(new MenuPathEntity().setAncestorId(menuId).setDescendantId(menuId).setDepth(0));
        if (dto.getPid() != null && dto.getPid() != 0) {
            int inherited = menuPathRepository.insertPathsFromParent(menuId, dto.getPid());
            if (inherited <= 0) {
                throw new BusinessRuntimeException("父级菜单闭包信息缺失，创建菜单失败");
            }
        }

        // 保存菜单权限
        saveMenuPermissions(menuId, dto.getPermissionCodes());

        return menuId;
    }

    /**
     * 更新菜单信息
     * - 对菜单标识唯一性进行校验（排除自身）
     *
     * @param dto 更新DTO
     * @throws NotFoundException 当菜单不存在时抛出
     * @throws BusinessRuntimeException 当菜单标识已存在时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(@NotNull @Valid MenuUpdateDto dto) {
        MenuEntity existEntity = menuRepository.selectById(dto.getId());
        if (existEntity == null) {
            throw new NotFoundException("菜单不存在");
        }

        try {
            MenuEntity entity = menuMapper.updateDtoToEntity(dto);
            menuRepository.updateById(entity);
        } catch (DuplicateKeyException e) {
            throw new BusinessRuntimeException("菜单标识已存在");
        }

        // 更新菜单权限
        menuAuthRepository.deleteByMenuId(dto.getId());
        saveMenuPermissions(dto.getId(), dto.getPermissionCodes());
    }

    /**
     * 逻辑删除菜单
     * - 检查是否存在子菜单，如有则不允许删除
     * - 自动维护菜单路径闭包表
     *
     * @param id 菜单ID
     * @throws NotFoundException 当菜单不存在时抛出
     * @throws BusinessRuntimeException 当存在子菜单时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(@NotNull Integer id) {
        MenuEntity entity = menuRepository.selectById(id);
        if (entity == null) {
            throw new NotFoundException("菜单不存在");
        }

        // 检查是否存在子菜单
        MenuQueryQo queryQo = new MenuQueryQo();
        queryQo.setPid(id);
        List<MenuEntity> children = menuRepository.selectByQo(queryQo);
        if (!CollectionUtils.isEmpty(children)) {
            throw new BusinessRuntimeException("存在子菜单，无法删除");
        }

        // 删除菜单
        menuRepository.deleteById(id);

        // 维护菜单路径闭包表
        menuPathRepository.deleteSubtreePaths(id);

        // 删除菜单权限
        menuAuthRepository.deleteByMenuId(id);
    }

    /**
     * 保存菜单权限
     */
    private void saveMenuPermissions(Integer menuId, List<String> permissionCodes) {
        if (!CollectionUtils.isEmpty(permissionCodes)) {
            List<MenuAuthEntity> authEntities = new ArrayList<>();
            for (String permissionCode : permissionCodes) {
                MenuAuthEntity authEntity = new MenuAuthEntity();
                authEntity.setMenuId(menuId);
                authEntity.setPermissionCode(permissionCode);
                authEntity.setCreateTime(LocalDateTime.now());
                authEntities.add(authEntity);
            }
            menuAuthRepository.insert(authEntities);
        }
    }

}
