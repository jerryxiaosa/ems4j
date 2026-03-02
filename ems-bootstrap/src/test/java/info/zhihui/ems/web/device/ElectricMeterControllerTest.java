package info.zhihui.ems.web.device;

import com.fasterxml.jackson.databind.ObjectMapper;
import info.zhihui.ems.business.plan.bo.ElectricPricePlanBo;
import info.zhihui.ems.business.plan.bo.WarnPlanBo;
import info.zhihui.ems.business.plan.service.ElectricPricePlanService;
import info.zhihui.ems.business.plan.service.WarnPlanService;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.config.satoken.SaWebConfig;
import info.zhihui.ems.components.translate.engine.TranslateEngine;
import info.zhihui.ems.components.translate.engine.TranslateMetadataCache;
import info.zhihui.ems.components.translate.resolver.EnumLabelResolver;
import info.zhihui.ems.components.translate.web.advice.ResponseTranslateAdvice;
import info.zhihui.ems.foundation.integration.core.bo.DeviceModelBo;
import info.zhihui.ems.foundation.integration.core.service.DeviceModelService;
import info.zhihui.ems.web.common.resolver.DeviceModelNameResolver;
import info.zhihui.ems.web.common.resolver.ElectricPricePlanNameResolver;
import info.zhihui.ems.web.common.resolver.WarnPlanNameResolver;
import info.zhihui.ems.web.device.biz.ElectricMeterBiz;
import info.zhihui.ems.web.device.controller.ElectricMeterController;
import info.zhihui.ems.web.device.vo.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ElectricMeterController.class)
@Import({
        ResponseTranslateAdvice.class,
        TranslateEngine.class,
        TranslateMetadataCache.class,
        EnumLabelResolver.class,
        DeviceModelNameResolver.class,
        WarnPlanNameResolver.class,
        ElectricPricePlanNameResolver.class
})
class ElectricMeterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ElectricMeterBiz electricMeterBiz;

    @MockitoBean
    private SaWebConfig saWebConfig;

    @MockitoBean
    private WarnPlanService warnPlanService;

    @MockitoBean
    private ElectricPricePlanService electricPricePlanService;

    @MockitoBean
    private DeviceModelService deviceModelService;

    @Test
    @DisplayName("分页查询电表应自动翻译展示字段")
    void testFindElectricMeterPage_ShouldTranslateLabels() throws Exception {
        ElectricMeterVo vo = new ElectricMeterVo()
                .setId(1)
                .setMeterName("MeterA")
                .setSpaceName("101房间")
                .setSpaceParentNames(List.of("1号楼", "1层"))
                .setModelId(33)
                .setPricePlanId(11)
                .setWarnPlanId(22)
                .setWarnType("FIRST")
                .setOfflineDurationText("2小时");
        PageResult<ElectricMeterVo> pageResult = new PageResult<ElectricMeterVo>()
                .setList(List.of(vo))
                .setPageNum(1)
                .setPageSize(10)
                .setTotal(1L);
        when(electricMeterBiz.findElectricMeterPage(any(), eq(1), eq(10))).thenReturn(pageResult);
        when(deviceModelService.findList(any())).thenReturn(List.of(new DeviceModelBo().setId(33).setModelName("DDSY-100")));
        when(electricPricePlanService.findList(any())).thenReturn(List.of(new ElectricPricePlanBo().setId(11).setName("居民电价")));
        when(warnPlanService.findList(any())).thenReturn(List.of(new WarnPlanBo().setId(22).setName("标准预警")));

        mockMvc.perform(get("/device/electric-meters/page")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.list[0].meterName").value("MeterA"))
                .andExpect(jsonPath("$.data.list[0].spaceName").value("101房间"))
                .andExpect(jsonPath("$.data.list[0].spaceParentNames[0]").value("1号楼"))
                .andExpect(jsonPath("$.data.list[0].modelName").value("DDSY-100"))
                .andExpect(jsonPath("$.data.list[0].pricePlanName").value("居民电价"))
                .andExpect(jsonPath("$.data.list[0].warnPlanName").value("标准预警"))
                .andExpect(jsonPath("$.data.list[0].electricWarnTypeName").value("一级预警"))
                .andExpect(jsonPath("$.data.list[0].offlineDurationText").value("2小时"));
    }

    @Test
    @DisplayName("获取电表详情应自动翻译展示字段")
    void testGetElectricMeter_ShouldTranslateLabels() throws Exception {
        ElectricMeterDetailVo detailVo = new ElectricMeterDetailVo();
        detailVo.setId(1);
        detailVo.setMeterName("MeterA");
        detailVo.setDeviceNo("DEV-001");
        detailVo.setSpaceName("101房间");
        detailVo.setSpaceParentNames(List.of("1号楼", "1层"));
        detailVo.setModelId(33);
        detailVo.setGatewayId(8);
        detailVo.setPortNo(2);
        detailVo.setMeterAddress(12);
        detailVo.setImei("IMEI-001");
        detailVo.setIsOnline(Boolean.FALSE);
        detailVo.setOfflineDurationText("2小时");
        detailVo.setIsCutOff(Boolean.FALSE);
        detailVo.setIotId("iot-1");
        detailVo.setIsCalculate(Boolean.TRUE);
        detailVo.setCalculateType(1);
        detailVo.setIsPrepay(Boolean.TRUE);
        detailVo.setProtectedModel(Boolean.FALSE);
        detailVo.setPricePlanId(11);
        detailVo.setWarnPlanId(22);
        detailVo.setWarnType("FIRST");
        detailVo.setAccountId(5);
        detailVo.setCt(30);
        detailVo.setOwnAreaId(9);
        when(electricMeterBiz.getElectricMeter(1)).thenReturn(detailVo);
        when(deviceModelService.findList(any())).thenReturn(List.of(new DeviceModelBo().setId(33).setModelName("DDSY-100")));
        when(electricPricePlanService.findList(any())).thenReturn(List.of(new ElectricPricePlanBo().setId(11).setName("居民电价")));
        when(warnPlanService.findList(any())).thenReturn(List.of(new WarnPlanBo().setId(22).setName("标准预警")));

        mockMvc.perform(get("/device/electric-meters/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.meterName").value("MeterA"))
                .andExpect(jsonPath("$.data.deviceNo").value("DEV-001"))
                .andExpect(jsonPath("$.data.spaceName").value("101房间"))
                .andExpect(jsonPath("$.data.spaceParentNames[1]").value("1层"))
                .andExpect(jsonPath("$.data.modelName").value("DDSY-100"))
                .andExpect(jsonPath("$.data.gatewayId").value(8))
                .andExpect(jsonPath("$.data.portNo").value(2))
                .andExpect(jsonPath("$.data.meterAddress").value(12))
                .andExpect(jsonPath("$.data.imei").value("IMEI-001"))
                .andExpect(jsonPath("$.data.isOnline").value(false))
                .andExpect(jsonPath("$.data.offlineDurationText").value("2小时"))
                .andExpect(jsonPath("$.data.isCutOff").value(false))
                .andExpect(jsonPath("$.data.iotId").value("iot-1"))
                .andExpect(jsonPath("$.data.isCalculate").value(true))
                .andExpect(jsonPath("$.data.calculateType").value(1))
                .andExpect(jsonPath("$.data.isPrepay").value(true))
                .andExpect(jsonPath("$.data.protectedModel").value(false))
                .andExpect(jsonPath("$.data.pricePlanName").value("居民电价"))
                .andExpect(jsonPath("$.data.warnPlanName").value("标准预警"))
                .andExpect(jsonPath("$.data.electricWarnTypeName").value("一级预警"))
                .andExpect(jsonPath("$.data.accountId").value(5))
                .andExpect(jsonPath("$.data.ct").value(30))
                .andExpect(jsonPath("$.data.ownAreaId").value(9));
    }

    @Test
    @DisplayName("新增电表")
    void testAddElectricMeter() throws Exception {
        when(electricMeterBiz.addElectricMeter(any(ElectricMeterCreateVo.class))).thenReturn(100);

        ElectricMeterCreateVo createVo = new ElectricMeterCreateVo()
                .setSpaceId(1)
                .setMeterName("MeterA")
                .setIsPrepay(Boolean.TRUE)
                .setModelId(2);

        mockMvc.perform(post("/device/electric-meters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createVo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(100));
    }

    @Test
    @DisplayName("查询电表电量")
    void testGetMeterPower() throws Exception {
        List<ElectricMeterPowerVo> powerVos = List.of(new ElectricMeterPowerVo().setType(0).setValue(BigDecimal.ONE));
        when(electricMeterBiz.getMeterPower(eq(1), any(ElectricMeterPowerQueryVo.class))).thenReturn(powerVos);

        ElectricMeterPowerQueryVo queryVo = new ElectricMeterPowerQueryVo().setTypes(List.of(0));

        mockMvc.perform(post("/device/electric-meters/1/power")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(queryVo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].type").value(0));
    }

    @Test
    @DisplayName("获取电表最近一次上报电量记录")
    void testGetLatestPowerRecord() throws Exception {
        ElectricMeterLatestPowerRecordVo latestPowerRecordVo = new ElectricMeterLatestPowerRecordVo()
                .setRecordTime(LocalDateTime.of(2026, 2, 28, 10, 58, 46))
                .setPower(new BigDecimal("1000.50"))
                .setPowerHigher(new BigDecimal("200.10"));
        when(electricMeterBiz.getLatestPowerRecord(1)).thenReturn(latestPowerRecordVo);

        mockMvc.perform(get("/device/electric-meters/1/latest-power-record"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.recordTime").value("2026-02-28 10:58:46"))
                .andExpect(jsonPath("$.data.power").value(1000.50))
                .andExpect(jsonPath("$.data.powerHigher").value(200.10));
    }

}
