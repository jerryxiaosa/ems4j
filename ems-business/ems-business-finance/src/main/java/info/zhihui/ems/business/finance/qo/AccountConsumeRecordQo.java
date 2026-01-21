package info.zhihui.ems.business.finance.qo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 账户消费记录查询对象QO
 *
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
public class AccountConsumeRecordQo {

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
     * 消费编号模糊匹配（可选）
     */
    private String consumeNoLike;

}