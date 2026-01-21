package info.zhihui.ems.foundation.integration.core.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.foundation.integration.core.bo.DeviceModelBo;
import info.zhihui.ems.foundation.integration.core.dto.DeviceModelQueryDto;
import info.zhihui.ems.foundation.integration.core.entity.DeviceModelEntity;
import info.zhihui.ems.foundation.integration.core.mapper.DeviceModelMapper;
import info.zhihui.ems.foundation.integration.core.qo.DeviceModelQueryQo;
import info.zhihui.ems.foundation.integration.core.repository.DeviceModelRepository;
import info.zhihui.ems.foundation.integration.core.service.DeviceModelService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * @author jerryxiaosa
 */
@Service
@RequiredArgsConstructor
@Validated
public class DeviceModelServiceImpl implements DeviceModelService {

    private final DeviceModelMapper mapper;
    private final DeviceModelRepository repository;

    @Override
    public PageResult<DeviceModelBo> findPage(@NotNull DeviceModelQueryDto query, @NotNull PageParam pageParam) {
        DeviceModelQueryQo qo = mapper.queryDtoToQo(query);
        PageInfo<DeviceModelEntity> pageInfo = PageHelper
                .startPage(pageParam.getPageNum(), pageParam.getPageSize())
                .doSelectPageInfo(() -> repository.findList(qo));

        return mapper.pageEntityToBo(pageInfo);
    }

    @Override
    public List<DeviceModelBo> findList(@NotNull DeviceModelQueryDto query) {
        DeviceModelQueryQo qo = mapper.queryDtoToQo(query);
        List<DeviceModelEntity> entityList = repository.findList(qo);
        return mapper.listEntityToBo(entityList);
    }

    @Override
    public DeviceModelBo getDetail(@NotNull Integer id) {
        DeviceModelEntity entity = repository.selectById(id);
        if (entity == null) {
            throw new NotFoundException("电表型号不存在，重新选择");
        }

        return mapper.entityToBo(entity);
    }
}
