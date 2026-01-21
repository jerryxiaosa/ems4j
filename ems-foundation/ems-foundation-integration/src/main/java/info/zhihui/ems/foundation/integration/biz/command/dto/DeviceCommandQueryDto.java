package info.zhihui.ems.foundation.integration.biz.command.dto;

import info.zhihui.ems.common.enums.DeviceTypeEnum;
import info.zhihui.ems.foundation.integration.biz.command.enums.CommandTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DeviceCommandQueryDto {

    /**
     * 操作人姓名
     */
    private String operateUserName;

    /**
     * 操作类型
     */
    private CommandTypeEnum commandType;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 所属机构id
     */
    private Integer organizationId;

    /**
     * 所属机构名称
     */
    private String organizationName;

    /**
     * 设备所属空间名称
     */
    private String spaceName;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 设备编号
     */
    private String deviceNo;

    /**
     * 设备类型
     */
    private DeviceTypeEnum deviceType;

    /**
     * 是否升序
     */
    private Boolean asc;

    /**
     * 查询结果限制
     */
    private Integer limit;
}
