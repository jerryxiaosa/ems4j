package info.zhihui.ems.business.plan.service.impl;

import cn.hutool.core.util.BooleanUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import info.zhihui.ems.common.enums.ElectricPricePeriodEnum;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.common.utils.JacksonUtil;
import info.zhihui.ems.business.plan.bo.ElectricPricePlanBo;
import info.zhihui.ems.business.plan.bo.ElectricPricePlanDetailBo;
import info.zhihui.ems.business.plan.bo.StepPriceBo;
import info.zhihui.ems.business.plan.dto.ElectricPricePlanQueryDto;
import info.zhihui.ems.business.plan.dto.ElectricPricePlanSaveDto;
import info.zhihui.ems.business.plan.dto.ElectricPriceTimeDto;
import info.zhihui.ems.business.plan.dto.ElectricPriceTypeDto;
import info.zhihui.ems.business.plan.entity.ElectricPricePlanEntity;
import info.zhihui.ems.business.plan.mapper.ElectricPlanMapper;
import info.zhihui.ems.business.plan.qo.ElectricPricePlanQo;
import info.zhihui.ems.business.plan.repository.ElectricPricePlanRepository;
import info.zhihui.ems.business.plan.service.ElectricPricePlanService;
import info.zhihui.ems.business.plan.utils.ElectricPlanValidationUtil;
import info.zhihui.ems.foundation.system.bo.ConfigBo;
import info.zhihui.ems.foundation.system.constant.SystemConfigConstant;
import info.zhihui.ems.foundation.system.dto.ConfigUpdateDto;
import info.zhihui.ems.foundation.system.service.ConfigService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author jerryxiaosa
 */
@Service
@RequiredArgsConstructor
@Validated
public class ElectricPricePlanServiceImpl implements ElectricPricePlanService {

    private final ElectricPricePlanRepository repository;
    private final ElectricPlanMapper mapper;
    private final ConfigService configService;

    private static final EnumSet<ElectricPricePeriodEnum> BASE_PRICE_TYPES =
            EnumSet.of(ElectricPricePeriodEnum.HIGHER, ElectricPricePeriodEnum.HIGH,
                    ElectricPricePeriodEnum.LOW, ElectricPricePeriodEnum.LOWER,
                    ElectricPricePeriodEnum.DEEP_LOW);

    /**
     * 根据查询条件获取电价方案列表
     * @param query 查询条件
     * @return 电价方案列表
     */
    @Override
    public List<ElectricPricePlanBo> findList(@NotNull ElectricPricePlanQueryDto query) {
        List<ElectricPricePlanEntity> list = repository.findList(mapper.queryDtoToQo(query));
        return mapper.listEntityToBo(list);
    }

    /**
     * 获取电价方案详细信息
     * @param id 方案ID
     * @return 方案详细信息
     */
    @Override
    public ElectricPricePlanDetailBo getDetail(@NotNull Integer id) {
        ElectricPricePlanEntity entity = repository.selectById(id);
        if (entity == null) {
            throw new NotFoundException("电价方案数据不存在");
        }
        ElectricPricePlanDetailBo detailBo = mapper.detailEntityToBo(entity);
        if (BooleanUtil.isTrue(detailBo.getIsCustomPrice())) {
            detailBo.setPriceHigherMultiply(null);
            detailBo.setPriceHighMultiply(null);
            detailBo.setPriceLowMultiply(null);
            detailBo.setPriceLowerMultiply(null);
            detailBo.setPriceDeepLowMultiply(null);
        }
        if (Boolean.TRUE.equals(detailBo.getIsStep())) {
            detailBo.setStepPrices(JacksonUtil.fromJson(detailBo.getStepPrice(), new TypeReference<>() {
            }));
        } else {
            detailBo.setStepPrices(new ArrayList<>());
        }
        return detailBo;
    }

    /**
     * 新增电价方案
     * @param dto 新增数据传输对象
     * @return 新增后的方案ID
     */
    @Override
    public Integer add(@Valid @NotNull ElectricPricePlanSaveDto dto) {
        dto.setId(null);
        ElectricPricePlanEntity entity = toEntity(dto);
        repository.insert(entity);
        return entity.getId();
    }

