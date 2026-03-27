package info.zhihui.ems.iot.simulator.protocol.acrel;

import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.constant.Acrel4gFrameConstants;
import info.zhihui.ems.iot.simulator.config.SimulatorTargetProperties;
import info.zhihui.ems.iot.simulator.runtime.SimulatorDeviceContext;
import info.zhihui.ems.iot.simulator.runtime.SimulatorDeviceContextUpdater;
import info.zhihui.ems.iot.util.HexUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * 安科瑞 4G 模拟器 TCP 会话。
 */
@Slf4j
public class Acrel4gSocketSession {

    private static final int READ_TIMEOUT_MS = 1000;

    private final SimulatorTargetProperties targetProperties;
    private final SimulatorDeviceContext deviceContext;
    private final Consumer<byte[]> inboundFrameConsumer;

    private final Object writeLock = new Object();

    private volatile Socket socket;
    private volatile InputStream inputStream;
    private volatile OutputStream outputStream;
    private volatile boolean closed;

    public Acrel4gSocketSession(SimulatorTargetProperties targetProperties,
                                SimulatorDeviceContext deviceContext,
                                Consumer<byte[]> inboundFrameConsumer) {
        this.targetProperties = Objects.requireNonNull(targetProperties, "targetProperties cannot be null");
        this.deviceContext = Objects.requireNonNull(deviceContext, "deviceContext cannot be null");
        this.inboundFrameConsumer = Objects.requireNonNull(inboundFrameConsumer, "inboundFrameConsumer cannot be null");
    }

    /**
     * 建立到目标 IoT 服务端的 TCP 连接，并启动入站读线程。
     */
    public synchronized void connect() throws IOException {
        closeResources();
        closed = false;
        Socket currentSocket = new Socket();
        currentSocket.connect(new InetSocketAddress(targetProperties.getHost(), targetProperties.getPort()),
                targetProperties.getConnectTimeoutMs());
        currentSocket.setSoTimeout(READ_TIMEOUT_MS);
        socket = currentSocket;
        inputStream = currentSocket.getInputStream();
        outputStream = currentSocket.getOutputStream();
        SimulatorDeviceContextUpdater.markConnected(deviceContext, UUID.randomUUID().toString(), LocalDateTime.now());
        log.info("模拟设备连接成功 deviceNo={} host={} port={} connectionId={}",
                deviceContext.getDeviceProperties().getDeviceNo(),
                targetProperties.getHost(),
                targetProperties.getPort(),
                deviceContext.getConnectionId());
        startReaderThread(currentSocket, inputStream);
    }

    /**
     * 判断当前 socket 是否处于可发送状态。
     */
    public boolean isConnected() {
        Socket currentSocket = socket;
        return currentSocket != null && currentSocket.isConnected() && !currentSocket.isClosed();
    }

    /**
     * 发送已经组装完成的协议帧，并刷新最近通信时间。
     */
    public void send(byte[] frame) throws IOException {
        if (frame == null || frame.length == 0) {
            return;
        }
        synchronized (writeLock) {
            if (!isConnected()) {
                throw new IOException("模拟器连接不可用");
            }
            outputStream.write(frame);
            outputStream.flush();
            SimulatorDeviceContextUpdater.touch(deviceContext, LocalDateTime.now());
            if (log.isDebugEnabled()) {
                log.debug("模拟设备发送原始帧 deviceNo={} bytes={} hex={}",
                        deviceContext.getDeviceProperties().getDeviceNo(),
                        frame.length,
                        HexUtil.bytesToHexString(frame));
            }
        }
    }

    /**
     * 主动关闭会话，读线程会在下次循环或 IO 异常时退出。
     */
    public synchronized void close() {
        closed = true;
        closeResources();
        SimulatorDeviceContextUpdater.markDisconnected(deviceContext);
    }

    /**
     * 启动单独的后台线程持续读取 IoT 侧下行数据。
     */
    private void startReaderThread(Socket currentSocket, InputStream currentInputStream) {
        Thread currentReaderThread = new Thread(() -> readLoop(currentSocket, currentInputStream),
                "acrel-simulator-reader-" + deviceContext.getDeviceProperties().getDeviceNo());
        currentReaderThread.setDaemon(true);
        currentReaderThread.start();
    }

