package info.zhihui.ems.mq.api.message.device;

import info.zhihui.ems.mq.api.message.BaseMessage;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 标准电量上报消息
 *
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class StandardEnergyReportMessage extends BaseMessage {

    private String source;

    @NotBlank(message = "来源流水号不能为空")
    private String sourceReportId;

    @NotBlank(message = "设备编号不能为空")
    private String deviceNo;

    @NotNull(message = "抄表时间不能为空")
    private LocalDateTime recordTime;

    @NotNull(message = "总电量不能为空")
    @DecimalMin(value = "0", message = "总电量不能小于0")
    private BigDecimal totalEnergy;

    @NotNull(message = "尖电量不能为空")
    @DecimalMin(value = "0", message = "尖电量不能小于0")
    private BigDecimal higherEnergy;

    @NotNull(message = "峰电量不能为空")
    @DecimalMin(value = "0", message = "峰电量不能小于0")
    private BigDecimal highEnergy;

    @NotNull(message = "平电量不能为空")
    @DecimalMin(value = "0", message = "平电量不能小于0")
    private BigDecimal lowEnergy;

    @NotNull(message = "谷电量不能为空")
    @DecimalMin(value = "0", message = "谷电量不能小于0")
    private BigDecimal lowerEnergy;

    @NotNull(message = "深谷电量不能为空")
    @DecimalMin(value = "0", message = "深谷电量不能小于0")
    private BigDecimal deepLowEnergy;
}
