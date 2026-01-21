package info.zhihui.ems.web.plan.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import info.zhihui.ems.common.serializer.BigDecimalScaleSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预警方案信息
 */
@Data
@Accessors(chain = true)
@Schema(name = "WarnPlanVo", description = "预警方案信息")
public class WarnPlanVo {

    @Schema(description = "方案ID")
    private Integer id;

    @Schema(description = "方案名称")
    private String name;

    @Schema(description = "第一阶段预警余额")
    @JsonSerialize(using = BigDecimalScaleSerializer.class)
    private BigDecimal firstLevel;

    @Schema(description = "第二阶段预警余额")
    @JsonSerialize(using = BigDecimalScaleSerializer.class)
    private BigDecimal secondLevel;

    @Schema(description = "欠费是否自动断闸")
    private Boolean autoClose;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建人ID")
    private Integer createUser;

    @Schema(description = "创建人名称")
    private String createUserName;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新人ID")
    private Integer updateUser;

    @Schema(description = "更新人名称")
    private String updateUserName;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
