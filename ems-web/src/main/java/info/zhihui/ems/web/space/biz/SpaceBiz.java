package info.zhihui.ems.web.space.biz;

import info.zhihui.ems.common.utils.TreeUtil;
import info.zhihui.ems.foundation.space.bo.SpaceBo;
import info.zhihui.ems.foundation.space.dto.SpaceCreateDto;
import info.zhihui.ems.foundation.space.dto.SpaceQueryDto;
import info.zhihui.ems.foundation.space.dto.SpaceUpdateDto;
import info.zhihui.ems.foundation.space.service.SpaceService;
import info.zhihui.ems.web.space.mapstruct.SpaceWebMapper;
import info.zhihui.ems.web.space.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 空间模块业务编排
 */
@Service
@RequiredArgsConstructor
public class SpaceBiz {

    private final SpaceService spaceService;
    private final SpaceWebMapper spaceWebMapper;

    /**
     * 查询空间列表
     */
    public List<SpaceVo> findSpaceList(SpaceQueryVo queryVo) {
        SpaceQueryDto queryDto = spaceWebMapper.toSpaceQueryDto(queryVo);
        List<SpaceBo> spaceBos = spaceService.findSpaceList(queryDto);

        return spaceWebMapper.toSpaceDetailVoList(spaceBos);
    }

    /**
     * 查询空间树形结构
     *
     * @param queryVo 查询条件
     * @return 空间树列表
     */
    public List<SpaceWithChildrenVo> findSpaceTree(SpaceQueryVo queryVo) {
        SpaceQueryDto queryDto = spaceWebMapper.toSpaceQueryDto(queryVo);
        List<SpaceBo> spaceBos = spaceService.findSpaceList(queryDto);
        if (spaceBos == null || spaceBos.isEmpty()) {
            return Collections.emptyList();
        }

        List<SpaceWithChildrenVo> spaceWithChildrenVos = spaceWebMapper.toSpaceVoList(spaceBos);
        return TreeUtil.buildTree(
                spaceWithChildrenVos,
                SpaceWithChildrenVo::getId,
                SpaceWithChildrenVo::getPid,
                (node, children) -> node.setChildren(children.isEmpty() ? Collections.emptyList() : new ArrayList<>(children))
        );
    }

    /**
     * 获取空间详情
     *
     * @param id 空间ID
     * @return 空间信息
     */
    public SpaceVo getSpace(Integer id) {
        SpaceBo bo = spaceService.getDetail(id);
        return spaceWebMapper.toSpaceDetailVo(bo);
    }

    /**
     * 创建空间
     *
     * @param createVo 创建参数
     * @return 空间ID
     */
    public Integer createSpace(SpaceCreateVo createVo) {
        SpaceCreateDto createDto = spaceWebMapper.toSpaceCreateDto(createVo);
        return spaceService.addSpace(createDto);
    }

    /**
     * 更新空间
     *
     * @param id       空间ID
     * @param updateVo 更新参数
     */
    public void updateSpace(Integer id, SpaceUpdateVo updateVo) {
        SpaceUpdateDto updateDto = spaceWebMapper.toSpaceUpdateDto(updateVo);
        updateDto.setId(id);
        spaceService.updateSpace(updateDto);
    }

    /**
     * 删除空间
     *
     * @param id 空间ID
     */
    public void deleteSpace(Integer id) {
        spaceService.deleteSpace(id);
    }
}
