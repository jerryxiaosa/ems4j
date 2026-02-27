package info.zhihui.ems.web.owner.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import info.zhihui.ems.common.utils.ResultUtil;
import info.zhihui.ems.common.vo.RestResult;
import info.zhihui.ems.web.owner.biz.OwnerAccountBiz;
import info.zhihui.ems.web.owner.vo.OwnerAccountStatusQueryVo;
import info.zhihui.ems.web.owner.vo.OwnerAccountStatusVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 主体账户接口
 */
@RestController
@RequestMapping("/owner-accounts")
@Tag(name = "系统对象主体账户接口")
@Validated
@RequiredArgsConstructor
public class OwnerAccountController {

    private final OwnerAccountBiz ownerAccountBiz;

    @SaCheckPermission("owners:accounts:status")
    @GetMapping("/status")
    @Operation(summary = "查询系统对象主体账户状态")
    public RestResult<OwnerAccountStatusVo> getAccountStatus(@Valid @ModelAttribute OwnerAccountStatusQueryVo queryVo) {
        return ResultUtil.success(ownerAccountBiz.getAccountStatus(queryVo));
    }
}

