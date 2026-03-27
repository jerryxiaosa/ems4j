package info.zhihui.ems.iot.simulator.state;

/**
 * 模拟器状态存储接口。
 */
public interface StateStore {

    SimulatorStateSnapshot load();

    void save(SimulatorStateSnapshot snapshot);
}
