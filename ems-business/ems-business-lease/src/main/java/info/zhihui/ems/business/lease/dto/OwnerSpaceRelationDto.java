package info.zhihui.ems.business.lease.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 主体空间租赁关系查询结果
 */
@Data
@Accessors(chain = true)
public class OwnerSpaceRelationDto {

    /**
     * 主体类型（见 OwnerTypeEnum code）
     */
    private Integer ownerType;

    /**
     * 主体ID
     */
    private Integer ownerId;

    /**
     * 空间ID
     */
    private Integer spaceId;
}
