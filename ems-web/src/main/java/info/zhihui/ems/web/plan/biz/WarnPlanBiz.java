package info.zhihui.ems.web.plan.biz;

import info.zhihui.ems.business.plan.bo.WarnPlanBo;
import info.zhihui.ems.business.plan.dto.WarnPlanQueryDto;
import info.zhihui.ems.business.plan.dto.WarnPlanSaveDto;
import info.zhihui.ems.business.plan.service.WarnPlanService;
import info.zhihui.ems.web.plan.mapstruct.WarnPlanWebMapper;
import info.zhihui.ems.web.plan.vo.WarnPlanQueryVo;
import info.zhihui.ems.web.plan.vo.WarnPlanSaveVo;
import info.zhihui.ems.web.plan.vo.WarnPlanVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 预警方案业务编排
 */
@Service
@RequiredArgsConstructor
public class WarnPlanBiz {

    private final WarnPlanService warnPlanService;
    private final WarnPlanWebMapper warnPlanWebMapper;

    /**
     * 查询方案列表
     */
    public List<WarnPlanVo> findWarnPlanList(WarnPlanQueryVo queryVo) {
        WarnPlanQueryDto queryDto = warnPlanWebMapper.toWarnPlanQueryDto(queryVo);
        List<WarnPlanBo> list = warnPlanService.findList(queryDto);

        return warnPlanWebMapper.toWarnPlanVoList(list);
    }

    /**
     * 查询详情
     */
    public WarnPlanVo getWarnPlan(Integer id) {
        WarnPlanBo bo = warnPlanService.getDetail(id);
        return warnPlanWebMapper.toWarnPlanVo(bo);
    }

    /**
     * 新增方案
     */
    public Integer addWarnPlan(WarnPlanSaveVo saveVo) {
        WarnPlanSaveDto saveDto = warnPlanWebMapper.toWarnPlanSaveDto(saveVo);
        return warnPlanService.add(saveDto);
    }

    /**
     * 编辑方案
     */
    public void updateWarnPlan(Integer id, WarnPlanSaveVo saveVo) {
        WarnPlanSaveDto saveDto = warnPlanWebMapper.toWarnPlanSaveDto(saveVo);

        saveDto.setId(id);
        warnPlanService.edit(saveDto);
    }

    /**
     * 删除方案
     */
    public void deleteWarnPlan(Integer id) {
        warnPlanService.delete(id);
    }
}
