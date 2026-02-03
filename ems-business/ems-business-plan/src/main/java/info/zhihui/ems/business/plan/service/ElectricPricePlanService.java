package info.zhihui.ems.business.plan.service;

import info.zhihui.ems.business.plan.bo.ElectricPricePlanBo;
import info.zhihui.ems.business.plan.bo.ElectricPricePlanDetailBo;
import info.zhihui.ems.business.plan.bo.StepPriceBo;
import info.zhihui.ems.business.plan.dto.ElectricPriceTimeDto;
import info.zhihui.ems.business.plan.dto.ElectricPriceTypeDto;
import info.zhihui.ems.business.plan.dto.ElectricPricePlanQueryDto;
import info.zhihui.ems.business.plan.dto.ElectricPricePlanSaveDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * @author jerryxiaosa
 */
public interface ElectricPricePlanService {

    /**
     * 根据查询条件获取电价方案列表
     * @param query 查询条件
     * @return 电价方案列表
     */
    List<ElectricPricePlanBo> findList(@NotNull ElectricPricePlanQueryDto query);

    /**
     * 获取电价方案详细信息
     * @param id 方案ID
     * @return 方案详细信息
     */
    ElectricPricePlanDetailBo getDetail(@NotNull Integer id);

    /**
     * 新增电价方案
     * @param dto 新增数据传输对象
     * @return 新增后的方案ID
     */
    Integer add(@Valid @NotNull ElectricPricePlanSaveDto dto);

    /**
     * 编辑电价方案
     * @param dto 编辑数据传输对象
     */
    void edit(@Valid @NotNull ElectricPricePlanSaveDto dto);

    /**
     * 删除电价方案
     * @param id 方案ID
     */
    void del(@NotNull Integer id);

    /**
     * 获取默认阶梯电价配置
     * @return 阶梯电价列表
     */
    List<StepPriceBo> getDefaultStepPrice();

    /**
     * 编辑默认阶梯电价配置
     * @param boList 阶梯电价列表
     */
    void editDefaultStepPrice(@NotEmpty List<StepPriceBo> boList);

    /**
     * 获取尖峰平谷深谷时间段配置
     * @return 时间段列表
     */
    List<ElectricPriceTimeDto> getElectricTime();

    /**
     * 编辑尖峰平谷深谷时间段配置
     * @param dtoList 时间段列表
     */
    void editElectricTime(@Valid @NotEmpty List<ElectricPriceTimeDto> dtoList);

    /**
     * 获取基础尖峰平谷深谷电价配置
     * @return 电价类型列表
     */
    List<ElectricPriceTypeDto> getElectricPrice();

    /**
     * 编辑基础尖峰平谷电价配置
     * @param boList 电价类型列表
     */
    void editElectricPrice(@NotEmpty List<ElectricPriceTypeDto> boList);
}