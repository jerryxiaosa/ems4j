package info.zhihui.ems.business.device.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GatewayCreateDto {

    @NotNull(message = "空间id不能为空")
    private Integer spaceId;

    @NotNull(message = "网关名称不能为空")
    private String gatewayName;

    @NotNull(message = "网关型号不能为空")
    private Integer modelId;

    @NotBlank(message = "设备编号不能为空")
    private String deviceNo;

    private String deviceSecret;

    private String sn;

    private String imei;

    /**
     * 网关配置信息,json字符串
     */
    private String configInfo;

    private String remark;

}
