package info.zhihui.ems.iot.infrastructure.registry;

import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.domain.model.DeviceCommandResult;
import info.zhihui.ems.iot.protocol.port.DeviceCommandTranslator;
import info.zhihui.ems.iot.enums.DeviceCommandTypeEnum;
import info.zhihui.ems.iot.protocol.modbus.ModbusRtuRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class DeviceCommandTranslatorRegistryTest {

    @Test
    void resolve_WhenTranslatorExists_ShouldReturnTranslator() {
        DeviceCommandTranslator<ModbusRtuRequest> translator = new StubTranslator("ACREL", "P1", DeviceCommandTypeEnum.GET_CT);
        DeviceCommandTranslatorRegistry registry = new DeviceCommandTranslatorRegistry(List.of(translator));

        Assertions.assertSame(translator, registry.resolve("acrel", "p1",
                DeviceCommandTypeEnum.GET_CT, ModbusRtuRequest.class));
    }

    @Test
    void resolve_WhenVendorMissing_ShouldThrow() {
        DeviceCommandTranslator<ModbusRtuRequest> translator = new StubTranslator("ACREL", "P1", DeviceCommandTypeEnum.GET_CT);
        DeviceCommandTranslatorRegistry registry = new DeviceCommandTranslatorRegistry(List.of(translator));

        Assertions.assertThrows(BusinessRuntimeException.class,
                () -> registry.resolve("OTHER", "P1", DeviceCommandTypeEnum.GET_CT, ModbusRtuRequest.class));
    }

    @Test
    void resolve_WhenTypeMissing_ShouldThrow() {
        DeviceCommandTranslator<ModbusRtuRequest> translator = new StubTranslator("ACREL", "P1", DeviceCommandTypeEnum.GET_CT);
        DeviceCommandTranslatorRegistry registry = new DeviceCommandTranslatorRegistry(List.of(translator));

        Assertions.assertThrows(BusinessRuntimeException.class,
                () -> registry.resolve("ACREL", "P1", DeviceCommandTypeEnum.SET_CT, ModbusRtuRequest.class));
    }

    @Test
    void resolve_WhenRequestTypeNull_ShouldThrow() {
        DeviceCommandTranslator<ModbusRtuRequest> translator = new StubTranslator("ACREL", "P1", DeviceCommandTypeEnum.GET_CT);
        DeviceCommandTranslatorRegistry registry = new DeviceCommandTranslatorRegistry(List.of(translator));

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> registry.resolve("ACREL", "P1", DeviceCommandTypeEnum.GET_CT, null));
    }

    @Test
    void constructor_WhenDuplicateType_ShouldThrow() {
        DeviceCommandTranslator<ModbusRtuRequest> first = new StubTranslator("ACREL", "P1", DeviceCommandTypeEnum.GET_CT);
        DeviceCommandTranslator<ModbusRtuRequest> second = new StubTranslator("ACREL", "P1", DeviceCommandTypeEnum.GET_CT);

        Assertions.assertThrows(BusinessRuntimeException.class,
                () -> new DeviceCommandTranslatorRegistry(List.of(first, second)));
    }

    @Test
    void constructor_WhenVendorBlank_ShouldThrow() {
        DeviceCommandTranslator<ModbusRtuRequest> translator = new StubTranslator(" ", "P1", DeviceCommandTypeEnum.GET_CT);

        Assertions.assertThrows(BusinessRuntimeException.class,
                () -> new DeviceCommandTranslatorRegistry(List.of(translator)));
    }

    @Test
    void resolve_WhenProductMissing_ShouldFallbackDefault() {
        DeviceCommandTranslator<ModbusRtuRequest> translator = new StubTranslator("ACREL", null, DeviceCommandTypeEnum.GET_CT);
        DeviceCommandTranslatorRegistry registry = new DeviceCommandTranslatorRegistry(List.of(translator));

        Assertions.assertSame(translator, registry.resolve("ACREL", "P1",
                DeviceCommandTypeEnum.GET_CT, ModbusRtuRequest.class));
    }

    @Test
    void resolve_WhenProductSpecificExists_ShouldPreferSpecific() {
        DeviceCommandTranslator<ModbusRtuRequest> generic = new StubTranslator("ACREL", null, DeviceCommandTypeEnum.GET_CT);
        DeviceCommandTranslator<ModbusRtuRequest> specific = new StubTranslator("ACREL", "P1", DeviceCommandTypeEnum.GET_CT);
        DeviceCommandTranslatorRegistry registry = new DeviceCommandTranslatorRegistry(List.of(generic, specific));

        Assertions.assertSame(specific, registry.resolve("ACREL", "P1",
                DeviceCommandTypeEnum.GET_CT, ModbusRtuRequest.class));
    }

    private static class StubTranslator implements DeviceCommandTranslator<ModbusRtuRequest> {

        private final String vendor;
        private final String productCode;
        private final DeviceCommandTypeEnum type;

        private StubTranslator(String vendor, String productCode, DeviceCommandTypeEnum type) {
            this.vendor = vendor;
            this.productCode = productCode;
            this.type = type;
        }

        @Override
        public DeviceCommandTypeEnum type() {
            return type;
        }

        @Override
        public String vendor() {
            return vendor;
        }

        @Override
        public String productCode() {
            return productCode;
        }

        @Override
        public ModbusRtuRequest toRequest(DeviceCommand command) {
            return null;
        }

        @Override
        public Class<ModbusRtuRequest> requestType() {
            return ModbusRtuRequest.class;
        }

        @Override
        public DeviceCommandResult parseResponse(DeviceCommand command, byte[] payload) {
            return null;
        }
    }
}
