package info.zhihui.ems.iot.infrastructure.transport.netty.channel;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

class ChannelSessionTest {

    @Test
    void queue_shouldUseConcurrentQueue() {
        ChannelSession session = new ChannelSession();

        Assertions.assertInstanceOf(ConcurrentLinkedQueue.class, session.getQueue());
    }

    @Test
    void abnormalTimestamps_shouldUseConcurrentDeque() {
        ChannelSession session = new ChannelSession();

        Assertions.assertInstanceOf(ConcurrentLinkedDeque.class, session.getAbnormalTimestamps());
    }
}
