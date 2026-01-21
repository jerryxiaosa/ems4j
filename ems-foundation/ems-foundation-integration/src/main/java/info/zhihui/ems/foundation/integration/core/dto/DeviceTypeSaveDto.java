package info.zhihui.ems.foundation.integration.core.dto;

import lombok.Data;

@Data
public class DeviceTypeSaveDto {
    private Integer id;
    private Integer pid;
    private String typeName;
    private String typeKey;
}
