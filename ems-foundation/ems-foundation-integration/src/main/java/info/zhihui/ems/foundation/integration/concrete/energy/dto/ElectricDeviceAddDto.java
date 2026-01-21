package info.zhihui.ems.foundation.integration.concrete.energy.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ElectricDeviceAddDto {

    private String deviceNo;

    private String productCode;

    private String deviceSecret;

    private Integer parentId;

    private Integer portNo;

    private Integer meterAddress;

    private Integer slaveAddress;

    private Integer areaId;
}
