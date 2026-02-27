package info.zhihui.ems.web.owner.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import info.zhihui.ems.common.utils.ResultUtil;
import info.zhihui.ems.common.vo.RestResult;
import info.zhihui.ems.web.owner.biz.OwnerSpaceLeaseBiz;
import info.zhihui.ems.web.owner.vo.OwnerSpaceRentVo;
import info.zhihui.ems.web.owner.vo.OwnerSpaceUnrentVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 主体空间租赁接口
 */
@RestController
@RequestMapping("/owner-space-leases")
@Tag(name = "系统对象主体空间租赁接口")
@Validated
@RequiredArgsConstructor
public class OwnerSpaceLeaseController {

    private final OwnerSpaceLeaseBiz ownerSpaceLeaseBiz;

    @SaCheckPermission("owners:spaces:rent")
    @PostMapping("/rent")
    @Operation(summary = "系统对象主体租赁空间")
    public RestResult<Void> rentSpaces(@NotNull(message = "请求参数不能为空")
                                       @Valid @RequestBody OwnerSpaceRentVo rentVo) {
        ownerSpaceLeaseBiz.rentSpaces(rentVo);
        return ResultUtil.success();
    }

    @SaCheckPermission("owners:spaces:unrent")
    @PostMapping("/unrent")
    @Operation(summary = "系统对象主体退租空间")
    public RestResult<Void> unrentSpaces(@NotNull(message = "请求参数不能为空")
                                         @Valid @RequestBody OwnerSpaceUnrentVo unrentVo) {
        ownerSpaceLeaseBiz.unrentSpaces(unrentVo);
        return ResultUtil.success();
    }
}
