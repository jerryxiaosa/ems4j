package info.zhihui.ems.business.lease.service;

import info.zhihui.ems.business.lease.dto.OwnerSpaceRelationDto;
import jakarta.validation.constraints.NotEmpty;

import java.util.Collection;
import java.util.List;

/**
 * 主体空间租赁关系查询服务
 */
public interface OwnerSpaceRelationQueryService {

    /**
     * 根据主体类型集合与主体ID集合查询租赁关系。
     *
     * @param ownerTypes 主体类型集合
     * @param ownerIds 主体ID集合
     * @return 租赁关系列表
     */
    List<OwnerSpaceRelationDto> findRelationListByOwnerTypesAndOwnerIds(@NotEmpty Collection<Integer> ownerTypes,
                                                                        @NotEmpty Collection<Integer> ownerIds);
}
