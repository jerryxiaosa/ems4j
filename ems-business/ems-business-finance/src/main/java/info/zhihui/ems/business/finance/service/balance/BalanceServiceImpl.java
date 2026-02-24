package info.zhihui.ems.business.finance.service.balance;

import info.zhihui.ems.common.enums.CodeEnum;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.business.finance.bo.BalanceBo;
import info.zhihui.ems.business.finance.dto.BalanceDeleteDto;
import info.zhihui.ems.business.finance.dto.BalanceDto;
import info.zhihui.ems.business.finance.dto.BalanceListQueryDto;
import info.zhihui.ems.business.finance.dto.BalanceQueryDto;
import info.zhihui.ems.business.finance.entity.BalanceEntity;
import info.zhihui.ems.business.finance.entity.OrderFlowEntity;
import info.zhihui.ems.common.enums.BalanceTypeEnum;
import info.zhihui.ems.business.finance.qo.BalanceListQueryQo;
import info.zhihui.ems.business.finance.qo.BalanceQo;
import info.zhihui.ems.business.finance.repository.BalanceRepository;
import info.zhihui.ems.business.finance.repository.OrderFlowRepository;
import info.zhihui.ems.mq.api.model.MqMessage;
import info.zhihui.ems.mq.api.constant.finance.FinanceMqConstant;
import info.zhihui.ems.mq.api.message.finance.BalanceChangedMessage;
import info.zhihui.ems.mq.api.service.MqService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 账户余额操作接口
 *
 * @author jerryxiaosa
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Validated
public class BalanceServiceImpl implements BalanceService {
    private final OrderFlowRepository orderFlowRepository;
    private final BalanceRepository balanceRepository;
    private final MqService mqService;

    /**
     * 账户余额充值
     *
     * @param topUpDto 充值参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void topUp(@Valid @NotNull BalanceDto topUpDto) {
        balanceProcess(topUpDto);
    }

    /**
     * 账户余额扣除
     *
     * @param deductDto 扣除参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deduct(@Valid @NotNull BalanceDto deductDto) {
        BalanceDto balanceDto = new BalanceDto()
                .setOrderNo(deductDto.getOrderNo())
                .setAccountId(deductDto.getAccountId())
                .setBalanceType(deductDto.getBalanceType())
                .setBalanceRelationId(deductDto.getBalanceRelationId())
                .setAmount(deductDto.getAmount().negate());
        balanceProcess(balanceDto);
    }

    private void balanceProcess(BalanceDto topUpDto) {
        try {
            OrderFlowEntity orderFlowEntity = new OrderFlowEntity()
                    .setConsumeId(topUpDto.getOrderNo())
                    .setAmount(topUpDto.getAmount())
                    .setBalanceRelationId(topUpDto.getBalanceRelationId())
                    .setBalanceType(topUpDto.getBalanceType().getCode())
                    .setAccountId(topUpDto.getAccountId())
                    .setCreateTime(LocalDateTime.now());
            orderFlowRepository.insert(orderFlowEntity);
        } catch (DuplicateKeyException e) {
            log.warn("订单{}不能重复操作，跳过操作", topUpDto.getOrderNo());
            throw new BusinessRuntimeException("订单不能重复操作，订单号：" + topUpDto.getOrderNo());
        }

        Integer updateRow = balanceRepository.balanceTopUp(new BalanceQo()
                .setBalanceRelationId(topUpDto.getBalanceRelationId())
                .setBalanceType(topUpDto.getBalanceType().getCode())
                .setAccountId(topUpDto.getAccountId())
                .setAmount(topUpDto.getAmount())
        );
        if (updateRow == null || updateRow != 1) {
            log.error("账户充值异常，未更新账户余额，参数：{}", topUpDto);
            throw new BusinessRuntimeException("账户余额结算异常，请重试");
        }

        publishBalanceChangedEvent(topUpDto);
    }

    /**
     * 账户余额查询
     *
     * @param queryDto 查询参数
     * @return 账户余额
     */
    @Override
    public BalanceBo getByQuery(@Valid @NotNull BalanceQueryDto queryDto) throws NotFoundException {
        BalanceBo balanceBo = findFirstByQuery(new BalanceListQueryDto()
                .setBalanceType(queryDto.getBalanceType())
                .setBalanceRelationIds(List.of(queryDto.getBalanceRelationId()))
        );
        if (balanceBo == null) {
            throw new NotFoundException("查询余额信息失败，余额信息不存在");
        }
        return balanceBo;
    }

    /**
     * 按账户ID批量查询余额明细（仅未删除数据）
     *
     * @param accountIds 账户ID列表
     * @return 余额明细列表
     */
    @Override
    public List<BalanceBo> findListByAccountIds(@NotEmpty List<@NotNull Integer> accountIds) {
        return findListByQuery(new BalanceListQueryDto().setAccountIds(accountIds));
    }

    /**
     * 初始化账户余额
     *
     * @param accountId 账户ID
     */
    @Override
    public void initAccountBalance(@NotNull Integer accountId) {
        initBalanceByType(accountId, BalanceTypeEnum.ACCOUNT, accountId);
    }

