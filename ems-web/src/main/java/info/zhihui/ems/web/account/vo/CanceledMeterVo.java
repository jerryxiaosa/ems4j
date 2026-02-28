package info.zhihui.ems.web.account.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 销户表计明细 VO
 */
@Data
@Accessors(chain = true)
@Schema(name = "CanceledMeterVo", description = "销户表计明细")
public class CanceledMeterVo {

    @Schema(description = "空间名称")
    private String spaceName;

    @Schema(description = "父空间名称，逗号分隔")
    private String spaceParentNames;

    @Schema(description = "表名称")
    private String meterName;

    @Schema(description = "表具号")
    private String meterNo;

    @Schema(description = "表类型编码，参考 meterType")
    private Integer meterType;

    @Schema(description = "表余额")
    private BigDecimal balance;

    @Schema(description = "总电量")
    private BigDecimal power;

    @Schema(description = "尖电量")
    private BigDecimal powerHigher;

    @Schema(description = "峰电量")
    private BigDecimal powerHigh;

    @Schema(description = "平电量")
    private BigDecimal powerLow;

    @Schema(description = "谷电量")
    private BigDecimal powerLower;

    @Schema(description = "深谷电量")
    private BigDecimal powerDeepLow;

    @Schema(description = "读表时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime showTime;

    @Schema(description = "历史总用量（用于重开户继承）")
    private BigDecimal historyPowerTotal;
}
