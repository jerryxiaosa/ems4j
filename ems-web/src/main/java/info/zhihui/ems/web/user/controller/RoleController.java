package info.zhihui.ems.web.user.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.common.utils.ResultUtil;
import info.zhihui.ems.common.vo.RestResult;
import info.zhihui.ems.web.user.biz.RoleBiz;
import info.zhihui.ems.web.user.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理控制器
 */
@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@Validated
@Tag(name = "角色管理接口")
public class RoleController {

    private final RoleBiz roleBiz;

    @SaCheckPermission("users:roles:page")
    @GetMapping("/page")
    @Operation(summary = "分页查询角色列表")
    public RestResult<PageResult<RoleVo>> findRolePage(
            @Valid @ModelAttribute RoleQueryVo queryVo,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量", example = "10") @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<RoleVo> page = roleBiz.findRolePage(queryVo, pageNum, pageSize);
        return ResultUtil.success(page);
    }

    @SaCheckPermission("users:roles:list")
    @GetMapping
    @Operation(summary = "查询角色列表")
    public RestResult<List<RoleVo>> findRoleList(@Valid @ModelAttribute RoleQueryVo queryVo) {
        List<RoleVo> list = roleBiz.findRoleList(queryVo);
        return ResultUtil.success(list);
    }

    @SaCheckPermission("users:roles:detail")
    @GetMapping("/{id}")
    @Operation(summary = "获取角色详情")
    public RestResult<RoleDetailVo> getRoleDetail(@Parameter(description = "角色ID") @PathVariable Integer id) {
        return ResultUtil.success(roleBiz.getRoleDetail(id));
    }

    @SaCheckPermission("users:roles:add")
    @PostMapping
    @Operation(summary = "新增角色")
    public RestResult<Integer> createRole(@Valid @RequestBody RoleCreateVo createVo) {
        Integer roleId = roleBiz.createRole(createVo);
        return ResultUtil.success(roleId);
    }

    @SaCheckPermission("users:roles:update")
    @PutMapping("/{id}")
    @Operation(summary = "更新角色信息")
    public RestResult<Void> updateRole(@Parameter(description = "角色ID") @PathVariable Integer id,
                                       @Valid @RequestBody RoleUpdateVo updateVo) {
        roleBiz.updateRole(id, updateVo);
        return ResultUtil.success();
    }

    @SaCheckPermission("users:roles:menu")
    @PutMapping("/{id}/menus")
    @Operation(summary = "保存角色菜单关联")
    public RestResult<Void> saveRoleMenu(@Parameter(description = "角色ID") @PathVariable Integer id,
                                         @Valid @RequestBody RoleMenuSaveVo saveVo) {
        roleBiz.saveRoleMenu(id, saveVo);
        return ResultUtil.success();
    }

    @SaCheckPermission("users:roles:delete")
    @DeleteMapping("/{id}")
    @Operation(summary = "删除角色")
    public RestResult<Void> deleteRole(@Parameter(description = "角色ID") @PathVariable Integer id) {
        roleBiz.deleteRole(id);
        return ResultUtil.success();
    }
}