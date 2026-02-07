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
    private final TransactionMessageRepository transactionMessageRepository;
    private final Integer maxRetryTimes;
    private final Integer fetchSize;
    private static final String LEGACY_MQ_MESSAGE_CLASS = "info.zhihui.ems.common.model.MqMessage";
    private static final String NEW_MQ_MESSAGE_CLASS = "info.zhihui.ems.mq.api.model.MqMessage";

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
            LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
            List<TransactionMessageEntity> entityList = transactionMessageRepository.getPastUnsuccessful(maxRetryTimes, oneDayAgo, fetchSize);

            List<TransactionMessageBo> messageBoList = new ArrayList<>();
            for (TransactionMessageEntity entity : entityList) {
                try {
                    messageBoList.add(entityToBo(entity));
                } catch (Exception e) {
                    handleConvertFailed(entity, e);
                }
            }
            return messageBoList;
        } catch (Exception e) {
            log.error("获取最近一天失败记录失败", e);
            throw new RuntimeException("获取最近一天失败记录失败", e);
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
                if (LEGACY_MQ_MESSAGE_CLASS.equals(payloadType)) {
                    payloadType = NEW_MQ_MESSAGE_CLASS;
                }
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
}
