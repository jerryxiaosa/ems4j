package info.zhihui.ems.iot.application;

import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.domain.model.Product;
import info.zhihui.ems.iot.domain.port.DeviceRegistry;
import info.zhihui.ems.iot.infrastructure.transport.netty.channel.ChannelClientSnapshot;
import info.zhihui.ems.iot.infrastructure.transport.netty.channel.ChannelManager;
import info.zhihui.ems.iot.vo.IotClientDetailVo;
import info.zhihui.ems.iot.vo.IotClientSimpleVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IotDebugAppService {

    private final ChannelManager channelManager;
    private final DeviceRegistry deviceRegistry;

    public List<IotClientSimpleVo> findClientList() {
        return channelManager.findClientSnapshotList().stream()
                .map(this::toSimpleVo)
                .sorted(Comparator.comparing(IotClientSimpleVo::getDeviceNo, Comparator.nullsLast(String::compareTo))
                        .thenComparing(IotClientSimpleVo::getChannelId, Comparator.nullsLast(String::compareTo)))
                .toList();
    }

    public IotClientDetailVo getClientDetail(String deviceNo) {
        ChannelClientSnapshot snapshot = channelManager.getClientSnapshotByDeviceNo(deviceNo);
        if (snapshot == null) {
            throw new NotFoundException("IoT客户端不存在");
        }

        IotClientDetailVo detailVo = toDetailVo(snapshot);
        if (!StringUtils.hasText(deviceNo)) {
            return detailVo;
        }

        try {
            Device device = deviceRegistry.getByDeviceNo(deviceNo);
            fillDeviceInfo(detailVo, device);
        } catch (NotFoundException ignored) {
            return detailVo;
        }
        return detailVo;
    }

    private IotClientSimpleVo toSimpleVo(ChannelClientSnapshot snapshot) {
        return new IotClientSimpleVo()
                .setChannelId(snapshot.getChannelId())
                .setDeviceNo(snapshot.getDeviceNo())
                .setDeviceType(snapshot.getDeviceType())
                .setActive(snapshot.isActive())
                .setPending(snapshot.isPending())
                .setQueueSize(snapshot.getQueueSize())
                .setAbnormalCount(snapshot.getAbnormalCount())
                .setRemoteAddress(snapshot.getRemoteAddress());
    }

    private IotClientDetailVo toDetailVo(ChannelClientSnapshot snapshot) {
        return new IotClientDetailVo()
                .setChannelId(snapshot.getChannelId())
                .setDeviceNo(snapshot.getDeviceNo())
                .setDeviceType(snapshot.getDeviceType())
                .setActive(snapshot.isActive())
                .setOpen(snapshot.isOpen())
                .setRegistered(snapshot.isRegistered())
                .setWritable(snapshot.isWritable())
                .setSending(snapshot.isSending())
                .setPending(snapshot.isPending())
                .setQueueSize(snapshot.getQueueSize())
                .setAbnormalCount(snapshot.getAbnormalCount())
                .setRemoteAddress(snapshot.getRemoteAddress())
                .setLocalAddress(snapshot.getLocalAddress());
    }

    private void fillDeviceInfo(IotClientDetailVo detailVo, Device device) {
        if (device == null) {
            return;
        }
        Product product = device.getProduct();
        detailVo.setParentId(device.getParentId())
                .setPortNo(device.getPortNo())
                .setMeterAddress(device.getMeterAddress())
                .setLastOnlineAt(device.getLastOnlineAt());
        if (product != null) {
            detailVo.setProductCode(product.getCode())
                    .setAccessMode(product.getAccessMode());
        }
    }
}
