package info.zhihui.ems.web.finance.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class PowerConsumeRecordVo {

    @Schema(description = "记录ID")
    private Integer id;

    @Schema(description = "账户ID")
    private Integer accountId;

    @Schema(description = "电表ID")
    private Integer meterId;

    @Schema(description = "消费编号")
    private String consumeNo;

    @Schema(description = "归属者ID")
    private Integer ownerId;

    @Schema(description = "归属者类型")
    private Integer ownerType;

    @Schema(description = "归属者名称")
    private String ownerName;

    @Schema(description = "电表名称")
    private String meterName;

    @Schema(description = "电表编号")
    private String meterNo;

    @Schema(description = "空间名称")
    private String spaceName;

    @Schema(description = "起始余额")
    private BigDecimal beginBalance;

    @Schema(description = "消费金额")
    private BigDecimal consumeAmount;

    @Schema(description = "结束余额")
    private BigDecimal endBalance;

    @Schema(description = "是否合并计量")
    private Boolean mergedMeasure;

    @Schema(description = "消费时间")
    private LocalDateTime consumeTime;
}
