package info.zhihui.ems.web.device.biz;

import info.zhihui.ems.common.enums.CodeEnum;
import info.zhihui.ems.common.enums.DeviceTypeEnum;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.foundation.integration.biz.command.bo.DeviceCommandExecuteRecordBo;
import info.zhihui.ems.foundation.integration.biz.command.bo.DeviceCommandRecordBo;
import info.zhihui.ems.foundation.integration.biz.command.dto.DeviceCommandQueryDto;
import info.zhihui.ems.foundation.integration.biz.command.enums.CommandSourceEnum;
import info.zhihui.ems.foundation.integration.biz.command.enums.CommandTypeEnum;
import info.zhihui.ems.foundation.integration.biz.command.service.DeviceCommandRetryService;
import info.zhihui.ems.foundation.integration.biz.command.service.DeviceCommandService;
import info.zhihui.ems.web.device.vo.DeviceOperationDetailVo;
import info.zhihui.ems.web.device.vo.DeviceOperationExecuteRecordVo;
import info.zhihui.ems.web.device.vo.DeviceOperationQueryVo;
import info.zhihui.ems.web.device.vo.DeviceOperationVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 设备操作业务编排
 */
@Service
@RequiredArgsConstructor
public class DeviceOperationBiz {

    private final DeviceCommandService deviceCommandService;
    private final DeviceCommandRetryService deviceCommandRetryService;

    /**
     * 分页查询设备操作
     */
    public PageResult<DeviceOperationVo> findDeviceOperationPage(DeviceOperationQueryVo queryVo, Integer pageNum, Integer pageSize) {
        DeviceCommandQueryDto queryDto = buildQueryDto(queryVo);
        PageParam pageParam = new PageParam()
                .setPageNum(Objects.requireNonNullElse(pageNum, 1))
                .setPageSize(Objects.requireNonNullElse(pageSize, 10));

        PageResult<DeviceCommandRecordBo> commandPage = deviceCommandService.findDeviceCommandPage(queryDto, pageParam);
        List<DeviceOperationVo> operationVoList = commandPage.getList() == null
                ? Collections.emptyList()
                : commandPage.getList().stream().map(this::toDeviceOperationVo).toList();
        return new PageResult<DeviceOperationVo>()
                .setPageNum(commandPage.getPageNum())
                .setPageSize(commandPage.getPageSize())
                .setTotal(commandPage.getTotal())
                .setList(operationVoList);
    }

    /**
     * 查询设备操作详情
     */
    public DeviceOperationDetailVo getDeviceOperation(Integer operationId) {
        return toDeviceOperationDetailVo(deviceCommandService.getDeviceCommandDetail(operationId));
    }

    /**
     * 查询设备操作执行记录
     */
    public List<DeviceOperationExecuteRecordVo> findDeviceOperationExecuteRecordList(Integer operationId) {
        List<DeviceCommandExecuteRecordBo> executeRecordBoList = deviceCommandService.findCommandExecuteRecordList(operationId);
        if (executeRecordBoList == null || executeRecordBoList.isEmpty()) {
            return Collections.emptyList();
        }
        return executeRecordBoList.stream().map(this::toDeviceOperationExecuteRecordVo).toList();
    }

    /**
     * 手动重试设备操作
     */
    public void retryDeviceOperation(Integer operationId) {
        deviceCommandRetryService.retryDeviceCommand(operationId, CommandSourceEnum.USER);
    }

    private DeviceCommandQueryDto buildQueryDto(DeviceOperationQueryVo queryVo) {
        DeviceOperationQueryVo actualQueryVo = queryVo == null ? new DeviceOperationQueryVo() : queryVo;
        CommandTypeEnum commandType = parseCommandType(actualQueryVo.getCommandType());
        DeviceTypeEnum deviceType = parseDeviceType(actualQueryVo.getDeviceType());

        return new DeviceCommandQueryDto()
                .setOperateUserName(actualQueryVo.getOperateUserName())
                .setCommandType(commandType)
                .setSuccess(actualQueryVo.getSuccess())
                .setDeviceType(deviceType)
                .setDeviceNo(actualQueryVo.getDeviceNo())
                .setDeviceName(actualQueryVo.getDeviceName())
                .setSpaceName(actualQueryVo.getSpaceName());
    }

