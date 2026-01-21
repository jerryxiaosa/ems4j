package info.zhihui.ems.business.plan.service.impl;

import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.business.plan.bo.WarnPlanBo;
import info.zhihui.ems.business.plan.dto.WarnPlanQueryDto;
import info.zhihui.ems.business.plan.dto.WarnPlanSaveDto;
import info.zhihui.ems.business.plan.entity.WarnPlanEntity;
import info.zhihui.ems.business.plan.mapper.WarningPlanMapper;
import info.zhihui.ems.business.plan.repository.WarnPlanRepository;
import info.zhihui.ems.business.plan.service.WarnPlanService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * 预警方案服务接口
 *
 * @author jerryxiaosa
 */
@Service
@RequiredArgsConstructor
@Validated
public class WarnPlanServiceImpl implements WarnPlanService {
    private final WarningPlanMapper mapper;
    private final WarnPlanRepository repository;

    /**
     * 查询预警方案列表
     *
     * @param query 查询条件
     * @return 预警方案列表
     */
    @Override
    public List<WarnPlanBo> findList(@NotNull WarnPlanQueryDto query) {
        List<WarnPlanEntity> list = repository.getList(mapper.queryDtoToQo(query));
        return mapper.listEntityToBo(list);
    }

    /**
     * 根据ID查询预警方案详情
     *
     * @param id 预警方案ID
     * @return 预警方案详情
     */
    @Override
    public WarnPlanBo getDetail(@NotNull Integer id) {
        WarnPlanEntity entity = repository.selectById(id);
        if (entity == null) {
            throw new NotFoundException("预警方案数据不存在");
        }
        return mapper.detailEntityToBo(entity);
    }

    /**
     * 新增预警方案
     *
     * @param saveDto 预警方案保存数据
     * @return 新增后的预警方案ID
     */
    @Override
    public Integer add(@NotNull @Valid WarnPlanSaveDto saveDto) {
        saveDto.setId(null);
        WarnPlanEntity entity = toEntity(saveDto);
        repository.insert(entity);
        return entity.getId();
    }

    /**
     * 编辑预警方案
     *
     * @param saveDto 预警方案保存数据
     */
    @Override
    public void edit(@NotNull @Valid WarnPlanSaveDto saveDto) {
        WarnPlanEntity old = repository.selectById(saveDto.getId());
        if (old == null) {
            throw new BusinessRuntimeException("数据不存在，请刷新后重试");
        }
        WarnPlanEntity entity = toEntity(saveDto);
        repository.updateById(entity);
    }

    /**
     * 删除预警方案
     *
     * @param id 预警方案ID
     */
    @Override
    public void delete(@NotNull Integer id) {
        repository.deleteById(id);
    }

    private WarnPlanEntity toEntity(WarnPlanSaveDto saveDto) {
        WarnPlanEntity entity = mapper.saveDtoToEntity(saveDto);
        if (entity.getFirstLevel().compareTo(entity.getSecondLevel()) <= 0) {
            throw new BusinessRuntimeException("第一预警金额必须大于第二预警金额");
        }
        return entity;
    }
}
