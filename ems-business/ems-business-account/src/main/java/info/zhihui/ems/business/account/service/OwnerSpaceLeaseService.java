package info.zhihui.ems.business.account.service;

import info.zhihui.ems.business.account.dto.OwnerSpaceRentDto;
import info.zhihui.ems.business.account.dto.OwnerSpaceUnrentDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * 主体空间租赁服务
 */
public interface OwnerSpaceLeaseService {

    /**
     * 批量租赁空间
     *
     * @param rentDto 租赁参数
     */
    void rentSpaces(@NotNull @Valid OwnerSpaceRentDto rentDto);

    /**
     * 批量退租空间
     *
     * @param unrentDto 退租参数
     */
    void unrentSpaces(@NotNull @Valid OwnerSpaceUnrentDto unrentDto);
}
