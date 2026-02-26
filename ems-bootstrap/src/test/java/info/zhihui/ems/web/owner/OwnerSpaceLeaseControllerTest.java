package info.zhihui.ems.web.owner;

import com.fasterxml.jackson.databind.ObjectMapper;
import info.zhihui.ems.config.satoken.SaWebConfig;
import info.zhihui.ems.web.owner.biz.OwnerSpaceLeaseBiz;
import info.zhihui.ems.web.owner.controller.OwnerSpaceLeaseController;
import info.zhihui.ems.web.owner.vo.OwnerSpaceRentVo;
import info.zhihui.ems.web.owner.vo.OwnerSpaceUnrentVo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OwnerSpaceLeaseController.class)
class OwnerSpaceLeaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OwnerSpaceLeaseBiz ownerSpaceLeaseBiz;

    @MockitoBean
    private SaWebConfig saWebConfig;

    @Test
    @DisplayName("主体租赁空间成功")
    void testRentSpaces_Success() throws Exception {
        OwnerSpaceRentVo rentVo = new OwnerSpaceRentVo()
                .setOwnerType(0)
                .setOwnerId(1001)
                .setSpaceIds(java.util.List.of(101, 102));

        mockMvc.perform(post("/owner-space-leases/rent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rentVo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(ownerSpaceLeaseBiz).rentSpaces(rentVo);
    }

    @Test
    @DisplayName("主体退租空间成功")
    void testUnrentSpaces_Success() throws Exception {
        OwnerSpaceUnrentVo unrentVo = new OwnerSpaceUnrentVo()
                .setOwnerType(1)
                .setOwnerId(1002)
                .setSpaceIds(java.util.List.of(101));

        mockMvc.perform(post("/owner-space-leases/unrent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(unrentVo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(ownerSpaceLeaseBiz).unrentSpaces(unrentVo);
    }
}
