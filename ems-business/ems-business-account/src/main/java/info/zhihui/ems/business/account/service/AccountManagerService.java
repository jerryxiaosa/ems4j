package info.zhihui.ems.business.account.service;

import info.zhihui.ems.business.account.dto.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * 账户业务管理服务
 *
 * @author jerryxiaosa
 */
public interface AccountManagerService {
    /**
     *  开户
     * @param openAccountDto 开户数据
     */
    Integer openAccount(@Valid @NotNull OpenAccountDto openAccountDto);

    /**
     * 追加电表开户
     * @param accountMetersOpenDto 追加绑定数据
     */
    void appendMeters(@Valid @NotNull AccountMetersOpenDto accountMetersOpenDto);

    /**
     *  销户
     * @param cancelAccountDto 销户数据
     * @return 销户结果
     */
    CancelAccountResponseDto cancelAccount(@Valid @NotNull CancelAccountDto cancelAccountDto);

    /**
     * 更新账户配置
     * @param accountConfigUpdateDto 配置信息
     */
    void updateAccountConfig(@NotNull @Valid AccountConfigUpdateDto accountConfigUpdateDto);
}
