package info.zhihui.ems.web.account.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.common.utils.ResultUtil;
import info.zhihui.ems.common.vo.RestResult;
import info.zhihui.ems.web.account.biz.AccountBiz;
import info.zhihui.ems.web.account.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 账户管理接口
 */
@RestController
@RequestMapping("/accounts")
@Tag(name = "账户管理接口")
@Validated
@RequiredArgsConstructor
public class AccountController {

    private final AccountBiz accountBiz;

    @SaCheckPermission("accounts:accounts:page")
    @GetMapping("/page")
    @Operation(summary = "分页查询账户列表")
    public RestResult<PageResult<AccountVo>> findAccountPage(
            @Valid @NotNull @ModelAttribute AccountQueryVo queryVo,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量", example = "10") @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<AccountVo> page = accountBiz.findAccountPage(queryVo, pageNum, pageSize);
        return ResultUtil.success(page);
    }

    @SaCheckPermission("accounts:accounts:detail")
    @GetMapping("/{id}")
    @Operation(summary = "获取账户详情")
    public RestResult<AccountDetailVo> getAccount(@Parameter(description = "账户ID") @PathVariable Integer id) {
        return ResultUtil.success(accountBiz.getAccount(id));
    }

    @SaCheckPermission("accounts:cancel:page")
    @GetMapping("/cancel/page")
    @Operation(summary = "分页查询销户记录")
    public RestResult<PageResult<AccountCancelRecordVo>> findCancelRecordPage(
            @Valid @ModelAttribute AccountCancelQueryVo queryVo,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量", example = "10") @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<AccountCancelRecordVo> page = accountBiz.findCancelRecordPage(queryVo, pageNum, pageSize);
        return ResultUtil.success(page);
    }

    @SaCheckPermission("accounts:cancel:detail")
    @GetMapping("/cancel/{cancelNo}")
    @Operation(summary = "获取销户详情")
    public RestResult<AccountCancelDetailVo> getCancelRecordDetail(@Parameter(description = "销户编号") @PathVariable String cancelNo) {
        return ResultUtil.success(accountBiz.getCancelRecordDetail(cancelNo));
    }

    @SaCheckPermission("accounts:accounts:open")
    @PostMapping("/open")
    @Operation(summary = "开户")
    public RestResult<Integer> openAccount(@Valid @RequestBody OpenAccountVo openAccountVo) {
        Integer accountId = accountBiz.openAccount(openAccountVo);
        return ResultUtil.success(accountId);
    }

    @SaCheckPermission("accounts:meters:open")
    @PostMapping("/{id}/meters/open")
    @Operation(summary = "追加绑定电表")
    public RestResult<Void> appendMeters(@Parameter(description = "账户ID") @PathVariable Integer id,
                                         @Valid @RequestBody AccountMetersOpenVo accountMetersOpenVo) {
        accountBiz.appendMeters(id, accountMetersOpenVo);
        return ResultUtil.success();
    }

    @SaCheckPermission("accounts:accounts:edit")
    @PutMapping("/{id}")
    @Operation(summary = "修改账户信息")
    public RestResult<Void> updateAccountConfig(@Parameter(description = "账户ID") @PathVariable Integer id,
                                                @Valid @RequestBody AccountConfigUpdateVo accountConfigUpdateVo) {
        accountBiz.updateAccountConfig(id, accountConfigUpdateVo);
        return ResultUtil.success();
    }

    @SaCheckPermission("accounts:accounts:cancel")
    @PostMapping("/cancel")
    @Operation(summary = "销户")
    public RestResult<CancelAccountResponseVo> cancelAccount(@Valid @RequestBody CancelAccountVo cancelAccountVo) {
        CancelAccountResponseVo responseVo = accountBiz.cancelAccount(cancelAccountVo);
        return ResultUtil.success(responseVo);
    }
}
