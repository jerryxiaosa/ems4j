package info.zhihui.ems.foundation.system.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ConfigUpdateDto {

    private String configModuleName;

    private String configKey;

    private String configName;

    private String configValue;

}
