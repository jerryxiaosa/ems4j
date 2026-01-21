package info.zhihui.ems.web.device.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 网关列表信息
 */
@Data
@Accessors(chain = true)
@Schema(name = "GatewayVo", description = "网关信息")
public class GatewayVo {

    @Schema(description = "网关ID")
    private Integer id;

    @Schema(description = "空间ID")
    private Integer spaceId;

    @Schema(description = "网关编号")
    private String gatewayNo;

    @Schema(description = "设备编号，设备上报标识")
    private String deviceNo;

    @Schema(description = "网关名称")
    private String gatewayName;

    @Schema(description = "型号ID")
    private Integer modelId;

    @Schema(description = "产品唯一标识")
    private String productCode;

    @Schema(description = "通信方式")
    private String communicateModel;

    @Schema(description = "网关序列号")
    private String sn;

    @Schema(description = "网关IMEI")
    private String imei;

    @Schema(description = "物联网ID")
    private Integer iotId;

    @Schema(description = "是否在线")
    private Boolean isOnline;

    @Schema(description = "网关配置信息")
    private String configInfo;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "所属区域ID")
    private Integer ownAreaId;
}
