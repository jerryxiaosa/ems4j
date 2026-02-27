package info.zhihui.ems.web.owner;

import info.zhihui.ems.config.satoken.SaWebConfig;
import info.zhihui.ems.web.owner.biz.OwnerCandidateMeterBiz;
import info.zhihui.ems.web.owner.controller.OwnerCandidateMeterController;
import info.zhihui.ems.web.owner.vo.OwnerCandidateMeterQueryVo;
import info.zhihui.ems.web.owner.vo.OwnerCandidateMeterVo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OwnerCandidateMeterController.class)
class OwnerCandidateMeterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OwnerCandidateMeterBiz ownerCandidateMeterBiz;

    @MockitoBean
    private SaWebConfig saWebConfig;

    @Test
    @DisplayName("查询主体候选电表列表应绑定参数并返回结果")
    void testFindCandidateMeterList_Success() throws Exception {
        when(ownerCandidateMeterBiz.findCandidateMeterList(any())).thenReturn(List.of(
                new OwnerCandidateMeterVo()
                        .setId(1)
                        .setMeterName("候选表")
                        .setMeterNo("EM001")
                        .setMeterType("电表")
        ));

        mockMvc.perform(get("/owner-candidate-meters")
                        .param("ownerType", "0")
                        .param("ownerId", "1001")
                        .param("spaceNameLike", "一层"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].meterName").value("候选表"))
                .andExpect(jsonPath("$.data[0].meterType").value("电表"));

        ArgumentCaptor<OwnerCandidateMeterQueryVo> captor = ArgumentCaptor.forClass(OwnerCandidateMeterQueryVo.class);
        verify(ownerCandidateMeterBiz).findCandidateMeterList(captor.capture());
        assertEquals(0, captor.getValue().getOwnerType());
        assertEquals(1001, captor.getValue().getOwnerId());
        assertEquals("一层", captor.getValue().getSpaceNameLike());
    }
}
