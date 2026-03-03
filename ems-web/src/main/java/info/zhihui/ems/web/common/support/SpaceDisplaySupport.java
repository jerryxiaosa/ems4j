package info.zhihui.ems.web.common.support;

import info.zhihui.ems.foundation.space.bo.SpaceBo;
import info.zhihui.ems.foundation.space.dto.SpaceQueryDto;
import info.zhihui.ems.foundation.space.service.SpaceService;
import info.zhihui.ems.web.common.dto.SpaceDisplayDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 空间展示信息支持组件
 */
@Component
@RequiredArgsConstructor
public class SpaceDisplaySupport {

    private final SpaceService spaceService;

    /**
     * 查询空间展示信息映射。
     *
     * @param spaceIdCollection 空间ID集合
     * @return 空间ID到展示信息的映射
     */
    public Map<Integer, SpaceDisplayDto> findSpaceDisplayMap(Collection<Integer> spaceIdCollection) {
        if (spaceIdCollection == null || spaceIdCollection.isEmpty()) {
            return Collections.emptyMap();
        }

        Set<Integer> spaceIdSet = spaceIdCollection.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (spaceIdSet.isEmpty()) {
            return Collections.emptyMap();
        }

        return spaceService.findSpaceList(new SpaceQueryDto().setIds(spaceIdSet)).stream()
                .filter(Objects::nonNull)
                .filter(spaceBo -> spaceBo.getId() != null)
                .collect(Collectors.toMap(
                        SpaceBo::getId,
                        this::toSpaceDisplayDto,
                        (left, right) -> left
                ));
    }

    private SpaceDisplayDto toSpaceDisplayDto(SpaceBo spaceBo) {
        return new SpaceDisplayDto()
                .setId(spaceBo.getId())
                .setName(spaceBo.getName())
                .setParentsNames(spaceBo.getParentsNames());
    }
}
