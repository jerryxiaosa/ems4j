package info.zhihui.ems.web.finance.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 电表计费查询VO
 */
@Data
@Accessors(chain = true)
public class MeterBillingQueryVo {

    @Schema(description = "搜索关键词（匹配电表名称/设备编号）")
    private String searchKey;

    @Schema(description = "空间名称（模糊匹配）")
    @Size(max = 100, message = "空间名称长度不能超过100")
    private String spaceNameLike;

    @Schema(description = "开始时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime beginTime;

    @Schema(description = "结束时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}
