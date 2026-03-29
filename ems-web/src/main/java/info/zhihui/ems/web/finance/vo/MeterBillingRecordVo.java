package info.zhihui.ems.web.finance.vo;

import info.zhihui.ems.common.enums.ElectricAccountTypeEnum;
import info.zhihui.ems.common.enums.MeterTypeEnum;
import info.zhihui.ems.components.translate.annotation.EnumLabel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class MeterBillingRecordVo {

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
    private String deviceNo;

    @Schema(description = "空间名称")
    private String spaceName;

    @Schema(description = "起始余额")
    private BigDecimal beginBalance;

    @Schema(description = "消费金额")
    private BigDecimal consumeAmount;

    @Schema(description = "结束余额")
    private BigDecimal endBalance;

    @Schema(description = "电费计费类型编码")
    private Integer electricAccountType;

    @Schema(description = "电费计费类型说明")
    @EnumLabel(source = "electricAccountType", enumClass = ElectricAccountTypeEnum.class)
    private String electricAccountTypeText;

    @Schema(description = "表类型编码")
    private Integer meterType;

    @Schema(description = "表类型说明")
    @EnumLabel(source = "meterType", enumClass = MeterTypeEnum.class)
    private String meterTypeName;

    @Schema(description = "消费时间")
    private LocalDateTime consumeTime;
}
