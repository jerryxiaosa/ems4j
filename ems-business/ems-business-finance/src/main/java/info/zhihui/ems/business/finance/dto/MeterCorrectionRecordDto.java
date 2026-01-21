package info.zhihui.ems.business.finance.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 补正记录展示对象
 */
@Data
@Accessors(chain = true)
public class MeterCorrectionRecordDto {

    private String consumeNo;

    private Integer accountId;

    private Integer ownerId;

    private Integer ownerType;

    private String ownerName;

    private Integer meterId;

    private String meterName;

    private String meterNo;

    private BigDecimal consumeAmount;

    private BigDecimal beginBalance;

    private BigDecimal endBalance;

    private String remark;

    private LocalDateTime meterConsumeTime;
}
