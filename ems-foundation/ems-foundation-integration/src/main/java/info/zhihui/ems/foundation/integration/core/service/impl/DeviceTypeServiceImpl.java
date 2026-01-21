package info.zhihui.ems.foundation.integration.core.service.impl;

import cn.hutool.core.collection.CollUtil;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.foundation.integration.core.bo.DeviceTypeBo;
import info.zhihui.ems.foundation.integration.core.dto.DeviceTypeQueryDto;
import info.zhihui.ems.foundation.integration.core.dto.DeviceTypeSaveDto;
import info.zhihui.ems.foundation.integration.core.entity.DeviceTypeEntity;
import info.zhihui.ems.foundation.integration.core.mapper.DeviceTypeMapper;
import info.zhihui.ems.foundation.integration.core.qo.DeviceTypeQueryQo;
import info.zhihui.ems.foundation.integration.core.repository.DeviceTypeRepository;
import info.zhihui.ems.foundation.integration.core.service.DeviceTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeviceTypeServiceImpl implements DeviceTypeService {

    private final DeviceTypeMapper mapper;
    private final DeviceTypeRepository repository;

    private final static Integer TOP_LEVEL = 0;

    @Override
    public List<DeviceTypeBo> findList(DeviceTypeQueryDto query) {
        DeviceTypeQueryQo qo = mapper.queryDtoToQo(query);
        List<DeviceTypeEntity> entityList = repository.findList(qo);
        return mapper.listEntityToBo(entityList);
    }

    @Override
    public DeviceTypeBo getDetail(Integer id) {
        DeviceTypeEntity entity = repository.selectById(id);
        return mapper.entityToBo(entity);
    }

    @Override
    public DeviceTypeBo getByKey(String typeKey) {
        List<DeviceTypeBo> list = findList(new DeviceTypeQueryDto().setTypeKey(typeKey));
        if (list.size() != 1) {
            throw new BusinessRuntimeException("该设备类型key不存在：" + typeKey);
        }
        return list.get(0);
    }


    @Override
    public void add(DeviceTypeSaveDto saveDto) {
        DeviceTypeEntity entity = mapper.saveDtoEntity(saveDto);
        if (saveDto.getPid() == null || TOP_LEVEL.equals(saveDto.getPid())) {
            // 顶级
            entity.setPid(TOP_LEVEL)
                    .setAncestorId(TOP_LEVEL.toString())
                    .setLevel(1);
        } else {
            DeviceTypeEntity parent = repository.selectById(saveDto.getPid());
            if (parent == null) {
                throw new BusinessRuntimeException("pid不存在，请确认");
            }
            entity.setAncestorId(genAncestorId(parent))
                    .setLevel(parent.getLevel() + 1);
        }
        repository.insert(entity);
    }

    private String genAncestorId(DeviceTypeEntity parent) {
        return parent.getAncestorId() + "," + parent.getId();
    }

    @Override
    public void update(DeviceTypeSaveDto saveDto) {
        DeviceTypeEntity old = repository.selectById(saveDto.getId());
        if (old == null) {
            throw new BusinessRuntimeException("数据不存在，请确认");
        }
        DeviceTypeEntity entity = mapper.saveDtoEntity(saveDto);
        repository.updateById(entity);
    }

    @Override
    public void delete(Integer id) {
        List<DeviceTypeBo> children = findList(new DeviceTypeQueryDto().setPid(id));
        if (CollUtil.isNotEmpty(children)) {
            throw new BusinessRuntimeException("该品类下有子级，无法删除");
        }
        repository.deleteById(id);
    }

}
