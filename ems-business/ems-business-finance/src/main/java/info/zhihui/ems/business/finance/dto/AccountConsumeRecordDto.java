package info.zhihui.ems.business.finance.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 包月消费记录响应DTO
 *
 * @author jerryxiaosa
 */
@Data
@Accessors(chain = true)
public class AccountConsumeRecordDto {

    /**
     * 记录ID
     */
    private Integer id;

    /**
     * 账户ID
     */
    private Integer accountId;

    /**
     * 消费编号
     */
    private String consumeNo;

    /**
     * 支付金额
     */
    private BigDecimal payAmount;

    /**
     * 开始余额
     */
    private BigDecimal beginBalance;

    /**
     * 结束余额
     */
    private BigDecimal endBalance;

    /**
     * 消费时间
     */
    private LocalDateTime consumeTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}