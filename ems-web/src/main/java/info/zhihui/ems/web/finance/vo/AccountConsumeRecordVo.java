package info.zhihui.ems.web.finance.vo;

import info.zhihui.ems.business.finance.enums.ConsumeTypeEnum;
import info.zhihui.ems.common.enums.OwnerTypeEnum;
import info.zhihui.ems.components.translate.annotation.EnumLabel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class AccountConsumeRecordVo {

    @Schema(description = "记录ID")
    private Integer id;

    @Schema(description = "账户ID")
    private Integer accountId;

    @Schema(description = "归属主体名称")
    private String ownerName;

    @Schema(description = "归属主体类型编码，参考 ownerType")
    private Integer ownerType;

    @Schema(description = "归属主体类型名称")
    @EnumLabel(source = "ownerType", enumClass = OwnerTypeEnum.class)
    private String ownerTypeName;

    @Schema(description = "消费类型编码，固定为包月消费")
    private Integer consumeType;

    @Schema(description = "消费类型名称")
    @EnumLabel(source = "consumeType", enumClass = ConsumeTypeEnum.class)
    private String consumeTypeName;

    @Schema(description = "联系人姓名")
    private String contactName;

    @Schema(description = "联系人手机号")
    private String contactPhone;

    @Schema(description = "消费编号")
    private String consumeNo;

    @Schema(description = "支付金额")
    private BigDecimal payAmount;

    @Schema(description = "开始余额")
    private BigDecimal beginBalance;

    @Schema(description = "结束余额")
    private BigDecimal endBalance;

    @Schema(description = "消费时间")
    private LocalDateTime consumeTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
