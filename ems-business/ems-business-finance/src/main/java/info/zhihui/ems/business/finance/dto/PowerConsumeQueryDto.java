package info.zhihui.ems.business.finance.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.checkerframework.checker.units.qual.N;

import java.time.LocalDateTime;

/**
 * 电量消费记录查询入参DTO
 *
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
public class PowerConsumeQueryDto {

    /**
     * 表名称模糊匹配（可选）
     */
    private String meterName;

    /**
     * 房间/空间名称模糊匹配（可选，直接匹配 space_name 字段）
     */
    private String spaceName;

    /**
     * 消费时间范围开始时间（可选）
     */
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime beginTime;

    /**
     * 消费时间范围结束时间（可选）
     */
    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;
}