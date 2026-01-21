package info.zhihui.ems.mq.rabbitmq.service;

import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.model.MqMessage;
import info.zhihui.ems.common.utils.JacksonUtil;
import info.zhihui.ems.mq.api.bo.TransactionMessageBo;
import info.zhihui.ems.mq.api.dto.TransactionMessageDto;
import info.zhihui.ems.mq.api.enums.TransactionMessageBusinessTypeEnum;
import info.zhihui.ems.mq.api.message.order.status.EnergyTopUpSuccessMessage;
import info.zhihui.ems.mq.rabbitmq.entity.TransactionMessageEntity;
import info.zhihui.ems.mq.rabbitmq.repository.TransactionMessageRepository;
import info.zhihui.ems.mq.rabbitmq.service.impl.TransactionMessageServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * TransactionMessageServiceImpl 单元测试
 *
 * @author jerryxiaosa
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("事务消息服务实现类测试")
class TransactionMessageServiceImplTest {

    @Mock
    private TransactionMessageRepository transactionMessageRepository;

    @InjectMocks
    private TransactionMessageServiceImpl transactionMessageService;

    private TransactionMessageDto addDto;

    @BeforeEach
    void setUp() {
        // Given: 准备测试数据
        MqMessage mqMessage = new MqMessage();
        mqMessage.setMessageDestination("test-topic");
        mqMessage.setRoutingIdentifier("test-key");
        mqMessage.setPayload("test-body");

        addDto = new TransactionMessageDto()
                .setBusinessType(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT)
                .setSn("TEST-SN-001")
                .setMessage(mqMessage);

    }

    @Test
    @DisplayName("新增事务消息 - 成功场景")
    void add_Success() {
        // Given: 模拟 repository 插入成功
        Mockito.when(transactionMessageRepository.insert(ArgumentMatchers.any(TransactionMessageEntity.class))).thenReturn(1);

        // When: 调用新增方法
        boolean result = transactionMessageService.add(addDto);

        // Then: 验证结果和调用
        Assertions.assertTrue(result);
        ArgumentCaptor<TransactionMessageEntity> entityCaptor = ArgumentCaptor.forClass(TransactionMessageEntity.class);
        Mockito.verify(transactionMessageRepository, Mockito.times(1)).insert(entityCaptor.capture());
        assertEquals(String.class.getName(), entityCaptor.getValue().getPayloadType());
    }

    @Test
    @DisplayName("新增事务消息 - 插入失败场景")
    void add_InsertFailed() {
        // Given: 模拟 repository 插入失败
        Mockito.when(transactionMessageRepository.insert(ArgumentMatchers.any(TransactionMessageEntity.class))).thenReturn(0);

        // When: 调用新增方法
        boolean result = transactionMessageService.add(addDto);

        // Then: 验证结果
        Assertions.assertFalse(result);
        Mockito.verify(transactionMessageRepository, Mockito.times(1)).insert(ArgumentMatchers.any(TransactionMessageEntity.class));
    }

    @Test
    @DisplayName("新增事务消息 - 异常场景")
    void add_Exception() {
        // Given: 模拟 repository 抛出异常
        Mockito.when(transactionMessageRepository.insert(ArgumentMatchers.any(TransactionMessageEntity.class)))
                .thenThrow(new RuntimeException("数据库异常"));

        // When & Then: 验证抛出业务异常
        BusinessRuntimeException exception = Assertions.assertThrows(BusinessRuntimeException.class, () -> {
            transactionMessageService.add(addDto);
        });

        Assertions.assertEquals("新增事务消息失败", exception.getMessage());
        Mockito.verify(transactionMessageRepository, Mockito.times(1)).insert(ArgumentMatchers.any(TransactionMessageEntity.class));
    }

    @Test
    @DisplayName("标记事务消息成功 - 成功场景")
    void success_Success() {
        // Given: 模拟 repository 更新成功
        Mockito.when(transactionMessageRepository.updateTransactionMessage(ArgumentMatchers.any(TransactionMessageEntity.class))).thenReturn(1);

        // When: 调用标记成功方法
        Assertions.assertDoesNotThrow(() -> {
            transactionMessageService.success(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT, "TEST-SN-001");
        });

        // Then: 验证调用
        Mockito.verify(transactionMessageRepository, Mockito.times(1)).updateTransactionMessage(ArgumentMatchers.any(TransactionMessageEntity.class));
    }

