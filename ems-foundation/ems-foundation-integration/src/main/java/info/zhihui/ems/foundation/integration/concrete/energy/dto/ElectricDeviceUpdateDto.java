package info.zhihui.ems.foundation.integration.concrete.energy.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class ElectricDeviceUpdateDto extends BaseElectricDeviceDto {

    private String deviceNo;

    private String productCode;

    private String deviceSecret;

    private String parentId;

    private Integer portNo;

    private Integer meterAddress;

    private Integer slaveAddress;
}
