package info.zhihui.ems.mq.api.enums;

import info.zhihui.ems.common.exception.BusinessRuntimeException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransactionMessageBusinessTypeEnumTest {

    @Test
    @DisplayName("getByName-合法值返回枚举")
    void testGetByName_Valid() {
        TransactionMessageBusinessTypeEnum result = TransactionMessageBusinessTypeEnum.getByName("ORDER_PAYMENT");
        assertEquals(TransactionMessageBusinessTypeEnum.ORDER_PAYMENT, result);
    }

    @Test
    @DisplayName("getByName-非法值抛出异常")
    void testGetByName_Invalid() {
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> TransactionMessageBusinessTypeEnum.getByName("UNKNOWN"));
        assertEquals("未知事务消息业务类型: UNKNOWN", exception.getMessage());
    }
}
