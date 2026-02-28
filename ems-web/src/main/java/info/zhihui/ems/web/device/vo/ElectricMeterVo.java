package info.zhihui.ems.web.device.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import java.time.LocalDateTime;

/**
 * 电表列表展示对象
 */
@Data
@Accessors(chain = true)
@Schema(name = "ElectricMeterVo", description = "电表列表信息")
public class ElectricMeterVo {

    @Schema(description = "电表ID")
    private Integer id;

    @Schema(description = "空间ID")
    private Integer spaceId;

    @Schema(description = "电表名称")
    private String meterName;

    @Schema(description = "电表编号")
    private String meterNo;

    @Schema(description = "设备编号，设备上报标识")
    private String deviceNo;

    @Schema(description = "型号ID")
    private Integer modelId;

    @Schema(description = "产品唯一标识")
    private String productCode;

    @Schema(description = "通信模式")
    private String communicateModel;

    @Schema(description = "网关ID")
    private Integer gatewayId;

    @Schema(description = "串口号")
    private Integer portNo;

    @Schema(description = "电表通讯地址")
    private Integer meterAddress;

    @Schema(description = "设备IMEI")
    private String imei;

    @Schema(description = "是否在线")
    private Boolean isOnline;

    @Schema(description = "离线时长")
    private String offlineDurationText;

    @Schema(description = "是否断闸")
    private Boolean isCutOff;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "物联网ID")
    private String iotId;

    @Schema(description = "是否计量")
    private Boolean isCalculate;

    @Schema(description = "计量类型编码")
    private Integer calculateType;

    @Schema(description = "是否为预付费")
    private Boolean isPrepay;

    @Schema(description = "是否保电")
    private Boolean protectedModel;

    @Schema(description = "计费方案ID")
    private Integer pricePlanId;

    @Schema(description = "预警方案ID")
    private Integer warnPlanId;

    @Schema(description = "预警类型，参考 warnType")
    private String warnType;

    @Schema(description = "账户ID")
    private Integer accountId;

    @Schema(description = "CT变比")
    private Integer ct;

    @Schema(description = "所属区域ID")
    private Integer ownAreaId;

    @Schema(description = "创建人ID")
    private Integer createUser;

    @Schema(description = "创建人名称")
    private String createUserName;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新人ID")
    private Integer updateUser;

    @Schema(description = "更新人名称")
    private String updateUserName;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