    @Test
    @DisplayName("标记事务消息成功 - 更新失败场景")
    void success_UpdateFailed() {
        // Given: 模拟 repository 更新失败（返回0）
        Mockito.when(transactionMessageRepository.updateTransactionMessage(ArgumentMatchers.any(TransactionMessageEntity.class))).thenReturn(0);

        transactionMessageService.success(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT, "TEST-SN-001");

        Mockito.verify(transactionMessageRepository, Mockito.times(1)).updateTransactionMessage(ArgumentMatchers.any(TransactionMessageEntity.class));
    }

    @Test
    @DisplayName("标记事务消息成功 - 异常场景")
    void success_Exception() {
        // Given: 模拟 repository 抛出异常
        Mockito.when(transactionMessageRepository.updateTransactionMessage(ArgumentMatchers.any(TransactionMessageEntity.class)))
                .thenThrow(new RuntimeException("数据库异常"));

        transactionMessageService.success(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT, "TEST-SN-001");
        Mockito.verify(transactionMessageRepository, Mockito.times(1)).updateTransactionMessage(ArgumentMatchers.any(TransactionMessageEntity.class));
    }

    @Test
    @DisplayName("标记事务消息失败 - 成功场景")
    void failure_Success() {
        // Given: 模拟 repository 更新成功
        Mockito.when(transactionMessageRepository.updateTransactionMessage(ArgumentMatchers.any(TransactionMessageEntity.class))).thenReturn(1);

        // When: 调用标记失败方法
        Assertions.assertDoesNotThrow(() -> {
            transactionMessageService.failure(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT, "TEST-SN-001");
        });

        // Then: 验证调用
        Mockito.verify(transactionMessageRepository, Mockito.times(1)).updateTransactionMessage(ArgumentMatchers.any(TransactionMessageEntity.class));
    }

    @Test
    @DisplayName("标记事务消息失败 - 更新失败场景")
    void failure_UpdateFailed() {
        // Given: 模拟 repository 更新失败（返回0）
        Mockito.when(transactionMessageRepository.updateTransactionMessage(ArgumentMatchers.any(TransactionMessageEntity.class))).thenReturn(0);

        transactionMessageService.failure(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT, "TEST-SN-001");
        Mockito.verify(transactionMessageRepository, Mockito.times(1)).updateTransactionMessage(ArgumentMatchers.any(TransactionMessageEntity.class));
    }

    @Test
    @DisplayName("获取最近一天失败记录 - 成功场景")
    void findRecentFailureRecords_Success() {
        // Given: 准备测试数据
        String payload1 = JacksonUtil.toJson(new EnergyTopUpSuccessMessage().setOrderSn("test1"));
        String payload2 = JacksonUtil.toJson(new EnergyTopUpSuccessMessage().setOrderSn("test2"));
        TransactionMessageEntity entity1 = new TransactionMessageEntity()
                .setId(1)
                .setBusinessType("ORDER_PAYMENT")
                .setSn("TEST-SN-001")
                .setDestination("test-topic")
                .setRoute("test-key")
                .setPayloadType(EnergyTopUpSuccessMessage.class.getName())
                .setPayload(payload1)
                .setCreateTime(LocalDateTime.now())
                .setLastRunAt(LocalDateTime.now())
                .setTryTimes(3)
                .setIsSuccess(false);

        TransactionMessageEntity entity2 = new TransactionMessageEntity()
                .setId(2)
                .setBusinessType("ORDER_PAYMENT")
                .setSn("TEST-SN-002")
                .setDestination("test-topic2")
                .setRoute("test-key2")
                .setPayloadType(EnergyTopUpSuccessMessage.class.getName())
                .setPayload(payload2)
                .setCreateTime(LocalDateTime.now())
                .setLastRunAt(LocalDateTime.now())
                .setTryTimes(5)
                .setIsSuccess(false);

        List<TransactionMessageEntity> entityList = Arrays.asList(entity1, entity2);

        // 模拟 repository 查询成功
        Mockito.when(transactionMessageRepository.getPastUnsuccessful(ArgumentMatchers.anyInt(), ArgumentMatchers.any(LocalDateTime.class), ArgumentMatchers.anyInt()))
                .thenReturn(entityList);

        // When: 调用获取失败记录方法
        List<TransactionMessageBo> result = transactionMessageService.findRecentFailureRecords();

        // Then: 验证结果
        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.size());

