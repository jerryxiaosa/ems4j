package info.zhihui.ems.business.finance.service.balance;

import info.zhihui.ems.business.finance.bo.BalanceBo;
import info.zhihui.ems.business.finance.dto.BalanceDeleteDto;
import info.zhihui.ems.business.finance.dto.BalanceQueryDto;
import info.zhihui.ems.business.finance.dto.BalanceDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * 账户余额操作接口
 *
 * @author jerryxiaosa
 */
public interface BalanceService {

    /**
     * 账户余额充值
     *
     * @param topUpDto 充值参数
     */
    void topUp(@Valid @NotNull BalanceDto topUpDto);

    /**
     * 账户余额扣除
     *
     * @param deductDto 扣除参数
     */
    void deduct(@Valid @NotNull BalanceDto deductDto);

    /**
     * 账户余额查询
     *
     * @param queryDto 查询参数
     * @return 账户余额
     */
    BalanceBo query(@Valid @NotNull BalanceQueryDto queryDto);

    /**
     * 初始化账户余额
     *
     * @param accountId 账户ID
     */
    void initAccountBalance(@NotNull Integer accountId);

    /**
     * 初始化电表余额
     *
     * @param electricMeterId 电表ID
     * @param accountId       账户ID
     */
    void initElectricMeterBalance(@NotNull Integer electricMeterId, @NotNull Integer accountId);

    /**
     * 删除账户余额
     *
     * @param deleteDto 删除参数
     */
    void deleteBalance(@Valid @NotNull BalanceDeleteDto deleteDto);
}