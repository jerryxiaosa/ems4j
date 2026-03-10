package info.zhihui.ems.business.order.dto.creation;

import info.zhihui.ems.common.enums.OwnerTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 订单主表主体快照。
 */
@Data
@Accessors(chain = true)
public class OrderOwnerSnapshotDto {

    /**
     * 账户ID。
     */
    private Integer accountId;

    /**
     * 账户归属者ID。
     */
    private Integer ownerId;

    /**
     * 账户归属者类型。
     */
    private OwnerTypeEnum ownerType;

    /**
     * 账户归属者名称。
     */
    private String ownerName;
}
