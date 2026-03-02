package info.zhihui.ems.web.finance.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class CorrectionRecordVo {

    @Schema(description = "消费单号")
    private String consumeNo;

    private Integer accountId;

    private Integer ownerId;

    private Integer ownerType;

    private String ownerName;

    private Integer meterId;

    private String meterName;

    private String deviceNo;

    private BigDecimal consumeAmount;

    private BigDecimal beginBalance;

    private BigDecimal endBalance;

    private String remark;

    private LocalDateTime meterConsumeTime;
}
