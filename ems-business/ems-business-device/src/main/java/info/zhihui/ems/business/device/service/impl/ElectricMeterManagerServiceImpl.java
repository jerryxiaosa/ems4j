package info.zhihui.ems.business.device.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.IdUtil;
import info.zhihui.ems.business.device.bo.ElectricMeterBo;
import info.zhihui.ems.business.device.bo.GatewayBo;
import info.zhihui.ems.business.device.constant.DeviceConstant;
import info.zhihui.ems.business.device.dto.*;
import info.zhihui.ems.business.device.entity.ElectricMeterEntity;
import info.zhihui.ems.business.device.entity.MeterCancelRecordEntity;
import info.zhihui.ems.business.device.entity.MeterStepEntity;
import info.zhihui.ems.business.device.entity.OpenMeterEntity;
import info.zhihui.ems.business.device.enums.ElectricSwitchStatusEnum;
import info.zhihui.ems.business.device.mapper.ElectricMeterMapper;
import info.zhihui.ems.business.device.qo.AccountMeterStepQo;
import info.zhihui.ems.business.device.qo.ElectricMeterBatchUpdateQo;
import info.zhihui.ems.business.device.qo.ElectricMeterResetAccountQo;
import info.zhihui.ems.business.device.repository.ElectricMeterRepository;
import info.zhihui.ems.business.device.repository.MeterCancelRecordRepository;
import info.zhihui.ems.business.device.repository.MeterStepRepository;
import info.zhihui.ems.business.device.repository.OpenMeterRepository;
import info.zhihui.ems.business.device.service.DeviceStatusSynchronizer;
import info.zhihui.ems.business.device.service.ElectricMeterInfoService;
import info.zhihui.ems.business.device.service.ElectricMeterManagerService;
import info.zhihui.ems.business.device.service.GatewayService;
import info.zhihui.ems.business.device.utils.DeviceUtil;
import info.zhihui.ems.business.finance.bo.BalanceBo;
import info.zhihui.ems.business.finance.dto.BalanceQueryDto;
import info.zhihui.ems.business.finance.dto.ElectricMeterDetailDto;
import info.zhihui.ems.business.finance.dto.ElectricMeterPowerRecordDto;
import info.zhihui.ems.business.finance.service.record.ElectricMeterPowerRecordService;
import info.zhihui.ems.business.finance.service.balance.BalanceService;
import info.zhihui.ems.business.finance.service.consume.MeterConsumeService;
import info.zhihui.ems.business.plan.bo.WarnPlanBo;
import info.zhihui.ems.business.plan.dto.ElectricPriceTimeDto;
import info.zhihui.ems.business.plan.service.ElectricPricePlanService;
import info.zhihui.ems.business.plan.service.WarnPlanService;
import info.zhihui.ems.business.plan.utils.ElectricPlanValidationUtil;
import info.zhihui.ems.common.enums.*;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.common.exception.ParamException;
import info.zhihui.ems.common.utils.JacksonUtil;
import info.zhihui.ems.common.utils.SerialNumberGeneratorUtil;
import info.zhihui.ems.components.context.RequestContext;
import info.zhihui.ems.foundation.integration.biz.command.dto.DeviceCommandAddDto;
import info.zhihui.ems.foundation.integration.biz.command.dto.DeviceCommandCancelDto;
import info.zhihui.ems.foundation.integration.biz.command.enums.CommandSourceEnum;
import info.zhihui.ems.foundation.integration.biz.command.enums.CommandTypeEnum;
import info.zhihui.ems.foundation.integration.biz.command.service.DeviceCommandService;
import info.zhihui.ems.foundation.integration.concrete.energy.dto.BaseElectricDeviceDto;
import info.zhihui.ems.foundation.integration.concrete.energy.dto.ElectricDeviceAddDto;
import info.zhihui.ems.foundation.integration.concrete.energy.dto.ElectricDeviceDegreeDto;
import info.zhihui.ems.foundation.integration.concrete.energy.dto.ElectricDeviceUpdateDto;
import info.zhihui.ems.foundation.integration.concrete.energy.service.EnergyService;
import info.zhihui.ems.foundation.integration.core.bo.DeviceModelBo;
import info.zhihui.ems.foundation.integration.core.service.DeviceModelService;
import info.zhihui.ems.foundation.integration.core.service.DeviceModuleContext;
import info.zhihui.ems.foundation.space.bo.SpaceBo;
import info.zhihui.ems.foundation.space.enums.SpaceTypeEnum;
import info.zhihui.ems.foundation.space.service.SpaceService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 电表管理服务接口
 * 提供电表的增删改、状态控制、计费管理等核心功能
 *
 * @author jerryxiaosa
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Validated
public class ElectricMeterManagerServiceImpl implements ElectricMeterManagerService {
    private final ElectricMeterInfoService electricMeterInfoService;
    private final ElectricMeterRepository repository;
    private final OpenMeterRepository openMeterRepository;
    private final MeterCancelRecordRepository meterCancelRecordRepository;
    private final ElectricMeterMapper mapper;
    private final ElectricPricePlanService electricPlanService;
    private final MeterConsumeService meterConsumeService;
    private final BalanceService balanceService;
    private final MeterStepRepository meterStepRepository;
    private final ElectricMeterPowerRecordService electricMeterPowerRecordService;
    private final RequestContext requestContext;
    private final GatewayService gatewayService;
    private final DeviceCommandService deviceCommandService;
    private final DeviceModuleContext deviceModuleContext;
    private final DeviceModelService deviceModelService;
    private final SpaceService spaceService;
    private final DeviceStatusSynchronizer<ElectricMeterBo> electricMeterOnlineStatusSynchronizer;
    private final WarnPlanService warnPlanService;

    /**
     * 新增电表
     *
     * @param dto 电表保存数据传输对象
     * @return 新增电表的ID
     * @throws BusinessRuntimeException 当业务规则验证失败时
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Integer add(@Valid @NotNull ElectricMeterCreateDto dto) {
        ElectricMeterEntity entity = validateAndSetEntity(dto);
        // 保存表数据
        saveNewMeterData(entity);

        // 同步设备到iot
        syncToIotPlatform(entity);

        // 下发尖峰平谷时间段
        setElectricTime(new ElectricMeterTimeDto()
                .setId(entity.getId())
                .setCommandSource(CommandSourceEnum.SYSTEM)
                .setTimeList(electricPlanService.getElectricTime())
        );

        // 设置ct变比
        setCtWhenAddNewMeter(entity.getId(), entity.getCt());

        return entity.getId();
    }

    /**
     * 更新电表本身的基础信息
     *
     * @param dto 电表保存数据传输对象
     * @throws NotFoundException        当电表不存在时
     * @throws BusinessRuntimeException 当业务规则验证失败时
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(@Valid @NotNull ElectricMeterUpdateDto dto) {
        ElectricMeterBo old = electricMeterInfoService.getDetail(dto.getId());
        ElectricMeterEntity entity = mapper.updateDtoToEntity(dto);

        checkAndSetUpdateInfo(entity, old);

        // 根据 dto.getCalculateType() 是否为 null 来决定 resetCalculateType 参数
        boolean resetCalculateType = dto.getCalculateType() == null;
        repository.updateWithCalculateTypeControl(entity, resetCalculateType);
    }

    /**
     * 删除电表
     *
     * @param id 电表ID
     * @throws BusinessRuntimeException 当电表已开户时
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(@NotNull Integer id) {
        ElectricMeterBo old = electricMeterInfoService.getDetail(id);

        // 已开户的电表无法删除
        if (old.getAccountId() != null) {
            throw new BusinessRuntimeException("该电表已开户，无法删除，请先销户");
        }

        repository.deleteById(id);
        deviceCommandService.cancelDeviceCommand(new DeviceCommandCancelDto()
                .setDeviceId(id)
                .setDeviceType(DeviceTypeEnum.ELECTRIC)
                .setReason("用户删除电表")
        );

        // 删除iot电表关联
        if (old.getIotId() != null) {
            EnergyService energyService = deviceModuleContext.getService(EnergyService.class, old.getOwnAreaId());
            energyService.delDevice(new BaseElectricDeviceDto().setDeviceId(old.getIotId()).setAreaId(old.getOwnAreaId()));
        }

    }

    /**
     * 设置电表开关状态（开闸/关闸）
     * 无论数据库状态是否一致，均会执行开关闸命令，避免因数据库状态不一致导致操作异常。
     *
     * @param electricMeterSwitchStatusDto 电表开关状态数据传输对象
     * @throws NotFoundException        当电表不存在时
     * @throws BusinessRuntimeException 当电表离线或其他业务规则验证失败时
     */
    @Override
    public void setSwitchStatus(@Valid @NotNull ElectricMeterSwitchStatusDto electricMeterSwitchStatusDto) {
        // 验证电表基本信息
        ElectricMeterBo meter = validateMeterForSwitchOperation(electricMeterSwitchStatusDto.getId());

        // 记录当前状态与目标状态，始终执行操作以消除状态不一致
        ElectricSwitchStatusEnum currentStatus = getCurrentSwitchStatus(meter);
        log.info("设备{}当前状态：{}，目标状态：{}，开始执行开关闸命令",
                meter.getMeterNo(), currentStatus.getInfo(), electricMeterSwitchStatusDto.getSwitchStatus().getInfo());

        // 更新电表状态
        updateMeterSwitchStatus(meter.getId(), electricMeterSwitchStatusDto.getSwitchStatus());

        // 执行开关闸操作
        performSwitchOperation(meter, electricMeterSwitchStatusDto.getSwitchStatus(), electricMeterSwitchStatusDto.getCommandSource());

        log.info("设备{}开关闸操作完成，目标状态：{}（原状态：{}）",
                meter.getMeterNo(), electricMeterSwitchStatusDto.getSwitchStatus().getInfo(), currentStatus.getInfo());
    }

