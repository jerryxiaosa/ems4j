package info.zhihui.ems.web.user.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import info.zhihui.ems.common.utils.ResultUtil;
import info.zhihui.ems.common.vo.RestResult;
import info.zhihui.ems.web.user.biz.MenuBiz;
import info.zhihui.ems.web.user.vo.*;
import info.zhihui.ems.web.user.vo.MenuVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单管理控制器
 */
@RestController
@RequestMapping("/menus")
@RequiredArgsConstructor
@Validated
@Tag(name = "菜单管理接口")
public class MenuController {

    private final MenuBiz menuBiz;

    @SaCheckPermission("users:menus:tree")
    @GetMapping
    @Operation(summary = "查询菜单树列表")
    public RestResult<List<MenuWithChildrenVo>> findMenuTree(@Valid @ModelAttribute MenuQueryVo queryVo) {
        List<MenuWithChildrenVo> menuTree = menuBiz.findTree(queryVo);
        return ResultUtil.success(menuTree);
    }

    @SaCheckPermission("users:menus:detail")
    @GetMapping("/{id}")
    @Operation(summary = "获取菜单详情")
    public RestResult<MenuVo> getDetail(@Parameter(description = "菜单ID") @PathVariable Integer id) {
        MenuVo detail = menuBiz.getDetail(id);
        return ResultUtil.success(detail);
    }

    @SaCheckPermission("users:menus:add")
    @PostMapping
    @Operation(summary = "新增菜单")
    public RestResult<Integer> add(@Valid @RequestBody MenuCreateVo createVo) {
        Integer id = menuBiz.add(createVo);
        return ResultUtil.success(id);
    }

    @SaCheckPermission("users:menus:update")
    @PutMapping("/{id}")
    @Operation(summary = "更新菜单信息")
    public RestResult<Void> update(@Parameter(description = "菜单ID") @PathVariable Integer id,
                                   @Valid @RequestBody MenuUpdateVo updateVo) {
        menuBiz.update(id, updateVo);
        return ResultUtil.success();
    }

    @SaCheckPermission("users:menus:delete")
    @DeleteMapping("/{id}")
    @Operation(summary = "删除菜单")
    public RestResult<Void> delete(@Parameter(description = "菜单ID") @PathVariable Integer id) {
        menuBiz.delete(id);
        return ResultUtil.success();
    }
}
