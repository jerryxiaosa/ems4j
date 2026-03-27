package info.zhihui.ems.iot.simulator.protocol.acrel;

import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.constant.Acrel4gCommandConstants;
import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.support.Acrel4gFrameCodec;
import info.zhihui.ems.iot.simulator.config.SimulatorDeviceProperties;
import info.zhihui.ems.iot.simulator.config.SimulatorTargetProperties;
import info.zhihui.ems.iot.simulator.runtime.SimulatorDeviceContext;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Acrel4gSocketSessionTest {

    private final Acrel4gFrameCodec frameCodec = new Acrel4gFrameCodec();

    @Test
    void testSocketSession_WhenConnectSendAndReceiveSplitFrames_ExpectedTransmitAndDispatchCompleteFrames() throws Exception {
        byte[] outboundFrame = frameCodec.encode(Acrel4gCommandConstants.HEARTBEAT, null);
        byte[] inboundFrameOne = frameCodec.encode(Acrel4gCommandConstants.REGISTER, "ack".getBytes(StandardCharsets.UTF_8));
        byte[] inboundFrameTwo = frameCodec.encode(Acrel4gCommandConstants.DOWNLINK, new byte[]{0x01, 0x02, 0x03});
        BlockingQueue<byte[]> inboundFrameQueue = new LinkedBlockingQueue<>();

        try (SocketStubServer socketStubServer = new SocketStubServer()) {
            SimulatorDeviceContext deviceContext = buildDeviceContext();
            Acrel4gSocketSession socketSession = new Acrel4gSocketSession(
                    buildTargetProperties(socketStubServer.getPort()),
                    deviceContext,
                    inboundFrameQueue::offer);

            socketSession.connect();
            socketSession.send(outboundFrame);

            assertArrayEquals(outboundFrame, socketStubServer.readFrame(Duration.ofSeconds(2)));

            socketStubServer.writeRaw(new byte[]{0x00, 0x11, 0x22});
            socketStubServer.writeSplitFrame(inboundFrameOne, 4);
            socketStubServer.writeSplitFrame(inboundFrameTwo, 3);

            assertArrayEquals(inboundFrameOne, inboundFrameQueue.poll(2, TimeUnit.SECONDS));
            assertArrayEquals(inboundFrameTwo, inboundFrameQueue.poll(2, TimeUnit.SECONDS));
            assertTrue(deviceContext.getConnectionId() != null && !deviceContext.getConnectionId().isBlank());

            socketSession.close();
        }
    }

    @Test
    void testReadLoop_WhenStaleReaderExits_ShouldNotCloseActiveConnection() throws Exception {
        SimulatorDeviceContext deviceContext = buildDeviceContext();
        Acrel4gSocketSession socketSession = new Acrel4gSocketSession(
                buildTargetProperties(0),
                deviceContext,
                frame -> {
                });
        TestSocket staleSocket = new TestSocket();
        BlockingInputStream staleInputStream = new BlockingInputStream();
        TestSocket activeSocket = new TestSocket();
        ByteArrayInputStream activeInputStream = new ByteArrayInputStream(new byte[0]);
        ByteArrayOutputStream activeOutputStream = new ByteArrayOutputStream();

        setField(socketSession, "socket", activeSocket);
        setField(socketSession, "inputStream", activeInputStream);
        setField(socketSession, "outputStream", activeOutputStream);
        deviceContext.setConnectionId("active-connection");

        Thread readerThread = new Thread(() -> invokeReadLoop(socketSession, staleSocket, staleInputStream),
                "stale-reader-test");
        readerThread.start();
        staleInputStream.awaitReadStarted();
        staleInputStream.finish();
        readerThread.join(2000);

        assertTrue(!readerThread.isAlive(), "旧 reader 线程未按预期退出");
        assertEquals("active-connection", deviceContext.getConnectionId());
        assertTrue(!activeSocket.isClosed(), "旧 reader 不应关闭当前活跃连接");
    }

    private SimulatorTargetProperties buildTargetProperties(int port) {
        SimulatorTargetProperties targetProperties = new SimulatorTargetProperties();
        targetProperties.setHost("127.0.0.1");
        targetProperties.setPort(port);
        targetProperties.setConnectTimeoutMs(2000);
        return targetProperties;
    }

    private SimulatorDeviceContext buildDeviceContext() {
        SimulatorDeviceProperties deviceProperties = new SimulatorDeviceProperties();
        deviceProperties.setDeviceNo("SIM001");
        return new SimulatorDeviceContext().setDeviceProperties(deviceProperties);
    }

    private void invokeReadLoop(Acrel4gSocketSession socketSession, Socket socket, InputStream inputStream) {
        try {
            var readLoopMethod = Acrel4gSocketSession.class.getDeclaredMethod("readLoop", Socket.class, InputStream.class);
            readLoopMethod.setAccessible(true);
            readLoopMethod.invoke(socketSession, socket, inputStream);
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = Acrel4gSocketSession.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static final class SocketStubServer implements AutoCloseable {

        private final ServerSocket serverSocket;
        private final BlockingQueue<Socket> socketQueue = new LinkedBlockingQueue<>();
        private volatile Socket acceptedSocket;

        private SocketStubServer() throws IOException {
            this.serverSocket = new ServerSocket(0);
            Thread acceptThread = new Thread(this::acceptLoop, "socket-stub-accept");
            acceptThread.setDaemon(true);
            acceptThread.start();
        }

        private int getPort() {
            return serverSocket.getLocalPort();
        }

        private void acceptLoop() {
            try {
                socketQueue.offer(serverSocket.accept());
            } catch (IOException ignored) {
                return;
            }
        }

        private byte[] readFrame(Duration timeout) throws Exception {
            Socket socket = getSocket(timeout);
            InputStream inputStream = socket.getInputStream();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            long deadlineTime = System.nanoTime() + timeout.toNanos();
            while (System.nanoTime() < deadlineTime) {
                int currentByte = inputStream.read();
                if (currentByte < 0) {
                    break;
                }
                byteArrayOutputStream.write(currentByte);
                byte[] currentBytes = byteArrayOutputStream.toByteArray();
                int length = currentBytes.length;
                if (length >= 4 && currentBytes[length - 2] == 0x7d && currentBytes[length - 1] == 0x7d) {
                    return currentBytes;
                }
            }
            throw new IllegalStateException("未在超时时间内读到完整帧");
        }

        private void writeRaw(byte[] bytes) throws Exception {
            Socket socket = getSocket(Duration.ofSeconds(2));
            socket.getOutputStream().write(bytes);
            socket.getOutputStream().flush();
        }

        private void writeSplitFrame(byte[] frame, int splitIndex) throws Exception {
            Socket socket = getSocket(Duration.ofSeconds(2));
            socket.getOutputStream().write(frame, 0, splitIndex);
            socket.getOutputStream().flush();
            socket.getOutputStream().write(frame, splitIndex, frame.length - splitIndex);
            socket.getOutputStream().flush();
        }

        private Socket getSocket(Duration timeout) throws Exception {
            if (acceptedSocket != null) {
                return acceptedSocket;
            }
            acceptedSocket = socketQueue.poll(timeout.toMillis(), TimeUnit.MILLISECONDS);
            assertNotNull(acceptedSocket);
            return acceptedSocket;
        }

        @Override
        public void close() throws Exception {
            if (acceptedSocket != null) {
                acceptedSocket.close();
            }
            List<Socket> pendingSockets = socketQueue.stream().toList();
            for (Socket socket : pendingSockets) {
                socket.close();
            }
            serverSocket.close();
        }
    }

    private static final class BlockingInputStream extends InputStream {

        private final CountDownLatch readStartedLatch = new CountDownLatch(1);
        private final CountDownLatch finishLatch = new CountDownLatch(1);

        @Override
        public int read() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int read(byte[] bytes, int offset, int length) throws IOException {
            readStartedLatch.countDown();
            try {
                if (!finishLatch.await(2, TimeUnit.SECONDS)) {
                    throw new IOException("等待测试放行超时");
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IOException("测试线程被中断", ex);
            }
            return -1;
        }

        private void awaitReadStarted() throws InterruptedException {
            assertTrue(readStartedLatch.await(2, TimeUnit.SECONDS), "reader 未进入读取");
        }

        private void finish() {
            finishLatch.countDown();
        }
    }

    private static final class TestSocket extends Socket {

        private boolean closed;

        @Override
        public synchronized void close() {
            closed = true;
        }

        @Override
        public synchronized boolean isClosed() {
            return closed;
        }

        @Override
        public boolean isConnected() {
            return true;
        }
    }
}
