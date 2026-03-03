package info.zhihui.ems.web.common.support;

import info.zhihui.ems.foundation.space.bo.SpaceBo;
import info.zhihui.ems.foundation.space.service.SpaceService;
import info.zhihui.ems.web.common.dto.SpaceDisplayDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpaceDisplaySupportTest {

    @InjectMocks
    private SpaceDisplaySupport spaceDisplaySupport;

    @Mock
    private SpaceService spaceService;

    @Test
    @DisplayName("查询空间展示信息映射_空集合应返回空映射")
    void testFindSpaceDisplayMap_WithEmptyIds_ShouldReturnEmptyMap() {
        Map<Integer, SpaceDisplayDto> result = spaceDisplaySupport.findSpaceDisplayMap(Collections.emptyList());

        assertThat(result).isEmpty();
        verifyNoInteractions(spaceService);
    }

    @Test
    @DisplayName("查询空间展示信息映射_应过滤空值和重复值")
    void testFindSpaceDisplayMap_ShouldFilterNullAndDuplicateIds() {
        SpaceBo spaceBo = new SpaceBo()
                .setId(10)
                .setName("101房间")
                .setParentsNames(List.of("1号楼", "1层"));
        when(spaceService.findSpaceList(any())).thenReturn(List.of(spaceBo));

        Map<Integer, SpaceDisplayDto> result = spaceDisplaySupport.findSpaceDisplayMap(Arrays.asList(null, 10, 10));

        assertThat(result).hasSize(1);
        assertThat(result).containsKey(10);
        assertThat(result.get(10).getName()).isEqualTo("101房间");
        assertThat(result.get(10).getParentsNames()).containsExactly("1号楼", "1层");
    }
}
