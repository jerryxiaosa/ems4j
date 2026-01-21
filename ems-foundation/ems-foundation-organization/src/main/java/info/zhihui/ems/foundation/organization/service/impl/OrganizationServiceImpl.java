package info.zhihui.ems.foundation.organization.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.foundation.organization.bo.OrganizationBo;
import info.zhihui.ems.foundation.organization.dto.OrganizationCreateDto;
import info.zhihui.ems.foundation.organization.dto.OrganizationQueryDto;
import info.zhihui.ems.foundation.organization.dto.OrganizationUpdateDto;
import info.zhihui.ems.foundation.organization.entity.OrganizationEntity;
import info.zhihui.ems.foundation.organization.mapper.OrganizationMapper;
import info.zhihui.ems.foundation.organization.qo.OrganizationQueryQo;
import info.zhihui.ems.foundation.organization.repository.OrganizationRepository;
import info.zhihui.ems.foundation.organization.service.OrganizationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.dao.DuplicateKeyException;

import java.util.List;

@Service
@Validated
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationRepository repository;
    private final OrganizationMapper mapper;

    /**
     * 根据主键获取机构详细信息
     * - 若不存在则抛出 `NotFoundException`
     *
     * @param id 机构主键ID，不能为空
     * @return 机构业务对象
     */
    @Override
    public OrganizationBo getDetail(@NotNull Integer id) {
        OrganizationEntity entity = repository.selectById(id);
        if (entity == null) {
            throw new NotFoundException("机构不存在");
        }

        return mapper.entityToBo(entity);
    }

    /**
     * 根据查询条件获取机构列表（不分页）
     * - 仅返回未删除的数据
     *
     * @param queryDto 查询条件
     * @return 机构业务对象列表
     */
    @Override
    public List<OrganizationBo> findOrganizationList(@NotNull @Valid OrganizationQueryDto queryDto) {
        OrganizationQueryQo qo = mapper.queryDtoToQo(queryDto);
        List<OrganizationEntity> list = repository.selectByQo(qo);
        return mapper.listEntityToBo(list);
    }

    /**
     * 创建机构
     * - 直接插入数据库，若触发唯一索引冲突则抛出 `BusinessRuntimeException`
     *
     * @param dto 创建参数
     * @return 创建后的机构业务对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer add(@NotNull @Valid OrganizationCreateDto dto) {
        OrganizationEntity entity = mapper.createDtoToEntity(dto);
        try {
            repository.insert(entity);
        } catch (DuplicateKeyException e) {
            throw new BusinessRuntimeException("机构名称已存在");
        }
        return entity.getId();
    }

    /**
     * 更新机构信息
     * - 先检查记录是否存在；更新时若触发唯一索引冲突则抛出 `BusinessRuntimeException`
     *
     * @param dto 更新参数（包含主键ID）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(@NotNull @Valid OrganizationUpdateDto dto) {
        OrganizationEntity old = repository.selectById(dto.getId());
        if (old == null) {
            throw new NotFoundException("机构不存在");
        }
        OrganizationEntity entity = mapper.updateDtoToEntity(dto);
        entity.setId(dto.getId());
        try {
            repository.updateById(entity);
        } catch (DuplicateKeyException e) {
            throw new BusinessRuntimeException("机构名称已存在");
        }
    }

    /**
     * 逻辑删除机构
     * - 若记录不存在则抛出 `NotFoundException`
     *
     * @param id 机构主键ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(@NotNull Integer id) {
        OrganizationEntity old = repository.selectById(id);
        if (old == null) {
            throw new NotFoundException("机构不存在");
        }
        int affected = repository.deleteById(id);
        if (affected == 0) {
            throw new BusinessRuntimeException("删除组织失败");
        }
    }

    /**
     * 分页查询机构列表
     * - 仅返回未删除的数据
     *
     * @param queryDto  查询条件
     * @param pageParam 分页参数
     * @return 分页结果
     */
    @Override
    public PageResult<OrganizationBo> findOrganizationPage(@NotNull @Valid OrganizationQueryDto queryDto, @NotNull PageParam pageParam) {
        OrganizationQueryQo qo = mapper.queryDtoToQo(queryDto);

        try (Page<OrganizationEntity> page = PageHelper.startPage(pageParam.getPageNum(), pageParam.getPageSize())) {
            PageInfo<OrganizationEntity> pageInfo = page.doSelectPageInfo(() -> repository.selectByQo(qo));
            return mapper.pageEntityToPageBo(pageInfo);
        }
    }
}
