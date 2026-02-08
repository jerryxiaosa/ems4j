package info.zhihui.ems.iot.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DeviceSaveVo {

    @NotBlank(message = "设备编号不能为空")
    private String deviceNo;
    private Integer portNo;
    private Integer meterAddress;
    private String deviceSecret;
    private Integer slaveAddress;
    @NotBlank(message = "产品编码不能为空")
    private String productCode;
    private Integer parentId;
}
