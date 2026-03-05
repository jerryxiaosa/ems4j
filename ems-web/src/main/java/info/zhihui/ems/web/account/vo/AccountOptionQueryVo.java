package info.zhihui.ems.web.account.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 账户下拉查询条件
 */
@Data
@Accessors(chain = true)
@Schema(name = "AccountOptionQueryVo", description = "账户下拉查询条件")
public class AccountOptionQueryVo {

    @Schema(description = "账户类型编码，参考 ownerType")
    private Integer ownerType;

    @Size(max = 100)
    @Schema(description = "账户归属名称模糊搜索（直接匹配账户owner_name）")
    private String ownerNameLike;

    @Min(1)
    @Max(200)
    @NotNull
    @Schema(description = "返回数量限制", defaultValue = "20")
    private Integer limit = 20;
}
