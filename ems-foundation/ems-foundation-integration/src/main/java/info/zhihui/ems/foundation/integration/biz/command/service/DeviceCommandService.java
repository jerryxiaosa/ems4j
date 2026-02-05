package info.zhihui.ems.foundation.integration.biz.command.service;

import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.foundation.integration.biz.command.bo.DeviceCommandExecuteRecordBo;
import info.zhihui.ems.foundation.integration.biz.command.bo.DeviceCommandRecordBo;
import info.zhihui.ems.foundation.integration.biz.command.dto.DeviceCommandAddDto;
import info.zhihui.ems.foundation.integration.biz.command.dto.DeviceCommandCancelDto;
import info.zhihui.ems.foundation.integration.biz.command.dto.DeviceCommandQueryDto;
import info.zhihui.ems.foundation.integration.biz.command.enums.CommandSourceEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 设备指令服务
 * 保留设备指令的执行记录，以及重拾设备指令执行
 */
public interface DeviceCommandService {

    /**
     * 保存设备指令（只会保存，并不下发），需要下发手动调用execDeviceCommand方法
     * @param dto 设备指令
     * @return 保存的设备指令ID
     */
    Integer saveDeviceCommand(@Valid @NotNull DeviceCommandAddDto dto);

    /**
     * 执行设备指令
     * @param commandId 设备指令ID
     * @param commandSource 执行来源
     */
    void execDeviceCommand(Integer commandId, CommandSourceEnum commandSource);

    /**
     * 获取操作指令组包含的操作指令分页
     * @param query 查询参数
     * @param pageParam 分页参数
     */
    PageResult<DeviceCommandRecordBo> findDeviceCommandPage(DeviceCommandQueryDto query, PageParam pageParam);

    /**
     * 获取操作指令组包含的操作指令详情
     * @param commandId 操作指令ID
     */
    DeviceCommandRecordBo getDeviceCommandDetail(Integer commandId);

    /**
     * 获取操作指令的操作（重试）记录
     * @param commandId 操作指令ID
     * @return 操作记录列表
     *
     */
    List<DeviceCommandExecuteRecordBo> findCommandExecuteRecordList(Integer commandId);

    /**
     * 废弃操作指令（针对需要重试的操作指令，即ensureSuccess=true）
     * @param dto 废弃操作指令参数
     */
    void cancelDeviceCommand(DeviceCommandCancelDto dto);

}
