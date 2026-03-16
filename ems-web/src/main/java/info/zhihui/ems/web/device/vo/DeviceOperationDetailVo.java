package info.zhihui.ems.web.device.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 设备操作详情
 */
@Data
@Accessors(chain = true)
@Schema(name = "DeviceOperationDetailVo", description = "设备操作详情")
public class DeviceOperationDetailVo {

    @Schema(description = "命令记录ID")
    private Integer id;

    @Schema(description = "设备类型key")
    private String deviceType;

    @Schema(description = "设备ID")
    private Integer deviceId;

    @Schema(description = "IoT设备ID")
    private String deviceIotId;

    @Schema(description = "设备编号")
    private String deviceNo;

    @Schema(description = "设备名称")
    private String deviceName;

    @Schema(description = "操作类型编码")
    private Integer commandType;

    @Schema(description = "操作类型名称")
    private String commandTypeName;

    @Schema(description = "命令来源编码")
    private Integer commandSource;

    @Schema(description = "命令来源名称")
    private String commandSourceName;

    @Schema(description = "指令内容")
    private String commandData;

    @Schema(description = "空间ID")
    private Integer spaceId;

    @Schema(description = "空间名称")
    private String spaceName;

    @Schema(description = "区域ID")
    private Integer areaId;

    @Schema(description = "账户ID")
    private Integer accountId;

    @Schema(description = "操作状态")
    private Boolean success;

    @Schema(description = "成功时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime successTime;

    @Schema(description = "最后执行时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastExecuteTime;

    @Schema(description = "是否保证成功")
    private Boolean ensureSuccess;

    @Schema(description = "执行次数")
    private Integer executeTimes;

    @Schema(description = "操作人ID")
    private Integer operateUser;

    @Schema(description = "操作人名称")
    private String operateUserName;

    @Schema(description = "操作时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "备注")
    private String remark;
}
