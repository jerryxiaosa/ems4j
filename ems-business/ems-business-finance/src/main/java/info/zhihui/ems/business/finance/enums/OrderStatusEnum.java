package info.zhihui.ems.business.finance.enums;

public enum OrderStatusEnum {
    NOT_PAY,
    SUCCESS,
    CLOSED,
    PAY_ERROR, // 支付金额不一致也设置为PAY_ERROR
    REFUND_PROCESSING, // 退款申请中
    FULL_REFUND, // 全额退款
    REFUND_CLOSED, // 退款关闭
    REFUND_ERROR // 退款异常
}
