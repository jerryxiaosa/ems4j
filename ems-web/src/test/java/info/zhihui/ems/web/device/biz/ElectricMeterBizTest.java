package info.zhihui.ems.web.device.biz;

import info.zhihui.ems.business.device.bo.ElectricMeterBo;
import info.zhihui.ems.business.device.service.ElectricMeterInfoService;
import info.zhihui.ems.business.device.service.ElectricMeterManagerService;
import info.zhihui.ems.business.finance.service.record.ElectricMeterPowerRecordService;
import info.zhihui.ems.foundation.space.bo.SpaceBo;
import info.zhihui.ems.foundation.space.service.SpaceService;
import info.zhihui.ems.web.device.mapstruct.ElectricMeterWebMapper;
import info.zhihui.ems.web.device.vo.ElectricMeterDetailVo;
import info.zhihui.ems.web.device.vo.ElectricMeterQueryVo;
import info.zhihui.ems.web.device.vo.ElectricMeterVo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ElectricMeterBizTest {

    @InjectMocks
    private ElectricMeterBiz electricMeterBiz;

    @Mock
    private ElectricMeterInfoService electricMeterInfoService;

    @Mock
    private ElectricMeterManagerService electricMeterManagerService;

    @Mock
    private ElectricMeterPowerRecordService electricMeterPowerRecordService;

    @Mock
    private SpaceService spaceService;

    @Mock
    private ElectricMeterWebMapper electricMeterWebMapper;

    @Test
    @DisplayName("查询电表列表_应补充空间信息")
    void testFindElectricMeterList_ShouldFillSpaceInfo() {
        ElectricMeterBo electricMeterBo = new ElectricMeterBo()
                .setId(1)
                .setSpaceId(10)
                .setMeterName("电表A")
                .setIsOnline(Boolean.FALSE)
                .setLastOnlineTime(LocalDateTime.now().minusHours(2));
        ElectricMeterVo electricMeterVo = new ElectricMeterVo()
                .setId(1)
                .setMeterName("电表A");
        SpaceBo spaceBo = new SpaceBo()
                .setId(10)
                .setName("101房间")
                .setParentsNames(List.of("1号楼", "1层"));
        when(electricMeterWebMapper.toElectricMeterQueryDto(any(ElectricMeterQueryVo.class))).thenReturn(null);
        when(electricMeterInfoService.findList(any())).thenReturn(List.of(electricMeterBo));
        when(electricMeterWebMapper.toElectricMeterVoList(List.of(electricMeterBo))).thenReturn(List.of(electricMeterVo));
        when(spaceService.findSpaceList(any())).thenReturn(List.of(spaceBo));

        List<ElectricMeterVo> result = electricMeterBiz.findElectricMeterList(new ElectricMeterQueryVo());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSpaceName()).isEqualTo("101房间");
        assertThat(result.get(0).getSpaceParentNames()).containsExactly("1号楼", "1层");
        assertThat(result.get(0).getOfflineDurationText()).isEqualTo("2小时");
    }

    @Test
    @DisplayName("获取电表详情_应补充空间信息")
    void testGetElectricMeter_ShouldFillSpaceInfo() {
        ElectricMeterBo electricMeterBo = new ElectricMeterBo()
                .setId(1)
                .setSpaceId(10)
                .setMeterName("电表A")
                .setIsOnline(Boolean.FALSE)
                .setLastOnlineTime(LocalDateTime.now().minusDays(2));
        ElectricMeterDetailVo detailVo = new ElectricMeterDetailVo();
        detailVo.setId(1);
        detailVo.setMeterName("电表A");
        SpaceBo spaceBo = new SpaceBo()
                .setId(10)
                .setName("101房间")
                .setParentsNames(List.of("1号楼", "1层"));
        when(electricMeterInfoService.getDetail(1)).thenReturn(electricMeterBo);
        when(electricMeterWebMapper.toElectricMeterDetailVo(electricMeterBo)).thenReturn(detailVo);
        when(spaceService.findSpaceList(any())).thenReturn(List.of(spaceBo));

        ElectricMeterDetailVo result = electricMeterBiz.getElectricMeter(1);

        assertThat(result.getSpaceName()).isEqualTo("101房间");
        assertThat(result.getSpaceParentNames()).containsExactly("1号楼", "1层");
        assertThat(result.getOfflineDurationText()).isEqualTo("2天");
    }
}
