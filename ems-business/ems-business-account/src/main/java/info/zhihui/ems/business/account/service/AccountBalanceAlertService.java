package info.zhihui.ems.business.account.service;

import info.zhihui.ems.mq.api.message.finance.BalanceChangedMessage;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * 余额预警服务
 *
 * @author jerryxiaosa
 */
public interface AccountBalanceAlertService {

    /**
     * 处理余额变更事件
     *
     * @param message 余额变更消息
     */
    void handleBalanceChange(@NotNull @Valid BalanceChangedMessage message);
}
