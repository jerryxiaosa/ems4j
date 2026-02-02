package info.zhihui.ems.business.finance.service.order.fee.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.foundation.system.service.ConfigService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static info.zhihui.ems.foundation.system.constant.SystemConfigConstant.SERVICE_RATE_KEY;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
}