    private ElectricPricePlanEntity toEntity(ElectricPricePlanSaveDto dto) {
        ElectricPricePlanEntity entity = mapper.saveDtoToEntity(dto);

        // 启用阶梯
        if (Boolean.TRUE.equals(dto.getIsStep())) {
            ElectricPlanValidationUtil.checkStepPrice(dto.getStepPrices());
            entity.setStepPrice(JacksonUtil.toJson(dto.getStepPrices()));
        }

        if (!Boolean.TRUE.equals(dto.getIsCustomPrice())) {
            Map<Integer, BigDecimal> priceTypeMap = loadElectricPriceBaseMap();
            applyBasePrice(entity, priceTypeMap);
        }

        checkEntity(entity);
        return entity;
    }


    /**
     * 编辑电价方案
     * @param dto 编辑数据传输对象
     */
    @Override
    public void edit(@Valid @NotNull ElectricPricePlanSaveDto dto) {
        ElectricPricePlanEntity old = repository.selectById(dto.getId());
        if (old == null) {
            throw new NotFoundException("数据不存在，请刷新后重试");
        }

        ElectricPricePlanEntity entity = toEntity(dto);
        repository.updateById(entity);
    }

    /**
     * 删除电价方案
     * @param id 方案ID
     */
    @Override
    public void del(@NotNull Integer id) {
        repository.deleteById(id);
    }

    /**
     * 获取默认阶梯电价配置
     * @return 阶梯电价列表
     */
    @Override
    public List<StepPriceBo> getDefaultStepPrice() {
        ConfigBo config = configService.getByKey(SystemConfigConstant.ELECTRIC_STEP_PRICE_KEY);
        if (config == null || StringUtils.isBlank(config.getConfigValue())) {
            throw new NotFoundException("请先配置默认阶梯电价");
        }

        return JacksonUtil.fromJson(config.getConfigValue(), new TypeReference<>() {
        });
    }

    /**
     * 编辑默认阶梯电价配置
     * @param boList 阶梯电价列表
     */
    @Override
    public void editDefaultStepPrice(@NotEmpty List<StepPriceBo> boList) {
        ElectricPlanValidationUtil.checkStepPrice(boList);
        updateSystemConfig(SystemConfigConstant.ELECTRIC_STEP_PRICE_KEY, JacksonUtil.toJson(boList));
    }

    /**
     * 获取默认时间段配置
     * @return 时间段列表
     */
    @Override
    public List<ElectricPriceTimeDto> getElectricTime() {
        ConfigBo config = configService.getByKey(SystemConfigConstant.ELECTRIC_PRICE_TIME_KEY);
        if (config == null || StringUtils.isBlank(config.getConfigValue())) {
            throw new NotFoundException("请先配置默认尖峰平谷深谷时间段");
        }

        return JacksonUtil.fromJson(config.getConfigValue(), new TypeReference<>() {
        });
    }

    /**
     * 编辑默认时间段配置
     * @param dtoList 时间段列表
     */
    @Override
    public void editElectricTime(@Valid @NotEmpty List<ElectricPriceTimeDto> dtoList) {
        // 校验时间段配置并返回规范化结果
        List<ElectricPriceTimeDto> normalizedList = ElectricPlanValidationUtil.getValidElectricPlanTime(dtoList);
        updateSystemConfig(SystemConfigConstant.ELECTRIC_PRICE_TIME_KEY, JacksonUtil.toJson(normalizedList));
    }