    /**
     * 验证电表是否可以进行开关闸操作
     */
    private ElectricMeterBo validateMeterForSwitchOperation(Integer id) {
        ElectricMeterBo meter = electricMeterInfoService.getDetail(id);
        if (meter.getIotId() == null) {
            throw new BusinessRuntimeException(
                    String.format("设备%s异常，请联系管理员处理", meter.getMeterNo()));
        }

        if (!BooleanUtil.isTrue(meter.getIsOnline())) {
            throw new BusinessRuntimeException(
                    String.format("设备%s处于离线状态，请重新选择", meter.getMeterNo()));
        }

        return meter;
    }

    /**
     * 获取电表当前开关状态
     */
    private ElectricSwitchStatusEnum getCurrentSwitchStatus(ElectricMeterBo meter) {
        return Boolean.TRUE.equals(meter.getIsCutOff())
                ? ElectricSwitchStatusEnum.OFF
                : ElectricSwitchStatusEnum.ON;
    }

    /**
     * 执行开关闸操作
     */
    private void performSwitchOperation(ElectricMeterBo meter, ElectricSwitchStatusEnum switchStatus, CommandSourceEnum commandSource) {
        String commandData = JacksonUtil.toJson(new BaseElectricDeviceDto()
                .setDeviceId(meter.getIotId())
                .setAreaId(meter.getOwnAreaId()));
        log.info("执行设备{}，{}命令：{}", meter.getMeterNo(), switchStatus.getInfo(), commandData);

        MeterCommandDto ctCommandDto = new MeterCommandDto()
                .setMeter(meter)
                .setCommandData(commandData)
                .setCommandSource(commandSource);

        switch (switchStatus) {
            case ON:
                ctCommandDto.setCommandType(CommandTypeEnum.ENERGY_ELECTRIC_TURN_ON);
                break;
            case OFF:
                ctCommandDto.setCommandType(CommandTypeEnum.ENERGY_ELECTRIC_TURN_OFF);
                break;
            default:
                throw new BusinessRuntimeException(
                        String.format("不支持的开关状态：%s", switchStatus));
        }

        saveMeterCommandAndRun(ctCommandDto);
    }

    /**
     * 更新电表开关状态
     */
    private void updateMeterSwitchStatus(Integer meterId, ElectricSwitchStatusEnum switchStatus) {
        boolean isCutOff = ElectricSwitchStatusEnum.OFF.equals(switchStatus);
        ElectricMeterEntity updateEntity = new ElectricMeterEntity()
                .setId(meterId)
                .setIsCutOff(isCutOff);
        repository.updateById(updateEntity);
    }

    /**
     * 设置电费尖峰平谷时间段
     *
     * @param electricMeterTimeDto 电表时间数据传输对象
     */
    @Override
    public void setElectricTime(ElectricMeterTimeDto electricMeterTimeDto) {
        ElectricMeterBo meter = electricMeterInfoService.getDetail(electricMeterTimeDto.getId());

        List<ElectricPriceTimeDto> validElectricPlanTime = ElectricPlanValidationUtil.getValidElectricPlanTime(electricMeterTimeDto.getTimeList());
        String commandData = JacksonUtil.toJson(validElectricPlanTime);
        log.info("生成电表尖峰平谷时间段命令数据：{}", commandData);

        MeterCommandDto commandDto = new MeterCommandDto()
                .setMeter(meter)
                .setCommandType(CommandTypeEnum.ENERGY_ELECTRIC_PRICE_TIME)
                .setCommandSource(electricMeterTimeDto.getCommandSource())
                .setCommandData(commandData);
        saveMeterCommandAndRun(commandDto);
    }

    /**
     * 设置电表保电模式
     * 保电模式下，电表在欠费时不会自动断电
     *
     * @param meterIds  电表ID列表
     * @param isProtect 是否启用保电模式
     * @throws BusinessRuntimeException 当电表离线、预付费或未开户时
     */
    @Override
    public void setProtectModel(@NotEmpty List<Integer> meterIds, boolean isProtect) {
        checkMeter(meterIds);

        ElectricMeterBatchUpdateQo updateQo = createBaseUpdateQo(meterIds);
        updateQo.setProtectedModel(isProtect);
        int affectedRows = repository.batchUpdate(updateQo);
        if (affectedRows != meterIds.size()) {
            log.warn("批量设保电模式{}，没有全部成功，请检查对应电表{}", isProtect, meterIds);
        }
    }

    private ElectricMeterBatchUpdateQo createBaseUpdateQo(List<Integer> meterIds) {
        return new ElectricMeterBatchUpdateQo()
                .setMeterIds(meterIds)
                .setUpdateUser(requestContext.getUserId())
                .setUpdateUserName(requestContext.getUserRealName())
                .setUpdateTime(LocalDateTime.now());
    }

    private void checkMeter(List<Integer> meterIds) {
        List<ElectricMeterBo> meterList = electricMeterInfoService.findList(new ElectricMeterQueryDto().setInIds(meterIds));
        if (meterIds.size() != meterList.size()) {
            throw new BusinessRuntimeException("部分电表数据不存在或已被删除");
        }

        for (ElectricMeterBo meter : meterList) {
            if (!BooleanUtil.isTrue(meter.getIsPrepay())) {
                throw new BusinessRuntimeException("只支持预付费电表" + ":" + meter.getMeterNo() + "不符合要求");
            }
            if (meter.getAccountId() == null) {
                throw new BusinessRuntimeException(meter.getMeterNo() + "尚未开户，无法操作");
            }
        }
    }

    /**
     * 获取电表用电量
     *
     * @param meterId  电表ID
     * @param typeList 电量类型
     * @return 各个电量类型的用电量，单位：千瓦时(kWh)
     * @throws NotFoundException 当电表不存在时
     */
    @Override
    public Map<ElectricPricePeriodEnum, BigDecimal> getMeterPower(@Valid @NotNull Integer meterId, @NotEmpty List<ElectricPricePeriodEnum> typeList) {
        ElectricMeterBo meter = electricMeterInfoService.getDetail(meterId);
        EnergyService energyService = deviceModuleContext.getService(EnergyService.class, meter.getOwnAreaId());

        Map<ElectricPricePeriodEnum, BigDecimal> res = new HashMap<>();
        for (ElectricPricePeriodEnum type : typeList) {
            ElectricDeviceDegreeDto dto = new ElectricDeviceDegreeDto();
            dto.setType(type)
                    .setDeviceId(meter.getIotId())
                    .setAreaId(meter.getOwnAreaId());
            BigDecimal power = energyService.getMeterEnergy(dto);
            res.put(type, power);
        }

        return res;
    }

    private void setMeterWarnPlanDirectly(List<Integer> meterIds, Integer warnPlanId) {
        checkMeter(meterIds);

        ElectricMeterBatchUpdateQo updateQo = createBaseUpdateQo(meterIds);
        updateQo.setWarnPlanId(warnPlanId);
        int affectedRows = repository.batchUpdate(updateQo);
        if (affectedRows != meterIds.size()) {
            log.warn("批量设置警告计划{}，没有全部成功，请检查对应电表{}", warnPlanId, meterIds);
        }
    }

    /**
     * 批量设置电表预警等级
     *
     * @param meterIds 电表ID列表
     * @param warnType 预警的状态
     * @throws BusinessRuntimeException 当电表验证失败时
     */
    @Override
    public void setMeterWarnLevel(@NotEmpty List<Integer> meterIds, @NotNull WarnTypeEnum warnType) {
        checkMeter(meterIds);

        ElectricMeterBatchUpdateQo updateQo = createBaseUpdateQo(meterIds);
        updateQo.setWarnType(warnType.name());
        int affectedRows = repository.batchUpdate(updateQo);
        if (affectedRows != meterIds.size()) {
            log.warn("批量设置警告等级{}，没有全部成功，请检查对应电表{}", warnType, meterIds);
        }
    }

