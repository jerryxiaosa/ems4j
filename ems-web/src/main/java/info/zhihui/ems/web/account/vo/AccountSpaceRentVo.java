package info.zhihui.ems.web.account.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 账户空间租赁请求
 */
@Data
@Accessors(chain = true)
@Schema(name = "AccountSpaceRentVo", description = "账户空间租赁请求")
public class AccountSpaceRentVo {

    @NotEmpty(message = "空间ID列表不能为空")
    @Schema(description = "空间ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<@NotNull(message = "空间ID不能为空") Integer> spaceIds;
}
