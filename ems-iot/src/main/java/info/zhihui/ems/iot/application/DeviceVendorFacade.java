package info.zhihui.ems.iot.application;

import info.zhihui.ems.common.enums.ElectricPricePeriodEnum;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.iot.config.IotOnlineProperties;
import info.zhihui.ems.iot.domain.command.DeviceCommandRequest;
import info.zhihui.ems.iot.domain.command.concrete.*;
import info.zhihui.ems.iot.domain.model.Device;
import info.zhihui.ems.iot.domain.model.DeviceCommandResult;
import info.zhihui.ems.iot.domain.port.DeviceRegistry;
import info.zhihui.ems.iot.enums.DeviceAccessModeEnum;
import info.zhihui.ems.iot.vo.electric.ElectricDateDurationVo;
import info.zhihui.ems.iot.vo.electric.ElectricDurationUpdateVo;
import info.zhihui.ems.iot.vo.electric.ElectricDurationVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceVendorFacade {

    private final CommandAppService commandAppService;
    private final DeviceRegistry deviceRegistry;
    private final IotOnlineProperties onlineProperties;

    public Integer getCt(Integer deviceId) {
        DeviceCommandResult result = sendAndAssertSuccess(deviceId, new GetCtCommand(), "CT读取");
        return requireInteger(result, "CT读取");
    }

    public void setCt(Integer deviceId, Integer ct) {
        SetCtCommand command = new SetCtCommand().setCt(ct);
        sendAndAssertSuccess(deviceId, command, "CT 下发");
    }

    public List<ElectricDurationVo> getDuration(Integer deviceId, Integer plan) {
        throw new BusinessRuntimeException("暂不支持读取时段电价");
    }

    public void setDuration(Integer deviceId, ElectricDurationUpdateVo dto) {
        List<ElectricDurationVo> copy = new ArrayList<>(dto.getElectricDurations());
        SetDailyEnergyPlanCommand command = new SetDailyEnergyPlanCommand()
                .setPlan(dto.getPlan())
                .setSlots(toDailySlots(copy));
        sendAndAssertSuccess(deviceId, command, "时段电价下发");
    }

    public List<ElectricDateDurationVo> getDateDuration(Integer deviceId, String plan) {
        throw new BusinessRuntimeException("暂不支持读取日期电价");
    }

    public void setDateDuration(Integer deviceId, String plan, List<ElectricDateDurationVo> dto) {
        List<ElectricDateDurationVo> copy = new ArrayList<>(dto);
        SetDatePlanCommand command = new SetDatePlanCommand()
                .setPlan(parsePlan(plan))
                .setItems(toDatePlanItems(copy));
        sendAndAssertSuccess(deviceId, command, "指定日期电价下发");
    }

    public BigDecimal getUsedPower(Integer deviceId, Integer type) {
        ElectricPricePeriodEnum period = ElectricPricePeriodEnum.fromCode(type == null
                ? ElectricPricePeriodEnum.TOTAL.getCode()
                : type);
        DeviceCommandRequest request = buildEnergyCommand(period);
        DeviceCommandResult result = sendAndAssertSuccess(deviceId, request, "电量读取");
        Integer energyValue = requireInteger(result, "电量读取");
        Integer ctValue;
        try {
            ctValue = getCt(deviceId);
        } catch (Exception ex) {
            log.warn("获取CT值失败，将使用默认值1", ex);
            // 没有获取到CT值，默认为1
            ctValue = 1;
        }

        // 返回的是单位为瓦的整数，需要转换成瓦的浮点数，同时需要乘以CT值
        return BigDecimal.valueOf(energyValue)
                .multiply(BigDecimal.valueOf(ctValue))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    public void cutPower(Integer deviceId) {
        sendAndAssertSuccess(deviceId, new CutOffCommand(), "拉闸下发");
    }

    public void recoverPower(Integer deviceId) {
        sendAndAssertSuccess(deviceId, new RecoverCommand(), "合闸下发");
    }

    public Boolean getOnline(Integer deviceId) {
        Device device = deviceRegistry.getById(deviceId);
        DeviceAccessModeEnum accessMode = device.getProduct() == null ? null : device.getProduct().getAccessMode();
        if (accessMode == null) {
            throw new BusinessRuntimeException("设备接入方式缺失");
        }
        if (DeviceAccessModeEnum.GATEWAY.equals(accessMode)) {
            return probeGatewayChildOnline(device);
        }
        return isDirectOnline(device);
    }

    private DeviceCommandResult sendAndAssertSuccess(Integer deviceId, DeviceCommandRequest request,
                                                     String action) {
        try {
            DeviceCommandResult result = commandAppService.sendCommand(deviceId, request).get();
            if (result == null) {
                throw new BusinessRuntimeException(action + "失败：返回结果为空");
            }
            if (!result.isSuccess()) {
                String error = result.getErrorMessage();
                if (error == null || error.isBlank()) {
                    error = "设备返回失败";
                }
                throw new BusinessRuntimeException(action + "失败：" + error);
            }
            return result;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new BusinessRuntimeException(action + "失败：线程被中断");
        } catch (ExecutionException ex) {
            Throwable cause = ex.getCause() == null ? ex : ex.getCause();
            throw new BusinessRuntimeException(action + "失败：" + cause.getMessage());
        }
    }

    private List<DailyEnergySlot> toDailySlots(List<ElectricDurationVo> durations) {
        List<DailyEnergySlot> slots = new ArrayList<>();
        if (durations == null) {
            return slots;
        }
        for (ElectricDurationVo duration : durations) {
            slots.add(new DailyEnergySlot()
                    .setPeriod(parsePeriod(duration.getType()))
                    .setTime(parseTime(duration.getHour(), duration.getMin())));
        }
        return slots;
    }

    private List<DatePlanItem> toDatePlanItems(List<ElectricDateDurationVo> dtoList) {
        List<DatePlanItem> items = new ArrayList<>();
        if (dtoList == null) {
            return items;
        }
        for (ElectricDateDurationVo dto : dtoList) {
            items.add(new DatePlanItem()
                    .setMonth(parseNumber(dto.getMonth()))
                    .setDay(parseNumber(dto.getDay()))
                    .setPlan(parseNumber(dto.getPlan())));
        }
        return items;
    }

    private ElectricPricePeriodEnum parsePeriod(String code) {
        Integer value = parseNumber(code);
        return ElectricPricePeriodEnum.fromCode(value);
    }

    private LocalTime parseTime(String hour, String minute) {
        return LocalTime.of(parseNumber(hour), parseNumber(minute));
    }

    private Integer parsePlan(String plan) {
        return parseNumber(plan);
    }

    private Integer parseNumber(String value) {
        if (value == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("参数格式不正确: " + value);
        }
    }

    private DeviceCommandRequest buildEnergyCommand(ElectricPricePeriodEnum period) {
        return switch (period) {
            case TOTAL -> new GetTotalEnergyCommand();
            case HIGHER -> new GetHigherEnergyCommand();
            case HIGH -> new GetHighEnergyCommand();
            case LOW -> new GetLowEnergyCommand();
            case LOWER -> new GetLowerEnergyCommand();
            case DEEP_LOW -> new GetDeepLowEnergyCommand();
        };
    }

    private Integer requireInteger(DeviceCommandResult result, String action) {
        Object data = result.getData();
        if (data == null) {
            throw new BusinessRuntimeException(action + "失败：返回数据为空");
        }
        if (data instanceof Number number) {
            return number.intValue();
        }
        throw new BusinessRuntimeException(action + "失败：返回数据格式不正确");
    }

    private boolean isDirectOnline(Device device) {
        if (device == null || device.getLastOnlineAt() == null) {
            return false;
        }
        if (!isWithinOnlineWindow(device.getLastOnlineAt(), LocalDateTime.now())) {
            return false;
        }
        return true;
    }

    private boolean isWithinOnlineWindow(LocalDateTime lastOnlineAt, LocalDateTime now) {
        long timeoutSeconds = onlineProperties.getTimeoutSeconds();
        if (timeoutSeconds <= 0) {
            return true;
        }
        long seconds = Duration.between(lastOnlineAt, now).getSeconds();
        return seconds <= timeoutSeconds;
    }

    private boolean probeGatewayChildOnline(Device device) {
        Integer parentId = device.getParentId();
        if (parentId == null) {
            throw new BusinessRuntimeException("网关设备缺失");
        }
        Device gateway = deviceRegistry.getById(parentId);
        if (!isDirectOnline(gateway)) {
            return false;
        }
        sendAndAssertSuccess(device.getId(), new GetTotalEnergyCommand(), "在线探测");
        return true;
    }
}
