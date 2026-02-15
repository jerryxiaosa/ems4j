package info.zhihui.ems.business.account.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 账户空间退租请求
 */
@Data
@Accessors(chain = true)
public class AccountSpaceUnrentDto {

    /**
     * 账户ID
     */
    @NotNull(message = "账户ID不能为空")
    private Integer accountId;

    /**
     * 空间ID列表
     */
    @NotEmpty(message = "空间ID列表不能为空")
    private List<@NotNull(message = "空间ID不能为空") Integer> spaceIds;
}
