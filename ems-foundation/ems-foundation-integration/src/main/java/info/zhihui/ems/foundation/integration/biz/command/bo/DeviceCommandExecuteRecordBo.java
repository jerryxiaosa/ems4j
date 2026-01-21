package info.zhihui.ems.foundation.integration.biz.command.bo;

import info.zhihui.ems.common.model.OperatorInfo;
import info.zhihui.ems.foundation.integration.biz.command.enums.CommandSourceEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class DeviceCommandExecuteRecordBo extends OperatorInfo {

    private Integer id;

    /**
     * 设备命令ID
     */
    private Integer commandId;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 失败原因
     */
    private String reason;

    /**
     * 运行时间
     */
    private LocalDateTime runTime;

    /**
     * 命令来源
     */
    private CommandSourceEnum commandSource;

}
