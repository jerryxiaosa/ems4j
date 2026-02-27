package info.zhihui.ems.handler;

import info.zhihui.ems.common.constant.ResultCode;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.vo.RestResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.BadSqlGrammarException;

import java.sql.SQLException;

class RuntimeExceptionHandlerTest {

    private final RuntimeExceptionHandler runtimeExceptionHandler = new RuntimeExceptionHandler();

    @Test
    void testHandleBusinessRuntimeException_WhenMessageContainsSql_ShouldSanitizeMessage() {
        BusinessRuntimeException exception = new BusinessRuntimeException(
                "开户失败：### Error updating database. Cause: java.sql.SQLSyntaxErrorException: Unknown column 'history_power_offset'"
        );

        RestResult<Void> result = runtimeExceptionHandler.handleRuntimeException(exception);

        Assertions.assertFalse(result.getSuccess());
        Assertions.assertEquals(ResultCode.BUSINESS_ERROR.getCode(), result.getCode());
        Assertions.assertEquals("系统异常，请稍后再试", result.getMessage());
    }

    @Test
    void testHandleBusinessRuntimeException_WhenMessageIsNormal_ShouldKeepOriginalMessage() {
        BusinessRuntimeException exception = new BusinessRuntimeException("账户正在操作，请稍后重试");

        RestResult<Void> result = runtimeExceptionHandler.handleRuntimeException(exception);

        Assertions.assertFalse(result.getSuccess());
        Assertions.assertEquals(ResultCode.BUSINESS_ERROR.getCode(), result.getCode());
        Assertions.assertEquals("账户正在操作，请稍后重试", result.getMessage());
    }

    @Test
    void testHandleDatabaseException_ShouldReturnGenericMessage() {
        BadSqlGrammarException exception = new BadSqlGrammarException(
                "insert meter step",
                "INSERT INTO energy_account_meter_step(history_power_offset) VALUES (?)",
                new SQLException("Unknown column 'history_power_offset' in 'field list'")
        );

        RestResult<Void> result = runtimeExceptionHandler.handleDatabaseException(exception);

        Assertions.assertFalse(result.getSuccess());
        Assertions.assertEquals(ResultCode.FAILED.getCode(), result.getCode());
        Assertions.assertEquals("系统异常，请稍后再试", result.getMessage());
    }
}

