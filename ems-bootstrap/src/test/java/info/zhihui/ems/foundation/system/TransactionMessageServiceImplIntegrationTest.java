package info.zhihui.ems.foundation.system;

import info.zhihui.ems.mq.api.model.MqMessage;
import info.zhihui.ems.mq.api.bo.TransactionMessageBo;
import info.zhihui.ems.mq.api.dto.TransactionMessageDto;
import info.zhihui.ems.mq.api.enums.TransactionMessageBusinessTypeEnum;
import info.zhihui.ems.mq.api.service.TransactionMessageService;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TransactionMessageService 集成测试
 *
 * @author jerryxiaosa
 */
@SpringBootTest
@ActiveProfiles("integrationtest")
@Transactional
class TransactionMessageServiceImplIntegrationTest {

    @Autowired
    private TransactionMessageService transactionMessageService;

    private TransactionMessageDto validAddDto;

    @BeforeEach
    void setUp() {
        // Given: 准备有效的测试数据
        MqMessage validMqMessage = new MqMessage()
                .setMessageDestination("test-topic")
                .setRoutingIdentifier("test-routing-key")
                .setPayload("test-payload-content");

        validAddDto = new TransactionMessageDto()
                .setBusinessType(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT)
                .setSn("INTEGRATION-TEST-SN-001")
                .setMessage(validMqMessage);
    }

    @Test
    @DisplayName("新增事务消息 - 参数校验测试 - DTO为null")
    void add_ValidationTest_NullDto() {
        // When & Then: 验证null参数抛出约束违反异常
        assertThrows(ConstraintViolationException.class, () -> {
            transactionMessageService.add(null);
        });
    }

    @Test
    @DisplayName("新增事务消息 - 参数校验测试 - 业务类型为null")
    void add_ValidationTest_NullBusinessType() {
        // Given: 设置业务类型为null
        validAddDto.setBusinessType(null);

        // When & Then: 验证参数校验失败
        assertThrows(ConstraintViolationException.class, () -> {
            transactionMessageService.add(validAddDto);
        });
    }

    @Test
    @DisplayName("新增事务消息 - 参数校验测试 - 序列号为空")
    void add_ValidationTest_EmptySn() {
        // Given: 设置序列号为空字符串
        validAddDto.setSn("");

        // When & Then: 验证参数校验失败
        assertThrows(ConstraintViolationException.class, () -> {
            transactionMessageService.add(validAddDto);
        });
    }

    @Test
    @DisplayName("新增事务消息 - 参数校验测试 - 消息内容为null")
    void add_ValidationTest_NullMessage() {
        // Given: 设置消息内容为null
        validAddDto.setMessage(null);

        // When & Then: 验证参数校验失败
        assertThrows(ConstraintViolationException.class, () -> {
            transactionMessageService.add(validAddDto);
        });
    }

    @Test
    @DisplayName("新增事务消息 - 正常添加功能测试")
    void add_Success() {
        // When & Then: 验证调用成功
        assertDoesNotThrow(() -> transactionMessageService.add(validAddDto));
    }

    @Test
    @DisplayName("标记事务消息成功 - 参数校验测试 - 业务类型为空")
    void success_ValidationTest_EmptyBusinessType() {
        // When & Then: 验证空业务类型抛出约束违反异常
        assertThrows(ConstraintViolationException.class, () -> {
            transactionMessageService.success(null, "TEST-SN-001");
        });
    }

    @Test
    @DisplayName("标记事务消息成功 - 参数校验测试 - 序列号为空")
    void success_ValidationTest_EmptySn() {
        // When & Then: 验证空序列号抛出约束违反异常
        assertThrows(ConstraintViolationException.class, () -> {
            transactionMessageService.success(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT, "");
        });
    }

    @Test
    @DisplayName("标记事务消息成功 - 正常标记功能测试")
    void success_Success() {
        // Given: 先添加一条事务消息
        transactionMessageService.add(validAddDto);

        // When: 标记为成功
        assertDoesNotThrow(() -> {
            transactionMessageService.success(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT, "INTEGRATION-TEST-SN-001");
        });

        // Then: 验证操作成功（无异常抛出即为成功）
    }

