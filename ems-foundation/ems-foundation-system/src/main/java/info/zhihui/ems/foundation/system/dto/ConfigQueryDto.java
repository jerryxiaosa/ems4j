package info.zhihui.ems.foundation.system.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Set;

/**
 * 系统配置查询 DTO
 */
@Data
@Accessors(chain = true)
public class ConfigQueryDto {

    /**
     * 配置ID集合
     */
    private Set<Integer> ids;

    /**
     * 配置模块
     */
    @Size(max = 64)
    private String configModuleName;

    /**
     * 配置键（模糊查询）
     */
    @Size(max = 64)
    private String configKeyLike;

    /**
     * 配置名称（模糊查询）
     */
    @Size(max = 128)
    private String configNameLike;

    /**
     * 是否系统内置
     */
    private Boolean isSystem;
}
