package info.zhihui.ems.web.user.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.common.utils.ResultUtil;
import info.zhihui.ems.common.vo.RestResult;
import info.zhihui.ems.web.user.biz.UserManageBiz;
import info.zhihui.ems.web.user.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
@Tag(name = "用户管理接口")
public class UserManageController {

    private final UserManageBiz userManageBiz;

    @SaCheckPermission("users:users:page")
    @GetMapping("/page")
    @Operation(summary = "分页查询用户列表")
    public RestResult<PageResult<UserVo>> findUserPage(
            @Valid @ModelAttribute UserQueryVo queryVo,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量", example = "10") @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<UserVo> page = userManageBiz.findUserPage(queryVo, pageNum, pageSize);
        return ResultUtil.success(page);
    }

    @SaCheckPermission("users:users:list")
    @GetMapping
    @Operation(summary = "查询用户列表")
    public RestResult<List<UserVo>> findUserList(@Valid @ModelAttribute UserQueryVo queryVo) {
        List<UserVo> list = userManageBiz.findUserList(queryVo);
        return ResultUtil.success(list);
    }

    @SaCheckPermission("users:users:detail")
    @GetMapping("/{id}")
    @Operation(summary = "获取用户详情")
    public RestResult<UserVo> getUser(@Parameter(description = "用户ID") @PathVariable Integer id) {
        return ResultUtil.success(userManageBiz.getUser(id));
    }

    @SaCheckPermission("users:users:add")
    @PostMapping
    @Operation(summary = "新增用户")
    public RestResult<Integer> createUser(@Valid @RequestBody UserCreateVo createVo) {
        Integer userId = userManageBiz.createUser(createVo);
        return ResultUtil.success(userId);
    }

    @SaCheckPermission("users:users:update")
    @PutMapping("/{id}")
    @Operation(summary = "更新用户信息")
    public RestResult<Void> updateUser(@Parameter(description = "用户ID") @PathVariable Integer id,
                                       @Valid @RequestBody UserUpdateVo updateVo) {
        userManageBiz.updateUser(id, updateVo);
        return ResultUtil.success();
    }

    @SaCheckPermission("users:users:delete")
    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    public RestResult<Void> deleteUser(@Parameter(description = "用户ID") @PathVariable Integer id) {
        userManageBiz.deleteUser(id);
        return ResultUtil.success();
    }

    @SaCheckPermission("users:users:password")
    @PutMapping("/{id}/password")
    @Operation(summary = "修改用户密码")
    public RestResult<Void> updatePassword(@Parameter(description = "用户ID") @PathVariable Integer id,
                                           @Valid @RequestBody UserPasswordUpdateVo passwordUpdateVo) {
        userManageBiz.updatePassword(id, passwordUpdateVo);
        return ResultUtil.success();
    }

    @SaCheckPermission("users:users:password:reset")
    @PutMapping("/{id}/password/reset")
    @Operation(summary = "重置用户密码")
    public RestResult<Void> resetPassword(@Parameter(description = "用户ID") @PathVariable Integer id,
                                          @Valid @RequestBody UserPasswordResetVo passwordResetVo) {
        userManageBiz.resetPassword(id, passwordResetVo);
        return ResultUtil.success();
    }
}