    /**
     * 获取基础尖峰平谷深谷电价配置
     * @return 电价类型列表
     */
    @Override
    public List<ElectricPriceTypeDto> getElectricPrice() {
        ConfigBo config = configService.getByKey(SystemConfigConstant.ELECTRIC_PRICE_TYPE_KEY);
        if (config == null || StringUtils.isBlank(config.getConfigValue())) {
            throw new BusinessRuntimeException("请先配置默认尖峰平谷电价");
        }

        List<ElectricPriceTypeDto> priceList = JacksonUtil.fromJson(config.getConfigValue(), new TypeReference<>() {
        });
        if (CollectionUtils.isEmpty(priceList)) {
            throw new BusinessRuntimeException("请先配置默认尖峰平谷电价");
        }
        for (ElectricPriceTypeDto priceType : priceList) {
            if (priceType == null || priceType.getType() == null || priceType.getPrice() == null) {
                throw new BusinessRuntimeException("默认尖峰平谷电价配置不完整，类型或价格不能为空");
            }
        }
        return priceList;
    }

    /**
     * 编辑基础尖峰平谷电价配置
     * @param boList 电价类型列表
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void editElectricPrice(@NotEmpty List<ElectricPriceTypeDto> boList) {
        ElectricPlanValidationUtil.checkPriceType(boList);
        updateSystemConfig(SystemConfigConstant.ELECTRIC_PRICE_TYPE_KEY, JacksonUtil.toJson(boList));

        updateAllNoneCustomPrice(boList);
    }


    /**
     * 基础电价变更后 更新所有方案的电价
     */
    private void updateAllNoneCustomPrice(List<ElectricPriceTypeDto> dtoList) {
        Map<Integer, BigDecimal> priceTypeMap = dtoList.stream()
                .collect(Collectors.toMap(
                        dto -> dto.getType() == null ? null : dto.getType().getCode(),
                        ElectricPriceTypeDto::getPrice));

        ElectricPricePlanQo query = new ElectricPricePlanQo();
        query.setIsCustomPrice(false);
        List<ElectricPricePlanEntity> list = repository.findList(query);

        for (ElectricPricePlanEntity relatedPlan : list) {
            applyBasePrice(relatedPlan, priceTypeMap);
            repository.updateById(relatedPlan);
        }
    }


    private void updateSystemConfig(String key, String value) {
        if (configService.getByKey(key) == null) {
            throw new BusinessRuntimeException("请先设置默认配置" + key);
        }
        ConfigUpdateDto config = new ConfigUpdateDto();
        config.setConfigKey(key);
        config.setConfigValue(value);
        configService.update(config);
    }

    /**
     * 校验电价方案实体
     *
     * @param entity     电价方案实体
     */
    public void checkEntity(ElectricPricePlanEntity entity) {
        if (!BooleanUtil.isTrue(entity.getIsCustomPrice())) {
            validateMultiplyFactors(entity);
            validatePriceCalculations(entity);
        }
        validateUniqueEntityName(entity);
    }

    /**
     * 校验倍率因子
     */
    private void validateMultiplyFactors(ElectricPricePlanEntity entity) {
        if (entity.getPriceHigherMultiply() == null
                || entity.getPriceHighMultiply() == null
                || entity.getPriceLowMultiply() == null
                || entity.getPriceLowerMultiply() == null
                || entity.getPriceDeepLowMultiply() == null) {
            throw new BusinessRuntimeException("倍率不能为空");
        }
    }

    /**
     * 校验价格计算的正确性
     */
    private void validatePriceCalculations(ElectricPricePlanEntity entity) {
        validatePriceCalculation(entity.getPriceHigher(), entity.getPriceHigherBase(),
                entity.getPriceHigherMultiply(), "尖电价");
        validatePriceCalculation(entity.getPriceHigh(), entity.getPriceHighBase(),
                entity.getPriceHighMultiply(), "峰电价");
        validatePriceCalculation(entity.getPriceLow(), entity.getPriceLowBase(),
                entity.getPriceLowMultiply(), "平电价");
        validatePriceCalculation(entity.getPriceLower(), entity.getPriceLowerBase(),
                entity.getPriceLowerMultiply(), "谷电价");
        validatePriceCalculation(entity.getPriceDeepLow(), entity.getPriceDeepLowBase(),
                entity.getPriceDeepLowMultiply(), "深谷电价");
    }

