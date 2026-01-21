package info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet;

import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.packet.definition.Acrel4gPacketDefinition;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 4G 命令注册表。
 */
@Component
public class Acrel4gPacketRegistry {

    private final Map<String, Acrel4gPacketDefinition> definitionMap;

    public Acrel4gPacketRegistry(List<Acrel4gPacketDefinition> definitions) {
        Map<String, Acrel4gPacketDefinition> map = new HashMap<>();
        for (Acrel4gPacketDefinition definition : definitions) {
            String commandKey = definition.command();
            if (map.containsKey(commandKey)) {
                throw new BusinessRuntimeException("重复的命令定义，command=" + commandKey);
            }
            map.put(commandKey, definition);
        }
        this.definitionMap = Collections.unmodifiableMap(map);
    }

    public Acrel4gPacketDefinition resolve(String commandKey) {
        return definitionMap.get(commandKey);
    }
}
