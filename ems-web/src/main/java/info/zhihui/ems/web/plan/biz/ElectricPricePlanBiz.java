package info.zhihui.ems.web.plan.biz;

import info.zhihui.ems.business.plan.bo.ElectricPricePlanBo;
import info.zhihui.ems.business.plan.bo.ElectricPricePlanDetailBo;
import info.zhihui.ems.business.plan.dto.ElectricPricePlanQueryDto;
import info.zhihui.ems.business.plan.dto.ElectricPricePlanSaveDto;
import info.zhihui.ems.business.plan.dto.ElectricPriceTimeDto;
import info.zhihui.ems.business.plan.dto.ElectricPriceTypeDto;
import info.zhihui.ems.business.plan.service.ElectricPricePlanService;
import info.zhihui.ems.web.plan.mapstruct.ElectricPricePlanWebMapper;
import info.zhihui.ems.web.plan.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 电价方案业务编排
 */
@Service
@RequiredArgsConstructor
public class ElectricPricePlanBiz {

    private final ElectricPricePlanService electricPricePlanService;
    private final ElectricPricePlanWebMapper electricPricePlanWebMapper;

    /**
     * 查询电价方案列表
     */
    public List<ElectricPricePlanVo> findElectricPricePlanList(ElectricPricePlanQueryVo queryVo) {
        ElectricPricePlanQueryDto queryDto = electricPricePlanWebMapper.toElectricPricePlanQueryDto(queryVo);
        List<ElectricPricePlanBo> bos = electricPricePlanService.findList(queryDto);

        return electricPricePlanWebMapper.toElectricPricePlanVoList(bos);
    }

    /**
     * 查询方案详情
     */
    public ElectricPricePlanDetailVo getElectricPricePlan(Integer id) {
        ElectricPricePlanDetailBo detailBo = electricPricePlanService.getDetail(id);
        ElectricPricePlanDetailVo detailVo = electricPricePlanWebMapper.toElectricPricePlanDetailVo(detailBo);

        List<StepPriceVo> stepPriceVos = electricPricePlanWebMapper.toStepPriceVoList(detailBo.getStepPrices());
        detailVo.setStepPrices(stepPriceVos);
        return detailVo;
    }

    /**
     * 新增方案
     */
    public Integer addElectricPricePlan(ElectricPricePlanSaveVo saveVo) {
        ElectricPricePlanSaveDto saveDto = electricPricePlanWebMapper.toElectricPricePlanSaveDto(saveVo);
        return electricPricePlanService.add(saveDto);
    }

    /**
     * 编辑方案
     */
    public void updateElectricPricePlan(Integer id, ElectricPricePlanSaveVo saveVo) {
        ElectricPricePlanSaveDto saveDto = electricPricePlanWebMapper.toElectricPricePlanSaveDto(saveVo);

        saveDto.setId(id);
        electricPricePlanService.edit(saveDto);
    }

    /**
     * 删除方案
     */
    public void deleteElectricPricePlan(Integer id) {
        electricPricePlanService.del(id);
    }

    /**
     * 获取默认阶梯电价
     */
    public List<StepPriceVo> getDefaultStepPrice() {
        return electricPricePlanWebMapper.toStepPriceVoList(electricPricePlanService.getDefaultStepPrice());
    }

    /**
     * 更新默认阶梯电价
     */
    public void updateDefaultStepPrice(List<StepPriceVo> stepPriceVos) {
        electricPricePlanService.editDefaultStepPrice(electricPricePlanWebMapper.toStepPriceBoList(stepPriceVos));
    }

    /**
     * 获取默认时间段
     */
    public List<ElectricPriceTimeSettingVo> getDefaultElectricTime() {
        List<ElectricPriceTimeDto> dtoList = electricPricePlanService.getElectricTime();
        return electricPricePlanWebMapper.toElectricPriceTimeSettingVoList(dtoList);
    }

    /**
     * 更新默认时间段
     */
    public void updateDefaultElectricTime(List<ElectricPriceTimeSettingVo> timeList) {
        List<ElectricPriceTimeDto> dtoList = electricPricePlanWebMapper.toElectricPriceTimeDtoList(timeList);
        electricPricePlanService.editElectricTime(dtoList);
    }

    /**
     * 获取默认电价配置
     */
    public List<ElectricPriceTypeVo> getDefaultElectricPrice() {
        List<ElectricPriceTypeDto> dtoList = electricPricePlanService.getElectricPrice();
        return electricPricePlanWebMapper.toElectricPriceTypeVoList(dtoList);
    }

    /**
     * 更新默认电价配置
     */
    public void updateDefaultElectricPrice(List<ElectricPriceTypeVo> priceList) {
        List<ElectricPriceTypeDto> dtoList = electricPricePlanWebMapper.toElectricPriceTypeDtoList(priceList);
        electricPricePlanService.editElectricPrice(dtoList == null ? Collections.emptyList() : dtoList);
    }
}
