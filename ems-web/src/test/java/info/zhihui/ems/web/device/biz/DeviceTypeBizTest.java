package info.zhihui.ems.web.device.biz;

import info.zhihui.ems.foundation.integration.core.bo.DeviceTypeBo;
import info.zhihui.ems.foundation.integration.core.service.DeviceTypeService;
import info.zhihui.ems.web.device.mapstruct.DeviceTypeWebMapper;
import info.zhihui.ems.web.device.vo.DeviceTypeTreeVo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceTypeBizTest {

    @InjectMocks
    private DeviceTypeBiz deviceTypeBiz;

    @Mock
    private DeviceTypeService deviceTypeService;

    @Mock
    private DeviceTypeWebMapper deviceTypeWebMapper;

    @Test
    @DisplayName("查询设备品类树_存在父子节点和孤儿节点_应忽略孤儿节点")
    void testFindDeviceTypeTree_WithOrphanNode_ShouldIgnoreOrphanNode() {
        List<DeviceTypeBo> deviceTypeBoList = List.of(
                new DeviceTypeBo().setId(1).setPid(0).setTypeName("电表").setTypeKey("electric").setLevel(1),
                new DeviceTypeBo().setId(2).setPid(1).setTypeName("单相电表").setTypeKey("single").setLevel(2),
                new DeviceTypeBo().setId(3).setPid(999).setTypeName("孤儿节点").setTypeKey("orphan").setLevel(2)
        );
        List<DeviceTypeTreeVo> deviceTypeTreeVoList = List.of(
                new DeviceTypeTreeVo().setId(1).setPid(0).setTypeName("电表").setTypeKey("electric").setLevel(1),
                new DeviceTypeTreeVo().setId(2).setPid(1).setTypeName("单相电表").setTypeKey("single").setLevel(2),
                new DeviceTypeTreeVo().setId(3).setPid(999).setTypeName("孤儿节点").setTypeKey("orphan").setLevel(2)
        );
        when(deviceTypeService.findList(any())).thenReturn(deviceTypeBoList);
        when(deviceTypeWebMapper.toDeviceTypeTreeVoList(deviceTypeBoList)).thenReturn(deviceTypeTreeVoList);

        List<DeviceTypeTreeVo> result = deviceTypeBiz.findDeviceTypeTree();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1);
        assertThat(result.get(0).getChildren()).hasSize(1);
        assertThat(result.get(0).getChildren().get(0).getId()).isEqualTo(2);
        assertThat(result).flatExtracting(DeviceTypeTreeVo::getChildren)
                .extracting(DeviceTypeTreeVo::getId)
                .doesNotContain(3);
    }

    @Test
    @DisplayName("查询设备品类树_无数据_应返回空列表")
    void testFindDeviceTypeTree_WithoutData_ShouldReturnEmptyList() {
        when(deviceTypeService.findList(any())).thenReturn(Collections.emptyList());

        List<DeviceTypeTreeVo> result = deviceTypeBiz.findDeviceTypeTree();

        assertThat(result).isEmpty();
    }
}
