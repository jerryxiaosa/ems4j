package info.zhihui.ems.business.account.service;

import info.zhihui.ems.mq.api.message.finance.BalanceChangedMessage;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * 电表余额变化服务
 *
 * @author jerryxiaosa
 */
public interface MeterBalanceChangeService {

    /**
     * 处理电表余额变化事件
     *
     * @param message 余额变化消息
     */
    void handleBalanceChange(@NotNull @Valid BalanceChangedMessage message);
}
