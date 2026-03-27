package info.zhihui.ems.iot.simulator.service;

import info.zhihui.ems.iot.plugins.acrel.protocol.fourthgeneration.tcp.support.Acrel4gFrameCodec;
import info.zhihui.ems.iot.simulator.config.SimulatorProperties;
import info.zhihui.ems.iot.simulator.protocol.acrel.Acrel4gCommandResponder;
import info.zhihui.ems.iot.simulator.protocol.acrel.Acrel4gSimulatorClient;
import info.zhihui.ems.iot.simulator.runtime.SimulatorDeviceContext;
import info.zhihui.ems.iot.simulator.state.SimulatorStateSnapshot;
import info.zhihui.ems.iot.simulator.state.StateStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 模拟器生命周期管理器。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SimulatorLifecycle implements SmartLifecycle {

    private final SimulatorProperties simulatorProperties;
    private final StateStore stateStore;
    private final EnergySimulationService energySimulationService;
    private final ReplayScheduleService replayScheduleService;
    private final Acrel4gSimulatorClient acrel4gSimulatorClient;
    private final Acrel4gCommandResponder acrel4gCommandResponder;
    private final Acrel4gFrameCodec acrel4gFrameCodec;
    private final SimulatorLauncher simulatorLauncher;

    private final List<SimulatorDeviceRuntime> deviceRuntimes = new ArrayList<>();

    private volatile boolean running;

    /**
     * 启动整个模拟器进程，加载状态、构建设备运行器并依次启动。
     */
    @Override
    public synchronized void start() {
        if (running) {
            return;
        }
        SimulatorStateSnapshot stateSnapshot = simulatorLauncher.loadStateSnapshot();
        List<SimulatorDeviceContext> deviceContexts = simulatorLauncher.loadDeviceContexts(stateSnapshot);
        deviceRuntimes.clear();
        for (SimulatorDeviceContext deviceContext : deviceContexts) {
            deviceRuntimes.add(new SimulatorDeviceRuntime(
                    simulatorProperties,
                    stateStore,
                    energySimulationService,
                    replayScheduleService,
                    acrel4gSimulatorClient,
                    acrel4gCommandResponder,
                    acrel4gFrameCodec,
                    deviceContext,
                    stateSnapshot));
        }
        for (SimulatorDeviceRuntime deviceRuntime : deviceRuntimes) {
            deviceRuntime.start();
        }
        running = true;
        log.info("IoT 模拟器已启动，设备数={}", deviceRuntimes.size());
    }

    /**
     * 停止所有设备运行器并清理内存中的运行列表。
     */
    @Override
    public synchronized void stop() {
        for (SimulatorDeviceRuntime deviceRuntime : deviceRuntimes) {
            deviceRuntime.stop();
        }
        deviceRuntimes.clear();
        running = false;
        log.info("IoT 模拟器已停止");
    }

    /**
     * 按 SmartLifecycle 约定执行停止回调，确保 Spring 容器能继续关闭流程。
     */
    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }

    /**
     * 返回模拟器整体是否处于运行状态。
     */
    @Override
    public boolean isRunning() {
        return running;
    }

    /**
     * 声明容器刷新后自动启动，无需额外手工触发。
     */
    @Override
    public boolean isAutoStartup() {
        return true;
    }

    /**
     * 让模拟器尽量在容器生命周期靠后阶段启动，避免抢在基础 Bean 前执行。
     */
    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }
}
