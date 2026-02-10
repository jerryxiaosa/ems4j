package info.zhihui.ems.business.device.service.impl;

import com.github.pagehelper.PageInfo;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.business.device.bo.ElectricMeterBo;
import info.zhihui.ems.business.device.bo.GatewayBo;
import info.zhihui.ems.business.device.dto.ElectricMeterQueryDto;
import info.zhihui.ems.business.device.dto.GatewayCreateDto;
import info.zhihui.ems.business.device.dto.GatewayOnlineStatusDto;
import info.zhihui.ems.business.device.dto.GatewayQueryDto;
import info.zhihui.ems.business.device.dto.GatewayUpdateDto;
import info.zhihui.ems.business.device.entity.GatewayEntity;
import info.zhihui.ems.business.device.mapper.GatewayMapper;
import info.zhihui.ems.business.device.qo.GatewayQo;
import info.zhihui.ems.business.device.repository.GatewayRepository;
import info.zhihui.ems.business.device.service.ElectricMeterInfoService;
import info.zhihui.ems.business.device.service.DeviceStatusSynchronizer;
import info.zhihui.ems.business.device.service.impl.sync.GatewayOnlineStatusSynchronizer;
import info.zhihui.ems.foundation.integration.core.bo.DeviceModelBo;
import info.zhihui.ems.foundation.integration.core.service.DeviceModelService;
import info.zhihui.ems.foundation.integration.core.service.DeviceModuleContext;
import info.zhihui.ems.foundation.integration.concrete.energy.dto.BaseElectricDeviceDto;
import info.zhihui.ems.foundation.integration.concrete.energy.dto.ElectricDeviceAddDto;
import info.zhihui.ems.foundation.integration.concrete.energy.dto.ElectricDeviceUpdateDto;
import info.zhihui.ems.foundation.integration.concrete.energy.service.EnergyService;
import info.zhihui.ems.foundation.space.bo.SpaceBo;
import info.zhihui.ems.foundation.space.enums.SpaceTypeEnum;
import info.zhihui.ems.foundation.space.service.SpaceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GatewayServiceImplTest {

    @Mock
    private GatewayRepository repository;

    @Mock
    private GatewayMapper mapper;

    @Mock
    private DeviceModelService deviceModelService;

    @Mock
    private SpaceService spaceService;

    @Mock
    private DeviceModuleContext deviceModuleContext;

    private DeviceStatusSynchronizer<GatewayEntity> gatewayOnlineStatusSynchronizer;

    @Mock
    private ElectricMeterInfoService electricMeterInfoService;

    @Mock
    private EnergyService energyService;

    @InjectMocks
    private GatewayServiceImpl gatewayService;

    private GatewayEntity entity;
    private GatewayBo bo;
    private GatewayCreateDto createDto;
    private GatewayUpdateDto updateDto;
    private GatewayQueryDto queryDto;
    private GatewayQo qo;
    private PageParam pageParam;
    private DeviceModelBo deviceModelBo;
    private SpaceBo spaceBo;

    @BeforeEach
    void setUp() {
        entity = new GatewayEntity();
        entity.setId(1)
                .setSpaceId(100)
                .setGatewayNo("GW202401010001")
                .setDeviceNo("SN123456")
                .setGatewayName("测试网关")
                .setModelId(200)
                .setProductCode("厂商-型号")
                .setCommunicateModel("485")
                .setSn("SN123456")
                .setImei("")
                .setIotId("12345")
                .setIsOnline(true)
                .setConfigInfo("{\"productCode\":\"TEST_PRODUCT\",\"data\":\"test_data\"}")
                .setRemark("测试备注")
                .setOwnAreaId(1000);

        bo = new GatewayBo()
                .setId(1)
                .setSpaceId(100)
                .setGatewayNo("GW202401010001")
                .setDeviceNo("SN123456")
                .setGatewayName("测试网关")
                .setModelId(200)
                .setProductCode("厂商-型号")
                .setCommunicateModel("485")
                .setSn("SN123456")
                .setImei("")
                .setIotId("12345")
                .setIsOnline(true)
                .setConfigInfo("{\"productCode\":\"TEST_PRODUCT\",\"data\":\"test_data\"}")
                .setRemark("测试备注")
                .setOwnAreaId(1000);

        gatewayOnlineStatusSynchronizer = new GatewayOnlineStatusSynchronizer(deviceModuleContext, repository);
        ReflectionTestUtils.setField(gatewayService, "gatewayOnlineStatusSynchronizer", gatewayOnlineStatusSynchronizer);

        createDto = new GatewayCreateDto();
        createDto.setSpaceId(100);
        createDto.setGatewayName("测试网关");
        createDto.setModelId(200);
        createDto.setDeviceNo("SN123456");
        createDto.setSn("SN123456");
        createDto.setImei("");
        createDto.setConfigInfo("{\"productCode\":\"TEST_PRODUCT\",\"data\":\"test_data\"}");
        createDto.setRemark("测试备注");

        updateDto = new GatewayUpdateDto();
        updateDto.setId(1);
        updateDto.setSpaceId(100);
        updateDto.setGatewayName("测试网关");
        updateDto.setModelId(200);
        updateDto.setDeviceNo("SN123456");
        updateDto.setSn("SN123456");
        updateDto.setImei("");
        updateDto.setConfigInfo("{\"productCode\":\"TEST_PRODUCT\",\"data\":\"test_data\"}");
        updateDto.setRemark("测试备注");

        queryDto = new GatewayQueryDto()
                .setSearchKey("测试")
                .setSn("SN123456")
                .setIsOnline(true);

        qo = new GatewayQo()
                .setSearchKey("测试")
                .setSn("SN123456")
                .setIsOnline(true);

        pageParam = new PageParam();

        Map<String, Object> modelProperty = Map.of("communicateModel", "485");
        deviceModelBo = new DeviceModelBo()
                .setId(200)
                .setManufacturerName("厂商")
                .setModelName("型号")
                .setProductCode("gateway-model-200")
                .setTypeKey("gateway")
                .setModelProperty(modelProperty);

        spaceBo = new SpaceBo()
                .setId(100)
                .setName("测试空间")
                .setOwnAreaId(1000)
                .setType(SpaceTypeEnum.ROOM);

    }

    @Test
    void testGetDetail_Success() {
        // Mock行为
        when(repository.selectById(1)).thenReturn(entity);
        when(mapper.entityToBo(entity)).thenReturn(bo);

        // 执行测试
        GatewayBo result = gatewayService.getDetail(1);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("测试网关", result.getGatewayName());

        // 验证方法调用
        verify(repository).selectById(1);
        verify(mapper).entityToBo(entity);
    }

    @Test
    void testGetDetail_NotFound() {
        // Mock行为
        when(repository.selectById(1)).thenReturn(null);

        // 执行测试并验证异常
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> gatewayService.getDetail(1));

        assertEquals("网关不存在，请确认", exception.getMessage());
        verify(repository).selectById(1);
        verify(mapper, never()).entityToBo(any());
    }

    @Test
    void testFindPage_Success() {
        // 准备数据
        PageResult<GatewayBo> expectedResult = new PageResult<>();

        // Mock行为
        when(mapper.queryDtoToQo(queryDto)).thenReturn(qo);
        when(mapper.pageEntityToBo(any(PageInfo.class))).thenReturn(expectedResult);

        // 执行测试
        PageResult<GatewayBo> result = gatewayService.findPage(queryDto, pageParam);

        // 验证结果
        assertNotNull(result);
        assertEquals(expectedResult, result);

        // 验证方法调用
        verify(mapper).queryDtoToQo(queryDto);
        verify(mapper).pageEntityToBo(any(PageInfo.class));
    }

    @Test
    void testFindList_Success() {
        // 准备数据
        List<GatewayEntity> entityList = List.of(entity);
        List<GatewayBo> expectedResult = List.of(bo);

        // Mock行为
        when(mapper.queryDtoToQo(queryDto)).thenReturn(qo);
        when(repository.findList(qo)).thenReturn(entityList);
        when(mapper.listEntityToBo(entityList)).thenReturn(expectedResult);

        // 执行测试
        List<GatewayBo> result = gatewayService.findList(queryDto);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedResult, result);

        // 验证方法调用
        verify(mapper).queryDtoToQo(queryDto);
        verify(repository).findList(qo);
        verify(mapper).listEntityToBo(entityList);
    }

    @Test
    void testAdd_Success() {
        // 准备数据
        GatewayEntity insertEntity = new GatewayEntity();
        insertEntity.setSpaceId(100)
                .setGatewayName("测试网关")
                .setDeviceNo("SN123456")
                .setModelId(200)
                .setCommunicateModel("485")
                .setSn("SN123456")
                .setImei("")
                .setConfigInfo("""
                        {"serverIp":"192.168.2.23",
                                    "aesKey":"fromhdsegtoacrel",
                                    "serverPort":"19500",
                                    "gatewayId":"TC888"
                        }""")
                .setRemark("测试备注")
                .setOwnAreaId(1000);

        // Mock行为
        when(mapper.createDtoToEntity(createDto)).thenReturn(insertEntity);
        when(deviceModelService.getDetail(200)).thenReturn(deviceModelBo);
        when(spaceService.getDetail(100)).thenReturn(spaceBo);
        when(deviceModuleContext.getService(EnergyService.class, 1000)).thenReturn(energyService);
        when(energyService.addDevice(any(ElectricDeviceAddDto.class))).thenReturn("12345");

        // 模拟insert后设置ID
        doAnswer(invocation -> {
            GatewayEntity arg = invocation.getArgument(0);
            arg.setId(1);
            return null;
        }).when(repository).insert(any(GatewayEntity.class));

        // 执行测试
        Integer result = gatewayService.add(createDto);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result);

        // 验证方法调用
        verify(mapper).createDtoToEntity(createDto);
        verify(deviceModelService).getDetail(200);
        verify(spaceService).getDetail(100);

        ArgumentCaptor<GatewayEntity> gatewayCaptor = ArgumentCaptor.forClass(GatewayEntity.class);
        verify(repository).insert(gatewayCaptor.capture());
        GatewayEntity capturedGateway = gatewayCaptor.getValue();
        assertEquals(100, capturedGateway.getSpaceId());
        assertEquals(200, capturedGateway.getModelId());

        verify(deviceModuleContext).getService(EnergyService.class, 1000);

        // 验证IoT设备添加参数
        ArgumentCaptor<ElectricDeviceAddDto> addDtoCaptor = ArgumentCaptor.forClass(ElectricDeviceAddDto.class);
        verify(energyService).addDevice(addDtoCaptor.capture());
        ElectricDeviceAddDto capturedAddDto = addDtoCaptor.getValue();
        assertEquals("gateway-model-200", capturedAddDto.getProductCode());
        assertEquals(1000, capturedAddDto.getAreaId());
    }

    @Test
    void testAdd_DeviceModelNotFound() {
        // Mock行为
        when(mapper.createDtoToEntity(createDto)).thenReturn(entity);
        when(deviceModelService.getDetail(200)).thenReturn(null);

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> gatewayService.add(createDto));

        assertEquals("网关型号不存在，请重新选择", exception.getMessage());
        verify(mapper).createDtoToEntity(createDto);
        verify(deviceModelService).getDetail(200);
        verify(repository, never()).insert(any(GatewayEntity.class));
    }

    @Test
    void testAdd_DeviceModelTypeMismatch() {
        DeviceModelBo wrongModel = new DeviceModelBo()
                .setId(200)
                .setManufacturerName("厂商")
                .setModelName("型号")
                .setProductCode("meter-model-200")
                .setTypeKey("electricMeter")
                .setModelProperty(Map.of("communicateModel", "485"));

        when(mapper.createDtoToEntity(createDto)).thenReturn(entity);
        when(deviceModelService.getDetail(200)).thenReturn(wrongModel);

        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> gatewayService.add(createDto));

        assertEquals("网关型号设置错误", exception.getMessage());
        verify(repository, never()).insert(any(GatewayEntity.class));
    }

    @Test
    void testAdd_NbModeWithoutImei() {
        // 准备数据 - NB模式但没有IMEI
        Map<String, Object> nbModelProperty = Map.of("communicateModel", "nb");
        DeviceModelBo nbDeviceModel = new DeviceModelBo()
                .setId(200)
                .setManufacturerName("厂商")
                .setProductCode("gateway-nb-200")
                .setTypeKey("gateway")
                .setModelProperty(nbModelProperty);

        createDto.setImei(""); // 空IMEI

        // Mock行为
        when(mapper.createDtoToEntity(createDto)).thenReturn(entity);
        when(deviceModelService.getDetail(200)).thenReturn(nbDeviceModel);

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> gatewayService.add(createDto));

        assertEquals("NB模式网关IMEI不能为空", exception.getMessage());
        verify(mapper).createDtoToEntity(createDto);
        verify(deviceModelService).getDetail(200);
        verify(repository, never()).insert(any(GatewayEntity.class));
    }

    @Test
    void testDelete_Success_WithIotId() {
        // 准备数据
        GatewayEntity deleteEntity = new GatewayEntity();
        deleteEntity.setId(1)
                .setIotId("12345")
                .setOwnAreaId(1000);

        // Mock行为
        when(repository.selectById(1)).thenReturn(deleteEntity);
        when(electricMeterInfoService.findList(any(ElectricMeterQueryDto.class))).thenReturn(Collections.emptyList());
        when(deviceModuleContext.getService(EnergyService.class, 1000)).thenReturn(energyService);
        when(repository.deleteById(1)).thenReturn(1);

        // 执行测试
        assertDoesNotThrow(() -> gatewayService.delete(1));

        // 验证方法调用
        verify(repository).selectById(1);
        ArgumentCaptor<ElectricMeterQueryDto> queryCaptor = ArgumentCaptor.forClass(ElectricMeterQueryDto.class);
        verify(electricMeterInfoService).findList(queryCaptor.capture());
        ElectricMeterQueryDto capturedQuery = queryCaptor.getValue();
        assertEquals(1, capturedQuery.getGatewayId());
        verify(deviceModuleContext).getService(EnergyService.class, 1000);
        verify(repository).deleteById(1);

        // 验证IoT设备删除参数
        ArgumentCaptor<BaseElectricDeviceDto> deleteDtoCaptor = ArgumentCaptor.forClass(BaseElectricDeviceDto.class);
        verify(energyService).delDevice(deleteDtoCaptor.capture());
        BaseElectricDeviceDto capturedDeleteDto = deleteDtoCaptor.getValue();
        assertEquals("12345", capturedDeleteDto.getDeviceId());
        assertEquals(1000, capturedDeleteDto.getAreaId());
    }

    @Test
    void testDelete_Success_WithoutIotId() {
        // 准备数据 - 没有iotId的网关
        GatewayEntity deleteEntity = new GatewayEntity();
        deleteEntity.setId(1)
                .setIotId(null) // 没有iotId
                .setOwnAreaId(1000);

        // Mock行为
        when(repository.selectById(1)).thenReturn(deleteEntity);
        when(electricMeterInfoService.findList(any(ElectricMeterQueryDto.class))).thenReturn(Collections.emptyList());
        when(repository.deleteById(1)).thenReturn(1);

        // 执行测试
        assertDoesNotThrow(() -> gatewayService.delete(1));

        // 验证方法调用
        verify(repository).selectById(1);
        ArgumentCaptor<ElectricMeterQueryDto> queryCaptor = ArgumentCaptor.forClass(ElectricMeterQueryDto.class);
        verify(electricMeterInfoService).findList(queryCaptor.capture());
        ElectricMeterQueryDto capturedQuery = queryCaptor.getValue();
        assertEquals(1, capturedQuery.getGatewayId());
        verify(repository).deleteById(1);

        // 验证不会调用IoT平台删除
        verify(deviceModuleContext, never()).getService(any(), any());
        verify(energyService, never()).delDevice(any());
    }

    @Test
    void testDelete_GatewayNotFound() {
        // Mock行为
        when(repository.selectById(999)).thenReturn(null);

        // 执行测试并验证异常
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> gatewayService.delete(999));

        assertEquals("网关不存在，请确认", exception.getMessage());
        verify(repository).selectById(999);
        verify(repository, never()).deleteById(any());
        verify(deviceModuleContext, never()).getService(any(), any());
        verify(energyService, never()).delDevice(any());
    }

    @Test
    void testDelete_HasAssociatedMeters() {
        // 准备数据
        GatewayEntity deleteEntity = new GatewayEntity();
        deleteEntity.setId(1)
                .setIotId("12345")
                .setOwnAreaId(1000);

        ElectricMeterBo associatedMeter = new ElectricMeterBo()
                .setId(100)
                .setMeterName("关联电表")
                .setGatewayId(1);

        // Mock行为
        when(repository.selectById(1)).thenReturn(deleteEntity);
        when(electricMeterInfoService.findList(any(ElectricMeterQueryDto.class)))
                .thenReturn(List.of(associatedMeter));

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> gatewayService.delete(1));

        assertEquals("该网关下还有关联电表，请先处理这些电表后再删除网关", exception.getMessage());

        // 验证方法调用
        verify(repository).selectById(1);
        ArgumentCaptor<ElectricMeterQueryDto> queryCaptor = ArgumentCaptor.forClass(ElectricMeterQueryDto.class);
        verify(electricMeterInfoService).findList(queryCaptor.capture());
        ElectricMeterQueryDto capturedQuery = queryCaptor.getValue();
        assertEquals(1, capturedQuery.getGatewayId());

        // 验证不会执行删除操作
        verify(repository, never()).deleteById(any());
        verify(deviceModuleContext, never()).getService(any(), any());
        verify(energyService, never()).delDevice(any());
    }

    @Test
    void testGetCommunicationOption_Success() {
        // 准备数据
        List<String> expectedOptions = List.of("485", "NB", "WIFI");

        // Mock行为
        when(repository.getCommunicationOption()).thenReturn(expectedOptions);

        // 执行测试
        List<String> result = gatewayService.getCommunicationOption();

        // 验证结果
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("485", result.get(0));
        assertEquals("NB", result.get(1));
        assertEquals("WIFI", result.get(2));

        // 验证方法调用
        verify(repository).getCommunicationOption();
    }

    @Test
    void testGetCommunicationOption_EmptyList() {
        // 准备数据 - 空列表
        List<String> expectedOptions = List.of();

        // Mock行为
        when(repository.getCommunicationOption()).thenReturn(expectedOptions);

        // 执行测试
        List<String> result = gatewayService.getCommunicationOption();

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // 验证方法调用
        verify(repository).getCommunicationOption();
    }

    @Test
    void testUpdate_Success() {
        // 准备数据
        GatewayEntity oldEntity = new GatewayEntity();
        oldEntity.setId(1)
                .setGatewayNo("GW202401010001")
                .setDeviceNo("SN123456")
                .setIotId("12345")
                .setOwnAreaId(1000);

        GatewayEntity updateEntity = new GatewayEntity();
        updateEntity.setId(1)
                .setSpaceId(100)
                .setGatewayName("更新后的网关")
                .setDeviceNo("SN123456")
                .setModelId(200)
                .setCommunicateModel("485")
                .setSn("SN123456")
                .setImei("")
                .setConfigInfo("{\"productCode\":\"TEST_PRODUCT\",\"data\":\"test_data\"}")
                .setRemark("更新后的备注")
                .setOwnAreaId(1000);

        // Mock行为
        when(repository.selectById(1)).thenReturn(oldEntity);
        when(mapper.updateDtoToEntity(updateDto)).thenReturn(updateEntity);
        when(deviceModelService.getDetail(200)).thenReturn(deviceModelBo);
        when(spaceService.getDetail(100)).thenReturn(spaceBo);
        when(deviceModuleContext.getService(EnergyService.class, 1000)).thenReturn(energyService);
        doNothing().when(energyService).editDevice(any(ElectricDeviceUpdateDto.class));
        when(repository.updateById(any(GatewayEntity.class))).thenReturn(1);

        // 执行测试
        assertDoesNotThrow(() -> gatewayService.update(updateDto));

        // 验证方法调用
        verify(repository).selectById(1);
        verify(mapper).updateDtoToEntity(updateDto);
        verify(deviceModelService).getDetail(200);
        verify(spaceService).getDetail(100);
        verify(deviceModuleContext).getService(EnergyService.class, 1000);

        // 验证IoT设备更新参数
        ArgumentCaptor<ElectricDeviceUpdateDto> updateDtoCaptor = ArgumentCaptor.forClass(ElectricDeviceUpdateDto.class);
        verify(energyService).editDevice(updateDtoCaptor.capture());
        ElectricDeviceUpdateDto capturedUpdateDto = updateDtoCaptor.getValue();
        assertEquals("gateway-model-200", capturedUpdateDto.getProductCode());
        assertEquals("12345", capturedUpdateDto.getDeviceId());
        assertEquals(1000, capturedUpdateDto.getAreaId());

        ArgumentCaptor<GatewayEntity> gatewayCaptor = ArgumentCaptor.forClass(GatewayEntity.class);
        verify(repository).updateById(gatewayCaptor.capture());
        GatewayEntity capturedGateway = gatewayCaptor.getValue();
        assertEquals(100, capturedGateway.getSpaceId());
        assertEquals(200, capturedGateway.getModelId());
    }

    @Test
    void testUpdate_GatewayNotFound() {
        // 准备数据
        updateDto.setId(999); // 不存在的ID

        // Mock行为
        when(repository.selectById(999)).thenReturn(null);

        // 执行测试并验证异常
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> gatewayService.update(updateDto));

        assertEquals("网关不存在，请确认", exception.getMessage());
        verify(repository).selectById(999);
        verify(mapper, never()).updateDtoToEntity(any());
        verify(repository, never()).updateById(any(GatewayEntity.class));
    }

    @Test
    void testUpdate_DeviceModelNotFound() {
        // 准备数据
        updateDto.setId(1);
        updateDto.setModelId(999); // 不存在的设备型号ID

        GatewayEntity oldEntity = new GatewayEntity();
        oldEntity.setId(1).setIotId("12345");

        GatewayEntity updateEntity = new GatewayEntity();
        updateEntity.setModelId(999).setDeviceNo("SN123456");

        // Mock行为
        when(repository.selectById(1)).thenReturn(oldEntity);
        when(mapper.updateDtoToEntity(updateDto)).thenReturn(updateEntity);
        when(deviceModelService.getDetail(999)).thenReturn(null);

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> gatewayService.update(updateDto));

        assertEquals("网关型号不存在，请重新选择", exception.getMessage());
        verify(repository).selectById(1);
        verify(mapper).updateDtoToEntity(updateDto);
        verify(deviceModelService).getDetail(999);
        verify(repository, never()).updateById(any(GatewayEntity.class));
    }

    @Test
    void testUpdate_SpaceNotFound() {
        // 准备数据
        updateDto.setId(1);
        updateDto.setSpaceId(999); // 不存在的空间ID

        GatewayEntity oldEntity = new GatewayEntity();
        oldEntity.setId(1).setIotId("12345");

        GatewayEntity updateEntity = new GatewayEntity();
        updateEntity.setModelId(200).setSpaceId(999).setDeviceNo("SN123456");

        // Mock行为
        when(repository.selectById(1)).thenReturn(oldEntity);
        when(mapper.updateDtoToEntity(updateDto)).thenReturn(updateEntity);
        when(deviceModelService.getDetail(200)).thenReturn(deviceModelBo);
        when(spaceService.getDetail(999)).thenReturn(null);

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> gatewayService.update(updateDto));

        assertEquals("空间信息不存在，请重新选择", exception.getMessage());
        verify(repository).selectById(1);
        verify(mapper).updateDtoToEntity(updateDto);
        verify(deviceModelService).getDetail(200);
        verify(spaceService).getDetail(999);
        verify(repository, never()).updateById(any(GatewayEntity.class));
    }

    @Test
    void testUpdate_NbModeWithoutImei() {
        // 准备数据
        updateDto.setId(1);
        updateDto.setImei(""); // NB模式下IMEI为空

        GatewayEntity oldEntity = new GatewayEntity();
        oldEntity.setId(1).setIotId("12345");

        GatewayEntity updateEntity = new GatewayEntity();
        updateEntity.setModelId(200)
                .setSpaceId(100)
                .setCommunicateModel("NB") // NB通讯模式
                .setImei("") // 空IMEI
                .setDeviceNo("SN123456");

        // 修改设备型号为NB模式
        Map<String, Object> nbModelProperty = Map.of("communicateModel", "NB");
        DeviceModelBo nbDeviceModelBo = new DeviceModelBo()
                .setId(200)
                .setManufacturerName("厂商")
                .setProductCode("gateway-nb-update-200")
                .setTypeKey("gateway")
                .setModelProperty(nbModelProperty);

        // Mock行为
        when(repository.selectById(1)).thenReturn(oldEntity);
        when(mapper.updateDtoToEntity(updateDto)).thenReturn(updateEntity);
        when(deviceModelService.getDetail(200)).thenReturn(nbDeviceModelBo);

        // 执行测试并验证异常
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> gatewayService.update(updateDto));

        assertEquals("NB模式网关IMEI不能为空", exception.getMessage());
        verify(repository).selectById(1);
        verify(mapper).updateDtoToEntity(updateDto);
        verify(deviceModelService).getDetail(200);
        verify(repository, never()).updateById(any(GatewayEntity.class));
    }

    @Test
    void testUpdate_EmptyConfigInfo() {
        // 准备数据
        updateDto.setId(1);
        updateDto.setConfigInfo(""); // 空配置信息

        GatewayEntity oldEntity = new GatewayEntity();
        oldEntity.setId(1).setDeviceNo("SN123456").setIotId("12345").setOwnAreaId(1000);

        GatewayEntity updateEntity = new GatewayEntity();
        updateEntity.setId(1)
                .setDeviceNo("SN123456")
                .setModelId(200)
                .setSpaceId(100)
                .setCommunicateModel("485")
                .setConfigInfo("")
                .setOwnAreaId(1000);

        // Mock行为
        when(repository.selectById(1)).thenReturn(oldEntity);
        when(mapper.updateDtoToEntity(updateDto)).thenReturn(updateEntity);
        when(deviceModelService.getDetail(200)).thenReturn(deviceModelBo);
        when(spaceService.getDetail(100)).thenReturn(spaceBo);

        // 执行测试并验证异常
        assertDoesNotThrow(() -> gatewayService.update(updateDto));

        verify(repository).selectById(1);
        verify(mapper).updateDtoToEntity(updateDto);
        verify(deviceModelService).getDetail(200);
        verify(spaceService).getDetail(100);
        verify(repository).updateById(any(GatewayEntity.class));
    }

    @Test
    void testUpdate_CreateNewIotDevice() {
        // 准备数据 - 测试当旧实体没有iotId时创建新设备的场景
        updateDto.setId(1);

        GatewayEntity oldEntity = new GatewayEntity();
        oldEntity.setId(1)
                .setGatewayNo("GW202401010001")
                .setDeviceNo("SN123456")
                .setIotId(null) // 没有iotId
                .setOwnAreaId(1000);

        GatewayEntity updateEntity = new GatewayEntity();
        updateEntity.setId(1)
                .setSpaceId(100)
                .setGatewayName("更新后的网关")
                .setDeviceNo("SN123456")
                .setModelId(200)
                .setProductCode("厂商-型号")
                .setCommunicateModel("485")
                .setSn("SN123456")
                .setImei("")
                .setConfigInfo("{\"productCode\":\"TEST_PRODUCT\",\"data\":\"test_data\"}")
                .setRemark("更新后的备注")
                .setOwnAreaId(1000);

        // Mock行为
        when(repository.selectById(1)).thenReturn(oldEntity);
        when(mapper.updateDtoToEntity(updateDto)).thenReturn(updateEntity);
        when(deviceModelService.getDetail(200)).thenReturn(deviceModelBo);
        when(spaceService.getDetail(100)).thenReturn(spaceBo);

        // 执行测试
        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,() -> gatewayService.update(updateDto));
        assertEquals("数据异常：网关没有对应的iot数据", exception.getMessage());

        // 验证方法调用
        verify(repository).selectById(1);
        verify(mapper).updateDtoToEntity(updateDto);
        verify(deviceModelService).getDetail(200);
        verify(spaceService).getDetail(100);
    }

    @Test
    void testSyncGatewayOnlineStatus_ForceUpdate() {
        GatewayEntity oldEntity = new GatewayEntity()
                .setId(1)
                .setIotId("123")
                .setGatewayNo("GW202401010001");
        oldEntity.setOwnAreaId(1000);

        when(repository.selectById(1)).thenReturn(oldEntity);

        GatewayOnlineStatusDto dto = new GatewayOnlineStatusDto()
                .setGatewayId(1)
                .setForce(true)
                .setOnlineStatus(Boolean.FALSE);

        gatewayService.syncGatewayOnlineStatus(dto);

        verify(repository).selectById(1);
        verify(energyService, never()).isOnline(any(BaseElectricDeviceDto.class));
        ArgumentCaptor<GatewayEntity> captor = ArgumentCaptor.forClass(GatewayEntity.class);
        verify(repository).updateById(captor.capture());
        GatewayEntity updated = captor.getValue();
        assertEquals(1, updated.getId());
        assertEquals(Boolean.FALSE, updated.getIsOnline());
    }

    @Test
    void testSyncGatewayOnlineStatus_FetchFromIot() {
        GatewayEntity oldEntity = new GatewayEntity()
                .setId(1)
                .setIotId("321")
                .setGatewayNo("GW202401010001");
        oldEntity.setOwnAreaId(1000);

        when(repository.selectById(1)).thenReturn(oldEntity);
        when(deviceModuleContext.getService(EnergyService.class, 1000)).thenReturn(energyService);
        when(energyService.isOnline(any(BaseElectricDeviceDto.class))).thenReturn(Boolean.TRUE);

        GatewayOnlineStatusDto dto = new GatewayOnlineStatusDto()
                .setGatewayId(1);

        gatewayService.syncGatewayOnlineStatus(dto);

        verify(repository).selectById(1);
        verify(deviceModuleContext).getService(EnergyService.class, 1000);
        ArgumentCaptor<BaseElectricDeviceDto> baseDtoCaptor = ArgumentCaptor.forClass(BaseElectricDeviceDto.class);
        verify(energyService).isOnline(baseDtoCaptor.capture());
        BaseElectricDeviceDto baseDto = baseDtoCaptor.getValue();
        assertEquals("321", baseDto.getDeviceId());
        assertEquals(1000, baseDto.getAreaId());

        ArgumentCaptor<GatewayEntity> captor = ArgumentCaptor.forClass(GatewayEntity.class);
        verify(repository).updateById(captor.capture());
        assertEquals(Boolean.TRUE, captor.getValue().getIsOnline());
    }

    @Test
    void testSyncGatewayOnlineStatus_GatewayNotFound() {
        when(repository.selectById(1)).thenReturn(null);

        GatewayOnlineStatusDto dto = new GatewayOnlineStatusDto().setGatewayId(1);

        assertThrows(NotFoundException.class, () -> gatewayService.syncGatewayOnlineStatus(dto));
        verify(repository).selectById(1);
        verifyNoMoreInteractions(repository);
        verifyNoInteractions(deviceModuleContext, energyService);
    }

}
