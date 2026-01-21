package info.zhihui.ems.foundation.space.service;

import info.zhihui.ems.foundation.space.bo.SpaceBo;
import info.zhihui.ems.foundation.space.dto.SpaceCreateDto;
import info.zhihui.ems.foundation.space.dto.SpaceQueryDto;
import info.zhihui.ems.foundation.space.dto.SpaceUpdateDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 空间服务接口
 *
 * @author jerryxiaosa
 */
public interface SpaceService {

    /**
     * 获取空间详情
     *
     * @param id 空间ID
     * @return 空间详情
     */
    SpaceBo getDetail(@NotNull Integer id);

    /**
     * 查询空间列表
     *
     * @param queryDto 查询条件
     * @return 空间列表
     */
    List<SpaceBo> findSpaceList(@NotNull SpaceQueryDto queryDto);

    /**
     * 新增空间
     *
     * @param createDto 创建参数
     * @return 创建的空间id
     */
    Integer addSpace(@Valid @NotNull SpaceCreateDto createDto);

    /**
     * 更新空间
     *
     * @param updateDto 更新参数
     */
    void updateSpace(@Valid @NotNull SpaceUpdateDto updateDto);

    /**
     * 删除空间
     *
     * @param id 空间ID
     */
    void deleteSpace(@NotNull Integer id);
}