    /**
     * 读取 socket 字节流并按完整帧切分后回调给上层处理。
     */
    private void readLoop(Socket currentSocket, InputStream currentInputStream) {
        byte[] readBuffer = new byte[1024];
        byte[] pendingBytes = new byte[0];
        try {
            while (!closed && !currentSocket.isClosed()) {
                int readLength;
                try {
                    readLength = currentInputStream.read(readBuffer);
                } catch (SocketTimeoutException ex) {
                    continue;
                }
                if (readLength < 0) {
                    break;
                }
                pendingBytes = append(pendingBytes, readBuffer, readLength);
                pendingBytes = dispatchFrames(pendingBytes);
            }
        } catch (IOException ex) {
            if (!closed) {
                log.warn("模拟设备读取连接异常 deviceNo={}", deviceContext.getDeviceProperties().getDeviceNo(), ex);
            }
        } finally {
            closeReaderConnection(currentSocket, currentInputStream);
        }
    }

    /**
     * 从缓存字节中依次切出完整的 7B7B...7D7D 协议帧。
     */
    private byte[] dispatchFrames(byte[] pendingBytes) {
        byte[] currentPendingBytes = pendingBytes;
        while (true) {
            int startIndex = findStartIndex(currentPendingBytes);
            if (startIndex < 0) {
                return new byte[0];
            }
            if (startIndex > 0) {
                currentPendingBytes = Arrays.copyOfRange(currentPendingBytes, startIndex, currentPendingBytes.length);
            }
            int endIndex = findEndIndex(currentPendingBytes);
            if (endIndex < 0) {
                return currentPendingBytes;
            }
            byte[] frame = Arrays.copyOfRange(currentPendingBytes, 0, endIndex + 2);
            if (log.isDebugEnabled()) {
                log.debug("模拟设备接收原始帧 deviceNo={} bytes={} hex={}",
                        deviceContext.getDeviceProperties().getDeviceNo(),
                        frame.length,
                        HexUtil.bytesToHexString(frame));
            }
            try {
                inboundFrameConsumer.accept(frame);
            } catch (RuntimeException ex) {
                log.warn("模拟器入站帧处理失败 deviceNo={}", deviceContext.getDeviceProperties().getDeviceNo(), ex);
                return new byte[0];
            }
            if (endIndex + 2 >= currentPendingBytes.length) {
                return new byte[0];
            }
            currentPendingBytes = Arrays.copyOfRange(currentPendingBytes, endIndex + 2, currentPendingBytes.length);
        }
    }

    /**
     * 查找帧起始分隔符位置。
     */
    private int findStartIndex(byte[] bytes) {
        for (int currentIndex = 0; currentIndex < bytes.length - 1; currentIndex++) {
            if (bytes[currentIndex] == Acrel4gFrameConstants.DELIMITER
                    && bytes[currentIndex + 1] == Acrel4gFrameConstants.DELIMITER) {
                return currentIndex;
            }
        }
        return -1;
    }

    /**
     * 查找帧结束分隔符位置。
     */
    private int findEndIndex(byte[] bytes) {
        for (int currentIndex = 2; currentIndex < bytes.length - 1; currentIndex++) {
            if (bytes[currentIndex] == Acrel4gFrameConstants.DELIMITER_END
                    && bytes[currentIndex + 1] == Acrel4gFrameConstants.DELIMITER_END) {
                return currentIndex;
            }
        }
        return -1;
    }

    /**
     * 将本次读取结果追加到未处理的缓存尾部。
     */
    private byte[] append(byte[] pendingBytes, byte[] readBuffer, int readLength) {
        byte[] mergedBytes = Arrays.copyOf(pendingBytes, pendingBytes.length + readLength);
        System.arraycopy(readBuffer, 0, mergedBytes, pendingBytes.length, readLength);
        return mergedBytes;
    }

    /**
     * 关闭当前会话持有的 IO 资源，并清空引用。
     */
    private synchronized void closeResources() {
        closeQuietly(inputStream);
        closeQuietly(outputStream);
        closeQuietly(socket);
        inputStream = null;
        outputStream = null;
        socket = null;
    }

    /**
     * reader 线程退出时只回收自己持有的连接资源；只有当前活跃连接退出时才清空共享字段并标记离线。
     */
    private void closeReaderConnection(Socket currentSocket, InputStream currentInputStream) {
        boolean activeConnection;
        synchronized (this) {
            activeConnection = currentSocket != null && currentSocket == socket;
            closeQuietly(currentInputStream);
            closeQuietly(currentSocket);
            if (activeConnection) {
                closeQuietly(outputStream);
                inputStream = null;
                outputStream = null;
                socket = null;
            }
        }
        if (activeConnection) {
            SimulatorDeviceContextUpdater.markDisconnected(deviceContext);
            log.info("模拟设备连接关闭 deviceNo={}", deviceContext.getDeviceProperties().getDeviceNo());
        }
    }

    /**
     * 安静关闭单个资源，避免在关闭链路中继续抛异常。
     */
    private void closeQuietly(AutoCloseable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Exception ignored) {
           // ignored
        }
    }
}
