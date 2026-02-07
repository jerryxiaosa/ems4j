package info.zhihui.ems.mq.api.enums;

import info.zhihui.ems.common.exception.BusinessRuntimeException;

/**
 * 事务消息业务类型枚举
 */
public enum TransactionMessageBusinessTypeEnum {
    ORDER_PAYMENT
    ;

    public static TransactionMessageBusinessTypeEnum getByName(String name) {
        for (TransactionMessageBusinessTypeEnum item : TransactionMessageBusinessTypeEnum.values()) {
            if (item.name().equals(name)) {
                return item;
            }
        }
        throw new BusinessRuntimeException("未知事务消息业务类型: " + name);
    }
}
