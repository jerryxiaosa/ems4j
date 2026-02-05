package info.zhihui.ems.foundation.integration.core.service;

import com.fasterxml.jackson.core.type.TypeReference;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.common.utils.JacksonUtil;
import info.zhihui.ems.foundation.integration.core.bo.DeviceModuleAreaConfigBo;
import info.zhihui.ems.foundation.integration.core.bo.DeviceModuleConfigBo;
import info.zhihui.ems.foundation.integration.core.service.impl.DeviceModuleConfigServiceImpl;
import info.zhihui.ems.foundation.integration.core.service.testdata.TestEnergy3Service;
import info.zhihui.ems.foundation.integration.core.service.testdata.TestEnergy2Service;
import info.zhihui.ems.foundation.integration.core.service.testdata.TestConfig;
import info.zhihui.ems.foundation.integration.core.service.testdata.TestEnergy1;
import info.zhihui.ems.foundation.system.bo.ConfigBo;
import info.zhihui.ems.foundation.system.constant.SystemConfigConstant;
import info.zhihui.ems.foundation.system.dto.ConfigUpdateDto;
import info.zhihui.ems.foundation.system.service.ConfigService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
@Slf4j
public class DeviceModuleConfigServiceTest {
    @InjectMocks
    private DeviceModuleConfigServiceImpl deviceModuleConfigServiceImpl;

    @Mock
    private ConfigService configService;

    @Test
    public void testGetDeviceConfigByModule() {
        String value1 = """
                {"key":"some value of config string"}
                """;
        String value2 = """
                {"key":"some value of config string"}
                """;
        DeviceModuleConfigBo deviceModuleConfigBo1 = new DeviceModuleConfigBo()
                .setModuleServiceName(TestEnergy2Service.class.getSimpleName())
                .setImplName("testEnergy2ServiceImpl")
                .setConfigValue(value1);
        DeviceModuleConfigBo deviceModuleConfigBo2 = new DeviceModuleConfigBo()
                .setModuleServiceName(TestEnergy3Service.class.getSimpleName())
                .setImplName("testEnergy3ServiceImpl")
                .setConfigValue(value2);
        List<DeviceModuleConfigBo> deviceModuleConfigBoList = new ArrayList<>();
        deviceModuleConfigBoList.add(deviceModuleConfigBo1);
        deviceModuleConfigBoList.add(deviceModuleConfigBo2);

        List<DeviceModuleAreaConfigBo> deviceModuleAreaConfigBoList = new ArrayList<>();
        DeviceModuleAreaConfigBo deviceModuleAreaConfigBo1 = new DeviceModuleAreaConfigBo().setAreaId(1);
        DeviceModuleAreaConfigBo deviceModuleAreaConfigBo2 = new DeviceModuleAreaConfigBo().setAreaId(2);
        DeviceModuleAreaConfigBo deviceModuleAreaConfigBo = new DeviceModuleAreaConfigBo()
                .setAreaId(3)
                .setDeviceConfigList(deviceModuleConfigBoList);
        deviceModuleAreaConfigBoList.add(deviceModuleAreaConfigBo1);
        deviceModuleAreaConfigBoList.add(deviceModuleAreaConfigBo2);
        deviceModuleAreaConfigBoList.add(deviceModuleAreaConfigBo);
        log.info(JacksonUtil.toJson(deviceModuleAreaConfigBoList));

        String configValue = """
                [{"areaId":1,"deviceConfigList":null},{"areaId":2,"deviceConfigList":null},{"areaId":3,"deviceConfigList":[{"moduleServiceName":"TestEnergy2Service","implName":"testEnergy2ServiceImpl","configValue":{"key":"some value of config string"}
                },{"moduleServiceName":"TestCameraService","implName":"testCameraServiceImpl","configValue":{"key":"some value of config string"}
                }]}]
                """;
        ConfigBo mockSystemConfig = new ConfigBo().setConfigValue(configValue);
        Mockito.when(configService.getByKey(info.zhihui.ems.foundation.system.constant.SystemConfigConstant.DEVICE_CONFIG)).thenReturn(mockSystemConfig);

        DeviceModuleConfigBo res = deviceModuleConfigServiceImpl.getDeviceConfigByModule(TestEnergy2Service.class, 3);
        DeviceModuleConfigBo except = new DeviceModuleConfigBo().setModuleServiceName("TestEnergy2Service").setImplName("testEnergy2ServiceImpl").setConfigValue("{\"key\":\"some value of config string\"}");
        Assertions.assertEquals(except, res);

        Assertions.assertThrows(NotFoundException.class, () -> {
            deviceModuleConfigServiceImpl.getDeviceConfigByModule(TestEnergy1.class, 3);
        });
    }

