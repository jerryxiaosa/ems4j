package info.zhihui.ems.web.user.biz;

import info.zhihui.ems.common.utils.TreeUtil;
import info.zhihui.ems.foundation.user.bo.MenuBo;
import info.zhihui.ems.foundation.user.bo.MenuDetailBo;
import info.zhihui.ems.foundation.user.dto.MenuCreateDto;
import info.zhihui.ems.foundation.user.dto.MenuQueryDto;
import info.zhihui.ems.foundation.user.dto.MenuUpdateDto;
import info.zhihui.ems.foundation.user.service.MenuService;
import info.zhihui.ems.web.user.mapstruct.MenuWebMapper;
import info.zhihui.ems.web.user.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 菜单管理业务编排。
 */
@Service
@RequiredArgsConstructor
public class MenuBiz {

    private final MenuService menuService;
    private final MenuWebMapper menuWebMapper;

    /**
     * 获取菜单详情。
     *
     * @param id 菜单ID
     * @return 菜单详情
     */
    public MenuVo getDetail(Integer id) {
        MenuDetailBo menuDetailBo = menuService.getDetail(id);
        return menuWebMapper.toMenuDetailVo(menuDetailBo);
    }

    /**
     * 新增菜单。
     *
     * @param createVo 创建信息
     * @return 菜单ID
     */
    public Integer add(MenuCreateVo createVo) {
        MenuCreateDto createDto = menuWebMapper.toMenuCreateDto(createVo);
        return menuService.add(createDto);
    }

    /**
     * 更新菜单。
     *
     * @param id       菜单ID
     * @param updateVo 更新信息
     */
    public void update(Integer id, MenuUpdateVo updateVo) {
        MenuUpdateDto updateDto = menuWebMapper.toMenuUpdateDto(updateVo);
        updateDto.setId(id);
        menuService.update(updateDto);
    }

    /**
     * 删除菜单。
     *
     * @param id 菜单ID
     */
    public void delete(Integer id) {
        menuService.delete(id);
    }

    /**
     * 查询菜单树形列表
     *
     * @param queryVo 查询条件
     * @return 树形菜单列表
     */
    public List<MenuWithChildrenVo> findTree(MenuQueryVo queryVo) {
        MenuQueryDto queryDto = menuWebMapper.toMenuQueryDto(queryVo);
        List<MenuBo> menuBos = menuService.findList(queryDto);
        if (menuBos == null || menuBos.isEmpty()) {
            return Collections.emptyList();
        }

        List<MenuWithChildrenVo> menuWithChildrenVos = menuWebMapper.toMenuVoList(menuBos);
        return TreeUtil.buildTree(
                menuWithChildrenVos,
                MenuWithChildrenVo::getId,
                MenuWithChildrenVo::getPid,
                (node, children) -> node.setChildren(children.isEmpty() ? Collections.emptyList() : new ArrayList<>(children))
        );
    }
}
