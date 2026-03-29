package info.zhihui.ems.web.device.biz;

import info.zhihui.ems.business.device.bo.ElectricMeterBo;
import info.zhihui.ems.business.device.service.ElectricMeterInfoService;
import info.zhihui.ems.business.device.service.ElectricMeterManagerService;
import info.zhihui.ems.business.billing.dto.ElectricMeterLatestPowerRecordDto;
import info.zhihui.ems.business.billing.dto.ElectricMeterPowerConsumeTrendPointDto;
import info.zhihui.ems.business.billing.dto.ElectricMeterPowerTrendPointDto;
import info.zhihui.ems.business.billing.service.record.ElectricMeterPowerConsumeRecordService;
import info.zhihui.ems.business.billing.service.record.ElectricMeterPowerRecordService;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.web.common.dto.SpaceDisplayDto;
import info.zhihui.ems.web.common.support.SpaceDisplaySupport;
import info.zhihui.ems.web.device.mapstruct.ElectricMeterWebMapper;
import info.zhihui.ems.web.device.vo.ElectricMeterDetailVo;
import info.zhihui.ems.web.device.vo.ElectricMeterLatestPowerRecordVo;
import info.zhihui.ems.web.device.vo.ElectricMeterPowerConsumeTrendPointVo;
import info.zhihui.ems.web.device.vo.ElectricMeterPowerTrendPointVo;
import info.zhihui.ems.web.device.vo.ElectricMeterPowerTrendQueryVo;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
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
    private ElectricMeterPowerConsumeRecordService electricMeterPowerConsumeRecordService;

    @Mock
    private SpaceDisplaySupport spaceDisplaySupport;

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
        SpaceDisplayDto spaceDisplayDto = new SpaceDisplayDto()
                .setId(10)
                .setName("101房间")
                .setParentsNames(List.of("1号楼", "1层"));
        when(electricMeterWebMapper.toElectricMeterQueryDto(any(ElectricMeterQueryVo.class))).thenReturn(null);
        when(electricMeterInfoService.findList(any())).thenReturn(List.of(electricMeterBo));
        when(electricMeterWebMapper.toElectricMeterVoList(List.of(electricMeterBo))).thenReturn(List.of(electricMeterVo));
        when(spaceDisplaySupport.findSpaceDisplayMap(any())).thenReturn(java.util.Map.of(10, spaceDisplayDto));

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
        ElectricMeterLatestPowerRecordDto latestPowerRecordDto = new ElectricMeterLatestPowerRecordDto()
                .setRecordTime(LocalDateTime.of(2026, 3, 10, 12, 30, 15))
                .setPower(new java.math.BigDecimal("123.45"));
        ElectricMeterLatestPowerRecordVo latestPowerRecordVo = new ElectricMeterLatestPowerRecordVo()
                .setRecordTime(LocalDateTime.of(2026, 3, 10, 12, 30, 15))
                .setPower(new java.math.BigDecimal("123.45"));
        ElectricMeterDetailVo detailVo = new ElectricMeterDetailVo();
        detailVo.setId(1);
        detailVo.setMeterName("电表A");
        SpaceDisplayDto spaceDisplayDto = new SpaceDisplayDto()
                .setId(10)
                .setName("101房间")
                .setParentsNames(List.of("1号楼", "1层"));
        when(electricMeterInfoService.getDetail(1)).thenReturn(electricMeterBo);
        when(electricMeterWebMapper.toElectricMeterDetailVo(electricMeterBo)).thenReturn(detailVo);
        when(electricMeterPowerRecordService.findLatestRecord(1)).thenReturn(latestPowerRecordDto);
        when(electricMeterWebMapper.toElectricMeterLatestPowerRecordVo(latestPowerRecordDto)).thenReturn(latestPowerRecordVo);
        when(spaceDisplaySupport.findSpaceDisplayMap(any())).thenReturn(java.util.Map.of(10, spaceDisplayDto));

        ElectricMeterDetailVo result = electricMeterBiz.getElectricMeter(1);

        assertThat(result.getSpaceName()).isEqualTo("101房间");
        assertThat(result.getSpaceParentNames()).containsExactly("1号楼", "1层");
        assertThat(result.getOfflineDurationText()).isEqualTo("2天");
        assertThat(result.getLatestPowerRecord()).isNotNull();
        assertThat(result.getLatestPowerRecord().getPower()).isEqualByComparingTo("123.45");
        assertThat(result.getLatestPowerRecord().getRecordTime()).isEqualTo(LocalDateTime.of(2026, 3, 10, 12, 30, 15));
    }

    @Test
    @DisplayName("查询电表趋势_应返回趋势点列表")
    void testFindPowerTrendList_ShouldReturnTrendPoints() {
        Integer meterId = 1;
        LocalDateTime beginTime = LocalDateTime.of(2026, 3, 27, 0, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 3, 28, 23, 59, 59);
        ElectricMeterBo electricMeterBo = new ElectricMeterBo().setId(meterId);
        ElectricMeterPowerTrendQueryVo queryVo = new ElectricMeterPowerTrendQueryVo()
                .setBeginTime(beginTime)
                .setEndTime(endTime);
        ElectricMeterPowerTrendPointDto trendPointDto = new ElectricMeterPowerTrendPointDto()
                .setRecordTime(LocalDateTime.of(2026, 3, 27, 8, 30, 0));
        ElectricMeterPowerTrendPointVo trendPointVo = new ElectricMeterPowerTrendPointVo()
                .setRecordTime(LocalDateTime.of(2026, 3, 27, 8, 30, 0));
        when(electricMeterInfoService.getDetail(meterId)).thenReturn(electricMeterBo);
        when(electricMeterPowerRecordService.findTrendRecordList(meterId, beginTime, endTime))
                .thenReturn(List.of(trendPointDto));
        when(electricMeterWebMapper.toElectricMeterPowerTrendPointVoList(List.of(trendPointDto)))
                .thenReturn(List.of(trendPointVo));

        List<ElectricMeterPowerTrendPointVo> result = electricMeterBiz.findPowerTrendList(meterId, queryVo);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRecordTime()).isEqualTo(LocalDateTime.of(2026, 3, 27, 8, 30, 0));
        verify(electricMeterPowerRecordService).findTrendRecordList(meterId, beginTime, endTime);
    }

    @Test
    @DisplayName("查询电表趋势_开始时间晚于结束时间应抛异常")
    void testFindPowerTrendList_WhenBeginTimeAfterEndTime_ShouldThrow() {
        Integer meterId = 1;
        ElectricMeterPowerTrendQueryVo queryVo = new ElectricMeterPowerTrendQueryVo()
                .setBeginTime(LocalDateTime.of(2026, 3, 29, 0, 0, 0))
                .setEndTime(LocalDateTime.of(2026, 3, 28, 23, 59, 59));
        when(electricMeterInfoService.getDetail(meterId)).thenReturn(new ElectricMeterBo().setId(meterId));

        assertThatThrownBy(() -> electricMeterBiz.findPowerTrendList(meterId, queryVo))
                .isInstanceOf(BusinessRuntimeException.class)
                .hasMessage("开始时间不能晚于结束时间");
    }

    @Test
    @DisplayName("查询电表区间耗电趋势_应返回趋势点列表")
    void testFindPowerConsumeTrendList_ShouldReturnTrendPoints() {
        Integer meterId = 1;
        LocalDateTime beginTime = LocalDateTime.of(2026, 3, 27, 0, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 3, 28, 23, 59, 59);
        ElectricMeterBo electricMeterBo = new ElectricMeterBo().setId(meterId);
        ElectricMeterPowerTrendQueryVo queryVo = new ElectricMeterPowerTrendQueryVo()
                .setBeginTime(beginTime)
                .setEndTime(endTime);
        ElectricMeterPowerConsumeTrendPointDto trendPointDto = new ElectricMeterPowerConsumeTrendPointDto()
                .setMeterConsumeTime(LocalDateTime.of(2026, 3, 27, 10, 30, 0));
        ElectricMeterPowerConsumeTrendPointVo trendPointVo = new ElectricMeterPowerConsumeTrendPointVo()
                .setMeterConsumeTime(LocalDateTime.of(2026, 3, 27, 10, 30, 0));
        when(electricMeterInfoService.getDetail(meterId)).thenReturn(electricMeterBo);
        when(electricMeterPowerConsumeRecordService.findTrendRecordList(meterId, beginTime, endTime))
                .thenReturn(List.of(trendPointDto));
        when(electricMeterWebMapper.toElectricMeterPowerConsumeTrendPointVoList(List.of(trendPointDto)))
                .thenReturn(List.of(trendPointVo));

        List<ElectricMeterPowerConsumeTrendPointVo> result = electricMeterBiz.findPowerConsumeTrendList(meterId, queryVo);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMeterConsumeTime()).isEqualTo(LocalDateTime.of(2026, 3, 27, 10, 30, 0));
        verify(electricMeterPowerConsumeRecordService).findTrendRecordList(meterId, beginTime, endTime);
    }
}
