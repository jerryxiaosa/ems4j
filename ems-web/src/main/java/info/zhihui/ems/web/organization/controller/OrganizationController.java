package info.zhihui.ems.web.organization.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.common.utils.ResultUtil;
import info.zhihui.ems.common.vo.RestResult;
import info.zhihui.ems.web.organization.biz.OrganizationBiz;
import info.zhihui.ems.web.organization.vo.OrganizationCreateVo;
import info.zhihui.ems.web.organization.vo.OrganizationQueryVo;
import info.zhihui.ems.web.organization.vo.OrganizationUpdateVo;
import info.zhihui.ems.web.organization.vo.OrganizationVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 组织管理控制器
 */
@RestController
@RequestMapping("/organizations")
@Tag(name = "组织管理接口")
@Validated
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationBiz organizationBiz;

    @SaCheckPermission("organizations:organizations:page")
    @GetMapping("/page")
    @Operation(summary = "分页查询组织列表")
    public RestResult<PageResult<OrganizationVo>> findOrganizationPage(
            @Valid @ModelAttribute OrganizationQueryVo queryVo,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量", example = "10") @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<OrganizationVo> page = organizationBiz.findOrganizationPage(queryVo, pageNum, pageSize);
        return ResultUtil.success(page);
    }

    @SaCheckPermission("organizations:organizations:list")
    @GetMapping
    @Operation(summary = "查询组织列表")
    public RestResult<List<OrganizationVo>> findOrganizationList(@Valid @ModelAttribute OrganizationQueryVo queryVo) {
        List<OrganizationVo> list = organizationBiz.findOrganizationList(queryVo);
        return ResultUtil.success(list);
    }

    @SaCheckPermission("organizations:organizations:detail")
    @GetMapping("/{id}")
    @Operation(summary = "获取组织详情")
    public RestResult<OrganizationVo> getOrganization(@Parameter(description = "组织ID") @PathVariable Integer id) {
        return ResultUtil.success(organizationBiz.getOrganization(id));
    }

    @SaCheckPermission("organizations:organizations:add")
    @PostMapping
    @Operation(summary = "新增组织")
    public RestResult<Integer> createOrganization(@Valid @RequestBody OrganizationCreateVo createVo) {
        Integer id = organizationBiz.createOrganization(createVo);
        return ResultUtil.success(id);
    }

    @SaCheckPermission("organizations:organizations:update")
    @PutMapping("/{id}")
    @Operation(summary = "更新组织信息")
    public RestResult<Void> updateOrganization(@Parameter(description = "组织ID") @PathVariable Integer id,
                                               @Valid @RequestBody OrganizationUpdateVo updateVo) {
        organizationBiz.updateOrganization(id, updateVo);
        return ResultUtil.success();
    }

    @SaCheckPermission("organizations:organizations:delete")
    @DeleteMapping("/{id}")
    @Operation(summary = "删除组织")
    public RestResult<Void> deleteOrganization(@Parameter(description = "组织ID") @PathVariable Integer id) {
        organizationBiz.deleteOrganization(id);
        return ResultUtil.success();
    }
}