    @Test
    public void testGetDeviceConfigByModuleWithDuplicateAreaId() {
        String configValue = """
                [{"areaId":1,"deviceConfigList":[{"moduleServiceName":"TestEnergy2Service","implName":"impl-old","configValue":{"key":"old"}}]},
                {"areaId":1,"deviceConfigList":[{"moduleServiceName":"TestEnergy2Service","implName":"impl-new","configValue":{"key":"new"}}]}]
                """;
        ConfigBo mockSystemConfig = new ConfigBo().setConfigValue(configValue);
        Mockito.when(configService.getByKey(SystemConfigConstant.DEVICE_CONFIG)).thenReturn(mockSystemConfig);

        DeviceModuleConfigBo result = deviceModuleConfigServiceImpl.getDeviceConfigByModule(TestEnergy2Service.class, 1);
        DeviceModuleConfigBo expected = new DeviceModuleConfigBo()
                .setModuleServiceName("TestEnergy2Service")
                .setImplName("impl-new")
                .setConfigValue("{\"key\":\"new\"}");
        Assertions.assertEquals(expected, result);
    }

    @Test
    public void testGetEquipmentConfigValue() {
        String mockConfigValue = "[{\"areaId\":1,\"deviceConfigList\":[{\"moduleServiceName\":\"TestEnergy3Service\",\"implName\":\"testEnergy3ServiceImpl\",\"configValue\":{\"somevalue\":\"aaa-bbb-ccc\"}}]}]";
        List<DeviceModuleConfigBo> deviceModuleConfigBoList = new ArrayList<>();
        deviceModuleConfigBoList.add(new DeviceModuleConfigBo().setModuleServiceName(TestEnergy3Service.class.getSimpleName()).setImplName("testCameraServiceImpl").setConfigValue("{\"somevalue\":\"aaa-bbb-ccc\"}"));

        String mockConfig = JacksonUtil.toJson(deviceModuleConfigBoList);
        log.info(mockConfig);

        ConfigBo mockSystemConfig = new ConfigBo().setConfigValue(mockConfigValue);
        Mockito.when(configService.getByKey(info.zhihui.ems.foundation.system.constant.SystemConfigConstant.DEVICE_CONFIG)).thenReturn(mockSystemConfig);

        TestConfig except = new TestConfig();
        except.setSomevalue("aaa-bbb-ccc");
        TestConfig res = deviceModuleConfigServiceImpl.getDeviceConfigValue(TestEnergy3Service.class, TestConfig.class, 1);
        Assertions.assertEquals(except, res);
    }

    @Test
    public void testAreaDoesNotExist() {
        String configValue = """
                [{"areaId":1,"deviceConfigList":[{"moduleServiceName":"TestCameraService","implName":"testCameraServiceImpl","configValue":{"somevalue":"test-value"}}]}]
                """;
        ConfigBo mockSystemConfig = new ConfigBo().setConfigValue(configValue);
        Mockito.when(configService.getByKey(info.zhihui.ems.foundation.system.constant.SystemConfigConstant.DEVICE_CONFIG)).thenReturn(mockSystemConfig);

        // Test with area ID that doesn't exist in config
        Assertions.assertThrows(NotFoundException.class, () -> {
            deviceModuleConfigServiceImpl.getDeviceConfigByModule(TestEnergy3Service.class, 999);
        });
    }

    @Test
    public void testDeviceConfigListIsNull() {
        String configValue = """
                [{"areaId":1,"deviceConfigList":null}]
                """;
        ConfigBo mockSystemConfig = new ConfigBo().setConfigValue(configValue);
        Mockito.when(configService.getByKey(info.zhihui.ems.foundation.system.constant.SystemConfigConstant.DEVICE_CONFIG)).thenReturn(mockSystemConfig);

        // Test with area that has null deviceConfigList
        Assertions.assertThrows(NotFoundException.class, () -> {
            deviceModuleConfigServiceImpl.getDeviceConfigByModule(TestEnergy3Service.class, 1);
        });
    }

