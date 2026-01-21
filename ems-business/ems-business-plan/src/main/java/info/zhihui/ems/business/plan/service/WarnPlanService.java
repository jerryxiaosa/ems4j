package info.zhihui.ems.business.plan.service;

import info.zhihui.ems.business.plan.bo.WarnPlanBo;
import info.zhihui.ems.business.plan.dto.WarnPlanQueryDto;
import info.zhihui.ems.business.plan.dto.WarnPlanSaveDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 预警方案服务接口
 *
 * @author jerryxiaosa
 */
public interface WarnPlanService {

    /**
     * 查询预警方案列表
     *
     * @param query 查询条件
     * @return 预警方案列表
     */
    List<WarnPlanBo> findList(@NotNull WarnPlanQueryDto query);

    /**
     * 根据ID查询预警方案详情
     *
     * @param id 预警方案ID
     * @return 预警方案详情
     */
    WarnPlanBo getDetail(@NotNull Integer id);

    /**
     * 新增预警方案
     *
     * @param saveDto 预警方案保存数据
     * @return 新增后的预警方案ID
     */
    Integer add(@NotNull @Valid WarnPlanSaveDto saveDto);

    /**
     * 编辑预警方案
     *
     * @param saveDto 预警方案保存数据
     */
    void edit(@NotNull @Valid WarnPlanSaveDto saveDto);

    /**
     * 删除预警方案
     *
     * @param id 预警方案ID
     */
    void delete(@NotNull Integer id);
}
