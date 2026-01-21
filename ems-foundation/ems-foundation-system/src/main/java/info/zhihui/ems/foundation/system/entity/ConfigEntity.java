package info.zhihui.ems.foundation.system.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import info.zhihui.ems.components.datasource.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


@Data
@Accessors(chain = true)
@TableName("sys_config")
@EqualsAndHashCode(callSuper = true)
public class ConfigEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Integer id;

    /**
     * 配置所属模块name
     */
    private String configModuleName;

    /**
     * 配置key
     */
    private String configKey;

    /**
     * 配置name
     */
    private String configName;

    /**
     * 配置value
     */
    private String configValue;

    /**
     * 是否内置，内置配置项可在系统配置列表修改
     */
    private Boolean isSystem;

}
