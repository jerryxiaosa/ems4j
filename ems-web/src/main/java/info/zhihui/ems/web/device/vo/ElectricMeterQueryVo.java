package info.zhihui.ems.web.device.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 电表查询条件
 */
@Data
@Schema(name = "ElectricMeterQueryVo", description = "电表查询条件")
public class ElectricMeterQueryVo {

    @Schema(description = "电表ID")
    private Integer meterId;

    @Schema(description = "电表名称")
    private String meterName;

    @Schema(description = "电表编号")
    private String deviceNo;

    @Schema(description = "账户ID列表")
    private List<Integer> accountIds;

    @Schema(description = "物联网设备ID")
    private String iotId;

    @Schema(description = "所属区域ID")
    private Integer areaId;

    @Schema(description = "是否在线")
    private Boolean isOnline;

    @Schema(description = "是否断闸")
    private Boolean isCutOff;

    @Schema(description = "是否计量")
    private Boolean isCalculate;

    @Schema(description = "计量类型编码")
    private Integer calculateType;

    @Schema(description = "是否为预付费")
    private Boolean isPrepay;

    @Schema(description = "设备IMEI")
    private String imei;

    @Schema(description = "网关ID")
    private Integer gatewayId;

    @Schema(description = "串口号")
    private Integer portNo;

    @Schema(description = "电表通讯地址")
    private Integer meterAddress;

    @Schema(description = "排除的电表ID")
    private Integer neId;

    @Schema(description = "包含的电表ID列表")
    private List<Integer> inIds;

    @Schema(description = "搜索关键字")
    private String searchKey;

    @Schema(description = "空间ID列表")
    private List<Integer> spaceIds;
}
