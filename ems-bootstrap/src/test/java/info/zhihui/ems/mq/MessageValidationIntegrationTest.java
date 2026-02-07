package info.zhihui.ems.mq;

import info.zhihui.ems.mq.api.message.order.OrderCompleteMessage;
import info.zhihui.ems.mq.api.message.order.delay.OrderDelayCheckMessage;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("integrationtest")
@DisplayName("MQ消息校验集成测试")
class MessageValidationIntegrationTest {

    @Autowired
    private Validator validator;

    @Test
    @DisplayName("OrderCompleteMessage-订单编号为空应校验失败")
    void testOrderCompleteMessage_InvalidOrderSn() {
        OrderCompleteMessage message = new OrderCompleteMessage();

        Set<ConstraintViolation<OrderCompleteMessage>> violations = validator.validate(message);

        assertEquals(1, violations.size());
        ConstraintViolation<OrderCompleteMessage> violation = violations.iterator().next();
        assertEquals("orderSn", violation.getPropertyPath().toString());
    }

    @Test
    @DisplayName("OrderDelayCheckMessage-订单编号为空应校验失败")
    void testOrderDelayCheckMessage_InvalidOrderSn() {
        OrderDelayCheckMessage message = new OrderDelayCheckMessage();
        message.setDelaySeconds(10);

        Set<ConstraintViolation<OrderDelayCheckMessage>> violations = validator.validate(message);

        assertEquals(1, violations.size());
        ConstraintViolation<OrderDelayCheckMessage> violation = violations.iterator().next();
        assertEquals("orderSn", violation.getPropertyPath().toString());
    }

    @Test
    @DisplayName("OrderDelayCheckMessage-延迟秒数小于1应校验失败")
    void testOrderDelayCheckMessage_InvalidDelaySeconds() {
        OrderDelayCheckMessage message = new OrderDelayCheckMessage();
        message.setOrderSn("ORDER-1")
                .setDelaySeconds(0);

        Set<ConstraintViolation<OrderDelayCheckMessage>> violations = validator.validate(message);

        assertTrue(violations.stream().anyMatch(item -> "delaySeconds".equals(item.getPropertyPath().toString())));
    }
}
