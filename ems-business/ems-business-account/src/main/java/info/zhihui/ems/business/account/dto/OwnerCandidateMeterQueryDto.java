package info.zhihui.ems.business.account.dto;

import info.zhihui.ems.common.enums.OwnerTypeEnum;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 主体候选电表查询条件
 */
@Data
@Accessors(chain = true)
public class OwnerCandidateMeterQueryDto {

    /**
     * 主体类型
     */
    @NotNull
    private OwnerTypeEnum ownerType;

    /**
     * 主体ID
     */
    @NotNull
    private Integer ownerId;

    /**
     * 租赁空间名称模糊查询
     */
    @Size(max = 100)
    private String spaceNameLike;
}
