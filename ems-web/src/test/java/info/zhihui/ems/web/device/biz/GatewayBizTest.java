package info.zhihui.ems.web.device.biz;

import info.zhihui.ems.business.device.bo.ElectricMeterBo;
import info.zhihui.ems.business.device.bo.GatewayBo;
import info.zhihui.ems.business.device.service.ElectricMeterInfoService;
import info.zhihui.ems.business.device.service.GatewayService;
import info.zhihui.ems.web.common.dto.SpaceDisplayDto;
import info.zhihui.ems.web.common.support.SpaceDisplaySupport;
import info.zhihui.ems.web.device.mapstruct.GatewayWebMapper;
import info.zhihui.ems.web.device.vo.GatewayDetailVo;
import info.zhihui.ems.web.device.vo.GatewayMeterVo;
import info.zhihui.ems.web.device.vo.GatewayQueryVo;
import info.zhihui.ems.web.device.vo.GatewayVo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GatewayBizTest {

    @InjectMocks
    private GatewayBiz gatewayBiz;

    @Mock
    private GatewayService gatewayService;

    @Mock
    private ElectricMeterInfoService electricMeterInfoService;

    @Mock
    private SpaceDisplaySupport spaceDisplaySupport;

    @Mock
    private GatewayWebMapper gatewayWebMapper;

    @Test
    @DisplayName("查询网关列表_应补充空间信息")
    void testFindGatewayList_ShouldFillSpaceInfo() {
        GatewayBo gatewayBo = new GatewayBo()
                .setId(1)
                .setSpaceId(10)
                .setGatewayName("网关A");
        GatewayVo gatewayVo = new GatewayVo()
                .setId(1)
                .setGatewayName("网关A");
        SpaceDisplayDto spaceDisplayDto = new SpaceDisplayDto()
                .setId(10)
                .setName("101房间")
                .setParentsNames(List.of("1号楼", "1层"));
        when(gatewayWebMapper.toGatewayQueryDto(any(GatewayQueryVo.class))).thenReturn(null);
        when(gatewayService.findList(any())).thenReturn(List.of(gatewayBo));
        when(gatewayWebMapper.toGatewayVoList(List.of(gatewayBo))).thenReturn(List.of(gatewayVo));
        when(spaceDisplaySupport.findSpaceDisplayMap(any())).thenReturn(java.util.Map.of(10, spaceDisplayDto));

        List<GatewayVo> result = gatewayBiz.findGatewayList(new GatewayQueryVo());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSpaceName()).isEqualTo("101房间");
        assertThat(result.get(0).getSpaceParentNames()).containsExactly("1号楼", "1层");
    }

    @Test
    @DisplayName("获取网关详情_应补充空间信息和电表列表")
    void testGetGateway_ShouldFillSpaceInfoAndMeterList() {
        GatewayBo gatewayBo = new GatewayBo()
                .setId(1)
                .setSpaceId(10)
                .setGatewayName("网关A");
        ElectricMeterBo meterBo = new ElectricMeterBo()
                .setId(100)
                .setMeterName("电表A")
                .setDeviceNo("DEVICE-001")
                .setIsOnline(Boolean.TRUE)
                .setPortNo(1)
                .setMeterAddress(11);
        GatewayDetailVo detailVo = new GatewayDetailVo();
        detailVo.setId(1);
        detailVo.setGatewayName("网关A");
        GatewayMeterVo meterVo = new GatewayMeterVo()
                .setId(100)
                .setMeterName("电表A")
                .setDeviceNo("DEVICE-001")
                .setIsOnline(Boolean.TRUE)
                .setPortNo(1)
                .setMeterAddress(11);
        SpaceDisplayDto spaceDisplayDto = new SpaceDisplayDto()
                .setId(10)
                .setName("101房间")
                .setParentsNames(List.of("1号楼", "1层"));
        when(gatewayService.getDetail(1)).thenReturn(gatewayBo);
        when(gatewayWebMapper.toGatewayDetailVo(gatewayBo)).thenReturn(detailVo);
        when(electricMeterInfoService.findList(any())).thenReturn(List.of(meterBo));
        when(gatewayWebMapper.toGatewayMeterVoList(List.of(meterBo))).thenReturn(List.of(meterVo));
        when(spaceDisplaySupport.findSpaceDisplayMap(any())).thenReturn(java.util.Map.of(10, spaceDisplayDto));

        GatewayDetailVo result = gatewayBiz.getGateway(1);

        assertThat(result.getSpaceName()).isEqualTo("101房间");
        assertThat(result.getSpaceParentNames()).containsExactly("1号楼", "1层");
        assertThat(result.getMeterList()).hasSize(1);
        assertThat(result.getMeterList().get(0).getMeterName()).isEqualTo("电表A");
        assertThat(result.getMeterList().get(0).getDeviceNo()).isEqualTo("DEVICE-001");
        assertThat(result.getMeterList().get(0).getPortNo()).isEqualTo(1);
        assertThat(result.getMeterList().get(0).getMeterAddress()).isEqualTo(11);
    }
}
