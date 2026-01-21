package info.zhihui.ems.foundation.integration.biz.command.bo;

import info.zhihui.ems.common.enums.DeviceTypeEnum;
import info.zhihui.ems.foundation.integration.biz.command.enums.CommandSourceEnum;
import info.zhihui.ems.foundation.integration.biz.command.enums.CommandTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class DeviceCommandRecordBo {
    private Integer id;

    /**
     * 设备类型
     */
    private DeviceTypeEnum deviceType;

    /**
     * 设备id
     */
    private Integer deviceId;

    /**
     * 设备对接的iot平台的id
     */
    private String deviceIotId;

    /**
     * 设备编号
     */
    private String deviceNo;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 命令类型
     */
    private CommandTypeEnum commandType;

    /**
     * 命令来源
     */
    private CommandSourceEnum commandSource;

    /**
     * 命令数据
     */
    private String commandData;

    /**
     * 设备所属空间id
     */
    private Integer spaceId;

    /**
     * 设备所属空间名称
     */
    private String spaceName;

    /**
     * 区域id
     */
    private Integer areaId;

    /**
     * 账户id
     */
    private Integer accountId;

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
    private LocalDateTime lastExecTime;

    /**
     * 是否重试
     */
    private Boolean ensureSuccess;

    /**
     * 运行次数
     */
    private Integer executeTimes;

    /**
     * 操作用户id
     */
    private Integer operateUser;

    /**
     * 操作用户名称
     */
    private String operateUserName;

    /**
     * 记录创建时间
     */
    private LocalDateTime createTime;

    /**
     * 备注
     */
    private String remark;

}
