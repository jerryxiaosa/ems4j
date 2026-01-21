package info.zhihui.ems.foundation.integration.biz.command.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import info.zhihui.ems.components.datasource.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@TableName("device_command_execute_record")
public class DeviceCommandExecuteRecordEntity extends BaseEntity {

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
    private LocalDateTime executeTime;

    /**
     * 命令来源
     */
    private Integer commandSource;

}
