package info.zhihui.ems.business.finance.dto;

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
     * 账户ID（必填）
     */
    private Integer accountId;

    /**
     * 消费开始时间（可选）
     */
    private LocalDateTime consumeTimeStart;

    /**
     * 消费结束时间（可选）
     */
    private LocalDateTime consumeTimeEnd;

    /**
     * 消费编号（可选，支持模糊查询）
     */
    private String consumeNo;

}