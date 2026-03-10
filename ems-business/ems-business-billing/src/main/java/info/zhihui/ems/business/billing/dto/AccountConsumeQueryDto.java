package info.zhihui.ems.business.billing.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 包月消费分页查询请求DTO
 *
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
public class AccountConsumeQueryDto {

    /**
     * 账户名称（模糊搜索，可选）
     */
    private String accountNameLike;

    /**
     * 消费开始时间（可选）
     */
    private LocalDateTime consumeTimeStart;

    /**
     * 消费结束时间（可选）
     */
    private LocalDateTime consumeTimeEnd;
}
