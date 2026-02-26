package info.zhihui.ems.business.account.dto;

import info.zhihui.ems.common.enums.OwnerTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 账户归属主体信息
 */
@Data
@Accessors(chain = true)
public class AccountOwnerInfoDto {

    /**
     * 账户ID
     */
    private Integer accountId;

    /**
     * 主体类型
     */
    private OwnerTypeEnum ownerType;

    /**
     * 主体ID
     */
    private Integer ownerId;
}

