package info.zhihui.ems.web.organization;

import com.fasterxml.jackson.databind.ObjectMapper;
import info.zhihui.ems.config.satoken.SaWebConfig;
import info.zhihui.ems.components.translate.engine.TranslateEngine;
import info.zhihui.ems.components.translate.engine.TranslateMetadataCache;
import info.zhihui.ems.components.translate.resolver.EnumLabelResolver;
import info.zhihui.ems.components.translate.web.advice.ResponseTranslateAdvice;
import info.zhihui.ems.web.organization.biz.OrganizationBiz;
import info.zhihui.ems.web.organization.controller.OrganizationController;
import info.zhihui.ems.web.organization.vo.OrganizationCreateVo;
import info.zhihui.ems.web.organization.vo.OrganizationOptionVo;
import info.zhihui.ems.web.organization.vo.OrganizationUpdateVo;
import info.zhihui.ems.web.organization.vo.OrganizationVo;
import info.zhihui.ems.common.paging.PageResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrganizationController.class)
@Import({
        ResponseTranslateAdvice.class,
        TranslateEngine.class,
        TranslateMetadataCache.class,
        EnumLabelResolver.class
})
class OrganizationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrganizationBiz organizationBiz;

    @MockitoBean
    private SaWebConfig saWebConfig;

    @Test
    @DisplayName("分页查询组织")
    void testFindOrganizationPage() throws Exception {
        OrganizationVo vo = new OrganizationVo()
                .setId(1)
                .setOrganizationName("测试组织")
                .setCreditCode("91330100MA123456")
                .setOrganizationType(1)
                .setEntryDate(LocalDate.of(2024, 1, 1))
                .setManagerName("张三")
                .setCreateTime(LocalDateTime.of(2024, 1, 2, 10, 20, 30))
                .setUpdateTime(LocalDateTime.of(2024, 1, 3, 11, 21, 31));

        PageResult<OrganizationVo> pageResult = new PageResult<OrganizationVo>()
                .setList(List.of(vo))
                .setPageNum(1)
                .setPageSize(10)
                .setTotal(1L);

        when(organizationBiz.findOrganizationPage(any(), eq(1), eq(10))).thenReturn(pageResult);

        mockMvc.perform(get("/v1/organizations/page").param("pageNum", "1").param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].organizationName").value("测试组织"))
                .andExpect(jsonPath("$.data.list[0].organizationTypeName").value("企业"))
                .andExpect(jsonPath("$.data.list[0].createTime").value("2024-01-02 10:20:30"))
                .andExpect(jsonPath("$.data.list[0].updateTime").value("2024-01-03 11:21:31"));
    }

    @Test
    @DisplayName("查询组织列表")
    void testFindOrganizationList() throws Exception {
        OrganizationVo vo = new OrganizationVo()
                .setId(1)
                .setOrganizationName("测试组织");
        when(organizationBiz.findOrganizationList(any())).thenReturn(List.of(vo));

        mockMvc.perform(get("/v1/organizations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].organizationName").value("测试组织"));
    }

    @Test
    @DisplayName("查询组织下拉列表-默认limit")
    void testFindOrganizationOptionList_DefaultLimit() throws Exception {
        OrganizationOptionVo vo = new OrganizationOptionVo()
                .setId(1)
                .setOrganizationName("组织A")
                .setOrganizationType(1)
                .setManagerName("张三")
                .setManagerPhone("13800000000");
        when(organizationBiz.findOrganizationOptionList(any())).thenReturn(List.of(vo));

        mockMvc.perform(get("/v1/organizations/options").param("organizationNameLike", "组织"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].organizationName").value("组织A"))
                .andExpect(jsonPath("$.data[0].organizationType").value(1))
                .andExpect(jsonPath("$.data[0].managerName").value("张三"))
                .andExpect(jsonPath("$.data[0].managerPhone").value("13800000000"));
    }

    @Test
    @DisplayName("查询组织下拉列表-自定义limit")
    void testFindOrganizationOptionList_CustomLimit() throws Exception {
        when(organizationBiz.findOrganizationOptionList(any())).thenReturn(List.of());

        mockMvc.perform(get("/v1/organizations/options")
                        .param("organizationNameLike", "测试")
                        .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("获取组织详情")
    void testGetOrganization() throws Exception {
        OrganizationVo vo = new OrganizationVo()
                .setId(2)
                .setOrganizationName("详情组织")
                .setOrganizationType(1)
                .setCreateTime(LocalDateTime.of(2024, 2, 2, 9, 8, 7))
                .setUpdateTime(LocalDateTime.of(2024, 2, 3, 6, 5, 4));
        when(organizationBiz.getOrganization(2)).thenReturn(vo);

        mockMvc.perform(get("/v1/organizations/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(2))
                .andExpect(jsonPath("$.data.organizationName").value("详情组织"))
                .andExpect(jsonPath("$.data.organizationTypeName").value("企业"))
                .andExpect(jsonPath("$.data.createTime").value("2024-02-02 09:08:07"))
                .andExpect(jsonPath("$.data.updateTime").value("2024-02-03 06:05:04"));
    }

    @Test
    @DisplayName("新增组织")
    void testCreateOrganization() throws Exception {
        when(organizationBiz.createOrganization(any(OrganizationCreateVo.class))).thenReturn(99);

        OrganizationCreateVo createVo = new OrganizationCreateVo()
                .setOrganizationName("新增组织")
                .setCreditCode("91330100MA000000")
                .setOrganizationType(1)
                .setManagerName("李四")
                .setManagerPhone("13800000000")
                .setEntryDate(LocalDate.of(2024, 8, 1))
                .setOwnAreaId(123)
                .setRemark("备注");

        mockMvc.perform(post("/v1/organizations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createVo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(99));
    }

    @Test
    @DisplayName("新增组织-ownAreaId可为空")
    void testCreateOrganization_OwnAreaIdCanBeNull() throws Exception {
        when(organizationBiz.createOrganization(any(OrganizationCreateVo.class))).thenReturn(100);

        OrganizationCreateVo createVo = new OrganizationCreateVo()
                .setOrganizationName("新增组织")
                .setCreditCode("91330100MA111111")
                .setOrganizationType(1)
                .setManagerName("李四")
                .setManagerPhone("13800000000")
                .setEntryDate(LocalDate.of(2024, 8, 1))
                .setRemark("备注");

        mockMvc.perform(post("/v1/organizations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createVo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(100));
    }

    @Test
    @DisplayName("更新组织")
    void testUpdateOrganization() throws Exception {
        OrganizationUpdateVo updateVo = new OrganizationUpdateVo()
                .setOrganizationName("更新组织")
                .setEntryDate(LocalDate.of(2024, 8, 30))
                .setRemark("更新备注");

        doNothing().when(organizationBiz).updateOrganization(eq(5), any(OrganizationUpdateVo.class));

        mockMvc.perform(put("/v1/organizations/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateVo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("删除组织")
    void testDeleteOrganization() throws Exception {
        doNothing().when(organizationBiz).deleteOrganization(10);

        mockMvc.perform(delete("/v1/organizations/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
