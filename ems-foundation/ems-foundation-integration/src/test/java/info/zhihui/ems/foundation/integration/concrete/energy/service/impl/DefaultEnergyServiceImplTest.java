package info.zhihui.ems.foundation.integration.concrete.energy.service.impl;

import com.sun.net.httpserver.HttpServer;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.foundation.integration.concrete.energy.dto.ElectricDeviceAddDto;
import info.zhihui.ems.foundation.integration.concrete.energy.dto.platform.DefaultIotHttpRequestConfig;
import info.zhihui.ems.foundation.integration.concrete.energy.service.EnergyService;
import info.zhihui.ems.foundation.integration.core.service.DeviceModuleConfigService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultEnergyServiceImplTest {

    private static final Integer AREA_ID = 1;

    @Mock
    private DeviceModuleConfigService deviceModuleConfigService;

    @Test
    void testAddDevice_NonNumericIotId_ThrowBusinessRuntimeException() throws IOException {
        HttpServer httpServer = startMockServer("{\"success\":true,\"data\":\"iot-1\"}");
        try {
            DefaultEnergyServiceImpl service = buildService(httpServer);
            ElectricDeviceAddDto addDto = buildAddDto();

            BusinessRuntimeException exception = Assertions.assertThrows(BusinessRuntimeException.class,
                    () -> service.addDevice(addDto));

            Assertions.assertTrue(exception.getMessage().contains("IoT设备ID格式不正确"));
        } finally {
            httpServer.stop(0);
        }
    }

    @Test
    void testAddDevice_NumericIotId_ReturnIotId() throws IOException {
        HttpServer httpServer = startMockServer("{\"success\":true,\"data\":\"1001\"}");
        try {
            DefaultEnergyServiceImpl service = buildService(httpServer);
            ElectricDeviceAddDto addDto = buildAddDto();

            String iotId = service.addDevice(addDto);

            Assertions.assertEquals("1001", iotId);
        } finally {
            httpServer.stop(0);
        }
    }

    private DefaultEnergyServiceImpl buildService(HttpServer httpServer) {
        DefaultIotHttpRequestConfig config = new DefaultIotHttpRequestConfig();
        config.setAddressUrl("http://127.0.0.1:" + httpServer.getAddress().getPort());
        when(deviceModuleConfigService.getDeviceConfigValue(eq(EnergyService.class),
                eq(DefaultIotHttpRequestConfig.class), eq(AREA_ID))).thenReturn(config);
        return new DefaultEnergyServiceImpl(deviceModuleConfigService);
    }

    private ElectricDeviceAddDto buildAddDto() {
        return new ElectricDeviceAddDto()
                .setAreaId(AREA_ID)
                .setDeviceNo("dev-1001")
                .setProductCode("P01");
    }

    private HttpServer startMockServer(String responseBody) throws IOException {
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(0), 0);
        httpServer.createContext("/api/devices", exchange -> {
            byte[] response = responseBody.getBytes(StandardCharsets.UTF_8);
            exchange.getRequestBody().close();
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(response);
            }
        });
        httpServer.start();
        return httpServer;
    }
}

