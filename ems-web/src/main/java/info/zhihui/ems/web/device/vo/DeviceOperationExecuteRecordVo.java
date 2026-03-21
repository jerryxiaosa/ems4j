package info.zhihui.ems.web.device.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 设备操作执行记录
 */
@Data
@Accessors(chain = true)
@Schema(name = "DeviceOperationExecuteRecordVo", description = "设备操作执行记录")
public class DeviceOperationExecuteRecordVo {

    @Schema(description = "执行记录ID")
    private Integer id;

    @Schema(description = "命令记录ID")
    private Integer commandId;

    @Schema(description = "执行状态")
    private Boolean success;

    @Schema(description = "失败原因")
    private String reason;

    @Schema(description = "执行时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime runTime;

    @Schema(description = "命令来源编码")
    private Integer commandSource;

    @Schema(description = "命令来源名称")
    private String commandSourceName;
}
