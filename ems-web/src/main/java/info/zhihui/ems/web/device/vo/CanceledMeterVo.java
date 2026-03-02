package info.zhihui.ems.web.device.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 销户电表信息
 */
@Data
@Accessors(chain = true)
@Schema(name = "CanceledMeterVo", description = "销户电表信息")
public class CanceledMeterVo {

    @Schema(description = "空间名称")
    private String spaceName;

    @Schema(description = "空间父级名称列表（逗号分隔）")
    private String spaceParentNames;

    @Schema(description = "电表名称")
    private String meterName;

    @Schema(description = "电表编号")
    private String deviceNo;

    @Schema(description = "电表类型编码，参考 meterType")
    private Integer meterType;

    @Schema(description = "账户余额")
    private BigDecimal balance;
}
