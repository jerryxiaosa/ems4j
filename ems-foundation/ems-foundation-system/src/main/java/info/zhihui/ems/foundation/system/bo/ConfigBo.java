package info.zhihui.ems.foundation.system.bo;

import info.zhihui.ems.common.model.OperatorInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;


@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class ConfigBo extends OperatorInfo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Integer id;

    private String configModuleName;

    private String configKey;

    private String configName;

    private String configValue;

    private Boolean isSystem;

}
