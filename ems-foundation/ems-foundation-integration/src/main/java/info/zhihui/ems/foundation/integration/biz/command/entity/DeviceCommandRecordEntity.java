package info.zhihui.ems.foundation.integration.biz.command.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import info.zhihui.ems.components.datasource.entity.AreaBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@TableName("device_command_record")
public class DeviceCommandRecordEntity extends AreaBaseEntity {
    private Integer id;

    /**
     * 设备类型
     */
    private String deviceTypeKey;

    /**
     * 设备id
     */
    private Integer deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 设备对接的iot平台的id
     */
    private String deviceIotId;

    /**
     * 设备编号
     */
    private String deviceNo;

    /**
     * 设备所属空间id
     */
    private Integer spaceId;

    /**
     * 设备所属空间名称
     */
    private String spaceName;

    /**
     * 账户id
     */
    private Integer accountId;

    /**
     * 命令类型
     */
    private Integer commandType;

    /**
     * 命令来源
     */
    private Integer commandSource;

    /**
     * 命令数据
     */
    private String commandData;

    /**
     * 是否执行成功
     */
    private Boolean success;

    /**
     * 成功时间
     */
    private LocalDateTime successTime;

    /**
     * 最后执行时间
     */
    private LocalDateTime lastExecuteTime;

    /**
     * 是否重试
     */
    private Boolean ensureSuccess;

    /**
     * 运行
     */
    private Integer executeTimes;

    /**
     * 备注
     */
    private String remark;

}
