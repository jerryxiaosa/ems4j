package info.zhihui.ems.business.finance.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 支付金额异常。
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PayAmountException extends RuntimeException {

    public PayAmountException(String message) {
        super(message);
    }
}
