package info.zhihui.ems.business.account.qo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Collection;

/**
 * 主体空间租赁关系查询条件
 */
@Data
@Accessors(chain = true)
public class OwnerSpaceRelQueryQo {

    /**
     * 主体类型
     */
    private Integer ownerType;

    /**
     * 主体ID
     */
    private Integer ownerId;

    /**
     * 空间ID集合
     */
    private Collection<Integer> spaceIds;
}

