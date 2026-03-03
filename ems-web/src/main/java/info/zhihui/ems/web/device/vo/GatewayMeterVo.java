package info.zhihui.ems.web.device.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 网关接入电表信息
 */
@Data
@Accessors(chain = true)
@Schema(name = "GatewayMeterVo", description = "网关接入电表信息")
public class GatewayMeterVo {

    @Schema(description = "电表ID")
    private Integer id;

    @Schema(description = "电表名称")
    private String meterName;

    @Schema(description = "设备编号，设备上报标识")
    private String deviceNo;

    @Schema(description = "是否在线")
    private Boolean isOnline;

    @Schema(description = "串口号")
    private Integer portNo;

    @Schema(description = "电表通讯地址")
    private Integer meterAddress;
}
