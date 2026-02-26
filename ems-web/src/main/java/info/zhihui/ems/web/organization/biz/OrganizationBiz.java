package info.zhihui.ems.web.organization.biz;

import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.foundation.organization.bo.OrganizationBo;
import info.zhihui.ems.foundation.organization.dto.OrganizationCreateDto;
import info.zhihui.ems.foundation.organization.dto.OrganizationQueryDto;
import info.zhihui.ems.foundation.organization.dto.OrganizationUpdateDto;
import info.zhihui.ems.foundation.organization.service.OrganizationService;
import info.zhihui.ems.foundation.space.bo.SpaceBo;
import info.zhihui.ems.foundation.space.enums.SpaceTypeEnum;
import info.zhihui.ems.foundation.space.service.SpaceService;
import info.zhihui.ems.web.organization.mapstruct.OrganizationWebMapper;
import info.zhihui.ems.web.organization.vo.OrganizationCreateVo;
import info.zhihui.ems.web.organization.vo.OrganizationOptionQueryVo;
import info.zhihui.ems.web.organization.vo.OrganizationOptionVo;
import info.zhihui.ems.web.organization.vo.OrganizationQueryVo;
import info.zhihui.ems.web.organization.vo.OrganizationUpdateVo;
import info.zhihui.ems.web.organization.vo.OrganizationVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 组织模块业务编排
 */
@Service
@RequiredArgsConstructor
public class OrganizationBiz {

    private final OrganizationService organizationService;
    private final OrganizationWebMapper organizationWebMapper;
    private final SpaceService spaceService;

    /**
     * 分页查询组织信息
     *
     * @param queryVo  查询条件
     * @param pageNum  页码
     * @param pageSize 页大小
     * @return 组织分页结果
     */
    public PageResult<OrganizationVo> findOrganizationPage(OrganizationQueryVo queryVo, Integer pageNum, Integer pageSize) {
        OrganizationQueryDto queryDto = organizationWebMapper.toOrganizationQueryDto(queryVo);
        PageParam pageParam = new PageParam()
                .setPageNum(Objects.requireNonNullElse(pageNum, 1))
                .setPageSize(Objects.requireNonNullElse(pageSize, 10));
        PageResult<OrganizationBo> pageResult = organizationService.findOrganizationPage(queryDto, pageParam);
        List<OrganizationBo> bos = pageResult.getList();

        List<OrganizationVo> vos = bos == null ? Collections.emptyList() : organizationWebMapper.toOrganizationVoList(bos);
        return new PageResult<OrganizationVo>()
                .setList(vos)
                .setPageNum(pageResult.getPageNum())
                .setPageSize(pageResult.getPageSize())
                .setTotal(pageResult.getTotal());
    }

    /**
     * 查询组织列表
     *
     * @param queryVo 查询条件
     * @return 组织列表
     */
    public List<OrganizationVo> findOrganizationList(OrganizationQueryVo queryVo) {
        OrganizationQueryDto queryDto = organizationWebMapper.toOrganizationQueryDto(queryVo);
        List<OrganizationBo> bos = organizationService.findOrganizationList(queryDto);
        if (CollectionUtils.isEmpty(bos)) {
            return Collections.emptyList();
        }
        return organizationWebMapper.toOrganizationVoList(bos);
    }

    /**
     * 查询组织下拉列表（默认取第一页）
     */
    public List<OrganizationOptionVo> findOrganizationOptionList(OrganizationOptionQueryVo queryVo) {
        OrganizationOptionQueryVo safeQueryVo = queryVo == null ? new OrganizationOptionQueryVo() : queryVo;
        Integer limit = Objects.requireNonNullElse(safeQueryVo.getLimit(), 20);

        OrganizationQueryDto queryDto = new OrganizationQueryDto()
                .setOrganizationNameLike(safeQueryVo.getOrganizationNameLike());
        PageParam pageParam = new PageParam()
                .setPageNum(1)
                .setPageSize(limit);

        PageResult<OrganizationBo> pageResult = organizationService.findOrganizationPage(queryDto, pageParam);
        List<OrganizationBo> bos = pageResult == null ? null : pageResult.getList();
        if (CollectionUtils.isEmpty(bos)) {
            return Collections.emptyList();
        }
        return organizationWebMapper.toOrganizationOptionVoList(bos);
    }

    /**
     * 获取组织详情
     *
     * @param id 组织ID
     * @return 组织信息
     */
    public OrganizationVo getOrganization(Integer id) {
        OrganizationBo bo = organizationService.getDetail(id);
        return organizationWebMapper.toOrganizationVo(bo);
    }

    /**
     * 新增组织
     *
     * @param createVo 创建参数
     * @return 新增组织ID
     */
    public Integer createOrganization(OrganizationCreateVo createVo) {
        OrganizationCreateDto createDto = organizationWebMapper.toOrganizationCreateDto(createVo);
        checkOrganizationSpace(createDto.getOwnAreaId());
        return organizationService.add(createDto);
    }

    private void checkOrganizationSpace(Integer areaId) {
        if (areaId != null) {
            SpaceBo spaceBo = spaceService.getDetail(areaId);
            if (!SpaceTypeEnum.MAIN.equals(spaceBo.getType())) {
                throw new BusinessRuntimeException("组织所属区域不正确");
            }
        }
    }

    /**
     * 更新组织
     *
     * @param id       组织ID
     * @param updateVo 更新参数
     */
    public void updateOrganization(Integer id, OrganizationUpdateVo updateVo) {
        OrganizationUpdateDto updateDto = organizationWebMapper.toOrganizationUpdateDto(updateVo);
        checkOrganizationSpace(updateDto.getOwnAreaId());

        updateDto.setId(id);
        organizationService.update(updateDto);
    }

    /**
     * 删除组织
     *
     * @param id 组织ID
     */
    public void deleteOrganization(Integer id) {
        organizationService.delete(id);
    }

}
