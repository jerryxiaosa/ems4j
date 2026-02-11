package info.zhihui.ems.mq.rabbitmq.service.impl;

import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.mq.api.model.MqMessage;
import info.zhihui.ems.common.utils.JacksonUtil;
import info.zhihui.ems.mq.api.bo.TransactionMessageBo;
import info.zhihui.ems.mq.api.dto.TransactionMessageDto;
import info.zhihui.ems.mq.api.enums.TransactionMessageBusinessTypeEnum;
import info.zhihui.ems.mq.api.service.TransactionMessageService;
import info.zhihui.ems.mq.rabbitmq.entity.TransactionMessageEntity;
import info.zhihui.ems.mq.rabbitmq.repository.TransactionMessageRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 事务消息服务实现类
 *
 * @author jerryxiaosa
 */
@Slf4j
@Validated
public class TransactionMessageServiceImpl implements TransactionMessageService {
    /**
     * 指数退避基础秒数。
     */
    private static final long RETRY_BASE_SECONDS = 60L;

    /**
     * 指数退避最大秒数上限。
     */
    private static final long RETRY_MAX_SECONDS = 3600L;

    /**
     * 单次查询补拉的最大扫描轮次，避免长时间占用数据库。
     */
    private static final int MAX_SCAN_ROUNDS = 10;

    /**
     * 最大重试次数。
     */
    private final Integer maxRetryTimes;

    /**
     * 每次查询的最大条数。
     */
    private final Integer fetchSize;

    private final TransactionMessageRepository transactionMessageRepository;


