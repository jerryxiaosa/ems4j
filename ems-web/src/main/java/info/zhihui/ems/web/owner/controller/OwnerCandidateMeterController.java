package info.zhihui.ems.web.owner.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import info.zhihui.ems.common.utils.ResultUtil;
import info.zhihui.ems.common.vo.RestResult;
import info.zhihui.ems.web.owner.biz.OwnerCandidateMeterBiz;
import info.zhihui.ems.web.owner.vo.OwnerCandidateMeterQueryVo;
import info.zhihui.ems.web.owner.vo.OwnerCandidateMeterVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 主体候选电表接口
 */
@RestController
@RequestMapping("/owner-candidate-meters")
@Tag(name = "系统对象主体候选电表接口")
@Validated
@RequiredArgsConstructor
public class OwnerCandidateMeterController {

    private final OwnerCandidateMeterBiz ownerCandidateMeterBiz;

    @SaCheckPermission("owners:meters:candidate:list")
    @GetMapping
    @Operation(summary = "查询系统对象主体候选电表列表")
    public RestResult<List<OwnerCandidateMeterVo>> findCandidateMeterList(@Valid @ModelAttribute OwnerCandidateMeterQueryVo queryVo) {
        return ResultUtil.success(ownerCandidateMeterBiz.findCandidateMeterList(queryVo));
    }
}
