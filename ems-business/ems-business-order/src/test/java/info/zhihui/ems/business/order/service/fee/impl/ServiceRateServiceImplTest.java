package info.zhihui.ems.business.order.service.fee.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.foundation.system.dto.ConfigUpdateDto;
import info.zhihui.ems.foundation.system.service.ConfigService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static info.zhihui.ems.foundation.system.constant.SystemConfigConstant.SERVICE_RATE_KEY;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("服务费配置服务测试")
class ServiceRateServiceImplTest {

    @Mock
    private ConfigService configService;

    @InjectMocks
    private ServiceRateServiceImpl serviceRateService;

    @Test
    @DisplayName("获取默认服务费-配置为空抛异常")
    void testGetDefaultServiceRate_NullConfig_ShouldThrow() {
        when(configService.getValueByKey(eq(SERVICE_RATE_KEY), any(TypeReference.class))).thenReturn(null);

        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> serviceRateService.getDefaultServiceRate());
        assertTrue(exception.getMessage().contains("服务费配置为空"));
    }

    @Test
    @DisplayName("获取默认服务费_等于1应抛异常")
    void testGetDefaultServiceRate_WhenRateEqualsOne_ShouldThrow() {
        when(configService.getValueByKey(eq(SERVICE_RATE_KEY), any(TypeReference.class))).thenReturn(BigDecimal.ONE);

        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> serviceRateService.getDefaultServiceRate());
        assertTrue(exception.getMessage().contains("不能等于1"));
    }

    @Test
    @DisplayName("更新默认服务费_等于1应抛异常且不更新配置")
    void testUpdateDefaultServiceRate_WhenRateEqualsOne_ShouldThrow() {
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> serviceRateService.updateDefaultServiceRate(BigDecimal.ONE));

        assertTrue(exception.getMessage().contains("不能等于1"));
        verify(configService, never()).update(any(ConfigUpdateDto.class));
    }
}
