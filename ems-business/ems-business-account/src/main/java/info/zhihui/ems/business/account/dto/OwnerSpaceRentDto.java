package info.zhihui.ems.business.account.dto;

import info.zhihui.ems.common.enums.OwnerTypeEnum;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 主体空间租赁请求
 */
@Data
@Accessors(chain = true)
public class OwnerSpaceRentDto {

    /**
     * 主体类型
     */
    @NotNull(message = "主体类型不能为空")
    private OwnerTypeEnum ownerType;

    /**
     * 主体ID
     */
    @NotNull(message = "主体ID不能为空")
    private Integer ownerId;

    /**
     * 空间ID列表
     */
    @NotEmpty(message = "空间ID列表不能为空")
    private List<@NotNull(message = "空间ID不能为空") Integer> spaceIds;
}
