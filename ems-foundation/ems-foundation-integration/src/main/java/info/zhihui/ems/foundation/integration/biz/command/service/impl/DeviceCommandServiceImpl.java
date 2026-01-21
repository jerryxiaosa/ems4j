package info.zhihui.ems.foundation.integration.biz.command.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import info.zhihui.ems.common.enums.CodeEnum;
import info.zhihui.ems.common.enums.DeviceTypeEnum;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.common.paging.PageParam;
import info.zhihui.ems.common.paging.PageResult;
import info.zhihui.ems.components.lock.core.LockTemplate;
import info.zhihui.ems.foundation.integration.biz.command.bo.DeviceCommandExecuteRecordBo;
import info.zhihui.ems.foundation.integration.biz.command.bo.DeviceCommandRecordBo;
import info.zhihui.ems.foundation.integration.biz.command.dto.DeviceCommandAddDto;
import info.zhihui.ems.foundation.integration.biz.command.dto.DeviceCommandCancelDto;
import info.zhihui.ems.foundation.integration.biz.command.dto.DeviceCommandQueryDto;
import info.zhihui.ems.foundation.integration.biz.command.entity.DeviceCommandExecuteRecordEntity;
import info.zhihui.ems.foundation.integration.biz.command.entity.DeviceCommandRecordEntity;
import info.zhihui.ems.foundation.integration.biz.command.enums.CommandSourceEnum;
import info.zhihui.ems.foundation.integration.biz.command.qo.DeviceCommandCancelQo;
import info.zhihui.ems.foundation.integration.biz.command.qo.DeviceCommandQo;
import info.zhihui.ems.foundation.integration.biz.command.repository.DeviceCommandExecuteRecordRepository;
import info.zhihui.ems.foundation.integration.biz.command.repository.DeviceCommandRecordRepository;
import info.zhihui.ems.foundation.integration.biz.command.service.DeviceCommandExecutor;
import info.zhihui.ems.foundation.integration.biz.command.service.DeviceCommandExecutorContext;
import info.zhihui.ems.foundation.integration.biz.command.service.DeviceCommandService;
import info.zhihui.ems.foundation.integration.biz.command.enums.CommandTypeEnum;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;

/**
 * @author jerryxiaosa
 */
@Service
@AllArgsConstructor
@Slf4j
public class DeviceCommandServiceImpl implements DeviceCommandService {
    private final DeviceCommandRecordRepository commandRecordRepository;
    private final DeviceCommandExecuteRecordRepository commandExecuteRecordRepository;
    private final DeviceCommandExecutorContext deviceCommandExecutorContext;
    private final LockTemplate lockTemplate;

    private final static String LOCK_DEVICE_COMMAND = "LOCK:DEVICE:COMMAND:%s-%d";

    @Override
    public Integer saveDeviceCommand(DeviceCommandAddDto dto) {
        DeviceCommandRecordEntity commandRecord = new DeviceCommandRecordEntity()
                .setDeviceTypeKey(dto.getDeviceType().getKey())
                .setDeviceId(dto.getDeviceId())
                .setDeviceIotId(dto.getDeviceIotId())
                .setDeviceNo(dto.getDeviceNo())
                .setDeviceName(dto.getDeviceName())
                .setSpaceId(dto.getSpaceId())
                .setSpaceName(dto.getSpaceName())
                .setAccountId(dto.getAccountId())
                .setCommandType(dto.getCommandType().getCode())
                .setCommandSource(dto.getCommandSource().getCode())
                .setCommandData(dto.getCommandData())
                .setEnsureSuccess(dto.getEnsureSuccess())
                .setRemark(dto.getRemark());

        commandRecord.setOwnAreaId(dto.getAreaId())
                .setCreateUser(dto.getOperateUser())
                .setCreateUserName(dto.getOperateUserName());

        commandRecordRepository.insert(commandRecord);
        return commandRecord.getId();
    }

