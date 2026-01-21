package info.zhihui.ems.business.finance.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 补正记录查询条件
 */
@Data
@Accessors(chain = true)
public class MeterCorrectionRecordQueryDto {

    private Integer accountId;

    private Integer meterId;

    private String meterName;

    @NotNull(message = "开始时间不能为空")
    private LocalDateTime beginTime;

    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;
}
