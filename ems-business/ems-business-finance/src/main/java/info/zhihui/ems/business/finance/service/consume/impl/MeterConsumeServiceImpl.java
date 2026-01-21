package info.zhihui.ems.business.finance.service.consume.impl;

import cn.hutool.core.util.BooleanUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import info.zhihui.ems.business.finance.bo.BalanceBo;
import info.zhihui.ems.business.finance.dto.*;
import info.zhihui.ems.business.finance.entity.*;
import info.zhihui.ems.business.finance.enums.ConsumeTypeEnum;
import info.zhihui.ems.business.finance.enums.CorrectionTypeEnum;
import info.zhihui.ems.business.finance.qo.ElectricMeterPowerRecordQo;
import info.zhihui.ems.business.finance.qo.ElectricPowerConsumeRecordQo;
import info.zhihui.ems.business.finance.repository.ElectricMeterBalanceConsumeRecordRepository;
import info.zhihui.ems.business.finance.repository.ElectricMeterPowerConsumeRecordRepository;
import info.zhihui.ems.business.finance.repository.ElectricMeterPowerRecordRepository;
import info.zhihui.ems.business.finance.repository.ElectricMeterPowerRelationRepository;
import info.zhihui.ems.business.finance.service.balance.BalanceService;
import info.zhihui.ems.business.finance.service.consume.MeterConsumeService;
import info.zhihui.ems.business.finance.service.consume.MeterCorrectionService;
import info.zhihui.ems.common.exception.ParamException;
import info.zhihui.ems.components.context.RequestContext;
import info.zhihui.ems.business.finance.utils.MoneyUtil;
import info.zhihui.ems.business.plan.bo.ElectricPricePlanDetailBo;
import info.zhihui.ems.business.plan.bo.StepPriceBo;
import info.zhihui.ems.business.plan.service.ElectricPricePlanService;
import info.zhihui.ems.common.constant.SerialNumberConstant;
import info.zhihui.ems.common.enums.*;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.common.utils.SerialNumberGeneratorUtil;
import info.zhihui.ems.components.lock.core.LockTemplate;
import info.zhihui.ems.foundation.organization.bo.OrganizationBo;
import info.zhihui.ems.foundation.organization.service.OrganizationService;
import info.zhihui.ems.foundation.space.service.SpaceService;
import info.zhihui.ems.foundation.space.util.SpaceInfoUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

