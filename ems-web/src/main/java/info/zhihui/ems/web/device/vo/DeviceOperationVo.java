package info.zhihui.ems.web.device.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 设备操作列表信息
 */
@Data
@Accessors(chain = true)
@Schema(name = "DeviceOperationVo", description = "设备操作列表信息")
public class DeviceOperationVo {

    @Schema(description = "命令记录ID")
    private Integer id;

    @Schema(description = "设备类型key")
    private String deviceType;

    @Schema(description = "设备编号")
    private String deviceNo;

    @Schema(description = "设备名称")
    private String deviceName;

    @Schema(description = "空间名称")
    private String spaceName;

    @Schema(description = "操作类型编码")
    private Integer commandType;

    @Schema(description = "操作类型名称")
    private String commandTypeName;

    @Schema(description = "操作状态")
    private Boolean success;

    @Schema(description = "是否执行中")
    private Boolean isRunning;

    @Schema(description = "执行次数")
    private Integer executeTimes;

    @Schema(description = "最大执行次数")
    private Integer maxExecuteTimes;

    @Schema(description = "操作人")
    private String operateUserName;

    @Schema(description = "操作时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
