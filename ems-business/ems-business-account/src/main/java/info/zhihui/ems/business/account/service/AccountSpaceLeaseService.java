package info.zhihui.ems.business.account.service;

import info.zhihui.ems.business.account.dto.AccountSpaceRentDto;
import info.zhihui.ems.business.account.dto.AccountSpaceUnrentDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * 账户空间租赁服务
 */
public interface AccountSpaceLeaseService {

    /**
     * 批量租赁空间
     *
     * @param rentDto 租赁参数
     */
    void rentSpaces(@NotNull @Valid AccountSpaceRentDto rentDto);

    /**
     * 批量退租空间
     *
     * @param unrentDto 退租参数
     */
    void unrentSpaces(@NotNull @Valid AccountSpaceUnrentDto unrentDto);
}