/**
 * @author jerryxiaosa
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Validated
public class MeterConsumeServiceImpl implements MeterConsumeService, MeterCorrectionService {
    private final BalanceService balanceService;
    private final ElectricMeterPowerRecordRepository electricMeterPowerRecordRepository;
    private final ElectricMeterPowerRelationRepository electricMeterPowerRelationRepository;
    private final ElectricMeterPowerConsumeRecordRepository electricMeterPowerConsumeRecordRepository;
    private final ElectricMeterBalanceConsumeRecordRepository electricMeterBalanceConsumeRecordRepository;
    private final ElectricPricePlanService electricPricePlanService;
    private final SpaceService spaceService;
    private final OrganizationService organizationService;
    private final LockTemplate lockTemplate;
    private final RequestContext requestContext;

    private final static String LOCK_METER = "LOCK:METER:%d";

    /**
     * 保存电表抄表数据
     * 根据参数自动处理电量消费和金额消费
     *
     * @param meterPowerRecordDto 电表抄表数据
     */
    @Override
    public void savePowerRecord(@Valid @NotNull ElectricMeterPowerRecordDto meterPowerRecordDto) {
        Lock lock = getMeterLock(meterPowerRecordDto.getElectricMeterDetailDto().getMeterId());
        if (!lock.tryLock()) {
            throw new BusinessRuntimeException("正在保存电表记录，请稍后重试");
        }

        try {
            // 保存电表记录
            ElectricMeterPowerRecordEntity meterPowerRecordEntity = saveMeterPowerRecord(meterPowerRecordDto);

            // 保存电表关系记录
            saveElectricMeterPowerRelation(meterPowerRecordDto, meterPowerRecordEntity.getId());

            // 跳过，即只保存电量记录，不进行电量消费和金额消费计算
            if (shouldSkipConsumeCalculation(meterPowerRecordDto)) {
                return;
            }

            // 处理消费计算
            processConsumeCalculation(meterPowerRecordDto, meterPowerRecordEntity);

        } finally {
            lock.unlock();
        }
    }

    /**
     * 保存电表记录
     */
    private ElectricMeterPowerRecordEntity saveMeterPowerRecord(ElectricMeterPowerRecordDto meterPowerRecordDto) {
        try {
            ElectricMeterPowerRecordEntity meterPowerRecordEntity = buildMeterPowerRecordEntity(meterPowerRecordDto);
            electricMeterPowerRecordRepository.insert(meterPowerRecordEntity);
            log.debug("成功保存电表记录，电表ID: {}, 记录ID: {}",
                    meterPowerRecordEntity.getMeterId(), meterPowerRecordEntity.getId());
            return meterPowerRecordEntity;
        } catch (Exception e) {
            log.error("保存电表记录失败，电表ID: {}, 错误信息: {}",
                    meterPowerRecordDto.getElectricMeterDetailDto().getMeterId(), e.getMessage(), e);
            throw new BusinessRuntimeException("保存电表记录失败: " + e.getMessage());
        }
    }

    /**
     * 构建电表记录实体
     */
    private ElectricMeterPowerRecordEntity buildMeterPowerRecordEntity(ElectricMeterPowerRecordDto dto) {
        return new ElectricMeterPowerRecordEntity()
                .setMeterId(dto.getElectricMeterDetailDto().getMeterId())
                .setMeterName(dto.getElectricMeterDetailDto().getMeterName())
                .setMeterNo(dto.getElectricMeterDetailDto().getMeterNo())
                .setIsPrepay(dto.getElectricMeterDetailDto().getIsPrepay())
                .setCt(dto.getElectricMeterDetailDto().getCt())
                .setAccountId(dto.getAccountId())
                .setPower(dto.getPower())
                .setPowerHigher(dto.getPowerHigher())
                .setPowerHigh(dto.getPowerHigh())
                .setPowerLow(dto.getPowerLow())
                .setPowerLower(dto.getPowerLower())
                .setPowerDeepLow(dto.getPowerDeepLow())
                .setOriginalReportId(dto.getOriginalReportId())
                .setRecordTime(dto.getRecordTime())
                .setCreateTime(LocalDateTime.now());
    }

    /**
     * 判断是否跳过消费计算
     */
    private boolean shouldSkipConsumeCalculation(ElectricMeterPowerRecordDto meterPowerRecordDto) {
        return BooleanUtil.isFalse(meterPowerRecordDto.getNeedConsume());
    }

    /**
     * 处理消费计算
     */
    private void processConsumeCalculation(ElectricMeterPowerRecordDto meterPowerRecordDto,
                                           ElectricMeterPowerRecordEntity meterPowerRecordEntity) {
        // 获取消费起始记录
        ElectricMeterPowerRecordEntity consumeBeginRecord = getConsumeBeginRecord(meterPowerRecordDto, meterPowerRecordEntity);
        if (consumeBeginRecord == null) {
            // 首次上报或时间异常，跳过计算
            return;
        }

        // 保存消费记录
        ElectricMeterPowerConsumeRecordEntity consumeRecord = saveConsumeRecord(meterPowerRecordDto, meterPowerRecordEntity, consumeBeginRecord);

        // 处理余额扣费
        if (shouldSkipBalanceDeduction(meterPowerRecordDto)) {
            return;
        }

        processBalanceDeduction(meterPowerRecordDto, meterPowerRecordEntity, consumeRecord);
    }

    /**
     * 获取消费起始记录
     */
    private ElectricMeterPowerRecordEntity getConsumeBeginRecord(ElectricMeterPowerRecordDto meterPowerRecordDto,
                                                                 ElectricMeterPowerRecordEntity meterPowerRecordEntity) {
        ElectricMeterPowerConsumeRecordEntity lastConsumeRecord =
                electricMeterPowerConsumeRecordRepository.getMeterLastConsumeRecord(meterPowerRecordEntity.getMeterId());

        if (lastConsumeRecord != null && meterPowerRecordDto.getAccountId().equals(lastConsumeRecord.getAccountId())) {
            return handleExistingConsumeRecord(lastConsumeRecord, meterPowerRecordEntity);
        } else {
            return handleFirstTimeOrDifferentAccount(meterPowerRecordEntity);
        }
    }

    /**
     * 处理已存在消费记录的情况
     */
    private ElectricMeterPowerRecordEntity handleExistingConsumeRecord(ElectricMeterPowerConsumeRecordEntity lastConsumeRecord,
                                                                       ElectricMeterPowerRecordEntity meterPowerRecordEntity) {
        if (lastConsumeRecord.getMeterConsumeTime().isAfter(meterPowerRecordEntity.getRecordTime())) {
            log.info("能耗用电：用电记录{}已包括电表记录{}时间的用电，无需重复计算",
                    lastConsumeRecord.getId(), meterPowerRecordEntity.getId());
            return null;
        }
        return electricMeterPowerRecordRepository.selectById(lastConsumeRecord.getEndRecordId());
    }

    /**
     * 处理首次上报或不同账户的情况
     */
    private ElectricMeterPowerRecordEntity handleFirstTimeOrDifferentAccount(ElectricMeterPowerRecordEntity meterPowerRecordEntity) {
        // 因为上面插入过一条，如果之前有记录那起码是两条
        final int MINIMUM_RECORD_COUNT = 2;
        final int PREVIOUS_RECORD_INDEX = 1;

        List<ElectricMeterPowerRecordEntity> recordList = electricMeterPowerRecordRepository.findRecordList(
                new ElectricMeterPowerRecordQo()
                        .setMeterId(meterPowerRecordEntity.getMeterId())
                        .setAccountId(meterPowerRecordEntity.getAccountId())
                        .setLimit(MINIMUM_RECORD_COUNT)
        );

        // 首次上报
        if (CollectionUtils.isEmpty(recordList) || recordList.size() < MINIMUM_RECORD_COUNT) {
            log.info("能耗用电：账户{}电表{}首次上报数据",
                    meterPowerRecordEntity.getAccountId(), meterPowerRecordEntity.getMeterId());
            return null;
        }

        return recordList.get(PREVIOUS_RECORD_INDEX);
    }

    /**
     * 保存消费记录
     */
    private ElectricMeterPowerConsumeRecordEntity saveConsumeRecord(ElectricMeterPowerRecordDto meterPowerRecordDto,
                                                                    ElectricMeterPowerRecordEntity meterPowerRecordEntity,
                                                                    ElectricMeterPowerRecordEntity consumeBeginRecord) {
        try {
            ElectricMeterPowerConsumeRecordEntity consumeRecord = buildConsumeRecordEntity(
                    meterPowerRecordDto, meterPowerRecordEntity, consumeBeginRecord);
            electricMeterPowerConsumeRecordRepository.insert(consumeRecord);
            log.debug("成功保存消费记录，电表ID: {}, 消费记录ID: {}, 消费电量: {}",
                    meterPowerRecordEntity.getMeterId(), consumeRecord.getId(), consumeRecord.getConsumePower());
            return consumeRecord;
        } catch (Exception e) {
            log.error("保存消费记录失败，电表ID: {}, 错误信息: {}",
                    meterPowerRecordEntity.getMeterId(), e.getMessage(), e);
            throw new BusinessRuntimeException("保存消费记录失败: " + e.getMessage());
        }
    }

    /**
     * 构建消费记录实体
     */
    private ElectricMeterPowerConsumeRecordEntity buildConsumeRecordEntity(ElectricMeterPowerRecordDto meterPowerRecordDto,
                                                                           ElectricMeterPowerRecordEntity meterPowerRecordEntity,
                                                                           ElectricMeterPowerRecordEntity consumeBeginRecord
    ) {
        LocalDateTime now = LocalDateTime.now();
        return new ElectricMeterPowerConsumeRecordEntity()
                .setMeterId(meterPowerRecordDto.getElectricMeterDetailDto().getMeterId())
                .setIsCalculate(meterPowerRecordDto.getElectricMeterDetailDto().getIsCalculate())
                .setCalculateType(meterPowerRecordDto.getElectricMeterDetailDto().getCalculateType() != null ?
                        meterPowerRecordDto.getElectricMeterDetailDto().getCalculateType().getCode() : null)
                .setAccountId(meterPowerRecordDto.getAccountId())
                .setSpaceId(meterPowerRecordDto.getElectricMeterDetailDto().getSpaceId())
                .setBeginRecordId(consumeBeginRecord.getId())
                .setBeginPower(consumeBeginRecord.getPower())
                .setBeginPowerHigher(consumeBeginRecord.getPowerHigher())
                .setBeginPowerHigh(consumeBeginRecord.getPowerHigh())
                .setBeginPowerLow(consumeBeginRecord.getPowerLow())
                .setBeginPowerLower(consumeBeginRecord.getPowerLower())
                .setBeginPowerDeepLow(consumeBeginRecord.getPowerDeepLow())
                .setBeginRecordTime(consumeBeginRecord.getRecordTime())
                .setEndRecordId(meterPowerRecordEntity.getId())
                .setEndPower(meterPowerRecordEntity.getPower())
                .setEndPowerHigher(meterPowerRecordEntity.getPowerHigher())
                .setEndPowerHigh(meterPowerRecordEntity.getPowerHigh())
                .setEndPowerLow(meterPowerRecordEntity.getPowerLow())
                .setEndPowerLower(meterPowerRecordEntity.getPowerLower())
                .setEndPowerDeepLow(meterPowerRecordEntity.getPowerDeepLow())
                .setEndRecordTime(meterPowerRecordEntity.getRecordTime())
                .setConsumePower(subtract(meterPowerRecordEntity.getPower(), consumeBeginRecord.getPower()))
                .setConsumePowerHigher(subtract(meterPowerRecordEntity.getPowerHigher(), consumeBeginRecord.getPowerHigher()))
                .setConsumePowerHigh(subtract(meterPowerRecordEntity.getPowerHigh(), consumeBeginRecord.getPowerHigh()))
                .setConsumePowerLow(subtract(meterPowerRecordEntity.getPowerLow(), consumeBeginRecord.getPowerLow()))
                .setConsumePowerLower(subtract(meterPowerRecordEntity.getPowerLower(), consumeBeginRecord.getPowerLower()))
                .setConsumePowerDeepLow(subtract(meterPowerRecordEntity.getPowerDeepLow(), consumeBeginRecord.getPowerDeepLow()))
                .setMeterConsumeTime(meterPowerRecordEntity.getRecordTime())
                .setCreateTime(now);
    }

    /**
     * 判断是否跳过余额扣费
     * 以下情况跳过扣费：1.非预付费电表 2.未开户电表 3.包月用户
     */
    private boolean shouldSkipBalanceDeduction(ElectricMeterPowerRecordDto meterPowerRecordDto) {
        ElectricMeterDetailDto meterDetail = meterPowerRecordDto.getElectricMeterDetailDto();

        // 非预付费电表跳过扣费
        if (BooleanUtil.isFalse(meterDetail.getIsPrepay())) {
            return true;
        }

        // 未开户电表跳过扣费
        if (meterPowerRecordDto.getAccountId() == null) {
            return true;
        }

        // 包月账户跳过扣费
        if (meterPowerRecordDto.getElectricAccountType() == null || ElectricAccountTypeEnum.MONTHLY.equals(meterPowerRecordDto.getElectricAccountType())) {
            return true;
        }

        return false;
    }

    /**
     * 处理余额扣费
     */
    private void processBalanceDeduction(ElectricMeterPowerRecordDto meterPowerRecordDto,
                                         ElectricMeterPowerRecordEntity meterPowerRecordEntity,
                                         ElectricMeterPowerConsumeRecordEntity consumeRecord) {
        // 获取价格计划详情
        ElectricPricePlanDetailBo planDetailBo = getPricePlanDetail(
                meterPowerRecordDto.getElectricMeterDetailDto().getPricePlanId());
        if (planDetailBo == null) {
            log.error("价格计划不存在，跳过余额扣费，pricePlanId: {}",
                    meterPowerRecordDto.getElectricMeterDetailDto().getPricePlanId());
            return;
        }

        // 创建余额消费记录
        ElectricMeterBalanceConsumeRecordEntity balanceConsumeRecord = createBalanceConsumeRecord(
                meterPowerRecordDto, meterPowerRecordEntity, consumeRecord);

        // 计算价格和消费金额
        calculatePriceAndAmount(balanceConsumeRecord, meterPowerRecordDto, consumeRecord, planDetailBo);

        // 执行余额扣费
        executeBalanceDeduction(balanceConsumeRecord, meterPowerRecordDto, meterPowerRecordEntity);

        // 保存余额消费记录
        electricMeterBalanceConsumeRecordRepository.insert(balanceConsumeRecord);
    }

    /**
     * 创建余额消费记录
     */
    private ElectricMeterBalanceConsumeRecordEntity createBalanceConsumeRecord(ElectricMeterPowerRecordDto meterPowerRecordDto,
                                                                               ElectricMeterPowerRecordEntity meterPowerRecordEntity,
                                                                               ElectricMeterPowerConsumeRecordEntity consumeRecord) {
        LocalDateTime now = LocalDateTime.now();
        ElectricMeterDetailDto meterDetail = meterPowerRecordDto.getElectricMeterDetailDto();

        ElectricMeterBalanceConsumeRecordEntity balanceConsumeRecord = new ElectricMeterBalanceConsumeRecordEntity()
                .setMeterConsumeRecordId(consumeRecord.getId())
                .setConsumeNo(SerialNumberGeneratorUtil.genUniqueNo(SerialNumberConstant.CONSUME_POWER_NO_PREFIX))
                .setConsumeType(ConsumeTypeEnum.ELECTRIC.getCode())
                .setMeterType(MeterTypeEnum.ELECTRIC.getCode())
                .setAccountId(meterPowerRecordDto.getAccountId())
                .setElectricAccountType(meterPowerRecordDto.getElectricAccountType().getCode())
                .setOwnerId(meterPowerRecordDto.getOwnerId())
                .setOwnerType(meterPowerRecordDto.getOwnerType().getCode())
                .setOwnerName(meterPowerRecordDto.getOwnerName())
                .setMeterId(meterDetail.getMeterId())
                .setMeterName(meterDetail.getMeterName())
                .setMeterNo(meterDetail.getMeterNo())
                .setSpaceId(meterDetail.getSpaceId())
                .setStepStartValue(meterDetail.getStepStartValue())
                .setHistoryPowerOffset(meterDetail.getHistoryPowerOffset())
                .setMeterConsumeTime(meterPowerRecordEntity.getRecordTime())
                .setCreateTime(now);

        // 设置空间信息
        SpaceInfoUtils.fillFromSpaceId(
                meterDetail.getSpaceId(),
                spaceService,
                balanceConsumeRecord::setSpaceName,
                null,
                null
        );

        return balanceConsumeRecord;
    }

    /**
     * 计算价格和消费金额
     */
    private void calculatePriceAndAmount(ElectricMeterBalanceConsumeRecordEntity balanceConsumeRecord,
                                         ElectricMeterPowerRecordDto meterPowerRecordDto,
                                         ElectricMeterPowerConsumeRecordEntity consumeRecord,
                                         ElectricPricePlanDetailBo planDetailBo) {
        // 计算价格倍率（阶梯电价）
        BigDecimal priceRate = calculatePriceRate(meterPowerRecordDto, consumeRecord, planDetailBo);

        // 设置各时段价格
        setPricesWithRate(balanceConsumeRecord, planDetailBo, priceRate);

        // 计算各时段消费金额
        calculateConsumeAmounts(consumeRecord, balanceConsumeRecord);
    }

    /**
     * 计算价格倍率（处理阶梯电价）
     */
    private BigDecimal calculatePriceRate(ElectricMeterPowerRecordDto meterPowerRecordDto,
                                          ElectricMeterPowerConsumeRecordEntity consumeRecord,
                                          ElectricPricePlanDetailBo planDetailBo) {
        BigDecimal priceRate = BigDecimal.ONE;
        if (BooleanUtil.isTrue(planDetailBo.getIsStep())) {
            BigDecimal usedPower = calculateUsedPower(meterPowerRecordDto, consumeRecord);
            try {
                priceRate = getStepValue(planDetailBo.getStepPrices(), usedPower);
            } catch (BusinessRuntimeException e) {
                log.warn("阶梯电价计算异常，使用默认电价。账户id：{}; 表id：{}; 价格方案id：{}。异常信息: {}",
                        meterPowerRecordDto.getAccountId(),
                        meterPowerRecordDto.getElectricMeterDetailDto().getMeterId(),
                        planDetailBo.getId(),
                        e.getMessage());
            }
        }
        return priceRate;
    }

    /**
     * 计算使用电量
     * 实际使用量 = 上报的电量 - 开户时的电量（第二年会统一成当时的电量） + 历史电量偏差
     */
    private BigDecimal calculateUsedPower(ElectricMeterPowerRecordDto meterPowerRecordDto, ElectricMeterPowerConsumeRecordEntity consumeRecord) {
        BigDecimal stepStartValue = Optional.ofNullable(
                        meterPowerRecordDto.getElectricMeterDetailDto().getStepStartValue())
                .orElse(BigDecimal.ZERO);
        BigDecimal historyPowerOffset = Optional.ofNullable(
                        meterPowerRecordDto.getElectricMeterDetailDto().getHistoryPowerOffset())
                .orElse(BigDecimal.ZERO);
        return consumeRecord.getEndPower()
                .subtract(stepStartValue)
                .add(historyPowerOffset);
    }

    /**
     * 设置各时段价格（应用价格倍率）
     */
    private void setPricesWithRate(ElectricMeterBalanceConsumeRecordEntity balanceConsumeRecord,
                                   ElectricPricePlanDetailBo planDetailBo,
                                   BigDecimal priceRate) {
        balanceConsumeRecord
                .setStepRate(priceRate)
                .setPricePlanId(planDetailBo.getId())
                .setPricePlanName(planDetailBo.getName())
                .setPriceHigher(planDetailBo.getPriceHigher().multiply(priceRate).setScale(8, RoundingMode.DOWN))
                .setPriceHigh(planDetailBo.getPriceHigh().multiply(priceRate).setScale(8, RoundingMode.DOWN))
                .setPriceLow(planDetailBo.getPriceLow().multiply(priceRate).setScale(8, RoundingMode.DOWN))
                .setPriceLower(planDetailBo.getPriceLower().multiply(priceRate).setScale(8, RoundingMode.DOWN))
                .setPriceDeepLow(planDetailBo.getPriceDeepLow().multiply(priceRate).setScale(8, RoundingMode.DOWN));
    }

    /**
     * 计算各时段消费金额
     */
    private void calculateConsumeAmounts(ElectricMeterPowerConsumeRecordEntity consumeRecord,
                                         ElectricMeterBalanceConsumeRecordEntity balanceConsumeRecord) {
        // 计算各时段消费金额：用电量 × 单价
        BigDecimal higherPeriodAmount = multiply(consumeRecord.getConsumePowerHigher(), balanceConsumeRecord.getPriceHigher());
        BigDecimal highPeriodAmount = multiply(consumeRecord.getConsumePowerHigh(), balanceConsumeRecord.getPriceHigh());
        BigDecimal lowPeriodAmount = multiply(consumeRecord.getConsumePowerLow(), balanceConsumeRecord.getPriceLow());
        BigDecimal lowerPeriodAmount = multiply(consumeRecord.getConsumePowerLower(), balanceConsumeRecord.getPriceLower());
        BigDecimal deepLowPeriodAmount = multiply(consumeRecord.getConsumePowerDeepLow(), balanceConsumeRecord.getPriceDeepLow());

        // 计算总消费金额
        BigDecimal totalConsumeAmount = sum(higherPeriodAmount, highPeriodAmount, lowPeriodAmount, lowerPeriodAmount, deepLowPeriodAmount);

        // 设置消费金额
        balanceConsumeRecord
                .setConsumeAmount(totalConsumeAmount)
                .setConsumeAmountHigher(higherPeriodAmount)
                .setConsumeAmountHigh(highPeriodAmount)
                .setConsumeAmountLow(lowPeriodAmount)
                .setConsumeAmountLower(lowerPeriodAmount)
                .setConsumeAmountDeepLow(deepLowPeriodAmount);
    }

    /**
     * 执行余额扣费
     */
    private void executeBalanceDeduction(
            ElectricMeterBalanceConsumeRecordEntity balanceConsumeRecord,
            ElectricMeterPowerRecordDto meterPowerRecordDto,
            ElectricMeterPowerRecordEntity meterPowerRecordEntity) {
        try {
            BalanceDto topUpDto = createTopUpDto(meterPowerRecordDto, meterPowerRecordEntity, balanceConsumeRecord);

            // 执行余额扣费
            balanceService.deduct(topUpDto);
            log.debug("成功执行余额扣费，订单号: {}, 扣费金额: {}",
                    topUpDto.getOrderNo(), balanceConsumeRecord.getConsumeAmount());

            // 查询扣费后余额
            BalanceBo balanceBo;
            try {
                balanceBo = balanceService.query(new BalanceQueryDto()
                        .setBalanceType(topUpDto.getBalanceType())
                        .setBalanceRelationId(topUpDto.getBalanceRelationId()));
            } catch (NotFoundException e) {
                throw new BusinessRuntimeException("查询余额信息失败，无法获取扣费后余额");
            }

            // 设置余额信息
            balanceConsumeRecord
                    .setEndBalance(balanceBo.getBalance())
                    .setBeginBalance(balanceBo.getBalance().add(balanceConsumeRecord.getConsumeAmount()));

        } catch (Exception e) {
            log.error("余额扣费异常，电表ID: {}, 消费金额: {}, 错误信息: {}",
                    meterPowerRecordEntity.getMeterId(), balanceConsumeRecord.getConsumeAmount(), e.getMessage(), e);
            throw new BusinessRuntimeException("余额扣费失败: " + e.getMessage());
        }
    }

    /**
     * 创建充值DTO（用于余额扣费）
     */
    private BalanceDto createTopUpDto(ElectricMeterPowerRecordDto meterPowerRecordDto,
                                      ElectricMeterPowerRecordEntity meterPowerRecordEntity,
                                      ElectricMeterBalanceConsumeRecordEntity balanceConsumeRecord) {
        BalanceDto topUpDto = new BalanceDto()
                .setAccountId(meterPowerRecordEntity.getAccountId())
                .setOrderNo(balanceConsumeRecord.getConsumeNo())
                .setAmount(balanceConsumeRecord.getConsumeAmount());

        if (ElectricAccountTypeEnum.QUANTITY.equals(meterPowerRecordDto.getElectricAccountType())) {
            topUpDto.setBalanceType(BalanceTypeEnum.ELECTRIC_METER)
                    .setBalanceRelationId(meterPowerRecordEntity.getMeterId());
        } else {
            topUpDto.setBalanceType(BalanceTypeEnum.ACCOUNT)
                    .setBalanceRelationId(meterPowerRecordEntity.getAccountId());
        }

        return topUpDto;
    }

    /**
     * 保存电表关系记录
     */
    private void saveElectricMeterPowerRelation(ElectricMeterPowerRecordDto electricMeterPowerRecordDto, Integer recordId) {
        try {
            ElectricMeterPowerRelationEntity relationEntity = buildElectricMeterPowerRelationEntity(
                    electricMeterPowerRecordDto, recordId);

            enrichRelationEntityData(relationEntity);

            electricMeterPowerRelationRepository.insert(relationEntity);
            log.debug("成功保存电表关系记录，电表ID: {}, 关系记录ID: {}",
                    electricMeterPowerRecordDto.getElectricMeterDetailDto().getMeterId(), relationEntity.getId());
        } catch (Exception e) {
            log.error("保存电表关系记录失败，电表ID: {}, 错误信息: {}",
                    electricMeterPowerRecordDto.getElectricMeterDetailDto().getMeterId(), e.getMessage(), e);
            throw new BusinessRuntimeException("保存电表关系记录失败: " + e.getMessage());
        }
    }

    /**
     * 构建电表关系实体基础信息
     */
    private ElectricMeterPowerRelationEntity buildElectricMeterPowerRelationEntity(
            ElectricMeterPowerRecordDto electricMeterPowerRecordDto, Integer recordId) {
        return new ElectricMeterPowerRelationEntity()
                .setRecordId(recordId)
                .setMeterId(electricMeterPowerRecordDto.getElectricMeterDetailDto().getMeterId())
                .setIsCalculate(electricMeterPowerRecordDto.getElectricMeterDetailDto().getIsCalculate())
                .setCalculateType(electricMeterPowerRecordDto.getElectricMeterDetailDto().getCalculateType() != null ?
                        electricMeterPowerRecordDto.getElectricMeterDetailDto().getCalculateType().getCode() : null)
                .setSpaceId(electricMeterPowerRecordDto.getElectricMeterDetailDto().getSpaceId())
                .setAccountId(electricMeterPowerRecordDto.getAccountId())
                .setElectricAccountType(electricMeterPowerRecordDto.getElectricAccountType().getCode())
                .setOwnerId(electricMeterPowerRecordDto.getOwnerId())
                .setOwnerType(electricMeterPowerRecordDto.getOwnerType().getCode())
                .setRecordTime(electricMeterPowerRecordDto.getRecordTime())
                .setCreateTime(LocalDateTime.now());
    }

    /**
     * 补充关系实体数据
     */
    private void enrichRelationEntityData(ElectricMeterPowerRelationEntity relationEntity) {
        // 设置计算类型名称
        setCalculateTypeName(relationEntity);

        // 设置空间信息
        SpaceInfoUtils.fillFromSpaceId(
                relationEntity.getSpaceId(),
                spaceService,
                relationEntity::setSpaceName,
                relationEntity::setSpaceParentIds,
                relationEntity::setSpaceParentNames
        );

        // 设置组织信息
        setOrganizationInfo(relationEntity);
    }

    /**
     * 设置计算类型名称
     */
    private void setCalculateTypeName(ElectricMeterPowerRelationEntity relationEntity) {
        if (relationEntity.getCalculateType() != null) {
            CalculateTypeEnum calculateTypeEnum = CodeEnum.fromCode(
                    relationEntity.getCalculateType(), CalculateTypeEnum.class);
            if (calculateTypeEnum != null) {
                relationEntity.setCalculateTypeName(calculateTypeEnum.getInfo());
            }
        }
    }

    /**
     * 获取价格计划详情（带空值检查）
     */
    private ElectricPricePlanDetailBo getPricePlanDetail(Integer pricePlanId) {
        if (pricePlanId == null) {
            return null;
        }
        try {
            return electricPricePlanService.getDetail(pricePlanId);
        } catch (Exception e) {
            log.warn("获取价格计划详情失败，pricePlanId: {}", pricePlanId, e);
            return null;
        }
    }

    /**
     * 获取组织详情（带空值检查）
     */
    private OrganizationBo getOrganizationDetail(Integer ownerId, Integer ownerType) {
        if (ownerId == null || !OwnerTypeEnum.ENTERPRISE.getCode().equals(ownerType)) {
            return null;
        }
        try {
            return organizationService.getDetail(ownerId);
        } catch (Exception e) {
            log.warn("获取组织详情失败，ownerId: {}", ownerId, e);
            return null;
        }
    }

    /**
     * 设置组织信息
     */
    private void setOrganizationInfo(ElectricMeterPowerRelationEntity relationEntity) {
        // 没有开户，不记录组织信息
        if (relationEntity.getOwnerId() == null || relationEntity.getOwnerType() == null || relationEntity.getAccountId() == null) {
            return;
        }

        OrganizationBo organizationBo = getOrganizationDetail(relationEntity.getOwnerId(), relationEntity.getOwnerType());

        if (organizationBo != null) {
            relationEntity.setOwnerName(organizationBo.getManagerName());
        }
    }

    private Lock getMeterLock(Integer meterId) {
        return lockTemplate.getLock(String.format(LOCK_METER, meterId));
    }

    private BigDecimal subtract(BigDecimal a, BigDecimal b) {
        if (a == null || b == null) {
            return null;
        }
        return a.subtract(b);
    }

    private BigDecimal multiply(BigDecimal a, BigDecimal b) {
        if (a == null || b == null) {
            return null;
        }
        return a.multiply(b);
    }

    private BigDecimal sum(BigDecimal... ele) {
        return Arrays.stream(ele).map(e -> Optional.ofNullable(e).orElse(BigDecimal.ZERO)).reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    /**
     * 获取阶梯电价倍率
     *
     * @param stepPriceList 阶梯价格列表
     * @param currentAmount 当前用电量
     * @return 阶梯电价倍率
     */
    private BigDecimal getStepValue(List<StepPriceBo> stepPriceList, BigDecimal currentAmount) {
        if (CollectionUtils.isEmpty(stepPriceList)) {
            throw new BusinessRuntimeException("阶梯价格配置为空，无法计算阶梯电价");
        }

        if (currentAmount == null) {
            throw new BusinessRuntimeException("用电量为空，无法计算阶梯电价");
        }

        // 查找匹配的阶梯价格
        for (StepPriceBo stepPrice : stepPriceList) {
            if (isAmountInStep(currentAmount, stepPrice)) {
                return stepPrice.getValue();
            }
        }

        throw new BusinessRuntimeException(
                String.format("当前用电量 %s 不在任何阶梯范围内，无法计算阶梯电价", currentAmount));
    }

    /**
     * 判断用电量是否在指定阶梯范围内
     *
     * @param amount    用电量
     * @param stepPrice 阶梯价格配置
     * @return 是否在范围内
     */
    private boolean isAmountInStep(BigDecimal amount, StepPriceBo stepPrice) {
        if (stepPrice.getStart() == null) {
            return false;
        }

        // 检查下限
        if (amount.compareTo(stepPrice.getStart()) < 0) {
            return false;
        }

        // 检查上限（如果存在）
        if (stepPrice.getEnd() != null) {
            return amount.compareTo(stepPrice.getEnd()) < 0;
        }

        // 无上限的情况
        return true;
    }

    /**
     * 查询电量消费记录
     *
     * @param queryDto  查询条件
     * @param pageParam 分页参数
     * @return 电量消费记录分页结果
     */
    @Override
    public PageResult<PowerConsumeRecordDto> findPowerConsumePage(@Valid @NotNull PowerConsumeQueryDto queryDto, @NotNull PageParam pageParam) {
        // 构建查询对象
        ElectricPowerConsumeRecordQo qo = new ElectricPowerConsumeRecordQo()
                .setConsumeType(ConsumeTypeEnum.ELECTRIC.getCode())
                .setMeterNameLike(queryDto.getMeterName())
                .setSpaceNameLike(queryDto.getSpaceName())
                .setBeginTime(queryDto.getBeginTime())
                .setEndTime(queryDto.getEndTime());

        // 分页查询
        try (Page<ElectricMeterBalanceConsumeRecordEntity> page = PageHelper.startPage(pageParam.getPageNum(), pageParam.getPageSize())) {
            PageInfo<ElectricMeterBalanceConsumeRecordEntity> pageInfo = page.doSelectPageInfo(() -> electricMeterBalanceConsumeRecordRepository.selectByQo(qo));

            // 转换为DTO
            List<PowerConsumeRecordDto> records = pageInfo.getList().stream()
                    .map(this::convertToConsumeDto)
                    .collect(Collectors.toList());

            return new PageResult<PowerConsumeRecordDto>()
                    .setPageNum(pageParam.getPageNum())
                    .setPageSize(pageParam.getPageSize())
                    .setTotal(pageInfo.getTotal())
                    .setList(records);
        }
    }

    /**
     * 指定金额补正
     *
     * @param correctMeterAmountDto 补正金额
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void correctByAmount(@NotNull @Valid CorrectMeterAmountDto correctMeterAmountDto) {
        BalanceTypeEnum balanceType;
        Integer balanceRelationId;

        if (ElectricAccountTypeEnum.QUANTITY.equals(correctMeterAmountDto.getElectricAccountType())) {
            if (correctMeterAmountDto.getMeterId() == null) {
                throw new BusinessRuntimeException("按需计费补正必须指定电表信息");
            }
            balanceType = BalanceTypeEnum.ELECTRIC_METER;
            balanceRelationId = correctMeterAmountDto.getMeterId();
        } else if (ElectricAccountTypeEnum.MERGED.equals(correctMeterAmountDto.getElectricAccountType())) {
            balanceType = BalanceTypeEnum.ACCOUNT;
            balanceRelationId = correctMeterAmountDto.getAccountId();
        } else {
            throw new BusinessRuntimeException("包月账户不支持补正");
        }

        String consumeNo = SerialNumberGeneratorUtil.genUniqueNo(SerialNumberConstant.CONSUME_POWER_NO_PREFIX);
        BigDecimal correctionAmount = MoneyUtil.scaleToCent(correctMeterAmountDto.getAmount());

        BalanceDto balanceDto = new BalanceDto()
                .setBalanceRelationId(balanceRelationId)
                .setBalanceType(balanceType)
                .setAccountId(correctMeterAmountDto.getAccountId())
                .setOrderNo(consumeNo)
                .setAmount(correctionAmount);

        // 执行账户补正
        BigDecimal recordAmount;
        if (CorrectionTypeEnum.PAY.equals(correctMeterAmountDto.getCorrectionType())) {
            recordAmount = correctionAmount;
            balanceService.deduct(balanceDto);
        } else if (CorrectionTypeEnum.REFUND.equals(correctMeterAmountDto.getCorrectionType())) {
            recordAmount = correctionAmount.negate();
            balanceService.topUp(balanceDto);
        } else {
            throw new ParamException("不支持的补正类型");
        }

        // 获取最新账户余额
        BalanceBo balanceBo = balanceService.query(new BalanceQueryDto()
                .setBalanceType(balanceType)
                .setBalanceRelationId(balanceRelationId));

        LocalDateTime now = LocalDateTime.now();
        // 构建补正的记录
        ElectricMeterBalanceConsumeRecordEntity record = new ElectricMeterBalanceConsumeRecordEntity()
                .setConsumeNo(consumeNo)
                .setConsumeType(ConsumeTypeEnum.CORRECTION.getCode())
                .setMeterType(MeterTypeEnum.ELECTRIC.getCode())
                .setAccountId(correctMeterAmountDto.getAccountId())
                .setElectricAccountType(correctMeterAmountDto.getElectricAccountType().getCode())
                .setOwnerId(correctMeterAmountDto.getOwnerId())
                .setOwnerType(correctMeterAmountDto.getOwnerType().getCode())
                .setOwnerName(correctMeterAmountDto.getOwnerName())
                .setMeterId(correctMeterAmountDto.getMeterId())
                .setMeterName(correctMeterAmountDto.getMeterName())
                .setMeterNo(correctMeterAmountDto.getMeterNo())
                .setConsumeAmount(recordAmount)
                .setEndBalance(balanceBo.getBalance())
                .setBeginBalance(balanceBo.getBalance().add(recordAmount))
                .setRemark(buildCorrectionRemark(correctMeterAmountDto))
                .setMeterConsumeTime(Optional.ofNullable(correctMeterAmountDto.getCorrectionTime()).orElse(now))
                .setCreateTime(now);
        electricMeterBalanceConsumeRecordRepository.insert(record);

        log.info("指定金额补正完成，accountId={}, amount={}", correctMeterAmountDto.getAccountId(), correctionAmount);
    }


    private String buildCorrectionRemark(CorrectMeterAmountDto dto) {
        String operator = Optional.ofNullable(requestContext.getUserRealName()).orElse("");
        return String.format("补正原因:%s, 操作人:%s", dto.getReason(), operator);
    }

    /**
     * 补正记录分页查询
     *
     * @param queryDto 查询参数
     * @param pageParam 分页参数
     * @return 补正记录
     */
    @Override
    public PageResult<MeterCorrectionRecordDto> findCorrectionRecordPage(@NotNull @Valid MeterCorrectionRecordQueryDto queryDto, @NotNull PageParam pageParam) {
        ElectricPowerConsumeRecordQo qo = new ElectricPowerConsumeRecordQo()
                .setAccountId(queryDto.getAccountId())
                .setMeterId(queryDto.getMeterId())
                .setMeterNameLike(queryDto.getMeterName())
                .setBeginTime(queryDto.getBeginTime())
                .setEndTime(queryDto.getEndTime())
                .setConsumeType(ConsumeTypeEnum.CORRECTION.getCode());

        try (Page<ElectricMeterBalanceConsumeRecordEntity> page = PageHelper.startPage(pageParam.getPageNum(), pageParam.getPageSize())) {
            PageInfo<ElectricMeterBalanceConsumeRecordEntity> pageInfo = page.doSelectPageInfo(() -> electricMeterBalanceConsumeRecordRepository.selectByQo(qo));
            List<MeterCorrectionRecordDto> records = pageInfo.getList().stream()
                    .map(this::convertCorrectionRecord)
                    .collect(Collectors.toList());

            return new PageResult<MeterCorrectionRecordDto>()
                    .setPageNum(pageParam.getPageNum())
                    .setPageSize(pageParam.getPageSize())
                    .setTotal(pageInfo.getTotal())
                    .setList(records);
        }
    }

    /**
     * 将实体转换为DTO
     */
    private PowerConsumeRecordDto convertToConsumeDto(ElectricMeterBalanceConsumeRecordEntity entity) {
        return new PowerConsumeRecordDto()
                .setId(entity.getId())
                .setAccountId(entity.getAccountId())
                .setMeterId(entity.getMeterId())
                .setConsumeNo(entity.getConsumeNo())
                .setOwnerId(entity.getOwnerId())
                .setOwnerType(entity.getOwnerType())
                .setOwnerName(entity.getOwnerName())
                .setMeterName(entity.getMeterName())
                .setMeterNo(entity.getMeterNo())
                .setSpaceName(entity.getSpaceName())
                .setBeginBalance(entity.getBeginBalance())
                .setConsumeAmount(entity.getConsumeAmount())
                .setEndBalance(entity.getEndBalance())
                .setMergedMeasure(ElectricAccountTypeEnum.MERGED.getCode().equals(entity.getElectricAccountType()))
                .setConsumeTime(entity.getMeterConsumeTime());
    }

    private MeterCorrectionRecordDto convertCorrectionRecord(ElectricMeterBalanceConsumeRecordEntity entity) {
        return new MeterCorrectionRecordDto()
                .setConsumeNo(entity.getConsumeNo())
                .setAccountId(entity.getAccountId())
                .setOwnerId(entity.getOwnerId())
                .setOwnerType(entity.getOwnerType())
                .setOwnerName(entity.getOwnerName())
                .setMeterId(entity.getMeterId())
                .setMeterName(entity.getMeterName())
                .setMeterNo(entity.getMeterNo())
                .setConsumeAmount(entity.getConsumeAmount())
                .setBeginBalance(entity.getBeginBalance())
                .setEndBalance(entity.getEndBalance())
                .setRemark(entity.getRemark())
                .setMeterConsumeTime(entity.getMeterConsumeTime());
    }

}
