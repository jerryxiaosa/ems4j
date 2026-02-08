package info.zhihui.ems.iot.domain.port;

import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.domain.model.Product;

public interface DeviceRegistry {

    /**
     * 保存设备信息。
     *
     * @param device 设备
     * @return 保存后的设备
     */
    Integer save(Device device);

    /**
     * 更新设备信息。
     *
     * @param device 设备
     */
    void update(Device device);

    /**
     * 按主键删除设备。
     *
     * @param id 设备ID
     * @throws NotFoundException 设备不存在
     */
    void deleteById(Integer id) throws NotFoundException;

    /**
     * 按主键获取设备。
     *
     * @param id 设备ID
     * @return 设备
     * @throws NotFoundException 设备不存在
     */
    Device getById(Integer id) throws NotFoundException;

    /**
     * 按设备编号获取设备。
     *
     * @param deviceNo 设备编号
     * @return 设备
     * @throws NotFoundException 设备不存在
     */
    Device getByDeviceNo(String deviceNo) throws NotFoundException;

    /**
     * 按父设备、串口号与电表通讯地址获取设备。
     *
     * @param parentId     父设备ID
     * @param portNo       串口号
     * @param meterAddress 电表通讯地址
     * @return 设备
     * @throws NotFoundException 设备不存在
     */
    Device getByParentIdAndPortNoAndMeterAddress(Integer parentId, Integer portNo,
                                                 Integer meterAddress) throws NotFoundException;

}
