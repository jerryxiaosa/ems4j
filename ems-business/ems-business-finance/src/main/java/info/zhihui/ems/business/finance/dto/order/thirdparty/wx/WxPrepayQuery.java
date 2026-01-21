package info.zhihui.ems.business.finance.dto.order.thirdparty.wx;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class WxPrepayQuery {
    private String outTradeNo;

    private String description;

    private LocalDateTime expireTime;

    private BigDecimal amount;

    private String openId;
}