    /**
     * 初始化电表余额
     *
     * @param electricMeterId 电表ID
     * @param accountId       账户ID
     */
    @Override
    public void initElectricMeterBalance(@NotNull Integer electricMeterId, @NotNull Integer accountId) {
        initBalanceByType(electricMeterId, BalanceTypeEnum.ELECTRIC_METER, accountId);
    }

    /**
     * 软删除账户余额
     *
     * @param deleteDto 删除参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBalance(@Valid @NotNull BalanceDeleteDto deleteDto) {
        log.info("开始软删除账户余额，参数：{}", deleteDto);

        // @NOTICE
        // 先查询余额是否存在，再删除仍然有可能删除余额不为0的账户
        // 后续优化
        BalanceQo queryQo = new BalanceQo()
                .setBalanceType(deleteDto.getBalanceType().getCode())
                .setBalanceRelationId(deleteDto.getBalanceRelationId());

        // 执行软删除操作
        Integer deleteRow = balanceRepository.deleteBalance(queryQo);
        if (deleteRow != 1) {
            log.error("软删除账户余额异常，参数：{}", deleteDto);
            throw new BusinessRuntimeException("账户余额删除异常，请重试");
        }

        log.info("账户余额软删除成功，参数：{}", deleteDto);
    }

    private void initBalanceByType(Integer relationId, BalanceTypeEnum balanceType, Integer accountId) {
        try {
            BalanceEntity entity = new BalanceEntity()
                    .setBalanceRelationId(relationId)
                    .setBalanceType(balanceType.getCode())
                    .setBalance(BigDecimal.ZERO)
                    .setAccountId(accountId);

            balanceRepository.insert(entity);
        } catch (DuplicateKeyException e) {
            log.error("初始化账户余额异常，参数：{}", accountId);
            throw new BusinessRuntimeException("账户余额初始化异常，账户已存在，请修复数据");
        }
    }

    /**
     * 通用余额明细查询，供单条查询与列表查询复用。
     */
    private List<BalanceBo> findListByQuery(BalanceListQueryDto queryDto) {
        List<Integer> accountIdList = normalizeIdList(queryDto.getAccountIds());
        List<Integer> balanceRelationIdList = normalizeIdList(queryDto.getBalanceRelationIds());
        BalanceTypeEnum balanceType = queryDto.getBalanceType();

        if (accountIdList.isEmpty() && balanceRelationIdList.isEmpty() && balanceType == null) {
            return Collections.emptyList();
        }

        BalanceListQueryQo queryQo = new BalanceListQueryQo()
                .setAccountIds(accountIdList)
                .setBalanceRelationIds(balanceRelationIdList)
                .setBalanceType(balanceType == null ? null : balanceType.getCode());
        List<BalanceEntity> entityList = balanceRepository.findListByQuery(queryQo);
        if (entityList == null || entityList.isEmpty()) {
            return Collections.emptyList();
        }

        return entityList.stream()
                .filter(Objects::nonNull)
                .map(this::toBalanceBo)
                .toList();
    }

    /**
     * 通用单条余额查询，供强语义查询与事件发布复用。
     */
    private BalanceBo findFirstByQuery(BalanceListQueryDto queryDto) {
        List<BalanceBo> balanceBoList = findListByQuery(queryDto);
        if (balanceBoList.isEmpty()) {
            return null;
        }
        return balanceBoList.get(0);
    }

    private List<Integer> normalizeIdList(List<Integer> idList) {
        if (idList == null || idList.isEmpty()) {
            return Collections.emptyList();
        }
        return idList.stream()
                .distinct()
                .toList();
    }

    private BalanceBo toBalanceBo(BalanceEntity entity) {
        return new BalanceBo()
                .setId(entity.getId())
                .setBalanceRelationId(entity.getBalanceRelationId())
                .setBalanceType(CodeEnum.fromCode(entity.getBalanceType(), BalanceTypeEnum.class))
                .setAccountId(entity.getAccountId())
                .setBalance(entity.getBalance());
    }

    private void publishBalanceChangedEvent(BalanceDto balanceDto) {
        BalanceBo latest = findFirstByQuery(new BalanceListQueryDto()
                .setBalanceType(balanceDto.getBalanceType())
                .setBalanceRelationIds(List.of(balanceDto.getBalanceRelationId()))
        );
        if (latest == null) {
            log.warn("余额信息不存在，余额变动事件未发布：{}", balanceDto);
            return;
        }

        BalanceChangedMessage message = new BalanceChangedMessage()
                .setAccountId(balanceDto.getAccountId())
                .setBalanceType(balanceDto.getBalanceType())
                .setBalanceRelationId(balanceDto.getBalanceRelationId())
                .setChangeAmount(balanceDto.getAmount())
                .setNewBalance(latest.getBalance())
                .setEventTime(LocalDateTime.now());

        MqMessage mqMessage = new MqMessage()
                .setMessageDestination(FinanceMqConstant.FINANCE_DESTINATION)
                .setRoutingIdentifier(FinanceMqConstant.ROUTING_KEY_BALANCE_CHANGED)
                .setPayload(message);
        mqService.sendMessageAfterCommit(mqMessage);
    }

}
