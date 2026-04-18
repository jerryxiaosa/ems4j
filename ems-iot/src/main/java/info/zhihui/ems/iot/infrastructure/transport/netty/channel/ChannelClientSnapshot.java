package info.zhihui.ems.iot.infrastructure.transport.netty.channel;

import info.zhihui.ems.common.enums.DeviceTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 在线客户端运行态快照。
 */
@Data
@Accessors(chain = true)
public class ChannelClientSnapshot {

    private String channelId;

    private String deviceNo;

    private DeviceTypeEnum deviceType;

    private boolean active;

    private boolean open;

    private boolean registered;

    private boolean writable;

    private boolean sending;

    private boolean pending;

    private int queueSize;

    private int abnormalCount;

    private String remoteAddress;

    private String localAddress;
}
