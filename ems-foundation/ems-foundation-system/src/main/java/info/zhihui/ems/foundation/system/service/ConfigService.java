package info.zhihui.ems.foundation.system.service;

import com.fasterxml.jackson.core.type.TypeReference;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.exception.DataException;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.foundation.system.bo.ConfigBo;
import info.zhihui.ems.foundation.system.dto.ConfigQueryDto;
import info.zhihui.ems.foundation.system.dto.ConfigUpdateDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * 系统配置服务接口
 *
 * @author jerryxiaosa
 */
public interface ConfigService {

    /**
     * 根据配置键获取配置信息
     *
     * @param key 配置键
     * @return 配置业务对象
     * @throws DataException 当配置不存在时抛出
     */
    ConfigBo getByKey(String key);

    /**
     * 根据配置键获取配置值并反序列化为指定类型
     *
     * @param key 配置键
     * @param typeReference 目标类型引用
     * @param <T> 目标类型
     * @return 反序列化后的配置值
     * @throws BusinessRuntimeException 当配置不存在或序列化异常时抛出
     */
    <T> T getValueByKey(String key, TypeReference<T> typeReference) throws BusinessRuntimeException;

    /**
     * 更新系统配置
     *
     * @param bo 配置更新DTO
     * @throws DataException 当配置不存在时抛出
     */
    void update(ConfigUpdateDto bo);

    /**
     * 分页查询系统配置
     *
     * @param queryDto 查询条件
     * @param pageParam 分页参数
     * @return 配置分页结果
     */
    PageResult<ConfigBo> findConfigPage(@NotNull @Valid ConfigQueryDto queryDto, @NotNull PageParam pageParam);
}
