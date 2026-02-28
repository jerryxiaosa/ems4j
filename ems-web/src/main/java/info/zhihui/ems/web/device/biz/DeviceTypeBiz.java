package info.zhihui.ems.web.device.biz;

import info.zhihui.ems.common.utils.TreeUtil;
import info.zhihui.ems.foundation.integration.core.bo.DeviceTypeBo;
import info.zhihui.ems.foundation.integration.core.dto.DeviceTypeQueryDto;
import info.zhihui.ems.foundation.integration.core.service.DeviceTypeService;
import info.zhihui.ems.web.device.mapstruct.DeviceTypeWebMapper;
import info.zhihui.ems.web.device.vo.DeviceTypeTreeVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 设备品类业务编排。
 */
@Service
@RequiredArgsConstructor
public class DeviceTypeBiz {

    private final DeviceTypeService deviceTypeService;
    private final DeviceTypeWebMapper deviceTypeWebMapper;

    /**
     * 查询设备品类树。
     *
     * @return 设备品类树
     */
    public List<DeviceTypeTreeVo> findDeviceTypeTree() {
        List<DeviceTypeBo> deviceTypeBoList = deviceTypeService.findList(new DeviceTypeQueryDto());
        if (deviceTypeBoList == null || deviceTypeBoList.isEmpty()) {
            return Collections.emptyList();
        }

        List<DeviceTypeTreeVo> deviceTypeTreeVoList = deviceTypeWebMapper.toDeviceTypeTreeVoList(deviceTypeBoList);
        return TreeUtil.buildTree(
                deviceTypeTreeVoList,
                DeviceTypeTreeVo::getId,
                DeviceTypeTreeVo::getPid,
                (node, children) -> node.setChildren(children.isEmpty() ? Collections.emptyList() : new ArrayList<>(children))
        );
    }
}
