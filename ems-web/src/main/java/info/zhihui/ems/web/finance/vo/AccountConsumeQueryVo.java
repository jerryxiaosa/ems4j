package info.zhihui.ems.web.finance.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
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

    @Schema(description = "账户名称（模糊搜索）")
    @Size(max = 50, message = "账户名称长度不能超过50")
    private String accountNameLike;

    @Schema(description = "消费开始时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime consumeTimeStart;

    @Schema(description = "消费结束时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime consumeTimeEnd;
}
