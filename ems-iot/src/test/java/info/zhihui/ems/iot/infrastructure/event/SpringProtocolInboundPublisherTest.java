package info.zhihui.ems.iot.infrastructure.event;

import info.zhihui.ems.iot.protocol.event.inbound.ProtocolInboundEvent;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;

class SpringProtocolInboundPublisherTest {

    @Test
    void testPublish_EventNull_NoInteraction() {
        ApplicationEventPublisher eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        SpringProtocolInboundPublisher publisher = new SpringProtocolInboundPublisher(eventPublisher);

        publisher.publish(null);

        Mockito.verifyNoInteractions(eventPublisher);
    }

    @Test
    void testPublish_EventProvided_DelegatesToEventPublisher() {
        ApplicationEventPublisher eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        SpringProtocolInboundPublisher publisher = new SpringProtocolInboundPublisher(eventPublisher);
        ProtocolInboundEvent event = new ProtocolInboundEvent() {
        };

        publisher.publish(event);

        Mockito.verify(eventPublisher).publishEvent(event);
    }
}
