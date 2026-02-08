package info.zhihui.ems.foundation.integration.concrete.energy.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import info.zhihui.ems.common.enums.ElectricPricePeriodEnum;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.model.energy.DailyEnergySlot;
import info.zhihui.ems.common.model.energy.DatePlanItem;
import info.zhihui.ems.common.utils.JacksonUtil;
import info.zhihui.ems.common.vo.RestResult;
import info.zhihui.ems.foundation.integration.concrete.energy.dto.*;
import info.zhihui.ems.foundation.integration.concrete.energy.service.EnergyService;
import info.zhihui.ems.foundation.integration.core.enums.ModuleEnum;
import info.zhihui.ems.foundation.integration.core.service.DeviceModuleConfigService;
import info.zhihui.ems.foundation.integration.concrete.energy.dto.platform.DefaultIotHttpRequestConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalTime;
import java.time.MonthDay;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author jerryxiaosa
 */
@Service
public class DefaultEnergyServiceImpl implements EnergyService {

    private static final Duration HTTP_TIMEOUT = Duration.ofSeconds(5);
    private final DeviceModuleConfigService deviceModuleConfigService;
    private final RestClient restClient;

    public DefaultEnergyServiceImpl(DeviceModuleConfigService deviceModuleConfigService) {
        this.deviceModuleConfigService = deviceModuleConfigService;
        int timeoutMillis = Math.toIntExact(HTTP_TIMEOUT.toMillis());
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(timeoutMillis);
        requestFactory.setReadTimeout(timeoutMillis);
        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * 新增 IoT 设备并返回 IoT 设备 ID。
     */
    @Override
    public Integer addDevice(ElectricDeviceAddDto addDto) {
        Integer areaId = requireAreaId(addDto.getAreaId());
        DeviceSaveRequest body = toDeviceRequest(
                addDto.getDeviceNo(),
                addDto.getPortNo(),
                addDto.getMeterAddress(),
                addDto.getDeviceSecret(),
                addDto.getSlaveAddress(),
                addDto.getProductCode(),
                addDto.getParentId()
        );
        RestResult<Integer> response = executeIotCall("新增设备", () -> restClient.post()
                .uri(buildUrl(areaId, "/api/devices"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                }));
        Integer iotId = response.getData();
        if (iotId == null) {
            throw new BusinessRuntimeException("新增设备失败：IoT设备ID为空");
        }
        return iotId;
    }

    /**
     * 更新 IoT 设备信息，成功后返回原设备 ID。
     */
    @Override
    public Integer editDevice(ElectricDeviceUpdateDto updateDto) {
        Integer areaId = requireAreaId(updateDto.getAreaId());
        Integer deviceId = requireDeviceId(updateDto.getDeviceId());
        DeviceSaveRequest body = toDeviceRequest(
                updateDto.getDeviceNo(),
                updateDto.getPortNo(),
                updateDto.getMeterAddress(),
                updateDto.getDeviceSecret(),
                updateDto.getSlaveAddress(),
                updateDto.getProductCode(),
                updateDto.getParentId()
        );
        executeIotCall("更新设备", () -> restClient.put()
                .uri(buildUrl(areaId, "/api/devices/" + deviceId))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                }));
        return deviceId;
    }

    /**
     * 删除 IoT 设备。
     */
    @Override
    public void delDevice(BaseElectricDeviceDto deleteDto) {
        Integer areaId = requireAreaId(deleteDto.getAreaId());
        Integer deviceId = requireDeviceId(deleteDto.getDeviceId());
        executeIotCall("删除设备", () -> restClient.delete()
                .uri(buildUrl(areaId, "/api/devices/" + deviceId))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                }));
    }

    /**
     * 下发拉闸命令。
     */
    @Override
    public void cutOff(BaseElectricDeviceDto cutOffDto) {
        Integer areaId = requireAreaId(cutOffDto.getAreaId());
        Integer deviceId = requireDeviceId(cutOffDto.getDeviceId());
        executeIotCall("下发拉闸命令", () -> restClient.post()
                .uri(buildUrl(areaId, "/api/commands/" + deviceId + "/cut-off"))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                }));
    }

    /**
     * 下发合闸命令。
     */
    @Override
    public void recover(BaseElectricDeviceDto recoverDto) {
        Integer areaId = requireAreaId(recoverDto.getAreaId());
        Integer deviceId = requireDeviceId(recoverDto.getDeviceId());
        executeIotCall("下发合闸命令", () -> restClient.post()
                .uri(buildUrl(areaId, "/api/commands/" + deviceId + "/recover"))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                }));
    }

    /**
     * 下发日时段电价方案。
     */
    @Override
    public void setDuration(DailyEnergyPlanUpdateDto durationUpdateDto) {
        Integer areaId = requireAreaId(durationUpdateDto.getAreaId());
        Integer deviceId = requireDeviceId(durationUpdateDto.getDeviceId());
        if (durationUpdateDto.getDailyPlanId() == null) {
            throw new BusinessRuntimeException("日方案编号不能为空");
        }
        List<DailyEnergySlot> slots = durationUpdateDto.getSlots();
        if (slots == null || slots.isEmpty()) {
            throw new BusinessRuntimeException("日时段方案不能为空");
        }
        ElectricDurationUpdateRequest body = new ElectricDurationUpdateRequest(
                durationUpdateDto.getDailyPlanId(),
                toDurationRequestItems(slots)
        );
        executeIotCall("下发日时段电价方案", () -> restClient.post()
                .uri(buildUrl(areaId, "/api/commands/" + deviceId + "/duration"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                }));
    }

    /**
     * 读取日时段电价方案并转换为通用时段模型。
     */
    @Override
    public List<DailyEnergySlot> getDuration(DailyEnergyPlanQueryDto durationQueryDto) {
        Integer areaId = requireAreaId(durationQueryDto.getAreaId());
        Integer deviceId = requireDeviceId(durationQueryDto.getDeviceId());
        if (durationQueryDto.getDailyPlanId() == null) {
            throw new BusinessRuntimeException("日方案编号不能为空");
        }
        String path = UriComponentsBuilder.fromPath("/api/commands/{deviceId}/duration")
                .queryParam("dailyPlanId", durationQueryDto.getDailyPlanId())
                .buildAndExpand(deviceId)
                .toUriString();
        RestResult<List<ElectricDurationPayload>> response = executeIotCall("读取日时段电价方案", () -> restClient.get()
                .uri(buildUrl(areaId, path))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                }));
        return toDailyEnergySlots(response.getData());
    }

    /**
     * 下发指定日期电价方案。
     */
    @Override
    public void setDateDuration(DateEnergyPlanUpdateDto dateDurationUpdateDto) {
        Integer areaId = requireAreaId(dateDurationUpdateDto.getAreaId());
        Integer deviceId = requireDeviceId(dateDurationUpdateDto.getDeviceId());
        List<DatePlanItem> items = dateDurationUpdateDto.getItems();
        if (items == null || items.isEmpty()) {
            throw new BusinessRuntimeException("日期电价方案不能为空");
        }
        List<ElectricDateDurationPayload> body = toDateDurationRequestItems(items);
        executeIotCall("下发指定日期电价方案", () -> restClient.post()
                .uri(buildUrl(areaId, "/api/commands/" + deviceId + "/date-duration"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                }));
    }

    /**
     * 读取指定日期电价方案并转换为通用日期模型。
     */
    @Override
    public List<DatePlanItem> getDateDuration(BaseElectricDeviceDto deviceDto) {
        Integer areaId = requireAreaId(deviceDto.getAreaId());
        Integer deviceId = requireDeviceId(deviceDto.getDeviceId());
        RestResult<List<ElectricDateDurationPayload>> response = executeIotCall("读取指定日期电价方案", () -> restClient.get()
                .uri(buildUrl(areaId, "/api/commands/" + deviceId + "/date-duration"))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                }));
        return toDatePlanItems(response.getData());
    }

    /**
     * 读取分时电量。
     */
    @Override
    public BigDecimal getMeterEnergy(ElectricDeviceDegreeDto degreeDto) {
        Integer areaId = requireAreaId(degreeDto.getAreaId());
        Integer deviceId = requireDeviceId(degreeDto.getDeviceId());
        UriComponentsBuilder pathBuilder = UriComponentsBuilder.fromPath("/api/commands/{deviceId}/used-power");
        ElectricPricePeriodEnum type = degreeDto.getType();
        if (type != null) {
            pathBuilder.queryParam("type", type.getCode());
        }
        String path = pathBuilder.buildAndExpand(deviceId).toUriString();
        RestResult<BigDecimal> response = executeIotCall("读取分时电量", () -> restClient.get()
                .uri(buildUrl(areaId, path))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                }));
        BigDecimal energy = response.getData();
        if (energy == null) {
            throw new BusinessRuntimeException("读取分时电量失败：响应数据为空");
        }
        return energy;
    }

    /**
     * 查询设备在线状态。
     */
    @Override
    public Boolean isOnline(BaseElectricDeviceDto deviceDto) {
        Integer areaId = requireAreaId(deviceDto.getAreaId());
        Integer deviceId = requireDeviceId(deviceDto.getDeviceId());
        RestResult<Boolean> response = executeIotCall("查询设备在线状态", () -> restClient.get()
                .uri(buildUrl(areaId, "/api/devices/" + deviceId + "/online"))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                }));
        Boolean online = response.getData();
        if (online == null) {
            throw new BusinessRuntimeException("查询设备在线状态失败：响应数据为空");
        }
        return online;
    }

    /**
     * 下发 CT 倍率。
     */
    @Override
    public void setElectricCt(ElectricDeviceCTDto ctDto) {
        Integer areaId = requireAreaId(ctDto.getAreaId());
        Integer deviceId = requireDeviceId(ctDto.getDeviceId());
        Integer ct = ctDto.getCt();
        if (ct == null || ct <= 0) {
            throw new BusinessRuntimeException("CT变比必须大于0");
        }
        String path = UriComponentsBuilder.fromPath("/api/commands/{deviceId}/ct")
                .queryParam("ct", ct)
                .buildAndExpand(deviceId)
                .toUriString();
        executeIotCall("下发CT倍率", () -> restClient.post()
                .uri(buildUrl(areaId, path))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                }));
    }

    /**
     * 读取 CT 倍率。
     */
    @Override
    public Integer getElectricCt(BaseElectricDeviceDto deviceDto) {
        Integer areaId = requireAreaId(deviceDto.getAreaId());
        Integer deviceId = requireDeviceId(deviceDto.getDeviceId());
        RestResult<Integer> response = executeIotCall("读取CT倍率", () -> restClient.get()
                .uri(buildUrl(areaId, "/api/commands/" + deviceId + "/ct"))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                }));
        Integer ct = response.getData();
        if (ct == null) {
            throw new BusinessRuntimeException("读取CT倍率失败：响应数据为空");
        }
        return ct;
    }

    /**
     * 当前服务对应能源模块。
     */
    @Override
    public ModuleEnum getModuleName() {
        return ModuleEnum.ENERGY;
    }

    /**
     * 构建设备新增/更新请求体。
     */
    private DeviceSaveRequest toDeviceRequest(String deviceNo, Integer portNo, Integer meterAddress,
                                              String deviceSecret, Integer slaveAddress,
                                              String productCode, Integer parentId) {
        return new DeviceSaveRequest(deviceNo, portNo, meterAddress, deviceSecret, slaveAddress, productCode, parentId);
    }

    /**
     * 将通用时段模型转换为 IoT 请求项。
     */
    private List<ElectricDurationPayload> toDurationRequestItems(List<DailyEnergySlot> slots) {
        List<ElectricDurationPayload> items = new ArrayList<>(slots.size());
        for (DailyEnergySlot slot : slots) {
            if (slot == null || slot.getPeriod() == null || slot.getTime() == null) {
                throw new BusinessRuntimeException("日时段方案数据不完整");
            }
            items.add(new ElectricDurationPayload(
                    slot.getPeriod().getCode(),
                    twoDigits(slot.getTime().getHour()),
                    twoDigits(slot.getTime().getMinute())
            ));
        }
        return items;
    }

    /**
     * 将 IoT 日时段响应转换为通用时段模型。
     */
    private List<DailyEnergySlot> toDailyEnergySlots(List<ElectricDurationPayload> responseItems) {
        if (responseItems == null || responseItems.isEmpty()) {
            return List.of();
        }
        List<DailyEnergySlot> slots = new ArrayList<>(responseItems.size());
        for (ElectricDurationPayload item : responseItems) {
            if (item == null || item.period() == null) {
                throw new BusinessRuntimeException("读取日时段电价方案失败：返回数据格式不正确");
            }
            int periodCode = item.period();
            int hour = parseIntegerField(item.hour(), "hour", "读取日时段电价方案");
            int minute = parseIntegerField(item.min(), "min", "读取日时段电价方案");
            try {
                slots.add(new DailyEnergySlot()
                        .setPeriod(ElectricPricePeriodEnum.fromCode(periodCode))
                        .setTime(LocalTime.of(hour, minute)));
            } catch (RuntimeException ex) {
                throw new BusinessRuntimeException("读取日时段电价方案失败：返回数据格式不正确");
            }
        }
        return slots;
    }

    /**
     * 将通用日期方案模型转换为 IoT 请求项。
     */
    private List<ElectricDateDurationPayload> toDateDurationRequestItems(List<DatePlanItem> items) {
        List<ElectricDateDurationPayload> requestItems = new ArrayList<>(items.size());
        for (DatePlanItem item : items) {
            if (item == null || item.getDate() == null || StringUtils.isBlank(item.getDailyPlanId())) {
                throw new BusinessRuntimeException("日期电价方案数据不完整");
            }
            requestItems.add(new ElectricDateDurationPayload(
                    String.valueOf(item.getDate().getMonthValue()),
                    String.valueOf(item.getDate().getDayOfMonth()),
                    item.getDailyPlanId()
            ));
        }
        return requestItems;
    }

    /**
     * 将 IoT 日期方案响应转换为通用日期模型。
     */
    private List<DatePlanItem> toDatePlanItems(List<ElectricDateDurationPayload> responseItems) {
        if (responseItems == null || responseItems.isEmpty()) {
            return List.of();
        }
        List<DatePlanItem> items = new ArrayList<>(responseItems.size());
        for (ElectricDateDurationPayload item : responseItems) {
            if (item == null) {
                throw new BusinessRuntimeException("读取指定日期电价方案失败：返回数据格式不正确");
            }
            int month = parseIntegerField(item.month(), "month", "读取指定日期电价方案");
            int day = parseIntegerField(item.day(), "day", "读取指定日期电价方案");
            if (StringUtils.isBlank(item.dailyPlanId())) {
                throw new BusinessRuntimeException("读取指定日期电价方案失败：dailyPlanId为空");
            }
            try {
                items.add(new DatePlanItem()
                        .setDate(MonthDay.of(month, day))
                        .setDailyPlanId(item.dailyPlanId()));
            } catch (RuntimeException ex) {
                throw new BusinessRuntimeException("读取指定日期电价方案失败：返回数据格式不正确");
            }
        }
        return items;
    }

    /**
     * 解析字符串整数字段并输出统一业务异常。
     */
    private int parseIntegerField(String value, String fieldName, String action) {
        if (StringUtils.isBlank(value)) {
            throw new BusinessRuntimeException(action + "失败：" + fieldName + "为空");
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new BusinessRuntimeException(action + "失败：" + fieldName + "格式不正确");
        }
    }

    /**
     * 将数字格式化为两位字符串（不足补零）。
     */
    private String twoDigits(int value) {
        return String.format("%02d", value);
    }

    /**
     * 执行 IoT 请求并统一处理 HTTP 层异常。
     */
    private <T> RestResult<T> executeIotCall(String action, Supplier<RestResult<T>> requestSupplier) {
        try {
            return assertSuccess(action, requestSupplier.get());
        } catch (RestClientResponseException ex) {
            throw new BusinessRuntimeException(action + "失败：" + resolveHttpErrorMessage(ex));
        } catch (RestClientException ex) {
            throw new BusinessRuntimeException(action + "失败：调用IoT接口异常");
        }
    }

    /**
     * 统一校验 IoT 响应对象是否成功。
     */
    private <T> RestResult<T> assertSuccess(String action, RestResult<T> response) {
        if (response == null) {
            throw new BusinessRuntimeException(action + "失败：响应解析异常");
        }
        if (!Boolean.TRUE.equals(response.getSuccess())) {
            String message = StringUtils.defaultIfBlank(response.getMessage(), "IoT返回失败");
            throw new BusinessRuntimeException(action + "失败：" + message);
        }
        return response;
    }

    /**
     * 解析 HTTP 异常响应中的业务错误信息。
     */
    private String resolveHttpErrorMessage(RestClientResponseException ex) {
        RestResult<?> errorResult = tryParseErrorBody(ex.getResponseBodyAsString());
        if (errorResult != null && StringUtils.isNotBlank(errorResult.getMessage())) {
            return errorResult.getMessage();
        }
        return "HTTP状态码:" + ex.getStatusCode().value();
    }

    /**
     * 尝试将错误响应体反序列化为统一返回结构。
     */
    private RestResult<?> tryParseErrorBody(String body) {
        if (StringUtils.isBlank(body)) {
            return null;
        }
        try {
            return JacksonUtil.fromJson(body, new TypeReference<>() {
            });
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * 拼接 IoT 完整请求地址。
     */
    private String buildUrl(Integer areaId, String path) {
        String addressUrl = getAddressUrl(areaId);
        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        return addressUrl + normalizedPath;
    }

    /**
     * 根据区域读取 IoT 服务地址并做规范化处理。
     */
    private String getAddressUrl(Integer areaId) {
        Integer validatedAreaId = requireAreaId(areaId);
        DefaultIotHttpRequestConfig configValue = deviceModuleConfigService
                .getDeviceConfigValue(EnergyService.class, DefaultIotHttpRequestConfig.class, validatedAreaId);
        String addressUrl = configValue.getAddressUrl();
        if (StringUtils.isBlank(addressUrl)) {
            throw new BusinessRuntimeException("设备配置缺少addressUrl");
        }
        String trimmedAddressUrl = addressUrl.trim();
        if (trimmedAddressUrl.endsWith("/")) {
            return trimmedAddressUrl.substring(0, trimmedAddressUrl.length() - 1);
        }
        return trimmedAddressUrl;
    }

    /**
     * 校验并返回区域 ID。
     */
    private Integer requireAreaId(Integer areaId) {
        if (areaId == null) {
            throw new BusinessRuntimeException("区域ID不能为空");
        }
        return areaId;
    }

    /**
     * 校验并返回设备 ID。
     */
    private Integer requireDeviceId(Integer deviceId) {
        if (deviceId == null) {
            throw new BusinessRuntimeException("设备ID不能为空");
        }
        return deviceId;
    }

    private record DeviceSaveRequest(String deviceNo, Integer portNo, Integer meterAddress,
                                     String deviceSecret, Integer slaveAddress,
                                     String productCode, Integer parentId) {
    }

    private record ElectricDurationUpdateRequest(Integer dailyPlanId, List<ElectricDurationPayload> electricDurations) {
    }

    private record ElectricDurationPayload(Integer period, String hour, String min) {
    }

    private record ElectricDateDurationPayload(String month, String day, String dailyPlanId) {
    }
}
