package info.zhihui.ems.iot.infrastructure.event;

import info.zhihui.ems.iot.protocol.event.inbound.ProtocolInboundEvent;
import info.zhihui.ems.iot.protocol.port.inbound.ProtocolInboundPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringProtocolInboundPublisher implements ProtocolInboundPublisher {

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void publish(ProtocolInboundEvent event) {
        if (event == null) {
            return;
        }
        eventPublisher.publishEvent(event);
    }
}