    @Override
    public void execDeviceCommand(Integer commandId, CommandSourceEnum commandSource) {
        DeviceCommandRecordBo commandRecordBo = getDeviceCommandDetail(commandId);
        Lock lock = getDeviceLock(commandRecordBo.getDeviceId(), commandRecordBo.getDeviceType().getKey());
        if (!lock.tryLock()) {
            throw new BusinessRuntimeException("设备命令正在处理中，请稍后重试");
        }

        boolean isSuccess = true;
        String reason = null;

        try {
            DeviceCommandExecutor executor = deviceCommandExecutorContext.getDeviceCommandExecutor(commandRecordBo.getCommandType());
            executor.execute(commandRecordBo);
        } catch (Exception e) {
            isSuccess = false;
            reason = e.getMessage();
        } finally {
            saveExecuteRecord(commandRecordBo, commandSource, isSuccess, reason);
            lock.unlock();
            log.info("命令执行完毕：{}；执行成功状态：{}", commandRecordBo.getDeviceId(), isSuccess);
        }
    }

    private void saveExecuteRecord(DeviceCommandRecordBo commandRecordBo, CommandSourceEnum commandSource, boolean isSuccess, String reason) {
        LocalDateTime now = LocalDateTime.now();

        DeviceCommandExecuteRecordEntity executeRecordEntity = new DeviceCommandExecuteRecordEntity()
                .setCommandId(commandRecordBo.getId())
                .setCommandSource(getCommandSource(commandRecordBo, commandSource))
                .setSuccess(isSuccess)
                .setReason(reason)
                .setExecuteTime(now);
        commandExecuteRecordRepository.insert(executeRecordEntity);

        DeviceCommandRecordEntity recordEntity = new DeviceCommandRecordEntity()
                .setId(commandRecordBo.getId())
                .setSuccess(isSuccess)
                .setSuccessTime(isSuccess ? now : null)
                .setLastExecuteTime(now);
        commandRecordRepository.updateCommandExecuteInfo(recordEntity);
    }

    private Lock getDeviceLock(Integer deviceId, String deviceType) {
        if (deviceId == null || deviceType == null) {
            throw new BusinessRuntimeException("设备ID和类型不能为空");
        }
        return lockTemplate.getLock(String.format(LOCK_DEVICE_COMMAND, deviceType, deviceId));
    }

    private Integer getCommandSource(DeviceCommandRecordBo commandRecordBo, CommandSourceEnum commandSource) {
        Integer result;
        if (commandSource != null) {
            result = commandSource.getCode();
        } else if (commandRecordBo.getCommandSource() != null) {
            result = commandRecordBo.getCommandSource().getCode();
        } else {
            result = CommandSourceEnum.SYSTEM.getCode();
        }
        return result;
    }

    @Override
    public PageResult<DeviceCommandRecordBo> findDeviceCommandPage(DeviceCommandQueryDto query, PageParam pageParam) {
        try (Page<DeviceCommandRecordEntity> page = PageHelper.startPage(pageParam.getPageNum(), pageParam.getPageSize())) {
            PageInfo<DeviceCommandRecordEntity> pageInfo = page.doSelectPageInfo(() -> commandRecordRepository.findList(toQo(query)));

            List<DeviceCommandRecordBo> deviceCommandRecordBoList = new ArrayList<>();
            pageInfo.getList().forEach(entity -> deviceCommandRecordBoList.add(toBo(entity)));

            PageResult<DeviceCommandRecordBo> result = new PageResult<>();
            result.setPageSize(pageInfo.getPageSize());
            result.setPageNum(pageInfo.getPageNum());
            result.setTotal(pageInfo.getTotal());
            result.setList(deviceCommandRecordBoList);

            return result;
        }
    }

