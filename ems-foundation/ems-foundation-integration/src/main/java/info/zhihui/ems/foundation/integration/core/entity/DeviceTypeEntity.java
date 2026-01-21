package info.zhihui.ems.foundation.integration.core.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import info.zhihui.ems.components.datasource.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@TableName("device_type")
public class DeviceTypeEntity extends BaseEntity {

    private Integer id;
    private Integer pid;
    /**
     * 祖级id，不包括自身
     */
    private String ancestorId;
    private String typeName;
    private String typeKey;
    /**
     * 层级
     */
    private Integer level;

}
