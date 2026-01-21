package info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet;

import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.iot.plugins.acrel.protocol.gateway.tcp.packet.definition.GatewayPacketDefinition;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 网关命令注册表。
 */
@Component
public class GatewayPacketRegistry {

    private final Map<String, GatewayPacketDefinition> definitionMap;

    public GatewayPacketRegistry(List<GatewayPacketDefinition> definitions) {
        Map<String, GatewayPacketDefinition> map = new HashMap<>();
        for (GatewayPacketDefinition definition : definitions) {
            String commandKey = definition.command();
            if (map.containsKey(commandKey)) {
                throw new BusinessRuntimeException("重复的网关命令定义，command=" + commandKey);
            }
            map.put(commandKey, definition);
        }
        this.definitionMap = Collections.unmodifiableMap(map);
    }

    public GatewayPacketDefinition resolve(String commandKey) {
        return definitionMap.get(commandKey);
    }
}
