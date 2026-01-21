package info.zhihui.ems.foundation.space.util;

import info.zhihui.ems.foundation.space.bo.SpaceBo;
import info.zhihui.ems.foundation.space.service.SpaceService;
import org.springframework.util.CollectionUtils;

import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 空间信息工具类：根据 spaceId 填充空间名称与父级谱系信息。
 */
public final class SpaceInfoUtils {
    private SpaceInfoUtils() {}

    /**
     * 根据 spaceId 填充空间名称与父级信息（ID/名称以逗号拼接）。
     * 为保证健壮性，内部对服务调用异常进行吞掉处理。
     */
    public static void fillFromSpaceId(Integer spaceId,
                                       SpaceService spaceService,
                                       Consumer<String> setName,
                                       Consumer<String> setParentIds,
                                       Consumer<String> setParentNames) {
        if (spaceId == null || spaceService == null) {
            return;
        }
        SpaceBo spaceBo;
        try {
            spaceBo = spaceService.getDetail(spaceId);
        } catch (Exception e) {
            // 忽略查询异常，保持调用方流程简洁
            return;
        }
        if (spaceBo == null) {
            return;
        }

        if (setName != null) {
            setName.accept(spaceBo.getName());
        }
        if (setParentIds != null && !CollectionUtils.isEmpty(spaceBo.getParentsIds())) {
            setParentIds.accept(spaceBo.getParentsIds().stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(",")));
        }
        if (setParentNames != null && !CollectionUtils.isEmpty(spaceBo.getParentsNames())) {
            setParentNames.accept(String.join(",", spaceBo.getParentsNames()));
        }
    }
}
