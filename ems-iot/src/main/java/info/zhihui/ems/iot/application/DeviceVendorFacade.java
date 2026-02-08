package info.zhihui.ems.iot.application;

import info.zhihui.ems.common.enums.ElectricPricePeriodEnum;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.model.energy.DailyEnergySlot;
import info.zhihui.ems.common.model.energy.DatePlanItem;
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
import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
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

    public List<ElectricDurationVo> getDuration(Integer deviceId, Integer dailyPlanId) {
        GetDailyEnergyPlanCommand command = new GetDailyEnergyPlanCommand().setDailyPlanId(dailyPlanId);
        DeviceCommandResult result = sendAndAssertSuccess(deviceId, command, "时段电价读取");
        return parseDurationResult(result, "时段电价读取");
    }

    public void setDuration(Integer deviceId, ElectricDurationUpdateVo dto) {
        List<ElectricDurationVo> copy = new ArrayList<>(dto.getElectricDurations());
        SetDailyEnergyPlanCommand command = new SetDailyEnergyPlanCommand()
                .setDailyPlanId(dto.getDailyPlanId())
                .setSlots(toDailySlots(copy));
        sendAndAssertSuccess(deviceId, command, "时段电价下发");
    }

    public List<ElectricDateDurationVo> getDateDuration(Integer deviceId) {
        GetDatePlanCommand command = new GetDatePlanCommand().setPlan(1);
        DeviceCommandResult result = sendAndAssertSuccess(deviceId, command, "日期电价读取");
        return parseDatePlanResult(result, "日期电价读取");
    }

    public void setDateDuration(Integer deviceId, List<ElectricDateDurationVo> dto) {
        List<ElectricDateDurationVo> copy = new ArrayList<>(dto);
        SetDatePlanCommand command = new SetDatePlanCommand()
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
        // 返回的是单位为瓦的整数，需要转换成瓦的浮点数
        return BigDecimal.valueOf(energyValue)
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
                    .setPeriod(parsePeriod(duration.getPeriod()))
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
                    .setDate(parseMonthDay(dto.getMonth(), dto.getDay()))
                    .setDailyPlanId(dto.getDailyPlanId()));
        }
        return items;
    }

    private ElectricPricePeriodEnum parsePeriod(Integer code) {
        Integer value = requireNumber(code);
        return ElectricPricePeriodEnum.fromCode(value);
    }

    private LocalTime parseTime(String hour, String minute) {
        return LocalTime.of(parseNumber(hour), parseNumber(minute));
    }

    private MonthDay parseMonthDay(String month, String day) {
        int monthValue = parseNumber(month);
        int dayValue = parseNumber(day);
        try {
            return MonthDay.of(monthValue, dayValue);
        } catch (DateTimeException ex) {
            throw new IllegalArgumentException("日期不正确");
        }
    }

    private Integer requireNumber(Integer value) {
        if (value == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        return value;
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
        if (data instanceof Number number && number.intValue() >= 0) {
            return number.intValue();
        }
        throw new BusinessRuntimeException(action + "失败：返回数据格式不正确");
    }

    private List<ElectricDurationVo> parseDurationResult(DeviceCommandResult result, String action) {
        List<DailyEnergySlot> slots = castDailySlots(result.getData(), action);

        List<ElectricDurationVo> durations = new ArrayList<>(slots.size());
        for (DailyEnergySlot slot : slots) {
            ElectricPricePeriodEnum period = slot.getPeriod();
            LocalTime time = slot.getTime();
            if (period == null || time == null) {
                throw new BusinessRuntimeException(action + "失败：返回数据格式不正确");
            }
            durations.add(new ElectricDurationVo()
                    .setPeriod(period.getCode())
                    .setHour(formatTwoDigits(time.getHour()))
                    .setMin(formatTwoDigits(time.getMinute())));
        }
        return durations;
    }

    private List<DailyEnergySlot> castDailySlots(Object data, String action) {
        if (!(data instanceof List<?> list)) {
            throw new BusinessRuntimeException(action + "失败：返回数据格式不正确");
        }

        List<DailyEnergySlot> slots = new ArrayList<>();
        for (Object item : list) {
            if (!(item instanceof DailyEnergySlot slot)) {
                throw new BusinessRuntimeException(action + "失败：返回数据格式不正确");
            }
            slots.add(slot);
        }
        return slots;
    }

    private List<ElectricDateDurationVo> parseDatePlanResult(DeviceCommandResult result, String action) {
        List<DatePlanItem> items = castDatePlanItems(result.getData(), action);
        if (items.isEmpty()) {
            return List.of();
        }
        List<ElectricDateDurationVo> vos = new ArrayList<>(items.size());
        for (DatePlanItem item : items) {
            if (item.getDate() == null || item.getDailyPlanId() == null) {
                throw new BusinessRuntimeException(action + "失败：返回数据格式不正确");
            }
            MonthDay date = item.getDate();
            vos.add(new ElectricDateDurationVo()
                    .setMonth(String.valueOf(date.getMonthValue()))
                    .setDay(String.valueOf(date.getDayOfMonth()))
                    .setDailyPlanId(item.getDailyPlanId()));
        }
        return vos;
    }

    private List<DatePlanItem> castDatePlanItems(Object data, String action) {
        if (!(data instanceof List<?> list)) {
            throw new BusinessRuntimeException(action + "失败：返回数据格式不正确");
        }
        List<DatePlanItem> items = new ArrayList<>();
        for (Object item : list) {
            if (!(item instanceof DatePlanItem planItem)) {
                throw new BusinessRuntimeException(action + "失败：返回数据格式不正确");
            }
            items.add(planItem);
        }
        return items;
    }

    private String formatTwoDigits(int value) {
        return String.format("%02d", value);
    }

    private boolean isDirectOnline(Device device) {
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
        if (lastOnlineAt == null || now == null) {
            return false;
        }
        if (lastOnlineAt.isAfter(now)) {
            log.warn("设备最近在线时间在未来，lastOnlineAt={}, now={}", lastOnlineAt, now);
            return false;
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
