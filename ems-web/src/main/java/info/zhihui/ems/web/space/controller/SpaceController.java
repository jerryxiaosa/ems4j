package info.zhihui.ems.web.space.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import info.zhihui.ems.common.utils.ResultUtil;
import info.zhihui.ems.common.vo.RestResult;
import info.zhihui.ems.web.space.biz.SpaceBiz;
import info.zhihui.ems.web.space.vo.SpaceCreateVo;
import info.zhihui.ems.web.space.vo.SpaceVo;
import info.zhihui.ems.web.space.vo.SpaceQueryVo;
import info.zhihui.ems.web.space.vo.SpaceUpdateVo;
import info.zhihui.ems.web.space.vo.SpaceWithChildrenVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 空间管理控制器
 */
@RestController
@RequestMapping("/spaces")
@Tag(name = "空间管理接口")
@Validated
@RequiredArgsConstructor
public class SpaceController {

    private final SpaceBiz spaceBiz;

    @SaCheckPermission("spaces:spaces:list")
    @GetMapping
    @Operation(summary = "查询空间列表")
    public RestResult<List<SpaceVo>> findSpaceList(@Valid @ModelAttribute SpaceQueryVo queryVo) {
        return ResultUtil.success(spaceBiz.findSpaceList(queryVo));
    }

    @SaCheckPermission("spaces:spaces:tree")
    @GetMapping("/tree")
    @Operation(summary = "查询空间树列表")
    public RestResult<List<SpaceWithChildrenVo>> findSpaceTree(@Valid @ModelAttribute SpaceQueryVo queryVo) {
        List<SpaceWithChildrenVo> tree = spaceBiz.findSpaceTree(queryVo);
        return ResultUtil.success(tree);
    }

    @SaCheckPermission("spaces:spaces:detail")
    @GetMapping("/{id}")
    @Operation(summary = "获取空间详情")
    public RestResult<SpaceVo> getSpace(@Parameter(description = "空间ID") @PathVariable Integer id) {
        return ResultUtil.success(spaceBiz.getSpace(id));
    }

    @SaCheckPermission("spaces:spaces:add")
    @PostMapping
    @Operation(summary = "新增空间")
    public RestResult<Integer> createSpace(@Valid @RequestBody SpaceCreateVo createVo) {
        Integer id = spaceBiz.createSpace(createVo);
        return ResultUtil.success(id);
    }

    @SaCheckPermission("spaces:spaces:update")
    @PutMapping("/{id}")
    @Operation(summary = "更新空间")
    public RestResult<Void> updateSpace(@Parameter(description = "空间ID") @PathVariable Integer id,
                                        @Valid @RequestBody SpaceUpdateVo updateVo) {
        spaceBiz.updateSpace(id, updateVo);
        return ResultUtil.success();
    }

    @SaCheckPermission("spaces:spaces:delete")
    @DeleteMapping("/{id}")
    @Operation(summary = "删除空间")
    public RestResult<Void> deleteSpace(@Parameter(description = "空间ID") @PathVariable Integer id) {
        spaceBiz.deleteSpace(id);
        return ResultUtil.success();
    }
}
