package info.zhihui.ems.iot.infrastructure.registry;

import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.domain.model.DeviceCommandResult;
import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;
import info.zhihui.ems.iot.protocol.port.outbound.DeviceCommandTranslator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class DeviceCommandTranslatorRegistryTest {

    @Test
    void resolve_whenProductTypeMissing_shouldFallbackToDefault() {
        DeviceCommandTranslatorRegistry registry = new DeviceCommandTranslatorRegistry(List.of(
                new DefaultTranslator(),
                new ProductSpecificTranslator()
        ));

        DeviceCommandTranslator<?> translator = registry.resolve("acrel", "P1",
                DeviceCommandTypeEnum.GET_CT, String.class);

        Assertions.assertInstanceOf(DefaultTranslator.class, translator);
    }

    @Test
    void resolve_whenProductHasType_shouldReturnProductTranslator() {
        DeviceCommandTranslatorRegistry registry = new DeviceCommandTranslatorRegistry(List.of(
                new DefaultTranslator(),
                new ProductSpecificTranslator()
        ));

        DeviceCommandTranslator<?> translator = registry.resolve("acrel", "P1",
                DeviceCommandTypeEnum.CUT_OFF, String.class);

        Assertions.assertInstanceOf(ProductSpecificTranslator.class, translator);
    }

    @Test
    void resolve_whenDefaultMissing_shouldThrow() {
        DeviceCommandTranslatorRegistry registry = new DeviceCommandTranslatorRegistry(List.of(
                new ProductSpecificTranslator()
        ));

        Assertions.assertThrows(BusinessRuntimeException.class, () -> registry.resolve("acrel", "P1",
                DeviceCommandTypeEnum.GET_CT, String.class));
    }

    private static class DefaultTranslator implements DeviceCommandTranslator<String> {

        @Override
        public String vendor() {
            return "ACREL";
        }

        @Override
        public String productCode() {
            return null;
        }

        @Override
        public DeviceCommandTypeEnum type() {
            return DeviceCommandTypeEnum.GET_CT;
        }

        @Override
        public Class<String> requestType() {
            return String.class;
        }

        @Override
        public String toRequest(DeviceCommand command) {
            return "default";
        }

        @Override
        public DeviceCommandResult parseResponse(DeviceCommand command, byte[] payload) {
            return new DeviceCommandResult().setSuccess(true);
        }
    }

    private static class ProductSpecificTranslator implements DeviceCommandTranslator<String> {

        @Override
        public String vendor() {
            return "ACREL";
        }

        @Override
        public String productCode() {
            return "P1";
        }

        @Override
        public DeviceCommandTypeEnum type() {
            return DeviceCommandTypeEnum.CUT_OFF;
        }

        @Override
        public Class<String> requestType() {
            return String.class;
        }

        @Override
        public String toRequest(DeviceCommand command) {
            return "product";
        }

        @Override
        public DeviceCommandResult parseResponse(DeviceCommand command, byte[] payload) {
            return new DeviceCommandResult().setSuccess(true);
        }
    }
}