    private DeviceCommandQo toQo(DeviceCommandQueryDto query) {
        return new DeviceCommandQo()
                .setOperateUserName(query.getOperateUserName())
                .setCommandType(query.getCommandType().getCode())
                .setSuccess(query.getSuccess())
                .setOrganizationName(query.getOrganizationName())
                .setSpaceName(query.getSpaceName())
                .setDeviceName(query.getDeviceName())
                .setDeviceNo(query.getDeviceNo())
                .setDeviceType(query.getDeviceType().getKey())
                .setAsc(query.getAsc())
                .setLimit(query.getLimit())
                ;
    }

    @Override
    public DeviceCommandRecordBo getDeviceCommandDetail(Integer commandId) {
        DeviceCommandRecordEntity entity = commandRecordRepository.selectById(commandId);
        if (entity == null) {
            throw new NotFoundException("没找到命令记录");
        }

        return toBo(entity);
    }

    private DeviceCommandRecordBo toBo(DeviceCommandRecordEntity entity) {
        return new DeviceCommandRecordBo()
                .setId(entity.getId())
                .setDeviceType(DeviceTypeEnum.fromKey(entity.getDeviceTypeKey()))
                .setDeviceId(entity.getDeviceId())
                .setDeviceIotId(entity.getDeviceIotId())
                .setDeviceNo(entity.getDeviceNo())
                .setDeviceName(entity.getDeviceName())
                .setCommandType(CodeEnum.fromCode(entity.getCommandType(), CommandTypeEnum.class))
                .setCommandSource(CodeEnum.fromCode(entity.getCommandSource(), CommandSourceEnum.class))
                .setCommandData(entity.getCommandData())
                .setSpaceId(entity.getSpaceId())
                .setSpaceName(entity.getSpaceName())
                .setAreaId(entity.getOwnAreaId())
                .setAccountId(entity.getAccountId())
                .setSuccess(entity.getSuccess())
                .setSuccessTime(entity.getSuccessTime())
                .setLastExecTime(entity.getLastExecuteTime())
                .setEnsureSuccess(entity.getEnsureSuccess())
                .setExecuteTimes(entity.getExecuteTimes())
                .setOperateUser(entity.getCreateUser())
                .setOperateUserName(entity.getCreateUserName())
                .setCreateTime(entity.getCreateTime())
                .setRemark(entity.getRemark());
    }

    @Override
    public List<DeviceCommandExecuteRecordBo> findCommandExecuteRecordList(Integer commandId) {
        List<DeviceCommandExecuteRecordEntity> executeRecordList = commandExecuteRecordRepository.findList(commandId);

        return executeRecordList.stream().map(this::toExecuteRecordBo).toList();
    }

    private DeviceCommandExecuteRecordBo toExecuteRecordBo(DeviceCommandExecuteRecordEntity entity) {
        return new DeviceCommandExecuteRecordBo()
                .setId(entity.getId())
                .setCommandId(entity.getCommandId())
                .setSuccess(entity.getSuccess())
                .setReason(entity.getReason())
                .setRunTime(entity.getExecuteTime())
                .setCommandSource(CodeEnum.fromCode(entity.getCommandSource(), CommandSourceEnum.class));
    }

    @Override
    public void cancelDeviceCommand(DeviceCommandCancelDto dto) {
        Lock lock = getDeviceLock(dto.getDeviceId(), dto.getDeviceType().getKey());
        if (!lock.tryLock()) {
            throw new BusinessRuntimeException("设备命令正在处理中，请稍后重试");
        }

        try {
            Integer commandType = dto.getCommandType() == null ? null : dto.getCommandType().getCode();
            DeviceCommandCancelQo qo = new DeviceCommandCancelQo()
                    .setDeviceId(dto.getDeviceId())
                    .setDeviceType(dto.getDeviceType().getKey())
                    .setCommandType(commandType)
                    .setReason(dto.getReason());
            commandRecordRepository.cancelDeviceCommand(qo);
        } finally {
            lock.unlock();
        }
    }


}
