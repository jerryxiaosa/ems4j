package info.zhihui.ems.iot.api.handler;

import info.zhihui.ems.common.constant.ResultCode;
import info.zhihui.ems.common.vo.RestResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RuntimeExceptionHandlerTest {

    private final RuntimeExceptionHandler runtimeExceptionHandler = new RuntimeExceptionHandler();

    @Test
    void testHandleRuntimeException_WhenIllegalArgumentException_ShouldReturnParameterError() {
        IllegalArgumentException exception = new IllegalArgumentException("参数错误");

        RestResult<Void> result = runtimeExceptionHandler.handleRuntimeException(exception);

        Assertions.assertFalse(result.getSuccess());
        Assertions.assertEquals(ResultCode.PARAMETER_ERROR.getCode(), result.getCode());
        Assertions.assertEquals("参数错误", result.getMessage());
    }
}