        TransactionMessageBo bo1 = result.get(0);
        assertEquals(1, bo1.getId());
        assertEquals(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT, bo1.getBusinessType());
        assertEquals("TEST-SN-001", bo1.getSn());
        assertEquals(3, bo1.getTryTimes());
        assertFalse(bo1.getIsSuccess());
        assertEquals(EnergyTopUpSuccessMessage.class.getName(), bo1.getPayloadType());
        Assertions.assertNotNull(bo1.getMessage());
        assertEquals("test-topic", bo1.getMessage().getMessageDestination());
        assertEquals(new EnergyTopUpSuccessMessage().setOrderSn("test1"), bo1.getMessage().getPayload());

        TransactionMessageBo bo2 = result.get(1);
        assertEquals(2, bo2.getId());
        assertEquals(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT, bo2.getBusinessType());
        assertEquals("TEST-SN-002", bo2.getSn());
        assertEquals(5, bo2.getTryTimes());
        assertFalse(bo2.getIsSuccess());
        assertEquals(EnergyTopUpSuccessMessage.class.getName(), bo2.getPayloadType());
        Assertions.assertNotNull(bo2.getMessage());
        assertEquals("test-topic2", bo2.getMessage().getMessageDestination());

        Mockito.verify(transactionMessageRepository, Mockito.times(1))
                .getPastUnsuccessful(ArgumentMatchers.eq(10), ArgumentMatchers.any(LocalDateTime.class), ArgumentMatchers.eq(100));
    }

    @Test
    @DisplayName("获取最近一天失败记录 - 空结果场景")
    void findRecentFailureRecords_EmptyResult() {
        // Given: 模拟 repository 返回空列表
        Mockito.when(transactionMessageRepository.getPastUnsuccessful(ArgumentMatchers.anyInt(), ArgumentMatchers.any(LocalDateTime.class), ArgumentMatchers.anyInt()))
                .thenReturn(List.of());

        // When: 调用获取失败记录方法
        List<TransactionMessageBo> result = transactionMessageService.findRecentFailureRecords();

        // Then: 验证结果
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
        Mockito.verify(transactionMessageRepository, Mockito.times(1))
                .getPastUnsuccessful(ArgumentMatchers.eq(10), ArgumentMatchers.any(LocalDateTime.class), ArgumentMatchers.eq(100));
    }

    @Test
    @DisplayName("获取最近一天失败记录 - 异常场景")
    void findRecentFailureRecords_Exception() {
        // Given: 模拟 repository 抛出异常
        Mockito.when(transactionMessageRepository.getPastUnsuccessful(ArgumentMatchers.anyInt(), ArgumentMatchers.any(LocalDateTime.class), ArgumentMatchers.anyInt()))
                .thenThrow(new RuntimeException("数据库查询异常"));

        // When & Then: 验证抛出运行时异常
        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> {
            transactionMessageService.findRecentFailureRecords();
        });

        Assertions.assertEquals("获取最近一天失败记录失败", exception.getMessage());
        Mockito.verify(transactionMessageRepository, Mockito.times(1))
                .getPastUnsuccessful(ArgumentMatchers.eq(10), ArgumentMatchers.any(LocalDateTime.class), ArgumentMatchers.eq(100));
    }

    @Test
    @DisplayName("JSON 解析异常场景测试")
    void findRecentFailureRecords_JsonParseException() {
        // Given: 准备包含无效 JSON 的测试数据
        TransactionMessageEntity entityWithInvalidJson = new TransactionMessageEntity()
                .setId(1)
                .setBusinessType("ORDER")
                .setSn("TEST-SN-001")
                .setDestination("test-topic")
                .setRoute("test-key")
                .setPayloadType(String.class.getName())
                .setPayload("invalid-json")
                .setCreateTime(LocalDateTime.now())
                .setLastRunAt(LocalDateTime.now())
                .setTryTimes(3)
                .setIsSuccess(false);

        List<TransactionMessageEntity> entityList = List.of(entityWithInvalidJson);

        // 模拟 repository 查询成功
        Mockito.when(transactionMessageRepository.getPastUnsuccessful(ArgumentMatchers.anyInt(), ArgumentMatchers.any(LocalDateTime.class), ArgumentMatchers.anyInt()))
                .thenReturn(entityList);

        // When & Then: 验证抛出运行时异常（由于 JSON 解析失败）
        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> {
            transactionMessageService.findRecentFailureRecords();
        });

        Assertions.assertEquals("获取最近一天失败记录失败", exception.getMessage());
        Mockito.verify(transactionMessageRepository, Mockito.times(1))
                .getPastUnsuccessful(ArgumentMatchers.eq(10), ArgumentMatchers.any(LocalDateTime.class), ArgumentMatchers.eq(100));
    }
}