    /**
     * 配置电表计费计划
     *
     * @param meterIds    电表ID列表
     * @param pricePlanId 计费计划ID
     * @throws BusinessRuntimeException 当电表离线或其他业务规则验证失败时
     */
    public void setMeterPricePlan(@NotEmpty List<Integer> meterIds, @NotNull Integer pricePlanId) {
        checkMeter(meterIds);

        ElectricMeterBatchUpdateQo updateQo = createBaseUpdateQo(meterIds);
        updateQo.setPricePlanId(pricePlanId);
        int affectedRows = repository.batchUpdate(updateQo);
        if (affectedRows != meterIds.size()) {
            log.warn("批量设置电价方案{}，没有全部成功，请检查对应电表{}", pricePlanId, meterIds);
        }
    }

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
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer setMeterCt(@Valid @NotNull ElectricMeterCtDto electricMeterCtDto) {
        ElectricMeterBo meter = electricMeterInfoService.getDetail(electricMeterCtDto.getMeterId());
        // 无论原CT是否相同，均执行以下流程，以确保设备与数据库状态一致
        log.info("准备执行CT变比更新，电表ID: {}，原CT: {}，目标CT: {}",
                electricMeterCtDto.getMeterId(),
                meter.getCt(),
                electricMeterCtDto.getCt());

        // 已开户的电表不能修改ct
        // 要销户后再开户
        checkUpdateCt(meter);

        meter.setCt(electricMeterCtDto.getCt());

        // 未开户的电表，删除重建
        // 因为调整了ct之后电量会变化；为防止状态不一致，即使CT未变也执行该流程
        // 注意：可能对统计造成影响
        Integer newMeterId = deleteAndSaveNewMeterForCt(meter);

        MeterCommandDto ctCommandDto = new MeterCommandDto()
                .setMeter(meter)
                .setCommandType(CommandTypeEnum.ENERGY_ELECTRIC_CT)
                .setCommandSource(CommandSourceEnum.SYSTEM)
                .setCommandData(meter.getCt().toPlainString());
        saveMeterCommandAndRun(ctCommandDto);

        return newMeterId;
    }

    private void checkUpdateCt(ElectricMeterBo meter) {
        DeviceModelBo deviceModel = deviceModelService.getDetail(meter.getModelId());
        Boolean isCt = DeviceUtil.getProperty(
                deviceModel.getModelProperty(),
                DeviceConstant.IS_CT,
                Boolean.class
        );
        if (!BooleanUtil.isTrue(isCt)) {
            throw new BusinessRuntimeException("该电表不支持ct变比");
        }
        if (meter.getAccountId() != null) {
            throw new BusinessRuntimeException("已开户电表不允许修改ct");
        }
    }

    // ct变化需要当成新表处理
    private Integer deleteAndSaveNewMeterForCt(ElectricMeterBo meter) {
        repository.deleteById(meter.getId());

        // 当作新表插入
        ElectricMeterEntity entity = mapper.boToEntity(meter);
        entity.setId(null);
        repository.insert(entity);

        return entity.getId();
    }

    /**
     * 同步电表在线状态
     *
     * @param onlineStatusDto 电表在线状态数据传输对象
     */
    @Override
    public void syncMeterOnlineStatus(@Valid @NotNull ElectricMeterOnlineStatusDto onlineStatusDto) {
        electricMeterOnlineStatusSynchronizer.syncStatus(
                () -> electricMeterInfoService.getDetail(onlineStatusDto.getMeterId()),
                new DeviceStatusSyncRequestDto()
                        .setForce(onlineStatusDto.getForce())
                        .setOnlineStatus(onlineStatusDto.getOnlineStatus())
        );
    }

    /**
     * 同步电表开合闸状态
     *
     * @param switchStatusSyncDto 电表开合闸状态同步参数
     */
    @Override
    public void syncMeterSwitchStatus(@Valid @NotNull ElectricMeterSwitchStatusSyncDto switchStatusSyncDto) {
        ElectricMeterBo meter = electricMeterInfoService.getDetail(switchStatusSyncDto.getMeterId());

        EnergyService energyService = deviceModuleContext.getService(EnergyService.class, meter.getOwnAreaId());
        Boolean isCutOff = energyService.isCutOff(new BaseElectricDeviceDto()
                .setDeviceId(meter.getIotId())
                .setAreaId(meter.getOwnAreaId()));

        if (!Objects.equals(meter.getIsCutOff(), isCutOff)) {
            ElectricMeterEntity updateEntity = new ElectricMeterEntity()
                    .setId(switchStatusSyncDto.getMeterId())
                    .setIsCutOff(isCutOff);
            repository.updateById(updateEntity);
        }
    }

