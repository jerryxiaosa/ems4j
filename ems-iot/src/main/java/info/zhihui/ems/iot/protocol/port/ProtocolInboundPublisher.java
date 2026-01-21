package info.zhihui.ems.iot.protocol.port;

import info.zhihui.ems.iot.protocol.event.inbound.ProtocolInboundEvent;

/**
 * Publishes inbound protocol events for application handling.
 */
public interface ProtocolInboundPublisher {

    /**
     * Publish an inbound protocol event.
     *
     * @param event inbound event
     */
    void publish(ProtocolInboundEvent event);
}
