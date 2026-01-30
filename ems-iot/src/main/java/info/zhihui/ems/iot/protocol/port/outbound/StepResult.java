package info.zhihui.ems.iot.protocol.port.outbound;

import info.zhihui.ems.iot.domain.model.DeviceCommandResult;

/**
 * 多步骤命令的步骤执行结果。
 *
 * @param <R> 协议请求类型
 */
public final class StepResult<R> {

    private final boolean finished;
    private final R nextRequest;
    private final DeviceCommandResult result;

    private StepResult(boolean finished, R nextRequest, DeviceCommandResult result) {
        this.finished = finished;
        this.nextRequest = nextRequest;
        this.result = result;
    }

    /**
     * 创建继续下一步的结果。
     *
     * @param request 下一步请求
     * @return 步骤结果
     */
    public static <R> StepResult<R> next(R request) {
        return new StepResult<>(false, request, null);
    }

    /**
     * 创建完成执行的结果。
     *
     * @param result 最终结果
     * @return 步骤结果
     */
    public static <R> StepResult<R> done(DeviceCommandResult result) {
        return new StepResult<>(true, null, result);
    }

    /**
     * 是否已完成执行。
     *
     * @return 是否完成
     */
    public boolean isFinished() {
        return finished;
    }

    /**
     * 获取下一步请求。
     *
     * @return 下一步请求
     */
    public R getNextRequest() {
        return nextRequest;
    }

    /**
     * 获取最终结果。
     *
     * @return 最终结果
     */
    public DeviceCommandResult getResult() {
        return result;
    }
}
