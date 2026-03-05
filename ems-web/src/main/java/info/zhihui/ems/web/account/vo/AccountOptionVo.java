package info.zhihui.ems.web.account.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 账户下拉选项
 */
@Data
@Accessors(chain = true)
@Schema(name = "AccountOptionVo", description = "账户下拉选项")
public class AccountOptionVo {

    @Schema(description = "账户ID")
    private Integer id;

    @Schema(description = "账户类型，参考 ownerType")
    private Integer ownerType;

    @Schema(description = "账户归属者ID")
    private Integer ownerId;

    @Schema(description = "账户归属者名称")
    private String ownerName;

    @Schema(description = "联系人")
    private String contactName;

    @Schema(description = "联系方式")
    private String contactPhone;
}
