package info.zhihui.ems.web.system;

import info.zhihui.ems.config.satoken.SaWebConfig;
import info.zhihui.ems.web.system.biz.ApiOptionBiz;
import info.zhihui.ems.web.system.controller.ApiOptionController;
import info.zhihui.ems.web.system.vo.ApiOptionVo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApiOptionController.class)
class ApiOptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ApiOptionBiz apiOptionBiz;

    @MockitoBean
    private SaWebConfig saWebConfig;

    @Test
    @DisplayName("查询系统接口选项")
    void testFindApiOptionList() throws Exception {
        when(apiOptionBiz.findApiOptionList()).thenReturn(List.of(
                new ApiOptionVo()
                        .setKey("GET:/v1/users/page")
                        .setPermissionCode("users:users:page")
        ));

        mockMvc.perform(get("/v1/system/api-options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].key").value("GET:/v1/users/page"))
                .andExpect(jsonPath("$.data[0].permissionCode").value("users:users:page"));
    }
}