    @Test
    public void testGetDeviceConfigValueWithInvalidJson() {
        String configValue = """
                [{"areaId":1,"deviceConfigList":[{"moduleServiceName":"TestEnergy3Service","implName":"testEnergy3ServiceImpl","configValue":"invalid-json"}]}]
                """;
        ConfigBo mockSystemConfig = new ConfigBo().setConfigValue(configValue);
        Mockito.when(configService.getByKey(info.zhihui.ems.foundation.system.constant.SystemConfigConstant.DEVICE_CONFIG)).thenReturn(mockSystemConfig);

        // Test with invalid JSON in configValue
        Assertions.assertThrows(BusinessRuntimeException.class, () -> {
            deviceModuleConfigServiceImpl.getDeviceConfigValue(TestEnergy3Service.class, TestConfig.class, 1);
        });
    }

    @Test
    public void testGetAllDeviceConfigStringWithBlankConfig() {
        ConfigBo mockSystemConfig = new ConfigBo().setConfigValue("");
        Mockito.when(configService.getByKey(info.zhihui.ems.foundation.system.constant.SystemConfigConstant.DEVICE_CONFIG)).thenReturn(mockSystemConfig);

        // Test with blank config value
        Assertions.assertThrows(NotFoundException.class, () -> {
            deviceModuleConfigServiceImpl.getDeviceConfigByModule(TestEnergy3Service.class, 1);
        });
    }

    @Test
    public void testGetAllDeviceConfigStringWithNullConfig() {
        ConfigBo mockSystemConfig = new ConfigBo().setConfigValue(null);
        Mockito.when(configService.getByKey(info.zhihui.ems.foundation.system.constant.SystemConfigConstant.DEVICE_CONFIG)).thenReturn(mockSystemConfig);

        // Test with null config value
        Assertions.assertThrows(NotFoundException.class, () -> {
            deviceModuleConfigServiceImpl.getDeviceConfigByModule(TestEnergy3Service.class, 1);
        });
    }