    private DeviceOperationVo toDeviceOperationVo(DeviceCommandRecordBo commandRecordBo) {
        CommandTypeEnum commandType = commandRecordBo.getCommandType();
        return new DeviceOperationVo()
                .setId(commandRecordBo.getId())
                .setDeviceType(commandRecordBo.getDeviceType() == null ? null : commandRecordBo.getDeviceType().getKey())
                .setDeviceNo(commandRecordBo.getDeviceNo())
                .setDeviceName(commandRecordBo.getDeviceName())
                .setSpaceName(commandRecordBo.getSpaceName())
                .setCommandType(commandType == null ? null : commandType.getCode())
                .setCommandTypeName(commandType == null ? null : commandType.getInfo())
                .setSuccess(commandRecordBo.getSuccess())
                .setExecuteTimes(commandRecordBo.getExecuteTimes())
                .setOperateUserName(commandRecordBo.getOperateUserName())
                .setCreateTime(commandRecordBo.getCreateTime());
    }

    private DeviceOperationDetailVo toDeviceOperationDetailVo(DeviceCommandRecordBo commandRecordBo) {
        CommandTypeEnum commandType = commandRecordBo.getCommandType();
        CommandSourceEnum commandSource = commandRecordBo.getCommandSource();
        return new DeviceOperationDetailVo()
                .setId(commandRecordBo.getId())
                .setDeviceType(commandRecordBo.getDeviceType() == null ? null : commandRecordBo.getDeviceType().getKey())
                .setDeviceId(commandRecordBo.getDeviceId())
                .setDeviceIotId(commandRecordBo.getDeviceIotId())
                .setDeviceNo(commandRecordBo.getDeviceNo())
                .setDeviceName(commandRecordBo.getDeviceName())
                .setCommandType(commandType == null ? null : commandType.getCode())
                .setCommandTypeName(commandType == null ? null : commandType.getInfo())
                .setCommandSource(commandSource == null ? null : commandSource.getCode())
                .setCommandSourceName(commandSource == null ? null : commandSource.getInfo())
                .setCommandData(commandRecordBo.getCommandData())
                .setSpaceId(commandRecordBo.getSpaceId())
                .setSpaceName(commandRecordBo.getSpaceName())
                .setAreaId(commandRecordBo.getAreaId())
                .setAccountId(commandRecordBo.getAccountId())
                .setSuccess(commandRecordBo.getSuccess())
                .setSuccessTime(commandRecordBo.getSuccessTime())
                .setLastExecuteTime(commandRecordBo.getLastExecTime())
                .setEnsureSuccess(commandRecordBo.getEnsureSuccess())
                .setExecuteTimes(commandRecordBo.getExecuteTimes())
                .setOperateUser(commandRecordBo.getOperateUser())
                .setOperateUserName(commandRecordBo.getOperateUserName())
                .setCreateTime(commandRecordBo.getCreateTime())
                .setRemark(commandRecordBo.getRemark());
    }

    private DeviceOperationExecuteRecordVo toDeviceOperationExecuteRecordVo(DeviceCommandExecuteRecordBo executeRecordBo) {
        CommandSourceEnum commandSource = executeRecordBo.getCommandSource();
        return new DeviceOperationExecuteRecordVo()
                .setId(executeRecordBo.getId())
                .setCommandId(executeRecordBo.getCommandId())
                .setSuccess(executeRecordBo.getSuccess())
                .setReason(executeRecordBo.getReason())
                .setRunTime(executeRecordBo.getRunTime())
                .setCommandSource(commandSource == null ? null : commandSource.getCode())
                .setCommandSourceName(commandSource == null ? null : commandSource.getInfo());
    }

    private CommandTypeEnum parseCommandType(Integer commandTypeCode) {
        if (commandTypeCode == null) {
            return null;
        }
        CommandTypeEnum commandType = CodeEnum.fromCode(commandTypeCode, CommandTypeEnum.class);
        if (commandType == null) {
            throw new BusinessRuntimeException("操作类型不正确");
        }
        return commandType;
    }

    private DeviceTypeEnum parseDeviceType(String deviceTypeKey) {
        if (!StringUtils.hasText(deviceTypeKey)) {
            return null;
        }
        DeviceTypeEnum deviceType = DeviceTypeEnum.fromKey(deviceTypeKey);
        if (deviceType == null) {
            throw new BusinessRuntimeException("设备类型不正确");
        }
        return deviceType;
    }
}
