package info.zhihui.ems.foundation.system.service.impl;

import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.foundation.system.dto.ConfigUpdateDto;
import info.zhihui.ems.foundation.system.entity.ConfigEntity;
import info.zhihui.ems.foundation.system.mapper.ConfigMapper;
import info.zhihui.ems.foundation.system.repository.ConfigRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("系统配置服务测试")
class ConfigServiceImplTest {

    @Mock
    private ConfigRepository repository;

    @Mock
    private ConfigMapper mapper;

    @InjectMocks
    private ConfigServiceImpl configService;

    @Test
    @DisplayName("更新配置-非内置配置应拒绝")
    void testUpdate_NonSystem_ShouldThrow() {
        ConfigUpdateDto updateDto = new ConfigUpdateDto().setConfigKey("test_key");
        ConfigEntity old = new ConfigEntity().setId(1).setIsSystem(false);
        when(repository.getByKey("test_key")).thenReturn(old);

        assertThrows(BusinessRuntimeException.class, () -> configService.update(updateDto));

        verify(mapper, never()).updateBoToEntity(any(ConfigUpdateDto.class));
        verify(repository, never()).updateById(any(ConfigEntity.class));
    }

    @Test
    @DisplayName("更新配置-内置配置允许更新并保留ID")
    void testUpdate_System_ShouldUpdate() {
        ConfigUpdateDto updateDto = new ConfigUpdateDto().setConfigKey("test_key");
        ConfigEntity old = new ConfigEntity().setId(10).setIsSystem(true);
        ConfigEntity mapped = new ConfigEntity();
        when(repository.getByKey("test_key")).thenReturn(old);
        when(mapper.updateBoToEntity(updateDto)).thenReturn(mapped);

        configService.update(updateDto);

        ArgumentCaptor<ConfigEntity> captor = ArgumentCaptor.forClass(ConfigEntity.class);
        verify(repository).updateById(captor.capture());
        assertEquals(10, captor.getValue().getId());
    }

    @Test
    @DisplayName("获取配置值-反序列化异常应包含key")
    void testGetValueByKey_DeserializeError_ShouldContainKey() {
        ConfigEntity entity = new ConfigEntity()
                .setConfigKey("bad_key")
                .setConfigValue("not-json");
        when(repository.getByKey("bad_key")).thenReturn(entity);

        BusinessRuntimeException exception = assertThrows(BusinessRuntimeException.class,
                () -> configService.getValueByKey("bad_key", new com.fasterxml.jackson.core.type.TypeReference<Integer>() {}));
        assertTrue(exception.getMessage().contains("bad_key"));
    }
}
