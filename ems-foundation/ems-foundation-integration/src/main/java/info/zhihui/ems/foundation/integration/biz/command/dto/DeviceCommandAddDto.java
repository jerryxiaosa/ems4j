package info.zhihui.ems.foundation.integration.biz.command.dto;

import info.zhihui.ems.common.enums.DeviceTypeEnum;
import info.zhihui.ems.foundation.integration.biz.command.enums.CommandSourceEnum;
import info.zhihui.ems.foundation.integration.biz.command.enums.CommandTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DeviceCommandAddDto {
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
     * 设备所属空间id
     */
    private Integer spaceId;

    /**
     * 设备所属空间名称
     */
    private String spaceName;

    /**
     * 所属区域id
     */
    private Integer areaId;

    /**
     * 账户id
     */
    private Integer accountId;

    /**
     * 操作用户id
     */
    private Integer operateUser;

    /**
     * 操作用户名称
     */
    private String operateUserName;

    /**
     * 是否重试，默认不重试
     */
    private Boolean ensureSuccess;

    /**
     * 备注
     */
    private String remark;
}