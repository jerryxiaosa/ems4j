package info.zhihui.ems.iot.protocol.port.outbound;

import info.zhihui.ems.iot.domain.model.DeviceCommand;
import info.zhihui.ems.iot.domain.model.DeviceCommandResult;

/**
 * 多步骤命令翻译器（支持多次请求-响应流程）。
 *
 * @param <R> 协议请求类型
 */
public interface MultiStepDeviceCommandTranslator<R> extends DeviceCommandTranslator<R> {

    /**
     * 构建首个请求，默认复用 {@link #toRequest(DeviceCommand)}。
     *
     * @param command 领域命令
     * @return 首个协议请求
     */
    default R firstRequest(DeviceCommand command) {
        return toRequest(command);
    }

    /**
     * 多步命令不使用单步解析方法。
     *
     * @param command 领域命令
     * @param payload 响应报文
     * @return 解析结果
     */
    @Override
    default DeviceCommandResult parseResponse(DeviceCommand command, byte[] payload) {
        throw new UnsupportedOperationException("多步命令不使用parseResponse");
    }

    /**
     * 解析当前步骤响应，并决定是否进入下一步。
     *
     * @param command 领域命令
     * @param payload 当前步骤响应报文
     * @param context 步骤上下文
     * @return 步骤结果
     */
    StepResult<R> parseStep(DeviceCommand command, byte[] payload, StepContext context);
}
