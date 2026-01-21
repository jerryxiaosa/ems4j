package info.zhihui.ems.foundation.integration.core.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import info.zhihui.ems.components.datasource.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("device_model")
public class DeviceModelEntity extends BaseEntity {

    private Integer id;
    private Integer typeId;
    private String typeKey;
    private String manufacturerName;
    private String modelName;
    private String productCode;
    private String modelProperty;
}