    public TransactionMessageServiceImpl(TransactionMessageRepository transactionMessageRepository,
                                         Integer maxRetryTimes,
                                         Integer fetchSize) {
        if (maxRetryTimes == null || maxRetryTimes < 1) {
            throw new IllegalArgumentException("maxRetryTimes 必须大于0");
        }
        if (fetchSize == null || fetchSize < 1) {
            throw new IllegalArgumentException("fetchSize 必须大于0");
        }
        this.transactionMessageRepository = transactionMessageRepository;
        this.maxRetryTimes = maxRetryTimes;
        this.fetchSize = fetchSize;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(@Valid @NotNull TransactionMessageDto dto) {
        try {
            TransactionMessageEntity entity = new TransactionMessageEntity()
                    .setBusinessType(dto.getBusinessType().name())
                    .setSn(dto.getSn())
                    .setDestination(dto.getMessage().getMessageDestination())
                    .setRoute(dto.getMessage().getRoutingIdentifier())
                    .setPayloadType(resolvePayloadType(dto.getMessage().getPayload()))
                    .setPayload(JacksonUtil.toJson(dto.getMessage().getPayload()))
                    .setCreateTime(LocalDateTime.now())
                    .setTryTimes(0)
                    .setIsSuccess(false);

            int result = transactionMessageRepository.insert(entity);
            if (result != 1) {
                log.error("新增事务消息失败，事务消息未落库，businessType: {}, sn: {}", dto.getBusinessType(), dto.getSn());
                throw new BusinessRuntimeException("新增事务消息失败，事务消息未落库");
            }
        } catch (BusinessRuntimeException e) {
            // 保留BusinessRuntimeException异常内容
            throw e;
        } catch (Exception e) {
            log.error("新增事务消息失败，businessType: {}, sn: {}", dto.getBusinessType(), dto.getSn(), e);
            throw new BusinessRuntimeException("新增事务消息失败");
        }
    }

    @Override
    public void success(@NotNull TransactionMessageBusinessTypeEnum businessType, @NotBlank String sn) {
        update(businessType, sn, true);
    }

    @Override
    public void failure(@NotNull TransactionMessageBusinessTypeEnum businessType, @NotBlank String sn) {
        update(businessType, sn, false);
    }

    private void update(TransactionMessageBusinessTypeEnum businessType, String sn, boolean isSuccess) {
        try {
            TransactionMessageEntity entity = new TransactionMessageEntity()
                    .setBusinessType(businessType.name())
                    .setSn(sn)
                    .setLastRunAt(LocalDateTime.now())
                    .setIsSuccess(isSuccess);
            transactionMessageRepository.updateTransactionMessage(entity);
        } catch (Exception e) {
            log.error("标记事务消息成功失败，businessType: {}, sn: {}", businessType, sn, e);
        }
    }

    @Override
    public List<TransactionMessageBo> findRecentFailureRecords() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime twoDaysAgo = now.minusDays(2);
            List<TransactionMessageBo> messageBoList = new ArrayList<>();

            LocalDateTime cursorCreateTime = null;
            Integer cursorId = null;
            int scanRounds = 0;
            while (messageBoList.size() < fetchSize && scanRounds < MAX_SCAN_ROUNDS) {
                scanRounds++;
                List<TransactionMessageEntity> entityList = transactionMessageRepository.getPastUnsuccessfulWithCursor(
                        maxRetryTimes,
                        twoDaysAgo,
                        cursorCreateTime,
                        cursorId,
                        fetchSize
                );

                if (entityList == null || entityList.isEmpty()) {
                    break;
                }

                for (TransactionMessageEntity entity : entityList) {
                    try {
                        TransactionMessageBo messageBo = entityToBo(entity);
                        if (shouldRetryNow(messageBo, now)) {
                            messageBoList.add(messageBo);
                            if (messageBoList.size() >= fetchSize) {
                                break;
                            }
                        }
                    } catch (Exception e) {
                        handleConvertFailed(entity, e);
                    }
                }

                TransactionMessageEntity lastEntity = entityList.get(entityList.size() - 1);
                if (lastEntity.getCreateTime() == null || lastEntity.getId() == null) {
                    log.warn("事务消息分页游标字段为空，提前结束补拉，id: {}, createTime: {}",
                            lastEntity.getId(), lastEntity.getCreateTime());
                    break;
                }
                cursorCreateTime = lastEntity.getCreateTime();
                cursorId = lastEntity.getId();

                if (entityList.size() < fetchSize) {
                    break;
                }
            }
            if (scanRounds >= MAX_SCAN_ROUNDS && messageBoList.size() < fetchSize) {
                log.warn("事务消息补拉达到扫描上限，已扫描{}轮，命中{}条，fetchSize={}",
                        scanRounds, messageBoList.size(), fetchSize);
            }
            return messageBoList;
        } catch (Exception e) {
            log.error("获取最近两天失败记录失败", e);
            throw new RuntimeException("获取最近两天失败记录失败", e);
        }
    }

    /**
     * 单条记录转换失败时，递增重试次数并跳过该记录，避免中断整批重试任务。
     *
     * @param entity    转换失败的事务消息记录
     * @param exception 转换异常
     */
    private void handleConvertFailed(TransactionMessageEntity entity, Exception exception) {
        log.error("事务消息转换失败，跳过本条，id: {}, businessType: {}, sn: {}",
                entity.getId(), entity.getBusinessType(), entity.getSn(), exception);

        if (entity.getId() == null) {
            log.warn("事务消息主键为空，无法递增重试次数，businessType: {}, sn: {}", entity.getBusinessType(), entity.getSn());
            return;
        }

        try {
            int affectedRows = transactionMessageRepository.incrementTryTimesById(entity.getId(), LocalDateTime.now());
            if (affectedRows != 1) {
                log.warn("递增事务消息重试次数失败，id: {}, 影响行数: {}", entity.getId(), affectedRows);
            }
        } catch (Exception ex) {
            log.error("递增事务消息重试次数异常，id: {}", entity.getId(), ex);
        }
    }

    private TransactionMessageBo entityToBo(TransactionMessageEntity entity) {
        Object payload = deserializePayload(entity);
        MqMessage message = new MqMessage()
                .setMessageDestination(entity.getDestination())
                .setRoutingIdentifier(entity.getRoute())
                .setPayload(payload);

        return new TransactionMessageBo()
                .setId(entity.getId())
                .setBusinessType(TransactionMessageBusinessTypeEnum.getByName(entity.getBusinessType()))
                .setSn(entity.getSn())
                .setPayloadType(entity.getPayloadType())
                .setMessage(message)
                .setLastRunAt(entity.getLastRunAt())
                .setIsSuccess(entity.getIsSuccess())
                .setTryTimes(entity.getTryTimes())
                .setCreateTime(entity.getCreateTime());
    }

    private Object deserializePayload(TransactionMessageEntity entity) {
        String payloadJson = entity.getPayload();
        if (!StringUtils.hasText(payloadJson)) {
            return null;
        }
        String payloadType = entity.getPayloadType();
        try {
            if (StringUtils.hasText(payloadType)) {
                Class<?> payloadClass = Class.forName(payloadType);
                return JacksonUtil.fromJson(payloadJson, payloadClass);
            }
            return JacksonUtil.fromJson(payloadJson, Object.class);
        } catch (ClassNotFoundException e) {
            log.warn("无法获取payload类型, payloadType: {}, sn: {}", payloadType, entity.getSn(), e);
            return JacksonUtil.fromJson(payloadJson, Object.class);
        } catch (Exception e) {
            log.error("序列化payload失败, businessType: {}, sn: {}", entity.getBusinessType(), entity.getSn(), e);
            throw new BusinessRuntimeException("序列化payload失败");
        }
    }

    private String resolvePayloadType(Object payload) {
        if (payload == null) {
            return null;
        }
        return payload.getClass().getName();
    }

    /**
     * 判断当前事务消息是否达到重试时间窗口。
     *
     * @param messageBo 事务消息
     * @param now       当前时间
     * @return true-达到重试窗口；false-未达到重试窗口
     */
    private boolean shouldRetryNow(TransactionMessageBo messageBo, LocalDateTime now) {
        LocalDateTime baseTime = messageBo.getLastRunAt();
        if (baseTime == null) {
            baseTime = messageBo.getCreateTime();
        }
        if (baseTime == null) {
            return true;
        }

        long backoffSeconds = calculateBackoffSeconds(messageBo.getTryTimes());
        LocalDateTime nextRetryAt = baseTime.plusSeconds(backoffSeconds);
        return !nextRetryAt.isAfter(now);
    }

    /**
     * 计算指数退避秒数。
     * 公式：delay = min(base * 2^(tryTimes-1), max)，其中 tryTimes<=1 时按 base 处理。
     *
     * @param tryTimes 当前重试次数
     * @return 退避秒数
     */
    private long calculateBackoffSeconds(Integer tryTimes) {
        int safeTryTimes = tryTimes == null ? 0 : Math.max(tryTimes, 0);
        if (safeTryTimes <= 1) {
            return RETRY_BASE_SECONDS;
        }

        long delaySeconds = RETRY_BASE_SECONDS;
        for (int i = 1; i < safeTryTimes; i++) {
            if (delaySeconds >= RETRY_MAX_SECONDS) {
                return RETRY_MAX_SECONDS;
            }
            delaySeconds = Math.min(delaySeconds * 2, RETRY_MAX_SECONDS);
        }
        return delaySeconds;
    }
}
