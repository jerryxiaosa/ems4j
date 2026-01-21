package info.zhihui.ems.mq.api.constant.finance;

/**
 * 财务类 MQ 常量
 *
 * @author jerryxiaosa
 */
public final class FinanceMqConstant {

    private FinanceMqConstant() {
    }

    public static final String FINANCE_DESTINATION = "exchange.finance";

    public static final String ROUTING_KEY_BALANCE_CHANGED = "finance.balance.changed";
}