    /**
     * 校验单个价格计算
     */
    private void validatePriceCalculation(BigDecimal actualPrice, BigDecimal basePrice,
                                                 BigDecimal multiply, String typeName) {
        if (actualPrice == null || basePrice == null || multiply == null) {
            throw new BusinessRuntimeException(typeName + "配置不完整");
        }
        if (actualPrice.compareTo(basePrice.multiply(multiply)) != 0) {
            throw new BusinessRuntimeException(typeName + "计算错误");
        }
    }

    /**
     * 校验实体名称唯一性
     */
    private void validateUniqueEntityName(ElectricPricePlanEntity entity) {
        List<ElectricPricePlanEntity> repeatList = repository.findList(new ElectricPricePlanQo()
                .setNeId(entity.getId())
                .setEqName(entity.getName()));
        if (!CollectionUtils.isEmpty(repeatList)) {
            throw new BusinessRuntimeException("方案名称已存在");
        }
    }

    private Map<Integer, BigDecimal> loadElectricPriceBaseMap() {
        List<ElectricPriceTypeDto> priceConfigList = getElectricPrice();
        Map<Integer, BigDecimal> priceTypeMap = priceConfigList.stream()
                .collect(Collectors.toMap(
                        dto -> dto.getType() == null ? null : dto.getType().getCode(),
                        ElectricPriceTypeDto::getPrice));
        ensureConfigCompleted(priceTypeMap);
        return priceTypeMap;
    }

    private void ensureConfigCompleted(Map<Integer, BigDecimal> priceTypeMap) {
        for (ElectricPricePeriodEnum typeEnum : BASE_PRICE_TYPES) {
            if (!priceTypeMap.containsKey(typeEnum.getCode())) {
                throw new BusinessRuntimeException("默认尖峰平谷电价配置不完整，缺少:" + typeEnum.name());
            }
            BigDecimal price = priceTypeMap.get(typeEnum.getCode());
            if (price == null) {
                throw new BusinessRuntimeException("默认尖峰平谷电价配置不完整，" + typeEnum.name() + "价格不能为空");
            }
            if (price.compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessRuntimeException("默认尖峰平谷电价配置不正确，" + typeEnum.name() + "价格不能为负数");
            }
        }
    }

    private void applyBasePrice(ElectricPricePlanEntity entity, Map<Integer, BigDecimal> priceTypeMap) {
        BigDecimal higherBase = priceTypeMap.get(ElectricPricePeriodEnum.HIGHER.getCode());
        BigDecimal highBase = priceTypeMap.get(ElectricPricePeriodEnum.HIGH.getCode());
        BigDecimal lowBase = priceTypeMap.get(ElectricPricePeriodEnum.LOW.getCode());
        BigDecimal lowerBase = priceTypeMap.get(ElectricPricePeriodEnum.LOWER.getCode());
        BigDecimal deepLowBase = priceTypeMap.get(ElectricPricePeriodEnum.DEEP_LOW.getCode());

        entity.setPriceHigherBase(higherBase);
        entity.setPriceHighBase(highBase);
        entity.setPriceLowBase(lowBase);
        entity.setPriceLowerBase(lowerBase);
        entity.setPriceDeepLowBase(deepLowBase);

        if (higherBase != null && entity.getPriceHigherMultiply() != null) {
            entity.setPriceHigher(higherBase.multiply(entity.getPriceHigherMultiply()));
        }
        if (highBase != null && entity.getPriceHighMultiply() != null) {
            entity.setPriceHigh(highBase.multiply(entity.getPriceHighMultiply()));
        }
        if (lowBase != null && entity.getPriceLowMultiply() != null) {
            entity.setPriceLow(lowBase.multiply(entity.getPriceLowMultiply()));
        }
        if (lowerBase != null && entity.getPriceLowerMultiply() != null) {
            entity.setPriceLower(lowerBase.multiply(entity.getPriceLowerMultiply()));
        }
        if (deepLowBase != null && entity.getPriceDeepLowMultiply() != null) {
            entity.setPriceDeepLow(deepLowBase.multiply(entity.getPriceDeepLowMultiply()));
        }
    }
}
