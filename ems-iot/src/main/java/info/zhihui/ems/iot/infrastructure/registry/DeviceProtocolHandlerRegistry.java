package info.zhihui.ems.iot.infrastructure.registry;

import info.zhihui.ems.iot.protocol.port.DeviceProtocolHandlerResolver;
import info.zhihui.ems.iot.protocol.port.ProtocolSignature;
import info.zhihui.ems.iot.enums.TransportProtocolEnum;
import info.zhihui.ems.iot.protocol.port.DeviceProtocolHandler;
import info.zhihui.ems.iot.enums.DeviceAccessModeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DeviceProtocolHandlerRegistry implements DeviceProtocolHandlerResolver {

    private final List<DeviceProtocolHandler> handlers;

    /**
     * 根据探测签名解析对应的协议处理器，优先匹配产品编码。
     */
    @Override
    public DeviceProtocolHandler resolve(ProtocolSignature signature) {
        if (signature == null || !StringUtils.hasText(signature.getVendor())) {
            throw new IllegalArgumentException("Unsupported vendor signature: null");
        }
        if (signature.getTransportType() == null) {
            throw new IllegalArgumentException("Unsupported transport signature: " + signature);
        }
        // 按 vendor + 传输协议 + 接入方式过滤候选处理器
        List<DeviceProtocolHandler> candidates = handlers.stream()
                .filter(handler -> handler.getVendor().equalsIgnoreCase(signature.getVendor()))
                .filter(handler -> transportMatches(handler.getSupportedTransports(), signature.getTransportType()))
                .filter(handler -> accessModeMatches(handler.getAccessMode(), signature.getAccessMode()))
                .toList();
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("Unsupported vendor signature: " + signature.getVendor());
        }

        String productCode = normalize(signature.getProductCode());
        // 如果指定了产品型号，用专用的处理器
        if (StringUtils.hasText(productCode)) {
            Optional<DeviceProtocolHandler> productSpecific = candidates.stream()
                    .filter(handler -> StringUtils.hasText(handler.getProductCode()))
                    .filter(handler -> handler.getProductCode().equalsIgnoreCase(productCode))
                    .findFirst();
            if (productSpecific.isPresent()) {
                return productSpecific.get();
            }
        }
        // 回退到该 vendor 的默认处理器
        return candidates.stream()
                .filter(handler -> !StringUtils.hasText(handler.getProductCode()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported vendor signature: " + signature));
    }

    /**
     * handler 未设置接入方式时视为全匹配。
     */
    private boolean accessModeMatches(DeviceAccessModeEnum handlerMode, DeviceAccessModeEnum signatureMode) {
        if (handlerMode == null) {
            return true;
        }
        return signatureMode != null && handlerMode == signatureMode;
    }

    private boolean transportMatches(Set<TransportProtocolEnum> handlerTransports,
                                     TransportProtocolEnum signatureTransport) {
        if (handlerTransports == null || handlerTransports.isEmpty()) {
            return false;
        }
        return signatureTransport != null && handlerTransports.contains(signatureTransport);
    }

    /**
     * 统一处理产品编码的空白与空值。
     */
    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
