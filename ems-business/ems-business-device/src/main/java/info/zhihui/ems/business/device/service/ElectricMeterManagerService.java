package info.zhihui.ems.business.device.service;

import info.zhihui.ems.business.device.dto.*;
import info.zhihui.ems.common.enums.ElectricPricePeriodEnum;
import info.zhihui.ems.common.enums.WarnTypeEnum;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.exception.NotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 电表管理服务接口
 * 提供电表的增删改、状态控制、计费管理等核心功能
 *
 * @author jerryxiaosa
 */
public interface ElectricMeterManagerService {

    /**
     * 新增电表
     *
     * @param dto 电表保存数据传输对象
     * @return 新增电表的ID
     * @throws BusinessRuntimeException 当业务规则验证失败时
     */
    Integer add(@Valid @NotNull ElectricMeterCreateDto dto);

    /**
     * 更新电表本身的基础信息
     *
     * @param dto 电表保存数据传输对象
     * @throws NotFoundException        当电表不存在时
     * @throws BusinessRuntimeException 当业务规则验证失败时
     */
    void update(@Valid @NotNull ElectricMeterUpdateDto dto);

    /**
     * 删除电表
     *
     * @param id 电表ID
     * @throws BusinessRuntimeException 当电表已开户时
     */
    void delete(@NotNull Integer id);

    /**
     * 设置电表开关状态（开闸/关闸）
     * 无论数据库状态是否一致，均会执行开关闸命令，避免因数据库状态不一致导致操作异常。
     *
     * @param electricMeterSwitchStatusDto 电表开关状态数据传输对象
     * @throws NotFoundException        当电表不存在时
     * @throws BusinessRuntimeException 当电表离线或其他业务规则验证失败时
     */
    void setSwitchStatus(@Valid @NotNull ElectricMeterSwitchStatusDto electricMeterSwitchStatusDto);

    /**
     * 设置电费尖峰平谷时间段
     *
     * @param meterTimeDto 电表时间数据传输对象
     */
    void setElectricTime(@Valid @NotNull ElectricMeterTimeDto meterTimeDto);

    /**
     * 设置电表保电模式
     * 保电模式下，电表在欠费时不会自动断电
     *
     * @param meterIds  电表ID列表
     * @param isProtect 是否启用保电模式
     * @throws BusinessRuntimeException 当电表离线、预付费或未开户时
     */
    void setProtectModel(@NotEmpty List<Integer> meterIds, boolean isProtect);

    /**
     * 配置电表计费计划
     *
     * @param meterIds       电表ID列表
     * @param pricePlanId    计费计划ID
     * @throws BusinessRuntimeException 当电表离线或其他业务规则验证失败时
     */
    void setMeterPricePlan(@NotEmpty List<Integer> meterIds, @NotNull Integer pricePlanId);

    /**
     * 配置电表预警计划
     * @param meterWarnPlanDto 电表预警计划数据传输对象
     */
    void setMeterWarnPlan(@NotNull @Valid ElectricMeterWarnPlanDto meterWarnPlanDto);

    /**
     * 批量设置电表预警等级
     *
     * @param meterIds 电表ID列表
     * @param warnType 预警的状态
     * @throws BusinessRuntimeException 当电表验证失败时
     */
    void setMeterWarnLevel(@NotEmpty List<Integer> meterIds, @NotNull WarnTypeEnum warnType);

    /**
     * 设置电表CT变比
     *
     * @param electricMeterCtDto CT变比设置参数
     * @return 新电表id
     * 说明：
     * - 不再判断原CT是否与目标CT相同，即使相同也会执行设备指令，
     * 以避免设备与数据库状态不一致导致的异常操作。
     * - 设备型号必须支持CT；已开户电表不允许修改CT。
     * @throws NotFoundException 当电表不存在时
     */
    Integer setMeterCt(@Valid @NotNull ElectricMeterCtDto electricMeterCtDto);

    /**
     * 同步电表在线状态
     *
     * @param onlineStatusDto 电表在线状态数据传输对象
     */
    void syncMeterOnlineStatus(@Valid @NotNull ElectricMeterOnlineStatusDto onlineStatusDto);

    /**
     * 获取电表用电量
     *
     * @param meterId  电表ID
     * @param typeList 电量类型
     * @return 各个电量类型的用电量，单位：千瓦时(kWh)
     * @throws NotFoundException 当电表不存在时
     */
    Map<ElectricPricePeriodEnum, BigDecimal> getMeterPower(@Valid @NotNull Integer meterId, @NotEmpty List<ElectricPricePeriodEnum> typeList);

    /**
     * 批量开户电表
     * 为电表绑定账户并设置初始余额
     *
     * @param meterOpenDto 电表开户传输对象
     */
    void openMeterAccount(@Valid @NotNull MeterOpenDto meterOpenDto);

    /**
     * 批量销户电表
     *
     * @param meterCancelDto 电表销户传输对象
     * @return 销户的电表余额
     */
    List<MeterCancelResultDto> cancelMeterAccount(@Valid @NotNull MeterCancelDto meterCancelDto);

    /**
     * 跨年或周期性重建电表阶梯记录（起点和历史偏移）
     *
     * @param resetDto 重建参数
     */
    void resetCurrentYearMeterStepRecord(@NotNull MeterStepResetDto resetDto);

}
