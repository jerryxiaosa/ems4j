package info.zhihui.ems.web.finance.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 包月消费查询VO
 */
@Data
@Accessors(chain = true)
public class AccountConsumeQueryVo {

    @Schema(description = "账户ID")
    private Integer accountId;

    @Schema(description = "消费开始时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime consumeTimeStart;

    @Schema(description = "消费结束时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime consumeTimeEnd;

    @Schema(description = "消费编号（模糊查询）")
    private String consumeNo;
}
