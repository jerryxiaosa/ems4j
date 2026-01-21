package info.zhihui.ems.iot.protocol.decode;

public record FrameDecodeResult(String commandKey, byte[] payload, ProtocolDecodeErrorEnum reason) {
}