    @Test
    public void testSetDeviceConfigByArea() {
        // 准备测试数据
        Integer areaId = 3;
        String configValue1 = "{\"host\":\"test-host-1\",\"port\":8080}";
        String configValue2 = "{\"apiKey\":\"test-api-key\",\"secret\":\"test-secret\"}";

        DeviceModuleConfigBo deviceModuleConfigBo1 = new DeviceModuleConfigBo()
                .setModuleServiceName(TestEnergy2Service.class.getSimpleName())
                .setImplName("testCarServiceImpl")
                .setConfigValue(configValue1);

        DeviceModuleConfigBo deviceModuleConfigBo2 = new DeviceModuleConfigBo()
                .setModuleServiceName(TestEnergy3Service.class.getSimpleName())
                .setImplName("testCameraServiceImpl")
                .setConfigValue(configValue2);

        List<DeviceModuleConfigBo> newDeviceConfigList = new ArrayList<>();
        newDeviceConfigList.add(deviceModuleConfigBo1);
        newDeviceConfigList.add(deviceModuleConfigBo2);

        // 模拟现有配置数据
        String existingConfigJson = """
                [{"areaId":1,"deviceConfigList":[{"moduleServiceName":"TestCameraService","implName":"testCameraServiceImpl","configValue":{"somevalue":"test-value"}}]},
                {"areaId":2,"deviceConfigList":[{"moduleServiceName":"TestCarService","implName":"testCarServiceImpl","configValue":{"somevalue":"test-value"}}]}]
                """;
        ConfigBo mockSystemConfig = new ConfigBo().setConfigValue(existingConfigJson);
        Mockito.when(configService.getByKey(info.zhihui.ems.foundation.system.constant.SystemConfigConstant.DEVICE_CONFIG)).thenReturn(mockSystemConfig);

        // 执行被测试方法
        deviceModuleConfigServiceImpl.setDeviceConfigByArea(newDeviceConfigList, areaId);

        // 验证结果
        ArgumentCaptor<ConfigUpdateDto> configUpdateBoCaptor = ArgumentCaptor.forClass(ConfigUpdateDto.class);
        Mockito.verify(configService).update(configUpdateBoCaptor.capture());

        ConfigUpdateDto capturedUpdateBo = configUpdateBoCaptor.getValue();
        Assertions.assertEquals(info.zhihui.ems.foundation.system.constant.SystemConfigConstant.DEVICE_CONFIG, capturedUpdateBo.getConfigKey());

        // 解析更新后的配置JSON
        String updatedConfigJson = capturedUpdateBo.getConfigValue();
        List<DeviceModuleAreaConfigBo> updatedAreaConfigs = JacksonUtil.fromJson(updatedConfigJson,
                new TypeReference<>() {
                });

        // 验证更新后的配置列表包含三个区域
        Assertions.assertEquals(3, updatedAreaConfigs.size());

        // 获取更新后的区域配置
        DeviceModuleAreaConfigBo updatedAreaConfig = updatedAreaConfigs.stream()
                .filter(config -> areaId.equals(config.getAreaId()))
                .findFirst()
                .orElse(null);

        // 验证新增的区域ID
        Assertions.assertNotNull(updatedAreaConfig);
        Assertions.assertEquals(areaId, updatedAreaConfig.getAreaId());

        // 验证新区域的配置列表内容
        List<DeviceModuleConfigBo> updatedDeviceConfigList = updatedAreaConfig.getDeviceConfigList();
        Assertions.assertEquals(2, updatedDeviceConfigList.size());

        // 验证Car服务配置
        DeviceModuleConfigBo updatedCarConfig = updatedDeviceConfigList.stream()
                .filter(config -> TestEnergy2Service.class.getSimpleName().equals(config.getModuleServiceName()))
                .findFirst()
                .orElse(null);
        Assertions.assertNotNull(updatedCarConfig);
        Assertions.assertEquals("testCarServiceImpl", updatedCarConfig.getImplName());

        // 验证Camera服务配置
        DeviceModuleConfigBo updatedCameraConfig = updatedDeviceConfigList.stream()
                .filter(config -> TestEnergy3Service.class.getSimpleName().equals(config.getModuleServiceName()))
                .findFirst()
                .orElse(null);
        Assertions.assertNotNull(updatedCameraConfig);
        Assertions.assertEquals("testCameraServiceImpl", updatedCameraConfig.getImplName());
    }

