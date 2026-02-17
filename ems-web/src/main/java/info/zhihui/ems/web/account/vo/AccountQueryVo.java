package info.zhihui.ems.web.account.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 账户查询 VO
 */
@Data
@Schema(name = "AccountQueryVo", description = "账户查询条件")
public class AccountQueryVo {

    @Schema(description = "是否包含已删除账户")
    private Boolean includeDeleted;

    @Schema(description = "账户类型编码，参考 ownerType")
    private Integer ownerType;

    @Size(max = 100, message = "账户名称模糊搜索长度不能超过100")
    @Schema(description = "账户名称模糊搜索（通过组织名称匹配归属ID）")
    private String ownerNameLike;

    @Schema(description = "电费计费类型编码，参考 electricAccountType")
    private Integer electricAccountType;

    @Schema(description = "预警方案ID")
    private Integer warnPlanId;
}
