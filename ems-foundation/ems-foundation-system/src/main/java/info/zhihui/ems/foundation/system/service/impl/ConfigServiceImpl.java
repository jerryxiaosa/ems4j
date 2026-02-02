package info.zhihui.ems.foundation.system.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.common.utils.JacksonUtil;
import info.zhihui.ems.foundation.system.bo.ConfigBo;
import info.zhihui.ems.foundation.system.dto.ConfigQueryDto;
import info.zhihui.ems.foundation.system.dto.ConfigUpdateDto;
import info.zhihui.ems.foundation.system.entity.ConfigEntity;
import info.zhihui.ems.foundation.system.mapper.ConfigMapper;
import info.zhihui.ems.foundation.system.qo.ConfigQueryQo;
import info.zhihui.ems.foundation.system.repository.ConfigRepository;
import info.zhihui.ems.foundation.system.service.ConfigService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author jerryxiaosa
 */
@Service
@Slf4j
@AllArgsConstructor
public class ConfigServiceImpl implements ConfigService {
    private final ConfigRepository repository;
    private final ConfigMapper mapper;

    /**
     * 根据配置键获取配置信息
     *
     * @param key 配置键
     * @return 配置业务对象
     * @throws NotFoundException 当配置不存在时抛出
     */
    @Override
    public ConfigBo getByKey(String key) {
        ConfigEntity entity = repository.getByKey(key);
        if (entity == null) {
            throw new NotFoundException("系统配置不存在");
        }
        return mapper.entityToBo(entity);
    }

    /**
     * 根据配置键获取配置值并反序列化为指定类型
     *
     * @param key 配置键
     * @param typeReference 目标类型引用
     * @param <T> 目标类型
     * @return 反序列化后的配置值
     * @throws BusinessRuntimeException 当配置不存在或序列化异常时抛出
     */
    @Override
    public <T> T getValueByKey(String key, TypeReference<T> typeReference) throws BusinessRuntimeException {
        try {
            ConfigEntity entity = repository.getByKey(key);
            if (entity == null) {
                throw new NotFoundException("系统配置不存在");
            }
            return JacksonUtil.fromJson(entity.getConfigValue(), typeReference);
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("配置序列化异常：", e);
            throw new BusinessRuntimeException("配置序列化异常：" + e.getMessage());
        }
    }

    /**
     * 更新系统配置
     *
     * @param bo 配置更新DTO
     * @throws NotFoundException 当配置不存在时抛出
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(ConfigUpdateDto bo) {
        ConfigEntity old = repository.getByKey(bo.getConfigKey());
        if (old == null) {
            throw new NotFoundException("系统配置不存在");
        }

        ConfigEntity entity = mapper.updateBoToEntity(bo);
        entity.setId(old.getId());
        repository.updateById(entity);
    }

    @Override
    public PageResult<ConfigBo> findConfigPage(@NotNull @Valid ConfigQueryDto queryDto, @NotNull PageParam pageParam) {
        ConfigQueryQo qo = mapper.queryDtoToQo(queryDto);
        try (Page<ConfigEntity> page = PageHelper.startPage(pageParam.getPageNum(), pageParam.getPageSize())) {
            PageInfo<ConfigEntity> pageInfo = page.doSelectPageInfo(() -> repository.selectByQo(qo));
            return mapper.pageEntityToPageBo(pageInfo);
        }
    }
}
