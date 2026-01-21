package info.zhihui.ems.web.organization;

import com.fasterxml.jackson.databind.ObjectMapper;
import info.zhihui.ems.config.satoken.SaWebConfig;
import info.zhihui.ems.web.organization.biz.OrganizationBiz;
import info.zhihui.ems.web.organization.controller.OrganizationController;
import info.zhihui.ems.web.organization.vo.OrganizationCreateVo;
import info.zhihui.ems.web.organization.vo.OrganizationUpdateVo;
import info.zhihui.ems.web.organization.vo.OrganizationVo;
import info.zhihui.ems.common.paging.PageResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrganizationController.class)
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
                .setManagerName("张三");

        PageResult<OrganizationVo> pageResult = new PageResult<OrganizationVo>()
                .setList(List.of(vo))
                .setPageNum(1)
                .setPageSize(10)
                .setTotal(1L);

        when(organizationBiz.findOrganizationPage(any(), eq(1), eq(10))).thenReturn(pageResult);

        mockMvc.perform(get("/organizations/page").param("pageNum", "1").param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].organizationName").value("测试组织"));
    }

    @Test
    @DisplayName("查询组织列表")
    void testFindOrganizationList() throws Exception {
        OrganizationVo vo = new OrganizationVo()
                .setId(1)
                .setOrganizationName("测试组织");
        when(organizationBiz.findOrganizationList(any())).thenReturn(List.of(vo));

        mockMvc.perform(get("/organizations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].organizationName").value("测试组织"));
    }

    @Test
    @DisplayName("获取组织详情")
    void testGetOrganization() throws Exception {
        OrganizationVo vo = new OrganizationVo()
                .setId(2)
                .setOrganizationName("详情组织")
                .setOrganizationType(1);
        when(organizationBiz.getOrganization(2)).thenReturn(vo);

        mockMvc.perform(get("/organizations/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(2))
                .andExpect(jsonPath("$.data.organizationName").value("详情组织"));
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

        mockMvc.perform(post("/organizations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createVo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(99));
    }

    @Test
    @DisplayName("更新组织")
    void testUpdateOrganization() throws Exception {
        OrganizationUpdateVo updateVo = new OrganizationUpdateVo()
                .setOrganizationName("更新组织")
                .setEntryDate(LocalDate.of(2024, 8, 30))
                .setRemark("更新备注");

        doNothing().when(organizationBiz).updateOrganization(eq(5), any(OrganizationUpdateVo.class));

        mockMvc.perform(put("/organizations/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateVo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("删除组织")
    void testDeleteOrganization() throws Exception {
        doNothing().when(organizationBiz).deleteOrganization(10);

        mockMvc.perform(delete("/organizations/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
