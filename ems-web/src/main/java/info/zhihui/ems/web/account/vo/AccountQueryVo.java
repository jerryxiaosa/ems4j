package info.zhihui.ems.web.account.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

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

    @Schema(description = "单一归属ID")
    private Integer ownerId;

    @Schema(description = "归属ID集合")
    private List<Integer> ownerIds;

    @Schema(description = "电费计费类型编码，参考 electricAccountType")
    private Integer electricAccountType;

    @Schema(description = "预警方案ID")
    private Integer warnPlanId;
}