    @Test
    @DisplayName("标记事务消息成功 - 记录不存在场景")
    void success_RecordNotFound() {
        assertDoesNotThrow(() -> {
            transactionMessageService.success(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT, "NON-EXISTENT-SN");
        });
    }

    @Test
    @DisplayName("标记事务消息失败 - 参数校验测试 - 业务类型为空")
    void failure_ValidationTest_EmptyBusinessType() {
        // When & Then: 验证空业务类型抛出约束违反异常
        assertThrows(ConstraintViolationException.class, () -> {
            transactionMessageService.failure(null, "TEST-SN-001");
        });
    }

    @Test
    @DisplayName("标记事务消息失败 - 参数校验测试 - 序列号为空")
    void failure_ValidationTest_EmptySn() {
        // When & Then: 验证空序列号抛出约束违反异常
        assertThrows(ConstraintViolationException.class, () -> {
            transactionMessageService.failure(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT, "");
        });
    }

    @Test
    @DisplayName("标记事务消息失败 - 正常标记功能测试")
    void failure_Success() {
        // Given: 先添加一条事务消息
        transactionMessageService.add(validAddDto);

        // When: 标记为失败
        assertDoesNotThrow(() -> {
            transactionMessageService.failure(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT, "INTEGRATION-TEST-SN-001");
        });

        // Then: 验证操作成功（无异常抛出即为成功）
    }

    @Test
    @DisplayName("标记事务消息失败 - 记录不存在场景")
    void failure_RecordNotFound() {
        assertDoesNotThrow(() -> {
            transactionMessageService.failure(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT, "NON-EXISTENT-SN");
        });
    }

    @Test
    @DisplayName("获取最近一天失败记录 - 正常查询功能测试")
    void findRecentFailureRecords_Success() {
        // When: 调用查询方法
        List<TransactionMessageBo> result = transactionMessageService.findRecentFailureRecords();

        // Then: 验证查询结果
        assertNotNull(result, "查询结果不应为null");
        // 注意：由于测试数据的存在，这里应该能查询到失败记录
        assertFalse(result.isEmpty(), "应该能查询到测试数据中的失败记录");

        // 验证返回的记录都是失败状态
        result.forEach(record -> {
            assertFalse(record.getIsSuccess(), "查询结果中的记录都应该是失败状态");
            assertNotNull(record.getBusinessType(), "业务类型不应为null");
            assertNotNull(record.getPayloadType(), "消息载荷类型不应为null");
            assertNotNull(record.getSn(), "序列号不应为null");
            assertNotNull(record.getCreateTime(), "创建时间不应为null");
        });
    }

    @Test
    @DisplayName("获取最近一天失败记录 - 验证返回数据完整性")
    void findRecentFailureRecords_DataIntegrity() {
        // When: 调用查询方法
        List<TransactionMessageBo> result = transactionMessageService.findRecentFailureRecords();

        // Then: 验证数据完整性
        assertNotNull(result, "查询结果不应为null");

        if (!result.isEmpty()) {
            TransactionMessageBo firstRecord = result.get(0);
            // 验证业务对象的关键字段
            assertNotNull(firstRecord.getId(), "记录ID不应为null");
            assertNotNull(firstRecord.getBusinessType(), "业务类型不应为null");
            assertNotNull(firstRecord.getSn(), "序列号不应为null");
            assertNotNull(firstRecord.getCreateTime(), "创建时间不应为null");
            assertNotNull(firstRecord.getLastRunAt(), "最后运行时间不应为null");
            assertTrue(firstRecord.getTryTimes() >= 0, "重试次数应该大于等于0");
            assertFalse(firstRecord.getIsSuccess(), "失败记录的成功标志应为false");
            assertNotNull(firstRecord.getPayloadType(), "消息载荷类型不应为null");
            // 验证消息对象
            if (firstRecord.getMessage() != null) {
                assertNotNull(firstRecord.getMessage().getMessageDestination(), "消息目的地不应为null");
            }
        }
    }
}
