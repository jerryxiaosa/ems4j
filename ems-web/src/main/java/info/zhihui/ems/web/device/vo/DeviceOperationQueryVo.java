package info.zhihui.ems.web.device.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 设备操作查询条件
 */
@Data
@Schema(name = "DeviceOperationQueryVo", description = "设备操作查询条件")
public class DeviceOperationQueryVo {

    @Schema(description = "操作人姓名")
    private String operateUserName;

    @Schema(description = "操作类型编码")
    private Integer commandType;

    @Schema(description = "操作状态")
    private Boolean success;

    @Schema(description = "设备类型key")
    private String deviceType;

    @Schema(description = "设备编号")
    private String deviceNo;

    @Schema(description = "设备名称")
    private String deviceName;

    @Schema(description = "空间名称")
    private String spaceName;
}