    @Test
    public void testSetDeviceConfigByAreaWithExistingArea() {
        // 准备测试数据
        Integer areaId = 1; // 使用已存在的区域ID
        String configValue = "{\"host\":\"updated-host\",\"port\":9090}";

        DeviceModuleConfigBo newDeviceConfigBo = new DeviceModuleConfigBo()
                .setModuleServiceName(TestEnergy2Service.class.getSimpleName())
                .setImplName("updatedCarServiceImpl")
                .setConfigValue(configValue);

        List<DeviceModuleConfigBo> newDeviceConfigList = new ArrayList<>();
        newDeviceConfigList.add(newDeviceConfigBo);

        // 模拟现有配置数据（区域1已存在）
        String existingConfigJson = """
                [{"areaId":1,"deviceConfigList":[{"moduleServiceName":"TestCameraService","implName":"testCameraServiceImpl","configValue":{"somevalue":"original-value"}}]},
                {"areaId":2,"deviceConfigList":[{"moduleServiceName":"TestCarService","implName":"testCarServiceImpl","configValue":{"somevalue":"test-value"}}]}]
                """;
        ConfigBo mockSystemConfig = new ConfigBo().setConfigValue(existingConfigJson);
        Mockito.when(configService.getByKey(info.zhihui.ems.foundation.system.constant.SystemConfigConstant.DEVICE_CONFIG)).thenReturn(mockSystemConfig);

        // 执行被测试方法
        deviceModuleConfigServiceImpl.setDeviceConfigByArea(newDeviceConfigList, areaId);

        // 验证结果
        ArgumentCaptor<ConfigUpdateDto> configUpdateBoCaptor = ArgumentCaptor.forClass(ConfigUpdateDto.class);
        Mockito.verify(configService).update(configUpdateBoCaptor.capture());

        ConfigUpdateDto capturedUpdateBo = configUpdateBoCaptor.getValue();
        Assertions.assertEquals(SystemConfigConstant.DEVICE_CONFIG, capturedUpdateBo.getConfigKey());

        // 解析更新后的配置JSON
        String updatedConfigJson = capturedUpdateBo.getConfigValue();
        List<DeviceModuleAreaConfigBo> updatedAreaConfigs = JacksonUtil.fromJson(updatedConfigJson,
                new TypeReference<>() {
                });

        // 验证仍然有两个区域
        Assertions.assertEquals(2, updatedAreaConfigs.size());

        // 获取更新后的区域配置
        DeviceModuleAreaConfigBo updatedAreaConfig = updatedAreaConfigs.stream()
                .filter(config -> areaId.equals(config.getAreaId()))
                .findFirst()
                .orElse(null);

        // 验证区域1的配置已被更新
        Assertions.assertNotNull(updatedAreaConfig);
        Assertions.assertEquals(areaId, updatedAreaConfig.getAreaId());

        // 验证区域1现在只有一个配置（TestCarService）
        List<DeviceModuleConfigBo> updatedDeviceConfigList = updatedAreaConfig.getDeviceConfigList();
        Assertions.assertEquals(1, updatedDeviceConfigList.size());

        // 验证Car服务配置已更新
        DeviceModuleConfigBo updatedCarConfig = updatedDeviceConfigList.get(0);
        Assertions.assertEquals(TestEnergy2Service.class.getSimpleName(), updatedCarConfig.getModuleServiceName());
        Assertions.assertEquals("updatedCarServiceImpl", updatedCarConfig.getImplName());
        Assertions.assertEquals(configValue, updatedCarConfig.getConfigValue());

        // 验证区域2配置保持不变
        DeviceModuleAreaConfigBo unchangedAreaConfig = updatedAreaConfigs.stream()
                .filter(config -> Integer.valueOf(2).equals(config.getAreaId()))
                .findFirst()
                .orElse(null);
        Assertions.assertNotNull(unchangedAreaConfig);
    }

    @Test
    public void testSetDeviceConfigByAreaWithDuplicateExistingArea() {
        Integer areaId = 3;
        DeviceModuleConfigBo newConfig = new DeviceModuleConfigBo()
                .setModuleServiceName(TestEnergy2Service.class.getSimpleName())
                .setImplName("testEnergy2ServiceImpl")
                .setConfigValue("{\"key\":\"value\"}");
        List<DeviceModuleConfigBo> newDeviceConfigList = new ArrayList<>();
        newDeviceConfigList.add(newConfig);

        String existingConfigJson = """
                [{"areaId":1,"deviceConfigList":[{"moduleServiceName":"TestEnergy2Service","implName":"impl-old","configValue":{"key":"old"}}]},
                {"areaId":1,"deviceConfigList":[{"moduleServiceName":"TestEnergy2Service","implName":"impl-new","configValue":{"key":"new"}}]},
                {"areaId":2,"deviceConfigList":[{"moduleServiceName":"TestEnergy3Service","implName":"impl-2","configValue":{"key":"v2"}}]}]
                """;
        ConfigBo mockSystemConfig = new ConfigBo().setConfigValue(existingConfigJson);
        Mockito.when(configService.getByKey(SystemConfigConstant.DEVICE_CONFIG)).thenReturn(mockSystemConfig);

        deviceModuleConfigServiceImpl.setDeviceConfigByArea(newDeviceConfigList, areaId);

        ArgumentCaptor<ConfigUpdateDto> captor = ArgumentCaptor.forClass(ConfigUpdateDto.class);
        Mockito.verify(configService).update(captor.capture());

        String updatedConfigJson = captor.getValue().getConfigValue();
        List<DeviceModuleAreaConfigBo> updatedAreaConfigs = JacksonUtil.fromJson(updatedConfigJson, new TypeReference<>() {
        });

        List<Integer> areaIds = updatedAreaConfigs.stream().map(DeviceModuleAreaConfigBo::getAreaId).toList();
        long distinctCount = areaIds.stream().distinct().count();
        Assertions.assertEquals(distinctCount, areaIds.size());
        Assertions.assertEquals(3, areaIds.size());
        Assertions.assertTrue(areaIds.containsAll(List.of(1, 2, 3)));
    }
}