    /**
     * 批量开户电表
     * 为电表绑定账户并设置初始余额
     *
     * @param meterOpenDto 电表开户传输对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void openMeterAccount(@Valid @NotNull MeterOpenDto meterOpenDto) {
        log.info("开始执行电表开户操作，账户ID: {}, 电表数量: {}", meterOpenDto.getAccountId(), meterOpenDto.getMeterOpenDetail().size());

        try {
            // 参数校验
            validateOpenAccountParams(meterOpenDto);

            // 初始化账户
            initMeterAccount(meterOpenDto);

            // 配置电表方案
            configureMeterPlans(meterOpenDto);

            // 处理电表详细配置
            processMeterDetailConfigurations(meterOpenDto);

            log.info("电表开户操作完成，账户ID: {}", meterOpenDto.getAccountId());
        } catch (Exception e) {
            log.error("电表开户系统异常，账户ID: {}", meterOpenDto.getAccountId(), e);
            throw new BusinessRuntimeException("电表开户操作失败：" + e.getMessage());
        }
    }

    /**
     * 校验开户参数
     */
    private void validateOpenAccountParams(MeterOpenDto meterOpenDto) {
        if (ElectricAccountTypeEnum.QUANTITY.equals(meterOpenDto.getElectricAccountType())) {
            if (meterOpenDto.getElectricPricePlanId() == null) {
                throw new ParamException("电价方案不能为空");
            }
            if (meterOpenDto.getWarnPlanId() == null) {
                throw new ParamException("预警方案不能为空");
            }
        }

        List<Integer> meterIds = meterOpenDto.getMeterOpenDetail().stream()
                .map(MeterOpenDetailDto::getMeterId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        List<ElectricMeterBo> meterList = electricMeterInfoService.findList(new ElectricMeterQueryDto().setInIds(meterIds));
        if (meterOpenDto.getMeterOpenDetail().size() != meterList.size()) {
            log.error("电表数据校验失败，期望数量: {}, 实际数量: {}, 电表ID列表: {}",
                    meterOpenDto.getMeterOpenDetail().size(), meterList.size(), meterIds);
            throw new BusinessRuntimeException("部分电表数据不存在或已被删除");
        }

        validateMeterStatus(meterList);
    }

    /**
     * 校验电表状态
     */
    private void validateMeterStatus(List<ElectricMeterBo> meterList) {
        for (ElectricMeterBo meterBo : meterList) {
            if (meterBo.getAccountId() != null) {
                throw new BusinessRuntimeException(meterBo.getMeterNo() + "已开户，请勿重复开户");
            }

            if (!BooleanUtil.isTrue(meterBo.getIsPrepay())) {
                throw new BusinessRuntimeException("只有预付费电表可以开户");
            }

            if (!BooleanUtil.isTrue(meterBo.getIsOnline())) {
                throw new BusinessRuntimeException(meterBo.getMeterName() + "电表离线，无法开户");
            }

        }
    }

    /**
     * 初始化电表账户
     */
    private void initMeterAccount(MeterOpenDto meterOpenDto) {

        // 逐个更新电表的账户信息
        for (MeterOpenDetailDto detail : meterOpenDto.getMeterOpenDetail()) {
            ElectricMeterEntity entity = new ElectricMeterEntity()
                    .setId(detail.getMeterId())
                    .setAccountId(meterOpenDto.getAccountId());
            repository.updateById(entity);

            if (ElectricAccountTypeEnum.QUANTITY.equals(meterOpenDto.getElectricAccountType())) {
                balanceService.initElectricMeterBalance(detail.getMeterId(), meterOpenDto.getAccountId());
            }
        }

        log.info("数据库开户操作完成，账户ID: {}, 电表数量: {}", meterOpenDto.getAccountId(), meterOpenDto.getMeterOpenDetail().size());
    }

    /**
     * 配置电表方案
     */
    private void configureMeterPlans(MeterOpenDto meterOpenDto) {
        List<Integer> meterIds = meterOpenDto.getMeterOpenDetail().stream()
                .map(MeterOpenDetailDto::getMeterId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        log.info("开始配置电表方案，账户ID: {}, 电表数量: {}", meterOpenDto.getAccountId(), meterIds.size());

        configurePricePlan(meterOpenDto, meterIds);
        configureProtectModel(meterOpenDto, meterIds);

        // 按需电表设置表上的预警计划
        if (ElectricAccountTypeEnum.QUANTITY.equals(meterOpenDto.getElectricAccountType())) {
            setMeterWarnPlan(new ElectricMeterWarnPlanDto()
                    .setWarnPlanId(meterOpenDto.getWarnPlanId())
                    .setMeterIds(meterIds)
            );
        }

        log.info("电表方案配置完成，账户ID: {}", meterOpenDto.getAccountId());
    }

    /**
     * 配置价格方案
     * 只有按需付费的电表才需要配置
     */
    private void configurePricePlan(MeterOpenDto meterOpenDto, List<Integer> meterIds) {
        Integer pricePlanId = ElectricAccountTypeEnum.QUANTITY.equals(meterOpenDto.getElectricAccountType())
                ? meterOpenDto.getElectricPricePlanId() : null;
        if (pricePlanId != null) {
            setMeterPricePlan(meterIds, pricePlanId);
        }
    }

    /**
     * 配置电表预警计划，会根据账户金额来设置预警等级
     * 只有按需付费电表才需要配置
     *
     * @param meterWarnPlanDto 电表预警计划数据传输对象
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void setMeterWarnPlan(@NotNull @Valid ElectricMeterWarnPlanDto meterWarnPlanDto) {
        // 绑定预警方案
        setMeterWarnPlanDirectly(meterWarnPlanDto.getMeterIds(), meterWarnPlanDto.getWarnPlanId());

        // 读取预警方案阈值
        WarnPlanBo warnPlanBo = warnPlanService.getDetail(meterWarnPlanDto.getWarnPlanId());
        BigDecimal firstLevel = warnPlanBo.getFirstLevel();
        BigDecimal secondLevel = warnPlanBo.getSecondLevel();

        // 基于余额分组计算预警等级
        EnumMap<WarnTypeEnum, List<Integer>> groups = groupWarnLevelsByBalance(meterWarnPlanDto.getMeterIds(), firstLevel, secondLevel);

        // 分组批量设置预警等级（最多三次）
        applyGroupedWarnLevels(groups);
    }

    /**
     * 按余额阈值分组计算电表预警等级
     */
    private EnumMap<WarnTypeEnum, List<Integer>> groupWarnLevelsByBalance(List<Integer> meterIds,
                                                                          BigDecimal firstLevel,
                                                                          BigDecimal secondLevel) {
        EnumMap<WarnTypeEnum, List<Integer>> groups = new EnumMap<>(WarnTypeEnum.class);
        groups.put(WarnTypeEnum.NONE, new ArrayList<>());
        groups.put(WarnTypeEnum.FIRST, new ArrayList<>());
        groups.put(WarnTypeEnum.SECOND, new ArrayList<>());

        for (Integer meterId : meterIds) {
            BigDecimal balance = null;
            try {
                BalanceBo balanceBo = balanceService.query(new BalanceQueryDto()
                        .setBalanceRelationId(meterId)
                        .setBalanceType(BalanceTypeEnum.ELECTRIC_METER));
                balance = balanceBo.getBalance();
            } catch (NotFoundException e) {
                // 缺失余额记录视为 NONE
            }

            WarnTypeEnum type = computeWarnType(balance, firstLevel, secondLevel);
            groups.get(type).add(meterId);
        }

        log.info("预警等级分组完成：NONE={}, FIRST={}, SECOND={}",
                groups.get(WarnTypeEnum.NONE).size(),
                groups.get(WarnTypeEnum.FIRST).size(),
                groups.get(WarnTypeEnum.SECOND).size());
        return groups;
    }

    /**
     * 计算单表预警等级
     */
    private WarnTypeEnum computeWarnType(BigDecimal balance,
                                         BigDecimal firstLevel,
                                         BigDecimal secondLevel) {
        if (balance == null) {
            return WarnTypeEnum.NONE;
        }
        if (balance.compareTo(secondLevel) <= 0) {
            return WarnTypeEnum.SECOND;
        }
        if (balance.compareTo(firstLevel) <= 0) {
            return WarnTypeEnum.FIRST;
        }
        return WarnTypeEnum.NONE;
    }

    /**
     * 按分组批量设置预警等级
     */
    private void applyGroupedWarnLevels(EnumMap<WarnTypeEnum, List<Integer>> groups) {
        for (WarnTypeEnum level : List.of(WarnTypeEnum.NONE, WarnTypeEnum.FIRST, WarnTypeEnum.SECOND)) {
            List<Integer> ids = groups.get(level);
            if (!CollectionUtils.isEmpty(ids)) {
                setMeterWarnLevel(ids, level);
            }
        }
    }

    /**
     * 配置保护模式
     */
    private void configureProtectModel(MeterOpenDto meterOpenDto, List<Integer> meterIds) {
        if (ElectricAccountTypeEnum.MONTHLY.equals(meterOpenDto.getElectricAccountType())) {
            setProtectModel(meterIds, true);
        }
    }

    /**
     * 处理电表详细配置
     */
    private void processMeterDetailConfigurations(MeterOpenDto meterOpenDto) {
        LocalDateTime nowTime = LocalDateTime.now();
        // 处理电表数据记录
        List<Integer> meterIds = meterOpenDto.getMeterOpenDetail().stream().map(MeterOpenDetailDto::getMeterId).toList();
        Map<Integer, ElectricMeterPowerDto> meterPowerRecordMap = getPowerData(meterIds, nowTime);

        for (MeterOpenDetailDto meterOpenDetailDto : meterOpenDto.getMeterOpenDetail()) {
            ElectricMeterPowerDto meterPowerRecord = meterPowerRecordMap.get(meterOpenDetailDto.getMeterId());

            // 记录开户时的信息
            saveElectricMeterAccountOpenRecord(meterOpenDetailDto, meterOpenDto.getAccountId(), meterPowerRecord, nowTime);
            // 创建电表阶梯记录
            BigDecimal historyPowerOffset = resolveHistoryPowerOffset(meterOpenDto, meterOpenDetailDto.getMeterId(), nowTime);
            saveElectricMeterStepRecord(meterOpenDetailDto, meterOpenDto.getAccountId(), meterPowerRecord, historyPowerOffset, nowTime);
            // 保存电表初始电量，供后续消费计算衔接
            saveInitialPowerRecord(meterOpenDetailDto, meterOpenDto, meterPowerRecord, nowTime);
        }
    }

    private void saveElectricMeterStepRecord(MeterOpenDetailDto meterOpenDetailDto, Integer accountId,
                                             ElectricMeterPowerDto meterPowerRecord, BigDecimal historyPowerOffset, LocalDateTime nowTime) {
        markPreviousMeterStepsAsHistory(accountId, meterOpenDetailDto.getMeterId());
        MeterStepEntity meterStepEntity = new MeterStepEntity()
                .setAccountId(accountId)
                .setMeterId(meterOpenDetailDto.getMeterId())
                .setMeterType(MeterTypeEnum.ELECTRIC.getCode())
                .setStepStartValue(meterPowerRecord.getPower())
                .setHistoryPowerOffset(Optional.ofNullable(historyPowerOffset).orElse(BigDecimal.ZERO))
                .setCurrentYear(nowTime.getYear())
                .setIsLatest(Boolean.TRUE);
        meterStepRepository.insert(meterStepEntity);
    }

    /**
     * 跨年或周期性重建电表阶梯记录（起点和历史偏移）
     *
     * @param resetDto 重建参数
     */
    @Override
    public void resetCurrentYearMeterStepRecord(@NotNull MeterStepResetDto resetDto) {
        ElectricMeterBo meter = electricMeterInfoService.getDetail(resetDto.getMeterId());
        if (meter.getAccountId() == null) {
            log.warn("电表{}未绑定账户，无法执行跨年阶梯重建", meter.getMeterNo());
            return;
        }

        MeterStepEntity latestStep = meterStepRepository.getOne(
                new AccountMeterStepQo()
                        .setAccountId(meter.getAccountId())
                        .setMeterId(resetDto.getMeterId())
                        .setMeterType(MeterTypeEnum.ELECTRIC.getCode())
        );

        int currentYear = LocalDateTime.now().getYear();
        if (latestStep != null && Objects.equals(latestStep.getCurrentYear(), currentYear)) {
            log.debug("电表{}已存在{}年阶梯记录，跳过重建", meter.getMeterNo(), currentYear);
            return;
        }

        BigDecimal stepStartValue = determineStepStartValue(resetDto.getMeterId());
        if (stepStartValue == null) {
            throw new BusinessRuntimeException("未获取到电表" + meter.getMeterNo() + "的阶梯起点电量");
        }

        markPreviousMeterStepsAsHistory(meter.getAccountId(), resetDto.getMeterId());

        MeterStepEntity meterStepEntity = new MeterStepEntity()
                .setAccountId(meter.getAccountId())
                .setMeterId(resetDto.getMeterId())
                .setMeterType(MeterTypeEnum.ELECTRIC.getCode())
                .setStepStartValue(stepStartValue)
                .setHistoryPowerOffset(BigDecimal.ZERO)
                .setCurrentYear(currentYear)
                .setIsLatest(Boolean.TRUE);
        meterStepRepository.insert(meterStepEntity);
        log.info("已为电表{}重建{}年度阶梯起点 {}", meter.getMeterNo(), currentYear, stepStartValue);
    }

    private void markPreviousMeterStepsAsHistory(Integer accountId, Integer meterId) {
        meterStepRepository.clearLatestFlag(new AccountMeterStepQo()
                .setAccountId(accountId)
                .setMeterId(meterId)
                .setMeterType(MeterTypeEnum.ELECTRIC.getCode()));
    }

    private BigDecimal determineStepStartValue(Integer meterId) {
        BigDecimal devicePower = readMeterPowerFromDevice(meterId);
        if (devicePower != null) {
            return devicePower;
        }
        return electricMeterPowerRecordService.findLatestPower(meterId);
    }

    private BigDecimal readMeterPowerFromDevice(Integer meterId) {
        try {
            Map<ElectricPricePeriodEnum, BigDecimal> powerMap = getMeterPower(meterId, List.of(
                    ElectricPricePeriodEnum.TOTAL));
            return powerMap.get(ElectricPricePeriodEnum.TOTAL);
        } catch (Exception ex) {
            log.warn("读取电表{}实时电量失败，将回退至历史记录", meterId, ex);
            return null;
        }
    }

    /**
     * 获取电表历史阶梯记录
     * 避免销户重开丢度阶梯信息
     */
    private BigDecimal resolveHistoryPowerOffset(MeterOpenDto meterOpenDto, Integer meterId, LocalDateTime nowTime) {
        if (!BooleanUtil.isTrue(meterOpenDto.getInheritHistoryPower())) {
            return BigDecimal.ZERO;
        }
        MeterCancelRecordEntity historyRecord = fetchLatestMeterHistory(meterId);
        if (historyRecord == null) {
            log.debug("电表{}未找到可继承的历史阶梯记录", meterId);
            return BigDecimal.ZERO;
        }
        LocalDateTime showTime = historyRecord.getShowTime();
        if (showTime == null || showTime.getYear() != nowTime.getYear()) {
            log.debug("电表{}历史阶梯记录年度({})与当前年度({})不一致，无法继承",
                    meterId,
                    showTime == null ? "null" : String.valueOf(showTime.getYear()),
                    nowTime.getYear());
            return BigDecimal.ZERO;
        }
        BigDecimal historyPowerTotal = Optional.ofNullable(historyRecord.getHistoryPowerTotal()).orElse(BigDecimal.ZERO);
        if (historyPowerTotal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        log.debug("电表{}继承历史阶梯累计用量: {}", meterId, historyPowerTotal);
        return historyPowerTotal;
    }

    private MeterCancelRecordEntity fetchLatestMeterHistory(Integer meterId) {
        if (meterId == null) {
            return null;
        }
        try {
            return meterCancelRecordRepository.selectLatestByMeter(meterId);
        } catch (Exception e) {
            log.warn("查询电表{}的历史阶梯记录失败", meterId, e);
            return null;
        }
    }

    private ElectricMeterDetailDto buildMeterDetailDto(Integer meterId) {
        ElectricMeterBo meterDetail = electricMeterInfoService.getDetail(meterId);

        MeterStepEntity stepEntity = meterStepRepository.getOne(
                new AccountMeterStepQo()
                        .setMeterId(meterId)
                        .setMeterType(MeterTypeEnum.ELECTRIC.getCode())
                        .setAccountId(meterDetail.getAccountId())
        );
        BigDecimal stepStartValue = stepEntity == null ? null : stepEntity.getStepStartValue();
        BigDecimal historyPowerOffset = Optional.ofNullable(stepEntity)
                .map(MeterStepEntity::getHistoryPowerOffset)
                .orElse(BigDecimal.ZERO);

        return new ElectricMeterDetailDto()
                .setMeterId(meterId)
                .setMeterName(meterDetail.getMeterName())
                .setMeterNo(meterDetail.getMeterNo())
                .setSpaceId(meterDetail.getSpaceId())
                .setIsCalculate(meterDetail.getIsCalculate())
                .setCalculateType(meterDetail.getCalculateType())
                .setIsPrepay(meterDetail.getIsPrepay())
                .setPricePlanId(meterDetail.getPricePlanId())
                .setCt(meterDetail.getCt())
                .setStepStartValue(stepStartValue)
                .setHistoryPowerOffset(historyPowerOffset)
                .setIsOnline(meterDetail.getIsOnline())
                .setIsCutOff(meterDetail.getIsCutOff());
    }

    /**
     * 保存开户瞬间的抄表记录，保证后续消费计算有起点
     * 为避免同一时刻多条记录产生顺序不确定，初始电量记录向后顺延 1 秒
     */
    private void saveInitialPowerRecord(MeterOpenDetailDto meterOpenDetailDto, MeterOpenDto meterOpenDto,
                                        ElectricMeterPowerDto meterPowerRecord, LocalDateTime nowTime) {
        ElectricMeterDetailDto meterDto = buildMeterDetailDto(meterOpenDetailDto.getMeterId());
        ElectricMeterPowerRecordDto recordDto = new ElectricMeterPowerRecordDto()
                .setOriginalReportId(mockOriginalReportId(meterOpenDetailDto.getMeterId(), meterOpenDto.getAccountId()))
                .setElectricMeterDetailDto(meterDto)
                .setAccountId(meterOpenDto.getAccountId())
                .setOwnerId(meterOpenDto.getOwnerId())
                .setOwnerType(meterOpenDto.getOwnerType())
                .setOwnerName(meterOpenDto.getOwnerName())
                .setElectricAccountType(meterOpenDto.getElectricAccountType())
                .setPower(meterPowerRecord.getPower())
                .setPowerHigher(meterPowerRecord.getPowerHigher())
                .setPowerHigh(meterPowerRecord.getPowerHigh())
                .setPowerLow(meterPowerRecord.getPowerLow())
                .setPowerLower(meterPowerRecord.getPowerLower())
                .setPowerDeepLow(meterPowerRecord.getPowerDeepLow())
                .setRecordTime(nowTime.plusSeconds(1))
                .setNeedConsume(false);
        meterConsumeService.savePowerRecord(recordDto);
    }

    private String mockOriginalReportId(Integer meterId, Integer accountId) {
        return String.format("ACCOUNT:%d-METER:%d-%s", accountId, meterId, UUID.randomUUID());
    }

    private Map<Integer, ElectricMeterPowerDto> getPowerData(List<Integer> meterIdList, LocalDateTime nowTime) {
        Map<Integer, ElectricMeterPowerDto> map = new HashMap<>();
        for (Integer meterId : meterIdList) {
            Map<ElectricPricePeriodEnum, BigDecimal> powerMap = getMeterPower(meterId,
                    List.of(ElectricPricePeriodEnum.HIGHER,
                            ElectricPricePeriodEnum.HIGH,
                            ElectricPricePeriodEnum.LOW,
                            ElectricPricePeriodEnum.LOWER,
                            ElectricPricePeriodEnum.DEEP_LOW)
            );
            BigDecimal power = Stream.of(
                            powerMap.get(ElectricPricePeriodEnum.HIGHER),
                            powerMap.get(ElectricPricePeriodEnum.HIGH),
                            powerMap.get(ElectricPricePeriodEnum.LOW),
                            powerMap.get(ElectricPricePeriodEnum.LOWER),
                            powerMap.get(ElectricPricePeriodEnum.DEEP_LOW))
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            ElectricMeterPowerDto powerDto = new ElectricMeterPowerDto()
                    .setMeterId(meterId)
                    .setPower(power)
                    .setPowerHigher(powerMap.get(ElectricPricePeriodEnum.HIGHER))
                    .setPowerHigh(powerMap.get(ElectricPricePeriodEnum.HIGH))
                    .setPowerLow(powerMap.get(ElectricPricePeriodEnum.LOW))
                    .setPowerLower(powerMap.get(ElectricPricePeriodEnum.LOWER))
                    .setPowerDeepLow(powerMap.get(ElectricPricePeriodEnum.DEEP_LOW))
                    .setRecordTime(nowTime);

            map.put(meterId, powerDto);
        }

        return map;
    }

    private void saveElectricMeterAccountOpenRecord(MeterOpenDetailDto meterOpenDetailDto, Integer accountId,
                                                    ElectricMeterPowerDto meterPowerRecord, LocalDateTime nowTime) {

        OpenMeterEntity openMeterEntity = new OpenMeterEntity()
                .setAccountId(accountId)
                .setMeterType(MeterTypeEnum.ELECTRIC.getCode())
                .setMeterId(meterOpenDetailDto.getMeterId())
                .setPower(meterPowerRecord.getPower())
                .setPowerHigher(meterPowerRecord.getPowerHigher())
                .setPowerHigh(meterPowerRecord.getPowerHigh())
                .setPowerLow(meterPowerRecord.getPowerLow())
                .setPowerLower(meterPowerRecord.getPowerLower())
                .setPowerDeepLow(meterPowerRecord.getPowerDeepLow())
                .setShowTime(nowTime);

        openMeterRepository.insert(openMeterEntity);
    }

    /**
     * 同步设备到iot平台
     */
    private void syncToIotPlatform(ElectricMeterEntity entity) {
        validateEntityForSync(entity);

        log.info("开始同步电表到IoT平台，电表ID: {}, 电表地址: {}", entity.getId(), entity.getMeterAddress());

        EnergyService energyService = deviceModuleContext.getService(EnergyService.class, entity.getOwnAreaId());
        ElectricDeviceAddDto addDto = createDeviceAddDto(entity);

        Integer iotId = energyService.addDevice(addDto);
        updateMeterIotId(entity.getId(), iotId);

        log.info("电表同步到IoT平台成功，电表ID: {}, IoT设备ID: {}", entity.getId(), iotId);
    }

    private void syncDeviceNoToIotPlatform(ElectricMeterBo old, String deviceNo) {
        if (old == null || old.getIotId() == null) {
            throw new BusinessRuntimeException("电表未同步到IoT平台，无法更新设备编号");
        }
        EnergyService energyService = deviceModuleContext.getService(EnergyService.class, old.getOwnAreaId());
        ElectricDeviceUpdateDto updateDto = new ElectricDeviceUpdateDto();
        updateDto.setDeviceNo(deviceNo)
                .setProductCode(old.getProductCode())
                .setDeviceId(old.getIotId())
                .setAreaId(old.getOwnAreaId());
        energyService.editDevice(updateDto);
    }

    /**
     * 验证实体数据
     */
    private void validateEntityForSync(ElectricMeterEntity entity) {
        if (entity == null) {
            throw new BusinessRuntimeException("电表实体不能为空");
        }
        if (entity.getId() == null) {
            throw new BusinessRuntimeException("电表ID不能为空");
        }
        if (entity.getOwnAreaId() == null) {
            throw new BusinessRuntimeException("电表所属区域不能为空");
        }
    }

    /**
     * 验证网关信息
     */
    private void validateGateway(GatewayBo gateway, Integer gatewayId) {
        if (gateway == null) {
            throw new BusinessRuntimeException("网关不存在，网关ID: " + gatewayId);
        }
        if (gateway.getIotId() == null) {
            throw new BusinessRuntimeException("网关未同步到IoT平台，网关ID: " + gatewayId);
        }
    }

    /**
     * 解析网关设备编号
     */
    private String resolveGatewayDeviceNo(ElectricMeterBo meter) {
        if (meter == null || meter.getGatewayId() == null) {
            throw new BusinessRuntimeException("网关信息有误，请重新选择");
        }
        GatewayBo gateway = gatewayService.getDetail(meter.getGatewayId());
        if (gateway == null) {
            throw new BusinessRuntimeException("网关不存在，网关ID: " + meter.getGatewayId());
        }
        if (StringUtils.isBlank(gateway.getDeviceNo())) {
            throw new BusinessRuntimeException("网关deviceNo不能为空");
        }
        return gateway.getDeviceNo();
    }

    /**
     * 创建设备添加DTO
     */
    private ElectricDeviceAddDto createDeviceAddDto(ElectricMeterEntity entity) {
        if (StringUtils.isBlank(entity.getProductCode())) {
            throw new BusinessRuntimeException("设备型号标识不能为空");
        }
        if (StringUtils.isBlank(entity.getDeviceNo())) {
            throw new BusinessRuntimeException("设备编号不能为空");
        }
        ElectricDeviceAddDto dto = new ElectricDeviceAddDto()
                .setDeviceNo(entity.getDeviceNo())
                .setProductCode(entity.getProductCode())
                .setAreaId(entity.getOwnAreaId());

        if (DeviceUtil.isNbCommunicateModel(entity.getCommunicateModel())) {
            if (StringUtils.isBlank(entity.getImei())) {
                throw new BusinessRuntimeException("NB设备的IMEI不能为空");
            }
            return dto;
        }

        // 非nb，需要走网关
        if (entity.getGatewayId() == null) {
            throw new BusinessRuntimeException("网关设备的网关ID不能为空");
        }

        GatewayBo gateway = gatewayService.getDetail(entity.getGatewayId());
        validateGateway(gateway, entity.getGatewayId());

        if (entity.getPortNo() == null) {
            throw new BusinessRuntimeException("网关设备的端口号不能为空");
        }

        if (entity.getMeterAddress() == null) {
            throw new BusinessRuntimeException("电表地址不能为空");
        }

        dto.setParentId(gateway.getIotId())
                .setPortNo(entity.getPortNo())
                .setMeterAddress(entity.getMeterAddress())
                .setSlaveAddress(entity.getMeterAddress());

        return dto;
    }

    /**
     * 更新电表IoT ID
     */
    private void updateMeterIotId(Integer meterId, Integer iotId) {
        ElectricMeterEntity updateEntity = new ElectricMeterEntity()
                .setId(meterId)
                .setIotId(iotId);

        int updateCount = repository.updateById(updateEntity);
        if (updateCount == 0) {
            throw new BusinessRuntimeException("更新电表IoT ID失败，电表可能已被删除");
        }
    }

    private void saveMeterCommandAndRun(MeterCommandDto meterCommandDto) {
        ElectricMeterBo meter = meterCommandDto.getMeter();
        String spaceName = Optional.ofNullable(spaceService.getDetail(meter.getSpaceId()))
                .map(SpaceBo::getName)
                .orElse("");

        DeviceCommandAddDto dto = new DeviceCommandAddDto()
                .setCommandType(meterCommandDto.getCommandType())
                .setCommandSource(CommandSourceEnum.USER)
                .setCommandData(meterCommandDto.getCommandData())
                .setDeviceType(DeviceTypeEnum.ELECTRIC)
                .setDeviceId(meter.getId())
                .setDeviceIotId(meter.getIotId().toString())
                .setDeviceNo(meter.getDeviceNo())
                .setDeviceName(meter.getMeterName())
                .setSpaceId(meter.getSpaceId())
                .setSpaceName(spaceName)
                .setAreaId(meter.getOwnAreaId())
                .setAccountId(meter.getAccountId())
                .setOperateUser(requestContext.getUserId())
                .setOperateUserName(requestContext.getUserRealName())
                .setEnsureSuccess(true);
        Integer commandId = deviceCommandService.saveDeviceCommand(dto);

        deviceCommandService.execDeviceCommand(commandId, meterCommandDto.getCommandSource());
    }

    /**
     * 校验并封装entity
     */
    private ElectricMeterEntity validateAndSetEntity(ElectricMeterCreateDto dto) {
        ElectricMeterEntity entity = mapper.saveDtoToEntity(dto);

        // 设备型号验证和属性设置
        DeviceModelBo deviceModel = validateAndSetDeviceModel(entity);

        // CT变比处理
        handleCtRatio(entity, deviceModel);

        // 验证预付费支持
        if (BooleanUtil.isTrue(dto.getIsPrepay())) {
            validatePrepaySupport(deviceModel);
        }

        // 通信模式验证和处理
        handleCommunicationMode(entity, dto);

        // 空间验证和设置
        validateAndSetSpace(entity);

        return entity;
    }

    private void saveNewMeterData(ElectricMeterEntity entity) {
        // 预付费电表默认不保电
        if (BooleanUtil.isTrue(entity.getIsPrepay())) {
            entity.setProtectedModel(false);
        }

        // 临时设置电表编号，后续用生成规则来更新
        // 新表假设默认是开闸的
        // 新表可以补偿，默认没有补偿过
        entity.setMeterNo(IdUtil.fastSimpleUUID())
                .setIsCutOff(false);
        repository.insert(entity);

        ElectricMeterEntity updateEntity = new ElectricMeterEntity()
                .setId(entity.getId())
                .setMeterNo(SerialNumberGeneratorUtil.genElectricMeterNo(entity.getId()));
        repository.updateById(updateEntity);
    }

    /**
     * 验证设备型号并设置相关属性
     */
    private DeviceModelBo validateAndSetDeviceModel(ElectricMeterEntity entity) {
        DeviceModelBo deviceModel = deviceModelService.getDetail(entity.getModelId());

        if (!DeviceTypeEnum.ELECTRIC.getKey().equals(deviceModel.getTypeKey())) {
            throw new BusinessRuntimeException("电表型号设置错误");
        }

        entity.setProductCode(deviceModel.getProductCode());
        entity.setCommunicateModel(DeviceUtil.getProperty(
                deviceModel.getModelProperty(),
                DeviceConstant.COMMUNICATE_MODE,
                String.class
        ));

        return deviceModel;
    }

    /**
     * 处理CT变比
     */
    private void handleCtRatio(ElectricMeterEntity entity, DeviceModelBo deviceModel) {
        Boolean isCt = DeviceUtil.getProperty(
                deviceModel.getModelProperty(),
                DeviceConstant.IS_CT,
                Boolean.class
        );

        if (BooleanUtil.isTrue(isCt)) {
            if (entity.getCt() == null) {
                entity.setCt(BigDecimal.ONE);
            }
        } else {
            if (entity.getCt() != null) {
                throw new BusinessRuntimeException("当前电表型号不支持CT变比");
            }
        }
    }

    /**
     * 验证预付费支持
     */
    private void validatePrepaySupport(DeviceModelBo deviceModel) {
        Boolean prepaySupported = DeviceUtil.getProperty(
                deviceModel.getModelProperty(),
                DeviceConstant.IS_PREPAY,
                Boolean.class
        );
        if (!BooleanUtil.isTrue(prepaySupported)) {
            throw new BusinessRuntimeException("当前电表型号不支持预付费");
        }
    }

    /**
     * 处理通信模式验证
     */
    private void handleCommunicationMode(ElectricMeterEntity entity, ElectricMeterCreateDto dto) {
        if (DeviceUtil.isNbCommunicateModel(entity.getCommunicateModel())) {
            handleNbMode(entity, dto);
        } else {
            handleNonNbMode(entity, dto);
        }
    }

    /**
     * 处理NB模式
     */
    private void handleNbMode(ElectricMeterEntity entity, ElectricMeterCreateDto dto) {
        if (StringUtils.isBlank(entity.getImei())) {
            throw new BusinessRuntimeException("NB模式电表IMEI不能为空");
        }
        if (dto == null || StringUtils.isBlank(dto.getDeviceNo())) {
            throw new BusinessRuntimeException("NB模式电表deviceNo不能为空");
        }

        entity.setGatewayId(null);
        entity.setDeviceNo(dto.getDeviceNo());

        List<ElectricMeterBo> repeatList = electricMeterInfoService.findList(new ElectricMeterQueryDto()
                .setImei(entity.getImei())
        );

        if (!CollectionUtils.isEmpty(repeatList)) {
            throw new BusinessRuntimeException("电表信息重复");
        }
    }

    /**
     * 处理非NB模式
     */
    private void handleNonNbMode(ElectricMeterEntity entity, ElectricMeterCreateDto dto) {
        if (entity.getGatewayId() == null) {
            throw new BusinessRuntimeException("非NB模式电表必须绑定网关");
        }

        if (entity.getPortNo() == null) {
            throw new BusinessRuntimeException("非NB模式电表串口号不能为空");
        }

        if (entity.getMeterAddress() == null) {
            throw new BusinessRuntimeException("非NB模式电表地址不能为空");
        }

        GatewayBo gateway = validateGateway(entity);
        entity.setImei("");
        if (StringUtils.isBlank(gateway.getDeviceNo())) {
            throw new BusinessRuntimeException("网关deviceNo不能为空");
        }
        entity.setDeviceNo(buildGatewayChildDeviceNo(gateway.getDeviceNo(), entity.getPortNo(), entity.getMeterAddress()));

        List<ElectricMeterBo> repeatList = electricMeterInfoService.findList(new ElectricMeterQueryDto()
                .setGatewayId(dto.getGatewayId())
                .setPortNo(dto.getPortNo())
                .setMeterAddress(dto.getMeterAddress()));

        if (!CollectionUtils.isEmpty(repeatList)) {
            throw new BusinessRuntimeException("电表信息重复");
        }
    }

    /**
     * 验证网关信息
     */
    private GatewayBo validateGateway(ElectricMeterEntity entity) {
        GatewayBo gateway = gatewayService.getDetail(entity.getGatewayId());
        if (gateway == null || gateway.getIotId() == null) {
            throw new BusinessRuntimeException("网关信息有误，请重新选择");
        }
        if (!BooleanUtil.isTrue(gateway.getIsOnline())) {
            throw new BusinessRuntimeException("网关不在线，请重试");
        }
        return gateway;
    }

    /**
     * 构建子设备编号
     */
    private String buildGatewayChildDeviceNo(String gatewayDeviceNo, Integer portNo, Integer meterAddress) {
        if (StringUtils.isBlank(gatewayDeviceNo) || portNo == null || meterAddress == null) {
            throw new BusinessRuntimeException("子设备编号缺少必要参数");
        }
        return gatewayDeviceNo + ":" + portNo + ":" + meterAddress;
    }

    /**
     * 验证空间信息并设置相关属性
     */
    private void validateAndSetSpace(ElectricMeterEntity entity) {
        SpaceBo space = spaceService.getDetail(entity.getSpaceId());
        entity.setOwnAreaId(space.getOwnAreaId());

        if (BooleanUtil.isTrue(entity.getIsPrepay()) && !SpaceTypeEnum.ROOM.equals(space.getType())) {
            throw new BusinessRuntimeException("预付费模式下电表只允许绑定到房间");
        }
    }

    private void setCtWhenAddNewMeter(Integer meterId, BigDecimal ct) {
        // 其他的参数校验在add方法最开始已完成
        if (ct == null) {
            return;
        }

        // 重新查询一次，保持最新的数据
        ElectricMeterBo meter = electricMeterInfoService.getDetail(meterId);
        MeterCommandDto ctCommandDto = new MeterCommandDto()
                .setMeter(meter)
                .setCommandType(CommandTypeEnum.ENERGY_ELECTRIC_CT)
                .setCommandSource(CommandSourceEnum.SYSTEM)
                .setCommandData(ct.toPlainString());
        saveMeterCommandAndRun(ctCommandDto);
    }

    private void checkAndSetUpdateInfo(ElectricMeterEntity entity, ElectricMeterBo old) {
        // 校验预付费修改
        if (entity.getIsPrepay() != null && !entity.getIsPrepay().equals(old.getIsPrepay())) {
            if (old.getAccountId() != null) {
                throw new BusinessRuntimeException("已开户电表不允许修改预付费属性");
            }

            // 校验是否可以有预付费功能
            if (Boolean.TRUE.equals(entity.getIsPrepay())) {
                // 注意modelId是不能修改的
                DeviceModelBo deviceModel = deviceModelService.getDetail(old.getModelId());

                validatePrepaySupport(deviceModel);
            }
        }

        // 校验空间修改
        if (entity.getSpaceId() != null && !old.getSpaceId().equals(entity.getSpaceId())) {
            if (old.getAccountId() != null) {
                throw new BusinessRuntimeException("已开户电表不允许修改绑定房间");
            }

            validateAndSetSpace(entity);
        }

        // 校验deviceNo修改
        boolean deviceNoChanged = StringUtils.isNotBlank(entity.getDeviceNo())
                && !entity.getDeviceNo().equals(old.getDeviceNo());
        if (deviceNoChanged) {
            if (DeviceUtil.isNbCommunicateModel(old.getCommunicateModel())) {
                syncDeviceNoToIotPlatform(old, entity.getDeviceNo());
            } else {
                entity.setDeviceNo(buildGatewayChildDeviceNo(resolveGatewayDeviceNo(old),
                        old.getPortNo(), old.getMeterAddress()));
            }
        }

        // 设置更新信息
        entity.setUpdateUser(requestContext.getUserId());
        entity.setUpdateUserName(requestContext.getUserRealName());
        entity.setUpdateTime(LocalDateTime.now());
    }

    /**
     * 批量销户电表
     *
     * @param meterCancelDto 电表销户传输对象
     * @return 销户的电表余额
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<MeterCancelResultDto> cancelMeterAccount(@Valid @NotNull MeterCancelDto meterCancelDto) {
        LocalDateTime now = LocalDateTime.now();
        List<MeterCancelResultDto> meterCancelBalanceList = new ArrayList<>();

        try {
            log.info("开始执行批量电表销户操作，账户ID: {}, 电表数量: {}",
                    meterCancelDto.getAccountId(), meterCancelDto.getMeterCloseDetail().size());

            for (MeterCancelDetailDto meterCloseDetail : meterCancelDto.getMeterCloseDetail()) {
                MeterCancelResultDto closedBalance = processSingleMeterClose(meterCancelDto, meterCloseDetail, now);
                meterCancelBalanceList.add(closedBalance);
            }

            // 批量清除绑定关系
            clearMeterAccountBindings(meterCancelDto.getMeterCloseDetail());

            log.info("批量电表销户操作完成，账户ID: {}, 处理电表数量: {}",
                    meterCancelDto.getAccountId(), meterCancelBalanceList.size());

        } catch (Exception e) {
            log.error("批量电表销户系统异常，账户ID: {}", meterCancelDto.getAccountId(), e);
            throw new BusinessRuntimeException("批量电表销户操作失败：" + e.getMessage());
        }

        return meterCancelBalanceList;
    }

    /**
     * 处理单个电表销户
     */
    private MeterCancelResultDto processSingleMeterClose(MeterCancelDto meterCancelDto,
                                                         MeterCancelDetailDto meterCloseDetail,
                                                         LocalDateTime now) {
        // 获取电表详情
        ElectricMeterDetailDto meterDto = buildMeterDetailDto(meterCloseDetail.getMeterId());

        // 获取电量数据并保存记录
        ElectricMeterPowerRecordDto recordDto = createAndSavePowerRecord(meterCancelDto, meterCloseDetail, meterDto, now);

        // 查询余额
        BigDecimal balance = queryMeterBalance(meterDto, meterCancelDto.getElectricAccountType());

        BigDecimal historyPowerTotal = calculateHistoryPowerTotal(meterDto, recordDto);

        // 创建销户记录
        createCancelMeterRecord(meterCancelDto, meterDto, recordDto, balance, historyPowerTotal, now);

        return new MeterCancelResultDto()
                .setMeterId(meterDto.getMeterId())
                .setBalance(balance)
                .setHistoryPowerTotal(historyPowerTotal);
    }

    /**
     * 创建并保存电量记录
     */
    private ElectricMeterPowerRecordDto createAndSavePowerRecord(MeterCancelDto meterCancelDto,
                                                                 MeterCancelDetailDto meterCloseDetail,
                                                                 ElectricMeterDetailDto meterDto,
                                                                 LocalDateTime now) {
        ElectricMeterPowerRecordDto recordDto = new ElectricMeterPowerRecordDto()
                .setOriginalReportId(mockOriginalReportId(meterCloseDetail.getMeterId(), meterCancelDto.getAccountId()))
                .setElectricMeterDetailDto(meterDto)
                .setAccountId(meterCancelDto.getAccountId())
                .setOwnerId(meterCancelDto.getOwnerId())
                .setOwnerType(meterCancelDto.getOwnerType())
                .setOwnerName(meterCancelDto.getOwnerName())
                .setElectricAccountType(meterCancelDto.getElectricAccountType())
                .setRecordTime(now)
                .setNeedConsume(true);

        // 获取电量数据
        setPowerDataToRecord(meterCloseDetail, meterDto, recordDto);

        // 保存电量记录
        meterConsumeService.savePowerRecord(recordDto);

        return recordDto;
    }

    /**
     * 设置电量数据到记录中
     */
    private void setPowerDataToRecord(MeterCancelDetailDto meterCloseDetail,
                                      ElectricMeterDetailDto meterDto,
                                      ElectricMeterPowerRecordDto recordDto) {
        BigDecimal powerHigher, powerHigh, powerLow, powerLower, powerDeepLow;

        if (BooleanUtil.isTrue(meterDto.getIsOnline())) {
            // 在线电表，获取实时电量
            Map<ElectricPricePeriodEnum, BigDecimal> powerMap = getMeterPower(meterCloseDetail.getMeterId(),
                    List.of(ElectricPricePeriodEnum.HIGHER, ElectricPricePeriodEnum.HIGH,
                            ElectricPricePeriodEnum.LOW, ElectricPricePeriodEnum.LOWER,
                            ElectricPricePeriodEnum.DEEP_LOW));

            powerHigher = powerMap.get(ElectricPricePeriodEnum.HIGHER);
            powerHigh = powerMap.get(ElectricPricePeriodEnum.HIGH);
            powerLow = powerMap.get(ElectricPricePeriodEnum.LOW);
            powerLower = powerMap.get(ElectricPricePeriodEnum.LOWER);
            powerDeepLow = powerMap.get(ElectricPricePeriodEnum.DEEP_LOW);
        } else {
            // 离线电表，使用手动输入的电量
            validateOfflineMeterPowerInput(meterCloseDetail, meterDto.getMeterName());

            powerHigher = meterCloseDetail.getPowerHigher();
            powerHigh = meterCloseDetail.getPowerHigh();
            powerLow = meterCloseDetail.getPowerLow();
            powerLower = meterCloseDetail.getPowerLower();
            powerDeepLow = meterCloseDetail.getPowerDeepLow();
        }

        // 计算总电量
        BigDecimal totalPower = Stream.of(powerHigher, powerHigh, powerLow, powerLower, powerDeepLow)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        recordDto.setPower(totalPower)
                .setPowerHigher(powerHigher)
                .setPowerHigh(powerHigh)
                .setPowerLow(powerLow)
                .setPowerLower(powerLower)
                .setPowerDeepLow(powerDeepLow);
    }

    /**
     * 校验离线电表的电量输入
     */
    private void validateOfflineMeterPowerInput(MeterCancelDetailDto meterCloseDetail, String meterName) {
        if (Stream.of(
                meterCloseDetail.getPowerHigher(),
                meterCloseDetail.getPowerHigh(),
                meterCloseDetail.getPowerLow(),
                meterCloseDetail.getPowerLower(),
                meterCloseDetail.getPowerDeepLow()
        ).allMatch(Objects::isNull)) {
            throw new BusinessRuntimeException("电表离线，请输入电量。电表名称：" + meterName);
        }
    }

    /**
     * 查询电表余额
     */
    private BigDecimal queryMeterBalance(ElectricMeterDetailDto meterDto, ElectricAccountTypeEnum accountType) {
        if (ElectricAccountTypeEnum.QUANTITY.equals(accountType)) {
            BalanceBo balanceBo = balanceService.query(new BalanceQueryDto()
                    .setBalanceRelationId(meterDto.getMeterId())
                    .setBalanceType(BalanceTypeEnum.ELECTRIC_METER));
            return balanceBo.getBalance();
        }
        return null;
    }

    /**
     * 创建销户记录
     */
    private void createCancelMeterRecord(MeterCancelDto meterCancelDto,
                                         ElectricMeterDetailDto meterDto,
                                         ElectricMeterPowerRecordDto recordDto,
                                         BigDecimal balance,
                                         BigDecimal historyPowerTotal,
                                         LocalDateTime now) {
        MeterCancelRecordEntity cancelRecord = new MeterCancelRecordEntity()
                .setCancelNo(meterCancelDto.getCancelNo())
                .setAccountId(meterCancelDto.getAccountId())
                .setMeterId(meterDto.getMeterId())
                .setMeterName(meterDto.getMeterName())
                .setMeterNo(meterDto.getMeterNo())
                .setMeterType(MeterTypeEnum.ELECTRIC.getCode())
                .setSpaceId(meterDto.getSpaceId())
                .setBalance(balance)
                .setIsOnline(meterDto.getIsOnline())
                .setIsCutOff(meterDto.getIsCutOff())
                .setPower(recordDto.getPower())
                .setPowerHigher(recordDto.getPowerHigher())
                .setPowerHigh(recordDto.getPowerHigh())
                .setPowerLow(recordDto.getPowerLow())
                .setPowerLower(recordDto.getPowerLower())
                .setPowerDeepLow(recordDto.getPowerDeepLow())
                .setHistoryPowerTotal(Optional.ofNullable(historyPowerTotal).orElse(BigDecimal.ZERO))
                .setShowTime(now);

        // 设置空间信息
        setSpaceInfoToCancelRecord(meterDto, cancelRecord);

        meterCancelRecordRepository.insert(cancelRecord);
    }

    /**
     * 计算销户时累计的阶梯用量，防止出现负值
     */
    private BigDecimal calculateHistoryPowerTotal(ElectricMeterDetailDto meterDto,
                                                  ElectricMeterPowerRecordDto recordDto) {
        BigDecimal power = Optional.ofNullable(recordDto.getPower()).orElse(BigDecimal.ZERO);
        BigDecimal stepStartValue = Optional.ofNullable(meterDto.getStepStartValue()).orElse(BigDecimal.ZERO);
        BigDecimal historyOffset = Optional.ofNullable(meterDto.getHistoryPowerOffset()).orElse(BigDecimal.ZERO);
        BigDecimal historyPowerTotal = power.subtract(stepStartValue).add(historyOffset);
        return historyPowerTotal.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : historyPowerTotal;
    }

    /**
     * 设置空间信息到销户记录
     */
    private void setSpaceInfoToCancelRecord(ElectricMeterDetailDto meterDto, MeterCancelRecordEntity cancelRecord) {
        if (meterDto.getSpaceId() != null) {
            SpaceBo spaceBo = spaceService.getDetail(meterDto.getSpaceId());
            String spaceParentIds = CollectionUtils.isEmpty(spaceBo.getParentsIds()) ? "" :
                    spaceBo.getParentsIds().stream()
                            .map(String::valueOf)
                            .collect(Collectors.joining(","));
            String spaceParentNames = CollectionUtils.isEmpty(spaceBo.getParentsNames()) ? "" :
                    String.join(",", spaceBo.getParentsNames());

            cancelRecord.setSpaceName(spaceBo.getName())
                    .setSpaceParentIds(spaceParentIds)
                    .setSpaceParentNames(spaceParentNames);
        }
    }

    /**
     * 批量清除电表账户绑定关系
     */
    private void clearMeterAccountBindings(List<MeterCancelDetailDto> meterCloseDetails) {
        List<Integer> meterIds = meterCloseDetails.stream()
                .map(MeterCancelDetailDto::getMeterId)
                .collect(Collectors.toList());
        repository.resetMeterAccountInfo(new ElectricMeterResetAccountQo()
                .setMeterIds(meterIds)
                .setUpdateUser(requestContext.getUserId())
                .setUpdateUserName(requestContext.getUserRealName())
                .setUpdateTime(LocalDateTime.now())
        );
    }
}
