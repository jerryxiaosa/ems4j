package info.zhihui.ems.web.system;

import com.fasterxml.jackson.databind.ObjectMapper;
import info.zhihui.ems.config.satoken.SaWebConfig;
import info.zhihui.ems.web.system.biz.ConfigBiz;
import info.zhihui.ems.web.system.controller.ConfigController;
import info.zhihui.ems.web.system.vo.ConfigUpdateVo;
import info.zhihui.ems.web.system.vo.ConfigVo;
import info.zhihui.ems.common.paging.PageResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ConfigController.class)
class ConfigControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ConfigBiz configBiz;

    @MockitoBean
    private SaWebConfig saWebConfig;

    @Test
    @DisplayName("分页查询配置")
    void testFindConfigPage() throws Exception {
        ConfigVo vo = new ConfigVo()
                .setId(1)
                .setConfigModuleName("system")
                .setConfigKey("key1")
                .setConfigName("配置1")
                .setConfigValue("value");
        PageResult<ConfigVo> pageResult = new PageResult<ConfigVo>()
                .setList(List.of(vo))
                .setPageNum(1)
                .setPageSize(10)
                .setTotal(1L);
        when(configBiz.findConfigPage(any(), eq(1), eq(10))).thenReturn(pageResult);

        mockMvc.perform(get("/system/configs/page").param("pageNum", "1").param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].configKey").value("key1"));
    }

    @Test
    @DisplayName("查询配置列表")
    void testFindConfigList() throws Exception {
        ConfigVo vo = new ConfigVo().setId(2).setConfigKey("key2");
        when(configBiz.findConfigList(any())).thenReturn(List.of(vo));

        mockMvc.perform(get("/system/configs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].configKey").value("key2"));
    }

    @Test
    @DisplayName("获取配置详情")
    void testGetConfig() throws Exception {
        ConfigVo vo = new ConfigVo().setId(3).setConfigKey("key3");
        when(configBiz.getConfig("key3")).thenReturn(vo);

        mockMvc.perform(get("/system/configs/key3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.configKey").value("key3"));
    }

    @Test
    @DisplayName("更新配置")
    void testUpdateConfig() throws Exception {
        ConfigUpdateVo updateVo = new ConfigUpdateVo()
                .setConfigKey("key1")
                .setConfigValue("value-updated");

        doNothing().when(configBiz).updateConfig(any(ConfigUpdateVo.class));

        mockMvc.perform(put("/system/configs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateVo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
